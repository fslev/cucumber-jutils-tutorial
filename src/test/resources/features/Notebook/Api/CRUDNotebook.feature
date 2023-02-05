@notebook
@local @prod
@owner=Florin_Slevoaca
@Epic=API
Feature: CRUD notebook feature
  Create, read, update and delete notebooks

  @issue=1
  @link=https://github.com/fslev/cucumber-jutils-tutorial
  Scenario: Create notebook and check is present inside the list
    * load vars from dir "Notebook/Api/scene1"
    * # Create notebook and extract its id
    * Create notebook with requestBody=#[createNotebookRequest1] and check response={"status":201,"body":{"id":"~[notebookId1]"}}
    * # Get all notebooks and check previous notebook is present inside the list
    * Get notebooks with queryParams={"page":0, "pageSize":100} and check 0s until response=#[getNotebooksResponse1]
    * # Create another notebook with missing price
    * Create notebook with requestBody={"name":"Notebook without price"} and check response={"status":201,"body":{"id":"~[notebookId2]"}}
    * Get notebooks with queryParams={"page":0, "pageSize":100} and check 0s until response=#[getNotebooksResponse2]
    * # Get notebooks from page very far away. Check field "_embedded" is missing from response body
    * Get notebooks with queryParams={"page":10000,"pageSize":100} and check 0s until response={"status":200, "body":{"!_embedded":".*"}}

  Scenario: Create notebook and get it
    * load vars from dir "Notebook/Api/scene2"
    * # Create notebook and extract its id
    * Create notebook with requestBody=#[createNotebookRequest1] and check response={"status":201,"body":{"id":"~[notebookId]"}}
    * # Check notebook details
    * Get notebook with id=#[notebookId] and check 0s until response=#[getNotebookResponse1]

  Scenario: Create notebook and update it
    * load vars from dir "Notebook/Api/scene3"
    * # Create notebook and extract its id
    * Create notebook with requestBody=#[createNotebookRequest1] and check response={"status":201,"body":{"id":"~[notebookId]"}}
    * # Check notebook details
    * Get notebook with id=#[notebookId] and check 0s until response=#[getNotebookResponse1]
    * # Update notebook and check details
    * Update notebook having id=#[notebookId] with requestBody=#[updateNotebookRequest1] and check response={"status":202}
    * Get notebook with id=#[notebookId] and check 5s until response=#[getNotebookResponse2]
    * Get notebooks with queryParams={"page":0, "pageSize":100} and check 0s until response=#[getNotebooksResponse1]

  Scenario Template: Update existing notebook with id <notebookId>
    * load vars from dir "Notebook/Api/scene3a"
    * var id="<notebookId>"
    * Update notebook having id=#[id] with requestBody=<updateNotebookRequest> and check response={"status":202}
    * Get notebook with id=#[id] and check 5s until response=<getNotebookResponse>
    @notebook_1
    Examples:
      | notebookId | updateNotebookRequest       | getNotebookResponse       |
      | 1          | #[updateNotebook1Request]   | #[getNotebook1Response]   |
      | 1          | #[updateNotebook1Request_a] | #[getNotebook1Response_a] |
    @notebook_2
    Examples:
      | notebookId | updateNotebookRequest       | getNotebookResponse       |
      | 2          | #[updateNotebook2Request]   | #[getNotebook2Response]   |
      | 2          | #[updateNotebook2Request_a] | #[getNotebook2Response_a] |

  Scenario: Create notebook and delete it
    * load vars from dir "Notebook/Api/scene4"
    * # Create notebook and extract its id
    * Create notebook with requestBody=#[createNotebookRequest1] and check response={"status":201,"body":{"id":"~[notebookId]"}}
    * # Check notebook details
    * Get notebook with id=#[notebookId] and check 0s until response=#[getNotebookResponse1]
    * # Delete notebook
    * Delete notebook with id=#[notebookId] and check response={"status":202}
    * # Check notebook does not exist anymore
    * Get notebook with id=#[notebookId] and check 5s until response={"status":404}
    * Get notebooks with queryParams={"page":0, "pageSize":100} and check 0s until response=#[getNotebooksResponse1]
    * # Delete notebook again and check for 404 Not Found status
    * Delete notebook with id=#[notebookId] and check response={"status":404}

