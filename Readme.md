[![Build status](https://github.com/fslev/cucumber-jutils-tutorial/workflows/Java%20CI%20with%20Maven/badge.svg?branch=main)](https://github.com/fslev/cucumber-jutils-tutorial/actions/workflows/build.yml)
[![Allure test reports](https://img.shields.io/static/v1?label=go%20to&message=Allure%20Test%20Reports&color=yellow)](https://fslev.github.io/cucumber-jutils-tutorial)
# Cucumber JUtils tutorial


A light tutorial on how to use [**cucumber-jutils**](https://github.com/fslev/cucumber-jutils) library  while testing a notebook manager web application.  

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

## Execution and Reports

_Run tests from Maven:_
```shell
mvn clean -Plocal,allure-reports verify
```
Check for Cucumber Allure reports inside `target/site/allure-maven-plugin/index.html`  
Or, directly inside GitHub pages by clicking on the Allure Test Reports badge or directly at  
https://fslev.github.io/cucumber-jutils-tutorial
  
![img.png](reports/allure-reports.png)