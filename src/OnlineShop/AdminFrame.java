package OnlineShop;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path; // Import Path
import java.nio.file.Paths; // Import Paths
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
// --- THEME: FlatLaf imported here, same as original ---
import com.formdev.flatlaf.FlatDarkLaf;

// --- XChart Imports ---
import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;
// Removed PieStyler as pie chart is removed
import org.knowm.xchart.style.CategoryStyler; // For Bar Chart
import org.knowm.xchart.style.XYStyler; // For Line Chart
import org.knowm.xchart.style.markers.Marker;

// --- THEME: Importing the external ThemeColors class ---
import OnlineShop.ThemeColors;


public class AdminFrame extends JFrame {
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JTable userTable, productTable, inventoryTable, salesTable, supplierTable, ordersTable;
    // Use the custom ProductTableModel
    private ProductTableModel productTableModel;
    private DefaultTableModel userTableModel, inventoryTableModel,
                            salesTableModel, supplierTableModel, ordersTableModel;

    // --- Components for Statistics Panel ---
    private JPanel statisticsContentPanel;      // Panel to hold the charts inside Statistics Panel
    private JPanel salesTrendChartPanel;        // Specific panel for the sales trend chart (to update it)
    private JPanel productRatingsChartPanel;    // Specific panel for the product ratings chart (to update it)
    private JComboBox<String> statisticsFilterComboBox; // Renamed for clarity, applies to all stat charts

    // --- Dashboard Components ---
    private JPanel dashboardPanel; // Make it a class member
    private JPanel metricsPanel;   // Make it a class member
    private JLabel totalProductsLabel, totalOrdersLabel, lowStockItemsLabel, totalCustomersLabel, totalRevenueLabel, pendingOrdersLabel; // Labels within cards

    // --- Declare Panel Variables as Class Members ---
    private JPanel usersPanel;
    private JPanel productsPanel; // <-- DECLARED HERE
    private JPanel inventoryPanel;
    private JPanel salesPanel;
    private JPanel ordersPanel;
    private JPanel statisticsPanel;
    // --- END OF DECLARED SECTION ---


    // --- Custom TableModel for Products to handle image paths ---
    private class ProductTableModel extends DefaultTableModel {
        private List<String> imagePaths = new ArrayList<>();
        private List<ImageIcon> imageIcons = new ArrayList<>(); // Store loaded icons

        public ProductTableModel() {
            // Added placeholder text "Image Path" for clarity, though the column shows icons
            super(new String[]{"ID", "Image", "Name", "Price", "Stock", "Group", "DB Image Path"}, 0); // Changed column name for clarity
        }

        @Override
        public Class<?> getColumnClass(int column) {
            // Column 1 is the image display
            return column == 1 ? ImageIcon.class : Object.class;
        }

        // Add row data along with the path and the loaded icon
        public void addRowWithImage(Object[] rowData, String imagePath, ImageIcon icon) {
            super.addRow(rowData);
            // Ensure the lists match the row count
            while (imagePaths.size() < getRowCount() - 1) imagePaths.add(null);
            while (imageIcons.size() < getRowCount() - 1) imageIcons.add(null);
            imagePaths.add(imagePath);
            imageIcons.add(icon); // Store the pre-loaded icon
        }


        public String getImagePath(int row) {
             if (row >= 0 && row < imagePaths.size()) {
                return imagePaths.get(row);
            }
            System.err.println("Warning: getImagePath called for invalid row: " + row);
            return null;
        }

        public ImageIcon getImageIcon(int row) {
             if (row >= 0 && row < imageIcons.size()) {
                return imageIcons.get(row);
            }
             System.err.println("Warning: getImageIcon called for invalid row: " + row);
            return null; // Or return a default placeholder icon
        }

         public void setImageData(int row, String path, ImageIcon icon) {
            if (row >= 0 && row < getRowCount() && row < imagePaths.size() && row < imageIcons.size()) {
                imagePaths.set(row, path);
                imageIcons.set(row, icon);
                // Update the icon displayed in the table
                setValueAt(icon, row, 1); // Update the ImageIcon in the cell
            } else {
                 System.err.println("Error: Attempted to set image data for invalid row: " + row);
             }
        }

        @Override
        public void removeRow(int row) {
             if (row >= 0 && row < getRowCount()) {
                  // Remove from internal lists BEFORE removing from the superclass model
                  // to avoid index out of bounds if lists are shorter
                 if (row < imagePaths.size()) imagePaths.remove(row);
                 if (row < imageIcons.size()) imageIcons.remove(row);
                 super.removeRow(row);
             }
        }


        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // Generally make tables read-only here
        }
    }


    public AdminFrame() {
        // --- THEME: Using imported ThemeColors ---
        setTitle("Admin Panel - K-Pop Merch Store");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Use DISPOSE_ON_CLOSE if this isn't the main exit point
        setLayout(new BorderLayout());

        // --- START MODIFICATION: Full Screen and Undecorated ---
        setUndecorated(true); // Remove window title bar and borders
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximize the frame
        // --- END MODIFICATION ---

        // CardLayout for switching between views
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(ThemeColors.BACKGROUND); // Use imported theme color

        // Create all panels (Initialize class member variables)
        dashboardPanel = createDashboardPanel();
        usersPanel = createUsersPanel();
        productsPanel = createProductsPanel(); // <-- INITIALIZE HERE
        inventoryPanel = createInventoryPanel();
        salesPanel = createSalesPanel();
        ordersPanel = createOrdersPanel();
        statisticsPanel = createStatisticsPanel(); // Create the new panel

        // Add panels to mainPanel
        mainPanel.add(dashboardPanel, "Dashboard");
        mainPanel.add(usersPanel, "Users");
        mainPanel.add(productsPanel, "Products"); // Now uses the class member
        mainPanel.add(inventoryPanel, "Inventory");
        mainPanel.add(salesPanel, "Sales");
        mainPanel.add(ordersPanel, "Orders");
        mainPanel.add(statisticsPanel, "Statistics"); // Add the new panel

        add(mainPanel, BorderLayout.CENTER);
        add(createNavigationBar(), BorderLayout.WEST);

        //setLocationRelativeTo(null); // No longer needed with MAXIMIZED_BOTH
        setVisible(true); // Make visible *after* setting undecorated and adding components

        // Load initial data for dashboard metrics after frame is visible
        SwingUtilities.invokeLater(this::refreshDashboardMetrics);
        // Load initial charts for statistics panel
        SwingUtilities.invokeLater(this::updateStatisticsCharts);
    }

    private JPanel createNavigationBar() {
         JPanel navBar = new JPanel(new BorderLayout());
         navBar.setBackground(ThemeColors.BACKGROUND); // Use imported theme color
         navBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
         navBar.setPreferredSize(new Dimension(220, getHeight()));

         // Logo Section
         JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
         logoPanel.setOpaque(false);

         JLabel logo = new JLabel("ADMIN");
         logo.setFont(new Font("Arial", Font.BOLD, 16));
         logo.setForeground(ThemeColors.PRIMARY); // Use imported theme color
         logoPanel.add(logo);
         navBar.add(logoPanel, BorderLayout.NORTH);

         // Navigation Buttons
         JPanel navButtons = new JPanel();
         navButtons.setLayout(new BoxLayout(navButtons, BoxLayout.Y_AXIS));
         navButtons.setOpaque(false);
         navButtons.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

         String[] navItems = {"Dashboard", "Users", "Products", "Inventory", "Sales", "Orders", "Statistics"};

         for (String item : navItems) {
             JButton btn = createNavButton(item); // createNavButton uses imported ThemeColors
             btn.setAlignmentX(Component.LEFT_ALIGNMENT);
             navButtons.add(btn);
             navButtons.add(Box.createRigidArea(new Dimension(0, 8)));
         }

         JScrollPane scrollPane = new JScrollPane(navButtons);
         scrollPane.setBorder(null);
         scrollPane.setOpaque(false);
         scrollPane.getViewport().setOpaque(false);
         navBar.add(scrollPane, BorderLayout.CENTER);

         // Logout Button
         JButton logoutBtn = createNavButton("Logout");
         logoutBtn.setBackground(ThemeColors.SECONDARY); // Use imported theme color
         logoutBtn.setForeground(Color.WHITE);
         logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

         JPanel bottomPanel = new JPanel();
         bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
         bottomPanel.setOpaque(false);
         bottomPanel.add(Box.createVerticalGlue());
         bottomPanel.add(logoutBtn);
         bottomPanel.add(Box.createRigidArea(new Dimension(0, 10)));

         navBar.add(bottomPanel, BorderLayout.SOUTH);

         return navBar;
    }

    private JButton createNavButton(String text) {
         JButton button = new JButton(text);

         // Button styling using imported ThemeColors
         button.setFont(new Font("Arial", Font.PLAIN, 14));
         button.setForeground(ThemeColors.TEXT); // Use imported theme color
         button.setBackground(ThemeColors.CARD_BG); // Use imported theme color
         button.setFocusPainted(false);
         button.setHorizontalAlignment(SwingConstants.CENTER);
         button.setVerticalAlignment(SwingConstants.CENTER);
         button.setBorder(BorderFactory.createCompoundBorder(
             BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.SECONDARY), // Use imported theme color
             BorderFactory.createEmptyBorder(12, 15, 12, 15)
         ));
         button.setPreferredSize(new Dimension(200, 40));
         button.setMaximumSize(new Dimension(200, 40));
         button.setMinimumSize(new Dimension(200, 40));

         // Icon configuration (optional)
         try {
            String iconPath = "/icons/" + text.toLowerCase() + ".png"; // Assuming icons are in a package 'icons'
            URL iconUrl = getClass().getResource(iconPath);
            if (iconUrl != null) {
                ImageIcon icon = new ImageIcon(iconUrl);
                 Image img = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                 button.setIcon(new ImageIcon(img));
                button.setHorizontalTextPosition(SwingConstants.RIGHT);
                button.setIconTextGap(10);
            } else {
                System.err.println("Icon not found: " + iconPath);
            }
         } catch (Exception e) {
              System.err.println("Error loading icon for button '" + text + "': " + e.getMessage());
         }

         // Action listeners for all navigation buttons
         switch(text) {
             case "Dashboard":
                 button.addActionListener(e -> {
                     refreshDashboardMetrics(); // Refresh metrics when switching to dashboard
                     cardLayout.show(mainPanel, "Dashboard");
                 });
                 break;
             case "Users":
                 button.addActionListener(e -> {
                     loadUsers();
                     cardLayout.show(mainPanel, "Users");
                 });
                 break;
             case "Products":
                 button.addActionListener(e -> {
                     loadProducts();
                     cardLayout.show(mainPanel, "Products");
                 });
                 break;
             case "Inventory":
                 button.addActionListener(e -> {
                     loadInventory();
                     cardLayout.show(mainPanel, "Inventory");
                 });
                 break;
             case "Sales":
                 button.addActionListener(e -> {
                     loadSales();
                     cardLayout.show(mainPanel, "Sales");
                 });
                 break;
             case "Orders":
                 button.addActionListener(e -> {
                     loadOrders();
                     cardLayout.show(mainPanel, "Orders");
                 });
                 break;
             case "Statistics":
                 button.addActionListener(e -> {
                     updateStatisticsCharts(); // Load default view for *all* charts
                     cardLayout.show(mainPanel, "Statistics");
                 });
                 break;
             case "Logout":
                 button.addActionListener(e -> logout());
                 break;
         }

         // Hover effects using imported ThemeColors
         button.addMouseListener(new MouseAdapter() {
             Color originalBg = button.getBackground();
             Color originalFg = button.getForeground();
             @Override
             public void mouseEntered(MouseEvent e) {
                 button.setBackground(ThemeColors.BUTTON_HOVER); // Use imported theme color
                 button.setForeground(Color.WHITE); // Keep white for hover contrast
                 button.setCursor(new Cursor(Cursor.HAND_CURSOR));
             }

             @Override
             public void mouseExited(MouseEvent e) {
                 if (!text.equals("Logout")) {
                     button.setBackground(originalBg);
                     button.setForeground(originalFg);
                 } else {
                      button.setBackground(ThemeColors.SECONDARY); // Use imported theme color for logout distinct look
                      button.setForeground(Color.WHITE);
                 }
                 button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
             }
         });

         return button;
    }

    // --- Dashboard Panel uses imported ThemeColors ---

    private JPanel createDashboardPanel() {
        dashboardPanel = new JPanel(new BorderLayout()); // Use the class member
        dashboardPanel.setBackground(ThemeColors.BACKGROUND); // Use imported theme color
        dashboardPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Admin Dashboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(ThemeColors.PRIMARY); // Use imported theme color
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        dashboardPanel.add(titleLabel, BorderLayout.NORTH);

        // Main content panel using GridBagLayout
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(ThemeColors.BACKGROUND); // Use imported theme color
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Padding
        gbc.fill = GridBagConstraints.BOTH; // Components fill space

        // Metrics panel (Top row, spanning 2 columns)
        metricsPanel = new JPanel(new GridLayout(2, 3, 20, 20)); // Use the class member
        metricsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        metricsPanel.setBackground(ThemeColors.BACKGROUND); // Use imported theme color

        // Create labels for metric values (will be updated in refreshDashboardMetrics)
        totalProductsLabel = createMetricValueLabel("0");
        totalOrdersLabel = createMetricValueLabel("0");
        lowStockItemsLabel = createMetricValueLabel("0");
        totalCustomersLabel = createMetricValueLabel("0");
        totalRevenueLabel = createMetricValueLabel("₱0.00");
        pendingOrdersLabel = createMetricValueLabel("0");

        // Add cards with titles and the labels
        metricsPanel.add(createMetricCard("Total Products", totalProductsLabel, "/icons/products.png"));
        metricsPanel.add(createMetricCard("Total Orders", totalOrdersLabel, "/icons/orders.png"));
        metricsPanel.add(createMetricCard("Low Stock Items", lowStockItemsLabel, "/icons/inventory.png"));
        metricsPanel.add(createMetricCard("Total Customers", totalCustomersLabel, "/icons/users.png"));
        metricsPanel.add(createMetricCard("Total Revenue", totalRevenueLabel, "/icons/sales.png"));
        metricsPanel.add(createMetricCard("Pending Orders", pendingOrdersLabel, "/icons/dashboard.png"));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Span 2 columns
        gbc.weightx = 1.0;
        gbc.weighty = 0.2; // Allocate less vertical space
        contentPanel.add(metricsPanel, gbc);

        // Real-time Order Notifications Panel (Below metrics, spanning 2 columns)
        JPanel notificationsPanel = createOrderNotificationsPanel(); // Extracted method
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2; // Span 2 columns
        gbc.weightx = 1.0;
        gbc.weighty = 0.8; // Takes remaining vertical space
        contentPanel.add(notificationsPanel, gbc);


        dashboardPanel.add(contentPanel, BorderLayout.CENTER);

        return dashboardPanel;
    }

    // Helper to create the JLabel for metric values
    private JLabel createMetricValueLabel(String initialText) {
        JLabel label = new JLabel(initialText, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        label.setForeground(ThemeColors.PRIMARY); // Use imported theme color
        return label;
    }

    // Method to refresh dashboard metrics
    private void refreshDashboardMetrics() {
        if (totalProductsLabel != null) totalProductsLabel.setText(getTotalProducts());
        if (totalOrdersLabel != null) totalOrdersLabel.setText(getTotalOrders());
        if (lowStockItemsLabel != null) lowStockItemsLabel.setText(getLowStockItems());
        if (totalCustomersLabel != null) totalCustomersLabel.setText(getTotalCustomers());
        if (totalRevenueLabel != null) totalRevenueLabel.setText(getTotalRevenue());
        if (pendingOrdersLabel != null) pendingOrdersLabel.setText(getPendingOrders());

        // Force redraw if necessary
        if (metricsPanel != null) {
            metricsPanel.revalidate();
            metricsPanel.repaint();
        }
        if (dashboardPanel != null) {
            dashboardPanel.revalidate();
            dashboardPanel.repaint();
        }
    }

    // --- Order Notifications Panel uses imported ThemeColors ---
    private JPanel createOrderNotificationsPanel() {
        JPanel notificationsPanel = new JPanel(new BorderLayout());
        notificationsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(ThemeColors.SECONDARY), // Use imported theme color
            "Recent Order Notifications",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 16),
            ThemeColors.PRIMARY // Use imported theme color
        ));
        notificationsPanel.setBackground(ThemeColors.BACKGROUND); // Use imported theme color

        // Notifications Table
        DefaultTableModel notificationsModel = new DefaultTableModel(
            new String[]{"Time", "Order #", "Customer", "Amount", "Items"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable notificationsTable = new JTable(notificationsModel);
        styleTable(notificationsTable); // styleTable uses imported ThemeColors
        notificationsTable.setRowHeight(28);

        // Specific column styling for notificationsTable
        TableColumnModel ncm = notificationsTable.getColumnModel();
        DefaultTableCellRenderer nCenterRenderer = createCenterRenderer(); // Renderer uses imported ThemeColors
        DefaultTableCellRenderer nRightRenderer = createRightRenderer();   // Renderer uses imported ThemeColors

        try {
            ncm.getColumn(0).setCellRenderer(nCenterRenderer); // Time Center
            ncm.getColumn(1).setCellRenderer(nCenterRenderer); // Order # Center
            ncm.getColumn(3).setCellRenderer(nRightRenderer);  // Amount Right
            ncm.getColumn(4).setCellRenderer(nCenterRenderer); // Items Center
            ncm.getColumn(0).setPreferredWidth(60);
            ncm.getColumn(1).setPreferredWidth(80);
            ncm.getColumn(2).setPreferredWidth(150);
            ncm.getColumn(3).setPreferredWidth(100);
            ncm.getColumn(4).setPreferredWidth(80);
        } catch (ArrayIndexOutOfBoundsException aioobe) {
             System.err.println("Error setting column renderers/widths for notifications table: " + aioobe.getMessage());
        }

        // Load initial notifications
        loadOrderNotifications(notificationsModel);

        // Auto-refresh timer
        Timer refreshTimer = new Timer(15000, e -> {
            int previousRowCount = notificationsModel.getRowCount();
            loadOrderNotifications(notificationsModel);
            if (notificationsModel.getRowCount() > previousRowCount) {
                Toolkit.getDefaultToolkit().beep();
                 notificationsPanel.setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(ThemeColors.PRIMARY, 2), // Use imported theme color
                        "NEW ORDERS! (" + (notificationsModel.getRowCount() - previousRowCount) + ") - Recent Order Notifications",
                        TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 16), ThemeColors.PRIMARY)); // Use imported theme color
                 new Timer(3000, ev -> {
                     notificationsPanel.setBorder(BorderFactory.createTitledBorder(
                             BorderFactory.createLineBorder(ThemeColors.SECONDARY), "Recent Order Notifications", // Use imported theme color
                             TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 16), ThemeColors.PRIMARY)); // Use imported theme color
                     ((Timer) ev.getSource()).stop();
                 }).start();
            }
        });
        refreshTimer.start();

        // Add view button
        JButton viewOrdersButton = new JButton("View All Orders");
        viewOrdersButton.addActionListener(e -> {
            loadOrders();
            cardLayout.show(mainPanel, "Orders");
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(viewOrdersButton);

        notificationsPanel.add(new JScrollPane(notificationsTable), BorderLayout.CENTER);
        notificationsPanel.add(buttonPanel, BorderLayout.SOUTH);

        return notificationsPanel;
    }


    // --- Method to load order notifications (no theme changes) ---
    private void loadOrderNotifications(DefaultTableModel model) {
         try (Connection conn = DBConnection.connect();
              Statement stmt = conn.createStatement();
              ResultSet rs = stmt.executeQuery(
                  "SELECT o.id, c.name, o.total_price, o.order_date, " +
                  "(SELECT COUNT(*) FROM order_items WHERE order_id = o.id) as item_count " +
                  "FROM orders o JOIN customers c ON o.customer_id = c.id " +
                  "WHERE o.order_date >= NOW() - INTERVAL 24 HOUR " +
                  "ORDER BY o.order_date DESC LIMIT 10")) {

             model.setRowCount(0);
             SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

             while (rs.next()) {
                 String time = sdf.format(rs.getTimestamp("order_date"));
                 String orderId = "#" + rs.getInt("id");
                 String customer = rs.getString("name");
                 String amount = String.format("₱%.2f", rs.getDouble("total_price"));
                 String items = rs.getInt("item_count") + " item(s)";

                 model.addRow(new Object[]{time, orderId, customer, amount, items});
             }
         } catch (SQLException ex) {
             ex.printStackTrace();
             JOptionPane.showMessageDialog(this,
                 "Error loading order notifications: " + ex.getMessage(),
                 "Database Error", JOptionPane.ERROR_MESSAGE);
         }
    }

    // --- Metric Card uses imported ThemeColors ---
    // Modified to accept JLabel for the value instead of a String
    private JPanel createMetricCard(String title, JLabel valueLabel, String iconResourcePath) {
         JPanel card = new JPanel(new BorderLayout(10, 10));
         card.setBackground(ThemeColors.CARD_BG); // Use imported theme color
         card.setBorder(BorderFactory.createCompoundBorder(
             BorderFactory.createMatteBorder(1, 1, 1, 1, ThemeColors.SECONDARY), // Use imported theme color
             BorderFactory.createEmptyBorder(15, 15, 15, 15)
         ));

         // Top panel with icon and title
         JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
         topPanel.setOpaque(false);

         JLabel iconLabel = new JLabel();
         try {
             URL iconUrl = getClass().getResource(iconResourcePath);
             if (iconUrl != null) {
                 ImageIcon icon = new ImageIcon(iconUrl);
                  Image img = icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                  iconLabel.setIcon(new ImageIcon(img));
             } else {
                 System.err.println("Metric icon not found: " + iconResourcePath);
                 iconLabel.setText("!");
                 iconLabel.setForeground(Color.RED);
             }
         } catch (Exception e) {
             System.err.println("Error loading metric icon " + iconResourcePath + ": " + e);
             iconLabel.setText("Err");
             iconLabel.setForeground(Color.RED);
         }

         JLabel titleLabel = new JLabel(title);
         titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
         titleLabel.setForeground(ThemeColors.TEXT); // Use imported theme color

         topPanel.add(iconLabel);
         topPanel.add(titleLabel);

         // Add the passed JLabel for value display
         card.add(topPanel, BorderLayout.NORTH);
         card.add(valueLabel, BorderLayout.CENTER); // Use the provided JLabel

         return card;
    }

    // --- Panel Creation Methods (Use imported ThemeColors via helpers) ---
    private JPanel createUsersPanel() {
        JPanel panel = createTablePanel("User Management");
        // Removed "Loyalty Points" column
        userTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Role"}, 0) {
             @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        userTable = new JTable(userTableModel);
        styleTable(userTable); // Apply common styling

        // Add edit button
        JButton editUserButton = new JButton("Edit User");
        editUserButton.addActionListener(e -> editUser());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(editUserButton);

        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createProductsPanel() {
        JPanel panel = createTablePanel("Product Management");
        productTableModel = new ProductTableModel();
        productTable = new JTable(productTableModel);
        styleTable(productTable); // Apply common styling

        // Set custom renderer for the image column (index 1)
        productTable.getColumnModel().getColumn(1).setCellRenderer(new ImageRenderer()); // Renderer uses imported ThemeColors
        productTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        productTable.getColumnModel().getColumn(1).setMaxWidth(120);
        productTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        productTable.getColumnModel().getColumn(0).setMaxWidth(60);
        productTable.getColumnModel().getColumn(3).setPreferredWidth(80); // Price
        productTable.getColumnModel().getColumn(4).setPreferredWidth(60); // Stock

        productTable.setRowHeight(80);

        // Add buttons for product management
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        JButton addProductButton = new JButton("Add Product");
        addProductButton.addActionListener(e -> addProduct());

        JButton editProductButton = new JButton("Edit Product");
        editProductButton.addActionListener(e -> editProduct());

        JButton deleteProductButton = new JButton("Delete Product");
        deleteProductButton.addActionListener(e -> deleteProduct());

        buttonPanel.add(addProductButton);
        buttonPanel.add(editProductButton);
        buttonPanel.add(deleteProductButton);

        panel.add(new JScrollPane(productTable), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createInventoryPanel() {
         JPanel panel = createTablePanel("Inventory Management");
         // Adjusted columns based on the schema (removed 'Supplier', added 'Reorder Level')
         inventoryTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Stock", "Reorder Level"}, 0){
              @Override public boolean isCellEditable(int row, int column) { return false; }
         };
         inventoryTable = new JTable(inventoryTableModel);
         styleTable(inventoryTable);

         // Add buttons for inventory management
         JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
         buttonPanel.setOpaque(false);

         JButton updateStockButton = new JButton("Update Stock");
         updateStockButton.addActionListener(e -> updateStock());

         JButton restockButton = new JButton("Generate Restock List");
         restockButton.addActionListener(e -> generateRestockList());

         buttonPanel.add(updateStockButton);
         buttonPanel.add(restockButton);

         panel.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);
         panel.add(buttonPanel, BorderLayout.SOUTH);
         return panel;
    }

    private JPanel createSalesPanel() {
         JPanel panel = createTablePanel("Sales Reports");
         // Adjusted columns based on the combined info from orders, order_items, products, customers
         salesTableModel = new DefaultTableModel(
             new String[]{"Order ID", "Customer", "Product", "Quantity", "Item Price", "Total Price", "Date", "Status"}, 0){
              @Override public boolean isCellEditable(int row, int column) { return false; }
         };
         salesTable = new JTable(salesTableModel);
         styleTable(salesTable); // Apply common styling and column adjustments

         // Add filter options
         JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
         filterPanel.setOpaque(false);

         // Updated status options based on the orders table schema (enum values + 'All')
         JComboBox<String> statusFilter = new JComboBox<>(
             new String[]{"All", "Processing", "Shipped", "Delivered", "Cancelled", "Completed"}); // Assuming 'Completed' maps to Delivered/Completed in DB
         JButton filterButton = new JButton("Filter");
         filterButton.addActionListener(e -> filterSales(statusFilter.getSelectedItem().toString()));

         filterPanel.add(new JLabel("Status:"));
         filterPanel.add(statusFilter);
         filterPanel.add(filterButton);

         panel.add(filterPanel, BorderLayout.NORTH);
         panel.add(new JScrollPane(salesTable), BorderLayout.CENTER);
         return panel;
    }

    private JPanel createOrdersPanel() {
         JPanel panel = createTablePanel("Order Management");
         // Adjusted columns based on the orders table schema
         ordersTableModel = new DefaultTableModel(
             new String[]{"Order ID", "Customer", "Total", "Date", "Status", "Cancellation Request"}, 0) {
             @Override
             public boolean isCellEditable(int row, int column) {
                 return false;
             }
         };
         ordersTable = new JTable(ordersTableModel);
         styleTable(ordersTable); // Apply common styling and column adjustments

         // Add order management buttons
         JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
         buttonPanel.setOpaque(false);

         JButton updateStatusButton = new JButton("Update Status");
         updateStatusButton.addActionListener(e -> updateOrderStatus());

         JButton viewDetailsButton = new JButton("View Details");
         viewDetailsButton.addActionListener(e -> viewOrderDetails());

         JButton processCancelButton = new JButton("Process Cancellation");
         processCancelButton.addActionListener(e -> processCancellation());

         buttonPanel.add(viewDetailsButton);
         buttonPanel.add(updateStatusButton);
         buttonPanel.add(processCancelButton);

         panel.add(new JScrollPane(ordersTable), BorderLayout.CENTER);
         panel.add(buttonPanel, BorderLayout.SOUTH);
         return panel;
    }


    // --- Table Panel Helper uses imported ThemeColors ---
    private JPanel createTablePanel(String title) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(ThemeColors.BACKGROUND); // Use imported theme color
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Title
        JLabel titleLabel = new JLabel(title, SwingConstants.LEFT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(ThemeColors.PRIMARY); // Use imported theme color
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 15, 5));
        panel.add(titleLabel, BorderLayout.NORTH);

        return panel;
    }

    // --- Table Styling uses imported ThemeColors ---
    private void styleTable(JTable table) {
        table.setBackground(ThemeColors.CARD_BG); // Use imported theme color
        table.setForeground(ThemeColors.TEXT); // Use imported theme color
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setGridColor(ThemeColors.SECONDARY.darker()); // Use imported theme color (darker version for grid)
        table.setSelectionBackground(ThemeColors.PRIMARY); // Use imported theme color
        table.setSelectionForeground(Color.WHITE); // Keep white for contrast
        table.setRowHeight(table == productTable ? 80 : 35);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Use OFF initially to set preferred widths

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(ThemeColors.PRIMARY); // Use imported theme color
        header.setForeground(Color.WHITE); // Keep white for contrast
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setDefaultRenderer(createHeaderRenderer()); // Renderer uses imported ThemeColors

        // Apply default cell renderer
        DefaultTableCellRenderer cellRenderer = createDefaultCellRenderer(); // Renderer uses imported ThemeColors
        for (int i = 0; i < table.getColumnCount(); i++) {
            // Skip image column for products table, it has its own renderer
            if (table == productTable && i == 1) continue;
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        // Specific Column Alignments & Renderers based on updated schemas
        DefaultTableCellRenderer centerRenderer = createCenterRenderer(); // Renderer uses imported ThemeColors
        DefaultTableCellRenderer rightRenderer = createRightRenderer();   // Renderer uses imported ThemeColors
        TableColumnModel cm = table.getColumnModel();

        try {
             if (table == productTable) { // Columns: ID, Image, Name, Price, Stock, Group, DB Image Path
                 cm.getColumn(0).setCellRenderer(centerRenderer); // ID
                 cm.getColumn(1).setCellRenderer(new ImageRenderer()); // Image (special renderer)
                 cm.getColumn(3).setCellRenderer(rightRenderer); // Price
                 cm.getColumn(4).setCellRenderer(centerRenderer); // Stock
                 cm.getColumn(0).setPreferredWidth(60);
                 cm.getColumn(1).setPreferredWidth(100);
                 cm.getColumn(2).setPreferredWidth(250); // Name
                 cm.getColumn(3).setPreferredWidth(100); // Price
                 cm.getColumn(4).setPreferredWidth(80);  // Stock
                 cm.getColumn(5).setPreferredWidth(150); // Group
                 cm.getColumn(6).setPreferredWidth(200); // DB Image Path (can be wider)
             } else if (table == userTable) { // Columns: ID, Name, Email, Role (Removed Loyalty Points)
                 cm.getColumn(0).setCellRenderer(centerRenderer); // ID
                 // No alignment needed for Loyalty Points as it's removed
                 cm.getColumn(0).setPreferredWidth(50);
                 cm.getColumn(1).setPreferredWidth(250); // Name // Reverted from 250
                 cm.getColumn(2).setPreferredWidth(300); // Email // Reverted from 300
                 cm.getColumn(3).setPreferredWidth(100); // Role
                 // No width needed for Loyalty Points
             } else if (table == inventoryTable) { // Columns: ID, Name, Stock, Reorder Level
                  cm.getColumn(0).setCellRenderer(centerRenderer); // ID
                  cm.getColumn(2).setCellRenderer(centerRenderer); // Stock
                  cm.getColumn(3).setCellRenderer(centerRenderer); // Reorder Level
                  cm.getColumn(0).setPreferredWidth(60);
                  cm.getColumn(1).setPreferredWidth(300); // Name
                  cm.getColumn(2).setPreferredWidth(100); // Stock
                  cm.getColumn(3).setPreferredWidth(120); // Reorder Level
             } else if (table == salesTable) { // Columns: Order ID, Customer, Product, Quantity, Item Price, Total Price, Date, Status
                  cm.getColumn(0).setCellRenderer(centerRenderer); // Order ID
                  cm.getColumn(3).setCellRenderer(centerRenderer); // Quantity
                  cm.getColumn(4).setCellRenderer(rightRenderer);  // Item Price
                  cm.getColumn(5).setCellRenderer(rightRenderer);  // Total Price
                  cm.getColumn(0).setPreferredWidth(80);
                  cm.getColumn(1).setPreferredWidth(180); // Customer
                  cm.getColumn(2).setPreferredWidth(250); // Product
                  cm.getColumn(3).setPreferredWidth(80);  // Quantity
                  cm.getColumn(4).setPreferredWidth(120); // Item Price
                  cm.getColumn(5).setPreferredWidth(120); // Total Price
                  cm.getColumn(6).setPreferredWidth(150); // Date
                  cm.getColumn(7).setPreferredWidth(120); // Status
             } else if (table == ordersTable) { // Columns: Order ID, Customer, Total, Date, Status, Cancellation Request
                  cm.getColumn(0).setCellRenderer(centerRenderer); // Order ID
                  cm.getColumn(2).setCellRenderer(rightRenderer); // Total
                  cm.getColumn(5).setCellRenderer(centerRenderer); // Cancellation Request
                  cm.getColumn(0).setPreferredWidth(80);
                  cm.getColumn(1).setPreferredWidth(200); // Customer
                  cm.getColumn(2).setPreferredWidth(120); // Total
                  cm.getColumn(3).setPreferredWidth(150); // Date
                  cm.getColumn(4).setPreferredWidth(120); // Status
                  cm.getColumn(5).setPreferredWidth(150); // Cancellation Request
             }
             // Note: Styling for notificationsTable is handled within its creation method
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Error setting column renderers/widths: " + e.getMessage());
        }
         // Resize columns after setting preferred widths
         table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
    }

    // --- Helper methods for creating table renderers (Use imported ThemeColors) ---
    private TableCellRenderer createHeaderRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (c instanceof JLabel) {
                    JLabel label = (JLabel) c;
                    label.setHorizontalAlignment(SwingConstants.LEFT);
                    label.setBorder(BorderFactory.createCompoundBorder(
                        label.getBorder(), BorderFactory.createEmptyBorder(0, 10, 0, 10)));
                    c.setBackground(ThemeColors.PRIMARY); // Use imported theme color
                    c.setForeground(Color.WHITE); // Keep white for contrast
                    c.setFont(new Font("Arial", Font.BOLD, 14));
                }
                return c;
            }
        };
    }

    private DefaultTableCellRenderer createDefaultCellRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                if (!isSelected) {
                    // Alternating row colors based on imported ThemeColors
                    Color slightlyDarkerCard = new Color(
                         Math.max(0, ThemeColors.CARD_BG.getRed() - 10),
                         Math.max(0, ThemeColors.CARD_BG.getGreen() - 10),
                         Math.max(0, ThemeColors.CARD_BG.getBlue() - 10));
                    c.setBackground(row % 2 == 0 ? ThemeColors.CARD_BG : slightlyDarkerCard);
                    c.setForeground(ThemeColors.TEXT); // Use imported theme color
                } else {
                    c.setBackground(table.getSelectionBackground()); // Uses table's selection color (ThemeColors.PRIMARY)
                    c.setForeground(table.getSelectionForeground()); // Uses table's selection foreground (WHITE)
                }
                setHorizontalAlignment(SwingConstants.LEFT);
                return c;
            }
        };
    }

    private DefaultTableCellRenderer createCenterRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
             public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                 setHorizontalAlignment(SwingConstants.CENTER);
                 setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                if (!isSelected) {
                     Color slightlyDarkerCard = new Color(
                         Math.max(0, ThemeColors.CARD_BG.getRed() - 10),
                         Math.max(0, ThemeColors.CARD_BG.getGreen() - 10),
                         Math.max(0, ThemeColors.CARD_BG.getBlue() - 10));
                    c.setBackground(row % 2 == 0 ? ThemeColors.CARD_BG : slightlyDarkerCard);
                    c.setForeground(ThemeColors.TEXT); // Use imported theme color
                } else {
                     c.setBackground(table.getSelectionBackground()); // ThemeColors.PRIMARY
                     c.setForeground(table.getSelectionForeground()); // WHITE
                }
                 return c;
             }
        };
    }

    private DefaultTableCellRenderer createRightRenderer() {
         return new DefaultTableCellRenderer(){
             @Override
             public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                 setHorizontalAlignment(SwingConstants.RIGHT);
                 setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                 if (!isSelected) {
                      Color slightlyDarkerCard = new Color(
                         Math.max(0, ThemeColors.CARD_BG.getRed() - 10),
                         Math.max(0, ThemeColors.CARD_BG.getGreen() - 10),
                         Math.max(0, ThemeColors.CARD_BG.getBlue() - 10));
                    c.setBackground(row % 2 == 0 ? ThemeColors.CARD_BG : slightlyDarkerCard);
                    c.setForeground(ThemeColors.TEXT); // Use imported theme color
                 } else {
                     c.setBackground(table.getSelectionBackground()); // ThemeColors.PRIMARY
                     c.setForeground(table.getSelectionForeground()); // WHITE
                 }
                 // Format currency for specific columns if needed
                 if (table == salesTable && (column == 4 || column == 5)) { // Item Price and Total Price
                     if (value instanceof Number) {
                         setText(String.format("₱%.2f", ((Number) value).doubleValue()));
                     } else {
                          setText(value != null ? value.toString() : ""); // Handle non-numeric gracefully
                     }
                 } else if (table == ordersTable && column == 2) { // Order Total
                      if (value instanceof String && ((String)value).startsWith("₱")) {
                          // Already formatted, do nothing extra
                      } else if (value instanceof Number) {
                          setText(String.format("₱%.2f", ((Number) value).doubleValue()));
                      } else {
                           setText(value != null ? value.toString() : ""); // Handle non-numeric gracefully
                      }
                 } else {
                     // For other columns handled by this renderer, just set the text directly
                     setText(value != null ? value.toString() : "");
                 }

                 return c;
             }
        };
    }

    // --- Image Renderer Class (Uses imported ThemeColors) ---
    private class ImageRenderer extends DefaultTableCellRenderer {
        public ImageRenderer() {
            super();
            setHorizontalAlignment(JLabel.CENTER);
            setVerticalAlignment(JLabel.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            JLabel label = (JLabel) c;
            label.setText("");
            label.setIcon(null);

            if (value instanceof ImageIcon) {
                ImageIcon icon = (ImageIcon) value;
                if (isIconValid(icon)) {
                    int padding = 10;
                    int targetHeight = table.getRowHeight() - padding;
                    int targetWidth = table.getColumnModel().getColumn(column).getWidth() - padding;
                    int size = Math.min(targetHeight, targetWidth);

                     if (size > 0) {
                         if (icon.getIconWidth() > size || icon.getIconHeight() > size) {
                             Image image = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                             label.setIcon(new ImageIcon(image));
                         } else {
                             label.setIcon(icon);
                         }
                     } else {
                        label.setIcon(icon);
                     }
                } else {
                    label.setText("No Image");
                    label.setFont(new Font("Arial", Font.ITALIC, 10));
                }
            } else {
                 label.setText("No Image");
                 label.setFont(new Font("Arial", Font.ITALIC, 10));
            }

             if (!isSelected) {
                // Alternating colors using imported ThemeColors
                 Color slightlyDarkerCard = new Color(
                         Math.max(0, ThemeColors.CARD_BG.getRed() - 10),
                         Math.max(0, ThemeColors.CARD_BG.getGreen() - 10),
                         Math.max(0, ThemeColors.CARD_BG.getBlue() - 10));
                label.setBackground(row % 2 == 0 ? ThemeColors.CARD_BG : slightlyDarkerCard);
                label.setForeground(ThemeColors.TEXT); // Use imported theme color
            } else {
                label.setBackground(table.getSelectionBackground()); // ThemeColors.PRIMARY
                label.setForeground(table.getSelectionForeground()); // WHITE
            }
            label.setOpaque(true);

            return label;
        }
    }

    // --- Data Loading Methods (Adjusted queries based on schema) ---
    private void loadUsers() {
         // Using columns from 'customers' table schema, removed loyalty_points
         try (Connection conn = DBConnection.connect();
              Statement stmt = conn.createStatement();
              ResultSet rs = stmt.executeQuery("SELECT id, name, email, role FROM customers")) { // Removed loyalty_points from query
             userTableModel.setRowCount(0);
             while (rs.next()) {
                 userTableModel.addRow(new Object[]{
                     rs.getInt("id"),
                     rs.getString("name"),
                     rs.getString("email"),
                     rs.getString("role")
                     // Removed loyalty points loading
                 });
             }
         } catch (SQLException ex) {
             ex.printStackTrace();
             showThemedJOptionPane("Error loading users: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
         }
    }
    private void loadProducts() {
        productTableModel = new ProductTableModel();
        productTable.setModel(productTableModel);
        styleTable(productTable); // Re-apply styles after setting new model
        productTable.getColumnModel().getColumn(1).setCellRenderer(new ImageRenderer());
        productTable.setRowHeight(80);

        // Query based on 'products' table schema
        String query = "SELECT id, name, price, stock, group_name, description, image_path FROM products ORDER BY name";

        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // Image loading logic (remains largely the same, assumes image_path stores the relative/absolute path)
            String[] relativeBaseDirs = {"images/products/", "src/images/products/"}; // Adjust if your project structure is different
            String[] classpathBaseDirs = {"/images/products/", "/"}; // Adjust if different classpath location

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                int stock = rs.getInt("stock");
                String group = rs.getString("group_name");
                String imagePathFromDB = rs.getString("image_path");
                String description = rs.getString("description"); // Included description

                ImageIcon icon = null;
                String loadedFromPath = imagePathFromDB; // Store the path used for loading

                // 1. Try loading directly from the database path (as file or URL)
                if (imagePathFromDB != null && !imagePathFromDB.isEmpty()) {
                    File dbFile = new File(imagePathFromDB);
                    if (dbFile.exists() && dbFile.isFile()) {
                        try {
                            icon = new ImageIcon(dbFile.toURI().toURL());
                            if (!isIconValid(icon)) icon = null; // Reset if invalid
                        } catch (Exception e) { icon = null; }
                    }
                    // If file not found, try as resource path
                    if (icon == null) {
                        String resourcePathDB = imagePathFromDB.replace("\\", "/");
                        if (!resourcePathDB.startsWith("/")) resourcePathDB = "/" + resourcePathDB; // Ensure leading slash for resources
                        URL urlDB = getClass().getResource(resourcePathDB);
                        if (urlDB != null) {
                             icon = new ImageIcon(urlDB);
                             if (!isIconValid(icon)) icon = null; // Reset if invalid
                        }
                    }
                }

                // 2. If DB path failed or was null, try deriving filename from product name
                if (icon == null && name != null && !name.isEmpty()) {
                    // Sanitize name and append .png (or other common extensions if needed)
                    String derivedFilename = name.replaceAll("[^a-zA-Z0-9.\\-_ ]", "_").toLowerCase() + ".png";
                    loadedFromPath = "[Derived] " + derivedFilename; // Indicate it's derived

                    // Try relative file paths
                    for (String baseDir : relativeBaseDirs) {
                        File derivedFile = new File(baseDir + derivedFilename);
                         if (derivedFile.exists() && derivedFile.isFile()) {
                            try {
                                icon = new ImageIcon(derivedFile.toURI().toURL());
                                if (isIconValid(icon)) {
                                    loadedFromPath = derivedFile.getPath(); // Update loaded path
                                    break; // Found it
                                } else {
                                    icon = null; // Reset if invalid
                                }
                            } catch (Exception e) { icon = null; }
                        }
                    }
                    // If still not found, try classpath resources
                    if (icon == null) {
                         for (String baseDir : classpathBaseDirs) {
                            String resourcePathDerived = baseDir + derivedFilename;
                            if (!resourcePathDerived.startsWith("/")) resourcePathDerived = "/" + resourcePathDerived;

                            URL urlDerived = getClass().getResource(resourcePathDerived);
                            if (urlDerived != null) {
                                 icon = new ImageIcon(urlDerived);
                                 if (isIconValid(icon)) {
                                     loadedFromPath = resourcePathDerived; // Update loaded path
                                     break; // Found it
                                 } else {
                                     icon = null; // Reset if invalid
                                 }
                            }
                        }
                    }
                }

                // 3. If all else fails, load default placeholder image
                if (icon == null) {
                    try {
                        URL defaultUrl = getClass().getResource("/images/default_product.png"); // Ensure this exists
                        if (defaultUrl != null) {
                            icon = new ImageIcon(defaultUrl);
                            if (!isIconValid(icon)) icon = new ImageIcon(); // Use empty icon if default is bad
                        } else {
                             System.err.println("ERROR: Default product image '/images/default_product.png' not found!");
                             icon = new ImageIcon(); // Empty icon as last resort
                        }
                    } catch (Exception e) {
                         System.err.println("Error loading default product image: " + e.getMessage());
                         icon = new ImageIcon(); // Empty icon
                    }
                    loadedFromPath = "[Default Image]"; // Indicate default was used
                }

                 // Ensure icon is not null before adding
                 if (icon == null) icon = new ImageIcon();

                 // Add row using the custom model method
                 productTableModel.addRowWithImage(
                    new Object[]{id, icon, name, price, stock, group, imagePathFromDB != null ? imagePathFromDB : ""}, // Data for table display
                    imagePathFromDB, // Original DB path for reference/editing
                    icon // The loaded ImageIcon
                );
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showThemedJOptionPane("Error loading products: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) { // Catch other potential errors (like file loading)
             e.printStackTrace();
             showThemedJOptionPane("An unexpected error occurred while loading products: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Re-apply styles again after loading data to ensure renderers are set correctly
        styleTable(productTable);
        productTable.getColumnModel().getColumn(1).setCellRenderer(new ImageRenderer());
        productTable.setRowHeight(80);
        productTable.revalidate();
        productTable.repaint();
    }
    private void loadInventory() {
         // Query based on 'products' table schema, focusing on inventory columns
         try (Connection conn = DBConnection.connect();
              Statement stmt = conn.createStatement();
              ResultSet rs = stmt.executeQuery("SELECT p.id, p.name, p.stock, COALESCE(p.reorder_level, 0) as reorder_level FROM products p")) {
             inventoryTableModel.setRowCount(0);
             while (rs.next()) {
                 inventoryTableModel.addRow(new Object[]{
                     rs.getInt("id"),
                     rs.getString("name"),
                     rs.getInt("stock"),
                     rs.getInt("reorder_level") // Using COALESCE to handle potential NULLs
                 });
             }
         } catch (SQLException ex) {
             ex.printStackTrace();
             showThemedJOptionPane("Error loading inventory: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
         }
    }
    private void loadSales() {
        loadSales("All"); // Load all by default
    }
    private void loadSales(String statusFilter) {
         // Query joining orders, order_items, products, customers
         try (Connection conn = DBConnection.connect()) {
             String sql = "SELECT o.id, c.name AS customer_name, p.name AS product_name, oi.quantity, oi.price, (oi.price * oi.quantity) AS total_item_price, o.order_date, o.status " +
                          "FROM orders o " +
                          "JOIN order_items oi ON o.id = oi.order_id " +
                          "JOIN products p ON oi.product_id = p.id " +
                          "JOIN customers c ON o.customer_id = c.id";

             if (!statusFilter.equals("All")) {
                 sql += " WHERE o.status = ?";
             }
             // Added Delivered and Completed as valid revenue statuses for filtering, adjust if needed
             else if (statusFilter.equals("Completed")) {
                  sql += " WHERE o.status IN ('Delivered', 'Completed')";
             }

             sql += " ORDER BY o.order_date DESC";

             try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                 if (!statusFilter.equals("All") && !statusFilter.equals("Completed")) {
                     stmt.setString(1, statusFilter);
                 }

                 ResultSet rs = stmt.executeQuery();
                 salesTableModel.setRowCount(0);
                 while (rs.next()) {
                     salesTableModel.addRow(new Object[]{
                         rs.getInt("id"), // Order ID
                         rs.getString("customer_name"), // Customer Name
                         rs.getString("product_name"), // Product Name
                         rs.getInt("quantity"), // Quantity
                         rs.getDouble("price"), // Item Price (Unit Price)
                         rs.getDouble("total_item_price"), // Total Price (Item * Quantity)
                         rs.getTimestamp("order_date"), // Date
                         rs.getString("status") // Status
                     });
                 }
             }
         } catch (SQLException ex) {
             ex.printStackTrace();
             showThemedJOptionPane("Error loading sales reports: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
         }
    }
    private void loadOrders() {
         // Query based on 'orders' and 'customers' tables
         try (Connection conn = DBConnection.connect();
              Statement stmt = conn.createStatement();
              ResultSet rs = stmt.executeQuery(
                  "SELECT o.id, c.name, o.total_price, o.order_date, o.status, " +
                  "CASE WHEN o.cancellation_requested = 1 THEN 'Yes' ELSE 'No' END as cancellation_request " + // Use CASE for boolean display
                  "FROM orders o JOIN customers c ON o.customer_id = c.id " +
                  "ORDER BY o.order_date DESC")) {
             ordersTableModel.setRowCount(0);
             while (rs.next()) {
                 ordersTableModel.addRow(new Object[]{
                     rs.getInt("id"),
                     rs.getString("name"),
                     String.format("₱%.2f", rs.getDouble("total_price")), // Format total price as currency string
                     rs.getTimestamp("order_date"),
                     rs.getString("status"),
                     rs.getString("cancellation_request") // Use the result of the CASE statement
                 });
             }
         } catch (SQLException ex) {
             ex.printStackTrace();
             showThemedJOptionPane("Error loading orders: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
         }
    }


    // --- Dashboard Metric Calculation Methods (Using correct column names from schema) ---
    private String getTotalProducts() {
         try (Connection conn = DBConnection.connect();
              Statement stmt = conn.createStatement();
              ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products")) {
             if (rs.next()) return rs.getString(1);
         } catch (SQLException ex) { ex.printStackTrace(); }
         return "0";
    }
    private String getTotalOrders() {
         try (Connection conn = DBConnection.connect();
              Statement stmt = conn.createStatement();
              ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM orders")) {
             if (rs.next()) return rs.getString(1);
         } catch (SQLException ex) { ex.printStackTrace(); }
         return "0";
    }
    private String getLowStockItems() {
         // Using 'reorder_level' column from products table
         try (Connection conn = DBConnection.connect();
              Statement stmt = conn.createStatement();
              ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products WHERE stock < COALESCE(reorder_level, 0)")) {
             if (rs.next()) return rs.getString(1);
         } catch (SQLException ex) { ex.printStackTrace(); }
         return "0";
    }
    private String getTotalCustomers() {
        // Using 'role' column from customers table
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM customers WHERE role = 'customer'")) {
            if (rs.next()) return rs.getString(1);
        } catch (SQLException ex) { ex.printStackTrace(); }
        return "0";
    }
    private String getTotalRevenue() {
         // Using 'status' column from orders table
         try (Connection conn = DBConnection.connect();
              PreparedStatement stmt = conn.prepareStatement(
                  // Consider both 'Delivered' and 'Completed' as revenue-generating statuses based on schema
                  "SELECT SUM(total_price) FROM orders WHERE status IN ('Delivered', 'Completed')")) {
             ResultSet rs = stmt.executeQuery();
             if (rs.next()) {
                 double total = rs.getDouble(1);
                 return "₱" + String.format("%,.2f", total); // Format as currency
             }
         } catch (SQLException ex) { ex.printStackTrace(); }
         return "₱0.00";
    }
    private String getPendingOrders() {
         // Using 'status' column from orders table
         try (Connection conn = DBConnection.connect();
              Statement stmt = conn.createStatement();
              // 'Processing' and 'Shipped' are likely pending statuses
              ResultSet rs = stmt.executeQuery(
                  "SELECT COUNT(*) FROM orders WHERE status IN ('Processing', 'Shipped')")) {
             if (rs.next()) return rs.getString(1);
         } catch (SQLException ex) { ex.printStackTrace(); }
         return "0";
    }


    // --- Action Handlers (Use imported ThemeColors in dialogs/components) ---
    private void addProduct() {
        JDialog dialog = new JDialog(this, "Add New Product", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(ThemeColors.BACKGROUND); // Use imported theme color
        dialog.setSize(550, 700); // Adjusted size for new fields
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(ThemeColors.BACKGROUND); // Use imported theme color

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(ThemeColors.BACKGROUND); // Use imported theme color
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField nameField = new JTextField(20);
        JTextField priceField = new JTextField(10);
        JTextField stockField = new JTextField(5);
        JTextField reorderLevelField = new JTextField(5);
        JTextField groupNameField = new JTextField(15); // Added group_name
        JTextField colorField = new JTextField(15);     // Added color
        JTextField sizeField = new JTextField(15);      // Added size
        JTextArea descriptionArea = new JTextArea(4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);

        JLabel imagePathLabel = new JLabel("No image selected");
        imagePathLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        imagePathLabel.setForeground(ThemeColors.TEXT); // Use imported theme color
        JButton browseButton = new JButton("Browse...");
        JLabel imagePreviewLabel = new JLabel();
        imagePreviewLabel.setPreferredSize(new Dimension(150, 150));
        imagePreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imagePreviewLabel.setBorder(BorderFactory.createLineBorder(ThemeColors.SECONDARY)); // Use imported theme color
        imagePreviewLabel.setBackground(ThemeColors.CARD_BG); // Use imported theme color
        imagePreviewLabel.setOpaque(true);

        final File[] selectedImageFile = {null};

        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Product Image");
            fileChooser.setAcceptAllFileFilterUsed(false);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif");
            fileChooser.addChoosableFileFilter(filter);

            if (fileChooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                selectedImageFile[0] = fileChooser.getSelectedFile();
                imagePathLabel.setText(selectedImageFile[0].getName());
                try {
                    ImageIcon icon = new ImageIcon(selectedImageFile[0].toURI().toURL());
                    Image image = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                    imagePreviewLabel.setIcon(new ImageIcon(image));
                    imagePreviewLabel.setText(null);
                } catch (Exception ex) {
                    imagePreviewLabel.setIcon(null);
                    imagePreviewLabel.setText("Preview Error");
                    showThemedJOptionPane("Error loading image preview.", "Preview Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Layout using GridBagLayout with createFormLabel (uses imported ThemeColors)
        int y = 0;
        gbc.gridx = 0; gbc.gridy = y++; fieldsPanel.add(createFormLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; fieldsPanel.add(nameField, gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;

        gbc.gridx = 0; gbc.gridy = y++; fieldsPanel.add(createFormLabel("Price (₱):"), gbc);
        gbc.gridx = 1; fieldsPanel.add(priceField, gbc);

        gbc.gridx = 0; gbc.gridy = y++; fieldsPanel.add(createFormLabel("Stock:"), gbc);
        gbc.gridx = 1; fieldsPanel.add(stockField, gbc);

        gbc.gridx = 0; gbc.gridy = y++; fieldsPanel.add(createFormLabel("Reorder Lvl:"), gbc);
        gbc.gridx = 1; fieldsPanel.add(reorderLevelField, gbc);

        gbc.gridx = 0; gbc.gridy = y++; fieldsPanel.add(createFormLabel("Group Name:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; fieldsPanel.add(groupNameField, gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;

        gbc.gridx = 0; gbc.gridy = y++; fieldsPanel.add(createFormLabel("Color(s):"), gbc); // Added Color field
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; fieldsPanel.add(colorField, gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;

        gbc.gridx = 0; gbc.gridy = y++; fieldsPanel.add(createFormLabel("Size(s):"), gbc); // Added Size field
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; fieldsPanel.add(sizeField, gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;

        gbc.gridx = 0; gbc.gridy = y++; gbc.anchor = GridBagConstraints.NORTHWEST; fieldsPanel.add(createFormLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0; fieldsPanel.add(descriptionScroll, gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weighty = 0.0; gbc.anchor = GridBagConstraints.WEST; gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = y++; fieldsPanel.add(createFormLabel("Image:"), gbc);
        gbc.gridx = 1; fieldsPanel.add(browseButton, gbc);
        gbc.gridx = 2; fieldsPanel.add(imagePathLabel, gbc);

        JPanel previewContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        previewContainer.setBackground(ThemeColors.BACKGROUND); // Use imported theme color
        previewContainer.add(imagePreviewLabel);
        previewContainer.setBorder(BorderFactory.createTitledBorder(
             BorderFactory.createEmptyBorder(10, 0, 10, 0),
             "Image Preview",
             TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION,
             new Font("Arial", Font.PLAIN, 12), ThemeColors.TEXT // Use imported theme color
        ));


        formPanel.add(fieldsPanel);
        formPanel.add(previewContainer);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(ThemeColors.BACKGROUND); // Use imported theme color
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton saveButton = new JButton("Save Product");
        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String priceStr = priceField.getText().trim();
                String stockStr = stockField.getText().trim();
                String reorderStr = reorderLevelField.getText().trim();
                String groupName = groupNameField.getText().trim();
                String color = colorField.getText().trim(); // Get color
                String size = sizeField.getText().trim();   // Get size
                String description = descriptionArea.getText().trim();

                if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty() || groupName.isEmpty()) {
                    showThemedJOptionPane("Name, Price, Stock, and Group Name are required.", "Input Error", JOptionPane.ERROR_MESSAGE); return;
                }

                double price = Double.parseDouble(priceStr);
                int stock = Integer.parseInt(stockStr);
                int reorderLevel = reorderStr.isEmpty() ? 0 : Integer.parseInt(reorderStr);

                String finalImagePath = null;
                if (selectedImageFile[0] != null) {
                    File sourceFile = selectedImageFile[0];
                    // Define target directory (e.g., project_root/images/products/)
                    Path targetDir = Paths.get("images", "products");
                     try {
                        Files.createDirectories(targetDir); // Create directory if it doesn't exist

                        String originalFilename = sourceFile.getName();
                        String extension = "";
                        int dotIndex = originalFilename.lastIndexOf('.');
                        if (dotIndex > 0 && dotIndex < originalFilename.length() - 1) {
                            extension = originalFilename.substring(dotIndex); // Get file extension
                        }

                        // Sanitize name and create a unique filename
                        String safeName = name.replaceAll("[^a-zA-Z0-9.\\-_ ]", "_");
                        String newFileName = "product_" + safeName + "_" + System.currentTimeMillis() + extension;
                        Path targetPath = targetDir.resolve(newFileName);

                        // Copy the file
                        Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                        // Store the relative path using forward slashes
                        finalImagePath = targetDir.toString().replace("\\", "/") + "/" + newFileName;
                        System.out.println("Image saved to: " + finalImagePath);

                     } catch (IOException ioEx) {
                         System.err.println("Error copying image file: " + ioEx.getMessage());
                         ioEx.printStackTrace();
                         showThemedJOptionPane("Could not save the image file: " + ioEx.getMessage(),"File Copy Error", JOptionPane.ERROR_MESSAGE);
                         return; // Stop if image copy fails
                     }
                }

                // Pass color and size to save method
                if (saveNewProduct(name, price, stock, groupName, description, finalImagePath, reorderLevel, color, size)) {
                    loadProducts();
                    dialog.dispose();
                    showThemedJOptionPane("Product added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                     showThemedJOptionPane("Failed to save product to database.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                showThemedJOptionPane("Please enter valid numbers for Price, Stock, and Reorder Level.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                 ex.printStackTrace();
                 showThemedJOptionPane("An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        dialog.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    private boolean saveNewProduct(String name, double price, int stock,
                                   String groupName, String description, String imagePath,
                                   int reorderLevel, String color, String size) { // Added color and size parameters
          // Updated SQL to include color and size columns
          String sql = "INSERT INTO products (name, price, stock, group_name, description, image_path, reorder_level, color, size) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
          try (Connection conn = DBConnection.connect();
               PreparedStatement stmt = conn.prepareStatement(sql)) {
              stmt.setString(1, name);
              stmt.setDouble(2, price);
              stmt.setInt(3, stock);
              stmt.setString(4, groupName);
              stmt.setString(5, description);
              if (imagePath != null && !imagePath.isEmpty()) stmt.setString(6, imagePath);
              else stmt.setNull(6, Types.VARCHAR);
              stmt.setInt(7, reorderLevel);
              // Set color and size, handle potential nulls/emptiness
              if (color != null && !color.isEmpty()) stmt.setString(8, color); else stmt.setNull(8, Types.VARCHAR);
              if (size != null && !size.isEmpty()) stmt.setString(9, size); else stmt.setNull(9, Types.VARCHAR);

              return stmt.executeUpdate() > 0;
          } catch (SQLException ex) {
               ex.printStackTrace();
               showThemedJOptionPane("Error adding product to database: " + ex.getMessage(),"Database Error", JOptionPane.ERROR_MESSAGE);
               return false;
          }
      }
    private void editProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) { showThemedJOptionPane("Please select a product to edit.", "Selection Error", JOptionPane.WARNING_MESSAGE); return; }

        final int productId = (int) productTableModel.getValueAt(selectedRow, 0);
        ImageIcon currentIcon = (ImageIcon) productTableModel.getValueAt(selectedRow, 1);
        String currentName = (String) productTableModel.getValueAt(selectedRow, 2);
        double currentPrice = (double) productTableModel.getValueAt(selectedRow, 3);
        int currentStock = (int) productTableModel.getValueAt(selectedRow, 4);
        String currentGroup = (String) productTableModel.getValueAt(selectedRow, 5);
        String currentImagePath = productTableModel.getImagePath(selectedRow); // Get the actual path

        // Fetch additional details (description, reorder_level, color, size) from DB
        String currentDescription = "";
        int currentReorderLevel = 0;
        String currentColor = "";
        String currentSize = "";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT description, reorder_level, color, size FROM products WHERE id = ?")) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                currentDescription = rs.getString("description");
                currentReorderLevel = rs.getInt("reorder_level");
                currentColor = rs.getString("color") != null ? rs.getString("color") : ""; // Handle null
                currentSize = rs.getString("size") != null ? rs.getString("size") : "";     // Handle null
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showThemedJOptionPane("Error fetching product details: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return; // Don't proceed if details can't be fetched
        }

        JDialog dialog = new JDialog(this, "Edit Product (ID: " + productId + ")", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(ThemeColors.BACKGROUND); // Use imported theme color
        dialog.setSize(550, 700); // Increased height for new fields
        dialog.setLocationRelativeTo(this);

        // --- Form structure similar to addProduct, using imported ThemeColors ---
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(ThemeColors.BACKGROUND); // Use imported theme color

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(ThemeColors.BACKGROUND); // Use imported theme color
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField nameField = new JTextField(currentName, 20);
        JTextField priceField = new JTextField(String.valueOf(currentPrice), 10);
        JTextField stockField = new JTextField(String.valueOf(currentStock), 5);
        JTextField reorderLevelField = new JTextField(String.valueOf(currentReorderLevel), 5);
        JTextField groupNameField = new JTextField(currentGroup, 15);
        JTextField colorField = new JTextField(currentColor, 15); // Added color field
        JTextField sizeField = new JTextField(currentSize, 15);   // Added size field
        JTextArea descriptionArea = new JTextArea(currentDescription, 4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);

        String currentImageFilename = "No image selected";
        if (currentImagePath != null && !currentImagePath.isEmpty()) {
            try { currentImageFilename = new File(currentImagePath).getName(); } catch (Exception e) {}
        }
        JLabel imagePathLabel = new JLabel(currentImageFilename);
        imagePathLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        imagePathLabel.setForeground(ThemeColors.TEXT); // Use imported theme color
        JButton browseButton = new JButton("Change Image...");
        JLabel imagePreviewLabel = new JLabel();
        imagePreviewLabel.setPreferredSize(new Dimension(150, 150));
        imagePreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imagePreviewLabel.setBorder(BorderFactory.createLineBorder(ThemeColors.SECONDARY)); // Use imported theme color
        imagePreviewLabel.setBackground(ThemeColors.CARD_BG); // Use imported theme color
        imagePreviewLabel.setOpaque(true);

        if (currentIcon != null && isIconValid(currentIcon)) {
            Image image = currentIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            imagePreviewLabel.setIcon(new ImageIcon(image));
        } else { imagePreviewLabel.setText("No Image"); }

        final File[] selectedImageFile = {null}; // Array to hold newly selected file
        final String[] originalImagePath = {currentImagePath}; // Store original path for potential deletion

        browseButton.addActionListener(e -> {
             JFileChooser fileChooser = new JFileChooser();
             fileChooser.setDialogTitle("Select New Product Image");
             fileChooser.setAcceptAllFileFilterUsed(false);
             FileNameExtensionFilter filter = new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif");
             fileChooser.addChoosableFileFilter(filter);
             if (fileChooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                 selectedImageFile[0] = fileChooser.getSelectedFile();
                 imagePathLabel.setText(selectedImageFile[0].getName());
                 try {
                    ImageIcon icon = new ImageIcon(selectedImageFile[0].toURI().toURL());
                    Image image = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                    imagePreviewLabel.setIcon(new ImageIcon(image));
                    imagePreviewLabel.setText(null);
                 } catch (Exception ex) {
                     imagePreviewLabel.setIcon(null);
                     imagePreviewLabel.setText("Preview Error");
                 }
             }
        });

        // --- Layout fields using GridBagLayout and createFormLabel (uses imported ThemeColors) ---
         int y = 0;
         gbc.gridx = 0; gbc.gridy = y++; fieldsPanel.add(createFormLabel("Name:"), gbc);
         gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; fieldsPanel.add(nameField, gbc);
         gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;

         gbc.gridx = 0; gbc.gridy = y++; fieldsPanel.add(createFormLabel("Price (₱):"), gbc);
         gbc.gridx = 1; fieldsPanel.add(priceField, gbc);

         gbc.gridx = 0; gbc.gridy = y++; fieldsPanel.add(createFormLabel("Stock:"), gbc);
         gbc.gridx = 1; fieldsPanel.add(stockField, gbc);

         gbc.gridx = 0; gbc.gridy = y++; fieldsPanel.add(createFormLabel("Reorder Lvl:"), gbc);
         gbc.gridx = 1; fieldsPanel.add(reorderLevelField, gbc);

         gbc.gridx = 0; gbc.gridy = y++; fieldsPanel.add(createFormLabel("Group Name:"), gbc);
         gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; fieldsPanel.add(groupNameField, gbc);
         gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;

         gbc.gridx = 0; gbc.gridy = y++; fieldsPanel.add(createFormLabel("Color(s):"), gbc); // Added Color
         gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; fieldsPanel.add(colorField, gbc);
         gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;

         gbc.gridx = 0; gbc.gridy = y++; fieldsPanel.add(createFormLabel("Size(s):"), gbc); // Added Size
         gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; fieldsPanel.add(sizeField, gbc);
         gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;

         gbc.gridx = 0; gbc.gridy = y++; gbc.anchor = GridBagConstraints.NORTHWEST; fieldsPanel.add(createFormLabel("Description:"), gbc);
         gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0; fieldsPanel.add(descriptionScroll, gbc);
         gbc.fill = GridBagConstraints.NONE; gbc.weighty = 0.0; gbc.anchor = GridBagConstraints.WEST; gbc.gridwidth = 1;

         gbc.gridx = 0; gbc.gridy = y++; fieldsPanel.add(createFormLabel("Image:"), gbc);
         gbc.gridx = 1; fieldsPanel.add(browseButton, gbc);
         gbc.gridx = 2; fieldsPanel.add(imagePathLabel, gbc);

        JPanel previewContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        previewContainer.setBackground(ThemeColors.BACKGROUND); // Use imported theme color
        previewContainer.add(imagePreviewLabel);
        previewContainer.setBorder(BorderFactory.createTitledBorder(
              BorderFactory.createEmptyBorder(10, 0, 10, 0),
              "Image Preview", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION,
              new Font("Arial", Font.PLAIN, 12), ThemeColors.TEXT // Use imported theme color
        ));

        formPanel.add(fieldsPanel);
        formPanel.add(previewContainer);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(ThemeColors.BACKGROUND); // Use imported theme color
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton saveButton = new JButton("Save Changes");
        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String priceStr = priceField.getText().trim();
                String stockStr = stockField.getText().trim();
                String reorderStr = reorderLevelField.getText().trim();
                String groupName = groupNameField.getText().trim();
                String color = colorField.getText().trim(); // Get updated color
                String size = sizeField.getText().trim();   // Get updated size
                String description = descriptionArea.getText().trim();

                if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty() || groupName.isEmpty()) {
                    showThemedJOptionPane("Name, Price, Stock, and Group Name are required.", "Input Error", JOptionPane.ERROR_MESSAGE); return;
                }

                double price = Double.parseDouble(priceStr);
                int stock = Integer.parseInt(stockStr);
                int reorderLevel = reorderStr.isEmpty() ? 0 : Integer.parseInt(reorderStr);

                String finalImagePath = originalImagePath[0]; // Start with the original path
                String oldImagePathToDelete = null;

                // If a new image was selected, process it
                if (selectedImageFile[0] != null) {
                    File sourceFile = selectedImageFile[0];
                    Path targetDir = Paths.get("images", "products");
                     try {
                        Files.createDirectories(targetDir); // Ensure directory exists

                        String originalFilename = sourceFile.getName();
                        String extension = "";
                        int dotIndex = originalFilename.lastIndexOf('.');
                        if (dotIndex > 0 && dotIndex < originalFilename.length() - 1) {
                            extension = originalFilename.substring(dotIndex);
                        }

                        String safeName = name.replaceAll("[^a-zA-Z0-9.\\-_ ]", "_");
                        String newFileName = "product_" + safeName + "_" + System.currentTimeMillis() + extension;
                        Path targetPath = targetDir.resolve(newFileName);

                        // Copy the new file
                        Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                        finalImagePath = targetDir.toString().replace("\\", "/") + "/" + newFileName; // Update path

                        // Mark old image for deletion if it's different
                        if (originalImagePath[0] != null && !originalImagePath[0].isEmpty() && !originalImagePath[0].equals(finalImagePath)) {
                            oldImagePathToDelete = originalImagePath[0];
                        }

                     } catch (IOException ioEx) {
                         System.err.println("Error copying updated image file: " + ioEx.getMessage());
                         ioEx.printStackTrace();
                         showThemedJOptionPane("Could not save the updated image file: " + ioEx.getMessage(),"File Copy Error", JOptionPane.ERROR_MESSAGE);
                         return; // Don't proceed with DB update if image copy failed
                     }
                }

                // Call update method with all fields, including color and size
                if (updateProduct(productId, name, price, stock, groupName, description, finalImagePath, reorderLevel, color, size)) {
                    // Delete old image *after* successful DB update
                     if (oldImagePathToDelete != null) {
                        try {
                             Path oldPath = Paths.get(oldImagePathToDelete);
                              if (Files.exists(oldPath) && !Files.isDirectory(oldPath)) {
                                 if (Files.deleteIfExists(oldPath)) {
                                     System.out.println("Deleted old image file: " + oldImagePathToDelete);
                                 } else {
                                     System.err.println("Could not delete old image file (in use?): " + oldImagePathToDelete);
                                 }
                              }
                        } catch (IOException ioEx) {
                             System.err.println("Error deleting old image file '" + oldImagePathToDelete + "': " + ioEx.getMessage());
                             // Don't stop the user flow for this, just log it
                        } catch (Exception ex) {
                            System.err.println("Unexpected error deleting old image file: " + ex.getMessage());
                        }
                     }

                    loadProducts(); // Reload the table to reflect changes
                    dialog.dispose();
                    showThemedJOptionPane("Product updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                     showThemedJOptionPane("Failed to update product in database.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                showThemedJOptionPane("Please enter valid numbers for Price, Stock, and Reorder Level.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                 ex.printStackTrace();
                 showThemedJOptionPane("An unexpected error occurred during update: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        dialog.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    private boolean updateProduct(int productId, String name, double price, int stock,
                                  String groupName, String description, String imagePath,
                                  int reorderLevel, String color, String size) { // Added color and size
        // Updated SQL to include color and size columns
        String sql = "UPDATE products SET name = ?, price = ?, stock = ?, group_name = ?, " +
                     "description = ?, image_path = ?, reorder_level = ?, color = ?, size = ? WHERE id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setDouble(2, price);
            stmt.setInt(3, stock);
            stmt.setString(4, groupName);
            stmt.setString(5, description);
            if (imagePath != null && !imagePath.isEmpty()) stmt.setString(6, imagePath); else stmt.setNull(6, Types.VARCHAR);
            stmt.setInt(7, reorderLevel);
            if (color != null && !color.isEmpty()) stmt.setString(8, color); else stmt.setNull(8, Types.VARCHAR); // Set color
            if (size != null && !size.isEmpty()) stmt.setString(9, size); else stmt.setNull(9, Types.VARCHAR);     // Set size
            stmt.setInt(10, productId); // WHERE clause parameter

            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
             ex.printStackTrace();
             showThemedJOptionPane("Error updating product in database: " + ex.getMessage(),"Database Error", JOptionPane.ERROR_MESSAGE);
             return false;
        }
    }
    private void deleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) { showThemedJOptionPane("Please select a product to delete.", "Selection Error", JOptionPane.WARNING_MESSAGE); return; }

        final int productId = (int) productTableModel.getValueAt(selectedRow, 0);
        String productName = (String) productTableModel.getValueAt(selectedRow, 2);
        String imagePath = productTableModel.getImagePath(selectedRow);
        final int rowToDelete = selectedRow; // Store the original view row index

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete '" + productName + "' (ID: " + productId + ")?\n" +
            "This action cannot be undone and might affect related order items (check constraints).",
            "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.connect();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM products WHERE id = ?")) {
                stmt.setInt(1, productId);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                     // Image deletion logic
                     if (imagePath != null && !imagePath.isEmpty()) {
                         try {
                             Path pathToDelete = Paths.get(imagePath);
                             // Check if it exists and is not a directory
                             if (Files.exists(pathToDelete) && !Files.isDirectory(pathToDelete)) {
                                 if (Files.deleteIfExists(pathToDelete)) {
                                     System.out.println("Deleted image file: " + imagePath);
                                 } else {
                                     System.err.println("Failed to delete image file (check permissions/usage): " + imagePath);
                                 }
                             } else {
                                 System.out.println("Image file not found or is a directory, skipping deletion: " + imagePath);
                             }
                         } catch (SecurityException se) {
                             System.err.println("Security error deleting image file " + imagePath + ": " + se.getMessage());
                             showThemedJOptionPane("Could not delete image file due to security restrictions:\n" + imagePath,"File Deletion Error", JOptionPane.ERROR_MESSAGE);
                         } catch (IOException ioEx) {
                            System.err.println("I/O Error attempting to delete image file " + imagePath + ": " + ioEx.getMessage());
                         } catch (Exception e) {
                             System.err.println("Error attempting to delete image file " + imagePath + ": " + e.getMessage());
                         }
                     }

                     // Remove row from model - Use invokeLater to ensure it runs on EDT after DB operation
                     SwingUtilities.invokeLater(() -> {
                           // Re-check the row validity and model state before removal
                           if (productTableModel != null && rowToDelete >= 0 && rowToDelete < productTableModel.getRowCount()) {
                                try {
                                    // Verify if the product at the stored index is still the one we intended to delete
                                    if ((int)productTableModel.getValueAt(rowToDelete, 0) == productId) {
                                        productTableModel.removeRow(rowToDelete);
                                    } else {
                                        // If the row content changed (e.g., due to sorting or concurrent modification), reload instead of removing wrong row
                                        System.err.println("Product ID mismatch at row " + rowToDelete + " during deletion UI update. Reloading table.");
                                        loadProducts();
                                    }
                                } catch (ArrayIndexOutOfBoundsException aioobe) {
                                     // If index became invalid after DB operation but before UI update, reload
                                     System.err.println("Row index " + rowToDelete + " out of bounds during deletion UI update. Reloading table.");
                                     loadProducts();
                                } catch (Exception ex) {
                                     // Catch any other unexpected errors during UI update
                                     System.err.println("Error removing row from table model: " + ex.getMessage());
                                     loadProducts(); // Reload as a fallback
                                }
                           } else {
                               // If row index was already invalid or model became null, just reload
                               System.err.println("Invalid row index " + rowToDelete + " or null model during deletion UI update. Reloading table.");
                               if(productTableModel != null) loadProducts(); // Reload only if model is not null
                           }
                     });
                    showThemedJOptionPane("Product '" + productName + "' deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                     showThemedJOptionPane("Product could not be deleted (maybe it was already removed?).", "Deletion Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                // Provide more specific feedback for foreign key constraint violations
                if (ex.getMessage().toLowerCase().contains("foreign key constraint")) {
                     showThemedJOptionPane("Error deleting product: Cannot delete '" + productName + "' because it is referenced in existing orders or other records.\nConsider marking the product as inactive instead.","Deletion Error (Constraint Violation)", JOptionPane.ERROR_MESSAGE);
                } else {
                    showThemedJOptionPane("Error deleting product from database: " + ex.getMessage(),"Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    private void updateStock() {
         int selectedRow = inventoryTable.getSelectedRow();
         if (selectedRow < 0) { showThemedJOptionPane("Please select a product from the Inventory table.", "Selection Error", JOptionPane.WARNING_MESSAGE); return; }

         // Get data directly from the table model to ensure consistency
         final int productId = (int) inventoryTableModel.getValueAt(selectedRow, 0);
         String productName = (String) inventoryTableModel.getValueAt(selectedRow, 1);
         int currentStock = (int) inventoryTableModel.getValueAt(selectedRow, 2);
         int currentReorder = (int) inventoryTableModel.getValueAt(selectedRow, 3);
         final int rowToUpdate = selectedRow; // Store the view row index

         JDialog dialog = new JDialog(this, "Update Stock & Reorder Level", true);
         dialog.setLayout(new BorderLayout());
         dialog.getContentPane().setBackground(ThemeColors.BACKGROUND); // Use imported theme color
         dialog.setSize(400, 250);
         dialog.setLocationRelativeTo(this);

         JPanel panel = new JPanel(new GridBagLayout());
         panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
         panel.setBackground(ThemeColors.BACKGROUND); // Use imported theme color
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.insets = new Insets(5, 5, 5, 5);
         gbc.anchor = GridBagConstraints.WEST;

         gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
         JLabel nameLabel = new JLabel("Product: " + productName + " (ID: " + productId + ")");
         nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
         nameLabel.setForeground(ThemeColors.TEXT); // Use imported theme color
         panel.add(nameLabel, gbc);
         gbc.gridwidth = 1;

         gbc.gridx = 0; gbc.gridy++;
         panel.add(createFormLabel("Current Stock:"), gbc); // Uses imported ThemeColors
         gbc.gridx = 1;
         JSpinner stockSpinner = new JSpinner(new SpinnerNumberModel(currentStock, 0, 10000, 1)); // Set min to 0
         panel.add(stockSpinner, gbc);

         gbc.gridx = 0; gbc.gridy++;
         panel.add(createFormLabel("Reorder Level:"), gbc); // Uses imported ThemeColors
         gbc.gridx = 1;
         JSpinner reorderSpinner = new JSpinner(new SpinnerNumberModel(currentReorder, 0, 10000, 1)); // Set min to 0
         panel.add(reorderSpinner, gbc);

         JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
         buttonPanel.setBackground(ThemeColors.BACKGROUND); // Use imported theme color

         JButton updateButton = new JButton("Update");
         updateButton.addActionListener(e -> {
             int newStock = (int) stockSpinner.getValue();
             int newReorder = (int) reorderSpinner.getValue();
             if (updateProductStockAndReorder(productId, newStock, newReorder)) {
                 // Update inventory table model immediately
                 // Ensure row index is still valid before updating
                 if (rowToUpdate >= 0 && rowToUpdate < inventoryTableModel.getRowCount() && (int)inventoryTableModel.getValueAt(rowToUpdate, 0) == productId) {
                     inventoryTableModel.setValueAt(newStock, rowToUpdate, 2);
                     inventoryTableModel.setValueAt(newReorder, rowToUpdate, 3);
                 } else {
                     loadInventory(); // Reload if row became invalid
                 }

                 // Update products table model if it's loaded and visible
                 if (productTable != null && productTableModel != null && productsPanel != null && productsPanel.isShowing()) { // Check if productsPanel is not null
                    for (int i = 0; i < productTableModel.getRowCount(); i++) {
                        // Check bounds before accessing value
                        if (i < productTableModel.getRowCount() && (int) productTableModel.getValueAt(i, 0) == productId) {
                            productTableModel.setValueAt(newStock, i, 4); // Update stock in product table (column 4)
                            break;
                        }
                    }
                 }
                 dialog.dispose();
                 showThemedJOptionPane("Stock and Reorder Level updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
             } else {
                  showThemedJOptionPane("Failed to update stock/reorder level in database.", "Database Error", JOptionPane.ERROR_MESSAGE);
             }
         });

         JButton cancelButton = new JButton("Cancel");
         cancelButton.addActionListener(e -> dialog.dispose());

         buttonPanel.add(cancelButton);
         buttonPanel.add(updateButton);

         dialog.add(panel, BorderLayout.CENTER);
         dialog.add(buttonPanel, BorderLayout.SOUTH);
         dialog.setVisible(true);
     }
    private boolean updateProductStockAndReorder(int productId, int newStock, int newReorderLevel) {
        // SQL uses reorder_level column based on schema
        String sql = "UPDATE products SET stock = ?, reorder_level = ? WHERE id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newStock);
            stmt.setInt(2, newReorderLevel);
            stmt.setInt(3, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            showThemedJOptionPane("Error updating stock/reorder level: " + ex.getMessage(),"Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    private void generateRestockList() {
         // Query uses reorder_level column
         try (Connection conn = DBConnection.connect();
              Statement stmt = conn.createStatement();
              ResultSet rs = stmt.executeQuery(
                  "SELECT name, stock, COALESCE(reorder_level, 0) as reorder_level FROM products WHERE stock < COALESCE(reorder_level, 0) ORDER BY (COALESCE(reorder_level, 0) - stock) DESC")) { // Order by urgency

             StringBuilder restockList = new StringBuilder("--- Products Needing Restock ---\n\n");
             restockList.append(String.format("%-40s | %-10s | %-10s | %-10s\n", "Product Name", "Current", "Reorder At", "Needed"));
             restockList.append(String.format("%-40s-+-%-10s-+-%-10s-+-%-10s\n", "----------------------------------------", "----------", "----------", "----------"));
             int count = 0;
             while (rs.next()) {
                 count++;
                 int currentStock = rs.getInt("stock");
                 int reorderLevel = rs.getInt("reorder_level");
                 int needed = Math.max(0, reorderLevel - currentStock); // Calculate amount needed to reach reorder level

                 restockList.append(String.format("%-40s | %-10d | %-10d | %-10d\n",
                                                  rs.getString("name"), currentStock, reorderLevel, needed));
             }
             if (count == 0) restockList.append("\nAll products are currently above their reorder levels.");

             JTextArea textArea = new JTextArea(restockList.toString());
             textArea.setEditable(false);
             textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
             textArea.setBackground(ThemeColors.BACKGROUND); // Use imported theme color
             textArea.setForeground(ThemeColors.TEXT); // Use imported theme color
             textArea.setRows(20); // Increase rows for better visibility
             textArea.setColumns(80); // Increase columns

             JScrollPane scrollPane = new JScrollPane(textArea);
             scrollPane.setPreferredSize(new Dimension(700, 450)); // Adjust size

             // --- FIX: Use standard JOptionPane.showMessageDialog ---
             // The global UIManager settings will apply the theme
             JOptionPane.showMessageDialog(this, scrollPane, "Restock List (" + count + " items)", JOptionPane.INFORMATION_MESSAGE);
             // --- END OF FIX ---

         } catch (SQLException ex) {
             ex.printStackTrace();
             // Use the instance helper method showThemedJOptionPane for simple String messages
             showThemedJOptionPane("Error generating restock list: " + ex.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);
         }
    }
    private void editUser() {
         int selectedRow = userTable.getSelectedRow();
         if (selectedRow < 0) { showThemedJOptionPane("Please select a user to edit.", "Selection Error", JOptionPane.WARNING_MESSAGE); return; }

         // Get data directly from the model
         final int userId = (int) userTableModel.getValueAt(selectedRow, 0);
         String currentName = (String) userTableModel.getValueAt(selectedRow, 1);
         String currentEmail = (String) userTableModel.getValueAt(selectedRow, 2);
         String currentRole = (String) userTableModel.getValueAt(selectedRow, 3);
         // Removed loyalty points variable
         final int rowToUpdate = selectedRow; // Store view row index

         JDialog dialog = new JDialog(this, "Edit User (ID: " + userId + ")", true);
         dialog.setLayout(new BorderLayout());
         dialog.getContentPane().setBackground(ThemeColors.BACKGROUND); // Use imported theme color
         dialog.setSize(450, 250); // Adjusted size after removing points field
         dialog.setLocationRelativeTo(this);

         JPanel formPanel = new JPanel(new GridBagLayout());
         formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
         formPanel.setBackground(ThemeColors.BACKGROUND); // Use imported theme color
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.insets = new Insets(8, 8, 8, 8);
         gbc.anchor = GridBagConstraints.WEST;

         JTextField nameField = new JTextField(currentName, 20);
         JTextField emailField = new JTextField(currentEmail, 20);
         // Roles based on 'customers' table enum definition
         JComboBox<String> roleCombo = new JComboBox<>(new String[]{"customer", "admin"});
         roleCombo.setSelectedItem(currentRole);
         // Removed points spinner

         gbc.gridx = 0; gbc.gridy = 0; formPanel.add(createFormLabel("Name:"), gbc); // Uses imported ThemeColors
         gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; formPanel.add(nameField, gbc);

         gbc.gridx = 0; gbc.gridy++; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0; formPanel.add(createFormLabel("Email:"), gbc); // Uses imported ThemeColors
         gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; formPanel.add(emailField, gbc);

         gbc.gridx = 0; gbc.gridy++; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0; formPanel.add(createFormLabel("Role:"), gbc); // Uses imported ThemeColors
         gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; formPanel.add(roleCombo, gbc);

         // Removed loyalty points label and field layout

         JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
         buttonPanel.setBackground(ThemeColors.BACKGROUND); // Use imported theme color

         JButton saveButton = new JButton("Save Changes");
         saveButton.addActionListener(e -> {
             try {
                 String name = nameField.getText().trim();
                 String email = emailField.getText().trim();
                 String role = (String) roleCombo.getSelectedItem();
                 // Removed points variable retrieval

                 if (name.isEmpty() || email.isEmpty()) { showThemedJOptionPane("Name and Email cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE); return; }
                 // Basic email format check
                 if (!email.contains("@") || !email.contains(".")) { showThemedJOptionPane("Please enter a valid email address.", "Input Error", JOptionPane.ERROR_MESSAGE); return; }

                 // Pass updated parameters to updateUser (without points)
                 if (updateUser(userId, name, email, role)) {
                      // Update table model directly if row is still valid
                      if (rowToUpdate >= 0 && rowToUpdate < userTableModel.getRowCount() && (int)userTableModel.getValueAt(rowToUpdate, 0) == userId) {
                         userTableModel.setValueAt(name, rowToUpdate, 1);
                         userTableModel.setValueAt(email, rowToUpdate, 2);
                         userTableModel.setValueAt(role, rowToUpdate, 3);
                         // Removed updating points column
                      } else {
                          loadUsers(); // Reload if row became invalid
                      }
                     dialog.dispose();
                     showThemedJOptionPane("User updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                 } else {
                      // Error message shown in updateUser method
                 }
             } catch (Exception ex) { // Catch general exceptions
                 ex.printStackTrace();
                 showThemedJOptionPane("An unexpected error occurred: " + ex.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);
             }
         });

         JButton cancelButton = new JButton("Cancel");
         cancelButton.addActionListener(e -> dialog.dispose());

         buttonPanel.add(cancelButton);
         buttonPanel.add(saveButton);

         dialog.add(formPanel, BorderLayout.CENTER);
         dialog.add(buttonPanel, BorderLayout.SOUTH);
         dialog.setVisible(true);
    }
    // Updated updateUser signature - removed points parameter
    private boolean updateUser(int userId, String name, String email, String role) {
         // SQL uses columns from 'customers' table, removed loyalty_points
         String sql = "UPDATE customers SET name = ?, email = ?, role = ? WHERE id = ?"; // Removed loyalty_points = ?
         try (Connection conn = DBConnection.connect();
              PreparedStatement stmt = conn.prepareStatement(sql)) {
             stmt.setString(1, name);
             stmt.setString(2, email);
             stmt.setString(3, role);
             // Removed setting loyalty_points
             stmt.setInt(4, userId); // Index is now 4 for ID
             return stmt.executeUpdate() > 0;
         } catch (SQLException ex) {
             ex.printStackTrace();
             // Check for duplicate email error (assuming standard SQL state for unique constraint violation)
             if (ex.getSQLState() != null && ex.getSQLState().startsWith("23")) { // SQL state for integrity constraint violation
                   showThemedJOptionPane("Error updating user: Email address '" + email + "' is already in use.","Update Error (Duplicate Email)", JOptionPane.ERROR_MESSAGE);
             } else {
                 showThemedJOptionPane("Error updating user: " + ex.getMessage(),"Database Error", JOptionPane.ERROR_MESSAGE);
             }
             return false;
         }
    }
    private void filterSales(String status) {
        loadSales(status);
    }
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to log out?", "Logout Confirmation",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose(); // Close the AdminFrame
            // Assuming LoginFrame handles its own visibility and setup
            // Apply theme *before* creating LoginFrame instance
             try {
                 UIManager.setLookAndFeel(new FlatDarkLaf());
                 UIManager.put("OptionPane.background", ThemeColors.DIALOG_BG);
                 UIManager.put("Panel.background", ThemeColors.DIALOG_BG);
                 UIManager.put("OptionPane.messageForeground", ThemeColors.DIALOG_FG);
                 UIManager.put("Button.background", ThemeColors.SECONDARY);
                 UIManager.put("Button.foreground", Color.WHITE);
             } catch (Exception ex) { System.err.println("Failed to re-apply theme for LoginFrame"); }
             SwingUtilities.invokeLater(LoginFrame::new); // Show LoginFrame
        }
    }
     private JLabel createFormLabel(String text) {
         JLabel label = new JLabel(text);
         label.setFont(new Font("Arial", Font.BOLD, 13));
         label.setForeground(ThemeColors.TEXT); // Use imported theme color
         return label;
     }
    private void updateOrderStatus() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow < 0) { showThemedJOptionPane("Please select an order first.", "Selection Error", JOptionPane.WARNING_MESSAGE); return; }

        final int orderId;
        Object idObj = ordersTableModel.getValueAt(selectedRow, 0);
        // Ensure the ID is actually an integer before casting
        if (idObj instanceof Integer) {
            orderId = (Integer) idObj;
        } else {
            showThemedJOptionPane("Invalid Order ID data type in table.", "Data Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        final int rowToUpdate = selectedRow; // Store view row index

        String currentStatus = (String) ordersTableModel.getValueAt(selectedRow, 4);

        JDialog dialog = new JDialog(this, "Update Order Status (ID: " + orderId + ")", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(ThemeColors.BACKGROUND); // Use imported theme color
        dialog.setSize(400, 180);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(ThemeColors.BACKGROUND); // Use imported theme color

        JLabel currentLabel = new JLabel("Current Status: " + currentStatus);
        currentLabel.setForeground(ThemeColors.TEXT); // Use imported theme color
        panel.add(currentLabel);

        // Use status options based on 'orders' table schema enum
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Processing", "Shipped", "Delivered", "Completed", "Cancelled"});
        statusCombo.setSelectedItem(currentStatus);
        panel.add(statusCombo);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(ThemeColors.BACKGROUND); // Use imported theme color

        JButton updateButton = new JButton("Update Status");
        updateButton.addActionListener(e -> {
            String newStatus = (String) statusCombo.getSelectedItem();
            if (updateOrderStatusInDB(orderId, newStatus)) {
                 // Update table model directly if row is still valid
                 if (rowToUpdate >= 0 && rowToUpdate < ordersTableModel.getRowCount() && (int)ordersTableModel.getValueAt(rowToUpdate, 0) == orderId) {
                    ordersTableModel.setValueAt(newStatus, rowToUpdate, 4);
                 } else {
                     loadOrders(); // Reload if row became invalid
                 }
                dialog.dispose();
                showThemedJOptionPane("Order status updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                 // Error message shown in updateOrderStatusInDB
                 // showThemedJOptionPane("Failed to update order status.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

         JButton cancelButton = new JButton("Cancel");
         cancelButton.addActionListener(e -> dialog.dispose());

         buttonPanel.add(cancelButton);
         buttonPanel.add(updateButton);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    private boolean updateOrderStatusInDB(int orderId, String newStatus) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            showThemedJOptionPane("Error updating order status in database: " + ex.getMessage(),"Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    private void viewOrderDetails() {
         int selectedRow = ordersTable.getSelectedRow();
         if (selectedRow < 0) { showThemedJOptionPane("Please select an order to view details.", "Selection Error", JOptionPane.WARNING_MESSAGE); return; }

         final int orderId;
         Object idObj = ordersTableModel.getValueAt(selectedRow, 0);
         // Validate Order ID type
         if (idObj instanceof Integer) {
             orderId = (Integer) idObj;
         } else {
             showThemedJOptionPane("Invalid Order ID data type in table.", "Data Error", JOptionPane.ERROR_MESSAGE);
             return;
         }

         JDialog dialog = new JDialog(this, "Order Details #" + orderId, true);
         dialog.setLayout(new BorderLayout());
         dialog.getContentPane().setBackground(ThemeColors.BACKGROUND); // Use imported theme color
         dialog.setSize(600, 450);
         dialog.setLocationRelativeTo(this);

         JPanel infoPanel = new JPanel(new GridLayout(3, 2, 10, 5)); // Adjusted layout slightly (3 rows for Total Price)
         infoPanel.setBorder(BorderFactory.createCompoundBorder(
             BorderFactory.createTitledBorder(
                 BorderFactory.createLineBorder(ThemeColors.SECONDARY), // Use imported theme color
                 "Order Information", TitledBorder.LEFT, TitledBorder.TOP,
                 new Font("Arial", Font.BOLD, 14), ThemeColors.PRIMARY), // Use imported theme color
             BorderFactory.createEmptyBorder(10, 10, 10, 10))
         );
         infoPanel.setBackground(ThemeColors.BACKGROUND); // Use imported theme color

         // Fetch customer and order details
         try (Connection conn = DBConnection.connect();
              PreparedStatement custStmt = conn.prepareStatement(
                  "SELECT c.name, c.email, o.order_date, o.status, o.total_price " +
                  "FROM orders o JOIN customers c ON o.customer_id = c.id WHERE o.id = ?")) {
             custStmt.setInt(1, orderId);
             ResultSet custRs = custStmt.executeQuery();
             if (custRs.next()) {
                 infoPanel.add(createFormLabel("Customer:")); // Uses imported ThemeColors
                 infoPanel.add(new JLabel(custRs.getString("name")));
                 infoPanel.add(createFormLabel("Order Date:")); // Uses imported ThemeColors
                 infoPanel.add(new JLabel(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(custRs.getTimestamp("order_date"))));
                 infoPanel.add(createFormLabel("Email:")); // Uses imported ThemeColors
                 infoPanel.add(new JLabel(custRs.getString("email")));
                 infoPanel.add(createFormLabel("Status:")); // Uses imported ThemeColors
                 infoPanel.add(new JLabel(custRs.getString("status")));
                 // Display Total Price in info panel as well
                 infoPanel.add(createFormLabel("Order Total:"));
                 infoPanel.add(new JLabel(String.format("₱%.2f", custRs.getDouble("total_price"))));

             } else {
                 infoPanel.add(new JLabel("Order/Customer info not found."));
             }
         } catch (SQLException ex) {
             infoPanel.add(new JLabel("Error loading customer info: " + ex.getMessage()));
             ex.printStackTrace();
         }
         dialog.add(infoPanel, BorderLayout.NORTH);

         // Table for Order Items
         DefaultTableModel itemsModel = new DefaultTableModel(
             new String[]{"Product", "Quantity", "Unit Price", "Item Total"}, 0) {
             @Override public boolean isCellEditable(int row, int column) { return false; }
         };
         JTable detailsTable = new JTable(itemsModel);
         styleTable(detailsTable); // Uses imported ThemeColors
         // Adjust column widths for item details
         detailsTable.getColumnModel().getColumn(0).setPreferredWidth(250); // Product Name
         detailsTable.getColumnModel().getColumn(1).setCellRenderer(createCenterRenderer()); // Quantity (Center align)
         detailsTable.getColumnModel().getColumn(1).setPreferredWidth(70);
         detailsTable.getColumnModel().getColumn(2).setCellRenderer(createRightRenderer()); // Unit Price (Right align)
         detailsTable.getColumnModel().getColumn(2).setPreferredWidth(100);
         detailsTable.getColumnModel().getColumn(3).setCellRenderer(createRightRenderer()); // Item Total (Right align)
         detailsTable.getColumnModel().getColumn(3).setPreferredWidth(100);


         // Fetch order items
         try (Connection conn = DBConnection.connect();
              PreparedStatement stmt = conn.prepareStatement(
                  "SELECT p.name, oi.quantity, oi.price FROM order_items oi " +
                  "JOIN products p ON oi.product_id = p.id WHERE oi.order_id = ?")) {
             stmt.setInt(1, orderId);
             ResultSet rs = stmt.executeQuery();
             while (rs.next()) {
                 double unitPrice = rs.getDouble("price");
                 int quantity = rs.getInt("quantity");
                 itemsModel.addRow(new Object[]{
                     rs.getString("name"),
                     quantity,
                     unitPrice, // Pass raw number to let renderer format it
                     unitPrice * quantity // Pass raw number for total
                 });
             }
         } catch (SQLException ex) {
             ex.printStackTrace();
             showThemedJOptionPane("Error loading order items: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
         }

         JScrollPane scrollPane = new JScrollPane(detailsTable);
         scrollPane.setBorder(BorderFactory.createLineBorder(ThemeColors.SECONDARY)); // Use imported theme color
         dialog.add(scrollPane, BorderLayout.CENTER);

         // Close Button
         JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
         bottomPanel.setBackground(ThemeColors.BACKGROUND); // Use imported theme color
         JButton closeButton = new JButton("Close");
         closeButton.addActionListener(e -> dialog.dispose());
         bottomPanel.add(closeButton);
         dialog.add(bottomPanel, BorderLayout.SOUTH);

         dialog.setVisible(true);
     }
     private void processCancellation() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow < 0) { showThemedJOptionPane("Please select an order to process cancellation.", "Selection Error", JOptionPane.WARNING_MESSAGE); return; }

        final int orderId;
        Object idObj = ordersTableModel.getValueAt(selectedRow, 0);
         if (idObj instanceof Integer) {
             orderId = (Integer) idObj;
         } else {
             showThemedJOptionPane("Invalid Order ID data type in table.", "Data Error", JOptionPane.ERROR_MESSAGE);
             return;
         }
        final int rowToProcess = selectedRow; // Store view row index

        String currentStatus = (String) ordersTableModel.getValueAt(selectedRow, 4);
        String cancellationRequest = (String) ordersTableModel.getValueAt(selectedRow, 5);

        if (!cancellationRequest.equalsIgnoreCase("Yes")) {
             showThemedJOptionPane("This order does not have a pending cancellation request.","No Request", JOptionPane.INFORMATION_MESSAGE);
             return;
        }
        // Check if order status allows cancellation processing
        if (currentStatus.equalsIgnoreCase("Delivered") || currentStatus.equalsIgnoreCase("Completed") || currentStatus.equalsIgnoreCase("Cancelled")) {
             showThemedJOptionPane("Cannot process cancellation for an order that is already " + currentStatus + ".","Action Not Allowed", JOptionPane.WARNING_MESSAGE);
             return;
        }

        Object[] options = {"Approve Cancellation", "Deny Cancellation", "Cancel Action"};
        int choice = JOptionPane.showOptionDialog(this,
            "Order #" + orderId + " has a cancellation request.\nCurrent Status: " + currentStatus + "\n\nChoose an action:",
            "Process Cancellation Request", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);

        switch (choice) {
            case 0: // Approve
                approveCancellation(orderId, rowToProcess);
                break;
            case 1: // Deny
                denyCancellation(orderId, rowToProcess);
                break;
            // case 2 or closing dialog: Cancel Action - do nothing
        }
    }
     private void approveCancellation(int orderId, int tableRow) {
        Connection conn = null;
        try {
            conn = DBConnection.connect();
            conn.setAutoCommit(false); // Start transaction

            // 1. Update order status and clear cancellation request flag
            try (PreparedStatement stmt = conn.prepareStatement(
                // Set status to 'Cancelled' and cancellation_requested to 0 (false)
                "UPDATE orders SET status = 'Cancelled', cancellation_requested = 0 WHERE id = ?")) {
                stmt.setInt(1, orderId);
                if (stmt.executeUpdate() == 0) {
                    conn.rollback(); // Rollback if order not found or already updated
                    showThemedJOptionPane("Order #" + orderId + " not found or status already updated.", "Update Failed", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            // 2. Restock items (get items and quantities first)
            String selectItemsSql = "SELECT product_id, quantity FROM order_items WHERE order_id = ?";
            String updateStockSql = "UPDATE products SET stock = stock + ? WHERE id = ?";
            try (PreparedStatement selectStmt = conn.prepareStatement(selectItemsSql);
                 PreparedStatement updateStmt = conn.prepareStatement(updateStockSql)) {
                selectStmt.setInt(1, orderId);
                ResultSet rs = selectStmt.executeQuery();
                while (rs.next()) {
                    updateStmt.setInt(1, rs.getInt("quantity")); // Quantity to add back
                    updateStmt.setInt(2, rs.getInt("product_id")); // Product ID
                    updateStmt.addBatch(); // Add to batch for efficiency
                }
                updateStmt.executeBatch(); // Execute all stock updates
            }

            // 3. Placeholder for actual refund logic (e.g., call payment gateway API, update payment table)
            // For now, just log and maybe update payment table 'refunded' status if applicable
            System.out.println("LOG: Refund processing would be initiated here for cancelled order #" + orderId);
            // Optional: Update payments table if needed
            /*
            try (PreparedStatement refundStmt = conn.prepareStatement("UPDATE payments SET refunded = 1 WHERE order_id = ?")) {
                refundStmt.setInt(1, orderId);
                refundStmt.executeUpdate();
            }
            */

            conn.commit(); // Commit transaction if all steps succeed

            // Update UI immediately after successful DB operations
             final int finalTableRow = tableRow;
             SwingUtilities.invokeLater(() -> {
                  if (finalTableRow >= 0 && finalTableRow < ordersTableModel.getRowCount()) {
                      if ((int)ordersTableModel.getValueAt(finalTableRow, 0) == orderId) {
                          ordersTableModel.setValueAt("Cancelled", finalTableRow, 4);
                          ordersTableModel.setValueAt("No", finalTableRow, 5); // Update cancellation request status
                      } else {
                          // Row mismatch, reload table
                          System.err.println("Order ID mismatch at row " + finalTableRow + " during approve cancellation UI update. Reloading orders.");
                          loadOrders();
                      }
                 } else {
                      // Row index invalid, reload table
                      System.err.println("Invalid row index " + finalTableRow + " during approve cancellation UI update. Reloading orders.");
                      loadOrders();
                 }
             });

            showThemedJOptionPane(
                "Cancellation approved for Order #" + orderId + ".\nItems have been restocked.",
                "Cancellation Approved", JOptionPane.INFORMATION_MESSAGE);
             // Refresh inventory view as stock has changed
             loadInventory();

        } catch (SQLException ex) {
            if (conn != null) try { conn.rollback(); } catch (SQLException e) { e.printStackTrace(); } // Rollback on error
            ex.printStackTrace();
            showThemedJOptionPane("Error approving cancellation for Order #" + orderId + ":\n" + ex.getMessage(),"Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Ensure connection resources are released
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
     private void denyCancellation(int orderId, int tableRow) {
         // Only need to clear the cancellation request flag
         String sql = "UPDATE orders SET cancellation_requested = 0 WHERE id = ?";
         try (Connection conn = DBConnection.connect();
              PreparedStatement stmt = conn.prepareStatement(sql)) {
             stmt.setInt(1, orderId);
             int rowsAffected = stmt.executeUpdate();
             if (rowsAffected > 0) {
                 // Update UI immediately
                  final int finalTableRow = tableRow;
                  SwingUtilities.invokeLater(() -> {
                      if (finalTableRow >= 0 && finalTableRow < ordersTableModel.getRowCount()) {
                          if ((int)ordersTableModel.getValueAt(finalTableRow, 0) == orderId) {
                              ordersTableModel.setValueAt("No", finalTableRow, 5); // Update cancellation request status
                          } else {
                               System.err.println("Order ID mismatch at row " + finalTableRow + " during deny cancellation UI update. Reloading orders.");
                               loadOrders();
                          }
                     } else {
                          System.err.println("Invalid row index " + finalTableRow + " during deny cancellation UI update. Reloading orders.");
                          loadOrders();
                     }
                  });
                 showThemedJOptionPane(
                     "Cancellation request for Order #" + orderId + " has been denied.\nThe order will proceed with its current status.",
                     "Cancellation Denied", JOptionPane.INFORMATION_MESSAGE);
             } else {
                  showThemedJOptionPane("Could not find or update the order (it might have been processed already).", "Update Failed", JOptionPane.WARNING_MESSAGE);
             }
         } catch (SQLException ex) {
             ex.printStackTrace();
             showThemedJOptionPane("Error denying cancellation request for Order #" + orderId + ":\n" + ex.getMessage(),"Database Error", JOptionPane.ERROR_MESSAGE);
         }
     }


    // --- Helper method to check if an ImageIcon loaded successfully (no theme changes) ---
    private boolean isIconValid(ImageIcon icon) {
        if (icon == null) return false;
        // MediaTracker.COMPLETE means the image data is fully loaded and available
        return icon.getImageLoadStatus() == MediaTracker.COMPLETE && icon.getIconWidth() > 0 && icon.getIconHeight() > 0;
    }

    // --- UPDATED Statistics Panel Creation (Uses imported ThemeColors) ---
    private JPanel createStatisticsPanel() {
        JPanel panel = createTablePanel("Statistics & Reports"); // Uses imported ThemeColors

        // --- Filter Panel (Top) ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setOpaque(false);
        statisticsFilterComboBox = new JComboBox<>(new String[]{
                "Last 7 Days", "Last 30 Days", "This Month", "Last Month", "This Year", "All Time"
        });
        statisticsFilterComboBox.setSelectedIndex(1); // Default to "Last 30 Days" or another sensible default
        JButton updateChartButton = new JButton("Update Charts");
        updateChartButton.addActionListener(e -> updateStatisticsCharts()); // Updated Action Listener
        filterPanel.add(new JLabel("Date Range:")); // Label uses default L&F text color
        filterPanel.add(statisticsFilterComboBox);
        filterPanel.add(updateChartButton);
        panel.add(filterPanel, BorderLayout.NORTH);


        // --- Main content area for charts (Center) ---
        statisticsContentPanel = new JPanel(new GridLayout(2, 1, 10, 10)); // Changed to 2 rows, 1 col
        statisticsContentPanel.setBackground(ThemeColors.BACKGROUND); // Use imported theme color
        statisticsContentPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // 1. Product Ratings Bar Chart Panel (Placeholder) - Top Row
        productRatingsChartPanel = new JPanel(new BorderLayout());
        productRatingsChartPanel.setOpaque(false);
        productRatingsChartPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(ThemeColors.SECONDARY), // Use imported theme color
            "Top 10 Highest Rated Products (Avg)",
            TitledBorder.CENTER, TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 16), ThemeColors.PRIMARY // Use imported theme color
        ));
        statisticsContentPanel.add(productRatingsChartPanel);

        // 2. Sales Trend Line Chart Panel (Placeholder) - Bottom Row
        salesTrendChartPanel = new JPanel(new BorderLayout());
        salesTrendChartPanel.setOpaque(false);
        salesTrendChartPanel.setBorder(BorderFactory.createTitledBorder(
             BorderFactory.createLineBorder(ThemeColors.SECONDARY), // Use imported theme color
             "Sales Revenue Trend",
             TitledBorder.CENTER, TitledBorder.TOP,
             new Font("Arial", Font.BOLD, 16), ThemeColors.PRIMARY // Use imported theme color
        ));
        statisticsContentPanel.add(salesTrendChartPanel);

        panel.add(statisticsContentPanel, BorderLayout.CENTER);

        return panel;
    }

    // --- Method to Update ALL Statistics Charts based on Filter ---
    private void updateStatisticsCharts() {
        String selectedFilter = (String) statisticsFilterComboBox.getSelectedItem();
        if (selectedFilter == null) {
             System.err.println("Statistics filter is null, cannot update charts.");
             return;
        }

        // Calculate Date Range
        Date endDate = new Date(); // Today's date/time
        Date startDate = null;
        Calendar cal = Calendar.getInstance();

        // Set end date to the very end of today for inclusive range
        cal.setTime(endDate);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        endDate = cal.getTime();

        // Set start date based on filter, ensuring time is 00:00:00.000
        cal.setTime(endDate); // Reset calendar to end date
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0); // Start of the day for the beginning of the range

        switch (selectedFilter) {
            case "Last 7 Days":
                cal.add(Calendar.DAY_OF_YEAR, -6); // Go back 6 days (inclusive of today makes 7)
                startDate = cal.getTime();
                break;
            case "Last 30 Days":
                cal.add(Calendar.DAY_OF_YEAR, -29); // Go back 29 days (inclusive of today makes 30)
                startDate = cal.getTime();
                break;
            case "This Month":
                cal.set(Calendar.DAY_OF_MONTH, 1); // First day of current month
                startDate = cal.getTime();
                break;
            case "Last Month":
                // End date is end of last month
                cal.setTime(endDate);
                cal.set(Calendar.DAY_OF_MONTH, 1); // Go to first day of current month
                cal.add(Calendar.DAY_OF_YEAR, -1); // Go to last day of last month
                cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999); // End of day
                endDate = cal.getTime();
                // Start date is first day of last month
                cal.setTime(endDate); // Use the calculated end date (end of last month)
                cal.set(Calendar.DAY_OF_MONTH, 1); // Go to first day of last month
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0); // Start of day
                startDate = cal.getTime();
                break;
            case "This Year":
                cal.set(Calendar.DAY_OF_YEAR, 1); // First day of current year
                startDate = cal.getTime();
                break;
            case "All Time":
                startDate = null; // No start date filter
                endDate = null;   // No end date filter
                break;
            default: // Should not happen, but default to last 30 days
                cal.add(Calendar.DAY_OF_YEAR, -29);
                startDate = cal.getTime();
        }

        // --- Update Both Charts ---
        updateSalesTrendChart(startDate, endDate);
        updateProductRatingsChart(startDate, endDate); // Pass calculated dates
    }


    // --- Chart Update/Creation Methods for Statistics Panel (Use imported ThemeColors) ---
    private void updateProductRatingsChart(Date startDate, Date endDate) { // Added Date parameters
         if (productRatingsChartPanel == null) {
              System.err.println("productRatingsChartPanel is null, cannot update chart.");
              return;
         }

        CategoryChart chart = new CategoryChartBuilder().width(400).height(350) // Adjust size as needed
            .title("").xAxisTitle("Product").yAxisTitle("Average Rating").build();

        // Customize Styler using imported ThemeColors
        CategoryStyler styler = chart.getStyler();
        styler.setLegendVisible(false);
        styler.setPlotBackgroundColor(ThemeColors.BACKGROUND); // Use imported theme color
        styler.setChartBackgroundColor(ThemeColors.CARD_BG); // Use imported theme color
        styler.setChartFontColor(ThemeColors.TEXT); // Use imported theme color
        styler.setAxisTickLabelsColor(ThemeColors.TEXT); // Use imported theme color
        styler.setPlotGridLinesVisible(false);
        styler.setAvailableSpaceFill(.5);
        styler.setXAxisLabelRotation(45);
        styler.setYAxisMin(0.0); styler.setYAxisMax(5.0);
        styler.setDecimalPattern("#.0");
        styler.setSeriesColors(new Color[]{ThemeColors.ACCENT}); // Use imported theme color

        // Load data (SQL logic adjusted for date range)
        List<String> productNames = new ArrayList<>();
        List<Double> averageRatings = new ArrayList<>();
        // Corrected column name from r.creation_date to r.review_date
        String sqlBase = "SELECT p.name, AVG(r.rating) as avg_rating FROM reviews r JOIN products p ON r.product_id = p.id ";
        String sqlWhere = ""; // Initialize WHERE clause
        List<Object> params = new ArrayList<>(); // List to hold parameters for PreparedStatement

        // Add date filtering if startDate and endDate are provided
        if (startDate != null && endDate != null) {
            sqlWhere = "WHERE r.review_date BETWEEN ? AND ? "; // Use review_date from 'reviews' table
            params.add(new java.sql.Timestamp(startDate.getTime())); // Use Timestamp for potential time component in review_date
            params.add(new java.sql.Timestamp(endDate.getTime()));
        }
        // Complete the SQL query
        String sqlGroupBy = "GROUP BY r.product_id, p.name ";
        String sqlOrderBy = "ORDER BY avg_rating DESC LIMIT 10"; // Get top 10
        String sql = sqlBase + sqlWhere + sqlGroupBy + sqlOrderBy;

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set parameters for the PreparedStatement
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                String name = rs.getString("name");
                 // Truncate long names for better chart display
                if (name.length() > 25) name = name.substring(0, 22) + "...";
                productNames.add(name);
                averageRatings.add(rs.getDouble("avg_rating"));
            }

            productRatingsChartPanel.removeAll(); // Clear previous content

            if (!hasData) {
                 JLabel noDataLabel = new JLabel("No review data available for the selected period.", SwingConstants.CENTER);
                 noDataLabel.setFont(new Font("Arial", Font.ITALIC, 14));
                 noDataLabel.setForeground(ThemeColors.TEXT); // Use imported theme color
                 productRatingsChartPanel.add(noDataLabel, BorderLayout.CENTER);
            } else {
                chart.addSeries("Average Rating", productNames, averageRatings);
                JPanel chartPanelComponent = new XChartPanel<>(chart);
                chartPanelComponent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                productRatingsChartPanel.add(chartPanelComponent, BorderLayout.CENTER);
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Print stack trace for debugging
            productRatingsChartPanel.removeAll(); // Clear previous content
            JLabel errorLabel = new JLabel("Error loading ratings data: " + e.getMessage(), SwingConstants.CENTER);
            errorLabel.setFont(new Font("Arial", Font.BOLD, 14)); errorLabel.setForeground(Color.RED);
            productRatingsChartPanel.add(errorLabel, BorderLayout.CENTER);
            // Optionally show a themed JOptionPane as well
            showThemedJOptionPane("Database Error: Could not load product ratings.\n" + e.getMessage(), "Ratings Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) { // Catch unexpected errors during chart generation/display
            e.printStackTrace();
             productRatingsChartPanel.removeAll(); // Clear previous content
             JLabel errorLabel = new JLabel("Error displaying ratings chart: " + e.getMessage(), SwingConstants.CENTER);
             errorLabel.setFont(new Font("Arial", Font.BOLD, 14)); errorLabel.setForeground(Color.RED);
             productRatingsChartPanel.add(errorLabel, BorderLayout.CENTER);
             showThemedJOptionPane("Chart Display Error: Could not display product ratings.\n" + e.getMessage(), "Ratings Error", JOptionPane.ERROR_MESSAGE);
        }

        // Refresh the panel to show the new chart or message
        productRatingsChartPanel.revalidate();
        productRatingsChartPanel.repaint();
        if (productRatingsChartPanel.getParent() != null) {
            productRatingsChartPanel.getParent().revalidate();
            productRatingsChartPanel.getParent().repaint();
        }
    }

    private void updateSalesTrendChart(Date startDate, Date endDate) { // Added Date parameters
         if (salesTrendChartPanel == null) {
              System.err.println("salesTrendChartPanel is null, cannot update chart.");
              return;
         }

        // Determine date grouping (Logic remains the same)
        String xAxisTitle = "Date"; String dateGroupFormat = "%Y-%m-%d"; // Default to daily
        String dateParsePattern = "yyyy-MM-dd"; String stylerDatePattern = "MM-dd";
        if (startDate != null && endDate != null) {
            long diffInMillis = Math.abs(endDate.getTime() - startDate.getTime());
            long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);
            if (diffInDays > 90) { // Group by month if range > ~3 months
                 dateGroupFormat = "%Y-%m"; dateParsePattern = "yyyy-MM";
                 xAxisTitle = "Month"; stylerDatePattern = "MMM yyyy";
            }
        } else { // All Time - Group by month
             dateGroupFormat = "%Y-%m"; dateParsePattern = "yyyy-MM";
             xAxisTitle = "Month"; stylerDatePattern = "MMM yyyy";
        }

        XYChart chart = new XYChartBuilder().width(800).height(400) // Adjust size as needed
            .title("").xAxisTitle(xAxisTitle).yAxisTitle("Total Revenue (₱)").build();

        // Customize Styler using imported ThemeColors
        XYStyler styler = chart.getStyler();
        styler.setLegendVisible(false);
        styler.setPlotBackgroundColor(ThemeColors.BACKGROUND); // Use imported theme color
        styler.setChartBackgroundColor(ThemeColors.CARD_BG); // Use imported theme color - Match panel border background
        styler.setChartFontColor(ThemeColors.TEXT); // Use imported theme color
        styler.setAxisTickLabelsColor(ThemeColors.TEXT); // Use imported theme color
        styler.setPlotGridLinesColor(new Color(80, 80, 80)); // Subtle grid - maybe use SECONDARY?
        styler.setXAxisLabelRotation(45);
        styler.setDatePattern(stylerDatePattern);
        styler.setDecimalPattern("₱ #,###.00"); // Ensure currency format
        styler.setMarkerSize(6);
        styler.setSeriesMarkers(new Marker[] { SeriesMarkers.CIRCLE });
        styler.setSeriesColors(new Color[]{ThemeColors.PRIMARY}); // Use imported theme color

        // Load data (SQL logic adjusted for date range)
        List<Date> xData = new ArrayList<>();
        List<Double> yData = new ArrayList<>();
        // Use order_date column from orders table
        String sqlBase = "SELECT DATE_FORMAT(order_date, ?) as period, SUM(total_price) as total FROM orders ";
        // Filter by statuses considered as revenue-generating
        String sqlWhere = "WHERE status IN ('Delivered', 'Completed') ";
        List<Object> params = new ArrayList<>();
        params.add(dateGroupFormat); // Parameter for DATE_FORMAT

        // Add date range filtering if applicable
        if (startDate != null && endDate != null) {
             sqlWhere += "AND order_date BETWEEN ? AND ? "; // Filter directly on order_date
             params.add(new java.sql.Timestamp(startDate.getTime()));
             params.add(new java.sql.Timestamp(endDate.getTime()));
        }
        String sqlGroupBy = "GROUP BY period ORDER BY period ASC";
        String sql = sqlBase + sqlWhere + sqlGroupBy;

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Set parameters for the PreparedStatement
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            SimpleDateFormat parser = new SimpleDateFormat(dateParsePattern);
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                try {
                    xData.add(parser.parse(rs.getString("period")));
                    yData.add(rs.getDouble("total"));
                } catch (ParseException pe) {
                     System.err.println("Error parsing date from DB: " + rs.getString("period") + " using pattern: " + dateParsePattern);
                     // Optionally skip this data point or handle differently
                } catch (SQLException sqle) {
                     System.err.println("SQL Error processing row for period: " + rs.getString("period") + ", Error: " + sqle.getMessage());
                }
            }

            salesTrendChartPanel.removeAll(); // Clear previous content

            if (!hasData) {
                 JLabel noDataLabel = new JLabel("No sales data available for the selected period.", SwingConstants.CENTER);
                 noDataLabel.setFont(new Font("Arial", Font.ITALIC, 14));
                 noDataLabel.setForeground(ThemeColors.TEXT); // Use imported theme color
                 salesTrendChartPanel.add(noDataLabel, BorderLayout.CENTER);
            } else {
                if (!xData.isEmpty()) { // Ensure data exists before adding series
                    chart.addSeries("Sales", xData, yData);
                }
                JPanel chartPanelComponent = new XChartPanel<>(chart);
                chartPanelComponent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                salesTrendChartPanel.add(chartPanelComponent, BorderLayout.CENTER);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            salesTrendChartPanel.removeAll(); // Clear previous content
            JLabel errorLabel = new JLabel("Error loading sales trend data: " + e.getMessage(), SwingConstants.CENTER);
            errorLabel.setFont(new Font("Arial", Font.BOLD, 14)); errorLabel.setForeground(Color.RED);
            salesTrendChartPanel.add(errorLabel, BorderLayout.CENTER);
            showThemedJOptionPane("Database Error: Could not load sales trend data.\n" + e.getMessage(), "Sales Trend Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) { // Catch unexpected errors during chart generation/display
            e.printStackTrace();
             salesTrendChartPanel.removeAll(); // Clear previous content
             JLabel errorLabel = new JLabel("Error displaying sales trend chart: " + e.getMessage(), SwingConstants.CENTER);
             errorLabel.setFont(new Font("Arial", Font.BOLD, 14)); errorLabel.setForeground(Color.RED);
             salesTrendChartPanel.add(errorLabel, BorderLayout.CENTER);
             showThemedJOptionPane("Chart Display Error: Could not display sales trend chart.\n" + e.getMessage(), "Sales Trend Error", JOptionPane.ERROR_MESSAGE);
        }

        salesTrendChartPanel.revalidate();
        salesTrendChartPanel.repaint();
        if (salesTrendChartPanel.getParent() != null) {
            salesTrendChartPanel.getParent().revalidate();
            salesTrendChartPanel.getParent().repaint();
        }
    }


    // --- Main Method (Applies the ORIGINAL FlatDarkLaf theme) ---
    public static void main(String[] args) {
        try {
            // --- THEME: Applying FlatDarkLaf, same as original ---
            UIManager.setLookAndFeel(new FlatDarkLaf());
            // Apply theme settings for JOptionPane globally here if preferred
            UIManager.put("OptionPane.background", ThemeColors.DIALOG_BG);
            UIManager.put("Panel.background", ThemeColors.DIALOG_BG); // Affects OptionPane panel
            UIManager.put("OptionPane.messageForeground", ThemeColors.DIALOG_FG);
            UIManager.put("Button.background", ThemeColors.SECONDARY);
            UIManager.put("Button.foreground", Color.WHITE);
            // Customize button focus color if desired (optional)
            UIManager.put("Button.focus", new Color(ThemeColors.SECONDARY.getRed(), ThemeColors.SECONDARY.getGreen(), ThemeColors.SECONDARY.getBlue(), 180)); // Slightly transparent
             UIManager.put("Button.hoverBackground", ThemeColors.BUTTON_HOVER);
             UIManager.put("Button.pressedBackground", ThemeColors.SECONDARY.darker());

        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatDarkLaf theme:");
            ex.printStackTrace();
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception e) { System.err.println("Failed to set system LookAndFeel."); }
        }

        SwingUtilities.invokeLater(() -> {
            new AdminFrame().setVisible(true);
        });
    }

    // --- Static nested class for DB Connection (Needed for functionality) ---
    // Keep this nested class as it was originally defined
    private static class DBConnection {
        private static final String URL = "jdbc:mysql://localhost:3306/kpop_merch_store";
        private static final String USER = "root";      // Replace if needed
        private static final String PASSWORD = "";  // Replace if needed

        public static Connection connect() throws SQLException {
            try {
                // Ensure the driver is loaded
                Class.forName("com.mysql.cj.jdbc.Driver");
            }
            catch (ClassNotFoundException e) {
                System.err.println("MySQL JDBC Driver not found. Include it in your library path.");
                e.printStackTrace();
                // Re-throw as SQLException to be caught by calling methods
                throw new SQLException("JDBC Driver not found", e);
            }
            // Establish and return the connection
            return DriverManager.getConnection(URL, USER, PASSWORD);
        }
    }

    // --- REMOVED INNER ThemeColors class ---
    // The colors are now referenced from the imported OnlineShop.ThemeColors

    // --- Helper for JOptionPane theming (uses imported ThemeColors) ---
    // Instance method version
    private void showThemedJOptionPane(String message, String title, int messageType) {
        // UIManager settings applied in main should handle this, but can be set here too for robustness
        UIManager.put("OptionPane.background", ThemeColors.DIALOG_BG);
        UIManager.put("Panel.background", ThemeColors.DIALOG_BG);
        UIManager.put("OptionPane.messageForeground", ThemeColors.DIALOG_FG);
        UIManager.put("Button.background", ThemeColors.SECONDARY);
        UIManager.put("Button.foreground", Color.WHITE);
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    // Static version if needed elsewhere without an AdminFrame instance
    private static void showThemedJOptionPane(Component parent, String message, String title, int messageType) {
        UIManager.put("OptionPane.background", ThemeColors.DIALOG_BG);
        UIManager.put("Panel.background", ThemeColors.DIALOG_BG);
        UIManager.put("OptionPane.messageForeground", ThemeColors.DIALOG_FG);
        UIManager.put("Button.background", ThemeColors.SECONDARY);
        UIManager.put("Button.foreground", Color.WHITE);
        JOptionPane.showMessageDialog(parent, message, title, messageType);
    }
}