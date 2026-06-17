package com.enterprise.adplatform.unit.mapper;

import com.enterprise.adplatform.dto.CampaignRequest;
import com.enterprise.adplatform.dto.CampaignResponse;
import com.enterprise.adplatform.entity.Campaign;
import com.enterprise.adplatform.mapper.CampaignMapperImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CampaignMapper unit tests")
class CampaignMapperTest {

    private final CampaignMapperImpl mapper = new CampaignMapperImpl();

    @Test
    @DisplayName("toEntity maps all fields correctly")
    void toEntity_mapsAllFields() {
        CampaignRequest request = CampaignRequest.builder()
                .advertiserId(42L)
                .campaignName("Mapper Test Campaign")
                .campaignType(Campaign.CampaignType.VIDEO)
                .budget(new BigDecimal("9999.99"))
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 6, 30))
                .status(Campaign.CampaignStatus.DRAFT)
                .build();

        Campaign entity = mapper.toEntity(request);

        assertThat(entity.getAdvertiserId()).isEqualTo(42L);
        assertThat(entity.getCampaignName()).isEqualTo("Mapper Test Campaign");
        assertThat(entity.getCampaignType()).isEqualTo(Campaign.CampaignType.VIDEO);
        assertThat(entity.getBudget()).isEqualByComparingTo("9999.99");
        assertThat(entity.getStartDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(entity.getEndDate()).isEqualTo(LocalDate.of(2025, 6, 30));
        assertThat(entity.getStatus()).isEqualTo(Campaign.CampaignStatus.DRAFT);
        assertThat(entity.getId()).isNull();
    }

    @Test
    @DisplayName("toResponse maps all fields correctly")
    void toResponse_mapsAllFields() {
        Campaign campaign = Campaign.builder()
                .id(7L)
                .advertiserId(42L)
                .campaignName("Response Mapper Test")
                .campaignType(Campaign.CampaignType.SEARCH)
                .budget(new BigDecimal("5000.00"))
                .startDate(LocalDate.of(2025, 3, 1))
                .endDate(LocalDate.of(2025, 9, 1))
                .status(Campaign.CampaignStatus.ACTIVE)
                .createdAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                .updatedAt(LocalDateTime.of(2025, 1, 2, 12, 0))
                .build();

        CampaignResponse response = mapper.toResponse(campaign);

        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getAdvertiserId()).isEqualTo(42L);
        assertThat(response.getCampaignName()).isEqualTo("Response Mapper Test");
        assertThat(response.getCampaignType()).isEqualTo(Campaign.CampaignType.SEARCH);
        assertThat(response.getBudget()).isEqualByComparingTo("5000.00");
        assertThat(response.getStatus()).isEqualTo(Campaign.CampaignStatus.ACTIVE);
        assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2025, 1, 1, 0, 0));
    }

    @Test
    @DisplayName("updateEntityFromRequest ignores null fields")
    void updateEntityFromRequest_ignoresNullFields() {
        Campaign existing = Campaign.builder()
                .id(1L)
                .campaignName("Original Name")
                .budget(new BigDecimal("1000.00"))
                .status(Campaign.CampaignStatus.ACTIVE)
                .build();

        CampaignRequest partial = CampaignRequest.builder()
                .advertiserId(1L)
                .campaignName(null)
                .campaignType(Campaign.CampaignType.DISPLAY)
                .budget(new BigDecimal("2000.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .status(Campaign.CampaignStatus.PAUSED)
                .build();

        mapper.updateEntityFromRequest(partial, existing);

        assertThat(existing.getCampaignName()).isEqualTo("Original Name");
        assertThat(existing.getBudget()).isEqualByComparingTo("2000.00");
        assertThat(existing.getStatus()).isEqualTo(Campaign.CampaignStatus.PAUSED);
    }
}
