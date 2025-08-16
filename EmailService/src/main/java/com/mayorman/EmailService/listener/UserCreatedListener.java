package com.mayorman.EmailService.listener;

import com.mayorman.EmailService.model.EmployeeDto;
import com.mayorman.EmailService.model.UserCreatedEventDto;
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
    public void handleUserCreatedEvent(UserCreatedEventDto eventDto) { // <-- Use the new DTO
        logger.info("Received User Created event for email: {}", eventDto.getEmail());

        // --- BUILD THE VERIFICATION LINK ---
        // Note: You should put the gateway URL in your application.properties
        String verificationUrl = "http://localhost:9082/employees/verify?token=" + eventDto.getVerificationToken();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(environment.getProperty("spring.mail.from"));
        message.setTo(eventDto.getEmail());
        message.setSubject("Welcome! Please Verify Your Account");

        // --- UPDATE THE EMAIL TEXT ---
        String emailText = "Hello " + eventDto.getFirstName() + ",\n\n"
                + "Welcome aboard! Please click the link below to verify your account:\n"
                + verificationUrl;
        message.setText(emailText);

        try {
            emailSender.send(message);
            logger.info("Verification email sent successfully to {}", eventDto.getEmail());
        } catch (Exception e) {
            logger.error("Error sending verification email to {}: {}", eventDto.getEmail(), e.getMessage());
        }
    }
}
