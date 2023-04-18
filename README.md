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

# Junit format

The following XML report will be generated:

```xml
<?xml version="1.0" encoding="UTF-8"?> 
<testsuite id="wmslayers" name="Checking WMS layers" tests="45" errors="17" time="1.00">
    <testcase classname="WmsLayer" name="layer:name" time="0.98">
        <error type="Error" message="errmsg">errmsg</error>
    </testcase>
    <testcase classname="WmsLayer" name="layer:name" time="0.02" />
</testsuite>
```