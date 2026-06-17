Feature: Campaign Management
  As an advertiser
  I want to manage advertising campaigns
  So that I can control my ad spend and reach

  Background:
    Given the ad platform is running

  Scenario: Create a campaign successfully
    When I create a campaign with the following details:
      | advertiserId | campaignName       | campaignType | budget   | status |
      | 1            | BDD Test Campaign  | DISPLAY      | 15000.00 | DRAFT  |
    Then the campaign is created with status 201
    And the response contains campaign name "BDD Test Campaign"
    And the response contains status "DRAFT"

  Scenario: Retrieve a campaign successfully
    Given a campaign exists with name "Existing BDD Campaign" and advertiser 2
    When I request the campaign by its id
    Then the campaign is retrieved with status 200
    And the response contains campaign name "Existing BDD Campaign"

  Scenario: Update a campaign successfully
    Given a campaign exists with name "BDD Update Test" and advertiser 3
    When I update the campaign status to "ACTIVE"
    Then the campaign is updated with status 200
    And the response contains status "ACTIVE"

  Scenario: Delete a campaign successfully
    Given a campaign exists with name "BDD Delete Test" and advertiser 4
    When I delete the campaign by its id
    Then the campaign is deleted with status 204
    And the campaign no longer exists

  Scenario: Create a campaign with invalid data returns 400
    When I create a campaign with the following details:
      | advertiserId | campaignName | campaignType | budget | status |
      |              |              | DISPLAY      | -100   | DRAFT  |
    Then the response status is 400
    And the response contains field errors
