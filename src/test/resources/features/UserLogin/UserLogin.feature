@all @login
@local @prod
Feature: Test Login feature

  Scenario Template: Call login API with invalid data <request> and check for correct error message
    Then Login with requestBody=<request> and check response=<response>
    @serial_group2
    Examples:
      | request                                        | response                                               |
      | { "email": "peter@klaven" }                    | {"status": 400, "body": {"error": "Missing password"}} |
      | { "email": "peter", "password": "cityslicka" } | {"status": 400, "body": {"error": "user not found"}}   |
    @serial_group1
    Examples:
      | request                 | response                                                        |
      | { "password": "12345" } | {"status": 400, "body": {"error": "Missing email or username"}} |
      | []                      | {"status": 400, "body": {"error": "Missing email or username"}} |

  @isolated
  Scenario Template: Call login API with valid username <email> and password and check for correct response
    Then Login with email=<email>, password=<password> and check response=<response>
    Examples:
      | email              | password   | response        |
      | eve.holt@reqres.in | cityslicka | {"status": 200} |