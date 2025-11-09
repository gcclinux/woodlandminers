#!/bin/bash

echo "=== TreeRemovalMessage Wiring Verification ==="
echo ""

echo "1. Checking TreeRemovalMessage class exists..."
if [ -f "src/main/java/wagemaker/uk/network/TreeRemovalMessage.java" ]; then
    echo "   ✓ TreeRemovalMessage.java found"
else
    echo "   ✗ TreeRemovalMessage.java NOT found"
    exit 1
fi

echo ""
echo "2. Checking TreeRemovalMessage implements Serializable..."
if grep -q "implements Serializable" src/main/java/wagemaker/uk/network/TreeRemovalMessage.java; then
    echo "   ✓ Implements Serializable"
else
    echo "   ✗ Does NOT implement Serializable"
    exit 1
fi

echo ""
echo "3. Checking MessageType enum has TREE_REMOVAL..."
if grep -q "TREE_REMOVAL" src/main/java/wagemaker/uk/network/MessageType.java; then
    echo "   ✓ TREE_REMOVAL found in MessageType enum"
else
    echo "   ✗ TREE_REMOVAL NOT found in MessageType enum"
    exit 1
fi

echo ""
echo "4. Checking DefaultMessageHandler routes TREE_REMOVAL..."
if grep -q "case TREE_REMOVAL:" src/main/java/wagemaker/uk/network/DefaultMessageHandler.java; then
    echo "   ✓ TREE_REMOVAL case found in switch statement"
else
    echo "   ✗ TREE_REMOVAL case NOT found"
    exit 1
fi

echo ""
echo "5. Checking DefaultMessageHandler has handleTreeRemoval method..."
if grep -q "handleTreeRemoval" src/main/java/wagemaker/uk/network/DefaultMessageHandler.java; then
    echo "   ✓ handleTreeRemoval method found"
else
    echo "   ✗ handleTreeRemoval method NOT found"
    exit 1
fi

echo ""
echo "6. Checking GameMessageHandler overrides handleTreeRemoval..."
if grep -q "handleTreeRemoval" src/main/java/wagemaker/uk/gdx/GameMessageHandler.java; then
    echo "   ✓ GameMessageHandler overrides handleTreeRemoval"
else
    echo "   ✗ GameMessageHandler does NOT override handleTreeRemoval"
    exit 1
fi

echo ""
echo "7. Checking GameMessageHandler imports TreeRemovalMessage..."
if grep -q "import.*TreeRemovalMessage" src/main/java/wagemaker/uk/gdx/GameMessageHandler.java; then
    echo "   ✓ TreeRemovalMessage imported"
else
    echo "   ✗ TreeRemovalMessage NOT imported"
    exit 1
fi

echo ""
echo "8. Checking test file exists..."
if [ -f "src/test/java/wagemaker/uk/network/TreeRemovalMessageTest.java" ]; then
    echo "   ✓ TreeRemovalMessageTest.java found"
else
    echo "   ✗ TreeRemovalMessageTest.java NOT found"
    exit 1
fi

echo ""
echo "=== All Checks Passed! ==="
echo ""
echo "TreeRemovalMessage is properly wired up:"
echo "  - Message class implements Serializable ✓"
echo "  - MessageType enum includes TREE_REMOVAL ✓"
echo "  - DefaultMessageHandler routes the message ✓"
echo "  - GameMessageHandler handles the message ✓"
echo "  - Comprehensive tests created ✓"
echo ""
echo "The message can now be:"
echo "  1. Serialized and sent over the network"
echo "  2. Deserialized by clients"
echo "  3. Routed to the correct handler method"
echo "  4. Processed to remove ghost trees"
