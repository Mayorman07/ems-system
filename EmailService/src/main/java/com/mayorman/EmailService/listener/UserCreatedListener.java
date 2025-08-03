package com.mayorman.EmailService.listener;

import com.mayorman.EmailService.model.EmployeeDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class UserCreatedListener {

    private static final Logger logger = LoggerFactory.getLogger(UserCreatedListener.class);

    @Autowired
    private JavaMailSender emailSender;
    @Autowired
    private Environment environment;


    // This annotation is the magic part. It tells Spring that this method
    // should automatically be triggered whenever a message arrives
    // in the "user-created-email-queue".
    @RabbitListener(queues = "user-created-email-queue")
    public void handleUserCreatedEvent(EmployeeDto employeeDto) {
//        logger.info("Received User Created event for email: {}", employeeDto.getEmail());
        logger.info("Received User Created event for email: {}", employeeDto.getEmail());

        // Create the welcome email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(environment.getProperty("spring.mail.from"));// This can be any "from" address you've verified
        message.setTo(employeeDto.getEmail());
        message.setSubject("Welcome to the Employee Management System!");
        message.setText("Hello " + employeeDto.getFirstName() + ",\n\nWelcome aboard! We are excited to have you.");

        try {
            // Send the email using the JavaMailSender we configured
            emailSender.send(message);
            logger.info("Welcome email sent successfully to {}", employeeDto.getEmail());
        } catch (Exception e) {
            // Log an error if the email fails to send for any reason
            logger.error("Error sending welcome email to {}: {}", employeeDto.getEmail(), e.getMessage());
        }
    }
}
