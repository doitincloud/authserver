About authserver
----------------

authserver is a deep dive into spring boot security and oauth2. It uses the rdbcache code to provide three layers (local memory, redis and mysql) of cache and persistency. All four grant types are supported. Token revocation services have been added to support reset or change of user password and client secret.

How to run
----------
### Requirments

It requires Java version 1.8+, maven 3.5+, redis 4.0+ and mysql 5+.

### Configuration

edit src/main/resources/application.properties, change following section according to your settings:

    # for redis
    #
    spring.redis.url=redis://localhost:6379

    # for database
    #
    spring.datasource.url=jdbc:mysql://localhost/doitincloud_db?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true&useUnicode=true
    
    spring.datasource.username=dbuser
    spring.datasource.password=rdbcache

### Run

mvn clean spring-boot:run

### Test

    #command jq is required for the test
    
    cd src/scripts
    
    ./tests
    
    #OR you can run individual tests as following
    
    ./authorization_code_flow
    ./client_credentials_flow
    ./implicit_flow
    ./password_flow
    ./response_type_code
    ./revocations
    ./scopes_roles
