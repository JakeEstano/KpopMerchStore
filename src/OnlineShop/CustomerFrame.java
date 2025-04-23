package OnlineShop;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.util.Collections;
import java.util.Enumeration;

public class CustomerFrame extends JFrame {
    private int customerId;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private String currentCategory;
    private String currentCard;
    
    // Navigation buttons
    private JButton shopButton, eventButton, faqButton, noticeButton, aboutButton;
    
    // Cart components
    private JPanel cartPanel;
    
    // Wishlist components
    private JPanel wishlistPanel;
    
    // Order tracking
    private DefaultTableModel orderTableModel;
    private JTable orderTable;
    
    // Product components
    private JPanel productsPanel;
    private JTextField searchField;
    
    private JPanel cartItemsPanel;
    private JLabel cartTotalLabel;
    
    private JPanel homeProductGridPanel;
    private JPanel productsProductGridPanel;
    
    private JPanel orderTrackingPanel;

    public CustomerFrame(int customerId) {
        this.customerId = customerId;
        this.currentCard = "Home";
        setTitle("케이팝 상점 - K-Pop Merch Store");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = env.getDefaultScreenDevice();
        
        if (device.isFullScreenSupported()) {
            setUndecorated(true);
            device.setFullScreenWindow(this);
        } else {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }

        // Initialize main components first
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(ThemeColors.BACKGROUND);

        // Initialize productGridPanel ONLY ONCE here
        productsProductGridPanel = new JPanel(new GridLayout(0, 4, 20, 20));
        productsProductGridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        productsProductGridPanel.setBackground(ThemeColors.BACKGROUND);

        // Create all panels
        JPanel homePanel = createHomePanel();
        this.productsPanel = createProductsPanel();
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
            loadAllProducts();
        });
        
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createNavigationBar() {
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(ThemeColors.BACKGROUND);
        navBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Logo
        JLabel logo = new JLabel("케이팝 상점 ", SwingConstants.LEFT);
        logo.setFont(new Font("Malgun Gothic", Font.BOLD, 24));
        logo.setForeground(ThemeColors.PRIMARY);
        navBar.add(logo, BorderLayout.WEST);

        // Navigation buttons
        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        navButtons.setOpaque(false);

        shopButton = createNavButton("SHOP");
        shopButton.addActionListener(e -> showCard("Home"));
        
        eventButton = createNavButton("EVENT");
        eventButton.addActionListener(e -> showCard("Events"));
        
        faqButton = createNavButton("FAQ");
        faqButton.addActionListener(e -> showCard("FAQ"));
        
        noticeButton = createNavButton("NOTICE");
        noticeButton.addActionListener(e -> showCard("Notices"));
        
        aboutButton = createNavButton("ABOUT US");
        aboutButton.addActionListener(e -> showCard("About"));

        navButtons.add(shopButton);
        navButtons.add(eventButton);
        navButtons.add(faqButton);
        navButtons.add(noticeButton);
        navButtons.add(aboutButton);

        navBar.add(navButtons, BorderLayout.CENTER);

        // User section
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userPanel.setOpaque(false);

        JButton cartButton = new JButton("CART");
        cartButton.setFont(new Font("Arial", Font.BOLD, 12));
        cartButton.setBackground(ThemeColors.SECONDARY);
        cartButton.setForeground(Color.WHITE);
        cartButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        cartButton.addActionListener(e -> {
            loadCartItems();
            showCard("Cart");
        });
        userPanel.add(cartButton);

        JButton wishlistButton = new JButton("WISHLIST");
        wishlistButton.setFont(new Font("Arial", Font.BOLD, 12));
        wishlistButton.setBackground(ThemeColors.SECONDARY);
        wishlistButton.setForeground(Color.WHITE);
        wishlistButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        wishlistButton.addActionListener(e -> {
            loadWishlist();
            showCard("Wishlist");
        });
        userPanel.add(wishlistButton);

        JButton ordersButton = new JButton("ORDERS");
        ordersButton.setFont(new Font("Arial", Font.BOLD, 12));
        ordersButton.setBackground(ThemeColors.SECONDARY);
        ordersButton.setForeground(Color.WHITE);
        ordersButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        ordersButton.addActionListener(e -> {
            loadOrders();
            showCard("Orders");
        });
        userPanel.add(ordersButton);

        JButton reviewButton = new JButton("REVIEWS");
        reviewButton.setFont(new Font("Arial", Font.BOLD, 12));
        reviewButton.setBackground(ThemeColors.CARD_BG);
        reviewButton.setForeground(ThemeColors.TEXT);
        reviewButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        reviewButton.addActionListener(e -> new ProductReviewFrame(customerId));
        userPanel.add(reviewButton);

        JButton logoutButton = new JButton("LOGOUT");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 12));
        logoutButton.setBackground(ThemeColors.CARD_BG);
        logoutButton.setForeground(ThemeColors.TEXT);
        logoutButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        logoutButton.addActionListener(e -> logout());
        userPanel.add(logoutButton);

        navBar.add(userPanel, BorderLayout.EAST);

        return navBar;
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

    private JPanel createHomePanel() {
        JPanel homePanel = new JPanel(new BorderLayout());
        homePanel.setBackground(ThemeColors.BACKGROUND);

        // ========== BANNER SECTION ==========
        JPanel bannerPanel = new JPanel(new BorderLayout());
        bannerPanel.setBackground(ThemeColors.CARD_BG);
        bannerPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        bannerPanel.setPreferredSize(new Dimension(1920, 180));

        try {
            URL gifURL = getClass().getResource("/images/promotional.gif");
            if (gifURL == null) {
                File imgFile = new File("src/images/promotional.gif");
                if (imgFile.exists()) {
                    gifURL = imgFile.toURI().toURL();
                }
            }

            if (gifURL == null) {
                String[] altPaths = {
                    "resources/images/promotional.gif",
                    "images/promotional.gif",
                    "promotional.gif"
                };

                for (String path : altPaths) {
                    File altFile = new File(path);
                    if (altFile.exists()) {
                        gifURL = altFile.toURI().toURL();
                        break;
                    }
                }
            }

            if (gifURL != null) {
                final ImageIcon originalIcon = new ImageIcon(gifURL);
                JPanel imagePanel = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        originalIcon.paintIcon(this, g, 
                            (this.getWidth() - originalIcon.getIconWidth()) / 2, 
                            (this.getHeight() - originalIcon.getIconHeight()) / 2);
                    }

                    @Override
                    public Dimension getPreferredSize() {
                        return new Dimension(originalIcon.getIconWidth(), originalIcon.getIconHeight());
                    }
                };

                imagePanel.setBackground(ThemeColors.CARD_BG);
                imagePanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                imagePanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        loadAllProducts();
                    }
                });

                JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                wrapperPanel.setBackground(ThemeColors.CARD_BG);
                wrapperPanel.add(imagePanel);
                bannerPanel.add(wrapperPanel, BorderLayout.CENTER);
            } else {
                throw new FileNotFoundException("Banner image not found in any location");
            }
        } catch (Exception e) {
            System.err.println("Banner error: " + e.getMessage());
            e.printStackTrace();

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
                    loadAllProducts();
                }
            });

            bannerPanel.add(fallbackPanel, BorderLayout.CENTER);
            fallbackPanel.add(fallbackLabel, BorderLayout.CENTER);
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
        searchField.addActionListener(e -> filterProducts());

        JButton searchButton = createStyledButton("Search", ThemeColors.PRIMARY);
        searchButton.addActionListener(e -> filterProducts());

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Add search panel below the banner in the top panel
        topPanel.add(searchPanel, BorderLayout.CENTER);

        // Add the complete top panel to the NORTH of the home panel
        homePanel.add(topPanel, BorderLayout.NORTH);

        // ========== PRODUCT GRID ==========
        homeProductGridPanel = new JPanel(new GridLayout(0, 4, 20, 20));
        homeProductGridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        homeProductGridPanel.setBackground(ThemeColors.BACKGROUND);

        // Create a wrapper panel for proper scrolling
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.add(homeProductGridPanel, BorderLayout.NORTH);
        wrapperPanel.setBackground(ThemeColors.BACKGROUND);

        JScrollPane scrollPane = new JScrollPane(wrapperPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);

        homePanel.add(scrollPane, BorderLayout.CENTER);

        // Load initial products to home panel
        loadProductsToPanel(homeProductGridPanel);

        return homePanel;
    }

    private void loadAllProducts() {
        loadProductsToPanel(homeProductGridPanel);
        loadProductsToPanel(productsProductGridPanel);
    }
    
    private void filterProducts() {
        String searchText = searchField.getText().trim().toLowerCase();
        JPanel currentPanel = currentCard.equals("Home") ? homeProductGridPanel : productsProductGridPanel;

        currentPanel.removeAll();
        currentPanel.setLayout(new GridLayout(0, 4, 20, 20));
        currentPanel.setBackground(ThemeColors.BACKGROUND);

        if (searchText.isEmpty()) {
            loadProductsToPanel(currentPanel);
            return;
        }

        try (Connection conn = DBConnection.connect()) {
            String query = "SELECT * FROM products WHERE LOWER(name) LIKE ? OR LOWER(group_name) LIKE ? ORDER BY name";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, "%" + searchText + "%");
            stmt.setString(2, "%" + searchText + "%");
            ResultSet rs = stmt.executeQuery();
            boolean hasResults = false;

            while (rs.next()) {
                hasResults = true;
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String groupName = rs.getString("group_name");
                double price = rs.getDouble("price");
                String description = rs.getString("description");
                currentPanel.add(createProductCard(name, groupName, price, id, description));
            }

            if (!hasResults) {
                showNoResultsMessage(searchText, currentPanel);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error searching products: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }

        currentPanel.revalidate();
        currentPanel.repaint();
    }

    private void showNoResultsMessage(String searchText, JPanel panel) {
        JLabel noResultsLabel = new JLabel("No products found matching \"" + searchText + "\"");
        noResultsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        noResultsLabel.setForeground(Color.GRAY);
        panel.add(noResultsLabel);
    }

    private void refreshProductDisplay() {
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
    private JPanel createProductCard(String name, String groupName, double price, int id, String description) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(ThemeColors.CARD_BG);
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        card.setPreferredSize(new Dimension(250, 350));

        // Image container with fixed size
        JPanel imageContainer = new JPanel(new BorderLayout());
        imageContainer.setPreferredSize(new Dimension(230, 230));
        imageContainer.setBackground(ThemeColors.BACKGROUND);

        try {
            // Try multiple image locations
            ImageIcon originalIcon = null;
            // Try multiple locations - add this to your image loading code
            String[] possiblePaths = {
                "src/images/products/" + name.replaceAll("\\s+", "_").toLowerCase() + ".png",
                "src\\images\\products\\" + name.replaceAll("\\s+", "_").toLowerCase() + ".png", // Windows path
                "images/products/" + name.replaceAll("\\s+", "_").toLowerCase() + ".png",
                "images\\products\\" + name.replaceAll("\\s+", "_").toLowerCase() + ".png", // Windows path
                "resources/images/products/" + name.replaceAll("\\s+", "_").toLowerCase() + ".png"
        };

            for (String path : possiblePaths) {
                File imageFile = new File(path);
                if (imageFile.exists()) {
                    originalIcon = new ImageIcon(path);
                    break;
                }
            }

            // If not found in filesystem, try resources
            if (originalIcon == null) {
                URL imageUrl = getClass().getResource("/images/products/" + name.replaceAll("\\s+", "_").toLowerCase() + ".png");
                if (imageUrl != null) {
                    originalIcon = new ImageIcon(imageUrl);
                }
            }

            if (originalIcon != null) {
                Image scaledImage = originalIcon.getImage().getScaledInstance(230, 230, Image.SCALE_SMOOTH);
                JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
                imageContainer.add(imageLabel, BorderLayout.CENTER);
            } else {
                JLabel noImageLabel = new JLabel("No Image Available", SwingConstants.CENTER);
                noImageLabel.setFont(new Font("Arial", Font.ITALIC, 12));
                noImageLabel.setForeground(Color.GRAY);
                imageContainer.add(noImageLabel, BorderLayout.CENTER);
            }
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Image Error", SwingConstants.CENTER);
            errorLabel.setForeground(Color.RED);
            imageContainer.add(errorLabel, BorderLayout.CENTER);
        }

        card.add(imageContainer, BorderLayout.CENTER);

        // Product info panel
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(ThemeColors.TEXT);

        JLabel priceLabel = new JLabel(String.format("₱%.2f", price)); // Ensure PHP formatting
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
        detailsButton.addActionListener(e -> showProductDetails(id, name, price, description));

        JButton addToCartButton = createStyledButton("Add to Cart", ThemeColors.PRIMARY);
        addToCartButton.setFont(new Font("Arial", Font.BOLD, 12));
        addToCartButton.addActionListener(e -> addToCart(id, name, price));

        buttonPanel.add(detailsButton);
        buttonPanel.add(addToCartButton);

        infoPanel.add(buttonPanel, BorderLayout.SOUTH);
        card.add(infoPanel, BorderLayout.SOUTH);

        return card;
    }

    private void showProductDetails(int productId, String name, double price, String description) {
        JDialog detailsDialog = new JDialog(this, "Product Details", true);
        detailsDialog.setSize(400, 400);
        detailsDialog.setLayout(new BorderLayout());
        detailsDialog.getContentPane().setBackground(ThemeColors.BACKGROUND);

        JPanel detailsPanel = new JPanel(new BorderLayout(10, 10));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        detailsPanel.setBackground(ThemeColors.BACKGROUND);

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        try {
            String imagePath = "src/images/products/" + name.replaceAll("\\s+", "_").toLowerCase() + ".png";
            File imageFile = new File(imagePath);
            ImageIcon originalIcon;
            
            if (imageFile.exists()) {
                originalIcon = new ImageIcon(imagePath);
            } else {
                URL imageUrl = getClass().getResource("/images/products/" + name.replaceAll("\\s+", "_").toLowerCase() + ".png");
                if (imageUrl != null) {
                    originalIcon = new ImageIcon(imageUrl);
                } else {
                    originalIcon = new ImageIcon(getClass().getResource("/images/products/placeholder.png"));
                }
            }
            Image scaledImage = originalIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            imageLabel.setText("No Image Available");
            imageLabel.setForeground(ThemeColors.TEXT);
        }
        detailsPanel.add(imageLabel, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new BorderLayout(5, 5));
        infoPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        nameLabel.setForeground(ThemeColors.PRIMARY);
        
        JLabel priceLabel = new JLabel(String.format("Price: ₱%.2f", price), SwingConstants.CENTER);
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        priceLabel.setForeground(ThemeColors.TEXT);
        
        JTextArea descArea = new JTextArea(description);
        descArea.setEditable(false);
        descArea.setFont(new Font("Arial", Font.PLAIN, 14));
        descArea.setBackground(ThemeColors.BACKGROUND);
        descArea.setForeground(ThemeColors.TEXT);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        
        infoPanel.add(nameLabel, BorderLayout.NORTH);
        infoPanel.add(priceLabel, BorderLayout.CENTER);
        infoPanel.add(new JScrollPane(descArea), BorderLayout.SOUTH);
        detailsPanel.add(infoPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        buttonPanel.setOpaque(false);
        
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

        JButton backButton = new JButton("← Back to Home");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(ThemeColors.CARD_BG);
        backButton.setForeground(ThemeColors.TEXT);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        backButton.addActionListener(e -> showCard("Home"));
        panel.add(backButton, BorderLayout.NORTH);

        productsProductGridPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 20, 20));
        productsProductGridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        productsProductGridPanel.setBackground(ThemeColors.BACKGROUND);

        JScrollPane scrollPane = new JScrollPane(productsProductGridPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Load products to products panel
        loadProductsToPanel(productsProductGridPanel);

        return panel;
    }
    
    private void loadProductsToPanel(JPanel targetPanel) {
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM products ORDER BY name")) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String groupName = rs.getString("group_name");
                double priceUSD = rs.getDouble("price"); // Assume stored in USD
                double pricePHP = priceUSD * 66.67; // Same conversion rate
                String description = rs.getString("description");

                targetPanel.add(createProductCard(name, groupName, pricePHP, id, description));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading products.", "Error", JOptionPane.ERROR_MESSAGE);
        }
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

        JButton checkoutButton = new JButton("PROCEED TO CHECKOUT");
        checkoutButton.setFont(new Font("Arial", Font.BOLD, 16));
        checkoutButton.setBackground(ThemeColors.PRIMARY);
        checkoutButton.setForeground(Color.WHITE);
        checkoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        checkoutButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, checkoutButton.getPreferredSize().height));
        checkoutButton.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        checkoutButton.addActionListener(e -> checkout());
        summaryPanel.add(checkoutButton);

        cartContentPanel.add(summaryPanel, BorderLayout.SOUTH);
        panel.add(cartContentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCartItemPanel(int cartId, String productName, double price, int quantity, String color, String size) {
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

        // Circular checkbox (centered vertically) - Now unchecked by default
        JCheckBox selectCheckbox = new JCheckBox() {
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
        selectCheckbox.setSelected(false); // Changed to unchecked by default
        selectCheckbox.setPreferredSize(new Dimension(20, 20));
        selectCheckbox.setBorder(BorderFactory.createEmptyBorder());
        selectCheckbox.setContentAreaFilled(false);
        selectCheckbox.setFocusPainted(false);
        selectCheckbox.putClientProperty("id", cartId);
        selectCheckbox.putClientProperty("price", price);
        selectCheckbox.putClientProperty("quantity", quantity);
        selectCheckbox.addActionListener(e -> calculateSelectedTotal());

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

        // Load product image
        try {
            ImageIcon originalIcon = null;
            String[] possiblePaths = {
                "src/images/products/" + productName.replaceAll("\\s+", "_").toLowerCase() + ".png",
                "images/products/" + productName.replaceAll("\\s+", "_").toLowerCase() + ".png",
                "resources/images/products/" + productName.replaceAll("\\s+", "_").toLowerCase() + ".png"
            };

            // Check filesystem paths first
            for (String path : possiblePaths) {
                File imageFile = new File(path);
                if (imageFile.exists()) {
                    originalIcon = new ImageIcon(path);
                    break;
                }
            }

            // If not found in filesystem, check resources
            if (originalIcon == null) {
                URL imageUrl = getClass().getResource("/images/products/" + 
                    productName.replaceAll("\\s+", "_").toLowerCase() + ".png");
                if (imageUrl != null) {
                    originalIcon = new ImageIcon(imageUrl);
                } else {
                    // Fallback to placeholder image
                    URL placeholderUrl = getClass().getResource("/images/products/placeholder.png");
                    if (placeholderUrl != null) {
                        originalIcon = new ImageIcon(placeholderUrl);
                    }
                }
            }

            if (originalIcon != null) {
                Image scaledImage = originalIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
                imagePanel.add(imageLabel);
            } else {
                throw new FileNotFoundException("Image not found in any location");
            }
        } catch (Exception e) {
            // Fallback when image loading fails
            JLabel noImageLabel = new JLabel("No Image", SwingConstants.CENTER);
            noImageLabel.setFont(new Font("Arial", Font.ITALIC, 10));
            noImageLabel.setForeground(Color.GRAY);
            imagePanel.add(noImageLabel);
        }

        leftPanel.add(imagePanel, BorderLayout.CENTER);
        contentPanel.add(leftPanel, BorderLayout.WEST);

        // Center panel for product info
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // Product name
        JLabel nameLabel = new JLabel(productName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(ThemeColors.TEXT);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Product details (color, size)
        JLabel detailsLabel = new JLabel("Color: " + (color != null ? color : "Default") + 
                              " | Size: " + (size != null ? size : "Default"));
        detailsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        detailsLabel.setForeground(ThemeColors.TEXT);
        detailsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Product price
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

        // Container for right-side components
        JPanel rightComponentsPanel = new JPanel();
        rightComponentsPanel.setLayout(new BoxLayout(rightComponentsPanel, BoxLayout.Y_AXIS));
        rightComponentsPanel.setOpaque(false);

        // Quantity controls (centered)
        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        quantityPanel.setOpaque(false);

        // Create a final reference to the quantity label so we can update it
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

        // Remove button (centered)
        JButton removeButton = new JButton("Remove");
        removeButton.setFont(new Font("Arial", Font.BOLD, 12));
        removeButton.setForeground(new Color(200, 0, 0));
        removeButton.setContentAreaFilled(false);
        removeButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        removeButton.addActionListener(e -> {
            removeCartItem(cartId);
            calculateSelectedTotal();
        });

        JPanel removeButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        removeButtonPanel.setOpaque(false);
        removeButtonPanel.add(removeButton);

        rightComponentsPanel.add(removeButtonPanel);

        // Add all right components to the main right panel
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
        checkbox.setSelected(true);
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
                    double price = (double) checkbox.getClientProperty("price");
                    int quantity = (int) checkbox.getClientProperty("quantity");
                    total += price * quantity;
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
        panel.putClientProperty("wishlistItemsPanel", wishlistItemsPanel);

        JScrollPane scrollPane = new JScrollPane(wishlistItemsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ThemeColors.BACKGROUND);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        wishlistContentPanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(ThemeColors.CARD_BG);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Remove selected button
        JButton removeSelectedButton = new JButton("REMOVE");
        removeSelectedButton.setFont(new Font("Arial", Font.BOLD, 16));
        removeSelectedButton.setBackground(new Color(200, 0, 0));
        removeSelectedButton.setForeground(Color.WHITE);
        removeSelectedButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeSelectedButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, removeSelectedButton.getPreferredSize().height));
        removeSelectedButton.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        removeSelectedButton.addActionListener(e -> removeSelectedWishlistItems());
        buttonPanel.add(removeSelectedButton);

        // Move to cart button
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton moveToCartButton = new JButton("MOVE TO CART");
        moveToCartButton.setFont(new Font("Arial", Font.BOLD, 16));
        moveToCartButton.setBackground(ThemeColors.PRIMARY);
        moveToCartButton.setForeground(Color.WHITE);
        moveToCartButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        moveToCartButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, moveToCartButton.getPreferredSize().height));
        moveToCartButton.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        moveToCartButton.addActionListener(e -> moveSelectedToCart());
        buttonPanel.add(moveToCartButton);

        wishlistContentPanel.add(buttonPanel, BorderLayout.SOUTH);
        panel.add(wishlistContentPanel, BorderLayout.CENTER);

        return panel;
    }
    
    private JPanel createWishlistItemPanel(int productId, String productName, double price, String description) {
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

        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout(10, 0));
        contentPanel.setOpaque(false);

        // Left panel (checkbox and image)
        JPanel leftPanel = new JPanel(new BorderLayout(10, 0));
        leftPanel.setOpaque(false);

        // Circular checkbox (centered vertically)
        JCheckBox selectCheckbox = new JCheckBox() {
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
        selectCheckbox.setSelected(false);
        selectCheckbox.setPreferredSize(new Dimension(20, 20));
        selectCheckbox.setBorder(BorderFactory.createEmptyBorder());
        selectCheckbox.setContentAreaFilled(false);
        selectCheckbox.setFocusPainted(false);

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

        // Image loading logic
        try {
            ImageIcon originalIcon = null;
            String[] possiblePaths = {
                "src/images/products/" + productName.replaceAll("\\s+", "_").toLowerCase() + ".png",
                "images/products/" + productName.replaceAll("\\s+", "_").toLowerCase() + ".png",
                "resources/images/products/" + productName.replaceAll("\\s+", "_").toLowerCase() + ".png"
            };

            // Check filesystem paths first
            for (String path : possiblePaths) {
                File imageFile = new File(path);
                if (imageFile.exists()) {
                    originalIcon = new ImageIcon(path);
                    break;
                }
            }

            // If not found in filesystem, check resources
            if (originalIcon == null) {
                URL imageUrl = getClass().getResource("/images/products/" + 
                    productName.replaceAll("\\s+", "_").toLowerCase() + ".png");
                if (imageUrl != null) {
                    originalIcon = new ImageIcon(imageUrl);
                } else {
                    // Fallback to placeholder image
                    URL placeholderUrl = getClass().getResource("/images/products/placeholder.png");
                    if (placeholderUrl != null) {
                        originalIcon = new ImageIcon(placeholderUrl);
                    }
                }
            }

            if (originalIcon != null) {
                Image scaledImage = originalIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
                imagePanel.add(imageLabel);
            } else {
                throw new FileNotFoundException("Image not found in any location");
            }
        } catch (Exception e) {
            // Fallback when image loading fails
            JLabel noImageLabel = new JLabel("No Image", SwingConstants.CENTER);
            noImageLabel.setFont(new Font("Arial", Font.ITALIC, 10));
            noImageLabel.setForeground(Color.GRAY);
            imagePanel.add(noImageLabel);
        }

        leftPanel.add(imagePanel, BorderLayout.CENTER);
        contentPanel.add(leftPanel, BorderLayout.WEST);

        // Center panel (product info)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(productName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(ThemeColors.TEXT);

        String shortDesc = description.length() > 50 ? description.substring(0, 50) + "..." : description;
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
        detailsButton.addActionListener(e -> showProductDetails(productId, productName, price, description));

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
        if (wishlistItemsPanel == null) return;

        List<Integer> productIdsToRemove = new ArrayList<>();

        for (Component comp : wishlistItemsPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel itemPanel = (JPanel) comp;
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
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.connect()) {
            conn.setAutoCommit(false);
            try {
                String sql = "DELETE FROM wishlist WHERE customer_id = ? AND product_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);

                for (int productId : productIdsToRemove) {
                    stmt.setInt(1, customerId);
                    stmt.setInt(2, productId);
                    stmt.addBatch();
                }

                stmt.executeBatch();
                conn.commit();
                loadWishlist(); // Refresh the view

                JOptionPane.showMessageDialog(this, 
                    "Selected items removed from wishlist", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error removing items from wishlist: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void moveSelectedToCart() {
        JPanel wishlistItemsPanel = (JPanel) wishlistPanel.getClientProperty("wishlistItemsPanel");
        if (wishlistItemsPanel == null) return;

        List<Integer> productIdsToRemove = new ArrayList<>();
        boolean anyMoved = false;

        for (Component comp : wishlistItemsPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel itemPanel = (JPanel) comp;

                // Skip filler components
                if (itemPanel.getClass().getName().contains("Box₱Filler")) {
                    continue;
                }

                JCheckBox checkbox = findCheckboxInPanel(itemPanel);
                if (checkbox != null && checkbox.isSelected()) {
                    Integer productId = (Integer) itemPanel.getClientProperty("productId");
                    String productName = (String) itemPanel.getClientProperty("productName");
                    Double price = (Double) itemPanel.getClientProperty("price");

                    if (productId != null && productName != null && price != null) {
                        // Add to cart
                        addToCart(productId, productName, price);
                        // Mark for removal from wishlist
                        productIdsToRemove.add(productId);
                        anyMoved = true;
                    }
                }
            }
        }

        // Remove from wishlist in a single transaction
        if (!productIdsToRemove.isEmpty()) {
            try (Connection conn = DBConnection.connect()) {
                conn.setAutoCommit(false);
                try {
                    String sql = "DELETE FROM wishlist WHERE customer_id = ? AND product_id = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);

                    for (int productId : productIdsToRemove) {
                        stmt.setInt(1, customerId);
                        stmt.setInt(2, productId);
                        stmt.addBatch();
                    }

                    stmt.executeBatch();
                    conn.commit();

                    // Refresh the wishlist view
                    loadWishlist();

                    JOptionPane.showMessageDialog(this,
                        "Selected items moved to cart and removed from wishlist",
                        "Success", JOptionPane.INFORMATION_MESSAGE);

                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                    "Error moving items: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (!anyMoved) {
            JOptionPane.showMessageDialog(this,
                "Please select at least one item to move",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
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
        panel.putClientProperty("ordersPanel", ordersPanel);

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

        // Review button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(ThemeColors.CARD_BG);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton reviewButton = new JButton("LEAVE REVIEW");
        reviewButton.setFont(new Font("Arial", Font.BOLD, 16));
        reviewButton.setBackground(ThemeColors.PRIMARY);
        reviewButton.setForeground(Color.WHITE);
        reviewButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        reviewButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, reviewButton.getPreferredSize().height));
        reviewButton.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        reviewButton.addActionListener(e -> {
            // Find selected order
            for (Component comp : ordersPanel.getComponents()) {
                if (comp instanceof JPanel) {
                    JPanel orderPanel = (JPanel) comp;
                    JCheckBox checkbox = findCheckboxInPanel(orderPanel);
                    if (checkbox != null && checkbox.isSelected()) {
                        Integer orderId = (Integer) orderPanel.getClientProperty("orderId");
                        if (orderId != null) {
                            new ProductReviewFrame(customerId);
                            return;
                        }
                    }
                }
            }
            JOptionPane.showMessageDialog(this, 
                "Please select an order to review", 
                "Error", JOptionPane.ERROR_MESSAGE);
        });
        buttonPanel.add(reviewButton);

        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }
    
    private JPanel createOrderItemPanel(int orderId, String productName, int quantity, 
                                    String status, String date, double price) {
      JPanel itemPanel = new JPanel(new BorderLayout(10, 10));
      itemPanel.setBackground(ThemeColors.CARD_BG);
      itemPanel.setBorder(BorderFactory.createCompoundBorder(
          BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.BACKGROUND),
          BorderFactory.createEmptyBorder(15, 15, 15, 15)
      ));
      // Increased height to 150px to accommodate all details
      itemPanel.setPreferredSize(new Dimension(800, 150));
      itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

      // Store order ID for reference
      itemPanel.putClientProperty("orderId", orderId);

      // Main content panel
      JPanel contentPanel = new JPanel(new BorderLayout(10, 0));
      contentPanel.setOpaque(false);

      // Left panel (image)
      JPanel leftPanel = new JPanel(new BorderLayout(10, 0));
      leftPanel.setOpaque(false);
      leftPanel.setPreferredSize(new Dimension(100, 120));

      // Image panel - increased size to 100x100
      JPanel imagePanel = new JPanel(new GridBagLayout());
      imagePanel.setPreferredSize(new Dimension(100, 100));
      imagePanel.setBackground(ThemeColors.BACKGROUND);
      imagePanel.setBorder(BorderFactory.createLineBorder(ThemeColors.SECONDARY, 1));

      // Image loading logic
      try {
          ImageIcon originalIcon = null;
          String[] possiblePaths = {
              "src/images/products/" + productName.replaceAll("\\s+", "_").toLowerCase() + ".png",
              "images/products/" + productName.replaceAll("\\s+", "_").toLowerCase() + ".png",
              "resources/images/products/" + productName.replaceAll("\\s+", "_").toLowerCase() + ".png"
          };

          for (String path : possiblePaths) {
              File imageFile = new File(path);
              if (imageFile.exists()) {
                  originalIcon = new ImageIcon(path);
                  break;
              }
          }

          if (originalIcon == null) {
              URL imageUrl = getClass().getResource("/images/products/" + 
                  productName.replaceAll("\\s+", "_").toLowerCase() + ".png");
              if (imageUrl != null) {
                  originalIcon = new ImageIcon(imageUrl);
              } else {
                  URL placeholderUrl = getClass().getResource("/images/products/placeholder.png");
                  if (placeholderUrl != null) {
                      originalIcon = new ImageIcon(placeholderUrl);
                  }
              }
          }

          if (originalIcon != null) {
              Image scaledImage = originalIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
              JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
              imagePanel.add(imageLabel);
          } else {
              throw new FileNotFoundException("Image not found in any location");
          }
      } catch (Exception e) {
          JLabel noImageLabel = new JLabel("No Image", SwingConstants.CENTER);
          noImageLabel.setFont(new Font("Arial", Font.ITALIC, 10));
          noImageLabel.setForeground(Color.GRAY);
          imagePanel.add(noImageLabel);
      }

      leftPanel.add(imagePanel, BorderLayout.CENTER);
      contentPanel.add(leftPanel, BorderLayout.WEST);

      // Center panel (order info) - added more vertical space
      JPanel centerPanel = new JPanel();
      centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
      centerPanel.setOpaque(false);

      JLabel nameLabel = new JLabel(productName);
      nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
      nameLabel.setForeground(ThemeColors.TEXT);

      JLabel quantityLabel = new JLabel("Quantity: " + quantity);
      quantityLabel.setFont(new Font("Arial", Font.PLAIN, 14)); // Increased font size
      quantityLabel.setForeground(ThemeColors.TEXT);

      JLabel priceLabel = new JLabel(String.format("Price: ₱%.2f", price));
      priceLabel.setFont(new Font("Arial", Font.BOLD, 16)); // Increased font size
      priceLabel.setForeground(ThemeColors.PRIMARY);

      JLabel statusLabel = new JLabel("Status: " + status);
      statusLabel.setFont(new Font("Arial", Font.PLAIN, 14)); // Increased font size
      // Color code based on status
      switch (status.toLowerCase()) {
          case "completed":
              statusLabel.setForeground(new Color(0, 150, 0)); // Green
              break;
          case "processing":
              statusLabel.setForeground(new Color(200, 150, 0)); // Orange
              break;
          case "shipped":
              statusLabel.setForeground(new Color(0, 100, 200)); // Blue
              break;
          case "cancelled":
              statusLabel.setForeground(Color.RED);
              break;
          default:
              statusLabel.setForeground(ThemeColors.TEXT);
      }

      JLabel dateLabel = new JLabel("Order Date: " + date);
      dateLabel.setFont(new Font("Arial", Font.PLAIN, 14)); // Increased font size
      dateLabel.setForeground(ThemeColors.TEXT);

      // Added more spacing between components
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

      itemPanel.add(contentPanel, BorderLayout.CENTER);
      return itemPanel;
  }
    
    private void showOrderDetails(int orderId, String productName, int quantity, 
                                String status, String date, double price) {
        JDialog detailsDialog = new JDialog(this, "Order Details", true);
        detailsDialog.setSize(400, 400);
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

        try {
            ImageIcon originalIcon = null;
            String[] possiblePaths = {
                "src/images/products/" + productName.replaceAll("\\s+", "_").toLowerCase() + ".png",
                "images/products/" + productName.replaceAll("\\s+", "_").toLowerCase() + ".png",
                "resources/images/products/" + productName.replaceAll("\\s+", "_").toLowerCase() + ".png"
            };

            for (String path : possiblePaths) {
                File imageFile = new File(path);
                if (imageFile.exists()) {
                    originalIcon = new ImageIcon(path);
                    break;
                }
            }

            if (originalIcon == null) {
                URL imageUrl = getClass().getResource("/images/products/" + 
                    productName.replaceAll("\\s+", "_").toLowerCase() + ".png");
                if (imageUrl != null) {
                    originalIcon = new ImageIcon(imageUrl);
                } else {
                    URL placeholderUrl = getClass().getResource("/images/products/placeholder.png");
                    if (placeholderUrl != null) {
                        originalIcon = new ImageIcon(placeholderUrl);
                    }
                }
            }

            if (originalIcon != null) {
                Image scaledImage = originalIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
                imagePanel.add(imageLabel);
            } else {
                throw new FileNotFoundException("Image not found in any location");
            }
        } catch (Exception e) {
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
        // Color code based on status
        switch (status.toLowerCase()) {
            case "completed":
                statusLabel.setForeground(new Color(0, 150, 0)); // Green
                break;
            case "processing":
                statusLabel.setForeground(new Color(200, 150, 0)); // Orange
                break;
            case "shipped":
                statusLabel.setForeground(new Color(0, 100, 200)); // Blue
                break;
            case "cancelled":
                statusLabel.setForeground(Color.RED);
                break;
            default:
                statusLabel.setForeground(ThemeColors.TEXT);
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

        // Add this test method to your CustomerFrame class
    private void testDatabasePersistence() {
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement()) {

            // Test insert
            stmt.executeUpdate("INSERT INTO cart (product_id, product_name, price, quantity, customer_id) " +
                             "VALUES (1, 'Test Product', 10.99, 1, " + customerId + ")");

            // Verify insert
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM cart WHERE customer_id = " + customerId);
            rs.next();
            int count = rs.getInt(1);
            JOptionPane.showMessageDialog(this, "Test record count: " + count);

            // Cleanup
            stmt.executeUpdate("DELETE FROM cart WHERE product_name = 'Test Product'");

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Database test failed: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
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
                // Standard dialog that will be properly centered
                JOptionPane.showMessageDialog(this, 
                    "Product added to cart!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);

                loadCartItems();
            }
        } catch (SQLException ex) {
            System.err.println("Cart SQL Error: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                "Error updating cart: " + ex.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // Helper method for debugging
    private void debugPrintCartContents(Connection conn) throws SQLException {
        System.out.println("Current Cart Contents:");
        String sql = "SELECT c.id, p.name, c.quantity FROM cart c JOIN products p ON c.product_id = p.id WHERE c.customer_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.printf("Cart ID: %d, Product: %s, Qty: %d%n", 
                    rs.getInt("id"), rs.getString("name"), rs.getInt("quantity"));
            }
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
                // No need to refresh UI here if called from product details
            }
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1062) { // Duplicate entry
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
    
    private JPanel createWishlistProductCard(String name, String category, double price, int id, String description) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(ThemeColors.CARD_BG);
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        card.setPreferredSize(new Dimension(250, 350));
        card.putClientProperty("productId", id);
        card.putClientProperty("productName", name);
        card.putClientProperty("price", price);

        // Image container
        JPanel imageContainer = new JPanel(new BorderLayout());
        imageContainer.setPreferredSize(new Dimension(230, 230));
        imageContainer.setBackground(ThemeColors.BACKGROUND);

        try {
            ImageIcon originalIcon = null;
            String[] possiblePaths = {
                "src/images/products/" + name.replaceAll("\\s+", "_").toLowerCase() + ".png",
                "images/products/" + name.replaceAll("\\s+", "_").toLowerCase() + ".png",
                "resources/images/products/" + name.replaceAll("\\s+", "_").toLowerCase() + ".png"
            };

            for (String path : possiblePaths) {
                File imageFile = new File(path);
                if (imageFile.exists()) {
                    originalIcon = new ImageIcon(path);
                    break;
                }
            }

            if (originalIcon == null) {
                URL imageUrl = getClass().getResource("/images/products/" + name.replaceAll("\\s+", "_").toLowerCase() + ".png");
                if (imageUrl != null) {
                    originalIcon = new ImageIcon(imageUrl);
                }
            }

            if (originalIcon != null) {
                Image scaledImage = originalIcon.getImage().getScaledInstance(230, 230, Image.SCALE_SMOOTH);
                JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
                imageContainer.add(imageLabel, BorderLayout.CENTER);
            } else {
                JLabel noImageLabel = new JLabel("No Image Available", SwingConstants.CENTER);
                noImageLabel.setFont(new Font("Arial", Font.ITALIC, 12));
                noImageLabel.setForeground(Color.GRAY);
                imageContainer.add(noImageLabel, BorderLayout.CENTER);
            }
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Image Error", SwingConstants.CENTER);
            errorLabel.setForeground(Color.RED);
            imageContainer.add(errorLabel, BorderLayout.CENTER);
        }

        card.add(imageContainer, BorderLayout.CENTER);

        // Product info panel
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Top panel for checkbox and name
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        // Selection checkbox
        JCheckBox selectCheckbox = new JCheckBox();
        selectCheckbox.setOpaque(false);
        selectCheckbox.setPreferredSize(new Dimension(20, 20));
        topPanel.add(selectCheckbox, BorderLayout.WEST);

        // Product name
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(ThemeColors.TEXT);
        topPanel.add(nameLabel, BorderLayout.CENTER);

        infoPanel.add(topPanel, BorderLayout.NORTH);

        // Price label
        JLabel priceLabel = new JLabel(String.format("₱%.2f", price));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        priceLabel.setForeground(ThemeColors.PRIMARY);
        infoPanel.add(priceLabel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton detailsButton = createStyledButton("Details", ThemeColors.SECONDARY);
        detailsButton.setFont(new Font("Arial", Font.BOLD, 12));
        detailsButton.addActionListener(e -> showProductDetails(id, name, price, description));

        JButton removeButton = createStyledButton("Remove", new Color(200, 0, 0));
        removeButton.setFont(new Font("Arial", Font.BOLD, 12));
        removeButton.addActionListener(e -> {
            removeFromWishlist(id);
            loadWishlist();
        });

        JButton moveToCartButton = createStyledButton("Cart", ThemeColors.PRIMARY);
        moveToCartButton.setFont(new Font("Arial", Font.BOLD, 12));
        moveToCartButton.addActionListener(e -> {
            addToCart(id, name, price);
            JOptionPane.showMessageDialog(this, "Product added to cart!");
        });

        buttonPanel.add(detailsButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(moveToCartButton);

        infoPanel.add(buttonPanel, BorderLayout.SOUTH);
        card.add(infoPanel, BorderLayout.SOUTH);

        return card;
    }
    
    private void removeFromWishlist(int productId) {
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                 "DELETE FROM wishlist WHERE customer_id = ? AND product_id = ?")) {

            stmt.setInt(1, customerId);
            stmt.setInt(2, productId);
            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.err.println("Error removing from wishlist: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                "Error removing item from wishlist",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void moveToCartFromWishlist() {
        // Find the wishlist items panel
        JPanel wishlistItemsPanel = null;
        for (Component comp : wishlistPanel.getComponents()) {
            if (comp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) comp;
                wishlistItemsPanel = (JPanel) scrollPane.getViewport().getView();
                break;
            }
        }

        if (wishlistItemsPanel == null) return;

        // Find the selected product
        Component[] components = wishlistItemsPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel itemPanel = (JPanel) comp;
                Component[] itemComponents = itemPanel.getComponents();
                if (itemComponents.length > 0 && itemComponents[0] instanceof JPanel) {
                    JPanel contentPanel = (JPanel) itemComponents[0];
                    Component[] contentComponents = contentPanel.getComponents();
                    if (contentComponents.length >= 3 && contentComponents[2] instanceof JPanel) {
                        JPanel rightPanel = (JPanel) contentComponents[2];
                        for (Component rightComp : rightPanel.getComponents()) {
                            if (rightComp instanceof JCheckBox) {
                                JCheckBox checkbox = (JCheckBox) rightComp;
                                if (checkbox.isSelected()) {
                                    int productId = (int) checkbox.getClientProperty("productId");
                                    String productName = (String) checkbox.getClientProperty("name");
                                    double price = (double) checkbox.getClientProperty("price");

                                    try (Connection conn = DBConnection.connect()) {
                                        conn.setAutoCommit(false); // Start transaction

                                        try {
                                            // 1. Remove from wishlist
                                            String deleteSql = "DELETE FROM wishlist WHERE customer_id = ? AND product_id = ?";
                                            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                                                deleteStmt.setInt(1, customerId);
                                                deleteStmt.setInt(2, productId);
                                                int deleted = deleteStmt.executeUpdate();
                                                if (deleted == 0) throw new SQLException("Failed to remove from wishlist");
                                            }

                                            // 2. Add to cart (or update quantity if exists)
                                            String cartSql = "INSERT INTO cart (product_id, product_name, price, quantity, customer_id) " +
                                                           "VALUES (?, ?, ?, 1, ?) " +
                                                           "ON DUPLICATE KEY UPDATE quantity = quantity + 1";
                                            try (PreparedStatement cartStmt = conn.prepareStatement(cartSql)) {
                                                cartStmt.setInt(1, productId);
                                                cartStmt.setString(2, productName);
                                                cartStmt.setDouble(3, price);
                                                cartStmt.setInt(4, customerId);
                                                int added = cartStmt.executeUpdate();
                                                if (added == 0) throw new SQLException("Failed to add to cart");
                                            }

                                            conn.commit(); // Commit transaction

                                            // 3. Update both UIs
                                            wishlistItemsPanel.remove(itemPanel);
                                            wishlistItemsPanel.revalidate();
                                            wishlistItemsPanel.repaint();
                                            loadCartItems();

                                            JOptionPane.showMessageDialog(this, 
                                                "Product moved to cart successfully!", 
                                                "Success", JOptionPane.INFORMATION_MESSAGE);
                                            return; // Exit after successful move

                                        } catch (SQLException ex) {
                                            conn.rollback(); // Rollback on error
                                            System.err.println("Transaction failed: " + ex.getMessage());
                                            JOptionPane.showMessageDialog(this,
                                                "Error moving product: " + ex.getMessage(),
                                                "Error", JOptionPane.ERROR_MESSAGE);
                                        }
                                    } catch (SQLException ex) {
                                        System.err.println("Database error: " + ex.getMessage());
                                        JOptionPane.showMessageDialog(this,
                                            "Database connection error",
                                            "Error", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        JOptionPane.showMessageDialog(this, 
            "Please select a product to move", 
            "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void loadCartItems() {
        cartItemsPanel.removeAll();
        cartItemsPanel.setLayout(new BoxLayout(cartItemsPanel, BoxLayout.Y_AXIS));
        double totalAmount = 0.0;

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT c.id, p.name, p.price, c.quantity, p.color, p.size " +
                 "FROM cart c JOIN products p ON c.product_id = p.id " +
                 "WHERE c.customer_id = ?")) {

            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) { 
                int cartId = rs.getInt("id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                int quantity = rs.getInt("quantity");
                String color = rs.getString("color");
                String size = rs.getString("size");

                JPanel itemPanel = createCartItemPanel(
                    cartId, 
                    name, 
                    price, 
                    quantity, 
                    color, 
                    size
                );
                // Store cart ID in the panel for later reference
                itemPanel.putClientProperty("cartId", cartId);

                cartItemsPanel.add(itemPanel);
                cartItemsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

                totalAmount += price * quantity;
            }

            cartTotalLabel.setText(String.format("₱%.2f", totalAmount));

        } catch (SQLException ex) {
            System.err.println("Error loading cart: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Error loading cart items", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }

        cartItemsPanel.revalidate();
        cartItemsPanel.repaint();
    }

    private void loadWishlist() {
        JPanel wishlistItemsPanel = (JPanel) wishlistPanel.getClientProperty("wishlistItemsPanel");
        if (wishlistItemsPanel == null) return;

        wishlistItemsPanel.removeAll();

        String sql = "SELECT p.id, p.name, p.price, p.description " +
                    "FROM wishlist w JOIN products p ON w.product_id = p.id " +
                    "WHERE w.customer_id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                String description = rs.getString("description");

                JPanel itemPanel = createWishlistItemPanel(id, name, price, description);
                wishlistItemsPanel.add(itemPanel);
                wishlistItemsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }

        } catch (SQLException ex) {
            System.err.println("Error loading wishlist: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Error loading wishlist items", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }

        wishlistItemsPanel.revalidate();
        wishlistItemsPanel.repaint();
    }


    private void loadOrders() {
        JPanel ordersPanel = (JPanel) orderTrackingPanel.getClientProperty("ordersPanel");
        if (ordersPanel == null) return;

        ordersPanel.removeAll();

        String sql = "SELECT o.id, p.name, oi.quantity, o.status, o.order_date, p.price " +
                    "FROM orders o " +
                    "JOIN order_items oi ON o.id = oi.order_id " +
                    "JOIN products p ON oi.product_id = p.id " +
                    "WHERE o.customer_id = ? ORDER BY o.order_date DESC";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int quantity = rs.getInt("quantity");
                String status = rs.getString("status");
                String date = rs.getTimestamp("order_date").toString();
                double price = rs.getDouble("price");

                JPanel itemPanel = createOrderItemPanel(id, name, quantity, status, date, price);
                ordersPanel.add(itemPanel);
                ordersPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }

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
                // Get the cart ID stored in the panel's client property
                Integer cartId = (Integer) itemPanel.getClientProperty("cartId");

                // Find the checkbox in this panel
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
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Only proceed with selected items
        new PaymentFrame(customerId, selectedCartIds);
        dispose();
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
            new LoginFrame();
            dispose();
        }
    }

    private void showCard(String cardName) {
        cardLayout.show(mainPanel, cardName);
        currentCard = cardName;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatDarkLaf");
            ex.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            new CustomerFrame(1); // Sample customer ID
        });
    }
}

class WrapLayout extends FlowLayout {
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
            int targetWidth = target.getSize().width;
            if (targetWidth == 0) {
                targetWidth = Integer.MAX_VALUE;
            }

            int hgap = getHgap();
            int vgap = getVgap();
            Insets insets = target.getInsets();
            int maxWidth = targetWidth - (insets.left + insets.right + hgap * 2);

            Dimension dim = new Dimension(0, 0);
            int rowWidth = 0;
            int rowHeight = 0;

            int nmembers = target.getComponentCount();

            for (int i = 0; i < nmembers; i++) {
                Component m = target.getComponent(i);
                if (m.isVisible()) {
                    Dimension d = m.getPreferredSize();
                    if (rowWidth + d.width > maxWidth) {
                        dim.width = Math.max(dim.width, rowWidth);
                        dim.height += rowHeight + vgap;
                        rowWidth = 0;
                        rowHeight = 0;
                    }
                    if (rowWidth != 0) {
                        rowWidth += hgap;
                    }
                    rowWidth += d.width;
                    rowHeight = Math.max(rowHeight, d.height);
                }
            }

            dim.width = Math.max(dim.width, rowWidth);
            dim.height += rowHeight;

            dim.width += insets.left + insets.right + hgap * 2;
            dim.height += insets.top + insets.bottom + vgap * 2;

            // Add some extra height to prevent cutting off last row
            dim.height += vgap * 2;
            
            return dim;
        }
    }

    @Override
    public void layoutContainer(Container target) {
        synchronized (target.getTreeLock()) {
            Insets insets = target.getInsets();
            int maxWidth = target.getWidth() - (insets.left + insets.right + getHgap() * 2);
            int x = insets.left + getHgap();
            int y = insets.top + getVgap();
            int rowHeight = 0;

            boolean firstInRow = true;
            
            for (Component comp : target.getComponents()) {
                if (comp.isVisible()) {
                    Dimension pref = comp.getPreferredSize();

                    if (!firstInRow && (x + pref.width > maxWidth)) {
                        x = insets.left + getHgap();
                        y += rowHeight + getVgap();
                        rowHeight = 0;
                        firstInRow = true;
                    }

                    comp.setBounds(x, y, pref.width, pref.height);
                    x += pref.width + getHgap();
                    rowHeight = Math.max(rowHeight, pref.height);
                    firstInRow = false;
                }
            }
            
            // Update container size
            target.setPreferredSize(new Dimension(
                target.getWidth(), 
                y + rowHeight + insets.bottom + getVgap()
            ));
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        return layoutSize(target, false);
    }

    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            int targetWidth = target.getSize().width;
            if (targetWidth == 0) {
                targetWidth = Integer.MAX_VALUE;
            }

            int hgap = getHgap();
            int vgap = getVgap();
            Insets insets = target.getInsets();
            int maxWidth = targetWidth - (insets.left + insets.right + hgap * 2);

            Dimension dim = new Dimension(0, 0);
            int rowWidth = 0;
            int rowHeight = 0;

            int nmembers = target.getComponentCount();

            for (int i = 0; i < nmembers; i++) {
                Component m = target.getComponent(i);
                if (m.isVisible()) {
                    Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                    if (rowWidth + d.width > maxWidth) {
                        dim.width = Math.max(dim.width, rowWidth);
                        dim.height += rowHeight + vgap;
                        rowWidth = 0;
                        rowHeight = 0;
                    }
                    if (rowWidth != 0) {
                        rowWidth += hgap;
                    }
                    rowWidth += d.width;
                    rowHeight = Math.max(rowHeight, d.height);
                }
            }
            
            dim.width = Math.max(dim.width, rowWidth);
            dim.height += rowHeight;

            dim.width += insets.left + insets.right + hgap * 2;
            dim.height += insets.top + insets.bottom + vgap * 2;

            return dim;
        }
    }
}