package wagemaker.uk.server;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ServerConfig planting range validation.
 * Tests loading, validation, and fallback behavior for planting.max.range property.
 */
public class ServerConfigTest {
    
    private File testConfigFile;
    private ByteArrayOutputStream outputStream;
    private ByteArrayOutputStream errorStream;
    private PrintStream originalOut;
    private PrintStream originalErr;
    
    @BeforeEach
    public void setUp() throws IOException {
        // Create a temporary test config file
        testConfigFile = File.createTempFile("test-server", ".properties");
        testConfigFile.deleteOnExit();
        
        // Capture System.out and System.err for logging verification
        outputStream = new ByteArrayOutputStream();
        errorStream = new ByteArrayOutputStream();
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(errorStream));
    }
    
    @AfterEach
    public void tearDown() {
        // Restore original streams
        System.setOut(originalOut);
        System.setErr(originalErr);
        
        // Clean up test file
        if (testConfigFile != null && testConfigFile.exists()) {
            testConfigFile.delete();
        }
    }
    
    /**
     * Helper method to create a properties file with the given planting range value.
     */
    private void createConfigFile(String plantingRangeValue) throws IOException {
        try (FileWriter writer = new FileWriter(testConfigFile)) {
            writer.write("# Test configuration\n");
            if (plantingRangeValue != null) {
                writer.write("planting.max.range=" + plantingRangeValue + "\n");
            }
            writer.write("server.port=25565\n");
        }
    }
    
    /**
     * Helper method to get the captured output.
     */
    private String getOutput() {
        return outputStream.toString();
    }
    
    /**
     * Helper method to get the captured error output.
     */
    private String getErrorOutput() {
        return errorStream.toString();
    }
    
    @Test
    public void testLoadValidRangeMinimum() throws IOException {
        // Test loading minimum valid range value (64)
        createConfigFile("64");
        
        ServerConfig config = ServerConfig.load(testConfigFile.getAbsolutePath());
        
        assertEquals(64, config.getPlantingMaxRange(), 
                     "Planting range should be 64 for minimum valid value");
        
        String output = getOutput();
        assertTrue(output.contains("64 pixels"), 
                   "Output should log the configured range in pixels");
        assertTrue(output.contains("1 tiles"), 
                   "Output should log the configured range in tiles");
    }
    
    @Test
    public void testLoadValidRangeDefault() throws IOException {
        // Test loading default range value (512)
        createConfigFile("512");
        
        ServerConfig config = ServerConfig.load(testConfigFile.getAbsolutePath());
        
        assertEquals(512, config.getPlantingMaxRange(), 
                     "Planting range should be 512 for default value");
        
        String output = getOutput();
        assertTrue(output.contains("512 pixels"), 
                   "Output should log the configured range in pixels");
        assertTrue(output.contains("8 tiles"), 
                   "Output should log the configured range in tiles");
    }
    
    @Test
    public void testLoadValidRangeMaximum() throws IOException {
        // Test loading maximum valid range value (1024)
        createConfigFile("1024");
        
        ServerConfig config = ServerConfig.load(testConfigFile.getAbsolutePath());
        
        assertEquals(1024, config.getPlantingMaxRange(), 
                     "Planting range should be 1024 for maximum valid value");
        
        String output = getOutput();
        assertTrue(output.contains("1024 pixels"), 
                   "Output should log the configured range in pixels");
        assertTrue(output.contains("16 tiles"), 
                   "Output should log the configured range in tiles");
    }
    
    @Test
    public void testLoadBelowMinimumValue() throws IOException {
        // Test loading value below minimum (expect fallback to 512)
        createConfigFile("50");
        
        ServerConfig config = ServerConfig.load(testConfigFile.getAbsolutePath());
        
        assertEquals(512, config.getPlantingMaxRange(), 
                     "Planting range should fallback to 512 for value below minimum");
        
        String errorOutput = getErrorOutput();
        assertTrue(errorOutput.contains("planting.max.range"), 
                   "Error output should mention the property name");
        assertTrue(errorOutput.contains("out of range"), 
                   "Error output should indicate out of range");
        assertTrue(errorOutput.contains("512"), 
                   "Error output should mention the default value");
    }
    
    @Test
    public void testLoadAboveMaximumValue() throws IOException {
        // Test loading value above maximum (expect fallback to 512)
        createConfigFile("2000");
        
        ServerConfig config = ServerConfig.load(testConfigFile.getAbsolutePath());
        
        assertEquals(512, config.getPlantingMaxRange(), 
                     "Planting range should fallback to 512 for value above maximum");
        
        String errorOutput = getErrorOutput();
        assertTrue(errorOutput.contains("planting.max.range"), 
                   "Error output should mention the property name");
        assertTrue(errorOutput.contains("out of range"), 
                   "Error output should indicate out of range");
        assertTrue(errorOutput.contains("512"), 
                   "Error output should mention the default value");
    }
    
    @Test
    public void testLoadNonNumericValue() throws IOException {
        // Test loading non-numeric value (expect fallback to 512)
        createConfigFile("invalid");
        
        ServerConfig config = ServerConfig.load(testConfigFile.getAbsolutePath());
        
        assertEquals(512, config.getPlantingMaxRange(), 
                     "Planting range should fallback to 512 for non-numeric value");
        
        String errorOutput = getErrorOutput();
        assertTrue(errorOutput.contains("Invalid integer value"), 
                   "Error output should indicate invalid integer");
        assertTrue(errorOutput.contains("planting.max.range"), 
                   "Error output should mention the property name");
        assertTrue(errorOutput.contains("512"), 
                   "Error output should mention the default value");
    }
    
    @Test
    public void testLoadMissingProperty() throws IOException {
        // Test loading when property is missing (expect fallback to 512)
        createConfigFile(null); // Don't include planting.max.range property
        
        ServerConfig config = ServerConfig.load(testConfigFile.getAbsolutePath());
        
        assertEquals(512, config.getPlantingMaxRange(), 
                     "Planting range should fallback to 512 when property is missing");
        
        String output = getOutput();
        assertTrue(output.contains("512 pixels"), 
                   "Output should log the default range in pixels");
        assertTrue(output.contains("8 tiles"), 
                   "Output should log the default range in tiles");
    }
    
    @Test
    public void testLoadZeroValue() throws IOException {
        // Test loading zero value (expect fallback to 512)
        createConfigFile("0");
        
        ServerConfig config = ServerConfig.load(testConfigFile.getAbsolutePath());
        
        assertEquals(512, config.getPlantingMaxRange(), 
                     "Planting range should fallback to 512 for zero value");
        
        String errorOutput = getErrorOutput();
        assertTrue(errorOutput.contains("out of range"), 
                   "Error output should indicate out of range");
    }
    
    @Test
    public void testLoadNegativeValue() throws IOException {
        // Test loading negative value (expect fallback to 512)
        createConfigFile("-100");
        
        ServerConfig config = ServerConfig.load(testConfigFile.getAbsolutePath());
        
        assertEquals(512, config.getPlantingMaxRange(), 
                     "Planting range should fallback to 512 for negative value");
        
        String errorOutput = getErrorOutput();
        assertTrue(errorOutput.contains("out of range"), 
                   "Error output should indicate out of range");
    }
    
    @Test
    public void testLoadWithWhitespace() throws IOException {
        // Test loading value with whitespace (should be trimmed and parsed correctly)
        createConfigFile("  256  ");
        
        ServerConfig config = ServerConfig.load(testConfigFile.getAbsolutePath());
        
        assertEquals(256, config.getPlantingMaxRange(), 
                     "Planting range should be 256 after trimming whitespace");
    }
    
    @Test
    public void testLoggingForValidConfiguration() throws IOException {
        // Test that valid configuration logs appropriately
        createConfigFile("768");
        
        ServerConfig config = ServerConfig.load(testConfigFile.getAbsolutePath());
        
        String output = getOutput();
        assertTrue(output.contains("Configuration loaded from:"), 
                   "Output should indicate configuration was loaded");
        assertTrue(output.contains("Planting Range Configuration:"), 
                   "Output should include planting range configuration header");
        assertTrue(output.contains("768 pixels"), 
                   "Output should log the configured range in pixels");
        assertTrue(output.contains("12 tiles"), 
                   "Output should log the configured range in tiles");
    }
    
    @Test
    public void testLoggingForInvalidConfiguration() throws IOException {
        // Test that invalid configuration logs error and default
        createConfigFile("abc123");
        
        ServerConfig config = ServerConfig.load(testConfigFile.getAbsolutePath());
        
        String errorOutput = getErrorOutput();
        assertTrue(errorOutput.contains("Invalid integer value"), 
                   "Error output should indicate invalid value");
        assertTrue(errorOutput.contains("abc123"), 
                   "Error output should include the invalid value");
        assertTrue(errorOutput.contains("Using default value: 512"), 
                   "Error output should indicate default is being used");
    }
}
