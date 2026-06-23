package devEmployees.Transportation;
import java.time.LocalDate;

/**
 * Mock class simulating the behavior and integration APIs of the external Transportation Module.
 * This class provides fixed response stubs to facilitate isolated testing of the HR and scheduling systems
 * without requiring an active instance of the delivery routing framework.
 */
public class TransportationMock {

    // Checks if there is at least one active delivery transport scheduled for a given date and shift
    public static boolean hasTransportInShift(LocalDate date, char shiftType) {
        // Hardcoded to true to guarantee that integration logic enforces constraint rules during testing
        return true;
    }

    // Retrieves the total volume of distinct transport dispatches assigned to a specific shift window
    public static int getTransportCount(LocalDate date, char shiftType) {
        // Returns a static value of 2 shipments to test dynamic roster bounds checking
        return 2;
    }

    // Evaluates and extracts the strict licensing benchmark required to legally operate a specific transport route
    public static String getRequiredLicenseForTransport(int transportId) {
        // Maps all simulated route nodes to the standard "C1" medium truck license signature
        return "C1";
    }

    // Generates a structural placeholder ID representing the primary active transport record
    public static int getTransportId() {
        // Fixed numerical marker representing the tracking sequence context
        return 123;
    }
}