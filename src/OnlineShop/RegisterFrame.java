package OnlineShop;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import com.formdev.flatlaf.FlatDarkLaf;

public class RegisterFrame extends JFrame {
    private JTextField nameField, emailField, phoneField;
    private JTextArea addressField;
    private JPasswordField passwordField;
    private JButton registerButton, backButton;

    public RegisterFrame() {
        setTitle("HAMTEO - Register");
        setSize(500, 650); // Increased height to accommodate the address field
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Dispose only this frame
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        getContentPane().setBackground(ThemeColors.BACKGROUND);

        // Header
        JLabel header = new JLabel("Create Account", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 28));
        header.setForeground(ThemeColors.PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(header, gbc);

        // Form fields
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST; // Align labels left
        gbc.gridy = 1; gbc.gridx = 0;
        add(createFormLabel("Full Name:"), gbc);
        nameField = createFormTextField();
        gbc.gridx = 1;
        add(nameField, gbc);

        gbc.gridy = 2; gbc.gridx = 0;
        add(createFormLabel("Email:"), gbc);
        emailField = createFormTextField();
        gbc.gridx = 1;
        add(emailField, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        add(createFormLabel("Phone:"), gbc);
        phoneField = createFormTextField();
        gbc.gridx = 1;
        add(phoneField, gbc);

        gbc.gridy = 4; gbc.gridx = 0; gbc.anchor = GridBagConstraints.NORTHWEST; // Align label top-left
        add(createFormLabel("Address:"), gbc);
        addressField = createAddressTextArea();
        JScrollPane addressScrollPane = new JScrollPane(addressField);
        addressScrollPane.setPreferredSize(new Dimension(200, 80));
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0; // Allow text area to grow vertically
        add(addressScrollPane, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weighty = 0.0; // Reset fill and weighty
        gbc.anchor = GridBagConstraints.WEST; // Reset anchor

        gbc.gridy = 5; gbc.gridx = 0;
        add(createFormLabel("Password:"), gbc);
        passwordField = createPasswordField();
        gbc.gridx = 1;
        add(passwordField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonPanel.setOpaque(false);

        registerButton = createStyledButton("Register", ThemeColors.PRIMARY);
        registerButton.addActionListener(e -> registerUser());

        backButton = createStyledButton("Back to Login", ThemeColors.SECONDARY); // Changed text
        backButton.addActionListener(e -> goBack());

        buttonPanel.add(registerButton);
        buttonPanel.add(backButton);

        gbc.gridy = 6; gbc.gridx = 0; gbc.gridwidth = 2; // Updated grid y position
        add(buttonPanel, gbc);

        setLocationRelativeTo(null); // Center the frame
        // setVisible(true); // Visibility is handled when creating the frame
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
        field.setCaretColor(ThemeColors.TEXT); // Caret visibility
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.SECONDARY, 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        return field;
    }

    private JTextArea createAddressTextArea() {
        JTextArea area = new JTextArea(3, 20);
        area.setFont(new Font("Arial", Font.PLAIN, 14));
        area.setBackground(ThemeColors.CARD_BG);
        area.setForeground(ThemeColors.TEXT);
        area.setCaretColor(ThemeColors.TEXT); // Caret visibility
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.SECONDARY, 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        return area;
    }

    private JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField(20);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBackground(ThemeColors.CARD_BG);
        field.setForeground(ThemeColors.TEXT);
        field.setCaretColor(ThemeColors.TEXT); // Caret visibility
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

    private void registerUser() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();
        String password = new String(passwordField.getPassword());

        // Basic validation
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Simple email format check
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
             JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation Error", JOptionPane.WARNING_MESSAGE);
             return;
        }
         // Simple phone number check (example: digits, optional +,-, spaces)
        if (!phone.matches("^[\\d\\s+-]+$")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid phone number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
         // Password length check (example: at least 6 characters)
        if (password.length() < 6) {
             JOptionPane.showMessageDialog(this, "Password must be at least 6 characters long.", "Validation Error", JOptionPane.WARNING_MESSAGE);
             return;
        }


        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.connect();
            if (conn == null) {
                 JOptionPane.showMessageDialog(this, "Database connection failed. Cannot register.", "DB Error", JOptionPane.ERROR_MESSAGE);
                 return;
            }

             // --- IMPORTANT: Password Hashing (Example using a placeholder) ---
             // In a real application, NEVER store plain text passwords. Use a strong hashing algorithm like BCrypt.
             // String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt()); // Example
             String storedPassword = password; // !! Replace with hashed password !!
             // --- END Password Hashing Placeholder ---


            stmt = conn.prepareStatement("INSERT INTO customers (name, email, phone, address, password, role, loyalty_points) VALUES (?, ?, ?, ?, ?, ?, 0)"); // Added loyalty_points default
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.setString(4, address);
            stmt.setString(5, storedPassword); // Store the hashed password
            stmt.setString(6, "customer"); // Default role
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Registration Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Close registration window
            // Optional: Automatically open LoginFrame or directly log the user in
             new LoginFrame().setVisible(true); // Reopen login frame

        } catch (SQLException ex) {
            ex.printStackTrace();
            // Check for unique constraint violation (e.g., email already exists)
             if (ex.getSQLState().startsWith("23")) { // SQLState for Integrity Constraint Violation
                JOptionPane.showMessageDialog(this, "Email address '" + email + "' is already registered.", "Registration Error", JOptionPane.ERROR_MESSAGE);
             } else {
                JOptionPane.showMessageDialog(this, "Error registering user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
             }
        } finally {
             try { if (stmt != null) stmt.close(); } catch (SQLException ignored) {}
             try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }
    }

    private void goBack() {
        // Close this window. Assume LoginFrame might still be open or will be reopened.
        dispose();
        // Optionally, explicitly ensure LoginFrame is visible if it was disposed
        // SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
             // Apply theme settings globally for OptionPanes here
             UIManager.put("OptionPane.background", ThemeColors.DIALOG_BG);
             UIManager.put("Panel.background", ThemeColors.DIALOG_BG);
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
        SwingUtilities.invokeLater(() -> new RegisterFrame().setVisible(true));
    }
}