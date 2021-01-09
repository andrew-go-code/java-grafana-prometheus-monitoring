# Java app monitoring with Prometheus and Grafana demo

## Description
We've got SpringBoot java application. 
Task is to collect some metrics via Prometheus and view them via Grafana. 

## Pre-requirements for Java Aap

* Dependencies
```
implementation 'org.springframework.boot:spring-boot-starter-actuator'
implementation 'io.micrometer:micrometer-registry-prometheus'
```

* Application.yml
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
```

## Demo scenario

* create application and build .jar file
```
./gradlew bootJar
```

* create image (./Dockerfile)
```
docker build -t app .
```

* remove image if needed
```
docker image rm -f app
```

* start docker-compose on background (./docker-compose/docker-compose.yml)
```
docker-compose up -d
```

* check out metrics that send to Prometheus
```
http://localhost:8765/actuator/prometheus
```

* check out Prometheus monitoring entities (should be at UP state for java-app)
```
http://localhost:9090/targets
```

* connect to Grafana (login: "admin"; password: "pass")
There is one dashboard (that one was from config: docker-compose/grafana/provisioning/dashboards/JavaAppDashboard.json)
```
http://localhost:3000
```

* emulate load
```
curl --location --request GET 'http://localhost:8765/server?delayMs=1&creatingTimeMs=10000&threadCount=6&forgetToClearObjects=false' \
--header 'Content-Type: application/json'
```

That request makes app to create objects and store them in list. Returns some info.

request params:
 * delayMs - Thread.sleep(delayMs);
 * creatingTimeMs - time during which objects created;
 * threadCount - threads number which created for work;
 * forgetToClearObjects - if true (or null) list of objects won't be cleared after work done.

p.s. Same request could be sent with Postman ofc.

* clear docker
```
docker-compose down
```
