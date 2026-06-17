package DomainLayer;
import java.time.LocalDate;


/**
 * Encapsulates the employment contract details for an employee.
 * Includes start date, work scope, and compensation details.
 */
public class JobTerms {
    private LocalDate start_date;
    private String job_scope;
    private double global_wage;
    private double hourly_wage;


    // Constructor to initialize employment terms
    public JobTerms(LocalDate start_date,String job_scope,double global_wage,double hourly_wage){
        this.job_scope=job_scope;
        this.start_date=start_date;
        this.global_wage=global_wage;
        this.hourly_wage=hourly_wage;
    }
}
