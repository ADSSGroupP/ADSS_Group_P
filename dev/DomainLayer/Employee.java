package DomainLayer;
import java.util.List;
import java.time.LocalDate;
import java.util.ArrayList;
import java.time.DayOfWeek;


/**
 * Represents an employee in the Super-Lee system.
 * This class holds personal information, professional roles, and financial data.
 */
public class Employee {
    private String name;
    private int id;
    private double salary;
    private int bank_num;
    private int branch_num;
    private int account_num;
    private Boolean is_shift_manager;
    private DayOfWeek day_off;
    private JobTerms terms;
    private Role[] roles;
    private List<Constraint> currentConstraints;
    private String license ;
    private int branch_id;

    // Constructor
    public Employee(String name, int id, Role[] roles, int bank_num, int branch_num, int account_num, DayOfWeek day_off, LocalDate start_date, String job_scope, double global_wage, double hourly_wage, String license, int branch_id) {
        this.name = name;
        this.id = id;
        this.bank_num = bank_num;
        this.branch_num = branch_num;
        this.account_num = account_num;
        this.day_off = day_off;
        this.is_shift_manager = false;
        this.terms= new JobTerms(start_date,job_scope, global_wage,hourly_wage);
        this.roles = roles;
        this.currentConstraints = new ArrayList<>();
        this.license = license;
        this.branch_id=branch_id;
    }
    public void setIs_shift_manager(){
        is_shift_manager=true;
    }
    public int getId(){
        return this.id;
    }
    public String getName(){
        return this.name;
    }
    public Role[] getRoles(){
        return roles;
    }

    public void setRoles(Role[] r){
        this.roles=r;
    }

    public void addConstraint(Constraint c) {
        this.currentConstraints.add(c);
    }

    public void clearConstraints() {
        this.currentConstraints.clear();
    }

    public List<Constraint> getCurrentConstraints() {
        return currentConstraints;
    }

    public DayOfWeek getDay_off() {
        return day_off;
    }

    public void removeConstraintByDate(LocalDate date) {
        currentConstraints.removeIf(c -> c.getDate().equals(date));
    }

    public String getLicense(){return this.license;}
}

