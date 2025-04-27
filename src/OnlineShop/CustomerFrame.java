package OnlineShop;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import com.formdev.flatlaf.FlatDarkLaf;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

public class CustomerFrame extends JFrame {
    private int customerId;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private String currentCard;

    // Navigation buttons
    private JButton shopButton, eventButton, faqButton, noticeButton, aboutButton;

    // Cart components
    private JPanel cartPanel;

    // Wishlist components
    private JPanel wishlistPanel;

    // Product components
    private JPanel productsPanel;
    private JTextField searchField;

    private JPanel cartItemsPanel;
    private JLabel cartTotalLabel;

    private JPanel homeProductGridPanel; // For Home Tab
    private JPanel productsProductGridPanel; // For Products Tab

    private JPanel orderTrackingPanel;

    private JButton cartButton, wishlistButton, ordersButton;

    private AddressManager addressManager;

    public CustomerFrame(int customerId) {
        // Set window properties before making it displayable
        setTitle("케이팝 상점 - K-Pop Merch Store");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Handle fullscreen/undecorated mode before any displayable operations
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = env.getDefaultScreenDevice();

        if (device.isFullScreenSupported()) {
            setUndecorated(true);  // Must be called before frame is displayable
            device.setFullScreenWindow(this);
        } else {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }

        // Initialize instance variables
        this.customerId = customerId;
        this.addressManager = new AddressManager(this, customerId);
        this.currentCard = "Home";

        // Initialize main components
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(ThemeColors.BACKGROUND);

        // Initialize product grid panels (one for home, one for products page)
        homeProductGridPanel = new JPanel(new WrapLayout(FlowLayout.CENTER, 20, 20));
        homeProductGridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        homeProductGridPanel.setBackground(ThemeColors.BACKGROUND);

        productsProductGridPanel = new JPanel(new WrapLayout(FlowLayout.CENTER, 20, 20));
        productsProductGridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        productsProductGridPanel.setBackground(ThemeColors.BACKGROUND);


        // Create all panels
        JPanel homePanel = createHomePanel(); // Will use homeProductGridPanel
        this.productsPanel = createProductsPanel(); // Will use productsProductGridPanel
        this.cartPanel = createCartPanel();
        this.wishlistPanel = createWishlistPanel();
        this.orderTrackingPanel = createOrderTrackingPanel();
        JPanel eventPanel = createEventPanel();
        JPanel faqPanel = createFAQPanel();
        JPanel noticePanel = createNoticePanel();
        JPanel aboutPanel = createAboutPanel();

        // Add panels to the mainPanel with the CardLayout
        mainPanel.add(homePanel, "Home");
        mainPanel.add(productsPanel, "Products");
        mainPanel.add(cartPanel, "Cart");
        mainPanel.add(orderTrackingPanel, "Orders");
        mainPanel.add(wishlistPanel, "Wishlist");
        mainPanel.add(eventPanel, "Events");
        mainPanel.add(faqPanel, "FAQ");
        mainPanel.add(noticePanel, "Notices");
        mainPanel.add(aboutPanel, "About");

        add(mainPanel, BorderLayout.CENTER);
        add(createNavigationBar(), BorderLayout.NORTH);

        // Load data after UI is fully initialized
        SwingUtilities.invokeLater(() -> {
            loadAllProducts(); // Load products for both home and products panel initially
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createNavigationBar() {
        JPanel navBar = new JPanel(new BorderLayout()) {
            @Override
            public boolean isOptimizedDrawingEnabled() {
                return false; // Disable optimized drawing to allow overlap
            }
        };
        navBar.setBackground(ThemeColors.BACKGROUND);
        navBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Logo
        JLabel logo = new JLabel("케이팝 상점 ", SwingConstants.LEFT);
        logo.setFont(new Font("Malgun Gothic", Font.BOLD, 24));
        logo.setForeground(ThemeColors.PRIMARY);
        navBar.add(logo, BorderLayout.WEST);

        // Main navigation buttons
        JPanel navButtons = new JPanel(new GridLayout(1, 5, 15, 0));
        navButtons.setOpaque(false);

        shopButton = createNavButton("SHOP");
        eventButton = createNavButton("EVENT");
        faqButton = createNavButton("FAQ");
        noticeButton = createNavButton("NOTICE");
        aboutButton = createNavButton("ABOUT US");

        navButtons.add(shopButton);
        navButtons.add(eventButton);
        navButtons.add(faqButton);
        navButtons.add(noticeButton);
        navButtons.add(aboutButton);

        JPanel centeredNavPanel = new JPanel(new BorderLayout());
        centeredNavPanel.setOpaque(false);
        centeredNavPanel.add(navButtons, BorderLayout.CENTER);
        centeredNavPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        navBar.add(centeredNavPanel, BorderLayout.CENTER);

        // User buttons
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)) {
            @Override
            public boolean isOptimizedDrawingEnabled() {
                return false; // Allow badge overlap
            }
        };
        userPanel.setOpaque(false);

        cartButton = createBadgeButtonWithCounter("CART", getCartItemCount());
        wishlistButton = createBadgeButtonWithCounter("WISHLIST", getWishlistItemCount());
        ordersButton = createBadgeButtonWithCounter("ORDERS", getOrderCount());
        JButton reviewButton = createUserButton("REVIEWS");
        JButton logoutButton = createUserButton("LOGOUT");

        userPanel.add(cartButton);
        userPanel.add(wishlistButton);
        userPanel.add(ordersButton);
        userPanel.add(reviewButton);
        userPanel.add(logoutButton);

        navBar.add(userPanel, BorderLayout.EAST);

        // Add action listeners
        shopButton.addActionListener(e -> showCard("Home")); // Changed to Home for main shop view
        eventButton.addActionListener(e -> showCard("Events"));
        faqButton.addActionListener(e -> showCard("FAQ"));
        noticeButton.addActionListener(e -> showCard("Notices"));
        aboutButton.addActionListener(e -> showCard("About"));

        cartButton.addActionListener(e -> {
            loadCartItems();
            showCard("Cart");
        });

        wishlistButton.addActionListener(e -> {
            loadWishlist();
            showCard("Wishlist");
        });

        ordersButton.addActionListener(e -> {
            loadOrders();
            showCard("Orders");
        });

        reviewButton.addActionListener(e -> new ProductReviewFrame(customerId));
        logoutButton.addActionListener(e -> logout());

        return navBar;
    }

    private JButton createUserButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(ThemeColors.CARD_BG);
        button.setForeground(ThemeColors.TEXT);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.SECONDARY, 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        button.setFocusPainted(false);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(ThemeColors.SECONDARY);
                button.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(ThemeColors.CARD_BG);
                button.setForeground(ThemeColors.TEXT);
            }
        });
        return button;
    }

    // New method to create buttons with counter badges in the upper right
    private JButton createBadgeButtonWithCounter(String text, int count) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                if (count > 0) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Badge dimensions and position
                    int diameter = 18;
                    int x = getWidth() - diameter - 1;  // Position from right edge
                    int y = -diameter/3;  // Partially above the button

                    // Draw badge background
                    g2.setColor(ThemeColors.PRIMARY);
                    g2.fillOval(x, y, diameter, diameter);

                    // Draw badge text
                    String countText = count > 99 ? "99+" : String.valueOf(count);
                    g2.setColor(Color.WHITE);
                    g2.setFont(getFont().deriveFont(Font.BOLD, 10f));
                    FontMetrics fm = g2.getFontMetrics();
                    int textWidth = fm.stringWidth(countText);
                    int textHeight = fm.getHeight();
                    g2.drawString(countText,
                        x + (diameter - textWidth)/2,
                        y + (diameter - textHeight)/2 + fm.getAscent());

                    g2.dispose();
                }
            }

            @Override
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                // Add extra space to prevent text clipping
                return new Dimension(size.width + 5, size.height);
            }
        };

        // Standard button styling
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(ThemeColors.CARD_BG);
        button.setForeground(ThemeColors.TEXT);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.SECONDARY, 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        button.setFocusPainted(false);

        // Hover effects
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(ThemeColors.SECONDARY);
                button.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(ThemeColors.CARD_BG);
                button.setForeground(ThemeColors.TEXT);
            }
        });

        return button;
    }

    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(ThemeColors.TEXT);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(ThemeColors.PRIMARY);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(ThemeColors.TEXT);
            }
        });
        return button;
    }

    // Methods to get item counts from database
    private int getCartItemCount() {
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT SUM(quantity) FROM cart WHERE customer_id = ?")) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException ex) {
             System.err.println("Error getting cart count: " + ex.getMessage());
            return 0;
        }
    }

    private int getWishlistItemCount() {
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM wishlist WHERE customer_id = ?")) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException ex) {
             System.err.println("Error getting wishlist count: " + ex.getMessage());
            return 0;
        }
    }

    private int getOrderCount() {
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM orders WHERE customer_id = ?")) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException ex) {
             System.err.println("Error getting order count: " + ex.getMessage());
            return 0;
        }
    }

    // Inside CustomerFrame.java
    private JPanel createHomePanel() {
        JPanel homePanel = new JPanel(new BorderLayout());
        homePanel.setBackground(ThemeColors.BACKGROUND);

        // ========== BANNER SECTION ==========
        JPanel bannerPanel = new JPanel(new BorderLayout());
        bannerPanel.setBackground(ThemeColors.CARD_BG);
        bannerPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        bannerPanel.setPreferredSize(new Dimension(1920, 180)); // Adjust height as needed

        try {
            URL gifURL = getClass().getResource("/images/promotional.gif");
            if (gifURL == null) {
                gifURL = new File("images/promotional.gif").toURI().toURL(); // Try relative path
            }

            if (gifURL != null) {
                final ImageIcon originalIcon = new ImageIcon(gifURL);
                 // Create a panel that centers the image if the panel is larger
                JPanel imagePanel = new JPanel(new GridBagLayout());
                imagePanel.setBackground(ThemeColors.CARD_BG); // Match banner background
                JLabel imageLabel = new JLabel(originalIcon);
                imagePanel.add(imageLabel); // Add label to center


                imagePanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                imagePanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        showCard("Products"); // Show the dedicated products panel on click
                        loadProductsToPanel(productsProductGridPanel); // Ensure it's loaded
                    }
                });

                bannerPanel.add(imagePanel, BorderLayout.CENTER);
            } else {
                throw new FileNotFoundException("Banner image not found in any location");
            }
        } catch (Exception e) {
            System.err.println("Banner error: " + e.getMessage());
            // Fallback Panel
            JPanel fallbackPanel = new JPanel(new BorderLayout());
            fallbackPanel.setBackground(new Color(50, 50, 70));
            fallbackPanel.setBorder(BorderFactory.createLineBorder(ThemeColors.PRIMARY, 2));
            JLabel fallbackLabel = new JLabel("HOT DEALS - CLICK HERE", SwingConstants.CENTER);
            fallbackLabel.setFont(new Font("Arial", Font.BOLD, 22));
            fallbackLabel.setForeground(Color.WHITE);
            fallbackLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            fallbackLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showCard("Products"); // Show the dedicated products panel on click
                    loadProductsToPanel(productsProductGridPanel); // Ensure it's loaded
                }
            });
            fallbackPanel.add(fallbackLabel, BorderLayout.CENTER);
            bannerPanel.add(fallbackPanel, BorderLayout.CENTER);
        }

        // Create a top panel to hold both banner and search components vertically
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(ThemeColors.BACKGROUND);
        topPanel.add(bannerPanel, BorderLayout.NORTH);

        // ========== SEARCH PANEL ==========
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        searchPanel.setBackground(ThemeColors.BACKGROUND);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        searchField = new JTextField(30);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBackground(ThemeColors.CARD_BG);
        searchField.setForeground(ThemeColors.TEXT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.SECONDARY, 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        searchField.addActionListener(e -> filterProducts()); // Trigger filter on Enter

        JButton searchButton = createStyledButton("Search", ThemeColors.PRIMARY);
        searchButton.addActionListener(e -> filterProducts()); // Trigger filter on click

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Add search panel below the banner in the top panel
        topPanel.add(searchPanel, BorderLayout.CENTER);

        // Add the complete top panel to the NORTH of the home panel
        homePanel.add(topPanel, BorderLayout.NORTH);

        // ========== PRODUCT GRID ==========
        // Note: homeProductGridPanel is already initialized as a class member

        // Create a wrapper panel for proper scrolling behavior with WrapLayout
        JPanel gridWrapper = new JPanel(new BorderLayout());
        // **** THIS IS THE KEY CHANGE ****
        gridWrapper.add(homeProductGridPanel, BorderLayout.CENTER); // Add grid to the CENTER
        // *********************************
        gridWrapper.setBackground(ThemeColors.BACKGROUND); // Match background

        JScrollPane scrollPane = new JScrollPane(gridWrapper); // Scroll the wrapper
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Faster scroll speed
        scrollPane.setBorder(null); // Remove scrollpane border

        homePanel.add(scrollPane, BorderLayout.CENTER);

        // Load initial products to home panel (will be done later in constructor)
        // loadProductsToPanel(homeProductGridPanel); // Don't load here, load in constructor

        return homePanel;
    }

    // Load products into BOTH panels initially
    private void loadAllProducts() {
        loadProductsToPanel(homeProductGridPanel);    // Load for Home tab
        loadProductsToPanel(productsProductGridPanel); // Load for Products tab
    }

    // Filter products based on search text IN THE CURRENTLY VIEWED PANEL
    private void filterProducts() {
        String searchText = searchField.getText().trim().toLowerCase();
        JPanel targetPanel;
        if (currentCard.equals("Home")) {
            targetPanel = homeProductGridPanel;
        } else if (currentCard.equals("Products")) {
            targetPanel = productsProductGridPanel;
        } else {
            return; // Don't filter if not on a product view
        }

        targetPanel.removeAll(); // Clear current display

        String query;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = DBConnection.connect();
            if (searchText.isEmpty()) {
                // If search is empty, load all products to the target panel
                loadProductsToPanel(targetPanel);
                targetPanel.revalidate();
                targetPanel.repaint();
                scrollToTop(targetPanel); // Scroll to top after reloading all
                return; // Exit the method here
            } else {
                // If search has text, filter based on name or group
                query = "SELECT id, name, group_name, price, description, image_path FROM products WHERE LOWER(name) LIKE ? OR LOWER(group_name) LIKE ? ORDER BY name";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, "%" + searchText + "%");
                stmt.setString(2, "%" + searchText + "%");
            }

            ResultSet rs = stmt.executeQuery();
            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String groupName = rs.getString("group_name");
                double price = rs.getDouble("price");
                String description = rs.getString("description");
                String imagePath = rs.getString("image_path");

                targetPanel.add(createProductCard(id, name, groupName, price, description, imagePath));
            }

            if (!hasResults) {
                showNoResultsMessage(searchText, targetPanel);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error searching products: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException ignored) {}
            try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }

        targetPanel.revalidate();
        targetPanel.repaint();
        scrollToTop(targetPanel); // Scroll to top after filtering
    }

    // Helper to scroll the parent JScrollPane to the top
    private void scrollToTop(JPanel panel) {
        Container parent = panel.getParent();
        while (parent != null && !(parent instanceof JViewport)) {
            parent = parent.getParent();
        }
        if (parent instanceof JViewport) {
            JViewport viewport = (JViewport) parent;
            viewport.setViewPosition(new Point(0, 0));
        }
    }


    private void showNoResultsMessage(String searchText, JPanel panel) {
        panel.removeAll(); // Clear previous results or message
        panel.setLayout(new BorderLayout()); // Use BorderLayout to center the message
        JLabel noResultsLabel = new JLabel("No products found matching \"" + searchText + "\"", SwingConstants.CENTER);
        noResultsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        noResultsLabel.setForeground(Color.GRAY);
        panel.add(noResultsLabel, BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();
    }

    private void refreshProductDisplay() {
         // Refresh both panels if they exist
        if (homeProductGridPanel != null) {
            homeProductGridPanel.revalidate();
            homeProductGridPanel.repaint();
            Container parent = homeProductGridPanel.getParent();
            if (parent != null) {
                parent.revalidate();
                parent.repaint();
            }
        }
         if (productsProductGridPanel != null) {
            productsProductGridPanel.revalidate();
            productsProductGridPanel.repaint();
             Container parent = productsProductGridPanel.getParent();
            if (parent != null) {
                parent.revalidate();
                parent.repaint();
            }
        }

        if (this.isDisplayable()) {
            this.revalidate();
            this.repaint();
        }
    }

    // Updated createProductCard to accept imagePath
    private JPanel createProductCard(int id, String name, String groupName, double price, String description, String imagePathFromDB) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(ThemeColors.CARD_BG);
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        // --- INCREASED WIDTH HERE ---
        card.setPreferredSize(new Dimension(341, 341)); // Increased width from 250
        // ---------------------------

        // Image container with fixed size (keep internal image size consistent for now)
        JPanel imageContainer = new JPanel(new BorderLayout());
        imageContainer.setPreferredSize(new Dimension(230, 230)); // Image container size remains
        imageContainer.setBackground(ThemeColors.BACKGROUND);

        // --- Use the robust image loading logic ---
        ImageIcon icon = loadImageIcon(imagePathFromDB, name);

        if (icon != null && isIconValid(icon)) {
             // Scale image smoothly to fit the image container
             Image scaledImage = icon.getImage().getScaledInstance(230, 230, Image.SCALE_SMOOTH);
             JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
             imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
             imageContainer.add(imageLabel, BorderLayout.CENTER);
        } else {
             // Fallback if no valid icon could be loaded
             JLabel noImageLabel = new JLabel("No Image Available", SwingConstants.CENTER);
             noImageLabel.setFont(new Font("Arial", Font.ITALIC, 12));
             noImageLabel.setForeground(Color.GRAY);
             imageContainer.add(noImageLabel, BorderLayout.CENTER);
        }
         // --- End of image loading ---

        card.add(imageContainer, BorderLayout.CENTER);

        // Product info panel
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(ThemeColors.TEXT);

        // Format price correctly
        JLabel priceLabel = new JLabel(String.format("₱%.2f", price));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        priceLabel.setForeground(ThemeColors.PRIMARY);

        infoPanel.add(nameLabel, BorderLayout.NORTH);
        infoPanel.add(priceLabel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton detailsButton = createStyledButton("Details", ThemeColors.SECONDARY);
        detailsButton.setFont(new Font("Arial", Font.BOLD, 12));
        detailsButton.addActionListener(e -> showProductDetails(id, name, price, description, imagePathFromDB));


        JButton addToCartButton = createStyledButton("Add to Cart", ThemeColors.PRIMARY);
        addToCartButton.setFont(new Font("Arial", Font.BOLD, 12));
        addToCartButton.addActionListener(e -> addToCart(id, name, price));

        buttonPanel.add(detailsButton);
        buttonPanel.add(addToCartButton);

        infoPanel.add(buttonPanel, BorderLayout.SOUTH);
        card.add(infoPanel, BorderLayout.SOUTH);

        return card;
    }

     // Updated showProductDetails to accept imagePath
    private void showProductDetails(int productId, String name, double price, String description, String imagePathFromDB) {
        JDialog detailsDialog = new JDialog(this, "Product Details", true);
        detailsDialog.setSize(400, 450); // Slightly taller for description
        detailsDialog.setLayout(new BorderLayout());
        detailsDialog.getContentPane().setBackground(ThemeColors.BACKGROUND);

        JPanel detailsPanel = new JPanel(new BorderLayout(10, 10));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        detailsPanel.setBackground(ThemeColors.BACKGROUND);

        // Image Panel
        JPanel imagePanel = new JPanel(new GridBagLayout()); // Use GridBagLayout to center
        imagePanel.setPreferredSize(new Dimension(200, 200));
        imagePanel.setOpaque(false); // Make transparent

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // --- Use the robust image loading logic ---
        ImageIcon icon = loadImageIcon(imagePathFromDB, name); // Pass DB path and name

        if (icon != null && isIconValid(icon)) {
            Image scaledImage = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
        } else {
            imageLabel.setText("No Image Available");
            imageLabel.setForeground(ThemeColors.TEXT);
        }
        // --- End of image loading ---

        imagePanel.add(imageLabel); // Add to centering panel
        detailsPanel.add(imagePanel, BorderLayout.NORTH);

        // Info Panel (Name, Price, Description)
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS)); // Vertical layout
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // Add spacing

        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        nameLabel.setForeground(ThemeColors.PRIMARY);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center horizontally

        JLabel priceLabel = new JLabel(String.format("Price: ₱%.2f", price), SwingConstants.CENTER);
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        priceLabel.setForeground(ThemeColors.TEXT);
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea descArea = new JTextArea(description != null ? description : "No description available.");
        descArea.setEditable(false);
        descArea.setFont(new Font("Arial", Font.PLAIN, 14));
        descArea.setBackground(ThemeColors.BACKGROUND);
        descArea.setForeground(ThemeColors.TEXT);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        JScrollPane descScrollPane = new JScrollPane(descArea);
        descScrollPane.setBorder(BorderFactory.createEmptyBorder()); // Remove border
        descScrollPane.setPreferredSize(new Dimension(340, 80)); // Limit description height

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(priceLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        infoPanel.add(descScrollPane);

        detailsPanel.add(infoPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0)); // Space above buttons

        JButton addToCartButton = createStyledButton("Add to Cart", ThemeColors.PRIMARY);
        addToCartButton.addActionListener(e -> {
            addToCart(productId, name, price);
            detailsDialog.dispose();
        });

        JButton addToWishlistButton = createStyledButton("Wishlist", ThemeColors.SECONDARY);
        addToWishlistButton.addActionListener(e -> {
            addToWishlist(productId, name, price);
            detailsDialog.dispose();
        });

        JButton closeButton = createStyledButton("Close", ThemeColors.CARD_BG);
        closeButton.setForeground(ThemeColors.TEXT);
        closeButton.addActionListener(e -> detailsDialog.dispose());

        buttonPanel.add(addToCartButton);
        buttonPanel.add(addToWishlistButton);
        buttonPanel.add(closeButton);

        detailsPanel.add(buttonPanel, BorderLayout.SOUTH);
        detailsDialog.add(detailsPanel, BorderLayout.CENTER);
        detailsDialog.setLocationRelativeTo(this);
        detailsDialog.setVisible(true);
    }


    private JPanel createProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeColors.BACKGROUND);

        // Removed the "Back to Home" button as the main SHOP button now leads here
        // Or, change the SHOP button action to show "Products" card instead of "Home"

        // Use the class member productsProductGridPanel directly
        productsProductGridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add the grid panel (which uses WrapLayout) to a scroll pane
        JScrollPane scrollPane = new JScrollPane(productsProductGridPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Improve scroll speed
        scrollPane.setBorder(null); // Remove scrollpane border

        panel.add(scrollPane, BorderLayout.CENTER);

        // Products will be loaded later by loadAllProducts or filterProducts
        return panel;
    }

    // Updated loadProductsToPanel to select and pass imagePath
    private void loadProductsToPanel(JPanel targetPanel) {
        targetPanel.removeAll(); // Clear previous items

        // Use the correct layout for the target panel
        if (targetPanel == homeProductGridPanel || targetPanel == productsProductGridPanel) {
             targetPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 20, 20)); // Use WrapLayout
        } else {
             targetPanel.setLayout(new BoxLayout(targetPanel, BoxLayout.Y_AXIS)); // Default for others like cart/wishlist/orders
        }


        // SQL Query now includes image_path
        String sql = "SELECT id, name, group_name, price, description, image_path FROM products ORDER BY name";

        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String groupName = rs.getString("group_name");
                double price = rs.getDouble("price"); // Assuming price is stored directly
                //double pricePHP = priceUSD * 66.67; // Remove USD conversion if price is already PHP
                String description = rs.getString("description");
                String imagePath = rs.getString("image_path"); // Get the path

                // Pass imagePath to createProductCard
                targetPanel.add(createProductCard(id, name, groupName, price, description, imagePath));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading products: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        targetPanel.revalidate();
        targetPanel.repaint();
    }


    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeColors.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Back button
        JButton backButton = new JButton("← Back to Home");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(ThemeColors.CARD_BG);
        backButton.setForeground(ThemeColors.TEXT);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        backButton.addActionListener(e -> showCard("Home"));
        panel.add(backButton, BorderLayout.NORTH);

        // Main cart content panel
        JPanel cartContentPanel = new JPanel(new BorderLayout());
        cartContentPanel.setBackground(ThemeColors.BACKGROUND);

        // Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(ThemeColors.BACKGROUND);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Your Shopping Cart");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(ThemeColors.PRIMARY);
        titlePanel.add(title);

        cartContentPanel.add(titlePanel, BorderLayout.NORTH);

        // Cart items panel
        cartItemsPanel = new JPanel();
        cartItemsPanel.setLayout(new BoxLayout(cartItemsPanel, BoxLayout.Y_AXIS));
        cartItemsPanel.setBackground(ThemeColors.BACKGROUND);
        cartItemsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(cartItemsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ThemeColors.BACKGROUND);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); // Ensure vertical scroll appears when needed
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Adjust scroll speed

        cartContentPanel.add(scrollPane, BorderLayout.CENTER);

        // Summary panel (simplified)
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBackground(ThemeColors.CARD_BG);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Total amount
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        totalPanel.setBackground(ThemeColors.CARD_BG);

        JLabel totalLabel = new JLabel("Total Amount:");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalLabel.setForeground(ThemeColors.TEXT);

        cartTotalLabel = new JLabel("₱0.00");
        cartTotalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        cartTotalLabel.setForeground(ThemeColors.PRIMARY);

        totalPanel.add(totalLabel);
        totalPanel.add(cartTotalLabel);
        summaryPanel.add(totalPanel);

        // Checkout button
        summaryPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton checkoutButton = createStyledButton("PROCEED TO CHECKOUT", ThemeColors.PRIMARY);
        checkoutButton.setFont(new Font("Arial", Font.BOLD, 16));
        //checkoutButton.setBackground(ThemeColors.PRIMARY); // Already handled by createStyledButton
        //checkoutButton.setForeground(Color.WHITE);
        checkoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        checkoutButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, checkoutButton.getPreferredSize().height));
        //checkoutButton.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30)); // Handled by createStyledButton
        checkoutButton.addActionListener(e -> checkout());
        summaryPanel.add(checkoutButton);

        cartContentPanel.add(summaryPanel, BorderLayout.SOUTH);
        panel.add(cartContentPanel, BorderLayout.CENTER);

        return panel;
    }

    // Updated createCartItemPanel to accept imagePath
    private JPanel createCartItemPanel(int cartId, String productName, double price, int quantity, String color, String size, String imagePathFromDB) {
        JPanel itemPanel = new JPanel(new BorderLayout(10, 10));
        itemPanel.setBackground(ThemeColors.CARD_BG);
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.BACKGROUND),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // Store cart ID on the panel
        itemPanel.putClientProperty("cartId", cartId);

        // Main content panel with all components
        JPanel contentPanel = new JPanel(new BorderLayout(10, 0));
        contentPanel.setOpaque(false);

        // Left panel for checkbox and image
        JPanel leftPanel = new JPanel(new BorderLayout(10, 0));
        leftPanel.setOpaque(false);

        // Circular checkbox (centered vertically)
        JCheckBox selectCheckbox = createCircularCheckbox(cartId, price, quantity); // Use helper
        selectCheckbox.setSelected(false); // Default to unchecked for selection

        JPanel checkboxPanel = new JPanel(new GridBagLayout());
        checkboxPanel.setOpaque(false);
        checkboxPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));
        checkboxPanel.add(selectCheckbox);
        leftPanel.add(checkboxPanel, BorderLayout.WEST);

        // Product image (centered vertically)
        JPanel imagePanel = new JPanel(new GridBagLayout());
        imagePanel.setPreferredSize(new Dimension(80, 80));
        imagePanel.setBackground(ThemeColors.BACKGROUND);
        imagePanel.setBorder(BorderFactory.createLineBorder(ThemeColors.SECONDARY, 1));

        // --- Use robust image loading ---
        ImageIcon icon = loadImageIcon(imagePathFromDB, productName);
        if (icon != null && isIconValid(icon)) {
            Image scaledImage = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
            imagePanel.add(imageLabel);
        } else {
            JLabel noImageLabel = new JLabel("No Image", SwingConstants.CENTER);
            noImageLabel.setFont(new Font("Arial", Font.ITALIC, 10));
            noImageLabel.setForeground(Color.GRAY);
            imagePanel.add(noImageLabel);
        }
        // --- End image loading ---

        leftPanel.add(imagePanel, BorderLayout.CENTER);
        contentPanel.add(leftPanel, BorderLayout.WEST);

        // Center panel for product info
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(productName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(ThemeColors.TEXT);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel detailsLabel = new JLabel("Color: " + (color != null ? color : "Default") +
                              " | Size: " + (size != null ? size : "Default"));
        detailsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        detailsLabel.setForeground(ThemeColors.TEXT);
        detailsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel priceLabel = new JLabel(String.format("₱%.2f", price));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        priceLabel.setForeground(ThemeColors.PRIMARY);
        priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        centerPanel.add(nameLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        centerPanel.add(detailsLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        centerPanel.add(priceLabel);

        contentPanel.add(centerPanel, BorderLayout.CENTER);

        // Right panel for quantity controls and remove button (centered vertically)
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(150, 80));

        JPanel rightComponentsPanel = new JPanel();
        rightComponentsPanel.setLayout(new BoxLayout(rightComponentsPanel, BoxLayout.Y_AXIS));
        rightComponentsPanel.setOpaque(false);

        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        quantityPanel.setOpaque(false);

        JLabel qtyLabel = new JLabel(String.valueOf(quantity), SwingConstants.CENTER);
        qtyLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        qtyLabel.setForeground(ThemeColors.TEXT);
        qtyLabel.setPreferredSize(new Dimension(40, 30));
        qtyLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton minusButton = new JButton("-");
        styleQuantityButton(minusButton);
        minusButton.addActionListener(e -> {
            int newQuantity = Integer.parseInt(qtyLabel.getText()) - 1;
            updateCartItemQuantity(cartId, newQuantity, qtyLabel);
        });

        JButton plusButton = new JButton("+");
        styleQuantityButton(plusButton);
        plusButton.addActionListener(e -> {
            int newQuantity = Integer.parseInt(qtyLabel.getText()) + 1;
            updateCartItemQuantity(cartId, newQuantity, qtyLabel);
        });

        quantityPanel.add(minusButton);
        quantityPanel.add(qtyLabel);
        quantityPanel.add(plusButton);

        rightComponentsPanel.add(quantityPanel);
        rightComponentsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton removeButton = new JButton("Remove");
        removeButton.setFont(new Font("Arial", Font.BOLD, 12));
        removeButton.setForeground(new Color(200, 0, 0));
        removeButton.setContentAreaFilled(false);
        removeButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        removeButton.addActionListener(e -> {
            removeCartItem(cartId);
            calculateSelectedTotal(); // Recalculate after removing
        });

        JPanel removeButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        removeButtonPanel.setOpaque(false);
        removeButtonPanel.add(removeButton);

        rightComponentsPanel.add(removeButtonPanel);

        rightPanel.add(rightComponentsPanel);
        contentPanel.add(rightPanel, BorderLayout.EAST);

        itemPanel.add(contentPanel, BorderLayout.CENTER);

        return itemPanel;
    }


    private JCheckBox createCircularCheckbox(int id, double price, int quantity) {
        JCheckBox checkbox = new JCheckBox() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw circular background
                g2.setColor(ThemeColors.BACKGROUND);
                g2.fillOval(0, 0, 20, 20);

                // Draw circular border
                g2.setColor(ThemeColors.PRIMARY);
                g2.drawOval(0, 0, 20, 20);

                // Draw checkmark if selected
                if (isSelected()) {
                    g2.setColor(ThemeColors.PRIMARY);
                    g2.fillOval(5, 5, 10, 10);
                }
            }
        };
        // checkbox.setSelected(true); // Changed - default is unselected
        checkbox.setPreferredSize(new Dimension(20, 20));
        checkbox.setBorder(BorderFactory.createEmptyBorder());
        checkbox.setContentAreaFilled(false);
        checkbox.setFocusPainted(false);
        checkbox.putClientProperty("id", id);
        checkbox.putClientProperty("price", price);
        checkbox.putClientProperty("quantity", quantity);
        checkbox.addActionListener(e -> calculateSelectedTotal());
        return checkbox;
    }

    private void styleQuantityButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(ThemeColors.SECONDARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(25, 25));
        button.setBorder(BorderFactory.createLineBorder(ThemeColors.SECONDARY, 1));
    }

    private void updateCartItemQuantity(int cartId, int newQuantity, JLabel qtyLabel) {
        if (newQuantity < 1) {
            removeCartItem(cartId);
            return;
        }

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE cart SET quantity = ? WHERE id = ?")) {

            stmt.setInt(1, newQuantity);
            stmt.setInt(2, cartId);
            stmt.executeUpdate();

            // Update the label if provided
            if (qtyLabel != null) {
                qtyLabel.setText(String.valueOf(newQuantity));
            }

            // Update the checkbox's quantity property
            for (Component comp : cartItemsPanel.getComponents()) {
                if (comp instanceof JPanel) {
                    JPanel itemPanel = (JPanel) comp;
                    Integer panelCartId = (Integer) itemPanel.getClientProperty("cartId");
                    if (panelCartId != null && panelCartId == cartId) {
                        JCheckBox checkbox = findCheckboxInPanel(itemPanel);
                        if (checkbox != null) {
                            checkbox.putClientProperty("quantity", newQuantity);
                        }
                        break; // Found the panel, no need to continue loop
                    }
                }
            }

            calculateSelectedTotal(); // Update the total

        } catch (SQLException ex) {
            System.err.println("Error updating quantity: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                "Error updating quantity",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void calculateSelectedTotal() {
        double total = 0.0;

        for (Component comp : cartItemsPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel itemPanel = (JPanel) comp;
                JCheckBox checkbox = findCheckboxInPanel(itemPanel);

                if (checkbox != null && checkbox.isSelected()) {
                    try {
                        // Retrieve properties safely
                        Object priceObj = checkbox.getClientProperty("price");
                        Object quantityObj = checkbox.getClientProperty("quantity");

                        if (priceObj instanceof Double && quantityObj instanceof Integer) {
                            double price = (Double) priceObj;
                            int quantity = (Integer) quantityObj;
                            total += price * quantity;
                        } else {
                             System.err.println("Warning: Invalid data type found in checkbox client properties for cart item.");
                        }
                    } catch (Exception e) {
                         System.err.println("Error calculating total from checkbox properties: " + e.getMessage());
                    }
                }
            }
        }

        cartTotalLabel.setText(String.format("₱%.2f", total));
    }


    private void removeCartItem(int cartId) {
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                 "DELETE FROM cart WHERE id = ?")) {

            stmt.setInt(1, cartId);
            stmt.executeUpdate();
            loadCartItems(); // Refresh cart

        } catch (SQLException ex) {
            System.err.println("Error removing item: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                "Error removing item from cart",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createWishlistPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeColors.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Back button
        JButton backButton = new JButton("← Back to Home");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(ThemeColors.CARD_BG);
        backButton.setForeground(ThemeColors.TEXT);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        backButton.addActionListener(e -> showCard("Home"));
        panel.add(backButton, BorderLayout.NORTH);

        // Main wishlist content panel
        JPanel wishlistContentPanel = new JPanel(new BorderLayout());
        wishlistContentPanel.setBackground(ThemeColors.BACKGROUND);

        // Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(ThemeColors.BACKGROUND);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Your Wishlist");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(ThemeColors.PRIMARY);
        titlePanel.add(title);

        wishlistContentPanel.add(titlePanel, BorderLayout.NORTH);

        // Wishlist items panel
        JPanel wishlistItemsPanel = new JPanel();
        wishlistItemsPanel.setLayout(new BoxLayout(wishlistItemsPanel, BoxLayout.Y_AXIS));
        wishlistItemsPanel.setBackground(ThemeColors.BACKGROUND);
        wishlistItemsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Store reference to items panel for later access
        panel.putClientProperty("wishlistItemsPanel", wishlistItemsPanel); // Make sure key matches

        JScrollPane scrollPane = new JScrollPane(wishlistItemsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ThemeColors.BACKGROUND);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);


        wishlistContentPanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(ThemeColors.CARD_BG);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Remove selected button
        JButton removeSelectedButton = createStyledButton("REMOVE", new Color(200, 0, 0));
        removeSelectedButton.setFont(new Font("Arial", Font.BOLD, 16));
        //removeSelectedButton.setBackground(new Color(200, 0, 0)); // handled by createStyledButton
        //removeSelectedButton.setForeground(Color.WHITE);
        removeSelectedButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeSelectedButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, removeSelectedButton.getPreferredSize().height));
        //removeSelectedButton.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30)); // handled by createStyledButton
        removeSelectedButton.addActionListener(e -> removeSelectedWishlistItems());
        buttonPanel.add(removeSelectedButton);

        // Move to cart button
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton moveToCartButton = createStyledButton("MOVE TO CART", ThemeColors.PRIMARY);
        moveToCartButton.setFont(new Font("Arial", Font.BOLD, 16));
        //moveToCartButton.setBackground(ThemeColors.PRIMARY); // handled by createStyledButton
        //moveToCartButton.setForeground(Color.WHITE);
        moveToCartButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        moveToCartButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, moveToCartButton.getPreferredSize().height));
        //moveToCartButton.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30)); // handled by createStyledButton
        moveToCartButton.addActionListener(e -> moveSelectedToCart());
        buttonPanel.add(moveToCartButton);

        wishlistContentPanel.add(buttonPanel, BorderLayout.SOUTH);
        panel.add(wishlistContentPanel, BorderLayout.CENTER);

        return panel;
    }

    // Updated createWishlistItemPanel to accept imagePath
    private JPanel createWishlistItemPanel(int productId, String productName, double price, String description, String imagePathFromDB) {
        JPanel itemPanel = new JPanel(new BorderLayout(10, 10));
        itemPanel.setBackground(ThemeColors.CARD_BG);
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.BACKGROUND),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // Store product info in client properties
        itemPanel.putClientProperty("productId", productId);
        itemPanel.putClientProperty("productName", productName);
        itemPanel.putClientProperty("price", price);
        itemPanel.putClientProperty("description", description);
        itemPanel.putClientProperty("imagePath", imagePathFromDB); // Store image path too

        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout(10, 0));
        contentPanel.setOpaque(false);

        // Left panel (checkbox and image)
        JPanel leftPanel = new JPanel(new BorderLayout(10, 0));
        leftPanel.setOpaque(false);

        // Circular checkbox (centered vertically)
        JCheckBox selectCheckbox = createCircularCheckbox(productId, price, 1); // Qty is 1 for wishlist item representation
        selectCheckbox.setSelected(false); // Default unchecked

        JPanel checkboxPanel = new JPanel(new GridBagLayout());
        checkboxPanel.setOpaque(false);
        checkboxPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));
        checkboxPanel.add(selectCheckbox);
        leftPanel.add(checkboxPanel, BorderLayout.WEST);

        // Image panel with loading logic
        JPanel imagePanel = new JPanel(new GridBagLayout());
        imagePanel.setPreferredSize(new Dimension(80, 80));
        imagePanel.setBackground(ThemeColors.BACKGROUND);
        imagePanel.setBorder(BorderFactory.createLineBorder(ThemeColors.SECONDARY, 1));

        // --- Use robust image loading ---
        ImageIcon icon = loadImageIcon(imagePathFromDB, productName);
        if (icon != null && isIconValid(icon)) {
            Image scaledImage = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
            imagePanel.add(imageLabel);
        } else {
            JLabel noImageLabel = new JLabel("No Image", SwingConstants.CENTER);
            noImageLabel.setFont(new Font("Arial", Font.ITALIC, 10));
            noImageLabel.setForeground(Color.GRAY);
            imagePanel.add(noImageLabel);
        }
        // --- End image loading ---

        leftPanel.add(imagePanel, BorderLayout.CENTER);
        contentPanel.add(leftPanel, BorderLayout.WEST);

        // Center panel (product info)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(productName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(ThemeColors.TEXT);

        String shortDesc = (description != null && description.length() > 50) ? description.substring(0, 50) + "..." : (description != null ? description : "");
        JLabel descLabel = new JLabel(shortDesc);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        descLabel.setForeground(ThemeColors.TEXT);

        JLabel priceLabel = new JLabel(String.format("₱%.2f", price));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        priceLabel.setForeground(ThemeColors.PRIMARY);

        centerPanel.add(nameLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        centerPanel.add(descLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        centerPanel.add(priceLabel);

        contentPanel.add(centerPanel, BorderLayout.CENTER);

        // Right panel - now only contains Details button
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(150, 80));

        // Only Details button remains
        JButton detailsButton = createStyledButton("Details", ThemeColors.SECONDARY);
        detailsButton.setFont(new Font("Arial", Font.BOLD, 12));
        detailsButton.addActionListener(e -> showProductDetails(productId, productName, price, description, imagePathFromDB));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(detailsButton);

        rightPanel.add(buttonPanel);
        contentPanel.add(rightPanel, BorderLayout.EAST);

        itemPanel.add(contentPanel, BorderLayout.CENTER);
        return itemPanel;
    }


    private void removeSelectedWishlistItems() {
        JPanel wishlistItemsPanel = (JPanel) wishlistPanel.getClientProperty("wishlistItemsPanel");
        if (wishlistItemsPanel == null) {
            System.err.println("Error: wishlistItemsPanel not found in client properties.");
            return;
        }


        List<Integer> productIdsToRemove = new ArrayList<>();

        for (Component comp : wishlistItemsPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel itemPanel = (JPanel) comp;
                 // Skip non-item panels like Box Fillers
                if (!(itemPanel.getClientProperty("productId") instanceof Integer)) continue;

                JCheckBox checkbox = findCheckboxInPanel(itemPanel);
                if (checkbox != null && checkbox.isSelected()) {
                    Integer productId = (Integer) itemPanel.getClientProperty("productId");
                    if (productId != null) {
                        productIdsToRemove.add(productId);
                    }
                }
            }
        }

        if (productIdsToRemove.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please select at least one item to remove",
                "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to remove the selected items from your wishlist?",
            "Confirm Removal",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

         if (confirm != JOptionPane.YES_OPTION) {
            return;
         }


        try (Connection conn = DBConnection.connect()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM wishlist WHERE customer_id = ? AND product_id = ?")) {

                for (int productId : productIdsToRemove) {
                    stmt.setInt(1, customerId);
                    stmt.setInt(2, productId);
                    stmt.addBatch();
                }

                int[] results = stmt.executeBatch();
                conn.commit();

                // Check if all deletions were successful (optional but good)
                int totalDeleted = 0;
                for(int result : results) {
                    if (result >= 0) totalDeleted += result; // Statement.SUCCESS_NO_INFO or row count
                }

                loadWishlist(); // Refresh the view
                 // Update badge counter
                updateWishlistBadge();


                JOptionPane.showMessageDialog(this,
                    totalDeleted + " item(s) removed from wishlist.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (SQLException ex) {
                conn.rollback(); // Rollback on error during batch execution
                throw ex; // Re-throw to be caught by outer catch
            }
        } catch (SQLException ex) {
            ex.printStackTrace(); // Log the full stack trace for debugging
            JOptionPane.showMessageDialog(this,
                "Error removing items from wishlist: " + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void moveSelectedToCart() {
        JPanel wishlistItemsPanel = (JPanel) wishlistPanel.getClientProperty("wishlistItemsPanel");
        if (wishlistItemsPanel == null) {
             System.err.println("Error: wishlistItemsPanel not found in client properties.");
            return;
        }

        List<Integer> productIdsToMove = new ArrayList<>();
        boolean anyMoved = false;

        // Collect items to move
        for (Component comp : wishlistItemsPanel.getComponents()) {
            if (comp instanceof JPanel) {
                 JPanel itemPanel = (JPanel) comp;
                 // Skip non-item panels
                 if (!(itemPanel.getClientProperty("productId") instanceof Integer)) continue;

                JCheckBox checkbox = findCheckboxInPanel(itemPanel);
                if (checkbox != null && checkbox.isSelected()) {
                    Integer productId = (Integer) itemPanel.getClientProperty("productId");
                    // We'll add to cart using productId, name, price later inside the loop
                    if (productId != null) {
                        productIdsToMove.add(productId);
                    }
                }
            }
        }


        if (productIdsToMove.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please select at least one item to move",
                "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

         int confirm = JOptionPane.showConfirmDialog(this,
            "Move selected items to cart and remove from wishlist?",
            "Confirm Move",
            JOptionPane.YES_NO_OPTION);

         if (confirm != JOptionPane.YES_OPTION) {
            return;
         }


        Connection conn = null;
        PreparedStatement deleteStmt = null;
        PreparedStatement insertStmt = null;
        PreparedStatement selectStmt = null;
        int itemsMovedCount = 0;
        List<Integer> successfullyMovedIds = new ArrayList<>(); // Keep track of IDs actually moved

        try {
            conn = DBConnection.connect();
            conn.setAutoCommit(false); // Start transaction

            // Prepare statements
            String selectSql = "SELECT name, price FROM products WHERE id = ?";
            String insertSql = "INSERT INTO cart (product_id, product_name, price, quantity, customer_id) " +
                               "VALUES (?, ?, ?, 1, ?) ON DUPLICATE KEY UPDATE quantity = quantity + 1";
            String deleteSql = "DELETE FROM wishlist WHERE customer_id = ? AND product_id = ?";

            selectStmt = conn.prepareStatement(selectSql);
            insertStmt = conn.prepareStatement(insertSql);
            deleteStmt = conn.prepareStatement(deleteSql);

            // Process each selected product ID
            for (int productId : productIdsToMove) {
                // 1. Get product details needed for cart
                selectStmt.setInt(1, productId);
                ResultSet rs = selectStmt.executeQuery();
                String productName = null;
                Double price = null;
                if (rs.next()) {
                    productName = rs.getString("name");
                    price = rs.getDouble("price");
                }
                rs.close(); // Close ResultSet immediately

                if (productName != null && price != null) {
                    // 2. Add/Update item in cart
                    insertStmt.setInt(1, productId);
                    insertStmt.setString(2, productName);
                    insertStmt.setDouble(3, price);
                    insertStmt.setInt(4, customerId);
                    insertStmt.executeUpdate();

                    // 3. Add to deletion batch (only if successfully added/updated in cart)
                    deleteStmt.setInt(1, customerId);
                    deleteStmt.setInt(2, productId);
                    deleteStmt.addBatch();
                    successfullyMovedIds.add(productId); // Track successful move
                    itemsMovedCount++;
                } else {
                    System.err.println("Warning: Could not find product details for ID: " + productId + ". Skipping move for this item.");
                }
                 selectStmt.clearParameters(); // Clear parameters for next iteration
                 insertStmt.clearParameters();
            }

            // 4. Execute batch deletion from wishlist
            if (!successfullyMovedIds.isEmpty()) {
                deleteStmt.executeBatch();
            }

            conn.commit(); // Commit transaction
            anyMoved = itemsMovedCount > 0;

        } catch (SQLException ex) {
            anyMoved = false; // Mark as failed if exception occurs
            if (conn != null) try { conn.rollback(); } catch (SQLException e) { e.printStackTrace(); }
            ex.printStackTrace(); // Log the full stack trace
            JOptionPane.showMessageDialog(this,
                "Error moving items: " + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Close resources in finally block
            try { if (selectStmt != null) selectStmt.close(); } catch (SQLException ignored) {}
            try { if (insertStmt != null) insertStmt.close(); } catch (SQLException ignored) {}
            try { if (deleteStmt != null) deleteStmt.close(); } catch (SQLException ignored) {}
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }

        // Show message and refresh UI if successful
        if (anyMoved) {
             loadWishlist(); // Refresh the wishlist view
             updateWishlistBadge();
             updateCartBadge(); // Update cart badge too
             JOptionPane.showMessageDialog(this,
                 itemsMovedCount + " item(s) moved to cart and removed from wishlist.",
                 "Success", JOptionPane.INFORMATION_MESSAGE);
        } else if (!productIdsToMove.isEmpty()) {
             // Message if selection was made but move failed (error shown in catch)
             System.err.println("Move operation failed, no items were moved.");
        }
        // No message needed if nothing was selected initially
    }


    private JPanel createOrderTrackingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeColors.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Back button
        JButton backButton = new JButton("← Back to Home");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(ThemeColors.CARD_BG);
        backButton.setForeground(ThemeColors.TEXT);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        backButton.addActionListener(e -> showCard("Home"));
        panel.add(backButton, BorderLayout.NORTH);

        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(ThemeColors.BACKGROUND);

        // Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(ThemeColors.BACKGROUND);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Your Orders");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(ThemeColors.PRIMARY);
        titlePanel.add(title);

        contentPanel.add(titlePanel, BorderLayout.NORTH);

        // Create a wrapper panel to prevent stretching
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(ThemeColors.BACKGROUND);

        // Orders items panel - use BoxLayout for vertical arrangement
        JPanel ordersPanel = new JPanel();
        ordersPanel.setLayout(new BoxLayout(ordersPanel, BoxLayout.Y_AXIS));
        ordersPanel.setBackground(ThemeColors.BACKGROUND);
        ordersPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Store reference to orders panel for later access
        panel.putClientProperty("ordersPanel", ordersPanel); // Use consistent key

        // Add the orders panel to a scroll pane
        JScrollPane scrollPane = new JScrollPane(ordersPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ThemeColors.BACKGROUND);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Add the scroll pane to the wrapper panel
        wrapperPanel.add(scrollPane, BorderLayout.CENTER);

        // Add the wrapper panel to the content panel
        contentPanel.add(wrapperPanel, BorderLayout.CENTER);

        /* REMOVED REVIEW BUTTON FROM HERE - Added to individual order items potentially
        JPanel buttonPanel = new JPanel();
        // ... button setup ...
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        */

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

     // Updated createOrderItemPanel to accept imagePath
    private JPanel createOrderItemPanel(int orderId, String productName, int quantity,
                                    String status, String date, double price, String imagePathFromDB) {
        JPanel itemPanel = new JPanel(new BorderLayout(10, 10));
        itemPanel.setBackground(ThemeColors.CARD_BG);
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.BACKGROUND),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        // Adjust preferred height slightly if needed to accommodate buttons better
        itemPanel.setPreferredSize(new Dimension(800, 160));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160)); // Match preferred height

        itemPanel.putClientProperty("orderId", orderId);
        itemPanel.putClientProperty("productId", getProductIdFromName(productName));
        itemPanel.putClientProperty("status", status);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 0));
        contentPanel.setOpaque(false);

        // --- Left Panel (Image) ---
        JPanel leftPanel = new JPanel(new BorderLayout(10, 0));
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(100, 120)); // Keep image size

        JPanel imagePanel = new JPanel(new GridBagLayout());
        imagePanel.setPreferredSize(new Dimension(100, 100)); // Keep image size
        imagePanel.setBackground(ThemeColors.BACKGROUND);
        imagePanel.setBorder(BorderFactory.createLineBorder(ThemeColors.SECONDARY, 1));

        ImageIcon icon = loadImageIcon(imagePathFromDB, productName);
        if (icon != null && isIconValid(icon)) {
            Image scaledImage = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
            imagePanel.add(imageLabel);
        } else {
            JLabel noImageLabel = new JLabel("No Image", SwingConstants.CENTER);
            noImageLabel.setFont(new Font("Arial", Font.ITALIC, 10));
            noImageLabel.setForeground(Color.GRAY);
            imagePanel.add(noImageLabel);
        }
        leftPanel.add(imagePanel, BorderLayout.CENTER);
        contentPanel.add(leftPanel, BorderLayout.WEST);

        // --- Center Panel (Info) ---
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        // Add some padding to align better vertically if needed
        centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JLabel nameLabel = new JLabel(productName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(ThemeColors.TEXT);

        JLabel quantityLabel = new JLabel("Quantity: " + quantity);
        quantityLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        quantityLabel.setForeground(ThemeColors.TEXT);

        JLabel priceLabel = new JLabel(String.format("Price: ₱%.2f", price));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        priceLabel.setForeground(ThemeColors.PRIMARY);

        JLabel statusLabel = new JLabel("Status: " + status);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        switch (status.toLowerCase()) {
            case "delivered": case "completed": statusLabel.setForeground(new Color(0, 150, 0)); break;
            case "processing": statusLabel.setForeground(new Color(200, 150, 0)); break;
            case "shipped": statusLabel.setForeground(new Color(0, 100, 200)); break;
            case "cancelled": statusLabel.setForeground(Color.RED); break;
            default: statusLabel.setForeground(ThemeColors.TEXT);
        }

        JLabel dateLabel = new JLabel("Order Date: " + date);
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        dateLabel.setForeground(ThemeColors.TEXT);

        centerPanel.add(nameLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        centerPanel.add(quantityLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        centerPanel.add(priceLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        centerPanel.add(statusLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        centerPanel.add(dateLabel);

        contentPanel.add(centerPanel, BorderLayout.CENTER);

        // --- Right Panel for Buttons (View Details, Review, Cancel) ---
        JPanel rightButtonPanel = new JPanel();
        rightButtonPanel.setLayout(new BoxLayout(rightButtonPanel, BoxLayout.Y_AXIS));
        rightButtonPanel.setOpaque(false);
        rightButtonPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 5)); // Add padding around buttons

        // --- Button Creation (BEFORE size calculation) ---
        JButton detailsButton = createStyledButton("View Details", ThemeColors.SECONDARY);
        detailsButton.addActionListener(e -> showOrderDetails(orderId, productName, quantity, status, date, price));

        JButton reviewButton = createStyledButton("Leave Review", ThemeColors.PRIMARY);
        reviewButton.setEnabled(status.equalsIgnoreCase("Delivered") || status.equalsIgnoreCase("Completed"));
        reviewButton.addActionListener(e -> {
            Integer pId = (Integer) itemPanel.getClientProperty("productId");
            if (pId != null) {
                new ProductReviewFrame(customerId, pId, orderId);
            } else {
                JOptionPane.showMessageDialog(this, "Could not determine product ID for review.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelButton = createStyledButton("Request Cancel", new Color(200, 0, 0));
        cancelButton.setEnabled(status.equalsIgnoreCase("Processing"));
        cancelButton.addActionListener(e -> requestOrderCancellation(orderId));
        // --- End Button Creation ---


        // --- START: Button Size Fix ---
        Dimension detailsPref = detailsButton.getPreferredSize();
        Dimension reviewPref = reviewButton.getPreferredSize();
        Dimension cancelPref = cancelButton.getPreferredSize();

        int maxWidth = Math.max(detailsPref.width, Math.max(reviewPref.width, cancelPref.width));
        // Use a consistent height, e.g., the height of the details button
        Dimension uniformSize = new Dimension(maxWidth, detailsPref.height);

        detailsButton.setPreferredSize(uniformSize);
        detailsButton.setMaximumSize(uniformSize); // Important for BoxLayout
        reviewButton.setPreferredSize(uniformSize);
        reviewButton.setMaximumSize(uniformSize);
        cancelButton.setPreferredSize(uniformSize);
        cancelButton.setMaximumSize(uniformSize);
        // --- END: Button Size Fix ---

        // Center buttons horizontally within the BoxLayout panel
        detailsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        reviewButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add buttons with spacing
        rightButtonPanel.add(Box.createVerticalGlue()); // Push content to center
        rightButtonPanel.add(detailsButton);
        rightButtonPanel.add(Box.createRigidArea(new Dimension(0, 8))); // Increased spacing
        rightButtonPanel.add(reviewButton);
        rightButtonPanel.add(Box.createRigidArea(new Dimension(0, 8))); // Increased spacing
        rightButtonPanel.add(cancelButton);
        rightButtonPanel.add(Box.createVerticalGlue()); // Push content to center


        contentPanel.add(rightButtonPanel, BorderLayout.EAST);


        itemPanel.add(contentPanel, BorderLayout.CENTER);
        return itemPanel;
    }


   // Helper to get product ID from name (use cautiously, assumes unique names for now)
   private int getProductIdFromName(String productName) {
       String sql = "SELECT id FROM products WHERE name = ? LIMIT 1";
       try (Connection conn = DBConnection.connect();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
           stmt.setString(1, productName);
           ResultSet rs = stmt.executeQuery();
           if (rs.next()) {
               return rs.getInt("id");
           }
       } catch (SQLException e) {
           System.err.println("Error fetching product ID by name: " + e.getMessage());
       }
       return -1; // Indicate error or not found
   }

   // Method to handle cancellation request
    private void requestOrderCancellation(int orderId) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to request cancellation for Order #" + orderId + "?\n" +
                "This request needs admin approval.",
                "Confirm Cancellation Request",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "UPDATE orders SET cancellation_requested = 1 WHERE id = ? AND status = 'Processing'";
            try (Connection conn = DBConnection.connect();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, orderId);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this,
                            "Cancellation requested for Order #" + orderId + ". Please wait for admin approval.",
                            "Request Submitted", JOptionPane.INFORMATION_MESSAGE);
                    loadOrders(); // Refresh the orders view to show the request
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Could not request cancellation. The order might no longer be in 'Processing' status.",
                            "Request Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error submitting cancellation request: " + ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


     // Updated showOrderDetails to accept only orderId and fetch details inside
    private void showOrderDetails(int orderId, String productName, int quantity, String status, String date, double price) {
        // This method now has enough basic info, but a more robust version
        // might requery the DB for full details based on orderId.
        // For simplicity, we'll use the passed info.

        JDialog detailsDialog = new JDialog(this, "Order Details", true);
        detailsDialog.setSize(400, 450); // Adjusted size
        detailsDialog.setLayout(new BorderLayout());
        detailsDialog.getContentPane().setBackground(ThemeColors.BACKGROUND);

        JPanel detailsPanel = new JPanel(new BorderLayout(10, 10));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        detailsPanel.setBackground(ThemeColors.BACKGROUND);

        // Title
        JLabel titleLabel = new JLabel("Order #" + orderId, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(ThemeColors.PRIMARY);
        detailsPanel.add(titleLabel, BorderLayout.NORTH);

        // Main content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // Product image
        JPanel imagePanel = new JPanel(new GridBagLayout());
        imagePanel.setPreferredSize(new Dimension(150, 150));
        imagePanel.setBackground(ThemeColors.BACKGROUND);
        imagePanel.setBorder(BorderFactory.createLineBorder(ThemeColors.SECONDARY, 1));

        // --- Fetch imagePath for the specific product ---
        String imagePath = getProductImagePath(getProductIdFromName(productName)); // Helper needed or pass it
        ImageIcon icon = loadImageIcon(imagePath, productName);
        // --- End fetch ---

        if (icon != null && isIconValid(icon)) {
            Image scaledImage = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
            imagePanel.add(imageLabel);
        } else {
            JLabel noImageLabel = new JLabel("No Image", SwingConstants.CENTER);
            noImageLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            noImageLabel.setForeground(Color.GRAY);
            imagePanel.add(noImageLabel);
        }


        contentPanel.add(imagePanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Product info
        JLabel nameLabel = new JLabel(productName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(nameLabel);

        JLabel quantityLabel = new JLabel("Quantity: " + quantity);
        quantityLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        quantityLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(quantityLabel);

        JLabel priceLabel = new JLabel(String.format("Price: ₱%.2f", price));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(priceLabel);

        JLabel totalLabel = new JLabel(String.format("Total: ₱%.2f", price * quantity));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(totalLabel);

        JLabel statusLabel = new JLabel("Status: " + status);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        // Color code based on status (same as createOrderItemPanel)
         switch (status.toLowerCase()) {
            case "delivered": case "completed": statusLabel.setForeground(new Color(0, 150, 0)); break;
            case "processing": statusLabel.setForeground(new Color(200, 150, 0)); break;
            case "shipped": statusLabel.setForeground(new Color(0, 100, 200)); break;
            case "cancelled": statusLabel.setForeground(Color.RED); break;
            default: statusLabel.setForeground(ThemeColors.TEXT);
        }
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(statusLabel);

        JLabel dateLabel = new JLabel("Order Date: " + date);
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(dateLabel);

        detailsPanel.add(contentPanel, BorderLayout.CENTER);

        // Close button
        JButton closeButton = createStyledButton("Close", ThemeColors.CARD_BG);
        closeButton.setForeground(ThemeColors.TEXT);
        closeButton.addActionListener(e -> detailsDialog.dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(closeButton);

        detailsPanel.add(buttonPanel, BorderLayout.SOUTH);
        detailsDialog.add(detailsPanel, BorderLayout.CENTER);
        detailsDialog.setLocationRelativeTo(this);
        detailsDialog.setVisible(true);
    }

     // Helper to get image path for a specific product ID
    private String getProductImagePath(int productId) {
        String sql = "SELECT image_path FROM products WHERE id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("image_path");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching image path for product " + productId + ": " + e.getMessage());
        }
        return null; // Return null if not found or error
    }


    private JPanel createEventPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeColors.BACKGROUND);

        JButton backButton = new JButton("← Back to Home");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(ThemeColors.CARD_BG);
        backButton.setForeground(ThemeColors.TEXT);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        backButton.addActionListener(e -> showCard("Home"));
        panel.add(backButton, BorderLayout.NORTH);

        JTextArea eventContent = new JTextArea();
        eventContent.setText("UPCOMING EVENTS:\n\n1. Summer Sale - 20% off all items (June 1-30)\n" +
                           "2. New Album Pre-orders (Starting May 15)\n" +
                           "3. Fan Meet & Greet Events (July 10-12)\n" +
                           "4. Exclusive Merch Drop (May 20)");
        eventContent.setEditable(false);
        eventContent.setFont(new Font("Arial", Font.PLAIN, 16));
        eventContent.setBackground(ThemeColors.BACKGROUND);
        eventContent.setForeground(ThemeColors.TEXT);
        eventContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JScrollPane(eventContent), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFAQPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeColors.BACKGROUND);

        JButton backButton = new JButton("← Back to Home");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(ThemeColors.CARD_BG);
        backButton.setForeground(ThemeColors.TEXT);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        backButton.addActionListener(e -> showCard("Home"));
        panel.add(backButton, BorderLayout.NORTH);

        JTextArea faqContent = new JTextArea();
        faqContent.setText("FREQUENTLY ASKED QUESTIONS:\n\n" +
                          "Q: How long does shipping take?\nA: 3-5 business days domestically, 7-14 internationally\n\n" +
                          "Q: What payment methods do you accept?\nA: Credit cards, PayPal, GCash, and Maya\n\n" +
                          "Q: Can I cancel my order?\nA: Yes, if it hasn't shipped yet. Contact customer service\n\n" +
                          "Q: Do you offer refunds?\nA: Yes, within 30 days for defective products");
        faqContent.setEditable(false);
        faqContent.setFont(new Font("Arial", Font.PLAIN, 16));
        faqContent.setBackground(ThemeColors.BACKGROUND);
        faqContent.setForeground(ThemeColors.TEXT);
        faqContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JScrollPane(faqContent), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createNoticePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeColors.BACKGROUND);

        JButton backButton = new JButton("← Back to Home");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(ThemeColors.CARD_BG);
        backButton.setForeground(ThemeColors.TEXT);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        backButton.addActionListener(e -> showCard("Home"));
        panel.add(backButton, BorderLayout.NORTH);

        JTextArea noticeContent = new JTextArea();
        noticeContent.setText("IMPORTANT NOTICES:\n\n" +
                            "1. System Maintenance: May 15, 2-4 AM (GMT+8)\n" +
                            "2. Shipping delays expected during holiday seasons\n" +
                            "3. New security measures implemented for payments\n" +
                            "4. Loyalty program updates coming June 1");
        noticeContent.setEditable(false);
        noticeContent.setFont(new Font("Arial", Font.PLAIN, 16));
        noticeContent.setBackground(ThemeColors.BACKGROUND);
        noticeContent.setForeground(ThemeColors.TEXT);
        noticeContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JScrollPane(noticeContent), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAboutPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeColors.BACKGROUND);

        JButton backButton = new JButton("← Back to Home");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(ThemeColors.CARD_BG);
        backButton.setForeground(ThemeColors.TEXT);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        backButton.addActionListener(e -> showCard("Home"));
        panel.add(backButton, BorderLayout.NORTH);

        JTextArea aboutContent = new JTextArea();
        aboutContent.setText("ABOUT HAMTEO:\n\n" +
                           "Founded in 2020, HAMTEO is the premier destination for K-Pop merchandise.\n\n" +
                           "Our mission is to connect fans worldwide with authentic, high-quality merchandise\n" +
                           "from their favorite artists while providing exceptional customer service.\n\n" +
                           "Contact us: support@hamteo.com\n" +
                           "Phone: +1 (800) 555-KPOP");
        aboutContent.setEditable(false);
        aboutContent.setFont(new Font("Arial", Font.PLAIN, 16));
        aboutContent.setBackground(ThemeColors.BACKGROUND);
        aboutContent.setForeground(ThemeColors.TEXT);
        aboutContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JScrollPane(aboutContent), BorderLayout.CENTER);

        return panel;
    }

    private void styleTable(JTable table) {
        table.setBackground(ThemeColors.CARD_BG);
        table.setForeground(ThemeColors.TEXT);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setGridColor(ThemeColors.BACKGROUND);
        table.setSelectionBackground(ThemeColors.PRIMARY);
        table.setSelectionForeground(Color.WHITE);
        table.setRowHeight(30);
         // Header styling (moved from AdminFrame, good to have here too)
        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.setFont(new Font("Arial", Font.BOLD, 14));
            header.setBackground(ThemeColors.PRIMARY);
            header.setForeground(Color.WHITE);
            header.setReorderingAllowed(false);
            header.setPreferredSize(new Dimension(header.getWidth(), 40)); // Taller header
             // Add padding to header cells (simplified version)
             TableCellRenderer headerRenderer = header.getDefaultRenderer();
            if (headerRenderer instanceof JLabel) {
                ((JLabel) headerRenderer).setHorizontalAlignment(SwingConstants.LEFT);
                ((JLabel) headerRenderer).setBorder(BorderFactory.createCompoundBorder(
                    ((JLabel) headerRenderer).getBorder(),
                    BorderFactory.createEmptyBorder(0, 10, 0, 10))
                );
            }
        }
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.addMouseListener(new MouseAdapter() {
             Color originalBg = bgColor; // Store original color
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

    private void addToCart(int productId, String name, double price) {
        String sql = "INSERT INTO cart (product_id, product_name, price, quantity, customer_id) " +
                     "VALUES (?, ?, ?, 1, ?) " +
                     "ON DUPLICATE KEY UPDATE quantity = quantity + 1";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            stmt.setString(2, name);
            stmt.setDouble(3, price);
            stmt.setInt(4, customerId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this,
                    "Product added to cart!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                // Update cart badge counter
                updateCartBadge();
                // If the cart panel is currently visible, refresh it
                if (currentCard.equals("Cart")) {
                    loadCartItems();
                }
            }
        } catch (SQLException ex) {
            System.err.println("Cart SQL Error: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                "Error updating cart: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addToWishlist(int productId, String name, double price) {
        String sql = "INSERT INTO wishlist (customer_id, product_id) VALUES (?, ?)";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            stmt.setInt(2, productId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Product added to wishlist!");
                // Update wishlist badge counter
                updateWishlistBadge();
                // If wishlist panel is visible, refresh it
                if (currentCard.equals("Wishlist")) {
                    loadWishlist();
                }
            }
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1062) { // Duplicate entry code for MySQL
                JOptionPane.showMessageDialog(this,
                    "Product is already in your wishlist!",
                    "Notice", JOptionPane.INFORMATION_MESSAGE);
            } else {
                System.err.println("Wishlist SQL Error: " + ex.getMessage());
                JOptionPane.showMessageDialog(this,
                    "Error updating wishlist: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void removeFromWishlist(int productId) {
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                 "DELETE FROM wishlist WHERE customer_id = ? AND product_id = ?")) {

            stmt.setInt(1, customerId);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
             // Update wishlist badge counter after removal
            updateWishlistBadge();

        } catch (SQLException ex) {
            System.err.println("Error removing from wishlist: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                "Error removing item from wishlist",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    // Updated loadCartItems to include imagePath
    private void loadCartItems() {
        cartItemsPanel.removeAll();
        // cartItemsPanel.setLayout(new BoxLayout(cartItemsPanel, BoxLayout.Y_AXIS)); // Keep this layout
        double totalAmount = 0.0;

        String sql = "SELECT c.id, p.name, p.price, c.quantity, p.color, p.size, p.image_path " + // Added image_path
                     "FROM cart c JOIN products p ON c.product_id = p.id " +
                     "WHERE c.customer_id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            boolean hasItems = false;
            while (rs.next()) {
                hasItems = true;
                int cartId = rs.getInt("id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                int quantity = rs.getInt("quantity");
                String color = rs.getString("color");
                String size = rs.getString("size");
                String imagePath = rs.getString("image_path"); // Get the image path

                JPanel itemPanel = createCartItemPanel(
                    cartId,
                    name,
                    price,
                    quantity,
                    color,
                    size,
                    imagePath // Pass the image path
                );
                itemPanel.putClientProperty("cartId", cartId); // Still useful

                cartItemsPanel.add(itemPanel);
                cartItemsPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Spacing

                // Calculate total based on INITIAL load, selection updates it later
                // totalAmount += price * quantity; // Total is calculated by selection now
            }

            if (!hasItems) {
                 cartItemsPanel.setLayout(new BorderLayout()); // Change layout to center message
                 JLabel emptyLabel = new JLabel("Your cart is empty.", SwingConstants.CENTER);
                 emptyLabel.setFont(new Font("Arial", Font.ITALIC, 16));
                 emptyLabel.setForeground(Color.GRAY);
                 cartItemsPanel.add(emptyLabel, BorderLayout.CENTER);
            } else {
                 cartItemsPanel.setLayout(new BoxLayout(cartItemsPanel, BoxLayout.Y_AXIS)); // Reset layout if items exist
            }


            // cartTotalLabel.setText(String.format("₱%.2f", totalAmount)); // Initial total is 0 until items selected
            calculateSelectedTotal(); // Calculate total based on initially selected items (which is none now)
            updateCartBadge(); // Update the badge count

        } catch (SQLException ex) {
            System.err.println("Error loading cart: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                "Error loading cart items",
                "Error", JOptionPane.ERROR_MESSAGE);
        }

        cartItemsPanel.revalidate();
        cartItemsPanel.repaint();
    }


    // Updated loadWishlist to include imagePath
    private void loadWishlist() {
        JPanel wishlistItemsPanel = (JPanel) wishlistPanel.getClientProperty("wishlistItemsPanel");
        if (wishlistItemsPanel == null) return;

        wishlistItemsPanel.removeAll();
        wishlistItemsPanel.setLayout(new BoxLayout(wishlistItemsPanel, BoxLayout.Y_AXIS)); // Ensure BoxLayout

        String sql = "SELECT p.id, p.name, p.price, p.description, p.image_path " + // Added image_path
                     "FROM wishlist w JOIN products p ON w.product_id = p.id " +
                     "WHERE w.customer_id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

             boolean hasItems = false;
            while (rs.next()) {
                hasItems = true;
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                String description = rs.getString("description");
                String imagePath = rs.getString("image_path"); // Get image path

                JPanel itemPanel = createWishlistItemPanel(id, name, price, description, imagePath); // Pass image path
                wishlistItemsPanel.add(itemPanel);
                wishlistItemsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }

             if (!hasItems) {
                 wishlistItemsPanel.setLayout(new BorderLayout()); // Change layout to center message
                 JLabel emptyLabel = new JLabel("Your wishlist is empty.", SwingConstants.CENTER);
                 emptyLabel.setFont(new Font("Arial", Font.ITALIC, 16));
                 emptyLabel.setForeground(Color.GRAY);
                 wishlistItemsPanel.add(emptyLabel, BorderLayout.CENTER);
            }

             updateWishlistBadge(); // Update badge count

        } catch (SQLException ex) {
            System.err.println("Error loading wishlist: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                "Error loading wishlist items",
                "Error", JOptionPane.ERROR_MESSAGE);
        }

        wishlistItemsPanel.revalidate();
        wishlistItemsPanel.repaint();
    }


    // Updated loadOrders to include imagePath
    private void loadOrders() {
        JPanel ordersPanel = (JPanel) orderTrackingPanel.getClientProperty("ordersPanel");
        if (ordersPanel == null) return;

        ordersPanel.removeAll();
        ordersPanel.setLayout(new BoxLayout(ordersPanel, BoxLayout.Y_AXIS)); // Ensure BoxLayout

        String sql = "SELECT o.id, p.name, oi.quantity, o.status, o.order_date, p.price, p.image_path " + // Added image_path
                     "FROM orders o " +
                     "JOIN order_items oi ON o.id = oi.order_id " +
                     "JOIN products p ON oi.product_id = p.id " +
                     "WHERE o.customer_id = ? ORDER BY o.order_date DESC";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

             boolean hasOrders = false;
            while (rs.next()) {
                 hasOrders = true;
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int quantity = rs.getInt("quantity");
                String status = rs.getString("status");
                // Format the date/time nicely
                 String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(rs.getTimestamp("order_date"));
                //String date = rs.getTimestamp("order_date").toString();
                double price = rs.getDouble("price");
                String imagePath = rs.getString("image_path"); // Get image path

                JPanel itemPanel = createOrderItemPanel(id, name, quantity, status, dateStr, price, imagePath); // Pass image path
                ordersPanel.add(itemPanel);
                ordersPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Spacing
            }

             if (!hasOrders) {
                 ordersPanel.setLayout(new BorderLayout()); // Change layout for centering message
                 JLabel emptyLabel = new JLabel("You have no orders yet.", SwingConstants.CENTER);
                 emptyLabel.setFont(new Font("Arial", Font.ITALIC, 16));
                 emptyLabel.setForeground(Color.GRAY);
                 ordersPanel.add(emptyLabel, BorderLayout.CENTER);
            }
             updateOrderBadge(); // Update badge count

        } catch (SQLException ex) {
            System.err.println("Error loading orders: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                "Error loading orders",
                "Error", JOptionPane.ERROR_MESSAGE);
        }

        ordersPanel.revalidate();
        ordersPanel.repaint();
    }

    private void checkout() {
        List<Integer> selectedCartIds = new ArrayList<>();
        double totalAmount = 0.0;

        // Iterate through all components in the cartItemsPanel
        for (Component comp : cartItemsPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel itemPanel = (JPanel) comp;
                 // Skip non-item panels
                if (!(itemPanel.getClientProperty("cartId") instanceof Integer)) continue;

                Integer cartId = (Integer) itemPanel.getClientProperty("cartId");
                JCheckBox checkbox = findCheckboxInPanel(itemPanel);

                if (checkbox != null && checkbox.isSelected() && cartId != null) {
                    selectedCartIds.add(cartId);
                    double price = (double) checkbox.getClientProperty("price");
                    int quantity = (int) checkbox.getClientProperty("quantity");
                    totalAmount += price * quantity;
                }
            }
        }

        if (selectedCartIds.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please select at least one item to checkout",
                "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Show address selection dialog
        AddressManager.Address selectedAddress = addressManager.showAddressSelection();
        if (selectedAddress == null) {
            JOptionPane.showMessageDialog(this,
                "Please select or add a delivery address",
                "Address Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Proceed with payment using the selected address
        new PaymentFrame(customerId, selectedCartIds, selectedAddress);
        dispose(); // Close the customer frame after proceeding to payment
    }

    private JCheckBox findCheckboxInPanel(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JCheckBox) {
                return (JCheckBox) comp;
            } else if (comp instanceof Container) {
                JCheckBox found = findCheckboxInPanel((Container) comp);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to log out?",
            "Logout",
            JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            // Show login frame on the Event Dispatch Thread
             SwingUtilities.invokeLater(LoginFrame::new);
            dispose(); // Close the current customer frame
        }
    }

    private void showCard(String cardName) {
        cardLayout.show(mainPanel, cardName);
        currentCard = cardName;
    }

    // --- Helper methods for robust image loading ---
    public static ImageIcon loadImageIcon(String dbPath, String productName) {
        ImageIcon icon = null;
        String loadedFrom = dbPath; // For debugging

        // --- Attempt 1 & 2: Use Database Path ---
        if (dbPath != null && !dbPath.isEmpty()) {
            // Try filesystem (relative to execution path)
            File dbFile = new File(dbPath);
            if (dbFile.exists() && dbFile.isFile()) {
                try {
                    icon = new ImageIcon(dbFile.toURI().toURL());
                    if (!isIconValid(icon)) icon = null;
                } catch (Exception e) { icon = null; }
            }
            // Try classpath
            if (icon == null) {
                String resourcePathDB = dbPath.replace("\\", "/");
                if (!resourcePathDB.startsWith("/")) resourcePathDB = "/" + resourcePathDB;
                URL urlDB = CustomerFrame.class.getResource(resourcePathDB);
                if (urlDB != null) {
                     icon = new ImageIcon(urlDB);
                     if (!isIconValid(icon)) icon = null;
                }
            }
        }

        // --- Attempt 3: Derive from Name ---
        if (icon == null && productName != null && !productName.isEmpty()) {
            String derivedFilename = productName.replaceAll("[^a-zA-Z0-9.\\-_ ]", "_").toLowerCase() + ".png";
            loadedFrom = "[Derived] " + derivedFilename;
            String[] relativeBaseDirs = {"images/products/", "src/images/products/"}; // Add src/ path for IDE running
            String[] classpathBaseDirs = {"/images/products/", "/"};

            // Try derived name in filesystem
            for (String baseDir : relativeBaseDirs) {
                 File derivedFile = new File(baseDir + derivedFilename);
                 if (derivedFile.exists() && derivedFile.isFile()) {
                     try {
                         icon = new ImageIcon(derivedFile.toURI().toURL());
                         if (isIconValid(icon)) {
                             loadedFrom = derivedFile.getPath();
                             break; // Found it
                         } else {
                             icon = null;
                         }
                     } catch (Exception e) { icon = null; }
                 }
            }
            // Try derived name in classpath
            if (icon == null) {
                 for (String baseDir : classpathBaseDirs) {
                    String resourcePathDerived = baseDir + derivedFilename;
                    if (!resourcePathDerived.startsWith("/")) resourcePathDerived = "/" + resourcePathDerived;
                    URL urlDerived = CustomerFrame.class.getResource(resourcePathDerived);
                    if (urlDerived != null) {
                         icon = new ImageIcon(urlDerived);
                         if (isIconValid(icon)) {
                             loadedFrom = resourcePathDerived;
                             break; // Found it
                         } else {
                             icon = null;
                         }
                    }
                }
            }
        }

        // --- Attempt 4: Default Image ---
        if (icon == null) {
            loadedFrom = "[Default Image]";
            try {
                URL defaultUrl = CustomerFrame.class.getResource("/images/default_product.png");
                 if (defaultUrl != null) {
                    icon = new ImageIcon(defaultUrl);
                    if (!isIconValid(icon)) icon = new ImageIcon(); // Use empty if default invalid
                 } else {
                      System.err.println("ERROR: Default image '/images/default_product.png' not found in classpath!");
                      icon = new ImageIcon(); // Empty icon
                 }
            } catch (Exception e) {
                System.err.println("Error loading default image: " + e.getMessage());
                icon = new ImageIcon(); // Empty icon
            }
        }

        // System.out.println("Loaded icon for '" + productName + "' from: " + loadedFrom); // Debug
        return (icon != null) ? icon : new ImageIcon(); // Return empty icon if truly nothing worked
    }

    // Helper method to check if an ImageIcon loaded successfully
    private static boolean isIconValid(ImageIcon icon) {
         if (icon == null) return false;
         return icon.getImageLoadStatus() == MediaTracker.COMPLETE && icon.getIconWidth() > 0 && icon.getIconHeight() > 0;
    }

     // --- Methods to update badge counts ---
    private void updateCartBadge() {
        int count = getCartItemCount();
        cartButton.setText("CART"); // Reset text first
        // Re-create the button to force repaint with new count - simple approach
        JPanel parent = (JPanel) cartButton.getParent();
        int index = getComponentIndex(parent, cartButton);
        parent.remove(cartButton);
        cartButton = createBadgeButtonWithCounter("CART", count);
        cartButton.addActionListener(e -> { loadCartItems(); showCard("Cart"); }); // Re-add listener
        parent.add(cartButton, index);
        parent.revalidate();
        parent.repaint();
    }

    private void updateWishlistBadge() {
        int count = getWishlistItemCount();
        wishlistButton.setText("WISHLIST");
        JPanel parent = (JPanel) wishlistButton.getParent();
         int index = getComponentIndex(parent, wishlistButton);
        parent.remove(wishlistButton);
        wishlistButton = createBadgeButtonWithCounter("WISHLIST", count);
        wishlistButton.addActionListener(e -> { loadWishlist(); showCard("Wishlist"); });
        parent.add(wishlistButton, index);
        parent.revalidate();
        parent.repaint();
    }

     private void updateOrderBadge() {
        int count = getOrderCount();
        ordersButton.setText("ORDERS");
        JPanel parent = (JPanel) ordersButton.getParent();
         int index = getComponentIndex(parent, ordersButton);
        parent.remove(ordersButton);
        ordersButton = createBadgeButtonWithCounter("ORDERS", count);
         ordersButton.addActionListener(e -> { loadOrders(); showCard("Orders"); });
        parent.add(ordersButton, index);
        parent.revalidate();
        parent.repaint();
    }

     // Helper to find component index
     private int getComponentIndex(Container container, Component component) {
         for (int i = 0; i < container.getComponentCount(); i++) {
             if (container.getComponent(i) == component) {
                 return i;
             }
         }
         return -1;
     }

    // --- Main Method ---
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatDarkLaf");
            ex.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            // Example: Replace '1' with the actual logged-in customer ID
            new CustomerFrame(1);
        });
    }
}

// --- WrapLayout Class (Keep as is) ---
class WrapLayout extends FlowLayout {
    // ... (WrapLayout code remains exactly the same as provided before) ...
     public WrapLayout() {
        super();
    }

    public WrapLayout(int align) {
        super(align);
    }

    public WrapLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        synchronized (target.getTreeLock()) {
            // --- FIX START ---
            // Get the ancestor Window, not Dimension
            Window Tparent = SwingUtilities.getWindowAncestor(target);
            // Get width from the Window if available, otherwise from the target container
            int targetWidth = (Tparent != null) ? Tparent.getWidth() : target.getWidth();

            // If targetWidth is still 0 (e.g., container not yet added to a visible window),
            // use a large value for preferred size calculation to avoid issues.
            if (targetWidth == 0) {
                targetWidth = Integer.MAX_VALUE;
            }
            // --- FIX END ---


            int hgap = getHgap();
            int vgap = getVgap();
            Insets insets = target.getInsets();
            // Use parent's width minus insets for max width calculation
            // Ensure maxWidth is not negative if insets/gaps are large
            int maxWidth = Math.max(1, targetWidth - (insets.left + insets.right + hgap * 2));


            Dimension dim = new Dimension(0, 0);
            int rowWidth = 0;
            int rowHeight = 0;

            int nmembers = target.getComponentCount();

            for (int i = 0; i < nmembers; i++) {
                Component m = target.getComponent(i);
                if (m.isVisible()) {
                    Dimension d = m.getPreferredSize();
                    // Check if adding this component exceeds max width
                    // If it's the first component in the row, always add it
                     if (rowWidth > 0 && (rowWidth + hgap + d.width) > maxWidth) {
                        // Start a new row
                        dim.width = Math.max(dim.width, rowWidth); // Update max width seen
                        dim.height += rowHeight + vgap;           // Add height of completed row
                        rowWidth = 0;                             // Reset row width
                        rowHeight = 0;                            // Reset row height
                    }
                    // Add component to current row
                    if (rowWidth > 0) {
                        rowWidth += hgap;
                    }
                    rowWidth += d.width;
                    rowHeight = Math.max(rowHeight, d.height);
                }
            }
            // Add the last row's dimensions
            dim.width = Math.max(dim.width, rowWidth);
            dim.height += rowHeight;

             // Add insets and gaps
            dim.width += insets.left + insets.right + hgap*2;
            dim.height += insets.top + insets.bottom + vgap*2;

            return dim;
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
         Dimension minimum = preferredLayoutSize(target); // Often the same as preferred for FlowLayout
         minimum.width -= (getHgap() + 1); // Adjust slightly if needed
         return minimum;
    }


    @Override
    public void layoutContainer(Container target) {
         synchronized (target.getTreeLock()) {
             Insets insets = target.getInsets();
             int targetWidth = target.getWidth();
             int maxWidth = targetWidth - (insets.left + insets.right + getHgap() * 2);

             int nmembers = target.getComponentCount();
             int x = insets.left + getHgap();
             int y = insets.top + getVgap();
             int rowh = 0;
             int start = 0;

             boolean ltr = target.getComponentOrientation().isLeftToRight();

             for (int i = 0; i < nmembers; i++) {
                 Component m = target.getComponent(i);
                 if (m.isVisible()) {
                     Dimension d = m.getPreferredSize();
                     m.setSize(d.width, d.height);

                     if ((x == (ltr ? insets.left + getHgap() : targetWidth - insets.right - getHgap())) || ((x + d.width) <= maxWidth)) {
                         if (x > (ltr ? insets.left + getHgap() : targetWidth - insets.right - getHgap())) {
                             x += getHgap();
                         }
                          // Place component
                          if (!ltr) {
                             m.setLocation(targetWidth - insets.right - x - d.width + (insets.left + getHgap()), y); // Adjust for RTL
                          } else {
                            m.setLocation(x, y);
                          }

                         x += d.width;
                         rowh = Math.max(rowh, d.height);
                     } else {
                         // New row
                         moveComponents(target, insets.left + getHgap(), y, maxWidth - x, rowh, start, i, ltr);
                         x = insets.left + getHgap();
                         y += getVgap() + rowh;
                         rowh = d.height;
                         start = i;
                         // Place the component that started the new row
                          if (!ltr) {
                             m.setLocation(targetWidth - insets.right - x - d.width + (insets.left + getHgap()), y); // Adjust for RTL
                          } else {
                            m.setLocation(x, y);
                          }
                         x += d.width;
                     }
                 }
             }
             moveComponents(target, insets.left + getHgap(), y, maxWidth - x, rowh, start, nmembers, ltr);
         }
    }


    // Helper method from FlowLayout to align components in a row
     private void moveComponents(Container target, int x, int y, int width, int height,
                                int rowStart, int rowEnd, boolean ltr) {
        switch (getAlignment()) {
        case FlowLayout.LEFT:
            x += ltr ? 0 : width;
            break;
        case FlowLayout.CENTER:
            x += width / 2;
            break;
        case FlowLayout.RIGHT:
            x += ltr ? width : 0;
            break;
        case FlowLayout.LEADING:
            break;
        case FlowLayout.TRAILING:
            x += width;
            break;
        }
        for (int i = rowStart ; i < rowEnd ; i++) {
            Component m = target.getComponent(i);
            if (m.isVisible()) {
                if (ltr) {
                    m.setLocation(x, y + (height - m.getHeight()) / 2);
                } else {
                    m.setLocation(target.getWidth() - x - m.getWidth(), y + (height - m.getHeight()) / 2);
                }
                x += m.getWidth() + getHgap();
            }
        }
    }
}