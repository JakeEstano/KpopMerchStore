package OnlineShop;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import com.formdev.flatlaf.FlatDarkLaf;

public class RegisterFrame extends JFrame {
    private JTextField nameField, emailField;
    private JPasswordField passwordField;
    private JButton registerButton, backButton;

    public RegisterFrame() {
        setTitle("HAMTEO - Register");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
        add(createFormLabel("Password:"), gbc);
        passwordField = createPasswordField();
        gbc.gridx = 1;
        add(passwordField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonPanel.setOpaque(false);
        
        registerButton = createStyledButton("Register", ThemeColors.PRIMARY);
        registerButton.addActionListener(e -> registerUser());
        
        backButton = createStyledButton("Back", ThemeColors.SECONDARY);
        backButton.addActionListener(e -> goBack());
        
        buttonPanel.add(registerButton);
        buttonPanel.add(backButton);
        
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
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

    private void registerUser() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO customers (name, email, password, role) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.setString(4, "customer");
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Registration Successful!");
            dispose();
            new LoginFrame();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error registering user.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void goBack() {
        new LoginFrame();
        dispose();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        new RegisterFrame();
    }
}