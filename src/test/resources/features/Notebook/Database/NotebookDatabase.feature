@notebook
@local @prod
@database
Feature: Notebook database feature
  Create, read, update and delete notebooks and check database

  Scenario: Create notebook and check is present inside the database
    * load vars from dir "features/Notebook/Database/scene1"
    * # Create notebook and extract its id
    * Create notebook with requestBody=#[createNotebookRequest1] and check response={"status":201,"body":{"id":"~[notebookId1]"}}
    * # Check notebook was persisted inside the database
    * [sql] Load data source from file path features/Notebook/Database/ds/notebook-manager-ds.properties
    * [sql] Execute query select id,name,current_price from note_book where id=#[notebookId1] and check result is
      | id             | current_price | name                      |
      | #[notebookId1] | 1500.0        | Asus ZEN Pro book db test |

  Scenario: Create notebook, update it and check is updated inside the database also
    * load vars from dir "features/Notebook/Database/scene2"
    * # Create notebook and extract its id
    * Create notebook with requestBody=#[createNotebookRequest1] and check response={"status":201,"body":{"id":"~[notebookId1]"}}
    * Update notebook having id=#[notebookId1] with requestBody=#[updateNotebookRequest1] and check response={"status":202}
    * # Check notebook was updated inside the database
    * [sql] Load data source from file path features/Notebook/Database/ds/notebook-manager-ds.properties
    * [sql] Execute query select id,name,current_price from note_book where id=#[notebookId1] and check result is
      | id             | current_price | name                               |
      | #[notebookId1] | 1600.9        | Asus ZEN Pro book db test2 updated |

  Scenario: Create notebook, delete it and check is removed from database also
    * load vars from dir "features/Notebook/Database/scene3"
    * # Create notebook and extract its id
    * Create notebook with requestBody=#[createNotebookRequest1] and check response={"status":201,"body":{"id":"~[notebookId1]"}}
    * # Check notebook was persisted inside the database
    * [sql] Load data source from file path features/Notebook/Database/ds/notebook-manager-ds.properties
    * [sql] Execute query select id,name,current_price from note_book where id=#[notebookId1] and check result is
      | id             | current_price | name                               |
      | #[notebookId1] | 1100.1        | Asus ZEN Pro book db delete test 1 |

    * # Delete notebook
    * Delete notebook with id=#[notebookId1] and check response={"status":202}
    * [sql] Execute query select id,name,current_price from note_book where id=#[notebookId1] and check result=[]

