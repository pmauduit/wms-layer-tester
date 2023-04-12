# Build

```
./mvnw clean package
```

# Run the flatjar

```
java -jar target/geoserver-check-1.0-SNAPSHOT-jar-with-dependencies.jar <WMS service url to test>
```

# Run from maven

```
./mvnw compile exec:java -Dexec.args=<WMS service url to test>
```

e.g.:

```
./mvnw compile exec:java -Dexec.args=https://dev.datagrandest.fr/geoserver/wms
```
