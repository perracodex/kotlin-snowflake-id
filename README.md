# Snowflake in Kotlin

A [SnowflakeFactory](https://github.com/perracolabs/Snowflake/blob/main/SnowflakeFactory.kt) to generate unique snowflake IDs in Kotlin. Suitable for ktor and distributed systems.

For the ID reverse parser, [Kotlinx](https://github.com/Kotlin/kotlinx.serialization) is used for serializaiton and [Date](https://github.com/Kotlin/kotlinx-datetime) types. So, the next dependencies must be included unless you ammed the code to use your own serialization solution and concrete Date types:

```kts
implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
```

## How to integrate the factory to show snowflake IDs in your server logs.

1. Add the ktor dependencies for the [CallLogging](https://ktor.io/docs/call-logging.html) and [CallId](https://ktor.io/docs/call-id.html) plugins:

```kts
implementation("io.ktor:ktor-server-call-logging:2.3.7")
implementation("io.ktor:ktor-server-call-id:2.3.7")
```

2. Register the plugins in your Application module, integrating the snowflake factory:

```kotlin
fun Application.configureCallLogging() {

    install(CallLogging) {
        level = Level.INFO

        // Integrates the unique call ID into the Mapped Diagnostic Context (MDC) for logging.
        // This allows the call ID to be included in each log entry, linking logs to specific requests.
        callIdMdc(name = "callid")
    }

    install(CallId) {
        // Generates a unique ID for each call. This ID is used for request tracing and logging.
        generate {
            SnowflakeFactory.nextId()
        }

        // Optionally we can also include the IDs to the response headers,
        // so that it can be retrieved by the client for tracing.
        replyToHeader(headerName = HttpHeaders.XRequestId)
    }
}
```

3. Finally include the ```callid``` tag into your logger's configuration file. For example if using ```logback.xml```, the tag ```%X{callid}``` is added as next:

```xml
<configuration debug="false">
    <statusListener class="ch.qos.logback.core.status.NopStatusListener" />
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{YYYY-MM-dd HH:mm:ss.SSS} | [%thread] | %-5level | %X{callid} | %logger | %msg%n</pattern>
        </encoder>
    </appender>
    <root level="TRACE">
        <appender-ref ref="STDOUT"/>
    </root>

    <logger name="Application" level="INFO"/>
</configuration>
```

Example where a GET request has been received. Notice how the '1iadis897lmgw' snowflake ID as been included in the traces:

```
2023-12-26 21:13:13.366 | [eventProxy] | TRACE | 1iadis897lmgw | io.ktor.server.plugins.ratelimit.RateLimit | Using key=kotlin.Unit and weight=1 for /v1/employees
2023-12-26 21:13:13.372 | [eventProxy] | TRACE | 1iadis897lmgw | io.ktor.server.plugins.ratelimit.RateLimit | Allowing /v1/employees
2023-12-26 21:13:13.386 | [eventProxy] | DEBUG | 1iadis897lmgw | Exposed | SELECT COUNT(*) FROM EMPLOYEE LEFT JOIN CONTACT ON EMPLOYEE.EMPLOYEE_ID = CONTACT.EMPLOYEE_ID
2023-12-26 21:13:13.513 | [eventProxy] | TRACE | 1iadis897lmgw | io.ktor.server.plugins.statuspages.StatusPages | No status code found for call: /v1/employees
2023-12-26 21:13:13.528 | [eventProxy] | INFO  | 1iadis897lmgw | Application | Call Metric: [127.0.0.1] GET - /v1/employees - 170ms
```

If the ```1iadis897lmgw```ID is parsed back, it will give its concrete detailed information:
```json
{
    "machineId": 1,
    "sequence": 0,
    "utc": "2023-12-26T20:13:13.348",
    "local": "2023-12-26T21:13:13.348"
}
```
