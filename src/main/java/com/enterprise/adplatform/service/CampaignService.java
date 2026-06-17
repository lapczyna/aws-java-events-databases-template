package com.enterprise.adplatform.service;

import com.enterprise.adplatform.dto.CampaignRequest;
import com.enterprise.adplatform.dto.CampaignResponse;

import java.util.List;

public interface CampaignService {

    CampaignResponse create(CampaignRequest request);

    List<CampaignResponse> findAll();

    CampaignResponse findById(Long id);

    CampaignResponse update(Long id, CampaignRequest request);

    void delete(Long id);
}
