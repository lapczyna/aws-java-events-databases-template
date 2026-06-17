package com.enterprise.adplatform.controller;

import com.enterprise.adplatform.dto.AdImpressionRequest;
import com.enterprise.adplatform.dto.AdImpressionResponse;
import com.enterprise.adplatform.service.AdImpressionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/impressions")
@RequiredArgsConstructor
@Slf4j
public class AdImpressionController {

    private final AdImpressionService adImpressionService;

    @PostMapping
    public ResponseEntity<AdImpressionResponse> createImpression(@Valid @RequestBody AdImpressionRequest request) {
        log.info("POST /api/v1/impressions");
        AdImpressionResponse response = adImpressionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AdImpressionResponse>> getAllImpressions() {
        log.info("GET /api/v1/impressions");
        return ResponseEntity.ok(adImpressionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdImpressionResponse> getImpression(@PathVariable String id) {
        log.info("GET /api/v1/impressions/{}", id);
        return ResponseEntity.ok(adImpressionService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdImpressionResponse> updateImpression(
            @PathVariable String id,
            @Valid @RequestBody AdImpressionRequest request) {
        log.info("PUT /api/v1/impressions/{}", id);
        return ResponseEntity.ok(adImpressionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImpression(@PathVariable String id) {
        log.info("DELETE /api/v1/impressions/{}", id);
        adImpressionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
