**Java EE App**

[![Build Status](https://travis-ci.org/krzysbaranski/javaee-example.svg?branch=master)](https://travis-ci.org/krzysbaranski/javaee-example)

To build app run:
```bash
mvn install
```

To run app in WildFly copy file *target/AwesomeApp.war*
to *$WILDFLY_HOME/standalone/deployment/*

Start WildFly
in Linux:
```
$WILDFLY_HOME/bin/standalone.sh
```
in Windows:
```
$WILDFLY_HOME/bin/standalone.bat
```

To add author send REST POST request to URL <http://localhost:8080/AwesomeApp/rest/authors>
```json
{
  "name": "Mark",
  "surname": "Twain"
}
```

```json
{
  "name": "Arthur C.",
  "surname": "Clark"
}
```

```json
{
"name": "Stephen",
"surname": "Baxter"
}
```
For example in CURL:
```bash
curl -H "Content-Type: application/json" -X POST -d '{"name":"Mark","surname":"Twain"}' http://localhost:8080/AwesomeApp/rest/authors
curl -H "Content-Type: application/json" -X POST -d '{"name":"Arthur C.","surname":"Clark"}' http://localhost:8080/AwesomeApp/rest/authors
curl -H "Content-Type: application/json" -X POST -d '{"name":"Stephen","surname":"Baxter"}' http://localhost:8080/AwesomeApp/rest/authors
```

GET authors data:
<http://localhost:8080/AwesomeApp/rest/authors>

To add book send REST POST request to URL <http://localhost:8080/AwesomeApp/rest/books>
```json
{
  "title": "The Adventures of Tom Sawyer",
  "year": 1876,
  "author": [
    {
      "id": 1
    }
  ]
}
```

```json
{
  "title": "Adventures of Huckleberry Finn",
  "year": 1884,
  "author": [
    {
      "id": 1
    }
  ]
}
```
Add book with two authors:
```json
{
  "title": "Firstborn",
  "year": 2007,
  "author": [
    {
      "id": 2
    },
    {
      "id": 3
    }
  ]
}
```

For example in CURL:
```bash
curl -vv -H "Content-Type: application/json" -X POST -d '{"title":"The Adventures of Tom Sawyer","year":1876,"author":[{"id":1}]}' http://localhost:8080/AwesomeApp/rest/books
curl -vv -H "Content-Type: application/json" -X POST -d '{"title":"Adventures of Huckleberry Finn","year":1884,"author":[{"id":1}]}' http://localhost:8080/AwesomeApp/rest/books
curl -vv -H "Content-Type: application/json" -X POST -d '{"title":"Firstborn","year":2007,"author":[{"id":2},{"id":3}]}' http://localhost:8080/AwesomeApp/rest/books
```

* GET all books:
  <http://localhost:8080/AwesomeApp/rest/books>

* GET book with id=1:
  <http://localhost:8080/AwesomeApp/rest/books/1>

* GET all authors:
  <http://localhost:8080/AwesomeApp/rest/authors>

* GET author with id=1:
  <http://localhost:8080/AwesomeApp/rest/authors/1>

* Other REST methods: UPDATE, DELETE allows you to modify or delete data
