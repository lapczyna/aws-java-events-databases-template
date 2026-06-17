package com.enterprise.adplatform.unit.service;

import com.enterprise.adplatform.dto.CampaignRequest;
import com.enterprise.adplatform.dto.CampaignResponse;
import com.enterprise.adplatform.entity.Campaign;
import com.enterprise.adplatform.exception.ResourceNotFoundException;
import com.enterprise.adplatform.mapper.CampaignMapper;
import com.enterprise.adplatform.messaging.kafka.KafkaProducerService;
import com.enterprise.adplatform.messaging.kafka.event.CampaignEvent;
import com.enterprise.adplatform.messaging.sqs.SqsProducerService;
import com.enterprise.adplatform.messaging.sqs.event.CampaignCreatedEvent;
import com.enterprise.adplatform.repository.CampaignRepository;
import com.enterprise.adplatform.service.CampaignServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CampaignService unit tests")
class CampaignServiceTest {

    @Mock CampaignRepository campaignRepository;
    @Mock CampaignMapper campaignMapper;
    @Mock SqsProducerService sqsProducerService;
    @Mock KafkaProducerService kafkaProducerService;

    @InjectMocks CampaignServiceImpl campaignService;

    private CampaignRequest validRequest;
    private Campaign savedCampaign;
    private CampaignResponse expectedResponse;

    @BeforeEach
    void setUp() {
        validRequest = CampaignRequest.builder()
                .advertiserId(1L)
                .campaignName("Test Campaign")
                .campaignType(Campaign.CampaignType.DISPLAY)
                .budget(new BigDecimal("10000.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .status(Campaign.CampaignStatus.DRAFT)
                .build();

        savedCampaign = Campaign.builder()
                .id(1L)
                .advertiserId(1L)
                .campaignName("Test Campaign")
                .campaignType(Campaign.CampaignType.DISPLAY)
                .budget(new BigDecimal("10000.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .status(Campaign.CampaignStatus.DRAFT)
                .build();

        expectedResponse = CampaignResponse.builder()
                .id(1L)
                .advertiserId(1L)
                .campaignName("Test Campaign")
                .campaignType(Campaign.CampaignType.DISPLAY)
                .budget(new BigDecimal("10000.00"))
                .status(Campaign.CampaignStatus.DRAFT)
                .build();
    }

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("creates campaign, persists, emits SQS and Kafka events")
        void createCampaign_success() {
            when(campaignMapper.toEntity(validRequest)).thenReturn(savedCampaign);
            when(campaignRepository.save(any(Campaign.class))).thenReturn(savedCampaign);
            when(campaignMapper.toResponse(savedCampaign)).thenReturn(expectedResponse);

            CampaignResponse result = campaignService.create(validRequest);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getCampaignName()).isEqualTo("Test Campaign");

            verify(campaignRepository).save(any(Campaign.class));
            verify(sqsProducerService).sendCampaignCreated(any(CampaignCreatedEvent.class));
            verify(kafkaProducerService).sendCampaignEvent(any(CampaignEvent.class));
        }

        @Test
        @DisplayName("SQS event carries correct campaign data")
        void createCampaign_sqsEventHasCorrectData() {
            when(campaignMapper.toEntity(validRequest)).thenReturn(savedCampaign);
            when(campaignRepository.save(any())).thenReturn(savedCampaign);
            when(campaignMapper.toResponse(any())).thenReturn(expectedResponse);

            campaignService.create(validRequest);

            ArgumentCaptor<CampaignCreatedEvent> captor = ArgumentCaptor.forClass(CampaignCreatedEvent.class);
            verify(sqsProducerService).sendCampaignCreated(captor.capture());
            CampaignCreatedEvent captured = captor.getValue();

            assertThat(captured.getCampaignId()).isEqualTo("1");
            assertThat(captured.getCampaignName()).isEqualTo("Test Campaign");
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindByIdTests {

        @Test
        @DisplayName("returns response when campaign exists")
        void findById_existing_returnsResponse() {
            when(campaignRepository.findById(1L)).thenReturn(Optional.of(savedCampaign));
            when(campaignMapper.toResponse(savedCampaign)).thenReturn(expectedResponse);

            CampaignResponse result = campaignService.findById(1L);

            assertThat(result).isEqualTo(expectedResponse);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when campaign does not exist")
        void findById_missing_throwsNotFoundException() {
            when(campaignRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> campaignService.findById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("findAll()")
    class FindAllTests {

        @Test
        @DisplayName("returns mapped list of all campaigns")
        void findAll_returnsMappedList() {
            when(campaignRepository.findAll()).thenReturn(List.of(savedCampaign));
            when(campaignMapper.toResponse(savedCampaign)).thenReturn(expectedResponse);

            List<CampaignResponse> result = campaignService.findAll();

            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(expectedResponse);
        }

        @Test
        @DisplayName("returns empty list when no campaigns exist")
        void findAll_noCampaigns_returnsEmptyList() {
            when(campaignRepository.findAll()).thenReturn(List.of());

            List<CampaignResponse> result = campaignService.findAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("deletes existing campaign")
        void delete_existing_success() {
            when(campaignRepository.existsById(1L)).thenReturn(true);

            campaignService.delete(1L);

            verify(campaignRepository).deleteById(1L);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException for non-existent campaign")
        void delete_missing_throwsNotFoundException() {
            when(campaignRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> campaignService.delete(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(campaignRepository, never()).deleteById(any());
        }
    }
}
