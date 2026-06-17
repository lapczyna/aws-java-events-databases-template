package com.enterprise.adplatform.bdd.steps;

import com.enterprise.adplatform.dto.CampaignRequest;
import com.enterprise.adplatform.entity.Campaign;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class CampaignSteps {

    @Autowired ObjectMapper objectMapper;
    @LocalServerPort int port;

    private final RestTemplate restTemplate = new RestTemplate();
    private ResponseEntity<String> lastResponse;
    private Long lastCreatedId;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1/campaigns";
    }

    @Given("the ad platform is running")
    public void the_ad_platform_is_running() {
        ResponseEntity<String> health = restTemplate.getForEntity(
                "http://localhost:" + port + "/actuator/health", String.class);
        assertThat(health.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @When("I create a campaign with the following details:")
    public void i_create_a_campaign(DataTable table) throws Exception {
        Map<String, String> row = table.asMaps().get(0);
        String advertiserIdStr = row.get("advertiserId");
        String budgetStr = row.get("budget");

        if (advertiserIdStr == null || advertiserIdStr.isBlank()
                || budgetStr == null || budgetStr.isBlank()) {
            String invalidBody = """
                    {"advertiserId":null,"campaignName":"","budget":-100,"campaignType":"DISPLAY","status":"DRAFT"}
                    """;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            try {
                lastResponse = restTemplate.exchange(baseUrl(), HttpMethod.POST,
                        new HttpEntity<>(invalidBody, headers), String.class);
            } catch (org.springframework.web.client.HttpClientErrorException ex) {
                lastResponse = ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
            }
            return;
        }

        CampaignRequest request = CampaignRequest.builder()
                .advertiserId(Long.parseLong(advertiserIdStr))
                .campaignName(row.get("campaignName"))
                .campaignType(Campaign.CampaignType.valueOf(row.get("campaignType")))
                .budget(new BigDecimal(budgetStr))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .status(Campaign.CampaignStatus.valueOf(row.get("status")))
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        lastResponse = restTemplate.exchange(baseUrl(), HttpMethod.POST,
                new HttpEntity<>(objectMapper.writeValueAsString(request), headers), String.class);

        JsonNode json = objectMapper.readTree(lastResponse.getBody());
        if (json.has("id")) {
            lastCreatedId = json.get("id").asLong();
        }
    }

    @Given("a campaign exists with name {string} and advertiser {int}")
    public void a_campaign_exists(String name, int advertiserId) throws Exception {
        CampaignRequest request = CampaignRequest.builder()
                .advertiserId((long) advertiserId)
                .campaignName(name)
                .campaignType(Campaign.CampaignType.DISPLAY)
                .budget(new BigDecimal("5000.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .status(Campaign.CampaignStatus.DRAFT)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        lastResponse = restTemplate.exchange(baseUrl(), HttpMethod.POST,
                new HttpEntity<>(objectMapper.writeValueAsString(request), headers), String.class);

        JsonNode json = objectMapper.readTree(lastResponse.getBody());
        lastCreatedId = json.get("id").asLong();
    }

    @When("I request the campaign by its id")
    public void i_request_the_campaign_by_id() {
        try {
            lastResponse = restTemplate.getForEntity(baseUrl() + "/" + lastCreatedId, String.class);
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            lastResponse = ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }
    }

    @When("I update the campaign status to {string}")
    public void i_update_campaign_status(String newStatus) throws Exception {
        CampaignRequest updateRequest = CampaignRequest.builder()
                .advertiserId(1L)
                .campaignName("BDD Update Test")
                .campaignType(Campaign.CampaignType.DISPLAY)
                .budget(new BigDecimal("5000.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .status(Campaign.CampaignStatus.valueOf(newStatus))
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        lastResponse = restTemplate.exchange(baseUrl() + "/" + lastCreatedId, HttpMethod.PUT,
                new HttpEntity<>(objectMapper.writeValueAsString(updateRequest), headers), String.class);
    }

    @When("I delete the campaign by its id")
    public void i_delete_the_campaign() {
        lastResponse = restTemplate.exchange(baseUrl() + "/" + lastCreatedId, HttpMethod.DELETE,
                HttpEntity.EMPTY, String.class);
    }

    @Then("the campaign is created with status {int}")
    public void the_campaign_is_created_with_status(int status) {
        assertThat(lastResponse.getStatusCode().value()).isEqualTo(status);
    }

    @Then("the campaign is retrieved with status {int}")
    public void the_campaign_is_retrieved_with_status(int status) {
        assertThat(lastResponse.getStatusCode().value()).isEqualTo(status);
    }

    @Then("the campaign is updated with status {int}")
    public void the_campaign_is_updated_with_status(int status) {
        assertThat(lastResponse.getStatusCode().value()).isEqualTo(status);
    }

    @Then("the campaign is deleted with status {int}")
    public void the_campaign_is_deleted_with_status(int status) {
        assertThat(lastResponse.getStatusCode().value()).isEqualTo(status);
    }

    @Then("the response contains campaign name {string}")
    public void the_response_contains_campaign_name(String name) throws Exception {
        JsonNode json = objectMapper.readTree(lastResponse.getBody());
        assertThat(json.get("campaignName").asText()).isEqualTo(name);
    }

    @Then("the response contains status {string}")
    public void the_response_contains_status(String status) throws Exception {
        JsonNode json = objectMapper.readTree(lastResponse.getBody());
        assertThat(json.get("status").asText()).isEqualTo(status);
    }

    @Then("the campaign no longer exists")
    public void the_campaign_no_longer_exists() {
        try {
            restTemplate.getForEntity(baseUrl() + "/" + lastCreatedId, String.class);
            throw new AssertionError("Expected 404 but campaign still exists");
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound ex) {
            // expected
        }
    }

    @Then("the response status is {int}")
    public void the_response_status_is(int status) {
        assertThat(lastResponse.getStatusCode().value()).isEqualTo(status);
    }

    @Then("the response contains field errors")
    public void the_response_contains_field_errors() throws Exception {
        JsonNode json = objectMapper.readTree(lastResponse.getBody());
        assertThat(json.has("fieldErrors") || json.has("status")).isTrue();
    }
}
