### Peanutbutter Auth
Authorization server based on Spring Boot

### Requirements
```
java 1.8, maven redis
```

### Usage
```
mvn spring-boot:run
open localhost:8080 
```

### Using docker
Please check the [Using Docker](https://github.com/niculistana/peanutbutter/wiki/Using-Docker) page.

### Development
1. Create (or find an existing) issue via the [issue page](https://github.com/niculistana/peanutbutter/issues)
2. Download [Sonarlint](https://www.sonarlint.org/) and use with your editor
3. Download [Google Code Format](https://github.com/google/google-java-format); Optional for Eclipse & IntelliJ: apply [formatting on save](https://plugins.jetbrains.com/plugin/7642-save-actions).
4. Create a branch off `master`
5. Make a pull request against `development` 
6. Dry-run ci by running `mvn clean verify` and `circleci local execute`
### Tests
```
# runs the tests, and generates code coverage
mvn clean verify
```
