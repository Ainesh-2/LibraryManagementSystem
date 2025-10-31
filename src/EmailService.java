import java.util.Properties;

import jakarta.mail.*;
import jakarta.mail.internet.*;

public class EmailService {

    private final Session session;
    private final String from;
    private final String user;
    private final String pass;

    public EmailService() {
        Properties p = new Properties();
        p.put("mail.smtp.host", getenv("SMTP_HOST", "smtp.gmail.com"));
        p.put("mail.smtp.port", getenv("SMTP_PORT", "587"));
        p.put("mail.smtp.auth", "true");
        p.put("mail.smtp.starttls.enable", "true");
        p.put("mail.smtp.connectiontimeout", "10000");
        p.put("mail.smtp.timeout", "15000");
        p.put("mail.smtp.writetimeout", "15000");

        this.user = getenv("SMTP_USER", "");
        this.pass = getenv("SMTP_PASS", "");
        this.from = getenv("MAIL_FROM", user);

        // Validate early to give clear errors
        if (user == null || user.isBlank())
            throw new IllegalStateException("SMTP_USER is missing; set it in Run Configuration → Environment.");
        if (pass == null || pass.isBlank())
            throw new IllegalStateException("SMTP_PASS is missing (use your 16‑char Gmail App Password).");
        if (from == null || from.isBlank())
            throw new IllegalStateException("MAIL_FROM is missing; set it or leave blank to default to SMTP_USER.");

        this.session = Session.getInstance(p, new jakarta.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });
    }

    public void send(String to, String subject, String body) throws Exception {
        if (to == null || to.isBlank())
            throw new IllegalArgumentException("Recipient address is empty.");
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from, false));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
        msg.setSubject(subject, "UTF-8");
        msg.setText(body, "UTF-8");
        Transport.send(msg);
    }

    private static String getenv(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v;
    }
}
