package com.enterprise.adplatform.service;

import com.enterprise.adplatform.dto.CampaignRequest;
import com.enterprise.adplatform.dto.CampaignResponse;
import com.enterprise.adplatform.entity.Campaign;
import com.enterprise.adplatform.exception.ResourceNotFoundException;
import com.enterprise.adplatform.mapper.CampaignMapper;
import com.enterprise.adplatform.messaging.sqs.SqsProducerService;
import com.enterprise.adplatform.messaging.sqs.event.CampaignCreatedEvent;
import com.enterprise.adplatform.messaging.sqs.event.CampaignPausedEvent;
import com.enterprise.adplatform.messaging.kafka.KafkaProducerService;
import com.enterprise.adplatform.messaging.kafka.event.CampaignEvent;
import com.enterprise.adplatform.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CampaignServiceImpl implements CampaignService {

    private final CampaignRepository campaignRepository;
    private final CampaignMapper campaignMapper;
    private final SqsProducerService sqsProducerService;
    private final KafkaProducerService kafkaProducerService;

    @Override
    @Transactional
    public CampaignResponse create(CampaignRequest request) {
        log.info("Creating campaign name={} advertiserId={}", request.getCampaignName(), request.getAdvertiserId());
        Campaign campaign = campaignMapper.toEntity(request);
        Campaign saved = campaignRepository.save(campaign);

        CampaignCreatedEvent sqsEvent = CampaignCreatedEvent.builder()
                .campaignId(saved.getId().toString())
                .campaignName(saved.getCampaignName())
                .advertiserId(saved.getAdvertiserId().toString())
                .occurredAt(Instant.now())
                .build();
        sqsProducerService.sendCampaignCreated(sqsEvent);

        CampaignEvent kafkaEvent = CampaignEvent.builder()
                .eventType("CAMPAIGN_CREATED")
                .campaignId(saved.getId().toString())
                .campaignName(saved.getCampaignName())
                .occurredAt(Instant.now())
                .build();
        kafkaProducerService.sendCampaignEvent(kafkaEvent);

        log.info("Campaign created id={}", saved.getId());
        return campaignMapper.toResponse(saved);
    }

    @Override
    public List<CampaignResponse> findAll() {
        log.debug("Fetching all campaigns");
        return campaignRepository.findAll().stream()
                .map(campaignMapper::toResponse)
                .toList();
    }

    @Override
    public CampaignResponse findById(Long id) {
        log.debug("Fetching campaign id={}", id);
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", "id", id));
        return campaignMapper.toResponse(campaign);
    }

    @Override
    @Transactional
    public CampaignResponse update(Long id, CampaignRequest request) {
        log.info("Updating campaign id={}", id);
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", "id", id));

        boolean wasPaused = campaign.getStatus() != request.getStatus()
                && request.getStatus() == Campaign.CampaignStatus.PAUSED;

        campaignMapper.updateEntityFromRequest(request, campaign);
        Campaign updated = campaignRepository.save(campaign);

        if (wasPaused) {
            CampaignPausedEvent pausedEvent = CampaignPausedEvent.builder()
                    .campaignId(updated.getId().toString())
                    .campaignName(updated.getCampaignName())
                    .occurredAt(Instant.now())
                    .build();
            sqsProducerService.sendCampaignPaused(pausedEvent);
        }

        return campaignMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting campaign id={}", id);
        if (!campaignRepository.existsById(id)) {
            throw new ResourceNotFoundException("Campaign", "id", id);
        }
        campaignRepository.deleteById(id);
    }
}
