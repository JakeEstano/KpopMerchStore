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
    private JLabel loyaltyPointsLabel;
    private String currentCategory;
    private String currentCard;

    // Navigation buttons
    private JButton shopButton, eventButton, faqButton, noticeButton, aboutButton;
    
    // Cart components
    private DefaultTableModel cartTableModel;
    private JTable cartTable;
    private JPanel cartPanel;
    
    // Wishlist components
    private DefaultTableModel wishlistTableModel;
    private JTable wishlistTable;
    private JPanel wishlistPanel;
    
    // Order tracking
    private DefaultTableModel orderTableModel;
    private JTable orderTable;
    
    // Product components
    private JPanel productsPanel;
    private JPanel productGridPanel;
    private JTextField searchField;
    

    public CustomerFrame(int customerId) {
        this.customerId = customerId;
        setTitle("케이팝 상점  - K-Pop Merch Store");
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

        // CardLayout for switching between views
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(ThemeColors.BACKGROUND);

        // Create all panels
        JPanel homePanel = createHomePanel();
        this.productsPanel = createProductsPanel();
        this.cartPanel = createCartPanel();
        JPanel orderTrackingPanel = createOrderTrackingPanel();
        this.wishlistPanel = createWishlistPanel();
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

        loadLoyaltyPoints();
        
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

        loyaltyPointsLabel = new JLabel("Loyalty: 0 pts");
        loyaltyPointsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        loyaltyPointsLabel.setForeground(ThemeColors.TEXT);
        userPanel.add(loyaltyPointsLabel);

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
        productGridPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 20, 20)) {
            @Override
            public Dimension getPreferredSize() {
                // Calculate preferred size based on content
                LayoutManager layout = getLayout();
                if (layout instanceof WrapLayout) {
                    return ((WrapLayout)layout).preferredLayoutSize(this);
                }
                return super.getPreferredSize();
            }
        };
        productGridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        productGridPanel.setBackground(ThemeColors.BACKGROUND);

        JScrollPane scrollPane = new JScrollPane(productGridPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);

        // Add this to ensure proper scrolling behavior
        scrollPane.getViewport().addChangeListener(e -> {
            productGridPanel.revalidate();
            productGridPanel.repaint();
        });

        homePanel.add(scrollPane, BorderLayout.CENTER);
        loadAllProducts();
        return homePanel;
    }

    private void loadAllProducts() {
    productGridPanel.removeAll();
    productGridPanel.setLayout(new GridLayout(0, 4, 20, 20)); // Same grid layout as search
    productGridPanel.setBackground(ThemeColors.BACKGROUND);

    try (Connection conn = DBConnection.connect();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT * FROM products ORDER BY name")) {

        while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            String category = rs.getString("category");
            double price = rs.getDouble("price");
            String description = rs.getString("description");

            JPanel productCard = createProductCard(name, category, price, id, description);
            productGridPanel.add(productCard);
        }

    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, 
            "Error loading products: " + ex.getMessage(), 
            "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    // Force complete UI refresh
    productGridPanel.revalidate();
    productGridPanel.repaint();
    this.revalidate();
    this.repaint();
}

    private void filterProducts() {
        String searchText = searchField.getText().trim().toLowerCase();

        // Completely reset the product grid
        productGridPanel.removeAll();
        productGridPanel.setLayout(new GridLayout(0, 4, 20, 20)); // Simple grid layout
        productGridPanel.setBackground(ThemeColors.BACKGROUND);

        if (searchText.isEmpty()) {
            loadAllProducts();
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
                String category = rs.getString("category");
                double price = rs.getDouble("price");
                String description = rs.getString("description");

                JPanel productCard = createProductCard(name, category, price, id, description);
                productGridPanel.add(productCard);
            }

            if (!hasResults) {
                productGridPanel.setLayout(new BorderLayout());
                JLabel noResults = new JLabel("No products found matching '" + searchText + "'", SwingConstants.CENTER);
                noResults.setFont(new Font("Arial", Font.PLAIN, 16));
                noResults.setForeground(ThemeColors.TEXT);
                productGridPanel.add(noResults, BorderLayout.CENTER);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error searching products: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Force complete UI refresh
        productGridPanel.revalidate();
        productGridPanel.repaint();
        this.revalidate();
        this.repaint();
    }

    private JPanel createProductCard(String name, String category, double price, int id, String description) {
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

        JLabel priceLabel = new JLabel(String.format("$%.2f", price));
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
        
        JLabel priceLabel = new JLabel(String.format("Price: $%.2f", price), SwingConstants.CENTER);
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

        productGridPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 20, 20));
        productGridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        productGridPanel.setBackground(ThemeColors.BACKGROUND);

        JScrollPane scrollPane = new JScrollPane(productGridPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeColors.BACKGROUND);

        JButton backButton = new JButton("← Back to Home");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(ThemeColors.CARD_BG);
        backButton.setForeground(ThemeColors.TEXT);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        backButton.addActionListener(e -> showCard("Home"));
        panel.add(backButton, BorderLayout.NORTH);

        JLabel title = new JLabel("Your Shopping Cart", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(ThemeColors.PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(title, BorderLayout.NORTH);

        cartTableModel = new DefaultTableModel(new Object[]{"Product", "Price", "Quantity", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2;
            }
        };
        cartTable = new JTable(cartTableModel);
        styleTable(cartTable);
        
        cartTable.getModel().addTableModelListener(e -> {
            if (e.getColumn() == 2) {
                int row = e.getFirstRow();
                try {
                    int quantity = Integer.parseInt(cartTableModel.getValueAt(row, 2).toString());
                    double price = (Double) cartTableModel.getValueAt(row, 1);
                    cartTableModel.setValueAt(price * quantity, row, 3);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid quantity", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(cartTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel checkoutPanel = new JPanel(new BorderLayout());
        checkoutPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        checkoutPanel.setBackground(ThemeColors.CARD_BG);
        
        JLabel totalLabel = new JLabel("Total: $0.00", SwingConstants.RIGHT);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalLabel.setForeground(ThemeColors.TEXT);
        checkoutPanel.add(totalLabel, BorderLayout.CENTER);
        
        JButton checkoutButton = new JButton("CHECKOUT");
        checkoutButton.setFont(new Font("Arial", Font.BOLD, 16));
        checkoutButton.setBackground(ThemeColors.PRIMARY);
        checkoutButton.setForeground(Color.WHITE);
        checkoutButton.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        checkoutButton.addActionListener(e -> checkout());
        checkoutPanel.add(checkoutButton, BorderLayout.EAST);
        
        panel.add(checkoutPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createWishlistPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeColors.BACKGROUND);

        JButton backButton = new JButton("← Back to Home");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(ThemeColors.CARD_BG);
        backButton.setForeground(ThemeColors.TEXT);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        backButton.addActionListener(e -> showCard("Home"));
        panel.add(backButton, BorderLayout.NORTH);

        JLabel title = new JLabel("Your Wishlist", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(ThemeColors.PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(title, BorderLayout.NORTH);

        wishlistTableModel = new DefaultTableModel(new Object[]{"Product", "Price"}, 0);
        wishlistTable = new JTable(wishlistTableModel);
        styleTable(wishlistTable);

        JScrollPane scrollPane = new JScrollPane(wishlistTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);
        
        JButton removeButton = createStyledButton("Remove", ThemeColors.SECONDARY);
        removeButton.addActionListener(e -> removeFromWishlist());
        
        JButton moveToCartButton = createStyledButton("Move to Cart", ThemeColors.PRIMARY);
        moveToCartButton.addActionListener(e -> moveToCartFromWishlist());
        
        buttonPanel.add(removeButton);
        buttonPanel.add(moveToCartButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createOrderTrackingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeColors.BACKGROUND);

        JButton backButton = new JButton("← Back to Home");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(ThemeColors.CARD_BG);
        backButton.setForeground(ThemeColors.TEXT);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        backButton.addActionListener(e -> showCard("Home"));
        panel.add(backButton, BorderLayout.NORTH);

        JLabel title = new JLabel("Your Orders", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(ThemeColors.PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(title, BorderLayout.NORTH);

        orderTableModel = new DefaultTableModel(new Object[]{"Order ID", "Product", "Quantity", "Status", "Date"}, 0);
        orderTable = new JTable(orderTableModel);
        styleTable(orderTable);

        JScrollPane scrollPane = new JScrollPane(orderTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton reviewButton = createStyledButton("Leave Review", ThemeColors.PRIMARY);
        reviewButton.addActionListener(e -> {
            int selectedRow = orderTable.getSelectedRow();
            if (selectedRow >= 0) {
                new ProductReviewFrame(customerId);
            } else {
                JOptionPane.showMessageDialog(this, "Please select an order to review", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(reviewButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
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
            System.out.println("Cart rows affected: " + rowsAffected);

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Product added to cart!");
                loadCartItems(); // Refresh the view
            }
        } catch (SQLException ex) {
            System.err.println("Cart SQL Error: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                "Error updating cart: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
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
            System.out.println("Wishlist rows affected: " + rowsAffected);

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Product added to wishlist!");
                // Add to UI immediately
                DefaultTableModel model = (DefaultTableModel) wishlistTable.getModel();
                model.addRow(new Object[]{name, price});
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
    

    private void removeFromWishlist() {
        int selectedRow = wishlistTable.getSelectedRow();
        if (selectedRow >= 0) {
            wishlistTableModel.removeRow(selectedRow);
            JOptionPane.showMessageDialog(this, "Product removed from wishlist!");
        } else {
            JOptionPane.showMessageDialog(this, "Please select a product to remove", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void moveToCartFromWishlist() {
        int selectedRow = wishlistTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Please select a product to move", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String productName = (String) wishlistTableModel.getValueAt(selectedRow, 0);
        double price = (Double) wishlistTableModel.getValueAt(selectedRow, 1);

        try (Connection conn = DBConnection.connect()) {
            conn.setAutoCommit(false); // Start transaction

            try {
                // 1. Get product ID from wishlist
                int productId = getProductIdByName(conn, productName);
                if (productId == -1) throw new SQLException("Product not found");

                // 2. Remove from wishlist
                String deleteSql = "DELETE FROM wishlist WHERE customer_id = ? AND product_id = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                    deleteStmt.setInt(1, customerId);
                    deleteStmt.setInt(2, productId);
                    int deleted = deleteStmt.executeUpdate();
                    if (deleted == 0) throw new SQLException("Failed to remove from wishlist");
                }

                // 3. Add to cart (or update quantity if exists)
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

                // 4. Update both UIs
                wishlistTableModel.removeRow(selectedRow);
                loadCartItems();

                JOptionPane.showMessageDialog(this, 
                    "Product moved to cart successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);

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

    // Helper method to get product ID by name
    private int getProductIdByName(Connection conn, String productName) throws SQLException {
        String sql = "SELECT id FROM products WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productName);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("id") : -1;
        }
    }

    private void loadCartItems() {
        cartTableModel.setRowCount(0);
        String sql = "SELECT p.name, p.price, c.quantity " +
                    "FROM cart c JOIN products p ON c.product_id = p.id " +
                    "WHERE c.customer_id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                cartTableModel.addRow(new Object[]{
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getInt("quantity"),
                    rs.getDouble("price") * rs.getInt("quantity")
                });
            }
        } catch (SQLException ex) {
            System.err.println("Error loading cart: " + ex.getMessage());
        }
    }

    private void loadWishlist() {
        wishlistTableModel.setRowCount(0);
        String sql = "SELECT p.name, p.price " +
                    "FROM wishlist w JOIN products p ON w.product_id = p.id " +
                    "WHERE w.customer_id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                wishlistTableModel.addRow(new Object[]{
                    rs.getString("name"),
                    rs.getDouble("price")
                });
            }
        } catch (SQLException ex) {
            System.err.println("Error loading wishlist: " + ex.getMessage());
        }
    }

    private void loadOrders() {
        orderTableModel.setRowCount(0);

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT o.id, p.name, oi.quantity, o.status, o.order_date " +
                 "FROM orders o " +
                 "JOIN order_items oi ON o.id = oi.order_id " +
                 "JOIN products p ON oi.product_id = p.id " +
                 "WHERE o.customer_id = ? ORDER BY o.order_date DESC")) {

            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String orderId = "ORD" + rs.getInt("id");
                String productName = rs.getString("name");
                int quantity = rs.getInt("quantity");
                String status = rs.getString("status");
                String date = rs.getTimestamp("order_date").toString();

                orderTableModel.addRow(new Object[]{
                    orderId,
                    productName,
                    quantity,
                    status,
                    date
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading orders: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadLoyaltyPoints() {
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT loyalty_points FROM customers WHERE id = ?")) {
            
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int loyalty_points = rs.getInt("loyalty_points");
                loyaltyPointsLabel.setText("Loyalty: " + loyalty_points + " pts");
            }
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            loyaltyPointsLabel.setText("Loyalty: 0 pts");
        }
    }

    private void checkout() {
        if (cartTableModel.getRowCount() > 0) {
            new PaymentFrame(customerId);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Your cart is empty!", "Error", JOptionPane.ERROR_MESSAGE);
        }
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