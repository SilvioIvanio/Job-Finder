import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/worknet";
    private static final String USER = "root";
    private static final String PASSWORD = "123456789";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Create the users table if it doesn't exist
    public static void initializeDatabase() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS users (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100),
                email VARCHAR(100) UNIQUE,
                password VARCHAR(255),
                user_type VARCHAR(20),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(createTableSQL)) {
            pstmt.executeUpdate();
            System.out.println("Database initialized successfully");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 