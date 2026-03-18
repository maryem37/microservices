package tn.enis.conge.email;

import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * 🔹 Send a password reset email (Asynchronous)
     */
    @Async
    public void sendPasswordResetEmail(String to, String newPassword) {
        String subject = "🔑 Password Reset Request";
        String text = "Hello,\n\n"
                + "Your new temporary password is: " + newPassword + "\n"
                + "Please log in and change your password immediately for security reasons.\n\n"
                + "Best regards.";

        System.out.println("📨 [RESET PASSWORD] Sending email to: " + to);
        sendEmail(to, subject, text);
    }

    /**
     * 📧 Generic email sending method
     */
    private void sendEmail(String to, String subject, String text) {
        try {
            if (to == null || !to.contains("@")) {
                System.err.println("❌ Error: Invalid email address (" + to + ")");
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom("emna.kaaniche@enis.tn");

            System.out.println("📤 Sending email...");
            mailSender.send(message);
            System.out.println("✅ Email sent successfully to " + to);
        } catch (Exception e) {
            System.err.println("❌ Error while sending email to " + to + ": " + e.getMessage());
            throw new MailAuthenticationException("SMTP Authentication failed.");
        }
    }
}