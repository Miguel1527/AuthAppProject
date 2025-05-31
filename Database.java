import java.sql.*;

public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/auth_db";
    private static final String USER = "root";  
    private static final String PASS = "";  

    public static class User {
        String username;
        String password;
        String fullName;
        String phone;
        String gender;
        String userType;

        public User(String username, String password, String fullName, String phone, String gender, String userType) {
            this.username = username;
            this.password = password;
            this.fullName = fullName;
            this.phone = phone;
            this.gender = gender;
            this.userType = userType;
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static boolean insertUser(Database.User user) {
        String sql = "INSERT INTO users (username, password, full_name, phone, gender, user_type) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.username);
            stmt.setString(2, user.password);
            stmt.setString(3, user.fullName);
            stmt.setString(4, user.phone);
            stmt.setString(5, user.gender);
            stmt.setString(6, user.userType);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Insert failed: " + e.getMessage());
            return false;
        }
    }

    public static Database.User getUser(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Database.User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getString("gender"),
                        rs.getString("user_type")
                );
            }
        } catch (SQLException e) {
            System.out.println("Fetch failed: " + e.getMessage());
        }
        return null;
    }
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Connection to database successful!");
            } else {
                System.out.println("❌ Connection failed.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Database error: " + e.getMessage());
        }
    }

}
