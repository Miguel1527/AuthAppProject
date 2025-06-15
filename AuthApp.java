import javax.swing.*;   // GUI Components
import javax.swing.table.DefaultTableModel; 
import javax.swing.table.TableRowSorter;
import java.awt.*;  //Layout and window features
import java.sql.*; //Database Connectivity
import java.time.LocalDateTime; 
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger; //Logging Framework
import java.util.regex.Pattern;


public class AuthApp {

	//Logger instance for logging important event and errors
    private static final Logger logger = AppLogger.getLogger();

    public static void main(String[] args) {
    	//Ensures GUI creation runs on the Event Dispatch Thread
        SwingUtilities.invokeLater(AuthApp::showLoginScreen);
    }

    //Displays the login screen
    public static void showLoginScreen() {
        JFrame frame = new JFrame("Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);
        frame.setLayout(new GridLayout(4, 2)); //4 rows, 2 columns

        // Input fields
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        
        //Buttons
        JButton loginButton = new JButton("Login");
        JButton signupButton = new JButton("Signup");

        // Add component to the frame
        frame.add(new JLabel("Username:"));
        frame.add(usernameField);
        frame.add(new JLabel("Password:"));
        frame.add(passwordField);
        frame.add(loginButton);
        frame.add(signupButton);

        //Login button action
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = String.valueOf(passwordField.getPassword());
            
            //Try to authenticate 
            String userType = authenticate(username, password);

            if (userType != null) {
                JOptionPane.showMessageDialog(frame, "Login successful!");
                frame.dispose();
                showDashboard(userType, username);
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid username or password.");
            }
        });

        //signup button action
        signupButton.addActionListener(e -> {
            frame.dispose();
            showSignupScreen(); 
        });

        frame.setVisible(true);
    }

    //Displays the signup/registration screen
    public static void showSignupScreen() {
        JFrame frame = new JFrame("Signup");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new GridLayout(8, 2)); //8 rows, 2 columns

        //Input fields
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField fullNameField = new JTextField();
        JTextField phoneField = new JTextField();
        
        //Dropdowns for gender and user type
        JComboBox<String> genderBox = new JComboBox<>(new String[]{"Male", "Female"});
        JComboBox<String> userTypeBox = new JComboBox<>(new String[]{"Customer", "Employee"});
        
        //Buttons 
        JButton registerButton = new JButton("Register");
        JButton backButton = new JButton("Back to Login");

        //Add components to frame
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

        //Register button action
        registerButton.addActionListener(e -> {
        	//collect input data
            String username = usernameField.getText();
            String password = String.valueOf(passwordField.getPassword());
            String fullName = fullNameField.getText();
            String phone = phoneField.getText();
            String gender = genderBox.getSelectedItem().toString();
            String userType = userTypeBox.getSelectedItem().toString();

            //Attempt registration
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

    //Displays dashboard based on user type
    public static void showDashboard(String userType, String username) {
        if (userType == null) {
            JOptionPane.showMessageDialog(null, "Error: No user type returned.");
            return;
        }

        if (userType.equalsIgnoreCase("Employee")) {
            JFrame frame = new JFrame("Employee Dashboard");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLayout(new BorderLayout());

            JLabel title = new JLabel("Welcome to the Employee Dashboard!", JLabel.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 18));
            frame.add(title, BorderLayout.NORTH);

            JPanel formPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;

            formPanel.add(new JLabel("Product Name:"), gbc);
            gbc.gridx = 1;
            JTextField nameField = new JTextField(20);
            formPanel.add(nameField, gbc);

            gbc.gridx = 0; gbc.gridy++;
            formPanel.add(new JLabel("Stock Amount:"), gbc);
            gbc.gridx = 1;
            JTextField stockField = new JTextField(20);
            formPanel.add(stockField, gbc);

            gbc.gridx = 0; gbc.gridy++;
            formPanel.add(new JLabel("Price:"), gbc);
            gbc.gridx = 1;
            JTextField priceField = new JTextField(20);
            formPanel.add(priceField, gbc);

            gbc.gridx = 1; gbc.gridy++;
            JButton addProductButton = new JButton("Add Product");
            formPanel.add(addProductButton, gbc);

            gbc.gridx = 0; gbc.gridy++;
            formPanel.add(new JLabel("Product ID to Update:"), gbc);
            gbc.gridx = 1;
            JTextField updateIdField = new JTextField(20);
            formPanel.add(updateIdField, gbc);

            gbc.gridx = 0; gbc.gridy++;
            formPanel.add(new JLabel("New Stock Amount:"), gbc);
            gbc.gridx = 1;
            JTextField updateStockField = new JTextField(20);
            formPanel.add(updateStockField, gbc);

            gbc.gridx = 1; gbc.gridy++;
            JButton updateStockButton = new JButton("Update Stock");
            formPanel.add(updateStockButton, gbc);

            gbc.gridx = 1; gbc.gridy++;
            JButton viewSalesButton = new JButton("View Sales Records");
            formPanel.add(viewSalesButton, gbc);

            frame.add(formPanel, BorderLayout.CENTER);

            // Add product logic
            addProductButton.addActionListener(e -> {
                try (Connection conn = Database.getConnection()) {
                    String sql = "INSERT INTO products (name, stock, price) VALUES (?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, nameField.getText());
                    stmt.setInt(2, Integer.parseInt(stockField.getText()));
                    stmt.setDouble(3, Double.parseDouble(priceField.getText()));
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(frame, "Product added!");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error adding product.");
                }
            });

            // Update stock logic
            updateStockButton.addActionListener(e -> {
                try (Connection conn = Database.getConnection()) {
                    String sql = "UPDATE products SET stock = ? WHERE id = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, Integer.parseInt(updateStockField.getText()));
                    stmt.setInt(2, Integer.parseInt(updateIdField.getText()));
                    int result = stmt.executeUpdate();
                    if (result > 0) {
                        JOptionPane.showMessageDialog(frame, "Stock updated!");
                    } else {
                        JOptionPane.showMessageDialog(frame, "Product ID not found.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error updating stock.");
                }
            });

            // View sales logic
            viewSalesButton.addActionListener(e -> {
                JFrame salesFrame = new JFrame("Sales Records");
                salesFrame.setSize(700, 400);
                String[] columns = {"Username", "Product Name", "Quantity", "Total Price", "Time"};
                DefaultTableModel model = new DefaultTableModel(columns, 0);
                JTable table = new JTable(model);
                JScrollPane pane = new JScrollPane(table);
                salesFrame.add(pane);

                try (Connection conn = Database.getConnection()) {
                    String sql = "SELECT username, product_name, quantity, total_price, purchase_time FROM purchases ORDER BY purchase_time DESC";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        model.addRow(new Object[]{
                                rs.getString("username"),
                                rs.getString("product_name"),
                                rs.getInt("quantity"),
                                rs.getDouble("total_price"),
                                rs.getTimestamp("purchase_time").toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(salesFrame, "Error loading sales.");
                }

                salesFrame.setVisible(true);
            });

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } else if (userType.equalsIgnoreCase("Customer")) {
            JFrame frame = new JFrame(userType + " Dashboard");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 600); // Increased size
            frame.setLayout(new BorderLayout());

            JLabel label = new JLabel("Welcome to the Customer Dashboard!", JLabel.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 16));
            frame.add(label, BorderLayout.NORTH);

            // Search panel
            JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel searchLabel = new JLabel("Search Products:");
            JTextField searchField = new JTextField(20);
            searchPanel.add(searchLabel);
            searchPanel.add(searchField);
            frame.add(searchPanel, BorderLayout.AFTER_LAST_LINE);

            // Product table
            String[] columnNames = {"ID", "Name", "Price", "Stock"};
            DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            JTable table = new JTable(model);
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            table.setRowSorter(sorter);

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
                    model.addRow(new Object[]{
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

            // Search field listener to filter table
            searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                private void updateFilter() {
                    String text = searchField.getText();
                    if (text.trim().length() == 0) {
                        sorter.setRowFilter(null);
                    } else {
                        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text), 1)); // filter on "Name" column
                    }
                }

                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    updateFilter();
                }

                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    updateFilter();
                }

                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    updateFilter();
                }
            });

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

                int modelRow = table.convertRowIndexToModel(selectedRow);

                int productId = (int) model.getValueAt(modelRow, 0);
                String productName = (String) model.getValueAt(modelRow, 1);
                double price = (double) model.getValueAt(modelRow, 2);
                int stock = (int) model.getValueAt(modelRow, 3);

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
                    model.setValueAt(stock - qty, modelRow, 3);

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

                historyModel.addRow(new Object[]{
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

    //Authenticates a user against the database
    private static String authenticate(String username, String password) {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT user_type FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String userType = rs.getString("user_type");
                logger.info("User '" + username + "' logged in as " + userType);
                return userType;
            } else {
                logger.warning("Failed login attempt for username: " + username);
                return null;
            }
        } catch (Exception e) {
            logger.severe("Login error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    //Registers a new user in the database
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
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            logger.severe("Registration error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
