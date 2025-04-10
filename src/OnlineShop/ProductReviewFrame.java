package OnlineShop;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ProductReviewFrame extends JFrame {
    private JTextArea reviewArea;
    private JComboBox<String> productBox;
    private JButton submitButton;
    private JSlider ratingSlider;
    private int customerId;

    public ProductReviewFrame(int customerId) {
        this.customerId = customerId;
        setTitle("Leave a Review");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(ThemeColors.BACKGROUND);

        JPanel formPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(ThemeColors.BACKGROUND);

        // Product selection
        JPanel productPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        productPanel.setOpaque(false);
        productPanel.add(new JLabel("Select Product:"));
        productBox = new JComboBox<>();
        productBox.setPreferredSize(new Dimension(300, 30));
        styleComboBox(productBox);
        productPanel.add(productBox);
        formPanel.add(productPanel);

        // Rating
        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ratingPanel.setOpaque(false);
        ratingPanel.add(new JLabel("Rating (1-5):"));
        ratingSlider = new JSlider(1, 5, 3);
        ratingSlider.setMajorTickSpacing(1);
        ratingSlider.setPaintTicks(true);
        ratingSlider.setPaintLabels(true);
        ratingSlider.setBackground(ThemeColors.BACKGROUND);
        ratingPanel.add(ratingSlider);
        formPanel.add(ratingPanel);

        // Review text
        JPanel reviewTextPanel = new JPanel(new BorderLayout());
        reviewTextPanel.setOpaque(false);
        reviewTextPanel.add(new JLabel("Your Review:"), BorderLayout.NORTH);
        reviewArea = new JTextArea(5, 20);
        reviewArea.setLineWrap(true);
        reviewArea.setWrapStyleWord(true);
        reviewArea.setBackground(ThemeColors.CARD_BG);
        reviewArea.setForeground(ThemeColors.TEXT);
        reviewTextPanel.add(new JScrollPane(reviewArea), BorderLayout.CENTER);
        formPanel.add(reviewTextPanel);

        // Submit button
        submitButton = createStyledButton("Submit Review", ThemeColors.PRIMARY);
        submitButton.addActionListener(e -> submitReview());
        formPanel.add(submitButton);

        add(formPanel, BorderLayout.CENTER);
        loadProducts();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        comboBox.setBackground(ThemeColors.CARD_BG);
        comboBox.setForeground(ThemeColors.TEXT);
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }

    private void loadProducts() {
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT DISTINCT p.name " +
                 "FROM orders o " +
                 "JOIN order_items oi ON o.id = oi.order_id " +
                 "JOIN products p ON oi.product_id = p.id " +
                 "WHERE o.customer_id = ?")) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                productBox.addItem(rs.getString("name"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading products.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void submitReview() {
        String productName = (String) productBox.getSelectedItem();
        String review = reviewArea.getText();
        int rating = ratingSlider.getValue();

        if (productName == null || productName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a product.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (review.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please write your review.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO reviews (customer_id, product_id, review, rating) VALUES (?, (SELECT id FROM products WHERE name = ?), ?, ?)")) {
            stmt.setInt(1, customerId);
            stmt.setString(2, productName);
            stmt.setString(3, review);
            stmt.setInt(4, rating);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Review submitted successfully!");
            dispose();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error submitting review.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}