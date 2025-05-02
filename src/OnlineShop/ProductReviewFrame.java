package OnlineShop;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter; // Import specific MouseAdapter
import java.awt.event.MouseEvent;   // Import specific MouseEvent
import java.sql.*;

public class ProductReviewFrame extends JFrame {
    private JTextArea reviewArea;
    private JComboBox<ProductItem> productBox; // Changed to hold ProductItem objects
    private JButton submitButton;
    private JSlider ratingSlider;
    private int customerId;
    private int specificProductId = -1; // To store the product ID if passed
    private int specificOrderId = -1;   // To store the order ID if passed

    // --- Inner class to hold product ID and name for the ComboBox ---
    private static class ProductItem {
        int id;
        String name;

        ProductItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return name; // Display name in ComboBox
        }
    }
    // --- End Inner class ---


    // --- ORIGINAL CONSTRUCTOR (for general review access) ---
    public ProductReviewFrame(int customerId) {
        this.customerId = customerId;
        setupUI("Leave a Review");
        loadAvailableProductsForReview(); // Load all reviewable products
    }

    // --- NEW CONSTRUCTOR (for specific product review from Orders page) ---
    public ProductReviewFrame(int customerId, int productId, int orderId) {
        this.customerId = customerId;
        this.specificProductId = productId;
        this.specificOrderId = orderId; // Store orderId if needed later
        setupUI("Review Product"); // Slightly different title maybe
        loadSpecificProductForReview(productId); // Load only the specific product
    }

    // --- Common UI Setup Method ---
    private void setupUI(String title) {
        setTitle(title);
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(ThemeColors.BACKGROUND);

        JPanel formPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(ThemeColors.BACKGROUND);

        // Product selection/display
        JPanel productPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        productPanel.setOpaque(false);
        productPanel.add(new JLabel("Product:"));
        productBox = new JComboBox<>(); // Will hold ProductItem objects
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
        ratingSlider.setForeground(ThemeColors.TEXT); // Style slider text
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
        reviewArea.setCaretColor(ThemeColors.TEXT); // Make caret visible
        reviewTextPanel.add(new JScrollPane(reviewArea), BorderLayout.CENTER);
        formPanel.add(reviewTextPanel);

        // Submit button
        submitButton = createStyledButton("Submit Review", ThemeColors.PRIMARY);
        submitButton.addActionListener(e -> submitReview());
        formPanel.add(submitButton);

        add(formPanel, BorderLayout.CENTER);
        setLocationRelativeTo(null); // Center the frame
        // setVisible(true) should be called AFTER loading data
    }

    // --- Load ALL available products (Original Constructor) ---
    private void loadAvailableProductsForReview() {
        productBox.removeAllItems(); // Clear existing items
        boolean productsFound = false;
        try (Connection conn = DBConnection.connect()) {
            // Query for delivered products that haven't been reviewed by this customer for this specific order item if orderId is available
            // Simplified: Query delivered products not reviewed by this customer at all
             String query = "SELECT DISTINCT p.id, p.name " +
                           "FROM orders o " +
                           "JOIN order_items oi ON o.id = oi.order_id " +
                           "JOIN products p ON oi.product_id = p.id " +
                           "WHERE o.customer_id = ? " +
                           "AND o.status IN ('Delivered', 'Completed') " + // Allow review for Completed too
                           "AND NOT EXISTS (" +
                           "   SELECT 1 FROM reviews r " +
                           "   WHERE r.customer_id = o.customer_id " +
                           "   AND r.product_id = p.id" +
                           ") ORDER BY p.name"; // Order alphabetically

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, customerId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    productsFound = true;
                    productBox.addItem(new ProductItem(rs.getInt("id"), rs.getString("name")));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading products for review.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }

        if (!productsFound) {
            productBox.addItem(new ProductItem(-1, "No products available for review")); // Use ProductItem
            productBox.setEnabled(false);
            reviewArea.setEnabled(false);
            ratingSlider.setEnabled(false);
            submitButton.setEnabled(false);
             JOptionPane.showMessageDialog(this,
                "You have no delivered products available for review.",
                "No Products", JOptionPane.INFORMATION_MESSAGE);
        }
        setVisible(true); // Show frame after loading
    }


     // --- Load only the SPECIFIC product (New Constructor) ---
    private void loadSpecificProductForReview(int productId) {
        productBox.removeAllItems(); // Clear just in case
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT name FROM products WHERE id = ?")) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                productBox.addItem(new ProductItem(productId, rs.getString("name")));
                productBox.setEnabled(false); // Disable selection since it's specific
            } else {
                 productBox.addItem(new ProductItem(-1,"Product not found"));
                 productBox.setEnabled(false);
                 reviewArea.setEnabled(false);
                 ratingSlider.setEnabled(false);
                 submitButton.setEnabled(false);
                 JOptionPane.showMessageDialog(this, "Could not find the specified product.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading specific product.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        setVisible(true); // Show frame after loading
    }

    private void styleComboBox(JComboBox<ProductItem> comboBox) { // Updated type
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        comboBox.setBackground(ThemeColors.CARD_BG);
        comboBox.setForeground(ThemeColors.TEXT);
         // Custom renderer to style dropdown list items
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? ThemeColors.PRIMARY : ThemeColors.CARD_BG);
                setForeground(isSelected ? Color.WHITE : ThemeColors.TEXT);
                setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Add padding
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
        button.addMouseListener(new MouseAdapter() { // Specify package if needed
            Color originalBg = bgColor;
            @Override public void mouseEntered(MouseEvent e) { button.setBackground(ThemeColors.BUTTON_HOVER); }
            @Override public void mouseExited(MouseEvent e) { button.setBackground(originalBg); }
        });
        return button;
    }

    private void submitReview() {
        ProductItem selectedProduct = (ProductItem) productBox.getSelectedItem();
        String review = reviewArea.getText().trim(); // Trim whitespace
        int rating = ratingSlider.getValue();
        int productIdToReview;

        // Determine which product ID to use
        if (specificProductId != -1) {
            // If opened for a specific product, use that ID
            productIdToReview = specificProductId;
             // Double-check the combo box matches, though it should be disabled
            if (selectedProduct == null || selectedProduct.getId() != productIdToReview) {
                 JOptionPane.showMessageDialog(this, "Product selection mismatch.", "Internal Error", JOptionPane.ERROR_MESSAGE);
                 return;
            }
        } else {
            // If opened generally, get ID from the selected item
             if (selectedProduct == null || selectedProduct.getId() == -1) { // Check for placeholder item
                JOptionPane.showMessageDialog(this, "Please select a valid product.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            productIdToReview = selectedProduct.getId();
        }


        if (review.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please write your review.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }


        try (Connection conn = DBConnection.connect()) {
            // Check again if the review already exists for this customer and product
            String checkQuery = "SELECT 1 FROM reviews WHERE customer_id = ? AND product_id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, customerId);
                checkStmt.setInt(2, productIdToReview);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this,
                        "You've already reviewed this product.",
                        "Review Exists", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            // Insert the review
            String insertQuery = "INSERT INTO reviews (customer_id, product_id, review, rating, review_date, order_id) " + // Added order_id
                               "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?)"; // Added placeholder for order_id
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                stmt.setInt(1, customerId);
                stmt.setInt(2, productIdToReview);
                stmt.setString(3, review);
                stmt.setInt(4, rating);
                // Set order_id, handle case where it might not be available (e.g., general review)
                if (specificOrderId != -1) {
                    stmt.setInt(5, specificOrderId);
                } else {
                    // If orderId wasn't passed, find a relevant delivered order ID for this product/customer
                    // This might be complex or you could set it to NULL if the DB allows
                    stmt.setNull(5, Types.INTEGER); // Example: Set to NULL if not specifically linked
                }

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this,
                        "Review submitted successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose(); // Close the review window
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to submit review.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error submitting review: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}