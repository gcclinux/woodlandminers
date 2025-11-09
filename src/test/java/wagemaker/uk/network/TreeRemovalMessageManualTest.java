package wagemaker.uk.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Manual test to verify TreeRemovalMessage serialization and message routing.
 * Run this class directly to verify the implementation.
 */
public class TreeRemovalMessageManualTest {
    
    public static void main(String[] args) {
        System.out.println("=== TreeRemovalMessage Manual Verification ===\n");
        
        boolean allTestsPassed = true;
        
        // Test 1: Message creation
        System.out.println("Test 1: Creating TreeRemovalMessage...");
        try {
            TreeRemovalMessage message = new TreeRemovalMessage("server", "100,200", "Ghost tree detected");
            System.out.println("  ✓ Message created successfully");
            System.out.println("    - Sender ID: " + message.getSenderId());
            System.out.println("    - Tree ID: " + message.getTreeId());
            System.out.println("    - Reason: " + message.getReason());
            System.out.println("    - Type: " + message.getType());
        } catch (Exception e) {
            System.out.println("  ✗ FAILED: " + e.getMessage());
            allTestsPassed = false;
        }
        
        // Test 2: Serialization
        System.out.println("\nTest 2: Testing serialization...");
        try {
            TreeRemovalMessage original = new TreeRemovalMessage("server", "256,512", "Test reason");
            
            // Serialize
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
            objectOut.writeObject(original);
            objectOut.flush();
            byte[] serializedData = byteOut.toByteArray();
            
            System.out.println("  ✓ Serialization successful");
            System.out.println("    - Serialized size: " + serializedData.length + " bytes");
            
            // Deserialize
            ByteArrayInputStream byteIn = new ByteArrayInputStream(serializedData);
            ObjectInputStream objectIn = new ObjectInputStream(byteIn);
            Object deserializedObject = objectIn.readObject();
            
            if (deserializedObject instanceof TreeRemovalMessage) {
                TreeRemovalMessage deserialized = (TreeRemovalMessage) deserializedObject;
                System.out.println("  ✓ Deserialization successful");
                
                // Verify data integrity
                boolean dataMatches = original.getSenderId().equals(deserialized.getSenderId()) &&
                                    original.getTreeId().equals(deserialized.getTreeId()) &&
                                    original.getReason().equals(deserialized.getReason()) &&
                                    original.getType() == deserialized.getType();
                
                if (dataMatches) {
                    System.out.println("  ✓ Data integrity verified");
                } else {
                    System.out.println("  ✗ FAILED: Data mismatch after deserialization");
                    allTestsPassed = false;
                }
            } else {
                System.out.println("  ✗ FAILED: Deserialized object is not TreeRemovalMessage");
                allTestsPassed = false;
            }
            
            objectOut.close();
            objectIn.close();
            
        } catch (Exception e) {
            System.out.println("  ✗ FAILED: " + e.getMessage());
            e.printStackTrace();
            allTestsPassed = false;
        }
        
        // Test 3: Message routing
        System.out.println("\nTest 3: Testing message routing...");
        try {
            final boolean[] handlerCalled = {false};
            final TreeRemovalMessage[] receivedMessage = {null};
            
            DefaultMessageHandler handler = new DefaultMessageHandler() {
                @Override
                protected void handleTreeRemoval(TreeRemovalMessage message) {
                    handlerCalled[0] = true;
                    receivedMessage[0] = message;
                }
            };
            
            TreeRemovalMessage testMessage = new TreeRemovalMessage("server", "400,400", "Routing test");
            handler.handleMessage(testMessage);
            
            if (handlerCalled[0]) {
                System.out.println("  ✓ Handler method called");
                
                if (receivedMessage[0] != null && 
                    receivedMessage[0].getTreeId().equals("400,400")) {
                    System.out.println("  ✓ Message routed correctly");
                } else {
                    System.out.println("  ✗ FAILED: Message data incorrect");
                    allTestsPassed = false;
                }
            } else {
                System.out.println("  ✗ FAILED: Handler method not called");
                allTestsPassed = false;
            }
            
        } catch (Exception e) {
            System.out.println("  ✗ FAILED: " + e.getMessage());
            e.printStackTrace();
            allTestsPassed = false;
        }
        
        // Test 4: MessageType enum
        System.out.println("\nTest 4: Verifying MessageType enum...");
        try {
            MessageType type = MessageType.TREE_REMOVAL;
            System.out.println("  ✓ TREE_REMOVAL exists in MessageType enum");
            System.out.println("    - Enum value: " + type);
        } catch (Exception e) {
            System.out.println("  ✗ FAILED: TREE_REMOVAL not found in MessageType enum");
            allTestsPassed = false;
        }
        
        // Test 5: Null handling
        System.out.println("\nTest 5: Testing null reason handling...");
        try {
            TreeRemovalMessage messageWithNullReason = new TreeRemovalMessage("server", "500,500", null);
            System.out.println("  ✓ Message created with null reason");
            System.out.println("    - Tree ID: " + messageWithNullReason.getTreeId());
            System.out.println("    - Reason: " + messageWithNullReason.getReason());
        } catch (Exception e) {
            System.out.println("  ✗ FAILED: " + e.getMessage());
            allTestsPassed = false;
        }
        
        // Summary
        System.out.println("\n=== Test Summary ===");
        if (allTestsPassed) {
            System.out.println("✓ ALL TESTS PASSED");
            System.out.println("\nTreeRemovalMessage is properly wired up:");
            System.out.println("  - Message can be created ✓");
            System.out.println("  - Message can be serialized ✓");
            System.out.println("  - Message can be deserialized ✓");
            System.out.println("  - Message routing works ✓");
            System.out.println("  - MessageType enum includes TREE_REMOVAL ✓");
            System.out.println("\nThe message is ready for end-to-end network communication.");
            System.exit(0);
        } else {
            System.out.println("✗ SOME TESTS FAILED");
            System.exit(1);
        }
    }
}
