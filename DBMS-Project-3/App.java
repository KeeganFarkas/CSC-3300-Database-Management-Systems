import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * This application allows the user to connect to a database and execute
 * queries.
 */
public class App {
    public static void main(String[] args) {
        // variables to read and store user input
        java.util.Scanner scan = new java.util.Scanner(System.in);
        String choice;

        // variables to connect to the database and execute queries
        Connection conn;
        Statement stmt;
        ResultSet rset;
        int rows;
        PreparedStatement openPS = null;

        // variables to store user input for adding a new course
        String courseID;
        String title;
        String deptName;
        int credits;

        try {
            // loading the MYSQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

        } catch (ClassNotFoundException e) {
            // if the driver is not found, the application terminates
            System.err.println("Driver not found: " + e.getMessage());
            scan.close();
            System.exit(1);
        }

        try {
            // attempt to connect to the database
            conn = userConnection(scan);
            stmt = conn.createStatement();

        } catch (SQLException e) {
            // if the connection fails, the user is prompted to re-enter username and
            // password
            // or quit the application until the connection is successful
            while (true) {
                System.err.println("Connection failed: Invalid username or password");
                System.out.println();
                System.out.println("Enter the letter of the action you would like to execute: ");
                System.out.println("(r) re-enter username and password");
                System.out.println("(q) Quit");
                System.out.println();
                System.out.print(">");
                choice = scan.nextLine();

                // while loop to check if user input is valid
                while (!choice.equalsIgnoreCase("r")
                        && !choice.equalsIgnoreCase("q")) {

                    // if user input is invalid, the user is prompted to try again
                    System.out.println("Invalid choice. Please try again.");
                    System.out.println("(r) re-enter username and password");
                    System.out.println("(q) Quit");
                    System.out.println();
                    System.out.print(">");
                    choice = scan.nextLine();
                }

                // if user chooses to re-enter username and password,
                // the userConnection method is called
                if (choice.equalsIgnoreCase("r")) {
                    try {
                        conn = userConnection(scan);
                        stmt = conn.createStatement();
                        break;
                    } catch (SQLException e1) {
                        continue;
                    }

                } else {
                    // if user chooses to quit, the application terminates
                    System.out.println("Goodbye!");
                    scan.close();
                    System.exit(0);
                }

            }
        }

        // if the connection is successful, the user is prompted to choose an action
        while (true) {
            System.out.println();
            System.out.println("Enter the letter of the action you would like to execute: ");
            System.out.println("(d) Retrieve all departments");
            System.out.println("(c) Retrieve all courses");
            System.out.println("(a) Add a new course");
            System.out.println("(r) Remove a course");
            System.out.println("(m) Modify a course's title/credits");
            System.out.println("(q) Quit");
            System.out.println();
            System.out.print(">");
            choice = scan.nextLine();

            // while loop to check if user input is valid
            while (!choice.equalsIgnoreCase("d")
                    && !choice.equalsIgnoreCase("c")
                    && !choice.equalsIgnoreCase("a")
                    && !choice.equalsIgnoreCase("r")
                    && !choice.equalsIgnoreCase("m")
                    && !choice.equalsIgnoreCase("q")) {

                // if user input is invalid, the user is prompted to try again
                System.out.println();
                System.out.println("Invalid choice. Please try again.");
                System.out.println("(d) Retrieve all departments");
                System.out.println("(c) Retrieve all courses");
                System.out.println("(a) Add a new course");
                System.out.println("(r) Remove a course");
                System.out.println("(m) Modify a course's title/credits");
                System.out.println("(q) Quit");
                System.out.println();
                System.out.print(">");
                choice = scan.nextLine();
            }

            try {
                // (d) Retrieve all departments
                if (choice.equalsIgnoreCase("d")) {
                    // query to retrieve all departments
                    rset = stmt.executeQuery("SELECT dept_name, building FROM department");

                    // while loop to print out the results
                    while (rset.next()) {
                        System.out.println(rset.getString("dept_name") +
                                " | " + rset.getString("building"));
                    }
                }

                // (c) Retrieve all courses
                else if (choice.equalsIgnoreCase("c")) {
                    // query to retrieve all courses
                    rset = stmt.executeQuery(
                            "SELECT course_id, title, dept_name, credits FROM course");

                    // while loop to print out the results
                    while (rset.next()) {
                        System.out.println(rset.getString("course_id") +
                                " | " + rset.getString("title") +
                                " | " + rset.getString("dept_name") +
                                " | " + rset.getString("credits"));
                    }
                }

                // (a) Add a new course
                else if (choice.equalsIgnoreCase("a")) {
                    // prompt user for course information
                    courseID = getCourseID(scan);
                    title = getTitle(scan);
                    deptName = getDeptName(scan);
                    credits = getCredits(scan);

                    // query to add a new course
                    PreparedStatement addCourse = conn.prepareStatement(
                            "INSERT INTO course VALUES (?, ?, ?, ?)");
                    openPS = addCourse;
                    addCourse.setString(1, courseID);
                    addCourse.setString(2, title);
                    addCourse.setString(3, deptName);
                    addCourse.setInt(4, credits);
                    addCourse.executeUpdate();

                    // if no exception was thrown, the course was successfully added
                    System.out.println("Course successfully added.");
                }

                // (r) Remove a course
                else if (choice.equalsIgnoreCase("r")) {
                    // prompt user for course ID
                    courseID = getCourseID(scan);

                    // query to remove a course
                    PreparedStatement removeCourse = conn.prepareStatement(
                            "DELETE FROM course WHERE course_id = ?");
                    openPS = removeCourse;
                    removeCourse.setString(1, courseID);
                    rows = removeCourse.executeUpdate();

                    // if no rows were affected, the course ID was invalid
                    if (rows == 0) {
                        System.out.println("Course ID not found.");
                        continue;
                    }

                    // if no exception was thrown, the course was successfully removed
                    System.out.println("Course successfully removed.");
                }

                // (m) Modify a course's title/credits
                else if (choice.equalsIgnoreCase("m")) {
                    // prompt user for course ID
                    courseID = getCourseID(scan);

                    // checking if course ID is valid
                    PreparedStatement checkCourse = conn.prepareStatement(
                            "SELECT course_id FROM course WHERE course_id = ?");
                    openPS = checkCourse;
                    checkCourse.setString(1, courseID);
                    rset = checkCourse.executeQuery();

                    // if course ID is invalid, the user is returned to the main menu
                    if (!rset.next()) {
                        System.out.println("Course ID not found.");
                        continue;
                    }
                    openPS.close();

                    // user is prompted to choose whether to modify the title, credits, or both
                    System.out.println("Enter the letter of the action you would like to execute: ");
                    System.out.println("(t) Modify title");
                    System.out.println("(c) Modify credits");
                    System.out.println("(b) Modify both");
                    System.out.println("(x) Cancel");
                    System.out.println();
                    System.out.print(">");
                    choice = scan.nextLine();

                    // while loop to check if user input is valid
                    while (!choice.equalsIgnoreCase("t")
                            && !choice.equalsIgnoreCase("c")
                            && !choice.equalsIgnoreCase("b")
                            && !choice.equalsIgnoreCase("x")) {

                        // if user input is invalid, the user is prompted to try again
                        System.out.println();
                        System.out.println("Invalid choice. Please try again.");
                        System.out.println("(t) Modify title");
                        System.out.println("(c) Modify credits");
                        System.out.println("(b) Modify both");
                        System.out.println("(x) Cancel");
                        System.out.println();
                        System.out.print(">");
                        choice = scan.nextLine();
                    }

                    // (t) Modify title
                    if (choice.equalsIgnoreCase("t")) {
                        // prompt user for new title
                        title = getTitle(scan);

                        // query to modify the title
                        PreparedStatement modifyTitle = conn.prepareStatement(
                                "UPDATE course SET title = ? WHERE course_id = ?");
                        openPS = modifyTitle;
                        modifyTitle.setString(1, title);
                        modifyTitle.setString(2, courseID);
                        rows = modifyTitle.executeUpdate();

                        // if no rows were affected, the course ID was invalid
                        if (rows == 0) {
                            System.out.println("Course ID not found.");
                            continue;
                        }

                        // if no exception was thrown, the title was successfully modified
                        System.out.println("Title successfully modified.");
                    }

                    // (c) Modify credits
                    else if (choice.equalsIgnoreCase("c")) {
                        // prompt user for new credits
                        credits = getCredits(scan);

                        // query to modify the credits
                        PreparedStatement modifyCredits = conn.prepareStatement(
                                "UPDATE course SET credits = ? WHERE course_id = ?");
                        openPS = modifyCredits;
                        modifyCredits.setInt(1, credits);
                        modifyCredits.setString(2, courseID);
                        rows = modifyCredits.executeUpdate();

                        // if no rows were affected, the course ID was invalid
                        if (rows == 0) {
                            System.out.println("Course ID not found.");
                            continue;
                        }

                        // if no exception was thrown, the credits were successfully modified
                        System.out.println("Credits successfully modified.");
                    }

                    // (b) Modify both
                    else if (choice.equalsIgnoreCase("b")) {
                        // prompt user for new title and credits
                        title = getTitle(scan);
                        credits = getCredits(scan);

                        // query to modify the title and credits
                        PreparedStatement modifyTitleCredits = conn.prepareStatement(
                                "UPDATE course SET title = ?, credits = ? WHERE course_id = ?");
                        openPS = modifyTitleCredits;
                        modifyTitleCredits.setString(1, title);
                        modifyTitleCredits.setInt(2, credits);
                        modifyTitleCredits.setString(3, courseID);
                        rows = modifyTitleCredits.executeUpdate();

                        // if no rows were affected, the course ID was invalid
                        if (rows == 0) {
                            System.out.println("Course ID not found.");
                            continue;
                        }

                        // if no exception was thrown, the title and credits were successfully modified
                        System.out.println("Title and credits successfully modified.");
                    }

                    // (x) Cancel
                    else {
                        // if user chooses to cancel, the application returns to the main menu
                        continue;
                    }
                }

                // (q) Quit
                else {
                    // if user chooses to quit, the application terminates
                    System.out.println("Goodbye!");
                    scan.close();
                    stmt.close();
                    System.exit(0);
                }
            } catch (SQLException e) {
                switch (e.getErrorCode()) {
                    // Course ID already exists
                    case 1062:
                        System.err.println("Error: Course ID already exists.");
                        break;
                    // Department does not exist
                    case 1452:
                        System.err.println("Error: Department does not exist.");
                        break;
                    // unexpected error
                    default:
                        System.err.println("Error: " + e.getMessage());
                }
            } finally {
                try {
                    // closing open prepared statement
                    if (openPS != null) {
                        openPS.close();
                    }
                } catch (SQLException e) {
                    System.err.println("Failed to close prepared statement");
                }
            }
        }
    }

    /**
     * method to prompt user for username and password, and then attempt to connect
     * to
     * the database using the credentials provided
     * 
     * @param scan - a Scanner object to read user input
     * @return conn - a Connection object to connect to the database
     * @throws SQLException - if the connection fails
     */
    public static Connection userConnection(java.util.Scanner scan) throws SQLException {
        // prompt user for username and password
        System.out.print("Please enter username:");
        String username = scan.nextLine();
        System.out.print("Please enter password:");
        String password = scan.nextLine();

        // attempt to connect to the database
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/university", username, password);

        // if the connection is successful, the user is welcomed
        System.out.println("Welcome, " + username);

        return conn;
    }

    /**
     * method to prompt user for course ID
     * 
     * @param scan - a Scanner object to read user input
     * @return courseID - a String containing the course ID
     */
    public static String getCourseID(java.util.Scanner scan) {
        String courseID;
        while (true) {
            // reading in course ID
            System.out.print("Enter course ID: ");
            courseID = scan.nextLine();

            // courseID cannot be empty
            if (courseID.isEmpty()) {
                System.out.println("Course ID cannot be empty. Try again.");
                continue;
            // courseID must be 2-4 capital letters followed by a dash and 3 digits
            } else if (!courseID.matches("^[A-Z]{2,4}-\\d{3}$")) {
                System.out.println("Course ID must be 2-4 capital letters followed " +
                        "by a dash and 3 digits. Try again.");
                continue;
            }
            break;
        }
        return courseID;
    }

    /**
     * method to prompt user for course title
     * 
     * @param scan - a Scanner object to read user input
     * @return title - a String containing the course title
     */
    public static String getTitle(java.util.Scanner scan) {
        String title;
        while (true) {
            // reading in course title
            System.out.print("Enter course title: ");
            title = scan.nextLine();

            // title must be 50 characters or less
            if (title.length() > 50) {
                System.out.println("Course title must be 50 characters or less. Try again.");
                continue;
            // title cannot be empty
            } else if (title.isEmpty()) {
                System.out.println("Course title cannot be empty. Try again.");
                continue;
            }
            break;
        }
        return title;
    }

    /**
     * method to prompt user for department name
     * 
     * @param scan - a Scanner object to read user input
     * @return deptName - a String containing the department name
     */
    public static String getDeptName(java.util.Scanner scan) {
        String deptName;
        while (true) {
            // reading in department name
            System.out.print("Enter department name: ");
            deptName = scan.nextLine();

            // department name must be 20 characters or less
            if (deptName.length() > 20) {
                System.out.println("Department name must be 20 characters or less. Try again.");
                continue;
            // department name cannot be empty
            } else if (deptName.isEmpty()) {
                System.out.println("Department name cannot be empty. Try again.");
                continue;
            }
            break;
        }
        return deptName;
    }

    /**
     * method to prompt user for number of credits
     * 
     * @param scan - a Scanner object to read user input
     * @return credits - an int containing the number of credits
     */
    public static int getCredits(java.util.Scanner scan) {
        String temp;
        int credits;
        while (true) {
            // reading in number of credits
            do {
                System.out.print("Enter number of credits: ");
                temp = scan.nextLine();

                // number of credits cannot be empty
                if (temp.isEmpty()) {
                    System.out.println("Number of credits cannot be empty. Try again.");
                }
            } while (temp.isEmpty());

            try {
                credits = Integer.parseInt(temp);
            } catch (NumberFormatException e) {
                // if the user enters a non-integer, they are prompted to try again
                System.out.println("Number of credits must be an integer. Try again.");
                continue;
            }

            // number of credits must be 2 digits or less
            if (credits > 99) {
                System.out.println("Number of credits must be 2 digits or less. Try again.");
                continue;
            // number of credits must be greater than 0
            } else if (credits <= 0) {
                System.out.println("Number of credits must be greater than 0. Try again.");
                continue;
            }
            break;
        }
        return credits;
    }
}
