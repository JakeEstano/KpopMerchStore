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
        setTitle("케이팝 상점  - Login");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        getContentPane().setBackground(ThemeColors.BACKGROUND);

        // Header
        JLabel header = new JLabel("케이팝 상점", SwingConstants.CENTER);
        header.setFont(new Font("Nanum Gothic", Font.BOLD, 36));
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
        registerButton.addActionListener(e -> openRegisterFrame()); // Changed action

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
        field.setCaretColor(ThemeColors.TEXT); // Ensure caret is visible
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
            Color originalBg = bgColor; // Store original color correctly
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(ThemeColors.BUTTON_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalBg); // Use stored original color
            }
        });
        return button;
    }

    private void authenticateUser() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
             JOptionPane.showMessageDialog(this, "Please enter both email and password.", "Input Required", JOptionPane.WARNING_MESSAGE);
             return;
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.connect(); // Use updated DBConnection
            if (conn == null) {
                 JOptionPane.showMessageDialog(this, "Database connection failed. Cannot log in.", "DB Error", JOptionPane.ERROR_MESSAGE);
                 return;
            }
            stmt = conn.prepareStatement("SELECT id, email, password, role FROM customers WHERE email=?"); // Check only email first
            stmt.setString(1, email);
            rs = stmt.executeQuery();

            if (rs.next()) {
                // Email found, now verify password
                String storedPassword = rs.getString("password");
                // --- VERY BASIC Password Check ---
                // In a real application, use hashing (e.g., BCrypt)
                // if (BCrypt.checkpw(password, storedPassword)) { // Example using BCrypt
                if (password.equals(storedPassword)) { // Replace with secure check
                    int customerId = rs.getInt("id");
                    String role = rs.getString("role");
                    JOptionPane.showMessageDialog(this, "Login Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);

                    if ("admin".equalsIgnoreCase(role)) {
                        new AdminFrame().setVisible(true); // Show admin frame
                    } else {
                        new CustomerFrame(customerId).setVisible(true); // Show customer frame
                    }
                    dispose(); // Close the login frame
                } else {
                    // Password mismatch
                    JOptionPane.showMessageDialog(this, "Invalid Credentials!", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Email not found
                JOptionPane.showMessageDialog(this, "Invalid Credentials!", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error during login: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
             // Ensure resources are closed
             try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
             try { if (stmt != null) stmt.close(); } catch (SQLException ignored) {}
             try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }
    }

    private void openRegisterFrame() {
        new RegisterFrame().setVisible(true); // Show register frame
        // Keep the login frame open or dispose it based on desired behavior
        // dispose(); // Uncomment to close login frame when opening register
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
            System.err.println("Failed to set FlatDarkLaf LookAndFeel");
        }
        SwingUtilities.invokeLater(() -> new LoginFrame()); // Ensure GUI creation on EDT
    }
}