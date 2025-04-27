package OnlineShop;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import com.formdev.flatlaf.FlatDarkLaf;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import OnlineShop.AddressManager.Address;

public class PaymentFrame extends JFrame {
    private int customerId;
    private List<Integer> cartIds;
    private Address address;
    private JTextField cardNumberField, cvvField, expiryField;
    private JTextField accountNameField, accountNumberField;
    private JPasswordField accountPasswordField;
    private JButton payButton, cancelButton;
    private JComboBox<String> paymentMethodBox;
    private JPanel fieldsPanel;

    public PaymentFrame(int customerId, List<Integer> cartIds, Address address) {
        this.customerId = customerId;
        this.cartIds = cartIds;
        this.address = address;
        setTitle("HAMTEO - Payment");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        getContentPane().setBackground(ThemeColors.BACKGROUND);

        // Header
        JLabel header = new JLabel("Payment Details", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        header.setForeground(ThemeColors.PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(header, gbc);

        // Payment method
        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0;
        add(createFormLabel("Payment Method:"), gbc);
        paymentMethodBox = new JComboBox<>(new String[]{"Credit Card", "PayPal", "GCash", "Maya"});
        paymentMethodBox.addActionListener(e -> updateFieldsVisibility());
        styleComboBox(paymentMethodBox);
        gbc.gridx = 1;
        add(paymentMethodBox, gbc);

        // Fields panel - will contain all possible fields
        fieldsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        fieldsPanel.setBackground(ThemeColors.BACKGROUND);
        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 2;
        add(fieldsPanel, gbc);

        // Create all possible fields (hidden by default)
        createCardFields();
        createDigitalWalletFields();

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonPanel.setOpaque(false);
        
        payButton = createStyledButton("Pay Now", ThemeColors.PRIMARY);
        payButton.addActionListener(e -> processPayment());
        
        cancelButton = createStyledButton("Cancel", ThemeColors.SECONDARY);
        cancelButton.addActionListener(e -> goBack());
        
        buttonPanel.add(payButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        add(buttonPanel, gbc);

        // Initialize fields visibility
        updateFieldsVisibility();

        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    public PaymentFrame(int customerId, List<Integer> cartIds) {
        this(customerId, cartIds, null); // Call main constructor with null address
    }
    

    private void createCardFields() {
        // Card number
        fieldsPanel.add(createFormLabel("Card Number:"));
        cardNumberField = createFormTextField();
        fieldsPanel.add(cardNumberField);

        // CVV
        fieldsPanel.add(createFormLabel("CVV:"));
        cvvField = createFormTextField();
        fieldsPanel.add(cvvField);

        // Expiry
        fieldsPanel.add(createFormLabel("Expiry Date (MM/YY):"));
        expiryField = createFormTextField();
        fieldsPanel.add(expiryField);
    }

    private void createDigitalWalletFields() {
        // Account name
        fieldsPanel.add(createFormLabel("Account Name:"));
        accountNameField = createFormTextField();
        fieldsPanel.add(accountNameField);

        // Account number
        fieldsPanel.add(createFormLabel("Account Number:"));
        accountNumberField = createFormTextField();
        fieldsPanel.add(accountNumberField);

        // Password
        fieldsPanel.add(createFormLabel("Password:"));
        accountPasswordField = new JPasswordField();
        accountPasswordField.setFont(new Font("Arial", Font.PLAIN, 14));
        accountPasswordField.setBackground(ThemeColors.CARD_BG);
        accountPasswordField.setForeground(ThemeColors.TEXT);
        accountPasswordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.SECONDARY, 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        fieldsPanel.add(accountPasswordField);
    }

    private void updateFieldsVisibility() {
        String method = (String) paymentMethodBox.getSelectedItem();
        
        // Hide all fields first
        cardNumberField.setVisible(false);
        cvvField.setVisible(false);
        expiryField.setVisible(false);
        accountNameField.setVisible(false);
        accountNumberField.setVisible(false);
        accountPasswordField.setVisible(false);
        
        // Show only the relevant fields
        if (method.equals("Credit Card")) {
            cardNumberField.setVisible(true);
            cvvField.setVisible(true);
            expiryField.setVisible(true);
        } else { // PayPal, GCash, Maya
            accountNameField.setVisible(true);
            accountNumberField.setVisible(true);
            accountPasswordField.setVisible(true);
        }
        
        // Update the UI
        revalidate();
        repaint();
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(ThemeColors.TEXT);
        return label;
    }

    private JTextField createFormTextField() {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBackground(ThemeColors.CARD_BG);
        field.setForeground(ThemeColors.TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.SECONDARY, 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        return field;
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        comboBox.setBackground(ThemeColors.CARD_BG);
        comboBox.setForeground(ThemeColors.TEXT);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.SECONDARY, 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(ThemeColors.BUTTON_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        return button;
    }

    private void processPayment() {
        // Validate payment fields first
        if (!validatePaymentFields()) {
            return;
        }

        String method = (String) paymentMethodBox.getSelectedItem();
        String paymentDetails = getPaymentDetails(method);

        try (Connection conn = DBConnection.connect()) {
            conn.setAutoCommit(false); // Start transaction

            try {
                // 1. Create the order record
                int orderId;
                String insertOrderSQL = "INSERT INTO orders (customer_id, status, order_date, total_price) " +
                                     "VALUES (?, 'Processing', NOW(), ?)";
                try (PreparedStatement orderStmt = conn.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS)) {
                    double orderTotal = calculateOrderTotal(conn);
                    orderStmt.setInt(1, customerId);
                    orderStmt.setDouble(2, orderTotal);
                    orderStmt.executeUpdate();

                    ResultSet rs = orderStmt.getGeneratedKeys();
                    if (rs.next()) {
                        orderId = rs.getInt(1);
                    } else {
                        throw new SQLException("Failed to create order");
                    }
                }

                // 2. Add order items
                String insertItemSQL = "INSERT INTO order_items (order_id, product_id, quantity, price) " +
                                     "SELECT ?, product_id, quantity, price FROM cart WHERE id = ?";
                try (PreparedStatement itemStmt = conn.prepareStatement(insertItemSQL)) {
                    for (int cartId : cartIds) {
                        itemStmt.setInt(1, orderId);
                        itemStmt.setInt(2, cartId);
                        itemStmt.executeUpdate();
                    }
                }

                // 3. Create payment record
                String insertPaymentSQL = "INSERT INTO payments (customer_id, order_id, method, " +
                                        "card_number, cvv, expiry, account_name, account_number) " +
                                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement paymentStmt = conn.prepareStatement(insertPaymentSQL)) {
                    paymentStmt.setInt(1, customerId);
                    paymentStmt.setInt(2, orderId);
                    paymentStmt.setString(3, method);
                    
                    if (method.equals("Credit Card")) {
                        paymentStmt.setString(4, cardNumberField.getText());
                        paymentStmt.setString(5, cvvField.getText());
                        paymentStmt.setString(6, expiryField.getText());
                        paymentStmt.setNull(7, Types.VARCHAR);
                        paymentStmt.setNull(8, Types.VARCHAR);
                    } else {
                        paymentStmt.setNull(4, Types.VARCHAR);
                        paymentStmt.setNull(5, Types.VARCHAR);
                        paymentStmt.setNull(6, Types.VARCHAR);
                        paymentStmt.setString(7, accountNameField.getText());
                        paymentStmt.setString(8, accountNumberField.getText());
                    }
                    paymentStmt.executeUpdate();
                }

                // 4. Clear cart items
                String deleteSQL = "DELETE FROM cart WHERE id IN (" + 
                    String.join(",", Collections.nCopies(cartIds.size(), "?")) + ")";
                try (PreparedStatement clearCartStmt = conn.prepareStatement(deleteSQL)) {
                    for (int i = 0; i < cartIds.size(); i++) {
                        clearCartStmt.setInt(i + 1, cartIds.get(i));
                    }
                    clearCartStmt.executeUpdate();
                }

                // 5. Update loyalty points (example: 1 point per $10 spent)
                String updateLoyaltySQL = "UPDATE customers SET loyalty_points = loyalty_points + FLOOR(? / 10) WHERE id = ?";
                try (PreparedStatement loyaltyStmt = conn.prepareStatement(updateLoyaltySQL)) {
                    double orderTotal = calculateOrderTotal(conn);
                    loyaltyStmt.setDouble(1, orderTotal);
                    loyaltyStmt.setInt(2, customerId);
                    loyaltyStmt.executeUpdate();
                }

                conn.commit(); // Commit transaction if all succeeds

                // Show success message
                JOptionPane.showMessageDialog(this,
                    "Payment processed successfully!\nOrder ID: ORD" + orderId,
                    "Success", JOptionPane.INFORMATION_MESSAGE);

                // Close payment window and return to customer view
                new CustomerFrame(customerId);
                dispose();

            } catch (SQLException ex) {
                conn.rollback();
                JOptionPane.showMessageDialog(this,
                    "Payment failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Database error: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private boolean validatePaymentFields() {
        String method = (String) paymentMethodBox.getSelectedItem();
        
        if (method.equals("Credit Card")) {
            if (cardNumberField.getText().trim().isEmpty() || 
                cvvField.getText().trim().isEmpty() || 
                expiryField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Please fill all credit card fields",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // Simple card number validation (16 digits)
            if (!cardNumberField.getText().matches("\\d{16}")) {
                JOptionPane.showMessageDialog(this,
                    "Card number must be 16 digits",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // Simple CVV validation (3-4 digits)
            if (!cvvField.getText().matches("\\d{3,4}")) {
                JOptionPane.showMessageDialog(this,
                    "CVV must be 3 or 4 digits",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // Simple expiry date validation (MM/YY format)
            if (!expiryField.getText().matches("(0[1-9]|1[0-2])/[0-9]{2}")) {
                JOptionPane.showMessageDialog(this,
                    "Expiry date must be in MM/YY format",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else { // Digital wallet
            if (accountNameField.getText().trim().isEmpty() || 
                accountNumberField.getText().trim().isEmpty() || 
                accountPasswordField.getPassword().length == 0) {
                JOptionPane.showMessageDialog(this,
                    "Please fill all account fields",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

    private String getPaymentDetails(String method) {
        if (method.equals("Credit Card")) {
            String cardNumber = cardNumberField.getText();
            return "Card ending with: " + cardNumber.substring(cardNumber.length() - 4);
        } else {
            return accountNameField.getText() + " (" + method + ")";
        }
    }

    private double calculateOrderTotal(Connection conn) throws SQLException {
        double total = 0.0;
        String sql = "SELECT SUM(price * quantity) FROM cart WHERE id IN (" + 
            String.join(",", Collections.nCopies(cartIds.size(), "?")) + ")";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < cartIds.size(); i++) {
                stmt.setInt(i + 1, cartIds.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                total = rs.getDouble(1);
            }
        }
        return total;
    }

    private void goBack() {
        new CustomerFrame(customerId);
        dispose();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Option 1: Use the simpler constructor (for testing)
        new PaymentFrame(1, new ArrayList<>());
    }
}