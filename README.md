**Java EE App**

To build app run:
```bash
mvn install
```

To run app in WildFly copy file *target/AwesomeApp.war*
to *$WILDFLY_HOME/standalone/deployment/*

and start WildFly by:
in Linux:
```
$WILDFLY_HOME/bin/standalone.sh
```
in Windows:
```
$WILDFLY_HOME/bin/standalone.bat
```

To add author send REST POST request to URL: http://localhost:8080/AwesomeApp/rest/authors
```json
{"name":"Mark","surname":"Twain"}'
```
For example in CURL:
```bash
curl -H "Content-Type: application/json" -X POST -d '{"name":"Mark","surname":"Twain"}' http://localhost:8080/AwesomeApp/rest/authors
```

To add book send REST POST request to URL: http://localhost:8080/AwesomeApp/rest/books
```json
{"title":"The Adventures of Tom Sawyer"}
```
For example in CURL:
```bash
curl -H "Content-Type: application/json" -X POST -d '{"title":"The Adventures of Tom Sawyer"}' http://localhost:8080/AwesomeApp/rest/books
```
