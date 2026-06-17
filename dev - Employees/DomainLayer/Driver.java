package DomainLayer;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class Driver extends Employee{
    private String license ;
    public Driver(String name, int id, Role[] roles, int bank_num, int branch_num, int account_num, DayOfWeek day_off, LocalDate start_date, String job_scope, double global_wage, double hourly_wage, int branch_id , String license) {
        super(name, id, roles,bank_num,branch_num,account_num,day_off,start_date,job_scope,global_wage,hourly_wage,branch_id);
        this.license = license;
    }
    public String getLicense(){
        return this.license;
    }
}
