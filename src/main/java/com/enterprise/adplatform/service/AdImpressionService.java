package com.enterprise.adplatform.service;

import com.enterprise.adplatform.dto.AdImpressionRequest;
import com.enterprise.adplatform.dto.AdImpressionResponse;

import java.util.List;

public interface AdImpressionService {

    AdImpressionResponse create(AdImpressionRequest request);

    AdImpressionResponse findById(String id);

    List<AdImpressionResponse> findAll();

    AdImpressionResponse update(String id, AdImpressionRequest request);

    void delete(String id);
}
