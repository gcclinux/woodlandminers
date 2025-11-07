#!/bin/bash

# Multiplayer Threading Fix Test Script
# This script helps test the item pickup fix in multiplayer mode

echo "=========================================="
echo "Multiplayer Threading Fix Test"
echo "=========================================="
echo ""
echo "This test verifies that the deferred operation mechanism"
echo "prevents crashes when picking up items in multiplayer mode."
echo ""

# Check if JARs exist
if [ ! -f "build/libs/woodlanders-server.jar" ]; then
    echo "ERROR: Server JAR not found. Please run 'gradle build' first."
    exit 1
fi

if [ ! -f "build/libs/woodlanders-client.jar" ]; then
    echo "ERROR: Client JAR not found. Please run 'gradle build' first."
    exit 1
fi

echo "Test Steps:"
echo "1. Start the dedicated server (this script will do this)"
echo "2. Connect two clients to localhost:25565"
echo "3. Destroy an AppleTree and verify apple pickup works without crash"
echo "4. Destroy a BananaTree and verify banana pickup works without crash"
echo "5. Test rapid item pickups from multiple trees"
echo "6. Verify both clients remain stable throughout testing"
echo ""
echo "Expected Results:"
echo "- No OpenGL context errors"
echo "- No crashes when picking up items"
echo "- Items disappear correctly for all clients"
echo "- Smooth gameplay without freezing"
echo ""

read -p "Press Enter to start the dedicated server..."

echo ""
echo "Starting dedicated server on port 25565..."
echo "Server logs will appear below:"
echo "=========================================="

# Start the server
java -jar build/libs/woodlanders-server.jar

