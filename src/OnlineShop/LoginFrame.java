package OnlineShop;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import com.formdev.flatlaf.FlatDarkLaf;

public class LoginFrame extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton, registerButton;

    public LoginFrame() {
        setTitle("HAMTEO - Login");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        getContentPane().setBackground(ThemeColors.BACKGROUND);

        // Header
        JLabel header = new JLabel("HAMTEO", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 36));
        header.setForeground(ThemeColors.PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(header, gbc);

        // Subheader
        JLabel subheader = new JLabel("K-Pop Merchandise Store", SwingConstants.CENTER);
        subheader.setFont(new Font("Arial", Font.PLAIN, 16));
        subheader.setForeground(ThemeColors.TEXT);
        gbc.gridy = 1;
        add(subheader, gbc);

        // Form fields
        gbc.gridwidth = 1;
        gbc.gridy = 2; gbc.gridx = 0;
        add(createFormLabel("Email:"), gbc);
        emailField = createFormTextField();
        gbc.gridx = 1;
        add(emailField, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        add(createFormLabel("Password:"), gbc);
        passwordField = createPasswordField();
        gbc.gridx = 1;
        add(passwordField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonPanel.setOpaque(false);
        
        loginButton = createStyledButton("Login", ThemeColors.PRIMARY);
        loginButton.addActionListener(e -> authenticateUser());
        
        registerButton = createStyledButton("Register", ThemeColors.SECONDARY);
        registerButton.addActionListener(e -> new RegisterFrame());
        
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        add(buttonPanel, gbc);

        // Footer
        JLabel footer = new JLabel("© 2025 HAMTEO - All Rights Reserved", SwingConstants.CENTER);
        footer.setFont(new Font("Arial", Font.PLAIN, 12));
        footer.setForeground(ThemeColors.TEXT);
        gbc.gridy = 5;
        add(footer, gbc);

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

    private JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField(20);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBackground(ThemeColors.CARD_BG);
        field.setForeground(ThemeColors.TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.SECONDARY, 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        return field;
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

    private void authenticateUser() {
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, email, password, role FROM customers WHERE email=? AND password=?")) {

            stmt.setString(1, emailField.getText());
            stmt.setString(2, new String(passwordField.getPassword()));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int customerId = rs.getInt("id");
                String role = rs.getString("role");
                JOptionPane.showMessageDialog(this, "Login Successful!");

                if ("admin".equalsIgnoreCase(role)) {
                    new AdminFrame();
                } else {
                    new CustomerFrame(customerId);
                }
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Credentials!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        new LoginFrame();
    }
}