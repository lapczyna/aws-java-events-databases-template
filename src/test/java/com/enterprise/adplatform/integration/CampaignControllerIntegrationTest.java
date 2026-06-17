package com.enterprise.adplatform.integration;

import com.enterprise.adplatform.dto.CampaignRequest;
import com.enterprise.adplatform.entity.Campaign;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Campaign Controller Integration Tests")
class CampaignControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private static Long createdId;

    private CampaignRequest validRequest() {
        return CampaignRequest.builder()
                .advertiserId(1L)
                .campaignName("Integration Test Campaign")
                .campaignType(Campaign.CampaignType.DISPLAY)
                .budget(new BigDecimal("5000.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(2))
                .status(Campaign.CampaignStatus.DRAFT)
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("POST /api/v1/campaigns creates campaign and returns 201")
    void createCampaign_returns201() throws Exception {
        String body = objectMapper.writeValueAsString(validRequest());

        String response = mockMvc.perform(post("/api/v1/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.campaignName").value("Integration Test Campaign"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        createdId = objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/v1/campaigns returns list with at least one campaign")
    void getAllCampaigns_returnsList() throws Exception {
        mockMvc.perform(get("/api/v1/campaigns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/v1/campaigns/{id} returns created campaign")
    void getCampaignById_returnsCorrectCampaign() throws Exception {
        mockMvc.perform(get("/api/v1/campaigns/{id}", createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdId))
                .andExpect(jsonPath("$.campaignName").value("Integration Test Campaign"));
    }

    @Test
    @Order(4)
    @DisplayName("PUT /api/v1/campaigns/{id} updates campaign and returns 200")
    void updateCampaign_returns200() throws Exception {
        CampaignRequest updateRequest = validRequest();
        updateRequest.setCampaignName("Updated Campaign Name");
        updateRequest.setStatus(Campaign.CampaignStatus.ACTIVE);

        mockMvc.perform(put("/api/v1/campaigns/{id}", createdId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campaignName").value("Updated Campaign Name"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @Order(5)
    @DisplayName("DELETE /api/v1/campaigns/{id} deletes campaign and returns 204")
    void deleteCampaign_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/campaigns/{id}", createdId))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(6)
    @DisplayName("GET /api/v1/campaigns/{id} returns 404 for deleted campaign")
    void getCampaignById_deleted_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/campaigns/{id}", createdId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @Order(7)
    @DisplayName("POST /api/v1/campaigns with invalid body returns 400 with field errors")
    void createCampaign_invalidRequest_returns400() throws Exception {
        String invalidBody = """
                {
                  "campaignName": "",
                  "budget": -100
                }
                """;
        mockMvc.perform(post("/api/v1/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors", hasSize(greaterThanOrEqualTo(1))));
    }
}
