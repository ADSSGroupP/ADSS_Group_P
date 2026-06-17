package DTO;
import java.time.LocalDate;
    public class ShiftDTO {
        public LocalDate date;
        public char type; // 'm' or 'e'
        public int managerId;
        public int branchId;

        public ShiftDTO() {}
}
