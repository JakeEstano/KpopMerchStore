package OnlineShop;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import com.formdev.flatlaf.FlatDarkLaf;

public class PaymentFrame extends JFrame {
    private int customerId;
    private JTextField cardNumberField, cvvField, expiryField;
    private JButton payButton, cancelButton;
    private JComboBox<String> paymentMethodBox;

    public PaymentFrame(int customerId) {
        this.customerId = customerId;
        setTitle("HAMTEO - Payment");
        setSize(600, 400);
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
        styleComboBox(paymentMethodBox);
        gbc.gridx = 1;
        add(paymentMethodBox, gbc);

        // Card number
        gbc.gridy = 2; gbc.gridx = 0;
        add(createFormLabel("Card Number:"), gbc);
        cardNumberField = createFormTextField();
        gbc.gridx = 1;
        add(cardNumberField, gbc);

        // CVV
        gbc.gridy = 3; gbc.gridx = 0;
        add(createFormLabel("CVV:"), gbc);
        cvvField = createFormTextField();
        gbc.gridx = 1;
        add(cvvField, gbc);

        // Expiry
        gbc.gridy = 4; gbc.gridx = 0;
        add(createFormLabel("Expiry Date (MM/YY):"), gbc);
        expiryField = createFormTextField();
        gbc.gridx = 1;
        add(expiryField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonPanel.setOpaque(false);
        
        payButton = createStyledButton("Pay Now", ThemeColors.PRIMARY);
        payButton.addActionListener(e -> processPayment());
        
        cancelButton = createStyledButton("Cancel", ThemeColors.SECONDARY);
        cancelButton.addActionListener(e -> goBack());
        
        buttonPanel.add(payButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 2;
        add(buttonPanel, gbc);

        setLocationRelativeTo(null);
        setVisible(true);
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
        String cardNumber = cardNumberField.getText();
        String cvv = cvvField.getText();
        String expiry = expiryField.getText();

        if (method.equals("Credit Card") && (cardNumber.isEmpty() || cvv.isEmpty() || expiry.isEmpty())) {
            JOptionPane.showMessageDialog(this, "Please enter all card details.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO payments (customer_id, method, card_number, cvv, expiry) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setInt(1, customerId);
            stmt.setString(2, method);
            stmt.setString(3, cardNumber);
            stmt.setString(4, cvv);
            stmt.setString(5, expiry);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Payment Successful!");
            new CustomerFrame(customerId);
            dispose();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error processing payment.", "Error", JOptionPane.ERROR_MESSAGE);
        }
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
        new PaymentFrame(1); // Sample customer ID
    }
}