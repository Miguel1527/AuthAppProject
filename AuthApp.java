import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.logging.Logger;

public class AuthApp {

    private static final Logger logger = AppLogger.getLogger();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AuthApp::showLoginScreen);
    }

    public static void showLoginScreen() {
        JFrame frame = new JFrame("Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);
        frame.setLayout(new GridLayout(4, 2));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        JButton loginButton = new JButton("Login");
        JButton signupButton = new JButton("Signup");

        frame.add(new JLabel("Username:"));
        frame.add(usernameField);
        frame.add(new JLabel("Password:"));
        frame.add(passwordField);
        frame.add(loginButton);
        frame.add(signupButton);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = String.valueOf(passwordField.getPassword());
            String userType = authenticate(username, password);

            if (userType != null) {
                JOptionPane.showMessageDialog(frame, "Login successful!");
                frame.dispose();
                showDashboard(userType);
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid username or password.");
            }
        });

        signupButton.addActionListener(e -> {
            frame.dispose();
            showSignupScreen();
        });

        frame.setVisible(true);
    }

    public static void showSignupScreen() {
        JFrame frame = new JFrame("Signup");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new GridLayout(8, 2));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField fullNameField = new JTextField();
        JTextField phoneField = new JTextField();

        String[] genders = { "Male", "Female" };
        JComboBox<String> genderBox = new JComboBox<>(genders);

        String[] userTypes = { "Customer", "Employee" };
        JComboBox<String> userTypeBox = new JComboBox<>(userTypes);

        JButton registerButton = new JButton("Register");
        JButton backButton = new JButton("Back to Login");

        frame.add(new JLabel("Username:"));
        frame.add(usernameField);
        frame.add(new JLabel("Password:"));
        frame.add(passwordField);
        frame.add(new JLabel("Full Name:"));
        frame.add(fullNameField);
        frame.add(new JLabel("Phone:"));
        frame.add(phoneField);
        frame.add(new JLabel("Gender:"));
        frame.add(genderBox);
        frame.add(new JLabel("User Type:"));
        frame.add(userTypeBox);
        frame.add(registerButton);
        frame.add(backButton);

        registerButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = String.valueOf(passwordField.getPassword());
            String fullName = fullNameField.getText();
            String phone = phoneField.getText();
            String gender = genderBox.getSelectedItem().toString();
            String userType = userTypeBox.getSelectedItem().toString();

            if (register(username, password, fullName, phone, gender, userType)) {
                JOptionPane.showMessageDialog(frame, "Registration successful!");
                logger.info("User '" + username + "' registered successfully.");
                frame.dispose();
                showLoginScreen();
            } else {
                JOptionPane.showMessageDialog(frame, "Registration failed.");
                logger.severe("Registration failed for user: " + username);
            }
        });

        backButton.addActionListener(e -> {
            frame.dispose();
            showLoginScreen();
        });

        frame.setVisible(true);
    }

    public static void showDashboard(String userType) {
        if (userType == null) {
            JOptionPane.showMessageDialog(null, "Error: No user type returned.");
            return;
        }

        JFrame frame = new JFrame(userType + " Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout());

        JLabel label = new JLabel("Welcome to the " + userType + " Dashboard!", JLabel.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        frame.add(label, BorderLayout.NORTH);

        if (userType.equalsIgnoreCase("Employee")) {
            JPanel productPanel = new JPanel(new GridLayout(5, 2));
            JTextField nameField = new JTextField();
            JTextField descField = new JTextField();
            JTextField priceField = new JTextField();
            JButton addProductButton = new JButton("Add Product");

            productPanel.add(new JLabel("Product Name:"));
            productPanel.add(nameField);
            productPanel.add(new JLabel("Description:"));
            productPanel.add(descField);
            productPanel.add(new JLabel("Price:"));
            productPanel.add(priceField);
            productPanel.add(new JLabel());
            productPanel.add(addProductButton);

            addProductButton.addActionListener(e -> {
                String name = nameField.getText();
                String desc = descField.getText();
                String priceText = priceField.getText();

                try (Connection conn = Database.getConnection()) {
                    String sql = "INSERT INTO products (name, description, price) VALUES (?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, name);
                    stmt.setString(2, desc);
                    stmt.setDouble(3, Double.parseDouble(priceText));
                    int rows = stmt.executeUpdate();
                    if (rows > 0) {
                        JOptionPane.showMessageDialog(frame, "Product added!");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error adding product.");
                }
            });

            frame.add(productPanel, BorderLayout.CENTER);

        } else if (userType.equalsIgnoreCase("Customer")) {
            JTextArea productArea = new JTextArea("Available Products:\n");
            productArea.setEditable(false);

            try (Connection conn = Database.getConnection()) {
                String sql = "SELECT name, description, price FROM products";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    productArea.append("\n" + rs.getString("name") + " - $" + rs.getDouble("price") + "\n");
                    productArea.append(rs.getString("description") + "\n");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                productArea.append("\nFailed to load products.");
            }

            frame.add(new JScrollPane(productArea), BorderLayout.CENTER);
        }

        frame.setVisible(true);
    }

    private static String authenticate(String username, String password) {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT user_type FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String userType = rs.getString("user_type");
                System.out.println("UserType: " + userType);
                logger.info("User '" + username + "' logged in as " + userType);
                return userType;
            } else {
                logger.warning("Failed login attempt for username: " + username);
                return null;
            }
        } catch (Exception e) {
            logger.severe("Login error for username: " + username + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static boolean register(String username, String password, String fullName, String phone, String gender, String userType) {
        try (Connection conn = Database.getConnection()) {
            String sql = "INSERT INTO users (username, password, full_name, phone, gender, user_type) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, fullName);
            stmt.setString(4, phone);
            stmt.setString(5, gender);
            stmt.setString(6, userType);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (Exception e) {
            logger.severe("Error during registration for user: " + username + " - " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
