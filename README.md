# googlyeyes-db-and
ORM Database for Android. Only SQLite connections are allowed at the moment. It uses two JSON files to configure the database and its connection.

## Features
 * Create and modify the structure of a database
 * Execute requests: Raw and simple requests
 * ObjectRequest factory: It allows to make requests directly with objects, as ORM.

##Installation

You can find the latest version of the library on jCenter repository.

### For Gradle users

In your `build.gradle` you should declare the jCenter repository into `repositories` section:
```gradle
   repositories {
       jcenter()
   }
```
Include the library as dependency:
```gradle
compile 'com.silicornio:googlyeyes-db-and:1.0.0'
```

### For Maven users

```maven
<dependency>
  <groupId>com.silicornio</groupId>
  <artifactId>googlyeyes-db-and</artifactId>
  <version>1.0.0</version>
  <type>pom</type>
</dependency>
```

##Usage

1. Create a database configuration file:

    ```json
    {
    	"driver": "SQLite",
    	"name": "test_system_db",
    	"version": 1
    }
    ```
      
  * driver - Type of database (SQLite only supported for the moment)
  * name - Name of the database
  * version - Version of the database

2. Create your own model file:

    ```json
    {
        "configuration": {
    
        },
    	"objects": [
    		{
    			"name": "GEMessage",
    			"attributes": [
    				{
    					"name": "messageId",
    					"type": "integer",
    					"id": true,
    					"autoincrement": true
    				},
    				{
    					"name": "title",
    					"type": "string",
    					"length": 50,
    					"mandatory": true,
    					"unique": true,
    					"defaultValue": ""
    				}
    			],
    			"dbAction": "CREATE"
    		}
    	]
    }
    
    ```
  * configuration - General configurations of the model
  * object.name - Name of the model (object)
  * object.attributes - List of attributes of the model (variables)
  * attribute.name - Name of the attribute (variable)
  * attribute.type - Type of attribute: 'integer', 'double', 'string' or a reference to another model
  * attribute.id - Indicates if this attribute is the identifier of the model
  * attribute.autoincrement - Indicates if this attribute should be incremented in each insert
  * attribute.length - Length of the attribute (if not included default length will be setted)
  * attribute.mandatory - Indicates if it is mandatory to include the value for this attribute in each insert
  * attribute.unique - Indicates if this attribute should be unique
  * attribute.defaultValue - Value to set by default when an object is inserted
  * dbAction - Permission to modify the database: 'CREATE', 'UPDATE', 'DELETE'

3. Add the files to the assets folder of the project

4. Create a GEDBController instance

  We need to send the database configuration and the model. We can use read it before from the assets.

    ```java
    GEDbConf dbConf = GEDBUtils.readConfObjectFromAssets(this, "FOLDER_INSIDE_ASSETS/db.conf", GEDbConf.class);
    GEModelConf modelConf = GEDBUtils.readConfObjectFromAssets(this, "FOLDER_INSIDE_ASSETS/model.conf", GEModelConf.class);
    GEDBController dbController = new GEDBController(dbConf, modelConf);
    ```

5. Connect with the database

  When we connect with the database it is modified if we gave it permissions in the model file and we changed the model. It should do automatically.

    ```java
    dbController.connectDb(context);
    ```

6. Execute requests

  GooglyeyesDB library works with the same Request object for any type of connection. The GEDBObjectFactory allows to create and execute directly requests working with objects. If we need to do complex requests we can create our own GERequest and execute it.

    ```java
    GEDBObjectFactory.addObject(dbController, object);
    ```

7. Disconnect the database connection

  When activity is destroyed you should disconnect the database.

    ```java
    dbController.disonnectDb()
    ```

## Logs

Googlyeyes has a lot of logs, showing all the process. You can enable it but remember to disable it in production releases.

  ```java
  GEL.showLogs = true;
  ```

## License

    Copyright 2016 SilicorniO

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    


