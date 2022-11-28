# Cucumber JUtils tutorial


A light tutorial on how to use [**cucumber-jutils**](https://github.com/fslev/cucumber-jutils) library  while testing a notebook manager web application.  

## Requirements
JDK11, Maven, Docker & docker-compose 

Notebook manager Swagger:
http://localhost:8090/swagger-ui.html

## Setup
_Start notebook-manager app:_
```shell
src/test/resources/docker

docker-compose up
```

_Run tests:_
```shell
mvn clean verify -Plocal
```