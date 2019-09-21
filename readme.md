### Peanutbutter Auth
Authorization server based on Spring Boot

### Requirements
```
install and run java 1.8, maven, redis, docker (optional)
```
### Usage
```
mvn spring-boot:run
# or if you are running docker
docker-compose up --build
```

### Development
1. Create (or find an existing) issue via the [issue page](https://github.com/niculistana/peanutbutter/issues)
2. Download [Sonarlint](https://www.sonarlint.org/) and use with your IDE
3. Create a branch off `master`
4. Make a pull request against `development` 
5. Dry-run ci by running `mvn clean verify` and `circleci local execute`
### Tests
```
# runs the tests, and generates code coverage
mvn clean verify
```
