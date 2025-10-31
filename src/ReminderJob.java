import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class ReminderJob implements Runnable {
    private final ParaphraserClient para;
    private final EmailService email;

    public ReminderJob() {
        this.para  = new ParaphraserClient(System.getenv("PARA_API_KEY"));
        this.email = new EmailService();
    }

    public ReminderJob(ParaphraserClient para, EmailService email) {
        this.para = para;
        this.email = email;
    }

    static class Reminder {
        final String email, name, book;
        final LocalDate dueDate;
        Reminder(String e, String n, String b, LocalDate d) {
            this.email = e; this.name = n; this.book = b; this.dueDate = d;
        }
    }

    @Override
    public void run() {
        try (Connection con = Library.getConnection()) {

            List<Reminder> dueToday = query(con,
                "SELECT s.email, s.name, b.title, DATE(i.return_date) AS due_date " +
                "FROM issues i " +
                "JOIN students s ON i.student_id = s.id " +
                "JOIN books b ON i.book_id = b.id " +
                "WHERE i.return_date IS NOT NULL AND DATE(i.return_date) = CURRENT_DATE");

            List<Reminder> overdue = query(con,
                "SELECT s.email, s.name, b.title, DATE(i.return_date) AS due_date " +
                "FROM issues i " +
                "JOIN students s ON i.student_id = s.id " +
                "JOIN books b ON i.book_id = b.id " +
                "WHERE i.return_date IS NOT NULL AND DATE(i.return_date) < CURRENT_DATE");

            for (Reminder r : dueToday) {
                String subject = "Due today: " + r.book;
                String body = "Hi " + r.name + ",\n\n"
                    + "Your book \"" + r.book + "\" is due today (" + r.dueDate + "). "
                    + "Please return it to avoid fines.\n\n"
                    + "— Library LMS";
                String safe = para.tryParaphrase(body);
                try {
                    email.send(r.email, subject, safe);
                } catch (Exception ignored) { }
            }

            for (Reminder r : overdue) {
                String subject = "Overdue: " + r.book;
                String body = "Hi " + r.name + ",\n\n"
                    + "Your book \"" + r.book + "\" was due on " + r.dueDate + ". "
                    + "Please return it as soon as possible to minimize fines.\n\n"
                    + "— Library LMS";
                String safe = para.tryParaphrase(body);
                try {
                    email.send(r.email, subject, safe);
                } catch (Exception ignored) { }
            }

        } catch (SQLException ignored) { }
    }

    private List<Reminder> query(Connection con, String sql) throws SQLException {
        List<Reminder> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String email = rs.getString(1);
                String name  = rs.getString(2);
                String book  = rs.getString(3);
                Date d       = rs.getDate(4);
                list.add(new Reminder(email, name, book, d.toLocalDate()));
            }
        }
        return list;
    }
}
