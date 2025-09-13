package com.mayorman.EmailService.listener;

import com.mayorman.EmailService.model.EmployeeDto;
import com.mayorman.EmailService.model.PasswordResetEventDto;
import com.mayorman.EmailService.model.UserCreatedEventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @RabbitListener(queues = "user-created-email-queue")
    public void handleUserCreatedEvent(UserCreatedEventDto eventDto) {
        logger.info("Received User Created event for email: {}", eventDto.getEmail());

        String verificationUrl = environment.getProperty("app.gateway.url") + "/employees/verify?token="
                + eventDto.getVerificationToken();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(environment.getProperty("spring.mail.from"));
        message.setTo(eventDto.getEmail());
        message.setSubject("Welcome! Please Verify Your Account");

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
    @RabbitListener(queues = "password-reset-email-queue")
    public void handlePasswordResetEvent(PasswordResetEventDto eventDto) {
        logger.info("Received Password Reset event for email: {}", eventDto.getEmail());
        String resetToken = "The password reset token is " + eventDto.getPasswordResetToken();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(environment.getProperty("spring.mail.from"));
        message.setTo(eventDto.getEmail());
        message.setSubject("Your Password Reset Request");

        String emailText = "Hello " + eventDto.getFirstName() + ",\n\n"
                + "A password reset was requested for your account. Please use the token below to set a new password:\n"
                + resetToken + "\n\n"
                + "If you did not request this, you can safely ignore this email.";
        message.setText(emailText);

        try {
            emailSender.send(message);
            logger.info("Password reset email sent successfully to {}", eventDto.getEmail());
        } catch (Exception e) {
            logger.error("Error sending password reset email to {}: {}", eventDto.getEmail(), e.getMessage());
        }
    }
}
