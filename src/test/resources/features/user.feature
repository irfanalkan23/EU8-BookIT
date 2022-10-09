@smoke
Feature: User Verification


  Scenario: verify information about logged user
    Given I logged Bookit api using "wcanadinea@ihg.com" and "waverleycanadine"
    When I get the current user information from api
    Then status code should be 200

  @db
  Scenario: verify information about logged user from api and database
    Given I logged Bookit api using "wcanadinea@ihg.com" and "waverleycanadine"
    When I get the current user information from api
    Then the information about current user from api and database should match

  @db @ui
  Scenario: three point verification (UI,API,Database)
    Given user logs in using "wcanadinea@ihg.com" "waverleycanadine"
    And  user is on the my self page
    Given I logged Bookit api using "wcanadinea@ihg.com" and "waverleycanadine"
    When I get the current user information from api
    Then UI,API and Database user information must be match

  @db @ui
  Scenario Outline: three point verification (UI,API,Database) DDT
    Given user logs in using "<email>" "<password>"
    And  user is on the my self page
    Given I logged Bookit api using "<email>" and "<password>"
    When I get the current user information from api
    Then UI,API and Database user information must be match

    Examples:
      | email              | password         |
      | wcanadinea@ihg.com | waverleycanadine |
#      | nshearsby7w@uiuc.edu | lorettebradnum |
