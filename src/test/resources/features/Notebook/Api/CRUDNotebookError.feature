@notebook
@local @prod
@epic=API
@severity=minor
Feature: CRUD notebook feature [ERROR cases]
  Create, read, update and delete notebooks with invalid data

  Scenario: Create notebook with invalid data
    * # Create notebook with invalid request body
    * Create notebook with requestBody=invalid json and check response={"status":500}

  Scenario: Create notebooks with same name
    * Create notebook with requestBody={"name":"notebookA"} and check response={"status":201}
    * # Conflict should be raised
    * Create notebook with requestBody={"name":"notebookA"} and check response={"status":409}

  Scenario: Get notebook with invalid data
    * Get notebook with id=abc and check 0s until response={"status":400}
    * Get notebook with id=99999999999999 and check 0s until response={"status":400}
    * Get notebook with id=0 and check 0s until response={"status":404}
    * Get notebook with id=-1 and check 0s until response={"status":404}

  Scenario: Get notebooks with invalid query params
    * # Missing query params
    * Get notebooks with queryParams={} and check 0s until response={"status":400}
    * # Missing pageSize
    * Get notebooks with queryParams={"page":1} and check 0s until response={"status":400}
    * # Missing pageS
    * Get notebooks with queryParams={"pageSize":1} and check 0s until response={"status":400}
    * # Invalid query params values
    * Get notebooks with queryParams={"page":-1,"pageSize":-1} and check 0s until response={"status":500}
    * # Out of bounds query params values
    * Get notebooks with queryParams={"page":10000000000000,"pageSize":1} and check 0s until response={"status":400}
    * Get notebooks with queryParams={"page":1,"pageSize":10000000000000} and check 0s until response={"status":400}

  Scenario: Get notebook with invalid id
    * Get notebook with id=0 and check 0s until response={"status":404}
    * Get notebook with id=-1 and check 0s until response={"status":404}
    * Get notebook with id=abc and check 0s until response={"status":400}

  Scenario: Create notebook and update it with invalid data
    * load vars from dir "features/Notebook/Api/Error/scene1"
    * # Create notebook and extract its id
    * Create notebook with requestBody=#[createNotebookRequest1] and check response={"status":201,"body":{"id":"~[notebookId]"}}
    * Get notebooks with queryParams={"page":0, "pageSize":100} and check 0s until response=#[getNotebooksResponse1]
    * # Update notebook with invalid data
    * # Invalid request body
    * Update notebook having id=#[notebookId] with requestBody=invalid and check response={"status":500}
    * Update notebook having id=#[notebookId] with requestBody={} and check response={"status":500}
    * Update notebook having id=#[notebookId] with requestBody={"name":"test"} and check response={"status":500}
    * # Invalid id
    * Update notebook having id=0 with requestBody={"name":"test","currentPrice":101.0,"id":"#[notebookId]"} and check response={"status":404}
    * Update notebook having id=-1 with requestBody={"name":"test","currentPrice":101.0,"id":"#[notebookId]"} and check response={"status":404}
    * Update notebook having id=abc with requestBody={"name":"test","currentPrice":101.0,"id":"#[notebookId]"} and check response={"status":400}

  Scenario: Delete notebook with invalid data
    * Delete notebook with id=abc and check response={"status":400}
    * Delete notebook with id=9999999999999 and check response={"status":400}
    * Delete notebook with id=0 and check response={"status":404}
    * Delete notebook with id=-1 and check response={"status":404}

