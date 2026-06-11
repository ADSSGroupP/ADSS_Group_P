package DTO;

import java.time.LocalDate;
import java.time.LocalTime;

    public class ConstraintDTO {
        public int employeeId;
        public LocalDate date;
        public LocalTime startTime;
        public LocalTime endTime;
        public boolean doubleShift;
        public int extraHours;
        public boolean isChangeable;

        public ConstraintDTO() {}
}
