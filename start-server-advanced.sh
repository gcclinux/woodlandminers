#!/bin/bash

# Woodlanders Server Launcher Script with JVM Config File Support
# This script reads JVM configuration from jvm.properties

JVM_CONFIG_FILE="jvm.properties"
SERVER_JAR="build/libs/woodlanders-server.jar"

# Function to read property from file
read_property() {
    local key=$1
    local default=$2
    local value=$(grep "^${key}=" "$JVM_CONFIG_FILE" 2>/dev/null | cut -d'=' -f2- | tr -d ' ')
    echo "${value:-$default}"
}

# Load JVM configuration
if [ -f "$JVM_CONFIG_FILE" ]; then
    echo "Loading JVM configuration from $JVM_CONFIG_FILE"
    JVM_MEMORY_MIN=$(read_property "jvm.memory.min" "4G")
    JVM_MEMORY_MAX=$(read_property "jvm.memory.max" "4G")
    JVM_GC=$(read_property "jvm.gc" "G1GC")
    JVM_GC_PAUSE=$(read_property "jvm.gc.max-pause-millis" "50")
    JVM_PARALLEL_REF=$(read_property "jvm.parallel-ref-processing" "true")
    JVM_ADDITIONAL=$(read_property "jvm.additional-args" "")
else
    echo "JVM config file not found, using defaults"
    JVM_MEMORY_MIN="4G"
    JVM_MEMORY_MAX="4G"
    JVM_GC="G1GC"
    JVM_GC_PAUSE="50"
    JVM_PARALLEL_REF="true"
    JVM_ADDITIONAL=""
fi

# Build JVM arguments
JVM_ARGS="-Xms${JVM_MEMORY_MIN} -Xmx${JVM_MEMORY_MAX}"

if [ "$JVM_GC" = "G1GC" ]; then
    JVM_ARGS="$JVM_ARGS -XX:+UseG1GC"
elif [ "$JVM_GC" = "ParallelGC" ]; then
    JVM_ARGS="$JVM_ARGS -XX:+UseParallelGC"
fi

if [ -n "$JVM_GC_PAUSE" ] && [ "$JVM_GC_PAUSE" != "0" ]; then
    JVM_ARGS="$JVM_ARGS -XX:MaxGCPauseMillis=${JVM_GC_PAUSE}"
fi

if [ "$JVM_PARALLEL_REF" = "true" ]; then
    JVM_ARGS="$JVM_ARGS -XX:+ParallelRefProcEnabled"
fi

if [ -n "$JVM_ADDITIONAL" ]; then
    JVM_ARGS="$JVM_ARGS $JVM_ADDITIONAL"
fi

# Print configuration
echo "=========================================="
echo "  Woodlanders Server Launcher"
echo "=========================================="
echo "JVM Configuration:"
echo "  Memory: ${JVM_MEMORY_MIN} - ${JVM_MEMORY_MAX}"
echo "  GC: ${JVM_GC}"
echo "  Max GC Pause: ${JVM_GC_PAUSE}ms"
echo "  Parallel Ref Processing: ${JVM_PARALLEL_REF}"
if [ -n "$JVM_ADDITIONAL" ]; then
    echo "  Additional Args: ${JVM_ADDITIONAL}"
fi
echo ""
echo "Starting server with: java $JVM_ARGS -jar $SERVER_JAR $@"
echo "=========================================="
echo ""

# Start the server
exec java $JVM_ARGS -jar "$SERVER_JAR" "$@"
