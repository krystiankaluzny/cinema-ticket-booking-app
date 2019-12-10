# cinema-ticket-booking-app

## Build and run
App requires Java 11 to compile and run.
If your **JAVA_HOME** points to java 11 (or higher) you can compile and run this app with following command:
```
bash build_and_run.sh
```

Otherwise you have to define JAVA_HOME in your console and run `build_and_run.sh` scrips:
```
JAVA_HOME=/path/to/java11
bash build_and_run.sh
```

Applications use default spring configuration so tomcat starts on **8080** port.

## Test

To run use case run
```
bash test.sh
``` 