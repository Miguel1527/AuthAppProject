import javax.swing.*;           // GUI components
import javax.swing.table.DefaultTableModel;
import java.awt.*;              // Layout and window features
import java.sql.*;              // Database connectivity
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger; // Logging framework

public class AuthApp {

    // Logger instance for logging important events and errors
    private static final Logger logger = AppLogger.getLogger();

    public static void main(String[] args) {
        // Ensures GUI creation runs on the Event Dispatch Thread
        SwingUtilities.invokeLater(AuthApp::showLoginScreen);
    }

    // Displays the login screen
    public static void showLoginScreen() {
        JFrame frame = new JFrame("Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);
        frame.setLayout(new GridLayout(4, 2)); // 4 rows, 2 columns

        // Input fields
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        // Buttons
        JButton loginButton = new JButton("Login");
        JButton signupButton = new JButton("Signup");

        // Add components to frame
        frame.add(new JLabel("Username:"));
        frame.add(usernameField);
        frame.add(new JLabel("Password:"));
        frame.add(passwordField);
        frame.add(loginButton);
        frame.add(signupButton);

        // Login button action
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = String.valueOf(passwordField.getPassword());

            // Try to authenticate
            String userType = authenticate(username, password);

            if (userType != null) {
                JOptionPane.showMessageDialog(frame, "Login successful!");
                frame.dispose();
                showDashboard(userType, username); // Show dashboard based on user type
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid username or password.");
            }
        });

        // Signup button action
        signupButton.addActionListener(e -> {
            frame.dispose();
            showSignupScreen(); // Show signup screen
        });

        frame.setVisible(true);
    }

    // Displays the signup/registration screen
    public static void showSignupScreen() {
        JFrame frame = new JFrame("Signup");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new GridLayout(8, 2)); // 8 rows, 2 columns

        // Input fields
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField fullNameField = new JTextField();
        JTextField phoneField = new JTextField();

        // Dropdowns for gender and user type
        String[] genders = { "Male", "Female" };
        JComboBox<String> genderBox = new JComboBox<>(genders);

        String[] userTypes = { "Customer", "Employee" };
        JComboBox<String> userTypeBox = new JComboBox<>(userTypes);

        // Buttons
        JButton registerButton = new JButton("Register");
        JButton backButton = new JButton("Back to Login");

        // Add components to frame
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

        // Register button action
        registerButton.addActionListener(e -> {
            // Collect input data
            String username = usernameField.getText();
            String password = String.valueOf(passwordField.getPassword());
            String fullName = fullNameField.getText();
            String phone = phoneField.getText();
            String gender = genderBox.getSelectedItem().toString();
            String userType = userTypeBox.getSelectedItem().toString();

            // Attempt registration
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

        // Back to login action
        backButton.addActionListener(e -> {
            frame.dispose();
            showLoginScreen();
        });

        frame.setVisible(true);
    }

    // Displays dashboard based on user type
    public static void showDashboard(String userType, String username) {
        if (userType == null) {
            JOptionPane.showMessageDialog(null, "Error: No user type returned.");
            return;
        }

        if (userType.equalsIgnoreCase("Employee")) {
            JFrame frame = new JFrame(userType + " Dashboard");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 400);
            frame.setLayout(new BorderLayout());

            JLabel label = new JLabel("Welcome to the " + userType + " Dashboard!", JLabel.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 16));
            frame.add(label, BorderLayout.NORTH);

            JPanel productPanel = new JPanel(new GridLayout(6, 2));
            JTextField nameField = new JTextField();
            JTextField stockField = new JTextField();
            JTextField priceField = new JTextField();
            JButton addProductButton = new JButton("Add Product");

            productPanel.add(new JLabel("Product Name:"));
            productPanel.add(nameField);
            productPanel.add(new JLabel("Stock Amount:"));
            productPanel.add(stockField);
            productPanel.add(new JLabel("Price:"));
            productPanel.add(priceField);

            // Extra components for updating stock
            JTextField updateIdField = new JTextField();
            JTextField updateStockField = new JTextField();
            JButton updateStockButton = new JButton("Update Stock");

            productPanel.add(new JLabel("Product ID to Update:"));
            productPanel.add(updateIdField);
            productPanel.add(new JLabel("New Stock Amount:"));
            productPanel.add(updateStockField);
            productPanel.add(new JLabel());
            productPanel.add(updateStockButton);

            // Action to add product
            addProductButton.addActionListener(e -> {
                String name = nameField.getText();
                String stockText = stockField.getText();
                String priceText = priceField.getText();

                try (Connection conn = Database.getConnection()) {
                    String sql = "INSERT INTO products (name, stock, price) VALUES (?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, name);
                    stmt.setInt(2, Integer.parseInt(stockText));
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

            // Action to update stock
            updateStockButton.addActionListener(e -> {
                String idText = updateIdField.getText();
                String newStockText = updateStockField.getText();

                try (Connection conn = Database.getConnection()) {
                    String sql = "UPDATE products SET stock = ? WHERE id = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, Integer.parseInt(newStockText));
                    stmt.setInt(2, Integer.parseInt(idText));
                    int rows = stmt.executeUpdate();
                    if (rows > 0) {
                        JOptionPane.showMessageDialog(frame, "Stock updated!");
                    } else {
                        JOptionPane.showMessageDialog(frame, "Product ID not found.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error updating stock.");
                }
            });

            frame.add(productPanel, BorderLayout.CENTER);
            frame.setVisible(true);

        } else if (userType.equalsIgnoreCase("Customer")) {
            JFrame frame = new JFrame(userType + " Dashboard");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 600); // Increased size
            frame.setLayout(new BorderLayout());

            JLabel label = new JLabel("Welcome to the Customer Dashboard!", JLabel.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 16));
            frame.add(label, BorderLayout.NORTH);

            // Product table
            String[] columnNames = {"ID", "Name", "Price", "Stock"};
            DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            JTable table = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(880, 200)); // fixed height for product list
            frame.add(scrollPane, BorderLayout.NORTH);

            // Purchase panel
            JPanel purchasePanel = new JPanel();
            JLabel qtyLabel = new JLabel("Purchase Quantity:");
            JTextField qtyField = new JTextField(5);
            JButton purchaseButton = new JButton("Purchase Selected Product");
            purchasePanel.add(qtyLabel);
            purchasePanel.add(qtyField);
            purchasePanel.add(purchaseButton);

            // Purchase history table
            String[] historyColumns = {"Date/Time", "Product Name", "Quantity", "Total Price"};
            DefaultTableModel historyModel = new DefaultTableModel(historyColumns, 0) {
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            JTable historyTable = new JTable(historyModel);
            JScrollPane historyScrollPane = new JScrollPane(historyTable);
            historyScrollPane.setPreferredSize(new Dimension(880, 250));

            // Panel to hold purchase controls and history vertically
            JPanel lowerPanel = new JPanel();
            lowerPanel.setLayout(new BorderLayout());
            lowerPanel.add(purchasePanel, BorderLayout.NORTH);
            lowerPanel.add(historyScrollPane, BorderLayout.CENTER);

            frame.add(lowerPanel, BorderLayout.CENTER);

            // Load products into table
            try (Connection conn = Database.getConnection()) {
                String sql = "SELECT id, name, price, stock FROM products";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                while (rs.next()) {
                    model.addRow(new Object[] {
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getInt("stock")
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Failed to load products.");
            }

            // Load purchase history
            loadPurchaseHistory(historyModel, username);

            // Purchase button action
            purchaseButton.addActionListener(e -> {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(frame, "Please select a product to purchase.");
                    return;
                }
                String qtyText = qtyField.getText();
                if (qtyText.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please enter a purchase quantity.");
                    return;
                }
                int qty;
                try {
                    qty = Integer.parseInt(qtyText);
                    if (qty <= 0) {
                        JOptionPane.showMessageDialog(frame, "Quantity must be positive.");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid quantity entered.");
                    return;
                }

                int productId = (int) model.getValueAt(selectedRow, 0);
                String productName = (String) model.getValueAt(selectedRow, 1);
                double price = (double) model.getValueAt(selectedRow, 2);
                int stock = (int) model.getValueAt(selectedRow, 3);

                if (qty > stock) {
                    JOptionPane.showMessageDialog(frame, "Insufficient stock. Available: " + stock);
                    return;
                }

                // Process purchase
                try (Connection conn = Database.getConnection()) {
                    conn.setAutoCommit(false);

                    // Reduce stock
                    String updateStockSql = "UPDATE products SET stock = stock - ? WHERE id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateStockSql)) {
                        updateStmt.setInt(1, qty);
                        updateStmt.setInt(2, productId);
                        int updated = updateStmt.executeUpdate();
                        if (updated == 0) {
                            conn.rollback();
                            JOptionPane.showMessageDialog(frame, "Product not found.");
                            return;
                        }
                    }

                    // Insert purchase record
                    String insertPurchaseSql = "INSERT INTO purchases (username, product_name, quantity, total_price, purchase_time) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement purchaseStmt = conn.prepareStatement(insertPurchaseSql)) {
                        purchaseStmt.setString(1, username);
                        purchaseStmt.setString(2, productName);
                        purchaseStmt.setInt(3, qty);
                        purchaseStmt.setDouble(4, qty * price);
                        purchaseStmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                        purchaseStmt.executeUpdate();
                    }

                    conn.commit();

                    // Update UI table stock value
                    model.setValueAt(stock - qty, selectedRow, 3);

                    // Add to purchase history table
                    historyModel.addRow(new Object[]{
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                            productName,
                            qty,
                            qty * price
                    });

                    JOptionPane.showMessageDialog(frame, "Purchase successful!");

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error processing purchase.");
                }
            });

            frame.setVisible(true);
        }
    }

    // Helper method to load purchase history for a user into a table model
    private static void loadPurchaseHistory(DefaultTableModel historyModel, String username) {
        historyModel.setRowCount(0); // Clear existing rows
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT purchase_time, product_name, quantity, total_price FROM purchases WHERE username = ? ORDER BY purchase_time DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("purchase_time");
                String dateTime = ts.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                historyModel.addRow(new Object[] {
                        dateTime,
                        rs.getString("product_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("total_price")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Authenticates a user against the database
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

    // Registers a new user in the database
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

