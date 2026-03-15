#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Resolve the java binary the same way Maven wrapper does: JAVA_HOME first, then PATH
if [ -n "${JAVA_HOME-}" ]; then
    JAVA_CMD="$JAVA_HOME/bin/java"
else
    JAVA_CMD="java"
fi

if ! command -v "$JAVA_CMD" &>/dev/null; then
    echo "Error: Java not found. Please install JDK 21 and set JAVA_HOME or add java to your PATH."
    exit 1
fi

JAVA_VERSION=$("$JAVA_CMD" -version 2>&1 | head -1 | sed 's/.*"\([0-9]*\)\..*/\1/')
if [ "$JAVA_VERSION" -lt 21 ] 2>/dev/null; then
    echo "Error: Java 21+ is required but found Java $JAVA_VERSION (from $JAVA_CMD)."
    echo ""
    if [ -n "${JAVA_HOME-}" ]; then
        echo "  JAVA_HOME is set to: $JAVA_HOME"
        echo "  Update JAVA_HOME to point to a JDK 21+ installation."
    else
        echo "  Set JAVA_HOME to a JDK 21+ installation or update your PATH."
    fi
    exit 1
fi

PIDS=()

cleanup() {
    echo ""
    echo "Shutting down services..."
    for pid in "${PIDS[@]+"${PIDS[@]}"}"; do
        kill "$pid" 2>/dev/null || true
    done
    wait 2>/dev/null
    echo "Done."
}
trap cleanup INT TERM

echo "=== Building all projects ==="

echo "Building order-service..."
(cd "$SCRIPT_DIR/order-service" && ./mvnw -q clean package -DskipTests)

echo "Building user-service..."
(cd "$SCRIPT_DIR/user-service" && ./mvnw -q clean package -DskipTests)

echo "Building mcp-actuator..."
(cd "$SCRIPT_DIR/mcp-actuator" && ./mvnw -q clean package -DskipTests)

echo ""
echo "=== Starting services ==="

echo "Starting order-service on port 8080..."
java -jar "$SCRIPT_DIR/order-service/target/order-service-0.0.1-SNAPSHOT.jar" &
PIDS+=($!)

echo "Starting user-service on port 8081..."
java -jar "$SCRIPT_DIR/user-service/target/user-service-0.0.1-SNAPSHOT.jar" &
PIDS+=($!)

echo ""
echo "Waiting for services to become healthy..."
for port in 8080 8081; do
    printf "  Waiting for localhost:%s" "$port"
    for i in $(seq 1 30); do
        if curl -sf "http://localhost:$port/actuator/health" >/dev/null 2>&1; then
            echo " ready"
            break
        fi
        if [ "$i" -eq 30 ]; then
            echo " timed out (continuing anyway)"
        fi
        printf "."
        sleep 1
    done
done

echo ""
echo "=== Services running ==="
echo "  order-service: http://localhost:8080"
echo "  user-service:  http://localhost:8081"
echo ""
echo "Add the MCP server to Claude Code:"
echo ""
echo "  claude mcp add --transport stdio spring-actuator \\"
echo "    -- java -jar $SCRIPT_DIR/mcp-actuator/target/mcp-actuator-0.0.1-SNAPSHOT.jar \\"
echo "    --apps=http://localhost:8080,http://localhost:8081"
echo ""
echo "Press Ctrl+C to stop all services."
# Wait in a loop so that one service exiting doesn't terminate the script.
# The script only exits when ALL background processes have finished or on Ctrl+C.
while true; do
    all_done=true
    for pid in "${PIDS[@]}"; do
        if kill -0 "$pid" 2>/dev/null; then
            all_done=false
        fi
    done
    if $all_done; then
        echo "All services have stopped."
        break
    fi
    wait -n 2>/dev/null || true
done
