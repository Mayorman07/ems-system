package com.mayorman.employees.scheduler;
import com.mayorman.employees.services.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
    private final EmployeeService employeeService;

    @Autowired
    public ScheduledTasks(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // This will run at 2 AM every day.
//    @Scheduled(cron = "0 0 2 * * ?")
    @Scheduled(cron = "0 0/3 * * * ?")
    public void deactivateInactiveUsers() {
        logger.info("Running scheduled task to deactivate inactive users...");
        int deactivatedCount = employeeService.deactivateInactiveUsers();
        logger.info("Scheduled task finished. Deactivated {} users.", deactivatedCount);
    }
}
