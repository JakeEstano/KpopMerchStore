package OnlineShop;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import com.formdev.flatlaf.FlatDarkLaf;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

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
    private JComboBox<String> filterBox;

    public CustomerFrame(int customerId) {
        this.customerId = customerId;
        setTitle("HAMTEO - K-Pop Merch Store");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

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

        // Add the mainPanel to the frame
        add(mainPanel, BorderLayout.CENTER);

        // Create navigation bar
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
        JLabel logo = new JLabel("HAMTEO", SwingConstants.LEFT);
        logo.setFont(new Font("Arial", Font.BOLD, 24));
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

        // Animated promotion banner
        JPanel releasePanel = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                int preferredHeight = Math.min(getParent().getHeight() / 4, 200);
                return new Dimension(super.getPreferredSize().width, preferredHeight);
            }
        };
        releasePanel.setBackground(ThemeColors.CARD_BG);
        releasePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        try {
            // Try loading from file system first (for development)
            File imageFile = new File("src/images/promotional.gif");
            ImageIcon originalIcon;
            
            if (imageFile.exists()) {
                originalIcon = new ImageIcon(imageFile.getAbsolutePath());
            } else {
                // Fallback to classpath resource
                URL imageUrl = getClass().getResource("/images/promotional.gif");
                if (imageUrl == null) {
                    throw new FileNotFoundException("Image not found in resources");
                }
                originalIcon = new ImageIcon(imageUrl);
            }

            JLabel gifLabel = new JLabel(originalIcon) {
                @Override
                public Dimension getPreferredSize() {
                    int parentWidth = getParent().getWidth() - 20;
                    double aspectRatio = (double)originalIcon.getIconWidth() / originalIcon.getIconHeight();
                    int height = (int)(parentWidth / aspectRatio);
                    return new Dimension(parentWidth, height);
                }
            };
            gifLabel.setHorizontalAlignment(SwingConstants.CENTER);

            releasePanel.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    gifLabel.revalidate();
                    releasePanel.revalidate();
                }
            });

            gifLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            gifLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showCategory("Albums");
                }
            });

            releasePanel.add(gifLabel, BorderLayout.CENTER);
        } catch (Exception e) {
            System.err.println("Error loading promotional banner: " + e.getMessage());
            JLabel placeholderLabel = new JLabel("HAMTEO Special Offers", SwingConstants.CENTER);
            placeholderLabel.setFont(new Font("Arial", Font.BOLD, 24));
            placeholderLabel.setForeground(ThemeColors.PRIMARY);
            releasePanel.add(placeholderLabel, BorderLayout.CENTER);
        }

        homePanel.add(releasePanel, BorderLayout.NORTH);

        // Search and filter panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        searchPanel.setBackground(ThemeColors.BACKGROUND);
        
        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBackground(ThemeColors.CARD_BG);
        searchField.setForeground(ThemeColors.TEXT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.SECONDARY, 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        
        filterBox = new JComboBox<>(new String[]{"All", "Albums", "Lightsticks", "Goods", "Photocards"});
        filterBox.setFont(new Font("Arial", Font.PLAIN, 14));
        filterBox.setBackground(ThemeColors.CARD_BG);
        filterBox.setForeground(ThemeColors.TEXT);
        
        JButton searchButton = createStyledButton("Search", ThemeColors.PRIMARY);
        searchButton.addActionListener(e -> filterProducts());
        
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("Filter:"));
        searchPanel.add(filterBox);
        searchPanel.add(searchButton);
        
        homePanel.add(searchPanel, BorderLayout.CENTER);

        // K-pop Group Categories
        JPanel categoriesPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        categoriesPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        categoriesPanel.setBackground(ThemeColors.BACKGROUND);

        String[] groups = {"BTS", "BLACKPINK", "TWICE", "SEVENTEEN", "STRAY KIDS", "NEWJEANS"};
        for (String group : groups) {
            JButton groupButton = createGroupButton(group);
            categoriesPanel.add(groupButton);
        }

        homePanel.add(categoriesPanel, BorderLayout.SOUTH);

        return homePanel;
    }

    private JButton createGroupButton(String groupName) {
        JButton button = new JButton(groupName);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBackground(ThemeColors.CARD_BG);
        button.setForeground(ThemeColors.TEXT);
        button.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        button.addActionListener(e -> showGroupCategory(groupName));
        return button;
    }

    private void showGroupCategory(String groupName) {
        this.currentCategory = groupName;
        
        // Create a panel for the group's merchandise categories
        JPanel groupPanel = new JPanel(new BorderLayout());
        groupPanel.setBackground(ThemeColors.BACKGROUND);

        // Back button
        JButton backButton = new JButton("← Back to Home");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(ThemeColors.CARD_BG);
        backButton.setForeground(ThemeColors.TEXT);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        backButton.addActionListener(e -> showCard("Home"));
        groupPanel.add(backButton, BorderLayout.NORTH);

        // Group header
        JLabel groupHeader = new JLabel(groupName + " Merchandise", SwingConstants.CENTER);
        groupHeader.setFont(new Font("Arial", Font.BOLD, 24));
        groupHeader.setForeground(ThemeColors.PRIMARY);
        groupHeader.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        groupPanel.add(groupHeader, BorderLayout.NORTH);

        // Merchandise type categories
        JPanel merchTypesPanel = new JPanel(new GridLayout(1, 4, 20, 20));
        merchTypesPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        merchTypesPanel.setBackground(ThemeColors.BACKGROUND);

        String[] merchTypes = {"Albums", "Lightsticks", "Goods", "Photocards"};
        for (String type : merchTypes) {
            JButton typeButton = createMerchTypeButton(type, groupName);
            merchTypesPanel.add(typeButton);
        }

        groupPanel.add(merchTypesPanel, BorderLayout.CENTER);

        // Add this panel to mainPanel with a unique name
        mainPanel.add(groupPanel, groupName + "Group");
        
        // Show this panel
        cardLayout.show(mainPanel, groupName + "Group");
    }

    private JButton createMerchTypeButton(String merchType, String groupName) {
        JButton button = new JButton(merchType);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(ThemeColors.SECONDARY);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        button.addActionListener(e -> {
            // Load products for this group and merchandise type
            loadGroupProducts(groupName, merchType);
            cardLayout.show(mainPanel, "Products");
        });
        return button;
    }

    private void loadGroupProducts(String groupName, String merchType) {
        productGridPanel.removeAll();
        productGridPanel.setLayout(new GridLayout(0, 3, 20, 20));

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM products WHERE category = ? AND group_name = ?")) {
            
            stmt.setString(1, merchType);
            stmt.setString(2, groupName);
            ResultSet rs = stmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                count++;
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

        productGridPanel.revalidate();
        productGridPanel.repaint();
    }

    private JPanel createEventPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeColors.BACKGROUND);
        
        // Back button
        JButton backButton = new JButton("← Back to Home");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(ThemeColors.CARD_BG);
        backButton.setForeground(ThemeColors.TEXT);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        backButton.addActionListener(e -> showCard("Home"));
        panel.add(backButton, BorderLayout.NORTH);
        
        // Content
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
        
        // Back button
        JButton backButton = new JButton("← Back to Home");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(ThemeColors.CARD_BG);
        backButton.setForeground(ThemeColors.TEXT);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        backButton.addActionListener(e -> showCard("Home"));
        panel.add(backButton, BorderLayout.NORTH);
        
        // FAQ Content
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
        
        // Back button
        JButton backButton = new JButton("← Back to Home");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(ThemeColors.CARD_BG);
        backButton.setForeground(ThemeColors.TEXT);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        backButton.addActionListener(e -> showCard("Home"));
        panel.add(backButton, BorderLayout.NORTH);
        
        // Notices Content
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
        
        // Back button
        JButton backButton = new JButton("← Back to Home");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(ThemeColors.CARD_BG);
        backButton.setForeground(ThemeColors.TEXT);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        backButton.addActionListener(e -> showCard("Home"));
        panel.add(backButton, BorderLayout.NORTH);
        
        // About Content
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

    private JButton createCategoryButton(String category) {
        JButton button = new JButton(category);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBackground(ThemeColors.CARD_BG);
        button.setForeground(ThemeColors.TEXT);
        button.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        button.addActionListener(e -> showCategory(category));
        return button;
    }

    private JPanel createProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeColors.BACKGROUND);

        // Back button
        JButton backButton = new JButton("← Back to Home");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(ThemeColors.CARD_BG);
        backButton.setForeground(ThemeColors.TEXT);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        backButton.addActionListener(e -> showCard("Home"));
        panel.add(backButton, BorderLayout.NORTH);

        // Product grid - initialize with proper layout
        productGridPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        productGridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        productGridPanel.setBackground(ThemeColors.BACKGROUND);

        // Wrap in scroll pane
        JScrollPane scrollPane = new JScrollPane(productGridPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createProductCard(String name, String category, double price, int id, String description) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(ThemeColors.CARD_BG);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(ThemeColors.TEXT);
        card.add(nameLabel, BorderLayout.NORTH);

        JLabel priceLabel = new JLabel(String.format("$%.2f", price), SwingConstants.CENTER);
        priceLabel.setFont(new Font("Arial", Font.BOLD, 18));
        priceLabel.setForeground(ThemeColors.PRIMARY);
        card.add(priceLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setOpaque(false);
        
        JButton detailsButton = new JButton("Details");
        detailsButton.setFont(new Font("Arial", Font.BOLD, 12));
        detailsButton.setBackground(ThemeColors.SECONDARY);
        detailsButton.setForeground(Color.WHITE);
        detailsButton.addActionListener(e -> showProductDetails(id, name, price, description));
        
        JButton addToCartButton = new JButton("Add to Cart");
        addToCartButton.setFont(new Font("Arial", Font.BOLD, 12));
        addToCartButton.setBackground(ThemeColors.PRIMARY);
        addToCartButton.setForeground(Color.WHITE);
        addToCartButton.addActionListener(e -> addToCart(id, name, price));
        
        buttonPanel.add(detailsButton);
        buttonPanel.add(addToCartButton);
        
        card.add(buttonPanel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeColors.BACKGROUND);

        // Back button
        JButton backButton = new JButton("← Back to Home");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(ThemeColors.CARD_BG);
        backButton.setForeground(ThemeColors.TEXT);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        backButton.addActionListener(e -> showCard("Home"));
        panel.add(backButton, BorderLayout.NORTH);

        // Cart title
        JLabel title = new JLabel("Your Shopping Cart", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(ThemeColors.PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(title, BorderLayout.NORTH);

        // Cart table
        cartTableModel = new DefaultTableModel(new Object[]{"Product", "Price", "Quantity", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; // Only quantity column is editable
            }
        };
        cartTable = new JTable(cartTableModel);
        styleTable(cartTable);
        
        // Add listener for quantity changes
        cartTable.getModel().addTableModelListener(e -> {
            if (e.getColumn() == 2) { // Quantity column
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

        // Checkout panel
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

        // Back button
        JButton backButton = new JButton("← Back to Home");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(ThemeColors.CARD_BG);
        backButton.setForeground(ThemeColors.TEXT);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        backButton.addActionListener(e -> showCard("Home"));
        panel.add(backButton, BorderLayout.NORTH);

        // Wishlist title
        JLabel title = new JLabel("Your Wishlist", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(ThemeColors.PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(title, BorderLayout.NORTH);

        // Wishlist table
        wishlistTableModel = new DefaultTableModel(new Object[]{"Product", "Price"}, 0);
        wishlistTable = new JTable(wishlistTableModel);
        styleTable(wishlistTable);

        JScrollPane scrollPane = new JScrollPane(wishlistTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
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

        // Back button
        JButton backButton = new JButton("← Back to Home");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(ThemeColors.CARD_BG);
        backButton.setForeground(ThemeColors.TEXT);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        backButton.addActionListener(e -> showCard("Home"));
        panel.add(backButton, BorderLayout.NORTH);

        // Orders title
        JLabel title = new JLabel("Your Orders", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(ThemeColors.PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(title, BorderLayout.NORTH);

        // Orders table
        orderTableModel = new DefaultTableModel(new Object[]{"Order ID", "Product", "Quantity", "Status", "Date"}, 0);
        orderTable = new JTable(orderTableModel);
        styleTable(orderTable);

        JScrollPane scrollPane = new JScrollPane(orderTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Add review button
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

    private void showProductDetails(int productId, String name, double price, String description) {
        JDialog detailsDialog = new JDialog(this, "Product Details", true);
        detailsDialog.setSize(400, 300);
        detailsDialog.setLayout(new BorderLayout());
        detailsDialog.getContentPane().setBackground(ThemeColors.BACKGROUND);

        JPanel detailsPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        detailsPanel.setBackground(ThemeColors.BACKGROUND);

        // Product info
        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        nameLabel.setForeground(ThemeColors.PRIMARY);
        
        JLabel priceLabel = new JLabel(String.format("Price: $%.2f", price), SwingConstants.CENTER);
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        priceLabel.setForeground(ThemeColors.TEXT);
        
        JLabel descLabel = new JLabel("Description: " + description, SwingConstants.CENTER);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        descLabel.setForeground(ThemeColors.TEXT);
        
        detailsPanel.add(nameLabel);
        detailsPanel.add(priceLabel);
        detailsPanel.add(descLabel);

        // Buttons
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
        
        detailsPanel.add(buttonPanel);
        detailsDialog.add(detailsPanel, BorderLayout.CENTER);
        detailsDialog.setLocationRelativeTo(this);
        detailsDialog.setVisible(true);
    }

    private void showCategory(String category) {
        this.currentCategory = category;
        loadProductsFromDatabase(category);
        cardLayout.show(mainPanel, "Products");
    }

    private void loadProductsFromDatabase(String category) {
        productGridPanel.removeAll();
        productGridPanel.setLayout(new GridLayout(0, 3, 20, 20)); // Ensure layout is set

        System.out.println("Loading products for category: " + category); // Debug

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM products WHERE category = ? OR ? = 'All'")) {

            stmt.setString(1, category);
            stmt.setString(2, category);
            ResultSet rs = stmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                count++;
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String productCategory = rs.getString("category");
                double price = rs.getDouble("price");
                String description = rs.getString("description");

                System.out.println("Found product: " + name); // Debug

                JPanel productCard = createProductCard(name, productCategory, price, id, description);
                productGridPanel.add(productCard);
            }

            System.out.println("Loaded " + count + " products"); // Debug

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading products: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }

        productGridPanel.revalidate();
        productGridPanel.repaint();
        System.out.println("Revalidated and repainted product grid"); // Debug
    }

    private void filterProducts() {
        String searchText = searchField.getText().toLowerCase();
        String selectedCategory = (String) filterBox.getSelectedItem();
        
        productGridPanel.removeAll();
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM products WHERE (LOWER(name) LIKE ? OR LOWER(description) LIKE ?) " +
                 "AND (category = ? OR ? = 'All')")) {
            
            stmt.setString(1, "%" + searchText + "%");
            stmt.setString(2, "%" + searchText + "%");
            stmt.setString(3, selectedCategory);
            stmt.setString(4, selectedCategory);
            ResultSet rs = stmt.executeQuery();
            
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
            JOptionPane.showMessageDialog(this, "Error filtering products", "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        productGridPanel.revalidate();
        productGridPanel.repaint();
    }

    private void showCard(String cardName) {
        cardLayout.show(mainPanel, cardName);
        currentCard = cardName;
    }

    private void addToCart(int productId, String name, double price) {
        // Check if product already in cart
        for (int i = 0; i < cartTableModel.getRowCount(); i++) {
            if (cartTableModel.getValueAt(i, 0).equals(name)) {
                int currentQty = (Integer) cartTableModel.getValueAt(i, 2);
                cartTableModel.setValueAt(currentQty + 1, i, 2);
                cartTableModel.setValueAt(price * (currentQty + 1), i, 3);
                JOptionPane.showMessageDialog(this, "Product quantity updated in cart!");
                return;
            }
        }
        
        // Add new product to cart
        cartTableModel.addRow(new Object[]{
            name,
            price,
            1,
            price
        });
        JOptionPane.showMessageDialog(this, "Product added to cart!");
    }

    private void addToWishlist(int productId, String name, double price) {
        // Check if product already in wishlist
        for (int i = 0; i < wishlistTableModel.getRowCount(); i++) {
            if (wishlistTableModel.getValueAt(i, 0).equals(name)) {
                JOptionPane.showMessageDialog(this, "Product is already in your wishlist!");
                return;
            }
        }
        
        // Add new product to wishlist
        wishlistTableModel.addRow(new Object[]{
            name,
            price
        });
        JOptionPane.showMessageDialog(this, "Product added to wishlist!");
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
        if (selectedRow >= 0) {
            String product = (String) wishlistTableModel.getValueAt(selectedRow, 0);
            double price = (Double) wishlistTableModel.getValueAt(selectedRow, 1);
            cartTableModel.addRow(new Object[]{product, price, 1, price});
            wishlistTableModel.removeRow(selectedRow);
            JOptionPane.showMessageDialog(this, "Product moved to cart!");
        } else {
            JOptionPane.showMessageDialog(this, "Please select a product to move", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCartItems() {
        cartTableModel.setRowCount(0);
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT p.name, p.price, c.quantity FROM cart c " +
                 "JOIN products p ON c.product_id = p.id " +
                 "WHERE c.customer_id = ?")) {
            
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                int quantity = rs.getInt("quantity");
                
                cartTableModel.addRow(new Object[]{
                    name,
                    price,
                    quantity,
                    price * quantity
                });
            }
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading cart items", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadWishlist() {
        wishlistTableModel.setRowCount(0);
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT p.name, p.price FROM wishlist w " +
                 "JOIN products p ON w.product_id = p.id " +
                 "WHERE w.customer_id = ?")) {
            
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                
                wishlistTableModel.addRow(new Object[]{
                    name,
                    price
                });
            }
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading wishlist", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadOrders() {
        orderTableModel.setRowCount(0);
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT o.id, p.name, o.quantity, o.status, o.order_date " +
                 "FROM orders o JOIN products p ON o.product_id = p.id " +
                 "WHERE o.customer_id = ? ORDER BY o.order_date DESC")) {
            
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String orderId = "ORD" + rs.getInt("id");
                String productName = rs.getString("name");
                int quantity = rs.getInt("quantity");
                String status = rs.getString("status");
                String date = rs.getDate("order_date").toString();
                
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
            JOptionPane.showMessageDialog(this, "Error loading orders", "Error", JOptionPane.ERROR_MESSAGE);
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

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        new CustomerFrame(1); // Sample customer ID
    }
}