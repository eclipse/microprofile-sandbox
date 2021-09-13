# The infrastructure.
There are 2 different contexts:
- Service mesh
- Observability demo

#Intro

To build specific docker images, `build-vegetables-open-liberty` in this example:
```
 cd ../apps
 docker build --no-cache --target build-vegetables-open-liberty .
```

Add alias to you console:
```
 source .alias
```
# Observability demo

## Setting infrastructure for observability

```shell
docker-compose -f docker-compose.yml -f docker-compose.observability up -d
# Or for brevity with alias, after the command from above.
observability up -d
```

Navigate to localhost:3000 to visit grafana

Initial credentials for grafana are:

```yml
username: admin
password: admin
```

