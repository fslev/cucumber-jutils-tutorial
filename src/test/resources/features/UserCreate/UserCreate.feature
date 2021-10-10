@all @create
@local @prod
Feature: Create User feature

  Scenario Template: Create user with valid data and check for correct response
    Given var expectedCreateUserResponse=
    """
    {
      "status": 201,
      "body": {
         "name": "<name>",
         "job": "<job>",
         "id": "[0-9]*",
         "createdAt": ".*"
      }
    }
    """
    # login and compare response (if comparison passes, token is automatically set inside scenario properties)
    When Login with email=eve.holt@reqres.in, password=cityslicka and check response={"status": 200, "body": {"token": "~[token]"}}
    # token is set as "authorization" header for Create user API
    Then Create user with name=<name>, job=<job> and check response=#[expectedCreateUserResponse]
    When var expectedCreateUserNegativeResponse=
    """
    {
      "status": 201,
      "body": {
         "name": "Wrong",
         "job": "<job>",
         "id": "[0-9]*",
         "createdAt": ".*"
      }
    }
    """
    Then Create user with name=<name>, job=<job> and check response!=#[expectedCreateUserNegativeResponse]
    Examples:
      | name   | job     |
      | florin | tester  |
      | john   | blogger |

  @serial_group1
  Scenario: Create user with valid data and check for correct response from file
  Same scenario as above, but define 'expectedCreateUserResponse' scenario property inside file
    * load vars from dir "UserCreate/scene1"
    When Login with email=eve.holt@reqres.in, password=cityslicka and check response={"status": 200, "body": {"token": "~[token]"}}
    Then Create user with name=David Jones, job=pirate and check response=#[expectedCreateUserResponse]