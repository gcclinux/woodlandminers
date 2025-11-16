#!/usr/bin/bash

# Woodlanders Server Launcher Script
# This script reads JVM configuration and starts the server

# Default JVM settings
JVM_MEMORY_MIN=${JVM_MEMORY_MIN:-4G}
JVM_MEMORY_MAX=${JVM_MEMORY_MAX:-4G}
JVM_GC=${JVM_GC:-G1GC}
JVM_GC_PAUSE=${JVM_GC_PAUSE:-50}
JVM_PARALLEL_REF=${JVM_PARALLEL_REF:-true}

# Server settings
SERVER_JAR="woodlanders-server.jar"
SERVER_ARGS="$@"

# Build JVM arguments
JVM_ARGS="-Xms${JVM_MEMORY_MIN} -Xmx${JVM_MEMORY_MAX}"

if [ "$JVM_GC" = "G1GC" ]; then
    JVM_ARGS="$JVM_ARGS -XX:+UseG1GC"
fi

if [ -n "$JVM_GC_PAUSE" ]; then
    JVM_ARGS="$JVM_ARGS -XX:MaxGCPauseMillis=${JVM_GC_PAUSE}"
fi

if [ "$JVM_PARALLEL_REF" = "true" ]; then
    JVM_ARGS="$JVM_ARGS -XX:+ParallelRefProcEnabled"
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
echo ""
echo "Starting server with: java $JVM_ARGS -jar $SERVER_JAR $SERVER_ARGS"
echo "=========================================="
echo ""

# Start the server
exec java $JVM_ARGS -jar "$SERVER_JAR" $SERVER_ARGS
