package com.enterprise.adplatform.mapper;

import com.enterprise.adplatform.dto.AdImpressionRequest;
import com.enterprise.adplatform.dto.AdImpressionResponse;
import com.enterprise.adplatform.infrastructure.dynamodb.AdImpressionItem;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdImpressionMapper {

    @Mapping(target = "impressionId", ignore = true)
    AdImpressionItem toItem(AdImpressionRequest request);

    AdImpressionResponse toResponse(AdImpressionItem item);
}
