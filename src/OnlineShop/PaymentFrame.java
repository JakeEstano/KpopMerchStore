package OnlineShop;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import com.formdev.flatlaf.FlatDarkLaf;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class PaymentFrame extends JFrame {
    private int customerId;
    private List<Integer> cartIds;
    private JTextField cardNumberField, cvvField, expiryField;
    private JTextField accountNameField, accountNumberField;
    private JPasswordField accountPasswordField;
    private JButton payButton, cancelButton;
    private JComboBox<String> paymentMethodBox;
    private JPanel fieldsPanel;

    public PaymentFrame(int customerId, List<Integer> cartIds) {
        this.customerId = customerId;
        this.cartIds = cartIds;
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
        String method = (String) paymentMethodBox.getSelectedItem();

        try (Connection conn = DBConnection.connect()) {
            conn.setAutoCommit(false); // Start transaction

            try {
                // 1. Create the order record
                int orderId;
                String insertOrderSQL = "INSERT INTO orders (customer_id, status, order_date, total_amount) VALUES (?, 'Processing', NOW(), ?)";
                try (PreparedStatement orderStmt = conn.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS)) {
                    // Calculate total from selected items
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

                // 2. Add only selected cart items to order_items
                String insertItemSQL = "INSERT INTO order_items (order_id, product_id, quantity, price) " +
                                     "SELECT ?, product_id, quantity, price FROM cart WHERE id = ?";
                try (PreparedStatement itemStmt = conn.prepareStatement(insertItemSQL)) {
                    for (int cartId : cartIds) {
                        itemStmt.setInt(1, orderId);
                        itemStmt.setInt(2, cartId);
                        itemStmt.executeUpdate();
                    }
                }

                // 3. Clear only the selected cart items
                String deleteSQL = "DELETE FROM cart WHERE id IN (" + 
                    String.join(",", Collections.nCopies(cartIds.size(), "?")) + ")";
                try (PreparedStatement clearCartStmt = conn.prepareStatement(deleteSQL)) {
                    for (int i = 0; i < cartIds.size(); i++) {
                        clearCartStmt.setInt(i + 1, cartIds.get(i));
                    }
                    clearCartStmt.executeUpdate();
                }

                // ... rest of payment processing code ...
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            // ... error handling ...
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
        new PaymentFrame(1, new ArrayList<>()); // Pass empty list for testing
    }
}