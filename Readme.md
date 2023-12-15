[![Build status](https://github.com/fslev/cucumber-jutils-tutorial/workflows/Java%20CI%20with%20Maven/badge.svg?branch=main)](https://github.com/fslev/cucumber-jutils-tutorial/actions/workflows/build.yml)
[![Allure test reports](https://img.shields.io/static/v1?label=Go%20To&message=Allure%20Test%20Reports&color=ff69b4)](https://fslev.github.io/cucumber-jutils-tutorial)
# Cucumber JUtils showcase


A simple showcase on how to use [**cucumber-jutils**](https://github.com/fslev/cucumber-jutils) library  while testing a notebook manager web application.  

## Requirements
JDK11, Maven, Docker & docker-compose 

## Setup
_Start notebook-manager app:_
```shell
src/test/resources/docker

docker-compose up
```

Notebook manager Swagger:  
http://localhost:8090/swagger-ui.html  

## Configure Cucumber tests from Intellij Idea

### Requirements
- __Intellij Idea__ version >= 2019.3
- Latest version of [Cucumber for Java](https://plugins.jetbrains.com/plugin/7212-cucumber-for-java) and [Gherkin](https://plugins.jetbrains.com/plugin/9164-gherkin) plugins

### Cucumber for Java plugin configuration

[Cucumber for Java](https://plugins.jetbrains.com/plugin/7212-cucumber-for-java) plugin should by default read configuration from _junit-platform.properties_

Also, for newer versions of Idea (>=2022) you need the following plugin setup:   
`Run -> Edit configurations... -> Edit configuration templates -> Cucumber for Java -> Before launch -> Build project`

## Execution and Reports

### Run tests from Idea

_src/test/resources/features:_
```
Right click any feature file or inside any feature file -> Run feature / Run scenario
```

### Run tests from Maven
```shell
mvn clean -Plocal,allure-reports verify
```
Check for Cucumber Allure reports inside `target/site/allure-maven-plugin/index.html`  
Or, directly inside GitHub pages by clicking on the Allure Test Reports badge or directly at  
https://fslev.github.io/cucumber-jutils-tutorial
  
![img.png](reports/allure-reports.png)
