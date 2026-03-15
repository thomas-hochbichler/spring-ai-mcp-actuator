# Spring Boot MCP Server Demo

Companion code for the article [Build an MCP Server with Spring Boot 4](https://blog.hochbichler.com/spring-boot-mcp-server).

> **Note:** This project uses **Spring Boot 4.0.0** and **Spring AI 2.0.0-M2** (milestone release).
> These are pre-release versions — APIs may change before GA. Check the
> [Spring AI releases](https://github.com/spring-projects/spring-ai/releases) for updates.

Three independent Maven projects (with a root POM for convenience):

- **mcp-actuator** — MCP server that monitors Spring Boot apps via Actuator (STDIO transport)
- **order-service** — Sample app on port 8080 with Actuator enabled
- **user-service** — Sample app on port 8081 with Actuator enabled

## Prerequisites

- Java 21+
- Maven 3.9+ (or use the included Maven wrapper)

## Quick Start

### Option 1: Use the startup script

```bash
chmod +x start-all.sh
./start-all.sh
```

This builds all three projects, starts order-service and user-service, and prints the `claude mcp add` command.

### Option 2: Manual setup

**1. Build all projects:**

```bash
mvn clean package -DskipTests
```

Or build individually:

```bash
cd order-service && ./mvnw clean package -DskipTests && cd ..
cd user-service  && ./mvnw clean package -DskipTests && cd ..
cd mcp-actuator  && ./mvnw clean package -DskipTests && cd ..
```

**2. Start the sample apps:**

```bash
java -jar order-service/target/order-service-0.0.1-SNAPSHOT.jar &
java -jar user-service/target/user-service-0.0.1-SNAPSHOT.jar &
```

**3. Verify Actuator is working:**

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
```

**4. Add the MCP server to Claude Code:**

```bash
claude mcp add --transport stdio spring-actuator \
  -- java -jar $(pwd)/mcp-actuator/target/mcp-actuator-0.0.1-SNAPSHOT.jar \
  --apps=http://localhost:8080,http://localhost:8081
```

> If you get a timeout on first launch, set `MCP_TIMEOUT=10000 claude` to give Spring Boot time to start.

**5. Verify in Claude Code:**

```
/mcp
```

You should see `spring-actuator` with three tools: `check-health`, `get-metric`, `list-metrics`.

## Example Conversation

```
You: Is localhost:8080 healthy?
You: Check all apps
You: What metrics are available on localhost:8080?
You: How much JVM memory is the order service using?
```
