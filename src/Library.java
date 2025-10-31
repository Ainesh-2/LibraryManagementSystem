import java.util.*;
import java.sql.*;
public class Library
{
    static final String URL = "jdbc:postgresql://localhost:5432/librarydb";
    static final String USER = "postgres";
    static final String PASS = "ainesh";
    static Scanner in = new Scanner(System.in);
    public static void main(String[] args)
    {
    	System.out.println("\n-----Welcome To Library-----\n");
    	ReminderScheduler.schedule(9, 0);
        while(true)
        {
            System.out.println("1. Add Student");
            System.out.println("2. Remove Student");
            System.out.println("3. Add Book");
            System.out.println("4. Issue Book");
            System.out.println("5. Return Book");
            System.out.println("6. View Students");
            System.out.println("7. View Books");
            System.out.println("8. View Issues");
            System.out.println("9. View Due Today & Overdues");
            System.out.println("10. Run Reminder Job Now");
            System.out.println("0. Exit");
            System.out.print("Enter Choice: ");
            int ch = in.nextInt();
            switch (ch)
            {
                case 1:
                	System.out.println("--------------------");
                	addStudent();
                	break;
                case 2:
                	System.out.println("------------------------------");
                	removeStudent();
                	break;
                case 3:
                	System.out.println("--------------------");
                	addBook();
                	break;
                case 4:
                	System.out.println("--------------------");
                	issueBook();
                	break;
                case 5:
                	System.out.println("--------------------");
                	returnBook();
                	break;
                case 6:
                	System.out.println("--------------------");
                	viewStudents();
                	break;
                case 7:
                	System.out.println("--------------------");
                	viewBooks();
                	break;
                case 8:
                	System.out.println("--------------------");
                	viewIssues();
                	break;
                case 9:
                	System.out.println("----------------------------");
                	viewDueAndOverdue();
                	break;
                case 10:
                	System.out.println("--------------------");
                	System.out.println("Starting Job...");
                	try {
                	new ReminderJob().run();
                	System.out.println("Job Finished.");
                	System.out.println("--------------------");
                	}
                	catch (Exception ex) {
                		ex.printStackTrace();
                	}
                    break;
                case 0:
                	System.out.println("--------------------");
                	System.out.println("Exiting...");
                	System.exit(0);
                	break;
                default: 
                	System.out.println("--------------------");
                	System.out.println("Invalid choice!");
                	System.out.println("--------------------");
            }
        }
    }
    
    static Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection(URL, USER, PASS);
    }
    
    static void addStudent()
    {
        try(Connection con = getConnection())
        {
            System.out.print("Enter Student ID: ");
            int id = in.nextInt();
            in.nextLine();
            System.out.print("Enter Name: ");
            String name = in.nextLine();
            System.out.print("Enter Email: ");
            String email = in.nextLine();
            String sql = "INSERT INTO students (id, name, email) VALUES (?, ?, ?)";
            try(PreparedStatement pst = con.prepareStatement(sql))
            {
                pst.setInt(1, id);
                pst.setString(2, name);
                pst.setString(3, email);
                pst.executeUpdate();
                System.out.println("--------------------------------");
                System.out.println("Student added successfully!");
                System.out.println("--------------------------------\n");
            }
        }
        catch(SQLException e)
        {
            System.out.println("Error adding student: " + e.getMessage());
        }
    }
    
    static void addBook()
    {
        try(Connection con = getConnection())
        {
            System.out.print("Enter Book ID: ");
            int id = in.nextInt();
            in.nextLine();
            System.out.print("Enter Title: ");
            String title = in.nextLine();
            System.out.print("Enter Author: ");
            String author = in.nextLine();
            String sql = "INSERT INTO books (id, title, author) VALUES (?, ?, ?)";
            try(PreparedStatement pst = con.prepareStatement(sql))
            {
                pst.setInt(1, id);
                pst.setString(2, title);
                pst.setString(3, author);
                pst.executeUpdate();
                System.out.println("-----------------------------");
                System.out.println("Book added successfully!");
                System.out.println("-----------------------------\n");
            }
        }
        catch(SQLException e)
        {
            System.out.println("Error adding book: " + e.getMessage());
        }
    }
    
    static void issueBook()
    {
        try (Connection con = getConnection())
        {
            System.out.print("Enter Book ID: ");
            int bookId = in.nextInt();
            System.out.print("Enter Student ID: ");
            int studentId = in.nextInt();
            String check = "SELECT issued FROM books WHERE id=?";
            PreparedStatement chk = con.prepareStatement(check);
            chk.setInt(1, bookId);
            ResultSet rs = chk.executeQuery();
            if (rs.next() && !rs.getBoolean("issued"))
            {
                Timestamp issueDate = new Timestamp(System.currentTimeMillis());
                Calendar cal = Calendar.getInstance();
                cal.setTime(issueDate);
                cal.add(Calendar.DAY_OF_MONTH, 14);
                Timestamp returnDate = new Timestamp(cal.getTimeInMillis());
                String issue = "INSERT INTO issues (book_id, student_id, issue_date, return_date) VALUES (?, ?, ?, ?)";
                PreparedStatement pst = con.prepareStatement(issue);
                pst.setInt(1, bookId);
                pst.setInt(2, studentId);
                pst.setTimestamp(3, issueDate);
                pst.setTimestamp(4, returnDate);
                pst.executeUpdate();
                String update = "UPDATE books SET issued=true WHERE id=?";
                PreparedStatement up = con.prepareStatement(update);
                up.setInt(1, bookId);
                up.executeUpdate();
                System.out.println("\nBook Issued!");
                System.out.println("Return Date: " + returnDate);
                System.out.println("-------------------------------------\n");
            }
            else
            {
            	System.out.println("-----------------------");
                System.out.println("Book not available!");
                System.out.println("-----------------------\n");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    static void returnBook()
    {
        try(Connection con = getConnection())
        {
            System.out.print("Enter Book ID: ");
            int bookId = in.nextInt();
            String findBook = "SELECT issued FROM books WHERE id = ?";
            try (PreparedStatement fb = con.prepareStatement(findBook)) {
                fb.setInt(1, bookId);
                try (ResultSet r = fb.executeQuery()) {
                    if (!r.next()) {
                        System.out.println("No book found with ID " + bookId + ".");
                        System.out.println("-----------------------\n");
                        return;
                    }
                    if (!r.getBoolean("issued")) {
                        System.out.println("Book " + bookId + " is not currently issued.");
                        System.out.println("---------------------------------------------\n");
                        return;
                    }
                }
            }
            String delete = "DELETE FROM issues WHERE book_id=?";
            PreparedStatement pst = con.prepareStatement(delete);
            pst.setInt(1, bookId);
            pst.executeUpdate();
            String update = "UPDATE books SET issued=false WHERE id=?";
            PreparedStatement up = con.prepareStatement(update);
            up.setInt(1, bookId);
            up.executeUpdate();
            System.out.println("Book Returned!");
            System.out.println("--------------------\n");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    static void viewStudents()
    {
        try(Connection con = getConnection())
        {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM students");
            System.out.println("\n---------- Students ----------");
            while(rs.next())
            {
                System.out.println(rs.getInt("id") + " | " + rs.getString("name") + " | " + rs.getString("email"));
            }
            System.out.println("------------------------------\n");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    static void viewBooks()
    {
        try(Connection con = getConnection())
        {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM books");
            System.out.println("\n---------- Books ----------");
            boolean any = false;
            while(rs.next())
            {
            	any = true;
                System.out.println(rs.getInt("id") +
                		        " | " + rs.getString("title") + 
                		        " | " + rs.getString("author") + 
                		        " | Issued: " + rs.getBoolean("issued"));
            }
            if(!any) {
            	System.out.println("No Books Found.");
            }
            System.out.println("---------------------------\n");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    static void viewIssues()
    {
        try(Connection con = getConnection())
        {
            String sql = "SELECT i.id, s.name, b.title, i.issue_date, i.return_date " +
                         "FROM issues i " +
                         "JOIN students s ON i.student_id=s.id " +
                         "JOIN books b ON i.book_id=b.id";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            System.out.println("\n----- Issued Books -----");
            boolean any = false;
            Timestamp now = new Timestamp(System.currentTimeMillis());
            while(rs.next())
            {
            	any = true;
                Timestamp issueDate = rs.getTimestamp("issue_date");
                Timestamp returnDate = rs.getTimestamp("return_date");
                String overdue = "";
                
                if (returnDate != null)
                {
                    overdue = returnDate.before(now) ? "⚠️OVERDUE\n" : "";
                }
                String issueDateStr = (issueDate != null) ? issueDate.toLocalDateTime().toLocalDate().toString() : "N/A";
                String returnDateStr = (returnDate != null) ? returnDate.toLocalDateTime().toLocalDate().toString() : "N/A";
                System.out.println("IssueID: " + rs.getInt("id") +
                                   " | Student: " + rs.getString("name") +
                                   " | Book: " + rs.getString("title") +
                                   " | Issue Date: " + issueDateStr +
                                   " | Return Date: " + returnDateStr +
                                   " | " + overdue);
            }
            if(!any) { 
            	System.out.println("No Issues Found.");
            }
            System.out.println("-------------------------\n");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    static void removeStudent()
    {
        try(Connection con = getConnection())
        {
            System.out.print("\nEnter Student ID to remove: ");
            int studentId = in.nextInt();
            in.nextLine();
            String checkSql = "SELECT * FROM students WHERE id=?";
            try(PreparedStatement pst = con.prepareStatement(checkSql))
            {
                pst.setInt(1, studentId);
                ResultSet rs = pst.executeQuery();
                if (!rs.next())
                {
                    System.out.println("\nStudent Not Found!");
                    System.out.println("----------------------\n");
                    return;
                }
            }
            String checkIssue = "SELECT * FROM issues WHERE student_id=? AND return_date IS NULL";
            try(PreparedStatement pst = con.prepareStatement(checkIssue))
            {
                pst.setInt(1, studentId);
                ResultSet rs = pst.executeQuery();
                if(rs.next())
                {
                    System.out.println("\nCannot Remove Student. They have Unreturned Books!");
                    System.out.println("------------------------------------------------------\n");
                    return;
                }
            }
            String deleteSql = "DELETE FROM students WHERE id=?";
            try(PreparedStatement pst = con.prepareStatement(deleteSql))
            {
                pst.setInt(1, studentId);
                pst.executeUpdate();
                System.out.println("\nStudent Removed Successfully!");
                System.out.println("---------------------------------\n");
            }
        }
        catch(SQLException e)
        {
            System.out.println("Error Removing Student: " + e.getMessage());
        }
    }
    
    static void viewDueAndOverdue()
    {
        String base = "FROM issues i " +
                      "JOIN students s ON i.student_id = s.id " +
                      "JOIN books b ON i.book_id = b.id ";
        String dueSql = "SELECT i.id, s.name, s.email, b.title, i.issue_date, i.return_date " +
                        base +
                        "WHERE i.return_date IS NOT NULL AND DATE(i.return_date) = CURRENT_DATE";
        String overSql = "SELECT i.id, s.name, s.email, b.title, i.issue_date, i.return_date " +
                         base +
                         "WHERE i.return_date IS NOT NULL AND DATE(i.return_date) < CURRENT_DATE";
        try (Connection con = getConnection())
        {
            try(Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(dueSql))
            {
                System.out.println("\n---------- Due Today ----------");
                boolean due = false;
                while(rs.next())
                {
                    due = true;
                    java.sql.Date dueDate = new java.sql.Date(rs.getTimestamp("return_date").getTime());
                    System.out.println("IssueID: " + rs.getInt("id") +
                                       " | Student: " + rs.getString("name") +
                                       " | Email: " + rs.getString("email") +
                                       " | Book: " + rs.getString("title") +
                                       " | Due Date: " + dueDate + " 📅 Due Today");
                }
                if (!due) System.out.println("No returns today.");
            }
            try(Statement st2 = con.createStatement();
                ResultSet rs2 = st2.executeQuery(overSql))
            {
                System.out.println("\n---------- Overdue ----------");
                boolean overdue = false;
                while (rs2.next())
                {
                    overdue = true;
                    java.sql.Date dueDate = new java.sql.Date(rs2.getTimestamp("return_date").getTime());
                    System.out.println("IssueID: " + rs2.getInt("id") +
                                       " | Student: " + rs2.getString("name") +
                                       " | Email: " + rs2.getString("email") +
                                       " | Book: " + rs2.getString("title") +
                                       " | Due Date: " + dueDate + " ⚠️ Overdue");
                }
                if (!overdue) System.out.println("\nNo overdue books.");
                System.out.println("---------------------------------\n");
            }
        }
        catch(SQLException e)
        {
            System.out.println("Database error while fetching due/overdue books: " + e.getMessage());
        }
    }

    static void updateOverdueTable() {
        String overdueSelect = "SELECT i.id as issue_id, s.id as student_id, s.name as student_name, s.email as student_email, " +
                               "b.id as book_id, b.title as book_title, i.return_date as due_date " +
                               "FROM issues i " +
                               "JOIN students s ON i.student_id = s.id " +
                               "JOIN books b ON i.book_id = b.id " +
                               "WHERE i.return_date < CURRENT_DATE";
        String checkExistSql = "SELECT 1 FROM overdue WHERE issue_id = ?";
        String insertOverdueSql = "INSERT INTO overdue (issue_id, student_id, student_name, student_email, book_id, book_title, due_date, overdue_date) " +
                                  "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_DATE)";
        try(Connection con = getConnection())
        {
            PreparedStatement checkExistStmt = con.prepareStatement(checkExistSql);
            PreparedStatement insertStmt = con.prepareStatement(insertOverdueSql);
            Statement selectStmt = con.createStatement();
            ResultSet rs = selectStmt.executeQuery(overdueSelect);

            while(rs.next())
            {
                int issueId = rs.getInt("issue_id");
                checkExistStmt.setInt(1, issueId);
                ResultSet checkRs = checkExistStmt.executeQuery();

                if(!checkRs.next())
                {
                	insertStmt.setInt(1, issueId);
                    insertStmt.setInt(2, rs.getInt("student_id"));
                    insertStmt.setString(3, rs.getString("student_name"));
                    insertStmt.setString(4, rs.getString("student_email"));
                    insertStmt.setInt(5, rs.getInt("book_id"));
                    insertStmt.setString(6, rs.getString("book_title"));
                    insertStmt.setDate(7, rs.getDate("due_date"));
                    insertStmt.executeUpdate();
                    System.out.println("Added overdue record for IssueID: " + issueId);
                }
                checkRs.close();
            }

            checkExistStmt.close();
            insertStmt.close();
            selectStmt.close();

        }
        catch (SQLException e)
        {
            System.out.println("Error updating overdue table: " + e.getMessage());
        }
    }
}
