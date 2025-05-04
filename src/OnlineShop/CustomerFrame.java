package OnlineShop;

// Keep existing imports
import OnlineShop.AddressManager;
import OnlineShop.LoginFrame;
// Removed import OnlineShop.PaymentFrame; // No longer needed directly from here
import OnlineShop.ProductReviewFrame;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException; // Added for file operations
import java.net.URL;
import java.nio.file.Files; // Added for file operations
import java.nio.file.Path; // Added for file operations
import java.nio.file.Paths; // Added for file operations
import java.nio.file.StandardCopyOption; // Added for file operations
import java.text.SimpleDateFormat;
import java.time.LocalDateTime; // Import LocalDateTime
import java.util.List;
import java.util.ArrayList; // Explicit import
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.filechooser.FileNameExtensionFilter; // Added for file chooser
import javax.swing.JLayeredPane;
import javax.swing.border.TitledBorder;
import java.util.Set; // Import Set
import java.util.HashSet; // Import HashSet
import java.util.Arrays; // Import Arrays for List creation

// --- THEME: Ensure the external ThemeColors is imported ---
import OnlineShop.ThemeColors;
import javax.swing.border.Border;
import OnlineShop.AddressManager.Address; // Explicitly import Address inner class
import OnlineShop.CheckoutFrame.AddressManagementDialog; // Import the static inner class


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

    // --- Updated User Buttons ---
    private JComponent cartButtonContainer, wishlistButtonContainer, ordersButtonContainer,
            notificationButtonContainer,
            profileButtonContainer; // JLayeredPane for icons/badges
    // *** Logout button is now directly a JButton ***
    private JButton logoutButtonContainer; // Changed type
    // --- END UPDATE ---

    // --- Notification Components ---
    private JPopupMenu notificationPopup;
    private JTabbedPane notificationTabbedPane;
    private JPanel unreadNotificationsPanel;
    private JPanel readNotificationsPanel;
    private int notificationCount = 0; // Track UNREAD notification count
    private Set<Integer> justReadOrderIds = new HashSet<>(); // Track IDs marked read in current session view
    // --- End Notification Components ---

    // AddressManager is no longer needed here for selection, but maybe for profile? Keeping it for now.
    private AddressManager addressManager;

    public CustomerFrame(int customerId) {
        // Set window properties before making it displayable
        setTitle("케이팝 상점 - K-Pop Merch Store");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Keep EXIT if this is the main app window
        setLayout(new BorderLayout());

        // --- START MODIFICATION: Full Screen and Undecorated ---
        setUndecorated(true); // Remove window title bar and borders
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximize the frame
        // --- END MODIFICATION ---

        // Initialize instance variables
        this.customerId = customerId;
        // Initialize AddressManager (needed for profile address display/edit if implemented)
        this.addressManager = new AddressManager(this, customerId);
        this.currentCard = "Home";

        // Initialize main components
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(ThemeColors.BACKGROUND); // Using external ThemeColors

        // Initialize product grid panels (one for home, one for products page)
        homeProductGridPanel = new JPanel(new WrapLayout(FlowLayout.CENTER, 20, 20));
        homeProductGridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        homeProductGridPanel.setBackground(ThemeColors.BACKGROUND); // Using external ThemeColors

        productsProductGridPanel = new JPanel(new WrapLayout(FlowLayout.CENTER, 20, 20));
        productsProductGridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        productsProductGridPanel.setBackground(ThemeColors.BACKGROUND); // Using external ThemeColors

        // --- Initialize Notification Popup & Tabs ---
        notificationPopup = new JPopupMenu();
        notificationPopup.setBackground(ThemeColors.CARD_BG);
        notificationPopup.setBorder(BorderFactory.createLineBorder(ThemeColors.SECONDARY, 1));

        notificationTabbedPane = new JTabbedPane();
        notificationTabbedPane.setFocusable(false); // Remove focus highlight
        notificationTabbedPane.setBackground(ThemeColors.CARD_BG);
        notificationTabbedPane.setForeground(ThemeColors.TEXT);
        // Customize Tab Colors (Optional but recommended for theme consistency)
        UIManager.put("TabbedPane.selected", ThemeColors.PRIMARY.darker()); // Active tab bg
        UIManager.put("TabbedPane.foreground", ThemeColors.TEXT); // Text color of inactive tabs
        UIManager.put("TabbedPane.contentAreaColor", ThemeColors.CARD_BG); // BG behind tabs
        UIManager.put("TabbedPane.selectedForeground", Color.WHITE); // Text color of active tab
        UIManager.put("TabbedPane.borderColor", ThemeColors.SECONDARY);
        UIManager.put("TabbedPane.darkShadow", ThemeColors.SECONDARY);
        UIManager.put("TabbedPane.light", ThemeColors.BACKGROUND);
        UIManager.put("TabbedPane.focus", ThemeColors.PRIMARY); // Color when tab has focus (if focusable)

        // Unread Panel
        unreadNotificationsPanel = new JPanel();
        unreadNotificationsPanel.setLayout(new BoxLayout(unreadNotificationsPanel, BoxLayout.Y_AXIS));
        unreadNotificationsPanel.setBackground(ThemeColors.CARD_BG);
        unreadNotificationsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane unreadScrollPane = new JScrollPane(unreadNotificationsPanel);
        styleScrollPane(unreadScrollPane); // Apply custom scrollbar style
        unreadScrollPane.setPreferredSize(new Dimension(350, 260)); // Adjust size
        unreadScrollPane.setBorder(null);
        unreadScrollPane.getViewport().setBackground(ThemeColors.CARD_BG);

        // Read Panel
        readNotificationsPanel = new JPanel();
        readNotificationsPanel.setLayout(new BoxLayout(readNotificationsPanel, BoxLayout.Y_AXIS));
        readNotificationsPanel.setBackground(ThemeColors.CARD_BG);
        readNotificationsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane readScrollPane = new JScrollPane(readNotificationsPanel);
        styleScrollPane(readScrollPane); // Apply custom scrollbar style
        readScrollPane.setPreferredSize(new Dimension(350, 260)); // Adjust size
        readScrollPane.setBorder(null);
        readScrollPane.getViewport().setBackground(ThemeColors.CARD_BG);

        // Add tabs
        notificationTabbedPane.addTab("Unread", unreadScrollPane);
        notificationTabbedPane.addTab("Read", readScrollPane);

        // Set tab text colors (can be customized further if needed)
        notificationTabbedPane.setForegroundAt(0, ThemeColors.PRIMARY); // Unread
        notificationTabbedPane.setForegroundAt(1, ThemeColors.TEXT); // Read

        // Add the tabbed pane to the popup
        notificationPopup.add(notificationTabbedPane);
        // --- End Notification Popup & Tabs Init ---

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
        add(createNavigationBar(), BorderLayout.NORTH); // Create navigation bar AFTER initializing button containers

        // Load data after UI is fully initialized
        SwingUtilities.invokeLater(() -> {
            loadAllProducts(); // Load products for both home and products panel initially
            // Initial badge update after UI is set up
            updateCartBadge();
            updateWishlistBadge();
            updateOrderBadge();
            // Fetch the initial unread count from DB for the notification badge
            this.notificationCount = getNotificationCount();
            updateNotificationBadge(); // Ensures initial badge count is displayed (based on unread)
        });

        //setLocationRelativeTo(null); // No longer needed with MAXIMIZED_BOTH
        setVisible(true); // Make visible AFTER setting undecorated and adding components
    }

    // --- createNavigationBar (Updated type hints for logout button) ---
    private JPanel createNavigationBar() {
        JPanel navBar = new JPanel(new GridBagLayout()); // Use GridBagLayout for main navbar
        navBar.setBackground(ThemeColors.BACKGROUND);
        navBar.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20)); // Adjusted padding
        GridBagConstraints gbc = new GridBagConstraints();

        // Logo (West)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 20);
        JLabel logo = new JLabel("케이팝 상점 ");
        logo.setFont(new Font("Malgun Gothic", Font.BOLD, 24));
        logo.setForeground(ThemeColors.PRIMARY);
        navBar.add(logo, gbc);

        // --- Main navigation buttons Panel (Center) ---
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.8;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 20, 0, 20);
        JPanel navButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 5));
        navButtonsPanel.setOpaque(false);

        shopButton = createNavButton("SHOP");
        eventButton = createNavButton("EVENT");
        faqButton = createNavButton("FAQ");
        noticeButton = createNavButton("NOTICE");
        aboutButton = createNavButton("ABOUT US");
        List<JButton> mainNavButtons = Arrays.asList(shopButton, eventButton, faqButton, noticeButton, aboutButton);

        mainNavButtons.forEach(navButtonsPanel::add);

        navBar.add(navButtonsPanel, gbc);

        shopButton.addActionListener(e -> showCard("Home"));
        eventButton.addActionListener(e -> showCard("Events"));
        faqButton.addActionListener(e -> showCard("FAQ"));
        noticeButton.addActionListener(e -> showCard("Notices"));
        aboutButton.addActionListener(e -> showCard("About"));

        // --- User buttons Panel (East) ---
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 10, 0, 0);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 5));
        userPanel.setOpaque(false);

        // Create ActionListeners
        ActionListener cartAction = e -> { loadCartItems(); showCard("Cart"); };
        ActionListener wishlistAction = e -> { loadWishlist(); showCard("Wishlist"); };
        ActionListener ordersAction = e -> { loadOrders(); showCard("Orders"); };
        ActionListener notificationAction = e -> showNotifications();
        ActionListener profileAction = e -> showProfileDialog();
        ActionListener logoutAction = e -> logout();

        // Create User Button Components
        cartButtonContainer = createBadgeButtonWithCounter("CART", 0, cartAction);
        wishlistButtonContainer = createBadgeButtonWithCounter("WISHLIST", 0, wishlistAction);
        ordersButtonContainer = createBadgeButtonWithCounter("ORDERS", 0, ordersAction);
        notificationButtonContainer = createIconButtonWithBadge("\uD83D\uDD14", 0, notificationAction); // Bell Emoji
        profileButtonContainer = createIconButton("\uD83D\uDC64", profileAction); // User Profile Emoji
        // *** Create logout button directly ***
        this.logoutButtonContainer = createUserButton("LOGOUT", logoutAction); // Store the JButton

        // *** Use Component as common superclass for the list ***
        List<Component> userComponents = Arrays.asList(
                cartButtonContainer, wishlistButtonContainer, ordersButtonContainer,
                notificationButtonContainer, profileButtonContainer, this.logoutButtonContainer // Add the JButton
        );

        // Add user buttons directly to the FlowLayout panel
        for (Component comp : userComponents) {
             userPanel.add(comp);
        }

        navBar.add(userPanel, gbc);

        return navBar;
    }
    // --- END UPDATED createNavigationBar ---

    // --- UPDATED HELPER: createIconButton (no image loading, emoji only) ---
    private JComponent createIconButton(String emojiText, ActionListener actionListener) {
        JButton actualButton = new JButton(emojiText); // Use emoji directly
        actualButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20)); // Use emoji font, adjust size
        actualButton.setForeground(ThemeColors.TEXT);
        actualButton.setContentAreaFilled(false);
        actualButton.setBorderPainted(false);
        actualButton.setFocusPainted(false);
        actualButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        actualButton.setOpaque(false);
        actualButton.setMargin(new Insets(0, 0, 0, 0)); // No margin

        // Subtle hover (optional: change color)
        actualButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { actualButton.setForeground(ThemeColors.PRIMARY); }
            @Override public void mouseExited(MouseEvent e) { actualButton.setForeground(ThemeColors.TEXT); }
        });

        if (actionListener != null) {
            actualButton.addActionListener(actionListener);
        }

        // Calculate preferred size based on emoji
        Dimension buttonPrefSize = actualButton.getPreferredSize();
        int padding = 8; // Add small padding around the emoji
        Dimension containerSize = new Dimension(buttonPrefSize.width + padding, buttonPrefSize.height + padding);

        // Wrap in JLayeredPane for consistent structure with badge buttons
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setOpaque(false);
        layeredPane.setPreferredSize(containerSize); // LayeredPane size based on calculation
        layeredPane.setMinimumSize(containerSize);
        layeredPane.setMaximumSize(containerSize); // Set max size too

        // Center button in LayeredPane
        int buttonX = (containerSize.width - buttonPrefSize.width) / 2;
        int buttonY = (containerSize.height - buttonPrefSize.height) / 2; // Center vertically
        actualButton.setBounds(buttonX, buttonY, buttonPrefSize.width, buttonPrefSize.height);
        layeredPane.add(actualButton, JLayeredPane.DEFAULT_LAYER);

        return layeredPane;
    }
    // --- END createIconButton ---

    // --- UPDATED: createIconButtonWithBadge (no image loading, emoji only) ---
     private JComponent createIconButtonWithBadge(String emojiText, int count, ActionListener actionListener) {
        // 1. Create the actual button with an emoji
        JButton actualButton = new JButton(emojiText);
        actualButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20)); // Use emoji font
        actualButton.setForeground(ThemeColors.TEXT);
        actualButton.setContentAreaFilled(false);
        actualButton.setBorderPainted(false);
        actualButton.setFocusPainted(false);
        actualButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        actualButton.setOpaque(false);
        actualButton.setMargin(new Insets(0, 0, 0, 0));

        // Add hover effect
        actualButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { actualButton.setForeground(ThemeColors.PRIMARY); }
            @Override public void mouseExited(MouseEvent e) { actualButton.setForeground(ThemeColors.TEXT); }
        });

        if (actionListener != null) {
            actualButton.addActionListener(actionListener);
        }

        // Calculate button size
        Dimension buttonPrefSize = actualButton.getPreferredSize();
        int padding = 4; // Small padding around the emoji
        Dimension paddedButtonSize = new Dimension(buttonPrefSize.width + padding, buttonPrefSize.height + padding);


        // 2. Create the badge label
        JLabel badgeLabel = null;
        Dimension badgeSize = new Dimension(0, 0);
        int badgeOffsetX = 4; // Fine-tune horizontal offset
        int badgeOffsetY = 0; // Fine-tune vertical offset

        if (count > 0) {
            String countText = count > 9 ? "9+" : String.valueOf(count);
            badgeLabel = new JLabel(countText, SwingConstants.CENTER) {
                 @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillOval(1, 1, getWidth()-2, getHeight()-2); // Draw oval background
                    g2.dispose();
                    super.paintComponent(g);
                }
                @Override public Dimension getPreferredSize() {
                    Dimension size = super.getPreferredSize();
                    int diameter = Math.max(size.width, size.height) + 4; // Padding inside oval
                    diameter = Math.max(diameter, 16); // Minimum size
                    return new Dimension(diameter, diameter);
                }
            };
            badgeLabel.setFont(new Font("Arial", Font.BOLD, 9));
            badgeLabel.setForeground(Color.WHITE);
            badgeLabel.setBackground(Color.RED); // Standard red badge
            badgeLabel.setOpaque(false); // We paint the background
            badgeSize = badgeLabel.getPreferredSize();
            badgeLabel.setSize(badgeSize);
        }

        // 3. Create the JLayeredPane
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setOpaque(false);

        // Calculate container size - make it tight around button + badge overlap
        int containerWidth = paddedButtonSize.width + (badgeLabel != null ? Math.max(0, badgeSize.width / 2 - badgeOffsetX / 2) : 0);
        int containerHeight = paddedButtonSize.height + (badgeLabel != null ? Math.max(0, badgeSize.height / 2 - badgeOffsetY / 2) : 0);
        containerHeight = Math.max(containerHeight, paddedButtonSize.height); // Ensure at least button height

        layeredPane.setPreferredSize(new Dimension(containerWidth, containerHeight));
        layeredPane.setMinimumSize(new Dimension(containerWidth, containerHeight));
        layeredPane.setMaximumSize(new Dimension(containerWidth, containerHeight)); // Set max size

        // 4. Position components
        // Position button towards bottom-left to make space for badge at top-right
        int buttonX = 0;
        int buttonY = containerHeight - paddedButtonSize.height; // Align button bottom
        actualButton.setBounds(buttonX, buttonY, paddedButtonSize.width, paddedButtonSize.height);
        layeredPane.add(actualButton, JLayeredPane.DEFAULT_LAYER);

        if (badgeLabel != null) {
            // Position badge at top-right, overlapping the button
            int badgeX = containerWidth - badgeSize.width - badgeOffsetX;
            int badgeActualY = badgeOffsetY;
            badgeLabel.setBounds(badgeX, badgeActualY, badgeSize.width, badgeSize.height);
            layeredPane.add(badgeLabel, JLayeredPane.PALETTE_LAYER); // Higher layer
        }

        return layeredPane;
    }
    // --- END createIconButtonWithBadge ---

    // --- UPDATED: Show notifications process ---
    private void showNotifications() {
        // 1. Mark existing unread notifications as read in the database
        markNotificationsAsRead();

        // 2. Fetch notifications (which will now populate read/unread tabs correctly)
        //    and update the badge *before* showing the popup
        fetchAndDisplayNotifications(); // This now also updates the badge internally

        // 3. Show the popup
        if (notificationButtonContainer != null) {
            // Adjust slightly if needed based on tab height and button size
            int yOffset = notificationButtonContainer.getHeight() - 5; // Adjust offset
            int xOffset = 0; // Can adjust horizontal offset if needed
            notificationPopup.show(notificationButtonContainer, xOffset, yOffset);
        } else {
            System.err.println("Notification button container is null, cannot show popup.");
        }
    }

    // --- NEW: Method to mark notifications as read ---
    private void markNotificationsAsRead() {
        // Clear the set tracking IDs read in this session
        justReadOrderIds.clear();

        // Get IDs of currently unread orders
        String selectUnreadSql = "SELECT id FROM orders WHERE customer_id = ? AND notification_read_status = 0";
        String updateSql = "UPDATE orders SET notification_read_status = 1 WHERE customer_id = ? AND notification_read_status = 0";

        Connection conn = null;
        PreparedStatement selectStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.connect();
            conn.setAutoCommit(false);

            // First, find which orders *will* be marked as read
            selectStmt = conn.prepareStatement(selectUnreadSql);
            selectStmt.setInt(1, customerId);
            rs = selectStmt.executeQuery();
            while (rs.next()) {
                justReadOrderIds.add(rs.getInt("id"));
            }
            rs.close();
            selectStmt.close();

            // Now, update them in the database
            if (!justReadOrderIds.isEmpty()) {
                updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, customerId);
                int updatedRows = updateStmt.executeUpdate();
                conn.commit();
                System.out.println("[Notification] Marked " + updatedRows + " order notifications as read for customer " + customerId);
            } else {
                conn.rollback(); // No changes needed
                 System.out.println("[Notification] No unread order notifications to mark as read for customer " + customerId);
            }

        } catch (SQLException ex) {
            System.err.println("[Notification] Error marking notifications as read: " + ex.getMessage());
            if (conn != null) try { conn.rollback(); } catch (SQLException e) { e.printStackTrace(); }
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
            try { if (selectStmt != null) selectStmt.close(); } catch (SQLException ignored) {}
            try { if (updateStmt != null) updateStmt.close(); } catch (SQLException ignored) {}
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }


     // --- UPDATED: Fetches and displays in tabs, updates badge ---
     private void fetchAndDisplayNotifications() {
        unreadNotificationsPanel.removeAll();
        readNotificationsPanel.removeAll();
        int currentUnreadCountForBadge = 0; // Actual count for the badge
        boolean hasUnreadForDisplay = false; // Tracks if anything goes into the Unread tab display
        boolean hasReadForDisplay = false;   // Tracks if anything goes into the Read tab display

        // Reset layout managers before adding components dynamically
        unreadNotificationsPanel.setLayout(new BoxLayout(unreadNotificationsPanel, BoxLayout.Y_AXIS));
        readNotificationsPanel.setLayout(new BoxLayout(readNotificationsPanel, BoxLayout.Y_AXIS));

        // Add headers
        JLabel unreadHeader = new JLabel("Unread Notifications");
        unreadHeader.setFont(new Font("Arial", Font.BOLD, 14));
        unreadHeader.setForeground(ThemeColors.PRIMARY);
        unreadHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        unreadHeader.setAlignmentX(Component.LEFT_ALIGNMENT); // Align left
        unreadNotificationsPanel.add(unreadHeader);

        JLabel readHeader = new JLabel("Read Notifications (Recent)");
        readHeader.setFont(new Font("Arial", Font.BOLD, 14));
        readHeader.setForeground(ThemeColors.TEXT); // Different color for read header
        readHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        readHeader.setAlignmentX(Component.LEFT_ALIGNMENT); // Align left
        readNotificationsPanel.add(readHeader);

        // --- Fetch Orders (Both Read and Unread Recent) ---
        String orderSql = "SELECT id, status, order_date, cancellation_requested, notification_read_status " +
                          "FROM orders " +
                          "WHERE customer_id = ? " +
                          "ORDER BY order_date DESC LIMIT 30"; // Limit total fetched orders
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(orderSql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int orderId = rs.getInt("id");
                String status = rs.getString("status");
                Timestamp orderTimestamp = rs.getTimestamp("order_date");
                boolean cancelledRequested = rs.getBoolean("cancellation_requested");
                boolean isRead = rs.getBoolean("notification_read_status");
                String dateStr = new SimpleDateFormat("MMM dd, hh:mm a").format(orderTimestamp);

                // Construct the message
                String message = "Order #" + orderId + " (" + dateStr + "): Status updated to '" + status + "'";
                if (cancelledRequested && "Processing".equalsIgnoreCase(status)) {
                    message = "Order #" + orderId + " (" + dateStr + "): Cancellation requested.";
                } else if ("Processing".equalsIgnoreCase(status) && isRecent(orderTimestamp, 1) && justReadOrderIds.contains(orderId)) {
                     // Display new order message only if it was just marked read
                     message = "New Order Placed: Order #" + orderId + " is now Processing.";
                }

                // Decide which panel to add to
                if (justReadOrderIds.contains(orderId)) {
                    // Item was just marked as read in this session, display in "Unread" tab for this view
                    addNotificationItem(message, unreadNotificationsPanel);
                    hasUnreadForDisplay = true;
                    // Don't count towards badge as it's effectively read now
                } else if (!isRead) {
                    // Item is still marked as unread in DB (and wasn't just read)
                    addNotificationItem(message, unreadNotificationsPanel);
                    hasUnreadForDisplay = true;
                    currentUnreadCountForBadge++; // Count towards badge
                } else {
                    // Item is marked as read in DB
                    addNotificationItem(message, readNotificationsPanel);
                    hasReadForDisplay = true;
                }
            }
        } catch (SQLException ex) {
            System.err.println("[Notification] Error fetching order notifications: " + ex.getMessage());
            addNotificationItem("Error loading order updates.", unreadNotificationsPanel);
            addNotificationItem("Error loading order updates.", readNotificationsPanel);
        }

        // --- Fetch Announcements (Treat as Unread for this view) ---
        List<String> announcements = getStoreAnnouncements();
        if (!announcements.isEmpty()) {
            if (hasUnreadForDisplay) { // Add separator only if orders were shown before
                 unreadNotificationsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                 JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
                 sep.setForeground(ThemeColors.SECONDARY);
                 sep.setAlignmentX(Component.LEFT_ALIGNMENT);
                 sep.setMaximumSize(new Dimension(340, 2)); // Constrain separator width
                 unreadNotificationsPanel.add(sep);
                 unreadNotificationsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
            JLabel announcementHeader = new JLabel("Announcements");
            announcementHeader.setFont(new Font("Arial", Font.BOLD, 13));
            announcementHeader.setForeground(ThemeColors.ACCENT); // Different color
            announcementHeader.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            announcementHeader.setAlignmentX(Component.LEFT_ALIGNMENT); // Align left
            unreadNotificationsPanel.add(announcementHeader);

            for (String announcement : announcements) {
                addNotificationItem("\uD83D\uDCE3 " + announcement, unreadNotificationsPanel); // Megaphone Emoji
                 hasUnreadForDisplay = true;
                 currentUnreadCountForBadge++; // Count announcements towards the badge
            }
        }

        // --- Add "Empty" messages if needed ---
        if (!hasUnreadForDisplay) {
            addEmptyNotificationLabel("No new notifications.", unreadNotificationsPanel);
        }
        if (!hasReadForDisplay) {
            addEmptyNotificationLabel("No older notifications found.", readNotificationsPanel);
        }

        // --- Update Badge Count ---
        this.notificationCount = currentUnreadCountForBadge; // Update the member variable
        updateNotificationBadge(); // Update the button display using the new count

        // --- Final UI updates ---
        unreadNotificationsPanel.revalidate();
        unreadNotificationsPanel.repaint();
        readNotificationsPanel.revalidate();
        readNotificationsPanel.repaint();
        notificationPopup.pack(); // Re-pack the popup after adding content

         // Ensure the popup stays visible if it was already shown
         if (notificationButtonContainer != null && notificationPopup.isVisible()) {
             int yOffset = notificationButtonContainer.getHeight() - 5; // Use consistent offset
             // Force the popup to recalculate size and repaint
             notificationPopup.setVisible(false);
             notificationPopup.setVisible(true);
              // Get location relative to screen for correct positioning
             Point btnLocation = notificationButtonContainer.getLocationOnScreen();
             notificationPopup.setLocation(btnLocation.x, btnLocation.y + yOffset);
         }
    }

    // Helper to add the "empty" label, ensuring proper layout
    private void addEmptyNotificationLabel(String text, JPanel targetPanel) {
         // Remove existing content (headers, items) before adding empty label
         targetPanel.removeAll();
         // Use BorderLayout to center the label
         targetPanel.setLayout(new BorderLayout());

         JLabel noNotifLabel = new JLabel(text);
         noNotifLabel.setFont(new Font("Arial", Font.ITALIC, 12));
         noNotifLabel.setForeground(Color.GRAY);
         noNotifLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center the text
         noNotifLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding

         targetPanel.add(noNotifLabel, BorderLayout.CENTER);
         // Revalidate the panel after adding the label
         targetPanel.revalidate();
         targetPanel.repaint();
    }


    // Unchanged: isRecent
    private boolean isRecent(Timestamp timestamp, int days) {
        if (timestamp == null) return false;
        LocalDateTime orderTime = timestamp.toLocalDateTime();
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(days);
        return orderTime.isAfter(cutoffTime);
    }

    // Unchanged: getStoreAnnouncements
    private List<String> getStoreAnnouncements() {
        List<String> announcements = new ArrayList<>();
        // Example static announcements:
        // announcements.add("\u2600\uFE0F Summer Sale ending soon!");
        // announcements.add("\uD83D\uDCBF New Album Pre-orders are live.");
        return announcements;
    }

    // --- UPDATED: Takes target panel, uses BoxLayout correctly ---
    private void addNotificationItem(String text, JPanel targetPanel) {
        // Ensure target panel uses BoxLayout before adding items dynamically
        // If it was set to BorderLayout for the "empty" message, reset it.
        if (!(targetPanel.getLayout() instanceof BoxLayout)) {
            targetPanel.setLayout(new BoxLayout(targetPanel, BoxLayout.Y_AXIS));
             // Re-add header if it was removed by addEmptyNotificationLabel
             boolean isUnread = (targetPanel == unreadNotificationsPanel);
             if (!hasHeader(targetPanel)) {
                JLabel header = new JLabel(isUnread ? "Unread Notifications" : "Read Notifications (Recent)");
                header.setFont(new Font("Arial", Font.BOLD, 14));
                header.setForeground(isUnread ? ThemeColors.PRIMARY : ThemeColors.TEXT);
                header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
                header.setAlignmentX(Component.LEFT_ALIGNMENT);
                targetPanel.add(header, 0); // Add header back at the top
             }
        }
        // Remove any "empty" label if we're adding a real item
        for (Component c : targetPanel.getComponents()) {
             if (c instanceof JLabel && (((JLabel)c).getText().contains("No new notifications") || ((JLabel)c).getText().contains("No older notifications"))) {
                targetPanel.remove(c);
                break;
             }
        }

        JLabel label = new JLabel("<html><body style='width: 280px;'>" + text + "</body></html>");
        label.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
        label.setForeground(ThemeColors.TEXT);
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.SECONDARY.darker()),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        label.setAlignmentX(Component.LEFT_ALIGNMENT); // Align items left
        label.setMaximumSize(new Dimension(340, Short.MAX_VALUE)); // Ensure width constraint
        targetPanel.add(label);
    }

    // Helper to check if a panel already has a header
    private boolean hasHeader(JPanel panel) {
        for(Component c : panel.getComponents()) {
             if (c instanceof JLabel && (((JLabel)c).getText().contains("Notifications") || ((JLabel)c).getText().contains("Announcements"))) {
                 return true;
             }
        }
        return false;
    }

    // --- UPDATED: Counts only UNREAD orders ---
    private int getNotificationCount() {
        int count = 0;
        // Query only for notification_read_status = 0
        String orderSql = "SELECT COUNT(*) FROM orders WHERE customer_id = ? AND notification_read_status = 0";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(orderSql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                count += rs.getInt(1);
            }
        } catch (SQLException ex) {
            System.err.println("[Notification] Error getting UNREAD order count: " + ex.getMessage());
        } catch (Exception e) {
             System.err.println("[Notification] Non-SQL Error getting UNREAD notification count: " + e.getMessage());
        }

        // Add count of static announcements (treated as unread until viewed)
        count += getStoreAnnouncements().size();
        // System.out.println("[Notification] Calculated unread count (DB Orders + Announcements): " + count); // Debugging
        return count;
    }

    // --- UPDATED: Updates badge based on current unread count ---
    private void updateNotificationBadge() {
        int currentUnreadCount = this.notificationCount; // Use the member variable

        ActionListener notificationAction = e -> showNotifications();
        JComponent newNotificationButtonContainer = createIconButtonWithBadge(
            "\uD83D\uDD14", // BELL emoji
            currentUnreadCount,
            notificationAction
        );
        replaceButtonContainer(notificationButtonContainer, newNotificationButtonContainer);
        notificationButtonContainer = newNotificationButtonContainer;
    }


    // --- START NEARLY UNCHANGED METHODS (Layout adjustments) ---

    // --- UPDATED createUserButton ---
    // Returns JButton directly, calculates width more generously.
    private JButton createUserButton(String text, ActionListener actionListener) { // Return JButton directly
        JButton actualButton = new JButton(text);
        actualButton.setFont(new Font("Arial", Font.BOLD, 12));
        actualButton.setBackground(ThemeColors.CARD_BG);
        actualButton.setForeground(ThemeColors.TEXT);

        // Define padding values
        int verticalPadding = 6;
        // Increase horizontal padding slightly more for text calculation to give breathing room
        int horizontalPaddingForCalc = 20; // Padding used for size calculation
        int horizontalPaddingForBorder = 18; // Actual border padding (can match badge buttons)

        // Apply the visual border padding
        actualButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeColors.SECONDARY, 1),
                BorderFactory.createEmptyBorder(verticalPadding, horizontalPaddingForBorder, verticalPadding, horizontalPaddingForBorder)
        ));
        actualButton.setFocusPainted(false);
        actualButton.setOpaque(true);
        actualButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // --- Calculate required size MORE generously ---
        FontMetrics fm = actualButton.getFontMetrics(actualButton.getFont());
        int textWidth = fm.stringWidth(text);
        // Calculate required width: text width + border padding + extra buffer
        int requiredWidth = textWidth + (horizontalPaddingForCalc * 2) + 10; // Added +10 buffer
        // Use default preferred height calculation (includes vertical padding from border)
        int requiredHeight = actualButton.getPreferredSize().height;
        // Ensure a slightly larger minimum width
        requiredWidth = Math.max(requiredWidth, 90); // Increased minimum width fallback

        Dimension requiredSize = new Dimension(requiredWidth, requiredHeight);
        // --- End Calculate required size ---

        // Set button preferred size based on calculated required size
        // Avoid setting min/max rigidly unless necessary, let FlowLayout handle it
        actualButton.setPreferredSize(requiredSize);

        actualButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                actualButton.setBackground(ThemeColors.SECONDARY);
                actualButton.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                actualButton.setBackground(ThemeColors.CARD_BG);
                actualButton.setForeground(ThemeColors.TEXT);
            }
        });

        if (actionListener != null) {
            actualButton.addActionListener(actionListener);
        }

        // Return the JButton directly, NO JLayeredPane wrapper
        return actualButton;
    }
    // --- END UPDATED createUserButton ---

    // Unchanged: createBadgeButtonWithCounter (already reasonably sized)
    private JComponent createBadgeButtonWithCounter(String text, int count, ActionListener actionListener) {
        JButton actualButton = new JButton(text);
        actualButton.setFont(new Font("Arial", Font.BOLD, 12));
        actualButton.setBackground(ThemeColors.CARD_BG);
        actualButton.setForeground(ThemeColors.TEXT);

        // Ensure minimum width for better look
        FontMetrics fm = actualButton.getFontMetrics(actualButton.getFont());
        int textWidth = fm.stringWidth(text);
        int minWidth = Math.max(textWidth + 35, 95); // Adjusted min width/padding

        actualButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeColors.SECONDARY, 1),
                BorderFactory.createEmptyBorder(6, 18, 6, 18) // Adjusted padding
        ));
        actualButton.setFocusPainted(false);
        actualButton.setOpaque(true);
        actualButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Set preferred/min/max based on calculated minWidth and actual button height
        Dimension buttonPrefSize = new Dimension(minWidth, actualButton.getPreferredSize().height);
        actualButton.setMinimumSize(buttonPrefSize);
        actualButton.setPreferredSize(buttonPrefSize);
        actualButton.setMaximumSize(buttonPrefSize);

        actualButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                actualButton.setBackground(ThemeColors.SECONDARY);
                actualButton.setForeground(Color.WHITE);
            }
            @Override public void mouseExited(MouseEvent e) {
                actualButton.setBackground(ThemeColors.CARD_BG);
                actualButton.setForeground(ThemeColors.TEXT);
            }
        });

        if (actionListener != null) {
            actualButton.addActionListener(actionListener);
        }

        // --- Badge Label ---
        JLabel badgeLabel = null;
        Dimension badgeSize = new Dimension(0, 0);
        int badgeOffsetX = 4; // Horizontal offset from top-right corner
        int badgeOffsetY = -2; // Vertical offset (negative to move up)

        if (count > 0) {
            String countText = count > 99 ? "99+" : String.valueOf(count);
            badgeLabel = new JLabel(countText, SwingConstants.CENTER) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillOval(0, 0, getWidth()-1, getHeight()-1); // Oval background
                    g2.dispose();
                    super.paintComponent(g);
                }
                @Override public Dimension getPreferredSize() {
                    Dimension size = super.getPreferredSize();
                    int diameter = Math.max(size.width, size.height) + 5; // Padding inside oval
                    diameter = Math.max(diameter, 18); // Minimum size
                    return new Dimension(diameter, diameter);
                }
            };
            badgeLabel.setFont(new Font("Arial", Font.BOLD, 10));
            badgeLabel.setForeground(Color.WHITE);
            badgeLabel.setBackground(ThemeColors.PRIMARY); // Badge color
            badgeLabel.setOpaque(false); // We paint the background
            badgeSize = badgeLabel.getPreferredSize();
            badgeLabel.setSize(badgeSize);
        }

        // --- JLayeredPane Setup ---
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setOpaque(false);

        // Calculate container size to encompass button and badge overlap
        int containerWidth = buttonPrefSize.width + (badgeLabel != null ? Math.max(0, badgeSize.width / 2 - badgeOffsetX) : 0);
        int containerHeight = buttonPrefSize.height + (badgeLabel != null ? Math.max(0, badgeSize.height / 2 + badgeOffsetY) : 0);
        containerHeight = Math.max(containerHeight, buttonPrefSize.height); // Must be at least button height

        Dimension containerSize = new Dimension(containerWidth, containerHeight);
        layeredPane.setPreferredSize(containerSize);
        layeredPane.setMinimumSize(containerSize);
        layeredPane.setMaximumSize(containerSize);

        // Position button towards bottom-left to make space for badge at top-right
        int buttonX = 0;
        int buttonY = containerHeight - buttonPrefSize.height; // Align button bottom
        actualButton.setBounds(buttonX, buttonY, buttonPrefSize.width, buttonPrefSize.height);
        layeredPane.add(actualButton, JLayeredPane.DEFAULT_LAYER);

        if (badgeLabel != null) {
            // Position badge at top-right, using offsets
            int badgeX = containerWidth - badgeSize.width - badgeOffsetX;
            int badgeActualY = badgeOffsetY;
            badgeLabel.setBounds(badgeX, badgeActualY, badgeSize.width, badgeSize.height);
            layeredPane.add(badgeLabel, JLayeredPane.PALETTE_LAYER); // Higher layer
        }

        return layeredPane;
    }


    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(ThemeColors.TEXT); // Using external ThemeColors
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Add padding for better spacing
        button.setMargin(new Insets(5, 10, 5, 10));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(ThemeColors.PRIMARY); // Using external ThemeColors
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(ThemeColors.TEXT); // Using external ThemeColors
            }
        });
        return button;
    }

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
                     "SELECT COUNT(DISTINCT id) FROM orders WHERE customer_id = ?")) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException ex) {
            System.err.println("Error getting order count: " + ex.getMessage());
            return 0;
        }
    }

    // --- END NEARLY UNCHANGED METHODS ---


    // ==============================================================
    // --- PRODUCT LOADING, PANELS, DIALOGS, HELPERS            ---
    // --- Modified to remove color/size where applicable       ---
    // ==============================================================

    private JPanel createHomePanel() {
        JPanel homePanel = new JPanel(new BorderLayout());
        homePanel.setBackground(ThemeColors.BACKGROUND); // Using external ThemeColors

        // ========== BANNER SECTION ==========
        JPanel bannerPanel = new JPanel(new BorderLayout());
        bannerPanel.setBackground(ThemeColors.CARD_BG); // Using external ThemeColors
        bannerPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        bannerPanel.setPreferredSize(new Dimension(1920, 180)); // Adjust height as needed

        try {
            URL gifURL = getClass().getResource("/images/promotional.gif");
            if (gifURL == null) {
                File gifFile = new File("images/promotional.gif");
                if (gifFile.exists()) {
                    gifURL = gifFile.toURI().toURL();
                }
            }

            if (gifURL != null) {
                final ImageIcon originalIcon = new ImageIcon(gifURL);
                JPanel imagePanel = new JPanel(new GridBagLayout());
                imagePanel.setBackground(ThemeColors.CARD_BG);
                JLabel imageLabel = new JLabel(originalIcon);
                imagePanel.add(imageLabel);

                imagePanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                imagePanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        showCard("Products");
                        loadProductsToPanel(productsProductGridPanel);
                    }
                });

                bannerPanel.add(imagePanel, BorderLayout.CENTER);
            } else {
                throw new FileNotFoundException("Banner image not found in classpath (/images/promotional.gif) or relative path (images/promotional.gif)");
            }
        } catch (Exception e) {
            System.err.println("Banner error: " + e.getMessage());
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
                    showCard("Products");
                    loadProductsToPanel(productsProductGridPanel);
                }
            });
            fallbackPanel.add(fallbackLabel, BorderLayout.CENTER);
            bannerPanel.add(fallbackPanel, BorderLayout.CENTER);
        }

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

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setForeground(ThemeColors.TEXT);
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        topPanel.add(searchPanel, BorderLayout.CENTER);
        homePanel.add(topPanel, BorderLayout.NORTH);

        // ========== PRODUCT GRID ==========
        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.add(homeProductGridPanel, BorderLayout.CENTER);
        gridWrapper.setBackground(ThemeColors.BACKGROUND);

        JScrollPane scrollPane = new JScrollPane(gridWrapper);
        styleScrollPane(scrollPane); // Apply custom scrollbar style
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);

        homePanel.add(scrollPane, BorderLayout.CENTER);

        return homePanel;
    }

    private void loadAllProducts() {
        loadProductsToPanel(homeProductGridPanel);
        loadProductsToPanel(productsProductGridPanel);
    }

    private void filterProducts() {
        String searchText = searchField.getText().trim().toLowerCase();
        JPanel targetPanel;
        if (currentCard.equals("Home")) {
            targetPanel = homeProductGridPanel;
            targetPanel.setLayout(new WrapLayout(FlowLayout.CENTER, 20, 20));
        } else if (currentCard.equals("Products")) {
            targetPanel = productsProductGridPanel;
            targetPanel.setLayout(new WrapLayout(FlowLayout.CENTER, 20, 20));
        } else {
            return;
        }

        targetPanel.removeAll();

        // Removed color/size from query - NOTE: Original query didn't include them directly anyway
        String query;
        PreparedStatement stmt = null;
        Connection conn = null;
        boolean hasResults = false;

        try {
            conn = DBConnection.connect();
            if (searchText.isEmpty()) {
                loadProductsToPanel(targetPanel);
                scrollToTop(targetPanel);
                return;
            } else {
                // Query based on name or group name
                query = "SELECT id, name, group_name, price, description, image_path FROM products WHERE LOWER(name) LIKE ? OR LOWER(group_name) LIKE ? ORDER BY name";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, "%" + searchText + "%");
                stmt.setString(2, "%" + searchText + "%");
            }

            ResultSet rs = stmt.executeQuery();
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
            showNoResultsMessage("Error occurred", targetPanel);
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException ignored) {}
            try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }

        targetPanel.revalidate();
        targetPanel.repaint();
        scrollToTop(targetPanel);
    }

    private void scrollToTop(JPanel panel) {
        SwingUtilities.invokeLater(() -> {
            Component view = panel;
            Container parent = view.getParent();
            JViewport viewport = null;

            while (parent != null) {
                if (parent instanceof JViewport) {
                    viewport = (JViewport) parent;
                    break;
                }
                view = parent;
                parent = parent.getParent();
            }

            if (viewport != null) {
                // Check if the panel itself or its wrapper is the view
                 if (viewport.getView() == panel || viewport.getView() == panel.getParent()) {
                    viewport.setViewPosition(new Point(0, 0));
                 } else {
                     // Fallback: try scrolling the panel directly if it's nested deeper
                     panel.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
                 }
            } else {
                 // Fallback if no viewport ancestor
                 System.err.println("Warning: Could not find JViewport ancestor for scrolling. Trying direct parent JScrollPane.");
                 Container scrollPaneParent = SwingUtilities.getAncestorOfClass(JScrollPane.class, panel);
                 if (scrollPaneParent instanceof JScrollPane) {
                     JScrollPane scrollPane = (JScrollPane) scrollPaneParent;
                     scrollPane.getVerticalScrollBar().setValue(0);
                     scrollPane.getHorizontalScrollBar().setValue(0);
                 } else {
                     System.err.println("Warning: Could not find scroll pane for panel.");
                 }
            }
        });
    }

    private void showNoResultsMessage(String searchText, JPanel panel) {
        panel.removeAll();
        panel.setLayout(new BorderLayout());
        JLabel noResultsLabel = new JLabel("No products found matching \"" + searchText + "\"", SwingConstants.CENTER);
        noResultsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        noResultsLabel.setForeground(Color.GRAY);
        panel.add(noResultsLabel, BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();
    }

    private void refreshProductDisplay() {
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

    // Removed color/size display logic and changed "Add to Cart" button to "Buy Now" with new functionality
    private JPanel createProductCard(int id, String name, String groupName, double price, String description, String imagePathFromDB) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(ThemeColors.CARD_BG);
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        card.setPreferredSize(new Dimension(341, 341)); // Keep size

        // Image Container
        JPanel imageContainer = new JPanel(new BorderLayout());
        imageContainer.setPreferredSize(new Dimension(230, 230));
        imageContainer.setBackground(ThemeColors.BACKGROUND);

        ImageIcon icon = loadImageIcon(imagePathFromDB, name);

        if (icon != null && isIconValid(icon)) { // Use the public static method
            Image scaledImage = icon.getImage().getScaledInstance(230, 230, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imageContainer.add(imageLabel, BorderLayout.CENTER);
        } else {
            JLabel noImageLabel = new JLabel("No Image Available", SwingConstants.CENTER);
            noImageLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            noImageLabel.setForeground(Color.GRAY);
            imageContainer.add(noImageLabel, BorderLayout.CENTER);
        }

        card.add(imageContainer, BorderLayout.CENTER);

        // Info Panel (Name, Price, Buttons)
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Display group name if available, otherwise just name
        String displayName = (groupName != null && !groupName.trim().isEmpty()) ? groupName + " - " + name : name;
        JLabel nameLabel = new JLabel(displayName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(ThemeColors.TEXT);

        JLabel priceLabel = new JLabel(String.format("₱%.2f", price));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        priceLabel.setForeground(ThemeColors.PRIMARY);

        infoPanel.add(nameLabel, BorderLayout.NORTH);
        infoPanel.add(priceLabel, BorderLayout.CENTER);

        // Button Panel (Details, Buy Now)
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton detailsButton = createStyledButton("Details", ThemeColors.SECONDARY);
        detailsButton.setFont(new Font("Arial", Font.BOLD, 12));
        detailsButton.addActionListener(e -> showProductDetails(id, name, price, description, imagePathFromDB));

        // --- MODIFICATION: "Buy Now" button with direct checkout functionality ---
        JButton buyNowButton = createStyledButton("Buy Now", ThemeColors.PRIMARY);
        buyNowButton.setFont(new Font("Arial", Font.BOLD, 12));
        buyNowButton.addActionListener(e -> buyNow(id, name, price)); // Call the new buyNow method
        // --- END MODIFICATION ---

        buttonPanel.add(detailsButton);
        buttonPanel.add(buyNowButton); // Add the "Buy Now" button

        infoPanel.add(buttonPanel, BorderLayout.SOUTH);
        card.add(infoPanel, BorderLayout.SOUTH);

        return card;
    }


    // --- REVISED METHOD: Handle "Buy Now" button click ---
    // --- Inside CustomerFrame.java ---

    // --- REVISED METHOD: Handle "Buy Now" button click ---
    private void buyNow(int productId, String name, double price) {
        String checkStockSql = "SELECT stock FROM products WHERE id = ?";
        // Ensure quantity is 1, handle duplicates, but don't rely on LAST_INSERT_ID() here
        String insertCartSql = "INSERT INTO cart (product_id, product_name, price, quantity, customer_id) VALUES (?, ?, ?, 1, ?) ON DUPLICATE KEY UPDATE quantity = 1";
        String findCartIdSql = "SELECT id FROM cart WHERE customer_id = ? AND product_id = ?"; // Used AFTER commit

        Connection conn = null;
        PreparedStatement checkStmt = null;
        PreparedStatement insertStmt = null;
        PreparedStatement findStmt = null;
        int cartId = -1;

        try {
            conn = DBConnection.connect();
            conn.setAutoCommit(false); // Start transaction

            // 1. Check Stock
            checkStmt = conn.prepareStatement(checkStockSql);
            checkStmt.setInt(1, productId);
            ResultSet rsStock = checkStmt.executeQuery();
            int stock = 0;
            if (rsStock.next()) {
                stock = rsStock.getInt("stock");
            } else {
                JOptionPane.showMessageDialog(this, "Product '" + name + "' not found.", "Error", JOptionPane.ERROR_MESSAGE);
                conn.rollback();
                return;
            }
            rsStock.close();
            checkStmt.close(); // Close statement

            if (stock <= 0) {
                JOptionPane.showMessageDialog(this, "Sorry, '" + name + "' is currently out of stock.", "Out of Stock", JOptionPane.WARNING_MESSAGE);
                conn.rollback();
                return;
            }

            // 2. Add/Update Cart (Quantity 1)
            insertStmt = conn.prepareStatement(insertCartSql);
            insertStmt.setInt(1, productId);
            insertStmt.setString(2, name);
            insertStmt.setDouble(3, price);
            insertStmt.setInt(4, customerId);
            int rowsAffected = insertStmt.executeUpdate();
            insertStmt.close(); // Close statement

            if (rowsAffected == 0) {
                 // This case should ideally not happen with INSERT...ON DUPLICATE unless there's a unique constraint issue beyond the primary key handled by ON DUPLICATE.
                 // Log a warning or error if needed.
                 System.err.println("[BUY NOW DEBUG] Warning: INSERT...ON DUPLICATE KEY UPDATE affected 0 rows for product " + productId);
                 // We can still try to find the cart ID below, as the row might exist.
            }

            // 3. Commit the transaction
            conn.commit();

            // 4. Reliably get the Cart ID *after* commit
            findStmt = conn.prepareStatement(findCartIdSql);
            findStmt.setInt(1, customerId);
            findStmt.setInt(2, productId);
            ResultSet findRs = findStmt.executeQuery();
            if (findRs.next()) {
                cartId = findRs.getInt("id");
            }
            findRs.close();
            findStmt.close(); // Close statement

            // ***** START DEBUGGING ADDITION *****
            System.out.println("[BUY NOW DEBUG] Retrieved cartId after commit: " + cartId + " for customerId: " + customerId + ", productId: " + productId);

            if (cartId == -1) {
                System.err.println("[BUY NOW DEBUG] CRITICAL: cartId is -1. Cannot proceed to checkout.");
                conn.rollback(); // Rollback just in case (although commit likely succeeded)
                JOptionPane.showMessageDialog(this, "Failed to identify the item added to the cart. Please try adding from the cart page.", "Checkout Error", JOptionPane.ERROR_MESSAGE);
                // Ensure CustomerFrame remains visible if necessary
                setVisible(true);
                return; // Stop execution
            }
            // ***** END DEBUGGING ADDITION *****


            // 5. Update Cart Badge (reflects the newly added item)
            updateCartBadge();

            // 6. Proceed to CheckoutFrame immediately using invokeLater
            final List<Integer> cartIdList = new ArrayList<>(); // Make final for lambda
            cartIdList.add(cartId);

            // ***** START DEBUGGING ADDITION *****
            System.out.println("[BUY NOW DEBUG] Passing cartIdList to CheckoutFrame: " + cartIdList);
            // ***** END DEBUGGING ADDITION *****

            // Use invokeLater for GUI operations
            SwingUtilities.invokeLater(() -> {
                // Pass 'this' (CustomerFrame instance) to CheckoutFrame
                new CheckoutFrame(customerId, cartIdList, this).setVisible(true);
                // Hide this CustomerFrame
                setVisible(false);
            });


        } catch (SQLException ex) {
            System.err.println("Buy Now SQL Error: " + ex.getMessage());
            ex.printStackTrace(); // Print stack trace for detailed debugging
            if (conn != null) try { conn.rollback(); } catch (SQLException e) { e.printStackTrace(); }
            JOptionPane.showMessageDialog(this, "Error processing Buy Now: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            System.err.println("Buy Now General Error: " + e.getMessage());
            e.printStackTrace(); // Print full stack trace for unexpected errors
            if (conn != null) try { conn.rollback(); } catch (SQLException se) { se.printStackTrace(); }
            JOptionPane.showMessageDialog(this, "An unexpected error occurred during Buy Now.", "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Close resources (Statements are closed within the try block now)
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }
    // --- END of REVISED buyNow method ---


    // Product details dialog - doesn't explicitly show color/size unless in description
    private void showProductDetails(int productId, String name, double price, String description, String imagePathFromDB) {
        JDialog detailsDialog = new JDialog(this, "Product Details", true);
        detailsDialog.setSize(400, 450);
        detailsDialog.setLayout(new BorderLayout());
        detailsDialog.getContentPane().setBackground(ThemeColors.BACKGROUND);

        JPanel detailsPanel = new JPanel(new BorderLayout(10, 10));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        detailsPanel.setBackground(ThemeColors.BACKGROUND);

        // Image Panel
        JPanel imagePanel = new JPanel(new GridBagLayout());
        imagePanel.setPreferredSize(new Dimension(200, 200));
        imagePanel.setOpaque(false);

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        ImageIcon icon = loadImageIcon(imagePathFromDB, name);

        if (icon != null && isIconValid(icon)) { // Use the public static method
            Image scaledImage = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
        } else {
            imageLabel.setText("No Image Available");
            imageLabel.setForeground(ThemeColors.TEXT); // Use ThemeColors.TEXT for consistency
        }
        imagePanel.add(imageLabel);
        detailsPanel.add(imagePanel, BorderLayout.NORTH);

        // Info Panel (Name, Price, Description)
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        nameLabel.setForeground(ThemeColors.PRIMARY);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel priceLabel = new JLabel(String.format("Price: ₱%.2f", price), SwingConstants.CENTER);
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        priceLabel.setForeground(ThemeColors.TEXT); // Use ThemeColors.TEXT
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea descArea = new JTextArea(description != null ? description : "No description available.");
        descArea.setEditable(false);
        descArea.setFont(new Font("Arial", Font.PLAIN, 14));
        descArea.setBackground(ThemeColors.BACKGROUND);
        descArea.setForeground(ThemeColors.TEXT); // Use ThemeColors.TEXT
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        JScrollPane descScrollPane = new JScrollPane(descArea);
        styleScrollPane(descScrollPane);
        descScrollPane.setBorder(BorderFactory.createEmptyBorder());
        descScrollPane.setPreferredSize(new Dimension(340, 80));

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(priceLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        infoPanel.add(descScrollPane);

        detailsPanel.add(infoPanel, BorderLayout.CENTER);

        // Button Panel (Add to Cart, Wishlist, Close)
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton addToCartButton = createStyledButton("Add to Cart", ThemeColors.PRIMARY);
        addToCartButton.addActionListener(e -> {
            addToCart(productId, name, price); // Still uses the regular addToCart
            detailsDialog.dispose();
        });

        JButton addToWishlistButton = createStyledButton("Wishlist", ThemeColors.SECONDARY);
        addToWishlistButton.addActionListener(e -> {
            addToWishlist(productId, name, price);
            detailsDialog.dispose();
        });

        // FIX: Use ThemeColors.SECONDARY or another defined color for Close button BG
        // Using CARD_BG might make it blend too much with the background.
        // Using SECONDARY provides better contrast typically.
        JButton closeButton = createStyledButton("Close", ThemeColors.SECONDARY);
        // You might still want distinct text color if SECONDARY is dark.
        // Keep this if SECONDARY is dark, or adjust as needed.
        // closeButton.setForeground(ThemeColors.TEXT);
        closeButton.addActionListener(e -> detailsDialog.dispose());

        buttonPanel.add(addToCartButton);
        buttonPanel.add(addToWishlistButton);
        buttonPanel.add(closeButton);

        detailsPanel.add(buttonPanel, BorderLayout.SOUTH);

        detailsDialog.add(detailsPanel, BorderLayout.CENTER);
        detailsDialog.setLocationRelativeTo(this);
        detailsDialog.setVisible(true);
    }

     // *** MODIFIED: Changed from private to public static ***
     public static ImageIcon loadImageIcon(String dbPath, String productName) {
        ImageIcon icon = null;
        URL imageURL = null;
        String loadedFrom = "N/A";

        // 1. Try DB Path in Classpath (with and without leading slash)
        if (dbPath != null && !dbPath.trim().isEmpty()) {
            String resourcePathDB = dbPath.replace("\\", "/");
            if (!resourcePathDB.startsWith("/")) {
                resourcePathDB = "/" + resourcePathDB;
            }
            imageURL = CustomerFrame.class.getResource(resourcePathDB);
            if (imageURL != null) {
                loadedFrom = "Classpath (DB Path): " + resourcePathDB;
            } else {
                 // Try without leading slash if first attempt failed
                 resourcePathDB = dbPath.replace("\\", "/");
                 if (resourcePathDB.startsWith("/")) resourcePathDB = resourcePathDB.substring(1);
                 imageURL = CustomerFrame.class.getResource("/" + resourcePathDB); // Ensure leading slash for this check
                 if (imageURL != null) {
                     loadedFrom = "Classpath (DB Path, Checked Leading Slash): /" + resourcePathDB;
                 }
            }
        }

        // 2. Try DB Path in Filesystem (absolute and relative to 'images')
        if (imageURL == null && dbPath != null && !dbPath.trim().isEmpty()) {
            try {
                File dbFile = new File(dbPath);
                if (dbFile.exists() && dbFile.isFile() && dbFile.canRead()) {
                    imageURL = dbFile.toURI().toURL();
                    loadedFrom = "Filesystem (DB Path): " + dbFile.getAbsolutePath();
                } else {
                    // Try relative path within an 'images' folder
                    File relativeFile = new File("images", dbPath.replace("\\", "/"));
                     if (relativeFile.exists() && relativeFile.isFile() && relativeFile.canRead()) {
                        imageURL = relativeFile.toURI().toURL();
                        loadedFrom = "Filesystem (Relative DB Path): " + relativeFile.getAbsolutePath();
                    }
                }
            } catch (Exception e) { /* Ignore MalformedURLException or SecurityException */ }
        }

        // 3. Try Derived Name in Classpath (common image locations)
        if (imageURL == null && productName != null && !productName.trim().isEmpty()) {
            String derivedFilenameBase = productName.replaceAll("[^a-zA-Z0-9.\\-_ ]", "_").trim();
            String[] extensions = {".png", ".jpg", ".jpeg", ".gif"};
            String[] classpathBaseDirs = {"/images/products/", "/images/"};

            outerLoopClasspathDerived:
            for (String baseDir : classpathBaseDirs) {
                 for (String ext : extensions) {
                    String derivedFilename = derivedFilenameBase + ext;
                    String resourcePathDerived = baseDir + derivedFilename;
                    imageURL = CustomerFrame.class.getResource(resourcePathDerived);
                    if (imageURL != null) {
                        loadedFrom = "Classpath (Derived Name): " + resourcePathDerived;
                        break outerLoopClasspathDerived;
                    }
                 }
            }
        }

        // 4. Try Derived Name in Filesystem (common project structures)
        if (imageURL == null && productName != null && !productName.trim().isEmpty()) {
             String derivedFilenameBase = productName.replaceAll("[^a-zA-Z0-9.\\-_ ]", "_").trim();
             String[] extensions = {".png", ".jpg", ".jpeg", ".gif"};
             String[] relativeBaseDirs = {"images/products", "images", ""}; // Adjusted paths

             outerLoopFSDerived:
             for (String baseDir : relativeBaseDirs) {
                 for (String ext : extensions) {
                     String derivedFilename = derivedFilenameBase + ext;
                     try {
                         // Construct path carefully, handling empty baseDir
                         File derivedFile = baseDir.isEmpty() ? new File(derivedFilename) : new File(baseDir, derivedFilename);
                         if (derivedFile.exists() && derivedFile.isFile() && derivedFile.canRead()) {
                             imageURL = derivedFile.toURI().toURL();
                             loadedFrom = "Filesystem (Derived Name): " + derivedFile.getAbsolutePath();
                             break outerLoopFSDerived;
                         }
                     } catch (Exception e) { /* Ignore */ }
                 }
             }
        }


        // 5. Fallback to Default Image in Classpath
        if (imageURL == null) {
            String defaultImagePath = "/images/default_product.png";
            imageURL = CustomerFrame.class.getResource(defaultImagePath);
            if (imageURL != null) {
                loadedFrom = "Classpath (Default Image): " + defaultImagePath;
            } else {
                System.err.println("CRITICAL ERROR: Default image '" + defaultImagePath + "' not found in classpath!");
                loadedFrom = "ERROR: Default Not Found";
                 return createPlaceholderIcon(productName != null ? productName : "Error");
            }
        }

        // 6. Create ImageIcon and Validate
        if (imageURL != null) {
            icon = new ImageIcon(imageURL);
            if (!isIconValid(icon)) { // Use the public static method
                System.err.println("Warning: Invalid image loaded for '" + productName + "' from " + loadedFrom + ". URL: " + imageURL + ". Using placeholder.");
                icon = createPlaceholderIcon(productName);
                loadedFrom += " (Invalid, using Placeholder)";
            }
        } else {
            System.err.println("Warning: Could not find any image for '" + productName + "' after all checks. Using placeholder.");
            icon = createPlaceholderIcon(productName);
            loadedFrom = "Placeholder (Final Fallback)";
        }

        // System.out.println("Loaded image for '" + productName + "' from: " + loadedFrom); // Optional: Debugging
        return icon;
    }


    private static ImageIcon createPlaceholderIcon(String text) {
        int width = 100;
        int height = 100;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setColor(ThemeColors.CARD_BG.darker());
        g.fillRect(0, 0, width, height);
        g.setColor(ThemeColors.SECONDARY);
        g.drawRect(0, 0, width - 1, height - 1);

        g.setColor(ThemeColors.TEXT);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();

        String line1 = "No Image";
        String line2 = text != null ? (text.length() > 12 ? text.substring(0, 10) + "..." : text) : "";

        int y = (height - fm.getHeight() * (line2.isEmpty() ? 1 : 2)) / 2 + fm.getAscent();
        int x1 = (width - fm.stringWidth(line1)) / 2;
        g.drawString(line1, x1, y);

        if (!line2.isEmpty()) {
             int x2 = (width - fm.stringWidth(line2)) / 2;
            g.drawString(line2, x2, y + fm.getHeight());
        }

        g.dispose();
        return new ImageIcon(image);
    }


    // *** MODIFIED: Changed from private to public static ***
    public static boolean isIconValid(ImageIcon icon) {
        if (icon == null) return false;
        return icon.getImageLoadStatus() == MediaTracker.COMPLETE && icon.getIconWidth() > 0 && icon.getIconHeight() > 0;
    }
    // *** END MODIFICATION ***


    private JPanel createProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeColors.BACKGROUND);

        productsProductGridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        productsProductGridPanel.setLayout(new WrapLayout(FlowLayout.CENTER, 20, 20));

        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.add(productsProductGridPanel, BorderLayout.CENTER);
        gridWrapper.setBackground(ThemeColors.BACKGROUND);

        JScrollPane scrollPane = new JScrollPane(gridWrapper);
        styleScrollPane(scrollPane);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }


    private void loadProductsToPanel(JPanel targetPanel) {
        targetPanel.removeAll();

        if (targetPanel == homeProductGridPanel || targetPanel == productsProductGridPanel) {
            targetPanel.setLayout(new WrapLayout(FlowLayout.CENTER, 20, 20));
        } else {
            targetPanel.setLayout(new BoxLayout(targetPanel, BoxLayout.Y_AXIS));
        }

        // Removed color/size from query
        String sql = "SELECT id, name, group_name, price, description, image_path FROM products ORDER BY name";

        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            boolean hasProducts = false;
            while (rs.next()) {
                hasProducts = true;
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String groupName = rs.getString("group_name");
                double price = rs.getDouble("price");
                String description = rs.getString("description");
                String imagePath = rs.getString("image_path");
                targetPanel.add(createProductCard(id, name, groupName, price, description, imagePath));
            }

            if (!hasProducts && (targetPanel == homeProductGridPanel || targetPanel == productsProductGridPanel)) {
                showNoResultsMessage("No products available", targetPanel);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading products: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            if (targetPanel == homeProductGridPanel || targetPanel == productsProductGridPanel) {
                showNoResultsMessage("Error loading products", targetPanel);
            }
        }
        targetPanel.revalidate();
        targetPanel.repaint();
    }


    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeColors.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Padding

        // Back Button
        JButton backButton = new JButton("← Back to Home");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(ThemeColors.CARD_BG);
        backButton.setForeground(ThemeColors.TEXT);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        backButton.addActionListener(e -> showCard("Home"));
        panel.add(backButton, BorderLayout.NORTH);

        // Main Content Area (Items + Summary)
        JPanel cartContentPanel = new JPanel(new BorderLayout());
        cartContentPanel.setBackground(ThemeColors.BACKGROUND);

        // Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(ThemeColors.BACKGROUND);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0)); // Bottom padding
        JLabel title = new JLabel("Your Shopping Cart");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(ThemeColors.PRIMARY);
        titlePanel.add(title);
        cartContentPanel.add(titlePanel, BorderLayout.NORTH);

        // Cart Items Panel (inside ScrollPane)
        cartItemsPanel = new JPanel(); // Initialize the member variable
        cartItemsPanel.setLayout(new BoxLayout(cartItemsPanel, BoxLayout.Y_AXIS)); // Vertical layout
        cartItemsPanel.setBackground(ThemeColors.BACKGROUND);
        cartItemsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(cartItemsPanel);
        styleScrollPane(scrollPane); // Apply custom scrollbar style
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // No border for scroll pane
        scrollPane.getViewport().setBackground(ThemeColors.BACKGROUND);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Faster scrolling

        cartContentPanel.add(scrollPane, BorderLayout.CENTER);

        // Summary Panel (Total + Checkout Button)
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS)); // Vertical layout
        summaryPanel.setBackground(ThemeColors.CARD_BG); // Different background for summary
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Total Amount Row
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        totalPanel.setBackground(ThemeColors.CARD_BG); // Match summary panel bg

        JLabel totalLabel = new JLabel("Total Amount (Selected):");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalLabel.setForeground(ThemeColors.TEXT);

        cartTotalLabel = new JLabel("₱0.00"); // Initialize the total label
        cartTotalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        cartTotalLabel.setForeground(ThemeColors.PRIMARY); // Highlight total

        totalPanel.add(totalLabel);
        totalPanel.add(cartTotalLabel);
        summaryPanel.add(totalPanel);

        summaryPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Spacer

        // Checkout Button
        JButton checkoutButton = createStyledButton("PROCEED TO CHECKOUT", ThemeColors.PRIMARY);
        checkoutButton.setFont(new Font("Arial", Font.BOLD, 16));
        checkoutButton.setAlignmentX(Component.CENTER_ALIGNMENT); // Center horizontally
        checkoutButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, checkoutButton.getPreferredSize().height));
        checkoutButton.addActionListener(e -> checkout());
        summaryPanel.add(checkoutButton);

        cartContentPanel.add(summaryPanel, BorderLayout.SOUTH); // Add summary to bottom
        panel.add(cartContentPanel, BorderLayout.CENTER); // Add content to main panel

        return panel;
    }


    // Removed color, size parameters and display
    private JPanel createCartItemPanel(int cartId, String productName, double price, int quantity, String imagePathFromDB) {
        JPanel itemPanel = new JPanel(new BorderLayout(10, 10));
        itemPanel.setBackground(ThemeColors.CARD_BG);
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.BACKGROUND),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        itemPanel.putClientProperty("cartId", cartId);
        itemPanel.putClientProperty("price", price);
        itemPanel.putClientProperty("quantity", quantity);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 0));
        contentPanel.setOpaque(false);

        JPanel leftPanel = new JPanel(new BorderLayout(10, 0));
        leftPanel.setOpaque(false);

        JCheckBox selectCheckbox = createCircularCheckbox(cartId, price, quantity);
        selectCheckbox.setSelected(false);
        JPanel checkboxPanel = new JPanel(new GridBagLayout());
        checkboxPanel.setOpaque(false);
        checkboxPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));
        checkboxPanel.add(selectCheckbox);
        leftPanel.add(checkboxPanel, BorderLayout.WEST);

        JPanel imagePanel = new JPanel(new GridBagLayout());
        imagePanel.setPreferredSize(new Dimension(80, 80));
        imagePanel.setBackground(ThemeColors.BACKGROUND);
        imagePanel.setBorder(BorderFactory.createLineBorder(ThemeColors.SECONDARY, 1));

        ImageIcon icon = loadImageIcon(imagePathFromDB, productName);
        if (icon != null && isIconValid(icon)) { // Use the public static method
            Image scaledImage = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
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

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(productName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(ThemeColors.TEXT);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel priceLabel = new JLabel(String.format("₱%.2f", price));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        priceLabel.setForeground(ThemeColors.PRIMARY);
        priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        centerPanel.add(nameLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        centerPanel.add(priceLabel);

        contentPanel.add(centerPanel, BorderLayout.CENTER);

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
        itemPanel.putClientProperty("qtyLabel", qtyLabel);

        JButton minusButton = new JButton("-");
        styleQuantityButton(minusButton);
        minusButton.addActionListener(e -> {
            int currentQuantity = Integer.parseInt(qtyLabel.getText());
            if (currentQuantity > 1) {
                updateCartItemQuantity(cartId, currentQuantity - 1, itemPanel);
            } else {
                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "Remove this item from your cart?",
                        "Confirm Removal",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    removeCartItem(cartId);
                }
            }
        });

        JButton plusButton = new JButton("+");
        styleQuantityButton(plusButton);
        plusButton.addActionListener(e -> {
            int newQuantity = Integer.parseInt(qtyLabel.getText()) + 1;
            updateCartItemQuantity(cartId, newQuantity, itemPanel);
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
        removeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        removeButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to remove this item from your cart?",
                    "Confirm Removal",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                removeCartItem(cartId);
            }
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


    // Creates a custom circular checkbox
    private JCheckBox createCircularCheckbox(int id, double price, int quantity) {
        JCheckBox checkbox = new JCheckBox() {
            @Override
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int diameter = Math.min(getWidth(), getHeight()) - 2;
                int x = (getWidth() - diameter) / 2;
                int y = (getHeight() - diameter) / 2;

                g2.setColor(ThemeColors.BACKGROUND.brighter());
                g2.fillOval(x, y, diameter, diameter);
                g2.setColor(ThemeColors.PRIMARY);
                g2.drawOval(x, y, diameter, diameter);

                if (isSelected()) {
                    g2.setColor(ThemeColors.PRIMARY);
                    int checkX = x + diameter / 4;
                    int checkY = y + diameter / 4;
                    int checkSize = diameter / 2;
                    g2.fillOval(checkX, checkY, checkSize, checkSize);
                }
                g2.dispose();
            }
        };
        checkbox.setSelected(false);
        checkbox.setPreferredSize(new Dimension(20, 20));
        checkbox.setBorder(BorderFactory.createEmptyBorder());
        checkbox.setContentAreaFilled(false);
        checkbox.setFocusPainted(false);
        checkbox.setOpaque(false);
        checkbox.putClientProperty("id", id); // Use 'id' for both cart and wishlist/product
        checkbox.putClientProperty("price", price);
        checkbox.putClientProperty("quantity", quantity);
        checkbox.addActionListener(e -> calculateSelectedTotal()); // Action is relevant for cart total
        return checkbox;
    }

    // Styles the small +/- buttons for quantity
    private void styleQuantityButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(ThemeColors.SECONDARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(25, 25));
        button.setBorder(BorderFactory.createLineBorder(ThemeColors.SECONDARY.darker(), 1));
        button.setMargin(new Insets(0,0,0,0));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // Updates quantity in DB and UI, handles stock
    private void updateCartItemQuantity(int cartId, int newQuantity, JPanel itemPanel) {
         if (newQuantity < 1) {
             removeCartItem(cartId); // Remove if quantity drops below 1
             return;
         }

        // NOTE: This method modifies stock directly, which might conflict
        // with stock handling in CheckoutFrame's placeOrder.
        // Consider REMOVING stock updates here and letting placeOrder handle final stock deduction.
        // For now, keeping the stock update logic but acknowledging the potential conflict.

        String checkStockSql = "SELECT stock FROM products WHERE id = (SELECT product_id FROM cart WHERE id = ?)";
        String updateCartSql = "UPDATE cart SET quantity = ? WHERE id = ?";
        // String updateStockSql = "UPDATE products SET stock = ? WHERE id = (SELECT product_id FROM cart WHERE id = ?)"; // Original update logic

        Connection conn = null;
        PreparedStatement checkStmt = null;
        PreparedStatement cartStmt = null;
        // PreparedStatement stockStmt = null; // REMOVED direct stock update

        try {
            conn = DBConnection.connect();
            conn.setAutoCommit(false); // Transaction control

            // Check available stock BEFORE updating cart
            int currentStock = 0;
            checkStmt = conn.prepareStatement(checkStockSql);
            checkStmt.setInt(1, cartId);
            ResultSet stockRs = checkStmt.executeQuery();
            if (stockRs.next()) {
                currentStock = stockRs.getInt("stock");
            } else {
                throw new SQLException("Could not find product stock information for cartId: " + cartId);
            }
            stockRs.close();

            if (newQuantity > currentStock) {
                 JOptionPane.showMessageDialog(this,
                         "Not enough stock available. Only " + currentStock + " left.",
                         "Stock Error", JOptionPane.ERROR_MESSAGE);
                 conn.rollback(); // Rollback transaction
                 // Reset spinner in UI if panel is provided
                 if(itemPanel != null) {
                    JLabel qtyLabel = (JLabel) itemPanel.getClientProperty("qtyLabel");
                    if (qtyLabel != null) qtyLabel.setText(String.valueOf(currentStock)); // Reset to max available
                 }
                 return;
            }

            // Update cart quantity
            cartStmt = conn.prepareStatement(updateCartSql);
            cartStmt.setInt(1, newQuantity);
            cartStmt.setInt(2, cartId);
            cartStmt.executeUpdate();

            // ** REMOVED direct stock update from here **
            // Stock deduction will happen during the `placeOrder` transaction in CheckoutFrame.

            conn.commit(); // Commit cart quantity update

            // Update UI components if panel is provided
            if (itemPanel != null) {
                JLabel qtyLabel = (JLabel) itemPanel.getClientProperty("qtyLabel");
                if (qtyLabel != null) {
                    qtyLabel.setText(String.valueOf(newQuantity));
                }
                JCheckBox checkbox = findCheckboxInPanel(itemPanel);
                if (checkbox != null) {
                    checkbox.putClientProperty("quantity", newQuantity); // Update quantity in checkbox property
                }
                 itemPanel.putClientProperty("quantity", newQuantity); // Update quantity in panel property
            }

            calculateSelectedTotal(); // Recalculate total based on selection
            updateCartBadge(); // Update badge count

        } catch (SQLException ex) {
            System.err.println("Error updating quantity for cartId " + cartId + ": " + ex.getMessage());
            if (conn != null) try { conn.rollback(); } catch (SQLException e) { e.printStackTrace(); } // Rollback on error
            JOptionPane.showMessageDialog(this,
                    "Error updating quantity: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Close resources
            try { if (checkStmt != null) checkStmt.close(); } catch (SQLException ignored) {}
            try { if (cartStmt != null) cartStmt.close(); } catch (SQLException ignored) {}
            // try { if (stockStmt != null) stockStmt.close(); } catch (SQLException ignored) {} // REMOVED
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }


    // Calculates total based on *selected* checkboxes in the cart
    private void calculateSelectedTotal() {
        double total = 0.0;
         if (cartItemsPanel == null) {
             System.err.println("calculateSelectedTotal called but cartItemsPanel is null.");
             if (cartTotalLabel != null) cartTotalLabel.setText("₱0.00");
             return;
         }

        for (Component comp : cartItemsPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel itemPanel = (JPanel) comp;
                if (itemPanel.getClientProperty("cartId") == null) continue; // Skip non-cart-item panels

                JCheckBox checkbox = findCheckboxInPanel(itemPanel); // Find checkbox within panel

                if (checkbox != null && checkbox.isSelected()) {
                    try {
                        // Retrieve price and quantity stored in the checkbox properties
                        Object priceObj = checkbox.getClientProperty("price");
                        Object quantityObj = checkbox.getClientProperty("quantity");

                        if (priceObj instanceof Double && quantityObj instanceof Integer) {
                            double price = (Double) priceObj;
                            int quantity = (Integer) quantityObj;
                            total += price * quantity; // Add to total if selected
                        } else {
                            // Log warning if properties are missing or wrong type
                            System.err.println("Warning: Invalid data type found in checkbox client properties for cart item panel with cartId=" + itemPanel.getClientProperty("cartId"));
                        }
                    } catch (Exception e) {
                        // Catch potential ClassCastException or NullPointerException
                        System.err.println("Error calculating total from checkbox properties: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
        // Update the total label in the UI
        if (cartTotalLabel != null) {
             cartTotalLabel.setText(String.format("₱%.2f", total));
        }
    }

    // Removes item from DB and UI. Stock is NOT restored here, as it wasn't decremented on add.
    private void removeCartItem(int cartId) {
        String deleteSql = "DELETE FROM cart WHERE id = ? AND customer_id = ?"; // Add customer_id check

        Connection conn = null;
        PreparedStatement deleteStmt = null;

        try {
            conn = DBConnection.connect();
            // No transaction needed for simple delete

            // Delete item from cart
            deleteStmt = conn.prepareStatement(deleteSql);
            deleteStmt.setInt(1, cartId);
            deleteStmt.setInt(2, customerId); // Ensure correct customer
            int rowsAffected = deleteStmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Cart item " + cartId + " removed for customer " + customerId);
                // Refresh UI
                loadCartItems();
                updateCartBadge();
            } else {
                 System.err.println("Warning: removeCartItem affected 0 rows for cartId " + cartId);
                 // Optionally show a message, or just log
            }


        } catch (SQLException ex) {
            System.err.println("Error removing item (cartId " + cartId + "): " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error removing item from cart: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            // Refresh UI even on error to reflect DB state if possible
            loadCartItems();
            updateCartBadge();
        } finally {
            // Close resources
            try { if (deleteStmt != null) deleteStmt.close(); } catch (SQLException ignored) {}
            try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }
    }


    private JPanel createWishlistPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeColors.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JButton backButton = new JButton("← Back to Home");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(ThemeColors.CARD_BG);
        backButton.setForeground(ThemeColors.TEXT);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        backButton.addActionListener(e -> showCard("Home"));
        panel.add(backButton, BorderLayout.NORTH);

        JPanel wishlistContentPanel = new JPanel(new BorderLayout());
        wishlistContentPanel.setBackground(ThemeColors.BACKGROUND);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(ThemeColors.BACKGROUND);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        JLabel title = new JLabel("Your Wishlist");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(ThemeColors.PRIMARY);
        titlePanel.add(title);
        wishlistContentPanel.add(titlePanel, BorderLayout.NORTH);

        JPanel wishlistItemsPanel = new JPanel();
        wishlistItemsPanel.setLayout(new BoxLayout(wishlistItemsPanel, BoxLayout.Y_AXIS));
        wishlistItemsPanel.setBackground(ThemeColors.BACKGROUND);
        wishlistItemsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Store reference to items panel for easier access later
        panel.putClientProperty("wishlistItemsPanel", wishlistItemsPanel);

        JScrollPane scrollPane = new JScrollPane(wishlistItemsPanel);
        styleScrollPane(scrollPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ThemeColors.BACKGROUND);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        wishlistContentPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel for bottom buttons (Remove Selected, Move to Cart)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(ThemeColors.CARD_BG);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton removeSelectedButton = createStyledButton("REMOVE SELECTED", new Color(200, 0, 0)); // Red for remove
        removeSelectedButton.setFont(new Font("Arial", Font.BOLD, 16));
        removeSelectedButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeSelectedButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, removeSelectedButton.getPreferredSize().height)); // Stretch width
        removeSelectedButton.addActionListener(e -> removeSelectedWishlistItems());
        buttonPanel.add(removeSelectedButton);

        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Spacer

        JButton moveToCartButton = createStyledButton("MOVE SELECTED TO CART", ThemeColors.PRIMARY);
        moveToCartButton.setFont(new Font("Arial", Font.BOLD, 16));
        moveToCartButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        moveToCartButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, moveToCartButton.getPreferredSize().height)); // Stretch width
        moveToCartButton.addActionListener(e -> moveSelectedToCart());
        buttonPanel.add(moveToCartButton);

        wishlistContentPanel.add(buttonPanel, BorderLayout.SOUTH);
        panel.add(wishlistContentPanel, BorderLayout.CENTER);

        return panel;
    }

    // Removed color/size display
    private JPanel createWishlistItemPanel(int productId, String productName, double price, String description, String imagePathFromDB) {
        JPanel itemPanel = new JPanel(new BorderLayout(10, 10));
        itemPanel.setBackground(ThemeColors.CARD_BG);
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.BACKGROUND),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120)); // Limit height

        // Store data in panel properties
        itemPanel.putClientProperty("productId", productId);
        itemPanel.putClientProperty("productName", productName);
        itemPanel.putClientProperty("price", price);
        itemPanel.putClientProperty("description", description);
        itemPanel.putClientProperty("imagePath", imagePathFromDB);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 0));
        contentPanel.setOpaque(false); // Transparent background

        // Left side: Checkbox and Image
        JPanel leftPanel = new JPanel(new BorderLayout(10, 0));
        leftPanel.setOpaque(false);

        // Checkbox for selection
        JCheckBox selectCheckbox = createCircularCheckbox(productId, price, 1); // Wishlist item quantity is 1 conceptually
        selectCheckbox.setSelected(false);
        JPanel checkboxPanel = new JPanel(new GridBagLayout()); // To center checkbox vertically
        checkboxPanel.setOpaque(false);
        checkboxPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10)); // Padding
        checkboxPanel.add(selectCheckbox);
        leftPanel.add(checkboxPanel, BorderLayout.WEST);

        // Image
        JPanel imagePanel = new JPanel(new GridBagLayout()); // Center image
        imagePanel.setPreferredSize(new Dimension(80, 80));
        imagePanel.setBackground(ThemeColors.BACKGROUND);
        imagePanel.setBorder(BorderFactory.createLineBorder(ThemeColors.SECONDARY, 1));

        ImageIcon icon = loadImageIcon(imagePathFromDB, productName);
        if (icon != null && isIconValid(icon)) { // Use the public static method
            Image scaledImage = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
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

        // Center: Product Info (Name, Desc, Price)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(productName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(ThemeColors.TEXT);

        // Show short description, no color/size here
        String shortDesc = (description != null && description.length() > 50)
                ? description.substring(0, 50) + "..."
                : (description != null ? description : ""); // Handle null description
        JLabel descLabel = new JLabel("<html><body style='width: 300px'>" + shortDesc + "</body></html>"); // HTML for wrapping
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

        // Right side: Buttons (Details, Add to Cart)
        JPanel rightPanel = new JPanel(new GridBagLayout()); // Center buttons vertically
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(150, 80)); // Fixed width for alignment

        JPanel buttonStack = new JPanel();
        buttonStack.setLayout(new BoxLayout(buttonStack, BoxLayout.Y_AXIS)); // Stack buttons vertically
        buttonStack.setOpaque(false);

        JButton detailsButton = createStyledButton("Details", ThemeColors.SECONDARY);
        detailsButton.setFont(new Font("Arial", Font.BOLD, 12));
        detailsButton.setAlignmentX(Component.CENTER_ALIGNMENT); // Center horizontally in the stack
        detailsButton.addActionListener(e -> showProductDetails(productId, productName, price, description, imagePathFromDB));

        JButton addToCartButton = createStyledButton("Add to Cart", ThemeColors.PRIMARY);
        addToCartButton.setFont(new Font("Arial", Font.BOLD, 12));
        addToCartButton.setAlignmentX(Component.CENTER_ALIGNMENT); // Center horizontally
        addToCartButton.addActionListener(e -> {
            addToCart(productId, productName, price); // Call addToCart directly
            // Ask user if they want to remove from wishlist after adding to cart
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Item added to cart. Remove it from your wishlist?",
                    "Wishlist Action", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                removeFromWishlist(productId); // Call remove method
            }
        });

        // Make buttons same width for alignment
        Dimension detailsSize = detailsButton.getPreferredSize();
        Dimension cartSize = addToCartButton.getPreferredSize();
        int maxWidthBtns = Math.max(detailsSize.width, cartSize.width);
        maxWidthBtns = Math.max(maxWidthBtns, 100); // Minimum
        Dimension uniformButtonSize = new Dimension(maxWidthBtns, detailsSize.height);

        detailsButton.setPreferredSize(uniformButtonSize);
        detailsButton.setMaximumSize(uniformButtonSize);
        addToCartButton.setPreferredSize(uniformButtonSize);
        addToCartButton.setMaximumSize(uniformButtonSize);

        buttonStack.add(detailsButton);
        buttonStack.add(Box.createRigidArea(new Dimension(0, 5))); // Spacer
        buttonStack.add(addToCartButton);


        rightPanel.add(buttonStack); // Add the button stack to the centering panel
        contentPanel.add(rightPanel, BorderLayout.EAST);

        itemPanel.add(contentPanel, BorderLayout.CENTER);
        return itemPanel;
    } // --- End of createWishlistItemPanel ---


    private void removeSelectedWishlistItems() {
        Object panelObj = wishlistPanel.getClientProperty("wishlistItemsPanel");
         if (!(panelObj instanceof JPanel)) {
             System.err.println("Error: wishlistItemsPanel not found in client properties.");
             JOptionPane.showMessageDialog(this, "Error accessing wishlist items.", "Internal Error", JOptionPane.ERROR_MESSAGE);
             return;
         }
         JPanel wishlistItemsPanel = (JPanel) panelObj;


        List<Integer> productIdsToRemove = new ArrayList<>();

        // Iterate through components in the wishlist panel
        for (Component comp : wishlistItemsPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel itemPanel = (JPanel) comp;
                // Ensure it's a wishlist item panel by checking for productId property
                if (!(itemPanel.getClientProperty("productId") instanceof Integer)) continue;

                JCheckBox checkbox = findCheckboxInPanel(itemPanel); // Find the checkbox
                if (checkbox != null && checkbox.isSelected()) {
                    // Use the 'id' property from the checkbox (which stores the product ID)
                    Integer productId = (Integer) checkbox.getClientProperty("id");
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
                "Are you sure you want to remove the selected " + productIdsToRemove.size() + " item(s) from your wishlist?",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.connect();
            conn.setAutoCommit(false); // Use transaction for batch delete
            String sql = "DELETE FROM wishlist WHERE customer_id = ? AND product_id = ?";
            stmt = conn.prepareStatement(sql);

            // Add each selected product ID to the batch delete
            for (int productId : productIdsToRemove) {
                stmt.setInt(1, customerId);
                stmt.setInt(2, productId);
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch(); // Execute the batch delete
            conn.commit(); // Commit transaction

            // Check results (optional, for logging/feedback)
            int totalDeleted = 0;
            for(int result : results) {
                if (result >= 0) totalDeleted += result; // SUCCESS_NO_INFO or rows affected
                else if (result == Statement.EXECUTE_FAILED) {
                    System.err.println("Warning: Batch delete item failed for one of the products.");
                }
            }
            System.out.println("Attempted to delete " + productIdsToRemove.size() + " items, successful deletes/no-info: " + totalDeleted);


            // Refresh UI
            loadWishlist();
            updateWishlistBadge();

            JOptionPane.showMessageDialog(this,
                    totalDeleted + " item(s) removed from wishlist.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException ex) {
            if (conn != null) try { conn.rollback(); } catch (SQLException e) { e.printStackTrace(); } // Rollback on error
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error removing items from wishlist: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException ignored) {}
             if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }

    // Modified to not handle color/size when adding to cart
    private void moveSelectedToCart() {
        Object panelObj = wishlistPanel.getClientProperty("wishlistItemsPanel");
         if (!(panelObj instanceof JPanel)) {
             System.err.println("Error: wishlistItemsPanel not found in client properties.");
              JOptionPane.showMessageDialog(this, "Error accessing wishlist items.", "Internal Error", JOptionPane.ERROR_MESSAGE);
             return;
         }
         JPanel wishlistItemsPanel = (JPanel) panelObj;

        List<Integer> productIdsToMove = new ArrayList<>();
        boolean anyMoved = false;

        // Collect selected product IDs from wishlist
        for (Component comp : wishlistItemsPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel itemPanel = (JPanel) comp;
                if (!(itemPanel.getClientProperty("productId") instanceof Integer)) continue;

                JCheckBox checkbox = findCheckboxInPanel(itemPanel);
                if (checkbox != null && checkbox.isSelected()) {
                     // Use 'id' property from checkbox which holds product ID
                     Integer productId = (Integer) checkbox.getClientProperty("id");
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
                "Move selected " + productIdsToMove.size() + " item(s) to cart and remove from wishlist?",
                "Confirm Move",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        Connection conn = null;
        PreparedStatement deleteStmt = null;
        PreparedStatement insertStmt = null;
        PreparedStatement selectStmt = null;
        PreparedStatement checkStockStmt = null;
        // PreparedStatement updateStockStmt = null; // REMOVED Stock update
        int itemsMovedCount = 0;
        List<Integer> successfullyMovedIds = new ArrayList<>();
        List<String> stockIssues = new ArrayList<>();

        try {
            conn = DBConnection.connect();
            conn.setAutoCommit(false); // Transaction control

            // SQL Statements (do not involve color/size for cart insert)
            String selectSql = "SELECT name, price FROM products WHERE id = ?"; // Get name/price for cart
            String insertSql = "INSERT INTO cart (product_id, product_name, price, quantity, customer_id) " +
                    "VALUES (?, ?, ?, 1, ?) ON DUPLICATE KEY UPDATE quantity = quantity + 1"; // Add/increment cart
            String deleteSql = "DELETE FROM wishlist WHERE customer_id = ? AND product_id = ?"; // Remove from wishlist
            String checkStockSql = "SELECT stock FROM products WHERE id = ?"; // Check stock
            // String decStockSql = "UPDATE products SET stock = stock - 1 WHERE id = ? AND stock > 0"; // REMOVED

            selectStmt = conn.prepareStatement(selectSql);
            insertStmt = conn.prepareStatement(insertSql);
            deleteStmt = conn.prepareStatement(deleteSql);
            checkStockStmt = conn.prepareStatement(checkStockSql);
            // updateStockStmt = conn.prepareStatement(decStockSql); // REMOVED

            for (int productId : productIdsToMove) {
                // 1. Check stock
                checkStockStmt.setInt(1, productId);
                ResultSet stockRs = checkStockStmt.executeQuery();
                int stock = 0;
                String productNameForMsg = "Product ID " + productId; // Fallback name
                if (stockRs.next()) {
                    stock = stockRs.getInt("stock");
                }
                stockRs.close();
                checkStockStmt.clearParameters();

                // 2. Get product name and price
                 selectStmt.setInt(1, productId);
                 ResultSet namePriceRs = selectStmt.executeQuery();
                 String productName = null;
                 Double price = null;
                 if (namePriceRs.next()) {
                     productName = namePriceRs.getString("name");
                     price = namePriceRs.getDouble("price");
                     productNameForMsg = productName; // Use actual name if found
                 }
                 namePriceRs.close();
                 selectStmt.clearParameters();

                 // 3. Validate product details and stock
                 if (productName == null || price == null) {
                     System.err.println("Warning: Could not find product details for ID: " + productId + ". Skipping move.");
                     stockIssues.add(productNameForMsg + " (Details not found)");
                     continue; // Skip this item
                 }
                 if (stock <= 0) {
                     stockIssues.add(productNameForMsg + " (Out of Stock)");
                     continue; // Skip this item
                 }

                 // 4. Attempt to add to cart (or increment quantity)
                 // Stock will be handled during checkout, not here.
                 insertStmt.setInt(1, productId);
                 insertStmt.setString(2, productName);
                 insertStmt.setDouble(3, price);
                 insertStmt.setInt(4, customerId);
                 int cartRowsAffected = insertStmt.executeUpdate();
                 insertStmt.clearParameters();

                 if (cartRowsAffected > 0) {
                     // Mark as successfully moved ( conceptually, added to cart)
                     successfullyMovedIds.add(productId);
                     itemsMovedCount++;
                     anyMoved = true;
                 } else {
                      // Should not happen with ON DUPLICATE KEY unless DB error
                      stockIssues.add(productNameForMsg + " (Failed to add to cart)");
                 }

            } // End loop through product IDs

            // 5. If any items were successfully moved to cart, remove them from wishlist
            if (!successfullyMovedIds.isEmpty()) {
                for (int movedId : successfullyMovedIds) {
                    deleteStmt.setInt(1, customerId);
                    deleteStmt.setInt(2, movedId);
                    deleteStmt.addBatch();
                }
                deleteStmt.executeBatch(); // Execute batch delete from wishlist
            }

            conn.commit(); // Commit the entire transaction

        } catch (SQLException ex) {
            anyMoved = false; // Ensure flag is false on error
            if (conn != null) try { conn.rollback(); } catch (SQLException e) { e.printStackTrace(); } // Rollback on error
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error moving items: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Close resources in finally block
            try { if (selectStmt != null) selectStmt.close(); } catch (SQLException ignored) {}
            try { if (insertStmt != null) insertStmt.close(); } catch (SQLException ignored) {}
            try { if (deleteStmt != null) deleteStmt.close(); } catch (SQLException ignored) {}
            try { if (checkStockStmt != null) checkStockStmt.close(); } catch (SQLException ignored) {}
            // try { if (updateStockStmt != null) updateStockStmt.close(); } catch (SQLException ignored) {} // REMOVED
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }

        // --- Report Results to User ---
        StringBuilder resultMessage = new StringBuilder();
        if (anyMoved) {
            resultMessage.append(itemsMovedCount).append(" item(s) moved to cart and removed from wishlist.");
            // Refresh relevant UI parts
            loadWishlist();
            updateWishlistBadge();
            updateCartBadge();
            if (currentCard.equals("Cart")) { // If user is currently viewing cart, refresh it too
                loadCartItems();
            }
        }

        // Append stock issues if any occurred
        if (!stockIssues.isEmpty()) {
             if (resultMessage.length() > 0) resultMessage.append("\n\n"); // Add separator if needed
            resultMessage.append("Could not move the following items:\n"); // Simpler message
            for(String issue : stockIssues) {
                resultMessage.append("- ").append(issue).append("\n");
            }
        }

        // Show the final message dialog if there's anything to report
        if (resultMessage.length() > 0) {
            JOptionPane.showMessageDialog(this,
                    resultMessage.toString(),
                    anyMoved ? "Move Results" : "Move Issues", // Title based on success
                    anyMoved ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE); // Icon based on success
        } else if (!productIdsToMove.isEmpty() && !anyMoved && stockIssues.isEmpty()) {
            // Case where the loop ran but nothing was moved and no specific stock issue logged (e.g., initial DB error)
             JOptionPane.showMessageDialog(this,
                     "Move operation failed. No items were moved.",
                     "Move Failed", JOptionPane.ERROR_MESSAGE);
        }
        // If productIdsToMove was empty initially, no message is shown (handled by initial check)
    }


    private JPanel createOrderTrackingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeColors.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JButton backButton = new JButton("← Back to Home");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(ThemeColors.CARD_BG);
        backButton.setForeground(ThemeColors.TEXT);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        backButton.addActionListener(e -> showCard("Home"));
        panel.add(backButton, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(ThemeColors.BACKGROUND);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(ThemeColors.BACKGROUND);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        JLabel title = new JLabel("Your Orders");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(ThemeColors.PRIMARY);
        titlePanel.add(title);
        contentPanel.add(titlePanel, BorderLayout.NORTH);

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(ThemeColors.BACKGROUND);

        JPanel ordersPanel = new JPanel();
        ordersPanel.setLayout(new BoxLayout(ordersPanel, BoxLayout.Y_AXIS));
        ordersPanel.setBackground(ThemeColors.BACKGROUND);
        ordersPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Store reference for easy access
        panel.putClientProperty("ordersPanel", ordersPanel);

        JScrollPane scrollPane = new JScrollPane(ordersPanel);
        styleScrollPane(scrollPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ThemeColors.BACKGROUND);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        wrapperPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(wrapperPanel, BorderLayout.CENTER);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }


    // Removed color/size display
    private JPanel createOrderItemPanel(int orderId, String productName, int quantity,
                                        String status, String date, double price, String imagePathFromDB, boolean cancellationRequested) {
        JPanel itemPanel = new JPanel(new BorderLayout(10, 10));
        itemPanel.setBackground(ThemeColors.CARD_BG);
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.BACKGROUND),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170)); // Max height
        itemPanel.setPreferredSize(new Dimension(800, 160)); // Preferred size

        // Store data in panel properties for later access (e.g., by buttons)
        itemPanel.putClientProperty("orderId", orderId);
        int productId = getProductIdFromName(productName); // Helper to get ID if needed
        itemPanel.putClientProperty("productId", productId);
        itemPanel.putClientProperty("status", status);
        itemPanel.putClientProperty("cancellationRequested", cancellationRequested);


        JPanel contentPanel = new JPanel(new BorderLayout(10, 0));
        contentPanel.setOpaque(false); // Transparent

        // Left: Image
        JPanel leftPanel = new JPanel(new BorderLayout(10, 0));
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(100, 120)); // Fixed size for image area

        JPanel imagePanel = new JPanel(new GridBagLayout()); // Center image
        imagePanel.setPreferredSize(new Dimension(100, 100));
        imagePanel.setBackground(ThemeColors.BACKGROUND);
        imagePanel.setBorder(BorderFactory.createLineBorder(ThemeColors.SECONDARY, 1));

        ImageIcon icon = loadImageIcon(imagePathFromDB, productName);
        if (icon != null && isIconValid(icon)) { // Use the public static method
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

        // Center: Order/Item Info
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0)); // Padding

        JLabel orderIdLabel = new JLabel("Order #" + orderId);
        orderIdLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        orderIdLabel.setForeground(Color.GRAY);

        JLabel nameLabel = new JLabel(productName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(ThemeColors.TEXT);

        JLabel quantityLabel = new JLabel("Quantity: " + quantity);
        quantityLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        quantityLabel.setForeground(ThemeColors.TEXT);

        JLabel priceLabel = new JLabel(String.format("Item Price: ₱%.2f", price));
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        priceLabel.setForeground(ThemeColors.TEXT);

        JLabel itemTotalLabel = new JLabel(String.format("Item Total: ₱%.2f", price * quantity));
        itemTotalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        itemTotalLabel.setForeground(ThemeColors.PRIMARY);


        JLabel statusLabel = new JLabel("Status: " + status);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        // Color code status
        switch (status.toLowerCase()) {
            case "delivered": case "completed": statusLabel.setForeground(new Color(0, 150, 0)); break; // Green
            case "processing": statusLabel.setForeground(new Color(200, 150, 0)); break; // Orange/Yellow
            case "shipped": statusLabel.setForeground(new Color(0, 100, 200)); break; // Blue
            case "cancelled": statusLabel.setForeground(Color.RED); break; // Red
            default: statusLabel.setForeground(ThemeColors.TEXT);
        }
        // Add note if cancellation is requested
        if (cancellationRequested && status.equalsIgnoreCase("Processing")) {
            statusLabel.setText("<html>Status: " + status + "<br><font color='magenta'>(Cancellation Requested)</font></html>");
        }

        JLabel dateLabel = new JLabel("Order Date: " + date);
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        dateLabel.setForeground(ThemeColors.TEXT);

        // Align labels left
        orderIdLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        quantityLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        itemTotalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add components with spacing
        centerPanel.add(orderIdLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        centerPanel.add(nameLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        centerPanel.add(quantityLabel);
        centerPanel.add(priceLabel);
        centerPanel.add(itemTotalLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        centerPanel.add(statusLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        centerPanel.add(dateLabel);

        contentPanel.add(centerPanel, BorderLayout.CENTER);

        // Right: Buttons
        JPanel rightButtonPanel = new JPanel();
        rightButtonPanel.setLayout(new BoxLayout(rightButtonPanel, BoxLayout.Y_AXIS)); // Stack buttons
        rightButtonPanel.setOpaque(false);
        rightButtonPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 5)); // Padding

        JButton detailsButton = createStyledButton("View Details", ThemeColors.SECONDARY);
        detailsButton.addActionListener(e -> showOrderDetails(orderId));

        JButton reviewButton = createStyledButton("Leave Review", ThemeColors.PRIMARY);
        // Enable review only for delivered/completed orders
        reviewButton.setEnabled(status.equalsIgnoreCase("Delivered") || status.equalsIgnoreCase("Completed"));
        reviewButton.addActionListener(e -> {
            // Retrieve product and order ID from panel properties
            Object pIdObj = itemPanel.getClientProperty("productId");
            Object orderIdObj = itemPanel.getClientProperty("orderId");

            if (pIdObj instanceof Integer && (Integer) pIdObj != -1 && orderIdObj instanceof Integer) {
                 // Placeholder: Launch review frame/dialog if implemented
                 // Assuming ProductReviewFrame exists and takes (customerId, productId, orderId)
                 new ProductReviewFrame(customerId, (Integer) pIdObj, (Integer) orderIdObj);
                 // JOptionPane.showMessageDialog(this, "Review functionality not implemented yet."); // Placeholder
            } else {
                JOptionPane.showMessageDialog(this, "Could not determine Product ID or Order ID for review.", "Error", JOptionPane.ERROR_MESSAGE);
                System.err.println("Review Error: Product ID=" + pIdObj + ", Order ID=" + orderIdObj);
            }
        });

        JButton cancelButton = createStyledButton("Request Cancel", new Color(200, 0, 0)); // Red button
        // Enable cancel request only if status is 'Processing' and not already requested
        cancelButton.setEnabled(status.equalsIgnoreCase("Processing") && !cancellationRequested);
        cancelButton.addActionListener(e -> requestOrderCancellation(orderId));
        // Change text/disable if cancellation is pending or not possible
        if (cancellationRequested && status.equalsIgnoreCase("Processing")) {
            cancelButton.setText("Cancel Pending");
            cancelButton.setEnabled(false);
        } else if (!status.equalsIgnoreCase("Processing")) {
             cancelButton.setEnabled(false);
             if (status.equalsIgnoreCase("Cancelled")) {
                 cancelButton.setText("Cancelled");
             }
        }

        // Make buttons same width
        Dimension detailsPref = detailsButton.getPreferredSize();
        Dimension reviewPref = reviewButton.getPreferredSize();
        Dimension cancelPref = cancelButton.getPreferredSize();
        int maxWidthBtns = Math.max(detailsPref.width, Math.max(reviewPref.width, cancelPref.width));
        maxWidthBtns = Math.max(maxWidthBtns, 120); // Min width
        Dimension uniformButtonSize = new Dimension(maxWidthBtns, 30); // Fixed height

        detailsButton.setPreferredSize(uniformButtonSize);
        detailsButton.setMaximumSize(uniformButtonSize);
        reviewButton.setPreferredSize(uniformButtonSize);
        reviewButton.setMaximumSize(uniformButtonSize);
        cancelButton.setPreferredSize(uniformButtonSize);
        cancelButton.setMaximumSize(uniformButtonSize);

        // Center buttons horizontally within the stack
        detailsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        reviewButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add buttons with glue/spacers for vertical centering/spacing
        rightButtonPanel.add(Box.createVerticalGlue()); // Push buttons to center
        rightButtonPanel.add(detailsButton);
        rightButtonPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        rightButtonPanel.add(reviewButton);
        rightButtonPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        rightButtonPanel.add(cancelButton);
        rightButtonPanel.add(Box.createVerticalGlue()); // Push buttons to center


        contentPanel.add(rightButtonPanel, BorderLayout.EAST);
        itemPanel.add(contentPanel, BorderLayout.CENTER);
        return itemPanel;
    }


    // Helper to get Product ID from Name (used for Review button)
    private int getProductIdFromName(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            return -1;
        }
        String sql = "SELECT id FROM products WHERE name = ? LIMIT 1";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching product ID by name '" + productName + "': " + e.getMessage());
        }
        System.err.println("Warning: Product ID not found for name: " + productName);
        return -1; // Return -1 if not found or error
    }

     private void requestOrderCancellation(int orderId) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to request cancellation for Order #" + orderId + "?\n" +
                        "This request needs admin approval and is only possible if the order is still 'Processing'.",
                "Confirm Cancellation Request",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            String checkStatusSql = "SELECT status, cancellation_requested FROM orders WHERE id = ?";
            String updateSql = "UPDATE orders SET cancellation_requested = 1 WHERE id = ? AND status = 'Processing' AND cancellation_requested = 0";

            Connection conn = null;
            PreparedStatement checkStmt = null;
            PreparedStatement updateStmt = null;
            try {
                conn = DBConnection.connect();
                conn.setAutoCommit(false); // Transaction control

                // Check current status and if already requested
                checkStmt = conn.prepareStatement(checkStatusSql);
                checkStmt.setInt(1, orderId);
                ResultSet rs = checkStmt.executeQuery();
                String currentStatus = null;
                boolean alreadyRequested = false;
                if (rs.next()) {
                    currentStatus = rs.getString("status");
                    alreadyRequested = rs.getBoolean("cancellation_requested");
                }
                rs.close();

                if (alreadyRequested) {
                     JOptionPane.showMessageDialog(this,
                            "Cancellation has already been requested for Order #" + orderId + ".",
                            "Request Already Submitted", JOptionPane.INFORMATION_MESSAGE);
                     conn.rollback(); // No change needed
                } else if ("Processing".equalsIgnoreCase(currentStatus)) {
                    // Attempt to set cancellation_requested flag
                    updateStmt = conn.prepareStatement(updateSql);
                    updateStmt.setInt(1, orderId);
                    int rowsAffected = updateStmt.executeUpdate();

                    if (rowsAffected > 0) {
                        conn.commit(); // Commit the change
                        JOptionPane.showMessageDialog(this,
                                "Cancellation requested for Order #" + orderId + ". Please wait for admin approval.",
                                "Request Submitted", JOptionPane.INFORMATION_MESSAGE);
                        loadOrders(); // Refresh the order list UI
                    } else {
                        // Update failed (e.g., status changed just before update)
                        conn.rollback();
                        JOptionPane.showMessageDialog(this,
                                "Could not submit cancellation request. The order status might have changed or request already submitted.",
                                "Request Failed", JOptionPane.ERROR_MESSAGE);
                        loadOrders(); // Refresh view even on failure
                    }
                } else {
                    // Order is not in 'Processing' status
                    conn.rollback();
                    JOptionPane.showMessageDialog(this,
                            "Cannot request cancellation. The order is no longer in 'Processing' status (Current: " + (currentStatus != null ? currentStatus : "Unknown") + ").",
                            "Request Failed", JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException ex) {
                if (conn != null) try { conn.rollback(); } catch (SQLException e) { e.printStackTrace(); } // Rollback on error
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error submitting cancellation request: " + ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                // Close resources
                try { if (checkStmt != null) checkStmt.close(); } catch (SQLException ignored) {}
                try { if (updateStmt != null) updateStmt.close(); } catch (SQLException ignored) {}
                if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }


    private void showOrderDetails(int orderId) {
        JDialog detailsDialog = new JDialog(this, "Order Details #" + orderId, true); // Modal dialog
        detailsDialog.setSize(550, 600); // Adjusted size
        detailsDialog.setLayout(new BorderLayout());
        detailsDialog.getContentPane().setBackground(ThemeColors.BACKGROUND);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(ThemeColors.BACKGROUND);

        JLabel titleLabel = new JLabel("Order Details #" + orderId, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(ThemeColors.PRIMARY);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Panel for general order info (Date, Status, Total)
        JPanel orderInfoPanel = new JPanel();
        orderInfoPanel.setLayout(new BoxLayout(orderInfoPanel, BoxLayout.Y_AXIS));
        orderInfoPanel.setOpaque(false); // Transparent background
        orderInfoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,1,0, ThemeColors.SECONDARY), // Bottom border
                BorderFactory.createEmptyBorder(10, 0, 10, 0) // Padding
        ));

        // Panel to hold the list of items (will be inside a scroll pane)
        JPanel itemDetailsPanel = new JPanel();
        itemDetailsPanel.setLayout(new BoxLayout(itemDetailsPanel, BoxLayout.Y_AXIS));
        itemDetailsPanel.setBackground(ThemeColors.CARD_BG.darker()); // Slightly darker background for items


        // SQL queries do not need color/size
        String orderSql = "SELECT o.status, o.order_date, o.total_price, o.cancellation_requested " +
                          "FROM orders o " +
                          "WHERE o.id = ?";
        String itemsSql = "SELECT oi.quantity, p.name, oi.price, p.image_path " + // Select only necessary fields
                "FROM order_items oi JOIN products p ON oi.product_id = p.id " +
                "WHERE oi.order_id = ?";

        Connection conn = null;
        PreparedStatement orderStmt = null;
        PreparedStatement itemsStmt = null;
        try {
            conn = DBConnection.connect();

            // Fetch general order details
            orderStmt = conn.prepareStatement(orderSql);
            orderStmt.setInt(1, orderId);
            ResultSet orderRs = orderStmt.executeQuery();

            if (orderRs.next()) {
                String status = orderRs.getString("status");
                String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(orderRs.getTimestamp("order_date"));
                double totalAmount = orderRs.getDouble("total_price");
                boolean cancellationRequested = orderRs.getBoolean("cancellation_requested");

                // Create and add labels for order info
                JLabel dateLabel = new JLabel("Order Date: " + dateStr);
                JLabel statusLabel = new JLabel("Status: " + status);
                // Color-code status label
                switch (status.toLowerCase()) {
                    case "delivered": case "completed": statusLabel.setForeground(new Color(0, 150, 0)); break;
                    case "processing": statusLabel.setForeground(new Color(200, 150, 0)); break;
                    case "shipped": statusLabel.setForeground(new Color(0, 100, 200)); break;
                    case "cancelled": statusLabel.setForeground(Color.RED); break;
                    default: statusLabel.setForeground(ThemeColors.TEXT);
                }
                // Add cancellation requested note
                if (cancellationRequested && status.equalsIgnoreCase("Processing")) {
                     statusLabel.setText("<html>Status: " + status + "<br><font color='magenta'>(Cancellation Requested)</font></html>");
                 }
                JLabel totalLabel = new JLabel(String.format("Order Total: ₱%.2f", totalAmount));

                // Apply styling
                Font plainFont = new Font("Arial", Font.PLAIN, 14);
                Font boldFont = new Font("Arial", Font.BOLD, 16);
                dateLabel.setFont(plainFont); dateLabel.setForeground(ThemeColors.TEXT);
                statusLabel.setFont(plainFont); // Color set above
                totalLabel.setFont(boldFont); totalLabel.setForeground(ThemeColors.PRIMARY);
                dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                totalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

                orderInfoPanel.add(dateLabel);
                orderInfoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                orderInfoPanel.add(statusLabel);
                orderInfoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                orderInfoPanel.add(totalLabel);

                // Fetch items for this order
                itemsStmt = conn.prepareStatement(itemsSql);
                itemsStmt.setInt(1, orderId);
                ResultSet itemsRs = itemsStmt.executeQuery();
                boolean hasItems = false;
                while(itemsRs.next()) {
                    hasItems = true;
                    String itemName = itemsRs.getString("name");
                    int itemQty = itemsRs.getInt("quantity");
                    double itemPrice = itemsRs.getDouble("price");
                    String itemImgPath = itemsRs.getString("image_path");

                    // Call helper to create item display (doesn't need color/size)
                    itemDetailsPanel.add(createMiniOrderItemPanel(itemName, itemQty, itemPrice, itemImgPath));
                    itemDetailsPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Spacer between items
                }
                 if (!hasItems) {
                     itemDetailsPanel.add(new JLabel("No items found for this order."));
                 }
                itemsRs.close();

            } else {
                // Order not found
                orderInfoPanel.add(new JLabel("Order details not found."));
                itemDetailsPanel.add(new JLabel("")); // Empty item panel
            }
            orderRs.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
            orderInfoPanel.removeAll(); // Clear panels on error
            orderInfoPanel.add(new JLabel("Error loading order details: " + ex.getMessage()));
             itemDetailsPanel.removeAll();
             itemDetailsPanel.add(new JLabel("Error loading items."));
        } finally {
            // Close resources
            try { if (orderStmt != null) orderStmt.close(); } catch (SQLException ignored) {}
            try { if (itemsStmt != null) itemsStmt.close(); } catch (SQLException ignored) {}
            try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }

        mainPanel.add(orderInfoPanel, BorderLayout.CENTER); // Add general info panel

        // Add item list panel inside a scroll pane
        JScrollPane itemsScrollPane = new JScrollPane(itemDetailsPanel);
        styleScrollPane(itemsScrollPane);
        // Add a titled border to the scroll pane
        itemsScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ThemeColors.SECONDARY), "Items in this Order",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 12), ThemeColors.TEXT));
        itemsScrollPane.getViewport().setBackground(itemDetailsPanel.getBackground()); // Match viewport bg
        itemsScrollPane.setPreferredSize(new Dimension(500, 250)); // Preferred size for item list
        mainPanel.add(itemsScrollPane, BorderLayout.SOUTH); // Add item list scroll pane below


        // Button panel at the bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false); // Transparent
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0)); // Top padding
        JButton closeButton = createStyledButton("Close", ThemeColors.SECONDARY);
        closeButton.addActionListener(e -> detailsDialog.dispose());
        buttonPanel.add(closeButton);

        detailsDialog.add(mainPanel, BorderLayout.CENTER); // Add main content
        detailsDialog.add(buttonPanel, BorderLayout.SOUTH); // Add close button panel
        detailsDialog.setLocationRelativeTo(this); // Center dialog
        detailsDialog.setVisible(true);
    }


    // Helper to create the small item display in the order details dialog
    // Does not use color/size
    private JPanel createMiniOrderItemPanel(String name, int quantity, double price, String imagePath) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setOpaque(false); // Transparent background
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Padding

        // Image on the left
        JLabel imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(50, 50));
        imageLabel.setBorder(BorderFactory.createLineBorder(ThemeColors.SECONDARY));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        ImageIcon icon = loadImageIcon(imagePath, name);
        if (icon != null && isIconValid(icon)) { // Use the public static method
            Image scaledImage = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
        } else {
            imageLabel.setText("N/A"); // Placeholder text
            imageLabel.setFont(new Font("Arial", Font.ITALIC, 10));
            imageLabel.setForeground(Color.GRAY);
        }
        panel.add(imageLabel, BorderLayout.WEST);

        // Info in the center (Name, Qty x Price)
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14)); nameLabel.setForeground(ThemeColors.TEXT);
        JLabel qtyPriceLabel = new JLabel(String.format("%d x ₱%.2f", quantity, price)); // Format: "2 x ₱100.00"
        qtyPriceLabel.setFont(new Font("Arial", Font.PLAIN, 12)); qtyPriceLabel.setForeground(ThemeColors.TEXT);

        infoPanel.add(nameLabel);
        infoPanel.add(qtyPriceLabel);
        panel.add(infoPanel, BorderLayout.CENTER);

        // Item total on the right
        JLabel itemTotalLabel = new JLabel(String.format("₱%.2f", quantity * price));
        itemTotalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        itemTotalLabel.setForeground(ThemeColors.PRIMARY);
        panel.add(itemTotalLabel, BorderLayout.EAST);

        panel.setAlignmentX(Component.LEFT_ALIGNMENT); // Align panel left within BoxLayout

        return panel;
    }


    // Helper to get image path (potentially redundant if already passed, but good practice)
    private String getProductImagePath(int productId) {
        if (productId == -1) return null;
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

    // --- Static Content Panels (Events, FAQ, Notice, About) ---
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
        eventContent.setText("UPCOMING EVENTS:\n\n" +
                "\u2600\uFE0F Summer Sale! \u2600\uFE0F\n" +
                "   - Enjoy 20% off ALL items storewide!\n" +
                "   - Dates: June 1st - June 30th\n\n" +
                "\uD83D\uDCBF New Album Pre-orders \uD83D\uDCBF\n" +
                "   - Pre-order the latest albums from top groups.\n" +
                "   - Starting: May 15th (Check specific artist pages)\n\n" +
                "\uD83C\uDFA4 Fan Meet & Greet Events \uD83C\uDFA4\n" +
                "   - Details and locations coming soon!\n" +
                "   - Tentative Dates: July 10th - 12th\n\n" +
                "\u2728 Exclusive Merch Drop \u2728\n" +
                "   - Limited edition lightsticks and photocards.\n" +
                "   - Date: May 20th - Don't miss out!");
        eventContent.setEditable(false);
        eventContent.setFont(new Font(Font.DIALOG, Font.PLAIN, 16));
        eventContent.setBackground(ThemeColors.CARD_BG);
        eventContent.setForeground(ThemeColors.TEXT);
        eventContent.setLineWrap(true);
        eventContent.setWrapStyleWord(true);
        eventContent.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeColors.SECONDARY),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JScrollPane scrollPane = new JScrollPane(eventContent);
        styleScrollPane(scrollPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        scrollPane.getViewport().setBackground(ThemeColors.BACKGROUND);

        panel.add(scrollPane, BorderLayout.CENTER);

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
                "Q: How long does shipping take?\n" +
                "A: Shipping times vary by location:\n" +
                "   - Domestic (Philippines): 3-5 business days.\n" +
                "   - International: 7-14 business days (may vary due to customs).\n\n" +
                "Q: What payment methods do you accept?\n" +
                "A: We accept major Credit Cards (Visa, Mastercard), PayPal, GCash, and Maya.\n\n" +
                "Q: Can I cancel my order?\n" +
                "A: You can request cancellation if your order status is still 'Processing' via the 'Orders' page. Cancellation is subject to admin approval and is not guaranteed.\n\n" +
                "Q: Do you offer refunds or returns?\n" +
                "A: Yes, we accept returns for defective or incorrect items within 30 days of delivery. Please contact customer service with your order details and photos of the issue.\n\n" +
                "Q: How can I track my order?\n" +
                "A: Once your order is shipped, you will receive a tracking number via email (if provided). You can also check the status on the 'Orders' page. Tracking details may take 24-48 hours to update after shipping.");
        faqContent.setEditable(false);
        faqContent.setFont(new Font("Arial", Font.PLAIN, 16));
        faqContent.setBackground(ThemeColors.CARD_BG);
        faqContent.setForeground(ThemeColors.TEXT);
        faqContent.setLineWrap(true);
        faqContent.setWrapStyleWord(true);
        faqContent.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeColors.SECONDARY),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JScrollPane scrollPane = new JScrollPane(faqContent);
        styleScrollPane(scrollPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        scrollPane.getViewport().setBackground(ThemeColors.BACKGROUND);

        panel.add(scrollPane, BorderLayout.CENTER);

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
                "\u26A0\uFE0F System Maintenance \u26A0\uFE0F\n" +
                "   - Scheduled for: May 25th, 1:00 AM - 3:00 AM (PHT/GMT+8)\n" +
                "   - The store may be temporarily unavailable during this time.\n\n" +
                "\uD83D\uDE9A Holiday Shipping Advisory \uD83D\uDE9A\n" +
                "   - Please anticipate potential shipping delays during upcoming holidays (e.g., Independence Day). Order early to ensure timely delivery!\n\n" +
                "\uD83D\uDD12 Security Update Reminder \uD83D\uDD12\n" +
                "   - We continuously work to enhance security. Always use strong passwords and be wary of phishing attempts.\n\n" +
                "\uD83C\uDFC6 Loyalty Program Update \uD83C\uDFC6\n" +
                "   - Point system adjustments are now live! Check your account for details.\n" +
                "   - More perks coming soon!");
        noticeContent.setEditable(false);
        noticeContent.setFont(new Font(Font.DIALOG, Font.PLAIN, 16));
        noticeContent.setBackground(ThemeColors.CARD_BG);
        noticeContent.setForeground(ThemeColors.TEXT);
        noticeContent.setLineWrap(true);
        noticeContent.setWrapStyleWord(true);
        noticeContent.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeColors.SECONDARY),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JScrollPane scrollPane = new JScrollPane(noticeContent);
        styleScrollPane(scrollPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        scrollPane.getViewport().setBackground(ThemeColors.BACKGROUND);

        panel.add(scrollPane, BorderLayout.CENTER);

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
        aboutContent.setText("ABOUT 케이팝 상점 (K-Pop Merch Store):\n\n" +
                "Launched in 2024, 케이팝 상점 is your premier online destination for official and high-quality K-Pop merchandise right here in the Philippines.\n\n" +
                "Our Mission:\n" +
                "To bridge the gap between K-Pop artists and their passionate fans across the Philippines and beyond, offering authentic albums, lightsticks, apparel, and exclusive collectibles, all delivered with exceptional service and fan-centric care.\n\n" +
                "Why Choose Us?\n" +
                "   \u2705 100% Authentic & Official Products\n" +
                "   \u2705 Curated Selection from Top & Rising Artists\n" +
                "   \u2705 Secure & Convenient Payment Options (GCash, Maya, Cards, PayPal)\n" +
                "   \u2705 Fast & Reliable Nationwide Shipping (International coming soon!)\n" +
                "   \u2705 Responsive & Friendly Customer Support\n\n" +
                "Contact Us:\n" +
                "   \uD83D\uDCE7 Email: support@kpopmerchstore.ph\n" +
                "   \uD83D\uDCDE Phone: +63 917 123 KPOP (5767)\n" +
                "   \uD83D\uDCAC Facebook: /KpopMerchStorePH\n" +
                "   \uD83D\uDCF8 Instagram: @kpopmerchstore_ph\n" +
                "   \uD83D\uDCAC Twitter: @KpopMerchPH_");
        aboutContent.setEditable(false);
        aboutContent.setFont(new Font(Font.DIALOG, Font.PLAIN, 16));
        aboutContent.setBackground(ThemeColors.CARD_BG);
        aboutContent.setForeground(ThemeColors.TEXT);
        aboutContent.setLineWrap(true);
        aboutContent.setWrapStyleWord(true);
        aboutContent.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeColors.SECONDARY),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JScrollPane scrollPane = new JScrollPane(aboutContent);
        styleScrollPane(scrollPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        scrollPane.getViewport().setBackground(ThemeColors.BACKGROUND);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

     // Styles a JTable with theme colors
     private void styleTable(JTable table) {
        table.setBackground(ThemeColors.CARD_BG);
        table.setForeground(ThemeColors.TEXT);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setGridColor(ThemeColors.BACKGROUND.brighter());
        table.setSelectionBackground(ThemeColors.PRIMARY);
        table.setSelectionForeground(Color.WHITE);
        table.setRowHeight(30);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.setFont(new Font("Arial", Font.BOLD, 14));
            header.setBackground(ThemeColors.PRIMARY);
            header.setForeground(Color.WHITE);
            header.setReorderingAllowed(false);
            header.setPreferredSize(new Dimension(header.getWidth(), 40));

            TableCellRenderer existingHeaderRenderer = header.getDefaultRenderer();
            header.setDefaultRenderer(new TableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = existingHeaderRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (c instanceof JLabel) {
                        JLabel label = (JLabel) c;
                        label.setHorizontalAlignment(SwingConstants.LEFT);
                        label.setBorder(BorderFactory.createCompoundBorder(
                                label.getBorder(),
                                BorderFactory.createEmptyBorder(0, 10, 0, 10))
                        );
                        label.setBackground(ThemeColors.PRIMARY);
                        label.setForeground(Color.WHITE);
                        label.setOpaque(true);
                    }
                    return c;
                }
            });
        }
    }

    // Creates a styled button with hover effect
    // *** MODIFIED: Made public static for reuse by ProfileDialog ***
    public static JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);

        // Determine text color based on background luminance for better contrast
        double luminance = (0.299 * bgColor.getRed() + 0.587 * bgColor.getGreen() + 0.114 * bgColor.getBlue()) / 255;
        button.setForeground(luminance > 0.5 ? Color.BLACK : Color.WHITE);

        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            Color originalBg = bgColor;
            Color originalFg = button.getForeground();

            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(ThemeColors.BUTTON_HOVER);
                // Recalculate text color for hover background
                double hoverLuminance = (0.299 * ThemeColors.BUTTON_HOVER.getRed() + 0.587 * ThemeColors.BUTTON_HOVER.getGreen() + 0.114 * ThemeColors.BUTTON_HOVER.getBlue()) / 255;
                button.setForeground(hoverLuminance > 0.5 ? Color.BLACK : Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalBg);
                button.setForeground(originalFg);
            }
        });
        return button;
    }


    // Adds item to cart (DB and UI), handles stock. Does not involve color/size.
    // This is used by the "Add to Cart" button in the details dialog.
    private void addToCart(int productId, String name, double price) {
        String checkStockSql = "SELECT stock FROM products WHERE id = ?";
        // Cart insert does not include color/size columns
        String insertSql = "INSERT INTO cart (product_id, product_name, price, quantity, customer_id) " +
                "VALUES (?, ?, ?, 1, ?) " +
                "ON DUPLICATE KEY UPDATE quantity = quantity + 1";
        // String updateStockSql = "UPDATE products SET stock = stock - 1 WHERE id = ? AND stock > 0"; // REMOVED - Stock handled in checkout

        Connection conn = null;
        PreparedStatement checkStmt = null;
        PreparedStatement insertStmt = null;
        // PreparedStatement updateStockStmt = null; // REMOVED

        try {
            conn = DBConnection.connect();
            conn.setAutoCommit(false); // Start transaction

            // 1. Check Stock
            checkStmt = conn.prepareStatement(checkStockSql);
            checkStmt.setInt(1, productId);
            ResultSet rs = checkStmt.executeQuery();
            int stock = 0;
            if (rs.next()) {
                stock = rs.getInt("stock");
            } else {
                 // Product doesn't exist
                 JOptionPane.showMessageDialog(this, "Product '" + name + "' not found.", "Error", JOptionPane.ERROR_MESSAGE);
                 conn.rollback(); // Rollback transaction
                 return;
            }
            rs.close();

            if (stock <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Sorry, '" + name + "' is currently out of stock.",
                        "Out of Stock",
                        JOptionPane.WARNING_MESSAGE);
                conn.rollback(); // Rollback transaction
                return;
            }

            // 2. Add/Update Cart (Stock deduction moved to checkout)
            insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setInt(1, productId);
            insertStmt.setString(2, name);
            insertStmt.setDouble(3, price);
            insertStmt.setInt(4, customerId);
            int rowsAffected = insertStmt.executeUpdate();

            conn.commit(); // Commit transaction

            // 3. Update UI
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this,
                        "'" + name + "' added to cart!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                updateCartBadge(); // Update the badge counter
                // If the cart panel is currently visible, refresh its content
                if (currentCard.equals("Cart")) {
                    loadCartItems();
                }
            } else {
                System.err.println("Cart update/insert query affected 0 rows for product ID: " + productId);
                JOptionPane.showMessageDialog(this,
                        "Could not add item to cart. Please try again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }


        } catch (SQLException ex) {
            System.err.println("Add to Cart SQL Error: " + ex.getMessage());
            if (conn != null) try { conn.rollback(); } catch (SQLException e) { e.printStackTrace(); } // Rollback on error
            JOptionPane.showMessageDialog(this,
                    "Error adding to cart: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            // Close resources in finally block
            try { if (checkStmt != null) checkStmt.close(); } catch (SQLException ignored) {}
            try { if (insertStmt != null) insertStmt.close(); } catch (SQLException ignored) {}
            // try { if (updateStockStmt != null) updateStockStmt.close(); } catch (SQLException ignored) {} // REMOVED
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {} // Reset auto-commit and close
        }
    }


    // Adds item to wishlist (DB and UI) - Does not involve color/size
    private void addToWishlist(int productId, String name, double price) {
        String sql = "INSERT INTO wishlist (customer_id, product_id) VALUES (?, ?)";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            stmt.setInt(2, productId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "'" + name + "' added to wishlist!");
                updateWishlistBadge();
                if (currentCard.equals("Wishlist")) {
                    loadWishlist();
                }
            }
        } catch (SQLException ex) {
            // Handle potential duplicate entry (MySQL error code 1062)
            if (ex.getErrorCode() == 1062) { // Duplicate entry error code for MySQL
                JOptionPane.showMessageDialog(this,
                        "'" + name + "' is already in your wishlist!",
                        "Notice", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Handle other SQL errors
                System.err.println("Wishlist SQL Error: " + ex.getMessage());
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error adding to wishlist: " + ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    // Removes item from wishlist (DB and UI) - Used internally
    private void removeFromWishlist(int productId) {
        String sql = "DELETE FROM wishlist WHERE customer_id = ? AND product_id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            stmt.setInt(2, productId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Product ID " + productId + " removed from wishlist for customer " + customerId);
                updateWishlistBadge();
                // If the wishlist panel is visible, refresh it
                if (currentCard.equals("Wishlist")) {
                    loadWishlist();
                }
            } else {
                System.err.println("Warning: removeFromWishlist affected 0 rows for product ID " + productId);
            }

        } catch (SQLException ex) {
            System.err.println("Error removing from wishlist: " + ex.getMessage());
            ex.printStackTrace();
            // Optionally show an error message to the user
            // JOptionPane.showMessageDialog(this, "Error removing item from wishlist.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    // Loads items currently in the customer's cart from DB - No color/size needed
    private void loadCartItems() {
         if (cartItemsPanel == null) {
             System.err.println("Error: cartItemsPanel is null in loadCartItems.");
             return;
         }

        cartItemsPanel.removeAll();
        cartItemsPanel.setLayout(new BoxLayout(cartItemsPanel, BoxLayout.Y_AXIS));

        // SQL does not need color/size fields from cart or product
        String sql = "SELECT c.id, p.name, p.price, c.quantity, p.image_path, p.stock " +
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
                String imagePath = rs.getString("image_path");
                int stock = rs.getInt("stock"); // Still need stock check

                // Check if cart quantity exceeds current stock
                if (quantity > stock) {
                    System.err.println("Warning: Cart quantity (" + quantity + ") for item '" + name + "' (cartId: " + cartId + ") exceeds stock (" + stock + "). Adjusting cart.");
                    int newQuantity = Math.max(0, stock); // Adjust to available stock (or 0 if none)

                    if (newQuantity == 0) {
                        // If no stock, remove item from cart in a separate UI thread action
                        final int finalCartId = cartId;
                        SwingUtilities.invokeLater(() -> removeCartItem(finalCartId));
                        continue; // Skip adding this item panel for now
                    } else {
                        // Update the cart quantity in DB and UI
                        updateCartItemQuantity(cartId, newQuantity, null); // Pass null for panel as it's not created yet
                        quantity = newQuantity; // Use the adjusted quantity for panel creation
                    }
                }

                // Create panel using the (potentially adjusted) quantity, without color/size
                JPanel itemPanel = createCartItemPanel(
                        cartId, name, price, quantity, imagePath
                );
                itemPanel.putClientProperty("cartId", cartId); // Store ID for later use

                cartItemsPanel.add(itemPanel);
                cartItemsPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Spacer
            }

            if (!hasItems) {
                // Display empty cart message
                cartItemsPanel.setLayout(new BorderLayout());
                JLabel emptyLabel = new JLabel("Your cart is empty.", SwingConstants.CENTER);
                emptyLabel.setFont(new Font("Arial", Font.ITALIC, 16));
                emptyLabel.setForeground(Color.GRAY);
                cartItemsPanel.add(emptyLabel, BorderLayout.CENTER);
            }

            calculateSelectedTotal(); // Update total based on selected items
            updateCartBadge(); // Update badge count

        } catch (SQLException ex) {
            System.err.println("Error loading cart: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading cart items: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            // Display error message in the panel
            cartItemsPanel.setLayout(new BorderLayout());
            cartItemsPanel.add(new JLabel("Error loading cart.", SwingConstants.CENTER), BorderLayout.CENTER);

        }

        // Refresh the panel display
        cartItemsPanel.revalidate();
        cartItemsPanel.repaint();
    }


    // Loads items currently in the customer's wishlist from DB - No color/size needed
    private void loadWishlist() {
         Object panelObj = wishlistPanel.getClientProperty("wishlistItemsPanel");
         if (!(panelObj instanceof JPanel)) {
             System.err.println("Error: Could not find wishlist items panel using client property.");
             return;
         }
         JPanel wishlistItemsPanel = (JPanel) panelObj;

        wishlistItemsPanel.removeAll();
        wishlistItemsPanel.setLayout(new BoxLayout(wishlistItemsPanel, BoxLayout.Y_AXIS));

        // SQL does not need color/size
        String sql = "SELECT p.id, p.name, p.price, p.description, p.image_path " +
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
                String imagePath = rs.getString("image_path");

                // Create panel without color/size
                JPanel itemPanel = createWishlistItemPanel(id, name, price, description, imagePath);
                wishlistItemsPanel.add(itemPanel);
                wishlistItemsPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Spacer
            }

            if (!hasItems) {
                // Display empty wishlist message
                wishlistItemsPanel.setLayout(new BorderLayout());
                JLabel emptyLabel = new JLabel("Your wishlist is empty.", SwingConstants.CENTER);
                emptyLabel.setFont(new Font("Arial", Font.ITALIC, 16));
                emptyLabel.setForeground(Color.GRAY);
                wishlistItemsPanel.add(emptyLabel, BorderLayout.CENTER);
            }

            updateWishlistBadge(); // Update badge count

        } catch (SQLException ex) {
            System.err.println("Error loading wishlist: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading wishlist items: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            // Display error message in panel
            wishlistItemsPanel.setLayout(new BorderLayout());
            wishlistItemsPanel.add(new JLabel("Error loading wishlist.", SwingConstants.CENTER), BorderLayout.CENTER);
        }

        wishlistItemsPanel.revalidate();
        wishlistItemsPanel.repaint();
    }


    // Loads customer's order history from DB - No color/size needed
    public void loadOrders() { // Made public for CheckoutFrame to call
         Object panelObj = orderTrackingPanel.getClientProperty("ordersPanel");
         if (!(panelObj instanceof JPanel)) {
             System.err.println("Error: Could not find orders panel using client property.");
             return;
         }
         JPanel ordersPanel = (JPanel) panelObj;

        ordersPanel.removeAll();
        ordersPanel.setLayout(new BoxLayout(ordersPanel, BoxLayout.Y_AXIS));

        // SQL does not fetch color/size
        String sql = "SELECT o.id, p.name AS product_name, oi.quantity, o.status, o.order_date, " +
                     "oi.price AS item_price, p.image_path, o.cancellation_requested, o.notification_read_status " +
                     "FROM orders o " +
                     "JOIN order_items oi ON o.id = oi.order_id " +
                     "JOIN products p ON oi.product_id = p.id " +
                     "WHERE o.customer_id = ? " +
                     "ORDER BY o.order_date DESC, o.id DESC, p.name ASC"; // Order by date, then order ID, then product name

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            boolean hasOrders = false;
            while (rs.next()) {
                hasOrders = true;
                int orderId = rs.getInt("id");
                String productName = rs.getString("product_name");
                int quantity = rs.getInt("quantity");
                String status = rs.getString("status");
                String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(rs.getTimestamp("order_date"));
                double itemPrice = rs.getDouble("item_price");
                String imagePath = rs.getString("image_path");
                boolean cancellationRequested = rs.getBoolean("cancellation_requested");

                // Create panel without color/size
                JPanel itemPanel = createOrderItemPanel(orderId, productName, quantity, status, dateStr, itemPrice, imagePath, cancellationRequested);
                ordersPanel.add(itemPanel);
                ordersPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Smaller spacer for orders
            }

            if (!hasOrders) {
                ordersPanel.setLayout(new BorderLayout());
                JLabel emptyLabel = new JLabel("You have no orders yet.", SwingConstants.CENTER);
                emptyLabel.setFont(new Font("Arial", Font.ITALIC, 16));
                emptyLabel.setForeground(Color.GRAY);
                ordersPanel.add(emptyLabel, BorderLayout.CENTER);
            }
            updateOrderBadge(); // Update badge count

        } catch (SQLException ex) {
            System.err.println("Error loading orders: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading orders: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            ordersPanel.setLayout(new BorderLayout());
            ordersPanel.add(new JLabel("Error loading orders.", SwingConstants.CENTER), BorderLayout.CENTER);
        }

        ordersPanel.revalidate();
        ordersPanel.repaint();
    }


    private void checkout() {
        List<Integer> selectedCartIds = new ArrayList<>();

        // --- Step 1: Get selected cart IDs ---
        if (cartItemsPanel == null) {
            System.err.println("ERROR: checkout() called but cartItemsPanel is null.");
            JOptionPane.showMessageDialog(this,
                    "Cannot proceed to checkout. Cart panel is unavailable.",
                    "Checkout Error", JOptionPane.ERROR_MESSAGE);
            return; // Stop if the panel doesn't exist
        }

        // Iterate through components in the cartItemsPanel to find selected items
        for (Component comp : cartItemsPanel.getComponents()) {
            // Check if the component is the JPanel representing a cart item
            if (comp instanceof JPanel) {
                JPanel itemPanel = (JPanel) comp;
                // Ensure it's actually a cart item panel by checking for the cartId property
                if (itemPanel.getClientProperty("cartId") instanceof Integer) {
                    // Find the checkbox within this item panel
                    JCheckBox checkbox = findCheckboxInPanel(itemPanel); // Use the helper method

                    if (checkbox != null && checkbox.isSelected()) {
                        // If the checkbox is found and selected, get the cart ID
                        Integer cartId = (Integer) itemPanel.getClientProperty("cartId");
                        if (cartId != null) {
                            selectedCartIds.add(cartId);
                            System.out.println("DEBUG: Added cartId to checkout: " + cartId); // Debug log
                        } else {
                             System.err.println("WARN: Found selected checkbox but cartId property was null in item panel.");
                        }
                    } else if (checkbox == null) {
                         System.err.println("WARN: Could not find checkbox in cart item panel with cartId=" + itemPanel.getClientProperty("cartId"));
                    }
                }
            }
        }

        // --- Step 2: Validate Selection ---
        if (selectedCartIds.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select at least one item from your cart to checkout.",
                    "Selection Error", JOptionPane.WARNING_MESSAGE);
            return; // Stop if nothing is selected
        }

        // --- Step 3: Launch CheckoutFrame ---
        System.out.println("Proceeding to checkout for customer " + customerId + " with cart IDs: " + selectedCartIds);

        // Make final for use in lambda expression
        final List<Integer> finalSelectedCartIds = selectedCartIds;

        // Use invokeLater to ensure GUI operations are on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            // Create and show the new combined CheckoutFrame
            // Pass 'this' (the CustomerFrame instance) so CheckoutFrame can interact back if needed
            // (e.g., to refresh badges or re-show CustomerFrame after order)
            new CheckoutFrame(customerId, finalSelectedCartIds, this).setVisible(true);

            // Hide the current CustomerFrame while the checkout process is active
            // CheckoutFrame will handle showing it again or closing it based on outcome.
            setVisible(false);
        });
    }

    // Helper method to find a JCheckBox within a container (ensure this exists in CustomerFrame)
    private JCheckBox findCheckboxInPanel(Container container) {
       if (container == null) return null;
       for (Component comp : container.getComponents()) {
           if (comp instanceof JCheckBox) {
               return (JCheckBox) comp;
           } else if (comp instanceof Container) {
               // Recursively search in sub-containers (important for nested layouts)
               JCheckBox found = findCheckboxInPanel((Container) comp);
               if (found != null) {
                   return found;
               }
           }
       }
       return null; // Checkbox not found in this container or its children
    }


    // Logs out the user and shows the login frame
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out?",
                "Logout",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            SwingUtilities.invokeLater(() -> {
                // Re-apply Look and Feel for the login frame (optional, but good practice)
                try {
                    UIManager.setLookAndFeel(new FlatDarkLaf());
                    // Apply UIManager defaults again if needed for LoginFrame specifically
                     UIManager.put("OptionPane.background", ThemeColors.DIALOG_BG);
                     UIManager.put("Panel.background", ThemeColors.DIALOG_BG); // General panel background
                     UIManager.put("OptionPane.messageForeground", ThemeColors.DIALOG_FG);
                     UIManager.put("Button.background", ThemeColors.SECONDARY); // Default button background
                     UIManager.put("Button.foreground", Color.WHITE); // Default button text color
                     UIManager.put("Button.focus", new Color(ThemeColors.SECONDARY.getRed(), ThemeColors.SECONDARY.getGreen(), ThemeColors.SECONDARY.getBlue(), 180)); // Focus color
                     UIManager.put("Button.hoverBackground", ThemeColors.BUTTON_HOVER);
                     UIManager.put("Button.pressedBackground", ThemeColors.SECONDARY.darker());
                } catch (Exception ex) {
                    System.err.println("Failed to re-initialize FlatDarkLaf for Login");
                }

                 new LoginFrame().setVisible(true);

                System.out.println("Logout successful. Closing Customer Frame."); // Can keep or remove this log
            });
            dispose(); // Close this CustomerFrame
        }
    }

    // Switches the visible panel in the main CardLayout
    public void showCard(String cardName) { // Made public for CheckoutFrame
        cardLayout.show(mainPanel, cardName);
        currentCard = cardName; // Update the tracking variable
        System.out.println("Showing card: " + cardName);

         // Attempt to scroll to top for relevant scrollable panels
         JPanel panelToScroll = null;
         switch(cardName) {
            case "Home": panelToScroll = homeProductGridPanel; break;
            case "Products": panelToScroll = productsProductGridPanel; break;
            case "Cart": panelToScroll = cartItemsPanel; break; // Scroll the items panel inside the cart
            case "Wishlist":
                Object wPanelObj = wishlistPanel.getClientProperty("wishlistItemsPanel");
                if (wPanelObj instanceof JPanel) panelToScroll = (JPanel)wPanelObj;
                break;
             case "Orders":
                 Object oPanelObj = orderTrackingPanel.getClientProperty("ordersPanel");
                 if (oPanelObj instanceof JPanel) panelToScroll = (JPanel) oPanelObj;
                 break;
             // Add other cases if needed (e.g., FAQ, Events if they are inside a ScrollPane)
         }
         // If a relevant panel was identified, attempt to scroll its viewport to the top
         if (panelToScroll != null) {
             scrollToTop(panelToScroll);
         }
    }


    // --- Badge Update Methods ---

    public void updateCartBadge() { // Made public for CheckoutFrame
        int count = getCartItemCount();
        ActionListener cartAction = e -> { loadCartItems(); showCard("Cart"); };
        JComponent newCartButtonContainer = createBadgeButtonWithCounter("CART", count, cartAction);
        replaceButtonContainer(cartButtonContainer, newCartButtonContainer);
        cartButtonContainer = newCartButtonContainer;
    }

    public void updateWishlistBadge() { // Made public for CheckoutFrame
        int count = getWishlistItemCount();
        ActionListener wishlistAction = e -> { loadWishlist(); showCard("Wishlist"); };
        JComponent newWishlistButtonContainer = createBadgeButtonWithCounter("WISHLIST", count, wishlistAction);
        replaceButtonContainer(wishlistButtonContainer, newWishlistButtonContainer);
        wishlistButtonContainer = newWishlistButtonContainer;
    }

    public void updateOrderBadge() { // Made public for CheckoutFrame
        int count = getOrderCount();
        ActionListener ordersAction = e -> { loadOrders(); showCard("Orders"); };
        JComponent newOrdersButtonContainer = createBadgeButtonWithCounter("ORDERS", count, ordersAction);
        replaceButtonContainer(ordersButtonContainer, newOrdersButtonContainer);
        ordersButtonContainer = newOrdersButtonContainer;
    }

    // Updated to handle replacing JComponent or JButton with new Component
    private void replaceButtonContainer(Component oldContainer, Component newContainer) { // Use Component
        if (oldContainer == null || newContainer == null) {
            System.err.println("Warning: Cannot replace button container. Old or new container is null.");
            return;
        }
        Container parent = oldContainer.getParent();
        if (parent == null) {
             System.err.println("Warning: Cannot replace button container. Parent is null.");
            return;
        }

        int oldIndex = getComponentIndex(parent, oldContainer);

        if (oldIndex != -1) {
            // Let the new container determine its own preferred size
            // No need to explicitly set size here as FlowLayout will use preferred size

            parent.remove(oldContainer);
            parent.add(newContainer, oldIndex); // Add new container at the original index

            // Revalidate and repaint the parent container
            parent.revalidate();
            parent.repaint();
        } else {
            System.err.println("Warning: Old container not found in parent during replacement.");
        }
    }


    // Helper to find the index of a component within its container
    private int getComponentIndex(Container container, Component component) {
        for (int i = 0; i < container.getComponentCount(); i++) {
            if (container.getComponent(i) == component) {
                return i;
            }
        }
        return -1; // Not found
    }

     // Styles a JScrollPane's vertical scrollbar
     // *** MODIFIED: Made public static for reuse by ProfileDialog ***
     public static void styleScrollPane(JScrollPane scrollPane) {
         JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
         verticalScrollBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
             @Override
             protected void configureScrollBarColors() {
                 thumbColor = ThemeColors.PRIMARY;
                 trackColor = ThemeColors.CARD_BG;
             }

             @Override
             protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
             @Override
             protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }

             private JButton createZeroButton() {
                 JButton button = new JButton();
                 Dimension zeroDim = new Dimension(0, 0);
                 button.setPreferredSize(zeroDim);
                 button.setMinimumSize(zeroDim);
                 button.setMaximumSize(zeroDim);
                 return button;
             }

             @Override
             protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                 if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
                 Graphics2D g2 = (Graphics2D) g.create();
                 g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                 g2.setColor(thumbColor);
                 g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4, 5, 5);
                 g2.dispose();
             }

             @Override
             protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                 g.setColor(trackColor);
                 g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
             }
         });
         verticalScrollBar.setBackground(ThemeColors.BACKGROUND);
         verticalScrollBar.setBorder(null);

         // Style horizontal scrollbar similarly if needed (usually not needed for vertical layouts)
         JScrollBar horizontalScrollBar = scrollPane.getHorizontalScrollBar();
         horizontalScrollBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
              @Override protected void configureScrollBarColors() { thumbColor = ThemeColors.PRIMARY; trackColor = ThemeColors.CARD_BG; }
              @Override protected JButton createDecreaseButton(int o) { return createZeroButton(); }
              @Override protected JButton createIncreaseButton(int o) { return createZeroButton(); }
              private JButton createZeroButton() { /* Same as above */ JButton b=new JButton(); Dimension d=new Dimension(0,0); b.setPreferredSize(d); b.setMinimumSize(d); b.setMaximumSize(d); return b; }
              @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) { /* Same as above */ if(r.isEmpty()||!scrollbar.isEnabled()) return; Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(thumbColor); g2.fillRoundRect(r.x+2,r.y+2,r.width-4,r.height-4,5,5); g2.dispose(); }
              @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) { /* Same as above */ g.setColor(trackColor); g.fillRect(r.x,r.y,r.width,r.height); }
         });
         horizontalScrollBar.setBackground(ThemeColors.BACKGROUND);
         horizontalScrollBar.setBorder(null);
     }

    // --- Action to show the profile dialog ---
    private void showProfileDialog() {
        ProfileDialog profileDialog = new ProfileDialog(this, customerId);
        profileDialog.setVisible(true);
    }

    // --- Profile Dialog Inner Class (Includes Change Picture Fix and Functionality) ---
    private class ProfileDialog extends JDialog {
        private int dialogCustomerId;
        private JLabel profilePicLabel;
        private JTextField nameField, emailField, phoneField;
        private JTextArea addressArea; // Keep for simple address display
        private JButton changePicButton, editButton, saveButton, cancelButton, closeButton;
        private JButton manageAddressesButton; // New button for address management
        private File selectedImageFile = null;
        private String currentImagePath = null;

        public ProfileDialog(JFrame parent, int customerId) {
            super(parent, "Your Profile", true);
            this.dialogCustomerId = customerId;
            setSize(500, 650); // Adjusted size for new button
            setLayout(new BorderLayout(10, 10));
            getContentPane().setBackground(ThemeColors.BACKGROUND);
            setLocationRelativeTo(parent);

            // --- Top Panel: Profile Picture ---
            JPanel picturePanel = new JPanel(new BorderLayout(10, 5));
            picturePanel.setOpaque(false);
            picturePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

            profilePicLabel = new JLabel("Loading...", SwingConstants.CENTER);
            profilePicLabel.setPreferredSize(new Dimension(150, 150));
            profilePicLabel.setBorder(BorderFactory.createLineBorder(ThemeColors.SECONDARY, 2));
            profilePicLabel.setOpaque(true);
            profilePicLabel.setBackground(ThemeColors.CARD_BG);
            profilePicLabel.setForeground(ThemeColors.TEXT);
            JPanel pictureCenterPanel = new JPanel(new GridBagLayout());
            pictureCenterPanel.setOpaque(false);
            pictureCenterPanel.add(profilePicLabel);
            picturePanel.add(pictureCenterPanel, BorderLayout.CENTER);

            changePicButton = createStyledButton("Change Picture", ThemeColors.SECONDARY);
            changePicButton.addActionListener(e -> chooseProfilePicture());
            JPanel changeButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            changeButtonPanel.setOpaque(false);
            changeButtonPanel.add(changePicButton);
            picturePanel.add(changeButtonPanel, BorderLayout.SOUTH);

            add(picturePanel, BorderLayout.NORTH);

            // --- Center Panel: Details Form ---
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(ThemeColors.BACKGROUND);
            formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 8, 8, 8);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            nameField = createProfileTextField();
            emailField = createProfileTextField();
            phoneField = createProfileTextField();
            addressArea = createProfileTextArea();
            addressArea.setText("Manage addresses using the button below."); // Placeholder text
            addressArea.setEditable(false); // Address area is display-only

            int y = 0;
            gbc.gridx = 0; gbc.gridy = y++; formPanel.add(createFormLabel("Name:"), gbc);
            gbc.gridx = 1; gbc.weightx = 1.0; formPanel.add(nameField, gbc); gbc.weightx = 0.0;

            gbc.gridx = 0; gbc.gridy = y++; formPanel.add(createFormLabel("Email:"), gbc);
            gbc.gridx = 1; gbc.weightx = 1.0; formPanel.add(emailField, gbc); gbc.weightx = 0.0;

            gbc.gridx = 0; gbc.gridy = y++; formPanel.add(createFormLabel("Phone:"), gbc);
            gbc.gridx = 1; gbc.weightx = 1.0; formPanel.add(phoneField, gbc); gbc.weightx = 0.0;

            // --- Address Display (Read-only) ---
            gbc.gridx = 0; gbc.gridy = y++; gbc.anchor = GridBagConstraints.NORTHWEST;
            formPanel.add(createFormLabel("Address:"), gbc);
            gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
            JScrollPane addressScrollPane = new JScrollPane(addressArea);
            CustomerFrame.styleScrollPane(addressScrollPane); // Use CustomerFrame's static method
            addressScrollPane.setPreferredSize(new Dimension(200, 60)); // Smaller height
            // Use non-editable theme colors for address area
            addressArea.setBackground(ThemeColors.CARD_BG); // Same as non-edit fields
            addressArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeColors.SECONDARY, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
            ));
            formPanel.add(addressScrollPane, gbc); gbc.weighty = 0.0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.WEST;


            // --- Manage Addresses Button ---
             y++; // Increment row
             gbc.gridx = 1; // Place button under the address area, aligned right
             gbc.gridy = y;
             gbc.anchor = GridBagConstraints.EAST; // Align button right
             gbc.fill = GridBagConstraints.NONE; // Don't stretch button
             gbc.weightx = 0.0; // Don't take extra horizontal space
             manageAddressesButton = createStyledButton("Manage Addresses", ThemeColors.ACCENT);
             manageAddressesButton.addActionListener(e -> openAddressManagementDialog());
             formPanel.add(manageAddressesButton, gbc);

            add(formPanel, BorderLayout.CENTER);


            // --- Bottom Panel: Buttons ---
            JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            bottomButtonPanel.setOpaque(false);
            bottomButtonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));

            editButton = createStyledButton("Edit Profile", ThemeColors.PRIMARY);
            saveButton = createStyledButton("Save Changes", ThemeColors.PRIMARY);
            cancelButton = createStyledButton("Cancel Edit", ThemeColors.SECONDARY);
            // FIX: Use ThemeColors.SECONDARY or another defined color for Close button BG
            closeButton = createStyledButton("Close", ThemeColors.SECONDARY);
            // closeButton.setForeground(ThemeColors.TEXT); // Keep if SECONDARY is dark, remove/adjust if light

            saveButton.setVisible(false); // Initially hidden
            cancelButton.setVisible(false); // Initially hidden

            editButton.addActionListener(e -> toggleEditMode(true));
            saveButton.addActionListener(e -> saveProfileChanges());
            cancelButton.addActionListener(e -> toggleEditMode(false));
            closeButton.addActionListener(e -> dispose());

            bottomButtonPanel.add(editButton);
            bottomButtonPanel.add(saveButton);
            bottomButtonPanel.add(cancelButton);
            bottomButtonPanel.add(closeButton);

            add(bottomButtonPanel, BorderLayout.SOUTH);

            loadProfileData();
            toggleEditMode(false); // Start in non-edit mode
        }

        private void openAddressManagementDialog() {
             // Use 'this' ProfileDialog as the parent Window for the AddressManagementDialog
             AddressManager profileAddrManager = new AddressManager(this, dialogCustomerId);
             Address currentDefaultAddr = profileAddrManager.getDefaultAddress();

             CheckoutFrame.AddressManagementDialog dialog = new CheckoutFrame.AddressManagementDialog(this, dialogCustomerId, profileAddrManager, currentDefaultAddr);
             dialog.setVisible(true);

             // After the dialog closes, reload the profile data to reflect potential address changes
             loadProfileData();
        }

        private JTextField createProfileTextField() {
            JTextField field = new JTextField(20);
            field.setFont(new Font("Arial", Font.PLAIN, 14));
            field.setBackground(ThemeColors.CARD_BG);
            field.setForeground(ThemeColors.TEXT);
            field.setCaretColor(ThemeColors.TEXT);
            field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeColors.SECONDARY, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
            ));
            field.setEditable(false); // Initially not editable
            return field;
        }

        private JTextArea createProfileTextArea() {
            JTextArea area = new JTextArea(3, 20);
             area.setFont(new Font("Arial", Font.PLAIN, 14));
             area.setBackground(ThemeColors.CARD_BG);
             area.setForeground(ThemeColors.TEXT);
             area.setCaretColor(ThemeColors.TEXT);
             area.setLineWrap(true);
             area.setWrapStyleWord(true);
             area.setBorder(BorderFactory.createCompoundBorder(
                 BorderFactory.createLineBorder(ThemeColors.SECONDARY, 1),
                 BorderFactory.createEmptyBorder(8, 8, 8, 8)
             ));
             area.setEditable(false); // Initially not editable
            return area;
        }

        private JLabel createFormLabel(String text) {
            JLabel label = new JLabel(text);
            label.setFont(new Font("Arial", Font.BOLD, 14));
            label.setForeground(ThemeColors.TEXT);
            return label;
        }

        private void loadProfileData() {
            // Load basic info
            String sql = "SELECT name, email, phone, profile_picture_path FROM customers WHERE id = ?";
            try (Connection conn = DBConnection.connect();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, dialogCustomerId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    nameField.setText(rs.getString("name"));
                    emailField.setText(rs.getString("email"));
                    phoneField.setText(rs.getString("phone"));
                    currentImagePath = rs.getString("profile_picture_path");
                    loadProfilePicture(currentImagePath);

                     // Load and display default address (read-only)
                     // Create a new AddressManager instance specifically for this dialog's use
                     AddressManager profileAddrManager = new AddressManager(this, dialogCustomerId);
                     Address defaultAddr = profileAddrManager.getDefaultAddress();
                     if (defaultAddr != null) {
                         // Display simplified address string
                         addressArea.setText(defaultAddr.getFormattedAddress());
                     } else {
                         addressArea.setText("No default address set. Use 'Manage Addresses'.");
                     }

                } else {
                    JOptionPane.showMessageDialog(this, "Customer data not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    dispose();
                }
            } catch (SQLException ex) {
                System.err.println("Error loading profile data: " + ex.getMessage());
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading profile data.", "Database Error", JOptionPane.ERROR_MESSAGE);
                dispose();
            }
        }

        private void loadProfilePicture(String imagePath) {
             ImageIcon profileIcon = null;
             int size = 150;

             if (imagePath != null && !imagePath.isEmpty()) {
                 // Use the same robust loader as for product images
                 profileIcon = loadImageIcon(imagePath, "ProfilePicture_" + dialogCustomerId);
             }

             // If loading failed or image is invalid, use placeholder
             if (profileIcon == null || !isIconValid(profileIcon)) { // Use CustomerFrame's static method
                  profileIcon = createPlaceholderIcon("\uD83D\uDC64", size); // User emoji placeholder
                  profilePicLabel.setText(null); // Remove any text
                  profilePicLabel.setIcon(profileIcon);
             } else {
                  // Scale valid image and display
                  Image scaledImage = profileIcon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                  profilePicLabel.setText(null); // Remove any text
                  profilePicLabel.setIcon(new ImageIcon(scaledImage));
             }
         }

        // Placeholder icon creation (same as the main class one, could be refactored)
        private ImageIcon createPlaceholderIcon(String symbol, int size) {
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Background
            g.setColor(ThemeColors.CARD_BG);
            g.fillRect(0, 0, size, size);

            // Symbol
            g.setColor(ThemeColors.TEXT.brighter());
            g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, size * 2 / 3)); // Adjust font size based on icon size
            FontMetrics fm = g.getFontMetrics();
            int x = (size - fm.stringWidth(symbol)) / 2;
            int y = (size - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(symbol, x, y);

            g.dispose();
            return new ImageIcon(image);
        }


        private void chooseProfilePicture() {
            // This method is only called when changePicButton is clicked,
            // and changePicButton is only enabled when edit mode is active.
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Profile Picture");
            fileChooser.setAcceptAllFileFilterUsed(false);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Image files (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif");
            fileChooser.addChoosableFileFilter(filter);

            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedImageFile = fileChooser.getSelectedFile();
                try {
                    // Preview the selected image
                    ImageIcon previewIcon = new ImageIcon(selectedImageFile.toURI().toURL());
                     if (isIconValid(previewIcon)) { // Use public static method
                         // Scale and display preview
                         Image scaledImage = previewIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                         profilePicLabel.setIcon(new ImageIcon(scaledImage));
                         profilePicLabel.setText(null); // Remove placeholder text if any
                         // Ensure save button is enabled, as changes can now be saved
                         saveButton.setEnabled(true);
                     } else {
                         JOptionPane.showMessageDialog(this, "Invalid image file selected.", "Image Error", JOptionPane.ERROR_MESSAGE);
                         selectedImageFile = null; // Reset selection if invalid
                     }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error loading image preview: " + ex.getMessage(), "Preview Error", JOptionPane.ERROR_MESSAGE);
                    selectedImageFile = null; // Reset selection on error
                }
            }
        }

        // Correctly enables/disables fields and buttons for edit mode
        private void toggleEditMode(boolean enable) {
            // Update field editability
            nameField.setEditable(enable);
            emailField.setEditable(enable);
            phoneField.setEditable(enable);
            // addressArea.setEditable(enable); // Address is managed separately

            // --- FIX: Enable/Disable 'Change Picture' button based on edit mode ---
            changePicButton.setEnabled(enable); // THIS IS THE CRUCIAL LINE

            // Change appearance based on edit mode
            Color bgColor = enable ? ThemeColors.CARD_BG.brighter() : ThemeColors.CARD_BG;
            Color borderColor = enable ? ThemeColors.PRIMARY : ThemeColors.SECONDARY;
            Border border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
            );

            nameField.setBackground(bgColor); nameField.setBorder(border);
            emailField.setBackground(bgColor); emailField.setBorder(border);
            phoneField.setBackground(bgColor); phoneField.setBorder(border);
            // addressArea.setBackground(bgColor); // Address area keeps non-edit style
            // addressArea.setBorder(border); // Address area keeps non-edit style


             // Apply border to the scroll pane containing the text area for consistency (but keep non-edit border color)
            /*
             Component scrollPaneComp = addressArea.getParent(); // Get the viewport
             if (scrollPaneComp instanceof JViewport) {
                 scrollPaneComp = scrollPaneComp.getParent(); // Get the scroll pane
                 if (scrollPaneComp instanceof JScrollPane) {
                    ((JScrollPane) scrollPaneComp).setBorder(BorderFactory.createLineBorder(ThemeColors.SECONDARY, 1)); // Always use non-edit border
                 } else { addressArea.setBorder(border); } // Fallback if structure is different
             } else { addressArea.setBorder(border); } // Fallback
            */


            // Toggle button visibility
            editButton.setVisible(!enable);
            saveButton.setVisible(enable);
            saveButton.setEnabled(enable); // Enable Save button when entering edit mode
            cancelButton.setVisible(enable);

            // Disable address management button during edit mode for basic info
            manageAddressesButton.setEnabled(!enable);


            // If exiting edit mode (cancelling or saving successfully)
            if (!enable) {
                 // If cancelling, revert any unsaved changes including picture preview
                 // Checking if save button is visible helps differentiate cancel from save success
                 if (saveButton.isVisible() && !saveButton.isEnabled()) {
                     // This means save just happened, don't reload (data is already current)
                 } else {
                     // This means Cancel was clicked or initial load
                     loadProfileData(); // Reloads all data from DB, including original picture and default address
                     selectedImageFile = null; // Discard selected file if cancelled
                 }
            } else {
                // Entering edit mode, request focus on the first editable field
                nameField.requestFocusInWindow();
            }
        }

        private void saveProfileChanges() {
            String newName = nameField.getText().trim();
            String newEmail = emailField.getText().trim();
            String newPhone = phoneField.getText().trim();
            // String newAddress = addressArea.getText().trim(); // Address not edited here

            // Basic Validation
            if (newName.isEmpty() || newEmail.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name and Email cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Simple email format check
            if (!newEmail.contains("@") || !newEmail.contains(".")) {
                 JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                 return;
            }

            String newImagePath = currentImagePath; // Start with current path
            String oldImagePathToDelete = null; // Track old path if new one is saved

            // Handle new profile picture selection
            if (selectedImageFile != null) {
                try {
                    Path sourcePath = selectedImageFile.toPath();
                    String originalFilename = selectedImageFile.getName();
                    String extension = "";
                    int dotIndex = originalFilename.lastIndexOf('.');
                    if (dotIndex > 0 && dotIndex < originalFilename.length() - 1) {
                        extension = originalFilename.substring(dotIndex); // Get file extension
                    }

                    // Define target directory (e.g., project_root/images/profiles/)
                    Path targetDir = Paths.get("images", "profiles");
                    Files.createDirectories(targetDir); // Create directory if it doesn't exist

                    // Create a unique filename (e.g., profile_customerId_timestamp.ext)
                    String uniqueFilename = "profile_" + dialogCustomerId + "_" + System.currentTimeMillis() + extension;
                    Path targetPath = targetDir.resolve(uniqueFilename);

                    // Copy the selected file to the target directory
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Profile picture copied to: " + targetPath.toString());

                    // Store the relative path for the database
                    // Use forward slashes for cross-platform compatibility in DB
                    newImagePath = targetDir.toString().replace("\\", "/") + "/" + uniqueFilename;

                    // Mark the old image path for deletion if it exists and is different
                    if (currentImagePath != null && !currentImagePath.isEmpty() && !currentImagePath.equals(newImagePath)) {
                         oldImagePathToDelete = currentImagePath;
                    }

                } catch (IOException ioEx) {
                    System.err.println("Error saving profile picture: " + ioEx.getMessage());
                    ioEx.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Could not save profile picture: " + ioEx.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
                    return; // Stop saving process if image handling fails
                } catch (Exception e) {
                     System.err.println("Unexpected error processing image: " + e.getMessage());
                     e.printStackTrace();
                     JOptionPane.showMessageDialog(this, "An error occurred while processing the image.", "Error", JOptionPane.ERROR_MESSAGE);
                     return; // Stop saving process
                }
            }

            // --- Database Update ---
            // Update only basic info, address is managed separately
            String sql = "UPDATE customers SET name = ?, email = ?, phone = ?, profile_picture_path = ? WHERE id = ?";
            Connection conn = null;
            PreparedStatement stmt = null;
            boolean updateSuccess = false;
            try {
                conn = DBConnection.connect();
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, newName);
                stmt.setString(2, newEmail);
                stmt.setString(3, newPhone);
                // Set the new image path (or null if none was selected/error occurred)
                if (newImagePath != null && !newImagePath.isEmpty()) {
                    stmt.setString(4, newImagePath);
                } else {
                    stmt.setNull(4, Types.VARCHAR); // Store NULL if no image path
                }
                stmt.setInt(5, dialogCustomerId); // ID is the 5th parameter now

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    updateSuccess = true;
                    System.out.println("Profile updated successfully for customer ID: " + dialogCustomerId);

                     // If update was successful and there's an old image to delete
                     if (oldImagePathToDelete != null) {
                         try {
                             Path oldPath = Paths.get(oldImagePathToDelete);
                             // Check if the old path exists and is a file before deleting
                             if (Files.exists(oldPath) && !Files.isDirectory(oldPath)) {
                                 if (Files.deleteIfExists(oldPath)) {
                                     System.out.println("Deleted old profile picture: " + oldImagePathToDelete);
                                 } else {
                                     // Log if deletion failed (e.g., file lock)
                                     System.err.println("Could not delete old profile picture (may be in use?): " + oldImagePathToDelete);
                                 }
                             }
                         } catch (IOException ioEx) {
                             System.err.println("Error deleting old profile picture file '" + oldImagePathToDelete + "': " + ioEx.getMessage());
                         } catch (Exception e) {
                              System.err.println("Unexpected error deleting old profile picture: " + e.getMessage());
                         }
                     }

                     // Update internal state and UI
                     currentImagePath = newImagePath; // Update the current path
                     selectedImageFile = null; // Clear the selected file reference
                     saveButton.setEnabled(false); // Disable save button after successful save
                     toggleEditMode(false); // Exit edit mode on success
                     JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                } else {
                    // Should not happen if ID is correct, but handle it
                    JOptionPane.showMessageDialog(this, "Failed to update profile. Customer not found?", "Update Failed", JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException ex) {
                 System.err.println("Error updating profile: " + ex.getMessage());
                 ex.printStackTrace();
                 // Check for unique constraint violation (e.g., email already exists)
                 if (ex.getSQLState() != null && ex.getSQLState().startsWith("23")) { // SQLState for Integrity Constraint Violation
                    JOptionPane.showMessageDialog(this, "Email address '" + newEmail + "' is already in use by another account.", "Update Error", JOptionPane.ERROR_MESSAGE);
                 } else {
                    JOptionPane.showMessageDialog(this, "Error updating profile in database.", "Database Error", JOptionPane.ERROR_MESSAGE);
                 }
            } catch (Exception e) {
                 // Catch any other unexpected errors
                 System.err.println("Unexpected error saving profile: " + e.getMessage());
                 e.printStackTrace();
                 JOptionPane.showMessageDialog(this, "An unexpected error occurred while saving.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            finally {
                // Ensure resources are closed
                try { if (stmt != null) stmt.close(); } catch (SQLException ignored) {}
                try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
            }
        }

    } // --- End ProfileDialog Inner Class ---


    // Main method to launch the CustomerFrame
    public static void main(String[] args) {
        try {
            // Apply FlatLaf look and feel
            UIManager.setLookAndFeel(new FlatDarkLaf());
            // Set global UI properties for consistent theme
             UIManager.put("OptionPane.background", ThemeColors.DIALOG_BG);
             UIManager.put("Panel.background", ThemeColors.DIALOG_BG); // General panel background
             UIManager.put("OptionPane.messageForeground", ThemeColors.DIALOG_FG);
             UIManager.put("Button.background", ThemeColors.SECONDARY); // Default button background
             UIManager.put("Button.foreground", Color.WHITE); // Default button text color
             UIManager.put("Button.focus", new Color(ThemeColors.SECONDARY.getRed(), ThemeColors.SECONDARY.getGreen(), ThemeColors.SECONDARY.getBlue(), 180)); // Focus color
             UIManager.put("Button.hoverBackground", ThemeColors.BUTTON_HOVER);
             UIManager.put("Button.pressedBackground", ThemeColors.SECONDARY.darker());
             // Tabbed Pane styling (consistent with initialization)
             UIManager.put("TabbedPane.selected", ThemeColors.PRIMARY.darker());
             UIManager.put("TabbedPane.foreground", ThemeColors.TEXT);
             UIManager.put("TabbedPane.contentAreaColor", ThemeColors.CARD_BG);
             UIManager.put("TabbedPane.selectedForeground", Color.WHITE);
             UIManager.put("TabbedPane.borderColor", ThemeColors.SECONDARY);
             UIManager.put("TabbedPane.darkShadow", ThemeColors.SECONDARY);
             UIManager.put("TabbedPane.light", ThemeColors.BACKGROUND);
             UIManager.put("TabbedPane.focus", ThemeColors.PRIMARY);
             // Increase default font size slightly (optional)
             UIManager.put("defaultFont", new Font("Arial", Font.PLAIN, 13));

        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatDarkLaf. Using default Look and Feel.");
        }

        SwingUtilities.invokeLater(() -> {
            // --- Replace '1' with the actual customer ID obtained after login ---
            // This ID should come from the LoginFrame successful login process
            int loggedInCustomerId = 1; // Example Customer ID - MUST BE REPLACED WITH ACTUAL ID
            // --- END ---
            new CustomerFrame(loggedInCustomerId); // Create and show the main customer window
        });
    }

    // --- Static inner class for Database Connection ---
    private static class DBConnection {
        // Database connection details (replace with your actual credentials)
        private static final String URL = "jdbc:mysql://localhost:3306/kpop_merch_store"; // Your DB URL
        private static final String USER = "root"; // Your DB Username
        private static final String PASSWORD = ""; // Your DB Password

        public static Connection connect() throws SQLException {
            try {
                // Ensure MySQL Connector/J driver is loaded
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                System.err.println("MySQL JDBC Driver not found. Include it in your library path.");
                e.printStackTrace();
                throw new SQLException("JDBC Driver not found", e);
            }
            // Establish and return the connection
            return DriverManager.getConnection(URL, USER, PASSWORD);
        }
    }

    // --- WrapLayout Inner Class (for product grid wrapping) ---
    private static class WrapLayout extends FlowLayout {
        // private Dimension preferredLayoutSize; // This variable seems unused

        public WrapLayout() { super(); }
        public WrapLayout(int align) { super(align); }
        public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            Dimension minimum = layoutSize(target, false);
            minimum.width -= (getHgap() + 1); // Adjust minimum width calculation
            return minimum;
        }

        // Calculates layout size considering wrapping
        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                // Determine the maximum width available for components
                int targetWidth = target.getSize().width;
                 Container container = target;
                 // If target width is 0, try finding the parent's width (useful during initial layout)
                 while (container.getSize().width == 0 && container.getParent() != null) {
                     container = container.getParent();
                 }
                 targetWidth = container.getSize().width;

                // If still 0, use a very large width (effectively no wrapping constraint yet)
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;

                int hgap = getHgap();
                int vgap = getVgap();
                Insets insets = target.getInsets();
                int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
                // Max width for components in a row
                int maxWidth = targetWidth - horizontalInsetsAndGap;

                Dimension dim = new Dimension(0, 0); // Total dimension of the layout
                int rowWidth = 0; // Current width of the row being built
                int rowHeight = 0; // Current height of the row being built

                int nmembers = target.getComponentCount();
                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        // Get component's preferred or minimum size
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        // Check if adding this component exceeds the max width
                        if (rowWidth + d.width > maxWidth && rowWidth > 0) {
                            // Current row is full, add its dimensions and start a new row
                            addRow(dim, rowWidth, rowHeight);
                            rowWidth = 0;
                            rowHeight = 0;
                        }
                        // Add horizontal gap if not the first component in the row
                        if (rowWidth != 0) { rowWidth += hgap; }
                        // Add component width to current row width
                        rowWidth += d.width;
                        // Update row height to the maximum component height in the row
                        rowHeight = Math.max(rowHeight, d.height);
                    }
                }
                // Add the last row's dimensions
                addRow(dim, rowWidth, rowHeight);

                // Add insets and vertical gaps to the total dimensions
                dim.width += horizontalInsetsAndGap;
                dim.height += insets.top + insets.bottom + vgap * 2;

                // Adjust width based on the containing scroll pane's viewport, if available
                Container scrollPaneContainer = SwingUtilities.getAncestorOfClass(JScrollPane.class, target);
                if (scrollPaneContainer instanceof JScrollPane && target.isValid()) {
                    JScrollPane scrollPane = (JScrollPane) scrollPaneContainer;
                    // Ensure the preferred width is at least the viewport width to avoid unnecessary horizontal scrollbars
                    dim.width = Math.max(scrollPane.getViewport().getWidth(), dim.width);
                }
                return dim;
            }
        }

        // Helper method to update total layout dimensions based on a completed row
        private void addRow(Dimension dim, int rowWidth, int rowHeight) {
            dim.width = Math.max(dim.width, rowWidth); // Update total width if this row is wider
            // Add vertical gap if this is not the first row
            if (dim.height > 0) { dim.height += getVgap(); }
            // Add the height of the current row
            dim.height += rowHeight;
        }
    } // --- End of WrapLayout ---

}