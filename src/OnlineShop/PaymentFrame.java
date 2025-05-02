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
import java.util.Calendar;

public class PaymentFrame extends JFrame {
    private int customerId;
    private List<Integer> cartIds;
    private Address address;
    private JTextField cardNumberField, cvvField, expiryField;
    private JTextField accountNameField, accountNumberField;
    private JPasswordField accountPasswordField;
    private JButton payButton, cancelButton;
    private JComboBox<String> paymentMethodBox;
    private JPanel fieldsPanel; // Panel to hold dynamic fields
    private GridBagConstraints gbcFields; // Keep GBC for fields panel

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
        gbc.gridy = 1; gbc.gridx = 0; gbc.anchor = GridBagConstraints.WEST; // Align label left
        add(createFormLabel("Payment Method:"), gbc);
        paymentMethodBox = new JComboBox<>(new String[]{"Credit Card", "PayPal", "GCash", "Maya"});
        paymentMethodBox.addActionListener(e -> updateFieldsVisibility());
        styleComboBox(paymentMethodBox);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.CENTER; // Keep combo box centered or WEST
        add(paymentMethodBox, gbc);

        // Fields panel - Use GridBagLayout for better control
        fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(ThemeColors.BACKGROUND);
        gbcFields = new GridBagConstraints(); // Initialize GBC for this panel
        gbcFields.insets = new Insets(5, 5, 5, 5); // Padding within fieldsPanel
        gbcFields.fill = GridBagConstraints.HORIZONTAL;
        gbcFields.anchor = GridBagConstraints.WEST;

        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.weighty = 1.0; // Allow fields panel to expand vertically if needed
        gbc.fill = GridBagConstraints.BOTH; // Allow fields panel to expand
        add(fieldsPanel, gbc);
        gbc.weighty = 0.0; // Reset weighty
        gbc.fill = GridBagConstraints.HORIZONTAL; // Reset fill

        // Create all possible fields (but don't add them yet)
        cardNumberField = createFormTextField();
        cvvField = createFormTextField();
        expiryField = createFormTextField();
        accountNameField = createFormTextField();
        accountNumberField = createFormTextField();
        accountPasswordField = createPasswordField(); // Use helper

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Align buttons right
        buttonPanel.setOpaque(false);

        payButton = createStyledButton("Pay Now", ThemeColors.PRIMARY);
        payButton.addActionListener(e -> processPayment());

        cancelButton = createStyledButton("Cancel", ThemeColors.SECONDARY);
        cancelButton.addActionListener(e -> goBack());

        buttonPanel.add(cancelButton); // Cancel first
        buttonPanel.add(payButton); // Then Pay Now

        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL; // Make button panel take width
        gbc.anchor = GridBagConstraints.SOUTHEAST; // Anchor buttons bottom-right
        add(buttonPanel, gbc);

        // Initialize fields visibility based on default selection
        updateFieldsVisibility();

        setLocationRelativeTo(null);
        // setVisible(true); // Visibility set after construction typically
    }

    // Simplified constructor if address isn't strictly needed at init
    public PaymentFrame(int customerId, List<Integer> cartIds) {
        this(customerId, cartIds, null); // Call main constructor with null address
        // Fetch address if needed here, or ensure checkout process requires it
    }


    // Helper to add a label and field pair to the fieldsPanel
    private void addFieldPair(String labelText, Component field, int gridY) {
        gbcFields.gridx = 0; gbcFields.gridy = gridY; gbcFields.weightx = 0.1; // Label takes less space
        fieldsPanel.add(createFormLabel(labelText), gbcFields);

        gbcFields.gridx = 1; gbcFields.gridy = gridY; gbcFields.weightx = 0.9; // Field takes more space
        fieldsPanel.add(field, gbcFields);
    }


    private void updateFieldsVisibility() {
        fieldsPanel.removeAll(); // Clear the panel first
        String method = (String) paymentMethodBox.getSelectedItem();
        int gridY = 0; // Reset grid row counter

        if ("Credit Card".equals(method)) {
            addFieldPair("Card Number:", cardNumberField, gridY++);
            addFieldPair("CVV:", cvvField, gridY++);
            addFieldPair("Expiry Date (MM/YY):", expiryField, gridY++);
        } else { // PayPal, GCash, Maya
            addFieldPair("Account Name:", accountNameField, gridY++);
            addFieldPair("Account Number:", accountNumberField, gridY++);
            addFieldPair("Password:", accountPasswordField, gridY++);
        }

        // Add glue at the bottom to push fields up if panel is larger than needed
        gbcFields.gridx = 0; gbcFields.gridy = gridY; gbcFields.gridwidth = 2;
        gbcFields.weighty = 1.0; // Takes remaining vertical space
        gbcFields.fill = GridBagConstraints.VERTICAL;
        fieldsPanel.add(Box.createVerticalGlue(), gbcFields);
        gbcFields.weighty = 0.0; // Reset
        gbcFields.fill = GridBagConstraints.HORIZONTAL; // Reset

        // Revalidate and repaint the fields panel and the frame
        fieldsPanel.revalidate();
        fieldsPanel.repaint();
        this.pack(); // Adjust frame size to fit content
        this.setSize(Math.max(600, getWidth()), Math.max(500, getHeight())); // Ensure minimum size
        this.setLocationRelativeTo(null); // Re-center
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
        field.setCaretColor(ThemeColors.TEXT); // Ensure caret is visible
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.SECONDARY, 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        return field;
    }

    private JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField(20);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBackground(ThemeColors.CARD_BG);
        field.setForeground(ThemeColors.TEXT);
        field.setCaretColor(ThemeColors.TEXT);
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
         // Custom renderer for dropdown items
         comboBox.setRenderer(new DefaultListCellRenderer() {
             @Override
             public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                 super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                 setBackground(isSelected ? ThemeColors.PRIMARY : ThemeColors.CARD_BG);
                 setForeground(isSelected ? Color.WHITE : ThemeColors.TEXT);
                 setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Padding
                 return this;
             }
         });
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.addMouseListener(new MouseAdapter() {
            Color originalBg = bgColor;
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(ThemeColors.BUTTON_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalBg);
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
        // String paymentDetails = getPaymentDetails(method); // Not used directly in DB insert

        Connection conn = null; // Declare conn outside try-with-resources for transaction management
        try {
            conn = DBConnection.connect();
            if (conn == null) {
                 JOptionPane.showMessageDialog(this, "Database connection failed.", "Error", JOptionPane.ERROR_MESSAGE);
                 return;
            }
            conn.setAutoCommit(false); // Start transaction

            // 1. Create the order record
            int orderId = -1;
            String insertOrderSQL = "INSERT INTO orders (customer_id, status, order_date, total_price, address_id) " +
                                 "VALUES (?, 'Processing', NOW(), ?, ?)"; // Added address_id
            try (PreparedStatement orderStmt = conn.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS)) {
                double orderTotal = calculateOrderTotal(conn);
                 if (orderTotal <= 0) { // Check if total is valid
                    throw new SQLException("Order total cannot be zero or negative.");
                 }
                orderStmt.setInt(1, customerId);
                orderStmt.setDouble(2, orderTotal);
                 // Ensure address is not null before getting ID
                 if (address == null) {
                     throw new SQLException("Shipping address is missing.");
                 }
                orderStmt.setInt(3, address.getId()); // Add address ID
                orderStmt.executeUpdate();

                ResultSet rs = orderStmt.getGeneratedKeys();
                if (rs.next()) {
                    orderId = rs.getInt(1);
                } else {
                    throw new SQLException("Failed to create order, no ID obtained.");
                }
            }

            // 2. Add order items
            String insertItemSQL = "INSERT INTO order_items (order_id, product_id, quantity, price) " +
                                 "SELECT ?, product_id, quantity, price FROM cart WHERE id = ?";
            try (PreparedStatement itemStmt = conn.prepareStatement(insertItemSQL)) {
                for (int cartId : cartIds) {
                    itemStmt.setInt(1, orderId);
                    itemStmt.setInt(2, cartId);
                    itemStmt.addBatch(); // Batch insertion for efficiency
                }
                itemStmt.executeBatch(); // Execute batch insert
            }

            // 3. Create payment record
            String insertPaymentSQL = "INSERT INTO payments (customer_id, order_id, method, " +
                                    "card_number, cvv, expiry, account_name, account_number) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement paymentStmt = conn.prepareStatement(insertPaymentSQL)) {
                paymentStmt.setInt(1, customerId);
                paymentStmt.setInt(2, orderId);
                paymentStmt.setString(3, method);

                if ("Credit Card".equals(method)) {
                    paymentStmt.setString(4, cardNumberField.getText());
                    paymentStmt.setString(5, cvvField.getText());
                    paymentStmt.setString(6, expiryField.getText());
                    paymentStmt.setNull(7, Types.VARCHAR);
                    paymentStmt.setNull(8, Types.VARCHAR);
                } else { // Digital wallet
                    paymentStmt.setNull(4, Types.VARCHAR);
                    paymentStmt.setNull(5, Types.VARCHAR);
                    paymentStmt.setNull(6, Types.VARCHAR);
                    paymentStmt.setString(7, accountNameField.getText());
                    paymentStmt.setString(8, accountNumberField.getText());
                }
                paymentStmt.executeUpdate();
            }

            // 4. Clear cart items for this customer (careful with multiple tabs/sessions)
            // Clear ONLY the items that were part of this checkout
            String deleteSQL = "DELETE FROM cart WHERE id IN (" +
                String.join(",", Collections.nCopies(cartIds.size(), "?")) + ") AND customer_id = ?";
            try (PreparedStatement clearCartStmt = conn.prepareStatement(deleteSQL)) {
                int paramIndex = 1;
                for (int cartId : cartIds) {
                    clearCartStmt.setInt(paramIndex++, cartId);
                }
                clearCartStmt.setInt(paramIndex, customerId); // Ensure we only delete for the current customer
                clearCartStmt.executeUpdate();
            }

            // 5. Optional: Update stock (If not handled during cart add/update)
            // Stock should ideally be managed when items are added/removed from cart
            // or at least re-checked *before* creating the order.
            // If stock needs update *after* payment, do it here. Example:
            /*
            String updateStockSQL = "UPDATE products SET stock = stock - ? WHERE id = ?";
            String selectCartItemsSQL = "SELECT product_id, quantity FROM cart WHERE id = ?";
            try (PreparedStatement stockStmt = conn.prepareStatement(updateStockSQL);
                 PreparedStatement cartItemStmt = conn.prepareStatement(selectCartItemsSQL)) {
                for (int cartId : cartIds) {
                    cartItemStmt.setInt(1, cartId);
                    ResultSet rs = cartItemStmt.executeQuery();
                    if (rs.next()) {
                        stockStmt.setInt(1, rs.getInt("quantity"));
                        stockStmt.setInt(2, rs.getInt("product_id"));
                        stockStmt.addBatch();
                    }
                    rs.close();
                }
                stockStmt.executeBatch();
            }
            */

            conn.commit(); // Commit transaction if all succeeds

            // Show success message
            JOptionPane.showMessageDialog(this,
                "Payment processed successfully!\nOrder ID: " + orderId, // Show actual ID
                "Success", JOptionPane.INFORMATION_MESSAGE);

            // Close payment window and return to customer view
            // Re-create CustomerFrame to reflect changes (like cleared cart, new order)
            SwingUtilities.invokeLater(() -> {
                new CustomerFrame(customerId).setVisible(true);
            });
            dispose(); // Close this payment frame

        } catch (SQLException ex) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException eRollback) { eRollback.printStackTrace(); }
            }
            JOptionPane.showMessageDialog(this,
                "Payment failed: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
             if (conn != null) {
                 try { conn.setAutoCommit(true); conn.close(); } catch (SQLException eClose) { eClose.printStackTrace(); }
             }
        }
    }

    private boolean validatePaymentFields() {
        String method = (String) paymentMethodBox.getSelectedItem();

        if ("Credit Card".equals(method)) {
            if (cardNumberField.getText().trim().isEmpty() ||
                cvvField.getText().trim().isEmpty() ||
                expiryField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Please fill all credit card fields",
                    "Validation Error", JOptionPane.WARNING_MESSAGE); // Changed to WARNING
                return false;
            }

            // Simple card number validation (example: 16 digits)
            if (!cardNumberField.getText().trim().matches("\\d{16}")) {
                JOptionPane.showMessageDialog(this,
                    "Card number must be 16 digits",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            // Simple CVV validation (example: 3 or 4 digits)
            if (!cvvField.getText().trim().matches("\\d{3,4}")) {
                JOptionPane.showMessageDialog(this,
                    "CVV must be 3 or 4 digits",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            // Simple expiry date validation (MM/YY format)
            if (!expiryField.getText().trim().matches("(0[1-9]|1[0-2])/[0-9]{2}")) {
                JOptionPane.showMessageDialog(this,
                    "Expiry date must be in MM/YY format",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            // Basic future date check for expiry
            try {
                String[] parts = expiryField.getText().split("/");
                int month = Integer.parseInt(parts[0]);
                int year = Integer.parseInt("20" + parts[1]); // Assume 20xx
                Calendar now = Calendar.getInstance();
                int currentYear = now.get(Calendar.YEAR);
                int currentMonth = now.get(Calendar.MONTH) + 1; // Calendar month is 0-based

                if (year < currentYear || (year == currentYear && month < currentMonth)) {
                    JOptionPane.showMessageDialog(this, "Card has expired", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                 JOptionPane.showMessageDialog(this, "Invalid expiry date format", "Validation Error", JOptionPane.WARNING_MESSAGE);
                 return false;
            }

        } else { // Digital wallet
            if (accountNameField.getText().trim().isEmpty() ||
                accountNumberField.getText().trim().isEmpty() ||
                accountPasswordField.getPassword().length == 0) {
                JOptionPane.showMessageDialog(this,
                    "Please fill all account fields for " + method,
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            // Add specific validation for account number format if needed
            // Example for GCash/Maya (11 digits starting with 09):
            if (("GCash".equals(method) || "Maya".equals(method)) && !accountNumberField.getText().trim().matches("09\\d{9}")) {
                 JOptionPane.showMessageDialog(this, "Invalid " + method + " number format (must be 09xxxxxxxxx).", "Validation Error", JOptionPane.WARNING_MESSAGE);
                 return false;
            }
        }
        return true;
    }

    private String getPaymentDetails(String method) {
        if ("Credit Card".equals(method)) {
            String cardNumber = cardNumberField.getText().trim();
            if (cardNumber.length() >= 4) {
                 return "Card ending with: " + cardNumber.substring(cardNumber.length() - 4);
            }
            return "Credit Card"; // Fallback if card number is short
        } else {
            return accountNameField.getText().trim() + " (" + method + ")";
        }
    }

    private double calculateOrderTotal(Connection conn) throws SQLException {
        if (cartIds == null || cartIds.isEmpty()) {
            return 0.0; // No items to calculate total for
        }
        double total = 0.0;
        String sql = "SELECT SUM(price * quantity) FROM cart WHERE id IN (" +
            String.join(",", Collections.nCopies(cartIds.size(), "?")) + ") AND customer_id = ?"; // Added customer_id check

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            for (int cartId : cartIds) {
                stmt.setInt(paramIndex++, cartId);
            }
            stmt.setInt(paramIndex, customerId); // Add customer ID to the end
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                total = rs.getDouble(1);
            }
        }
        return total;
    }

    private void goBack() {
        // Instead of creating a new CustomerFrame, consider just closing this one
        // The CustomerFrame that opened this should still be there.
        // If CustomerFrame was disposed, then you need to recreate it.
        dispose(); // Close only the payment frame

        // Optional: If CustomerFrame was disposed earlier, recreate it:
        // SwingUtilities.invokeLater(() -> new CustomerFrame(customerId).setVisible(true));
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
             // Apply theme settings globally for OptionPanes here
             UIManager.put("OptionPane.background", ThemeColors.DIALOG_BG);
             UIManager.put("Panel.background", ThemeColors.DIALOG_BG); // Affects OptionPane panel
             UIManager.put("OptionPane.messageForeground", ThemeColors.DIALOG_FG);
             UIManager.put("Button.background", ThemeColors.SECONDARY);
             UIManager.put("Button.foreground", Color.WHITE);
             UIManager.put("Button.focus", new Color(ThemeColors.SECONDARY.getRed(), ThemeColors.SECONDARY.getGreen(), ThemeColors.SECONDARY.getBlue(), 180));
             UIManager.put("Button.hoverBackground", ThemeColors.BUTTON_HOVER);
             UIManager.put("Button.pressedBackground", ThemeColors.SECONDARY.darker());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Example usage for testing
        SwingUtilities.invokeLater(() -> {
            List<Integer> testCartIds = new ArrayList<>();
            testCartIds.add(1); // Add some dummy cart IDs if needed for testing calculateOrderTotal
            testCartIds.add(2);
             // Dummy address for testing
             Address testAddress = new AddressManager.Address(1, "123 Test St", "Test City", "TS", "12345", "Country");
            new PaymentFrame(1, testCartIds, testAddress).setVisible(true); // Use valid constructor
        });
    }
}