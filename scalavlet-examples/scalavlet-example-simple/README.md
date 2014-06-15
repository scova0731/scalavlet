
## Run the sample

    mvn jetty:run

If you want to debug in your IDE, `export` maven option before `jetty:run`

    export MAVEN_OPTS="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000"


## Access to the Scalavlets

### RestScalavlet

    curl -X GET http://localhost:8080/rest/users

    curl -X GET http://localhost:8080/rest/users/1
    curl -X GET http://localhost:8080/rest/users/2

    curl  -X POST \
          -d "{\"name\":\"Post Hanako\", \"email\":\"hanako@scalavlet.org\"}" \
          http://localhost:8080/rest/users/new

    curl  -X PUT \
          -d "{\"id\" :3, \"name\":\"Put Hanako\", \"email\":\"hanako@scalavlet.org\"}" \
          http://localhost:8080/rest/users/3

    curl  -X PATCH \
          -d "{\"id\" :3, \"name\":\"Patch Hanako\", \"email\":\"hanako@scalavlet.org\"}" \
          http://localhost:8080/rest/users/3

    curl  -X DELETE \
          http://localhost:8080/rest/users/3

    curl -X GET http://localhost:8080/rest/raise-exception

You might want to see the details. Please add the verbose: `-v` option.

### MoreJsonScalavlet

TODO

### FutureScalavlet

    curl -X GET http://localhost:8080/future/slow

### PageScalavlet
