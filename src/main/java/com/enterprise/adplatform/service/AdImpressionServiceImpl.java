package com.enterprise.adplatform.service;

import com.enterprise.adplatform.dto.AdImpressionRequest;
import com.enterprise.adplatform.dto.AdImpressionResponse;
import com.enterprise.adplatform.exception.ResourceNotFoundException;
import com.enterprise.adplatform.infrastructure.dynamodb.AdImpressionItem;
import com.enterprise.adplatform.mapper.AdImpressionMapper;
import com.enterprise.adplatform.messaging.sqs.SqsProducerService;
import com.enterprise.adplatform.messaging.sqs.event.AdImpressionReceivedEvent;
import com.enterprise.adplatform.persistence.dynamodb.DynamoDbAdImpressionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdImpressionServiceImpl implements AdImpressionService {

    private final DynamoDbAdImpressionRepository impressionRepository;
    private final AdImpressionMapper impressionMapper;
    private final SqsProducerService sqsProducerService;

    @Override
    public AdImpressionResponse create(AdImpressionRequest request) {
        log.info("Recording ad impression campaignId={} placementId={}", request.getCampaignId(), request.getPlacementId());
        AdImpressionItem item = impressionMapper.toItem(request);
        item.setImpressionId(UUID.randomUUID().toString());
        AdImpressionItem saved = impressionRepository.save(item);

        AdImpressionReceivedEvent event = AdImpressionReceivedEvent.builder()
                .impressionId(saved.getImpressionId())
                .campaignId(saved.getCampaignId())
                .placementId(saved.getPlacementId())
                .cost(saved.getCost())
                .occurredAt(Instant.now())
                .build();
        sqsProducerService.sendAdImpressionReceived(event);

        log.info("Ad impression recorded id={}", saved.getImpressionId());
        return impressionMapper.toResponse(saved);
    }

    @Override
    public AdImpressionResponse findById(String id) {
        log.debug("Fetching impression id={}", id);
        AdImpressionItem item = impressionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AdImpressionEvent", "impressionId", id));
        return impressionMapper.toResponse(item);
    }

    @Override
    public List<AdImpressionResponse> findAll() {
        log.debug("Fetching all impressions");
        return impressionRepository.findAll().stream()
                .map(impressionMapper::toResponse)
                .toList();
    }

    @Override
    public AdImpressionResponse update(String id, AdImpressionRequest request) {
        log.info("Updating impression id={}", id);
        AdImpressionItem existing = impressionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AdImpressionEvent", "impressionId", id));

        AdImpressionItem updated = impressionMapper.toItem(request);
        updated.setImpressionId(existing.getImpressionId());
        AdImpressionItem saved = impressionRepository.update(updated);
        return impressionMapper.toResponse(saved);
    }

    @Override
    public void delete(String id) {
        log.info("Deleting impression id={}", id);
        if (!impressionRepository.existsById(id)) {
            throw new ResourceNotFoundException("AdImpressionEvent", "impressionId", id);
        }
        impressionRepository.deleteById(id);
    }
}
