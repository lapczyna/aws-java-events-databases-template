package com.enterprise.adplatform.mapper;

import com.enterprise.adplatform.dto.CampaignRequest;
import com.enterprise.adplatform.dto.CampaignResponse;
import com.enterprise.adplatform.entity.Campaign;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CampaignMapper {

    Campaign toEntity(CampaignRequest request);

    CampaignResponse toResponse(Campaign campaign);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(CampaignRequest request, @MappingTarget Campaign campaign);
}
