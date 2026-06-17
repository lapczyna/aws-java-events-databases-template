package com.enterprise.adplatform.controller;

import com.enterprise.adplatform.dto.CampaignRequest;
import com.enterprise.adplatform.dto.CampaignResponse;
import com.enterprise.adplatform.service.CampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/campaigns")
@RequiredArgsConstructor
@Slf4j
public class CampaignController {

    private final CampaignService campaignService;

    @PostMapping
    public ResponseEntity<CampaignResponse> createCampaign(@Valid @RequestBody CampaignRequest request) {
        log.info("POST /api/v1/campaigns");
        CampaignResponse response = campaignService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CampaignResponse>> getAllCampaigns() {
        log.info("GET /api/v1/campaigns");
        return ResponseEntity.ok(campaignService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampaignResponse> getCampaign(@PathVariable Long id) {
        log.info("GET /api/v1/campaigns/{}", id);
        return ResponseEntity.ok(campaignService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CampaignResponse> updateCampaign(
            @PathVariable Long id,
            @Valid @RequestBody CampaignRequest request) {
        log.info("PUT /api/v1/campaigns/{}", id);
        return ResponseEntity.ok(campaignService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCampaign(@PathVariable Long id) {
        log.info("DELETE /api/v1/campaigns/{}", id);
        campaignService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
