# Cucumber JUtils tutorial

Cucumber-Utils has been renamed to Cucumber-JUtils  

Here is a small tutorial on how to use [**cucumber-jutils**](https://github.com/fslev/cucumber-utils) library inside a test framework.  
**Test target**: HTTP REST APIs, hosted by [reqres](https://reqres.in/).   

## Summary
[*Reqres*](https://reqres.in/) provides a series of HTTPs REST APIs that accept fake test data and returns a limited set of static responses.  
This tutorial describes how to setup a basic test-framework which executes Cucumber acceptance/integration tests that call these APIs and compares actual responses with expected data.    

You will see some tips and tricks on how to use [**cucumber-jutils**](https://github.com/fslev/cucumber-utils) library, as well on how to use **Cucumber** native *parallelization* feature. This will ease your work as a test engineer / developer.  
This library contains many features such as:
 - easy to use HTTP client  ([**jtest-utils**](https://github.com/fslev/jtest-utils))  
 - database clients ([**jtest-utils**](https://github.com/fslev/jtest-utils))  
 - Mechanisms for comparing HTTP responses, JSONs, XMLs and strings using REGEX patterns ([**jtest-utils**](https://github.com/fslev/jtest-utils))      
 - predefined Cucumber steps for:
   - instantiating Scenario properties (sharing state between steps within a Scenario)  
   - defining and comparing Dates
   - querying and updating databases and compare results
   - loading Scenario properties directly from external resources  
 - etc.  
For more details read about [**cucumber-jutils**](https://github.com/fslev/cucumber-utils) and [**jtest-utils**](https://github.com/fslev/jtest-utils).    

_Finally_, you will learn how to generate test reports with [**maven-cucumber-reporting**](https://github.com/damianszczepanik/maven-cucumber-reporting) plugin.  

* [Test cases](#test-cases)
* [Run tests from Intellij Idea](#run-idea)
* [Run tests from Maven serially or in parallel](#run-maven)
* [Test Reports](#test-reports)

<a name="test-cases"></a>

<a name="run-idea"></a>
## Configure Intellij Idea to run Cucumber feature files 
### Requirements
- __Intellij Idea__ version >= 2019.3
- Latest version of __Cucumber for Java__ and __Gherkin__ plugins

### Cucumber for Java Plugin Configuration
Setup _Glue_ packages and _Program arguments_:
- **Run -> Edit Configurations**:  
  - Clean any "Cucumber java" configuration instances that ran in the past
  - Inside **Templates -> Cucumber java**, setup the followings:
    - **Glue**: _com.cucumber.utils com.cucumber.tutorial_
    - **Program arguments**: _--plugin junit:output_
    - Optional: for parallelization add "--threads 5" at the beginning inside **Program arguments**
    - Rest of the fields, leave them as they are


## Test cases
### Test Login API 
```
POST: /api/login
Body:
{
    "email": "eve.holt@reqres.in",
    "password": "cityslicka"
}
```  

1. Define Cucumber Java step definitions:
```java
@ScenarioScoped
public class LoginSteps extends RestScenario {
    @Inject
    private LoginService loginService;

    @Then("Login with requestBody={} and check response={}")
    public void login(String requestBody, String expected) {
        executeAndCompare(loginService.buildLogin(requestBody), expected);
    }

    @Then("Login with email={}, password={} and check response={}")
    public void login(String email, String password, String expected) {
        executeAndCompare(loginService.buildLogin(email, password), expected);
    }
}
```
where LoginService looks like this:
```java
@ScenarioScoped
public class LoginService extends RestService {

    public static final String PATH = "/api/login";
    public static String REQUEST_BODY_TEMPLATE = "{\"email\": \"#[email]\", \"password\": \"#[password]\"}";

    public HttpClient buildLogin(String email, String pwd) {
        return buildLogin(StringFormat.replaceProps(REQUEST_BODY_TEMPLATE, Map.of("email", email, "password", pwd)));
    }

    public HttpClient buildLogin(String requestBody) {
        return getBuilder(address()).path(PATH).method(Method.POST).entity(requestBody).build();
    }

    protected String address() {
        return scenarioProps.getAsString("reqresin.address");
    }
}
```
  
2. Define the test Gherkin scenarios:  
```gherkin
@all @login
Feature: Test Login feature

  Scenario Template: Call login API with invalid data <request> and check for correct error message
    Then Login with requestBody=<request> and check response=<response>
    Examples:
      | request                                        | response                                                        |
      | { "email": "peter@klaven" }                    | {"status": 400, "body": {"error": "Missing password"}}          |
      | { "email": "peter", "password": "cityslicka" } | {"status": 400, "body": {"error": "user not found"}}            |
      | { "password": "12345" }                        | {"status": 400, "body": {"error": "Missing email or username"}} |
      | []                                             | {"status": 400, "body": {"error": "Missing email or username"}} |


  Scenario Template: Call login API with valid username <email> and password and check for correct response
    Then Login with email=<email>, password=<password> and check response=<response>
    Examples:
      | email              | password   | response        |
      | eve.holt@reqres.in | cityslicka | {"status": 200} |
```

### Test Create User API

```
POST: /api/create
Body:
{
    "name": "morpheus",
    "job": "leader"
}
```  
1. Define Cucumber Java step definitions:
```java
@ScenarioScoped
public class CreateUserSteps extends RestScenario {
    @Inject
    private UserService userService;

    @Then("Create user with name={}, job={} and check response={}")
    public void createUserAndCompare(String name, String job, String expected) {
        executeAndCompare(userService.buildCreate(name, job, scenarioProps.getAsString("token")), expected);
    }

    @Then("Create user with name={}, job={} and check response!={}")
    public void createUserAndCompareNegative(String name, String job, String expected) {
        executeAndCompare(userService.buildCreate(name, job, scenarioProps.getAsString("token")), expected, MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_BODY);
    }

    @Then("Create user with request={} and check response={}")
    public void createUserAndCompare(String request, String expected) {
        executeAndCompare(userService.buildCreate(
                scenarioProps.getAsString("reqresin.address"), request, scenarioProps.getAsString("token")), expected);
    }
}
```
2. Define the Cucumber test scenarios:
```gherkin
@all @create
Feature: Create User feature

  Scenario Template: Create user with valid data and check for correct response
    Given param expectedCreateUserResponse=
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
    When param expectedCreateUserNegativeResponse=
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

  Scenario: Create user with valid data and check for correct response from file
  Same scenario as above, but define 'expectedCreateUserResponse' scenario property inside file
    * load all scenario props from dir "UserCreate/scene1"
    When Login with email=eve.holt@reqres.in, password=cityslicka and check response={"status": 200, "body": {"token": "~[token]"}}
    Then Create user with name=David Jones, job=pirate and check response=#[expectedCreateUserResponse]
```
 
You can see that we used inside the second scenario a pre-defined step from Cucumber-JUtils:
```gherkin
    * load all scenario props from dir "UserCreate/scene1"
```  
By loading values from separate files or directories, we do not burden the Gherkin scenario with bulky Strings representing our expected values. We do this with scenario properties.   
Behind the scenes, Cucumber-JUtils sets new scenario properties, each one having as property name the file name, and as property value the file content.  

Taking the example from above, '#[expectedCreateUserResponse]' represents a scenario property, which has the name of a file (without extension) from 'UserCreate/scene1' directory and its value is actually the content of the file.     
Cucumber-JUtils has a special mechanism for parsing these variables '#[]' present inside the Gherkin steps. It replaces these variables with their values, before passing them to the parameters from the corresponding Java step definition methods.  


## Comparing
In current tutorial project, we compare using the JTest-Utils compare mechanism:  
https://github.com/fslev/jtest-utils/wiki/Compare-mechanisms

## General best practices for writing Cucumber scenarios
- Defined steps should be simple and reusable. Otherwise, you will end up writing both Java code and Gherkin syntax for each scenario  
- One step should do two things: call an API and compare response  
- Log scenario steps (Ex: log API call details; log compared values)  
- Use helper methods as much as possible (Ex: a single method which calls an API, compares the response and also logs the whole thing)   

<a name="run-maven"></a>
## Run Cucumber tests with Maven in serial or parallel mode
_Maven command_:  
mvn clean verify -P{environment} (optional -Dtags=@foo -Dconcurrent=true)
```
mvn clean verify -Pprod -Dtags=@all -Dconcurrent=true
```   

<a name="test-reports"></a>
## Cucumber Test Report
The report is generated in HTML format inside target/cucumber-html-reports:  

![Features overview](https://github.com/fslev/cucumber-utils-tutorial/blob/master/reports/1a.png)
![Scenario overview](https://github.com/fslev/cucumber-utils-tutorial/blob/master/reports/1b.png)
