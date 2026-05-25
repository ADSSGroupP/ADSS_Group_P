package Transportation;

import java.time.LocalDate;

public class TransportationMock {


    public static boolean hasTransportInShift(LocalDate date, char shiftType) {
        return true;
    }

    public static int getTransportCount(LocalDate date, char shiftType) {
        return 2;
    }

    public static String getRequiredLicenseForTransport(int transportId) {
        return "C1";
    }
    public static int getTransportId() {
        return 123;
    }
    public static String getTransportShift(int transportId) {
        return "m";
    }

}
