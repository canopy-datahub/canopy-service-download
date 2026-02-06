# Download Service

Spring Boot 3.1 microservice for Data Hub 3.0. It is running on Java 17.

# Install and Run

## Maven

### Local

There are a few environment variable that need to be set:
* db_username
    * database user that you're connecting through
* db_password
    * database password for db_username
* spring_profiles_active
    * This should be set to 'local'
* UuidSpreadsheetPath
  * s3 path pointing  to the spreadsheet mapping study UUID's to their study

I typically just set these via Java environment variables in IntelliJ.

Once the environment variables are set:
```
mvn clean install
```
Once all classes are generated, you can run the application with maven or via the application context.
```
mvn spring-boot:run
```

### Cloud

If running a cloud configuration locally, AWS CLI needs to be installed and configured.

There are a few environment variable that need to be set in AWS Secrets Manager:
* dbuser
    * database user that you're connecting through
* password
    * database password for dbuser
* host
    * hostname of database
* port
    * database port
* dbname
    * database name
* UuidSpreadsheetPath
  * s3 path pointing  to the spreadsheet mapping study UUID's to their study

In a specific instance, the only environment variable that needs to be set is:
* spring_profiles_active
    * This should be set to '{environment}'
        * The current environments are dev, test, prod

Once the environment variables are set:
```
mvn clean install 
```
Once all classes are generated, you can run the application with maven or via the application context.
```
mvn spring-boot:run
```

### Endpoint

The base endpoint for this service is:
```
{{hostname}}/api/download/v1/
```
