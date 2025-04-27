package OnlineShop;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import com.formdev.flatlaf.FlatDarkLaf;

// --- XChart Imports (Add XChart library to your project) ---
import org.knowm.xchart.*;
import org.knowm.xchart.style.PieStyler;
import org.knowm.xchart.style.Styler;
// --- End XChart Imports ---

import java.io.File;
// Removed FileNotFoundException import as it's not explicitly thrown/caught
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
// Removed unused Map import
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;


public class AdminFrame extends JFrame {
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JTable userTable, productTable, inventoryTable, salesTable, supplierTable, ordersTable;
    // Use the custom ProductTableModel
    private ProductTableModel productTableModel;
    private DefaultTableModel userTableModel, inventoryTableModel,
                            salesTableModel, supplierTableModel, ordersTableModel;


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
    // --- End ProductTableModel ---

    public AdminFrame() {
        setTitle("Admin Panel - K-Pop Merch Store");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Set to full screen or maximized
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
        JPanel dashboardPanel = createDashboardPanel();
        JPanel usersPanel = createUsersPanel();
        JPanel productsPanel = createProductsPanel();
        JPanel inventoryPanel = createInventoryPanel();
        JPanel salesPanel = createSalesPanel();
        JPanel ordersPanel = createOrdersPanel();

        // Add panels to mainPanel
        mainPanel.add(dashboardPanel, "Dashboard");
        mainPanel.add(usersPanel, "Users");
        mainPanel.add(productsPanel, "Products");
        mainPanel.add(inventoryPanel, "Inventory");
        mainPanel.add(salesPanel, "Sales");
        mainPanel.add(ordersPanel, "Orders");

        add(mainPanel, BorderLayout.CENTER);
        add(createNavigationBar(), BorderLayout.WEST);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createNavigationBar() {
         JPanel navBar = new JPanel(new BorderLayout());
         navBar.setBackground(ThemeColors.BACKGROUND);
         navBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
         navBar.setPreferredSize(new Dimension(220, getHeight()));

         // Logo Section
         JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
         logoPanel.setOpaque(false);

         JLabel logo = new JLabel("ADMIN");
         logo.setFont(new Font("Arial", Font.BOLD, 16));
         logo.setForeground(ThemeColors.PRIMARY);
         logoPanel.add(logo);
         navBar.add(logoPanel, BorderLayout.NORTH);

         // Navigation Buttons
         JPanel navButtons = new JPanel();
         navButtons.setLayout(new BoxLayout(navButtons, BoxLayout.Y_AXIS));
         navButtons.setOpaque(false);
         navButtons.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

         String[] navItems = {"Dashboard", "Users", "Products", "Inventory", "Sales", "Orders"};

         for (String item : navItems) {
             JButton btn = createNavButton(item);
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
         logoutBtn.setBackground(ThemeColors.SECONDARY);
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

         // Button styling
         button.setFont(new Font("Arial", Font.PLAIN, 14));
         button.setForeground(ThemeColors.TEXT);
         button.setBackground(ThemeColors.CARD_BG);
         button.setFocusPainted(false);
         button.setHorizontalAlignment(SwingConstants.CENTER);
         button.setVerticalAlignment(SwingConstants.CENTER);
         button.setBorder(BorderFactory.createCompoundBorder(
             BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.SECONDARY),
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
                // Optional: Scale icon if needed
                 Image img = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                 button.setIcon(new ImageIcon(img));
                // button.setIcon(icon);
                button.setHorizontalTextPosition(SwingConstants.RIGHT); // Text to the right of icon
                button.setIconTextGap(10); // Space between icon and text
            } else {
                System.err.println("Icon not found: " + iconPath);
            }

         } catch (Exception e) {
              System.err.println("Error loading icon for button '" + text + "': " + e.getMessage());
         }

         // Action listeners for all navigation buttons
         switch(text) {
             case "Dashboard":
                 button.addActionListener(e -> cardLayout.show(mainPanel, "Dashboard"));
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
             case "Logout":
                 button.addActionListener(e -> logout());
                 break;
         }

         // Hover effects
         button.addMouseListener(new MouseAdapter() {
             Color originalBg = button.getBackground();
             Color originalFg = button.getForeground();
             @Override
             public void mouseEntered(MouseEvent e) {
                 button.setBackground(ThemeColors.BUTTON_HOVER);
                 button.setForeground(Color.WHITE);
                 button.setCursor(new Cursor(Cursor.HAND_CURSOR));
             }

             @Override
             public void mouseExited(MouseEvent e) {
                  // Special case for logout button hover
                 if (!text.equals("Logout")) {
                     button.setBackground(originalBg);
                     button.setForeground(originalFg);
                 } else {
                      button.setBackground(ThemeColors.SECONDARY); // Keep logout distinct
                      button.setForeground(Color.WHITE);
                 }
                 button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
             }
         });

         return button;
    }

    // --- Updated Dashboard Panel with Chart ---
    private JPanel createDashboardPanel() {
        JPanel dashboardPanel = new JPanel(new BorderLayout());
        dashboardPanel.setBackground(ThemeColors.BACKGROUND);
        dashboardPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Admin Dashboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(ThemeColors.PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        dashboardPanel.add(titleLabel, BorderLayout.NORTH);

        // Main content panel using GridBagLayout for more control
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(ThemeColors.BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Padding
        gbc.fill = GridBagConstraints.BOTH; // Components fill space

        // Metrics panel (Top row, spanning 2 columns)
        JPanel metricsPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        metricsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); // Reduced bottom padding
        metricsPanel.setBackground(ThemeColors.BACKGROUND);
        metricsPanel.add(createMetricCard("Total Products", getTotalProducts(), "/icons/products.png")); // Use resource path
        metricsPanel.add(createMetricCard("Total Orders", getTotalOrders(), "/icons/orders.png"));
        metricsPanel.add(createMetricCard("Low Stock Items", getLowStockItems(), "/icons/inventory.png")); // Changed icon
        metricsPanel.add(createMetricCard("Total Customers", getTotalCustomers(), "/icons/users.png"));
        metricsPanel.add(createMetricCard("Total Revenue", getTotalRevenue(), "/icons/sales.png")); // Changed icon
        metricsPanel.add(createMetricCard("Pending Orders", getPendingOrders(), "/icons/dashboard.png")); // Changed icon

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Span 2 columns
        gbc.weightx = 1.0;
        gbc.weighty = 0.2; // Allocate less vertical space
        contentPanel.add(metricsPanel, gbc);

        // Sales Category Chart Panel (Middle left)
        JPanel chartPanel = createSalesCategoryChartPanel(); // Use the new method
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1; // Reset width
        gbc.weightx = 0.5; // Half horizontal space
        gbc.weighty = 0.8; // More vertical space
        contentPanel.add(chartPanel, gbc);


        // Real-time Order Notifications Panel (Middle right)
        JPanel notificationsPanel = new JPanel(new BorderLayout());
        notificationsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(ThemeColors.SECONDARY),
            "Recent Order Notifications",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 16),
            ThemeColors.PRIMARY
        ));
        notificationsPanel.setBackground(ThemeColors.BACKGROUND);

        // Notifications Table
        DefaultTableModel notificationsModel = new DefaultTableModel(
            new String[]{"Time", "Order #", "Customer", "Amount", "Items"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable notificationsTable = new JTable(notificationsModel); // Declared locally here

        // Apply generic styling first
        styleTable(notificationsTable);
        notificationsTable.setRowHeight(28); // Slightly smaller row height for notifications

        // --- Apply specific column styling for notificationsTable HERE ---
        TableColumnModel ncm = notificationsTable.getColumnModel();
        // Create renderers specifically for this table (can reuse the instances created in styleTable if made accessible)
        DefaultTableCellRenderer nCenterRenderer = new DefaultTableCellRenderer() {
             @Override
             public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                 setHorizontalAlignment(SwingConstants.CENTER);
                 setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? ThemeColors.CARD_BG : new Color(ThemeColors.CARD_BG.getRed() - 10, ThemeColors.CARD_BG.getGreen() - 10, ThemeColors.CARD_BG.getBlue() - 10));
                    c.setForeground(ThemeColors.TEXT);
                } else {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                }
                 return c;
             }
        };
        DefaultTableCellRenderer nRightRenderer = new DefaultTableCellRenderer(){
             @Override
             public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                 setHorizontalAlignment(SwingConstants.RIGHT);
                 setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                 if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? ThemeColors.CARD_BG : new Color(ThemeColors.CARD_BG.getRed() - 10, ThemeColors.CARD_BG.getGreen() - 10, ThemeColors.CARD_BG.getBlue() - 10));
                    c.setForeground(ThemeColors.TEXT);
                 } else {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                 }
                 return c;
             }
        };

        try {
            ncm.getColumn(0).setCellRenderer(nCenterRenderer); // Time Center
            ncm.getColumn(1).setCellRenderer(nCenterRenderer); // Order # Center
            ncm.getColumn(3).setCellRenderer(nRightRenderer);  // Amount Right
            ncm.getColumn(4).setCellRenderer(nCenterRenderer); // Items Center
            // Set preferred widths (adjust as needed)
            ncm.getColumn(0).setPreferredWidth(60);
            ncm.getColumn(1).setPreferredWidth(80);
            ncm.getColumn(2).setPreferredWidth(150); // Customer
            ncm.getColumn(3).setPreferredWidth(100);
            ncm.getColumn(4).setPreferredWidth(80);
            notificationsTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS); // Ensure resize mode is set
        } catch (ArrayIndexOutOfBoundsException aioobe) {
             System.err.println("Error setting column renderers/widths for notifications table: " + aioobe.getMessage());
        }
         // --- End of specific styling for notificationsTable ---


        // Load initial notifications
        loadOrderNotifications(notificationsModel);

        // Auto-refresh every 15 seconds (keep existing logic)
        Timer refreshTimer = new Timer(15000, e -> {
            int previousRowCount = notificationsModel.getRowCount();
            loadOrderNotifications(notificationsModel);
            if (notificationsModel.getRowCount() > previousRowCount) {
                Toolkit.getDefaultToolkit().beep();
                 notificationsPanel.setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(ThemeColors.PRIMARY, 2), // Highlight border
                        "NEW ORDERS! (" + (notificationsModel.getRowCount() - previousRowCount) + ") - Recent Order Notifications",
                        TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 16), ThemeColors.PRIMARY));
                // Reset border after 3 seconds
                 new Timer(3000, ev -> {
                     notificationsPanel.setBorder(BorderFactory.createTitledBorder(
                             BorderFactory.createLineBorder(ThemeColors.SECONDARY), "Recent Order Notifications",
                             TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 16), ThemeColors.PRIMARY));
                     ((Timer) ev.getSource()).stop();
                 }).start();
            }
        });
        refreshTimer.start();

        // Add view button
        JButton viewOrdersButton = new JButton("View All Orders");
        viewOrdersButton.addActionListener(e -> {
            loadOrders(); // Load before showing
            cardLayout.show(mainPanel, "Orders");
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(viewOrdersButton);

        notificationsPanel.add(new JScrollPane(notificationsTable), BorderLayout.CENTER);
        notificationsPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add Notifications Panel to GridBagLayout
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        gbc.weighty = 0.8;
        contentPanel.add(notificationsPanel, gbc);

        dashboardPanel.add(contentPanel, BorderLayout.CENTER);

        return dashboardPanel;
    }

        // --- Method to create the Sales Category Pie Chart ---
    private JPanel createSalesCategoryChartPanel() {
        JPanel chartWrapperPanel = new JPanel(new BorderLayout());
        chartWrapperPanel.setBackground(ThemeColors.CARD_BG); // Use card background
        chartWrapperPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(ThemeColors.SECONDARY),
            "Top 5 Sales Categories (Revenue)",
            TitledBorder.CENTER, // Center title
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 16),
            ThemeColors.PRIMARY
        ));

        // Create Chart
        PieChart chart = new PieChartBuilder().width(400).height(350).title("").build(); // Remove default title

        // Customize Chart Theme
        PieStyler styler = chart.getStyler();
        styler.setLegendVisible(true);
        styler.setLegendPosition(Styler.LegendPosition.InsideNE); // Or OutsideE
        styler.setAnnotationType(PieStyler.AnnotationType.LabelAndPercentage);
        styler.setAnnotationDistance(1.15);
        styler.setPlotContentSize(.7);
        styler.setStartAngleInDegrees(90);
        styler.setPlotBackgroundColor(ThemeColors.BACKGROUND);
        styler.setChartBackgroundColor(ThemeColors.CARD_BG);
        styler.setLegendBackgroundColor(ThemeColors.CARD_BG);
        styler.setChartFontColor(ThemeColors.TEXT);
        styler.setPlotBorderVisible(false);
        styler.setChartTitleVisible(false); // Hide default title, use border title
        styler.setLegendFont(new Font("Arial", Font.PLAIN, 12));

        styler.setAnnotationsFont(new Font("Arial", Font.PLAIN, 11));
        styler.setSumVisible(true); // Show total value
        styler.setSumFont(new Font("Arial", Font.BOLD, 14));
        styler.setDecimalPattern("₱ #,###.##"); // Currency format for sum/annotations

         // Set custom colors matching the theme (add more if needed)
         styler.setSeriesColors(new Color[]{
                 ThemeColors.PRIMARY,
                 ThemeColors.SECONDARY,
                 new Color(100, 180, 220), // Lighter blue
                 new Color(150, 120, 200), // Purple variant
                 new Color(80, 160, 160)   // Teal variant
         });


        // Load data
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT p.group_name, SUM(oi.quantity * oi.price) as total_revenue " +
                 "FROM order_items oi " +
                 "JOIN products p ON oi.product_id = p.id " +
                 "JOIN orders o ON oi.order_id = o.id " +
                 "WHERE o.status IN ('Delivered', 'Completed') " + // Consider completed status
                 "GROUP BY p.group_name " +
                 "ORDER BY total_revenue DESC LIMIT 5"
             )) {

            ResultSet rs = stmt.executeQuery();
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                String groupName = rs.getString("group_name");
                // Handle null or empty group names gracefully for the chart
                if (groupName == null || groupName.trim().isEmpty()){
                    groupName = "[Uncategorized]";
                }
                chart.addSeries(groupName, rs.getDouble("total_revenue"));
            }

            if (!hasData) {
                 // Display a message if no sales data
                 JLabel noDataLabel = new JLabel("No sales data available for chart.", SwingConstants.CENTER);
                 noDataLabel.setFont(new Font("Arial", Font.ITALIC, 14));
                 noDataLabel.setForeground(ThemeColors.TEXT);
                 chartWrapperPanel.add(noDataLabel, BorderLayout.CENTER);
                 return chartWrapperPanel;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JLabel errorLabel = new JLabel("Error loading chart data.", SwingConstants.CENTER);
            errorLabel.setFont(new Font("Arial", Font.BOLD, 14));
            errorLabel.setForeground(Color.RED);
            chartWrapperPanel.add(errorLabel, BorderLayout.CENTER);
            return chartWrapperPanel; // Return panel with error message
        }

        // Display chart
        JPanel chartPanel = new XChartPanel<>(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding around chart
        chartWrapperPanel.add(chartPanel, BorderLayout.CENTER);

        return chartWrapperPanel;
    }


    private void loadOrderNotifications(DefaultTableModel model) {
         try (Connection conn = DBConnection.connect();
              Statement stmt = conn.createStatement();
              ResultSet rs = stmt.executeQuery(
                  "SELECT o.id, c.name, o.total_price, o.order_date, " +
                  "(SELECT COUNT(*) FROM order_items WHERE order_id = o.id) as item_count " +
                  "FROM orders o JOIN customers c ON o.customer_id = c.id " +
                  "WHERE o.order_date >= NOW() - INTERVAL 24 HOUR " + // Show orders from last 24 hours
                  "ORDER BY o.order_date DESC LIMIT 10")) { // Limit to recent 10

             model.setRowCount(0); // Clear previous notifications
             SimpleDateFormat sdf = new SimpleDateFormat("HH:mm"); // Time only

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

    private JPanel createMetricCard(String title, String value, String iconResourcePath) {
         JPanel card = new JPanel(new BorderLayout(10, 10));
         card.setBackground(ThemeColors.CARD_BG);
         card.setBorder(BorderFactory.createCompoundBorder(
             BorderFactory.createMatteBorder(1, 1, 1, 1, ThemeColors.SECONDARY),
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
                 // Optionally scale:
                  Image img = icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                  iconLabel.setIcon(new ImageIcon(img));
                 // iconLabel.setIcon(icon);
             } else {
                 System.err.println("Metric icon not found: " + iconResourcePath);
                 iconLabel.setText("!"); // Fallback text
                 iconLabel.setForeground(Color.RED);
             }
         } catch (Exception e) {
             System.err.println("Error loading metric icon " + iconResourcePath + ": " + e);
             iconLabel.setText("Err");
             iconLabel.setForeground(Color.RED);
         }


         JLabel titleLabel = new JLabel(title);
         titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
         titleLabel.setForeground(ThemeColors.TEXT);

         topPanel.add(iconLabel);
         topPanel.add(titleLabel);

         // Value display
         JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
         valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
         valueLabel.setForeground(ThemeColors.PRIMARY);

         card.add(topPanel, BorderLayout.NORTH);
         card.add(valueLabel, BorderLayout.CENTER);

         return card;
    }

    private JPanel createUsersPanel() {
        JPanel panel = createTablePanel("User Management");
        userTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Role", "Loyalty Points"}, 0) {
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

    // --- Updated Products Panel Creation ---
    private JPanel createProductsPanel() {
        JPanel panel = createTablePanel("Product Management");
        // Initialize the custom model
        productTableModel = new ProductTableModel();
        productTable = new JTable(productTableModel);

        // Apply common styling FIRST
        styleTable(productTable);

        // Set custom renderer for the image column (index 1)
        productTable.getColumnModel().getColumn(1).setCellRenderer(new ImageRenderer());
        productTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Give image column reasonable width
        productTable.getColumnModel().getColumn(1).setMaxWidth(120);
        productTable.getColumnModel().getColumn(0).setPreferredWidth(50); // ID column smaller
        productTable.getColumnModel().getColumn(0).setMaxWidth(60);
        productTable.getColumnModel().getColumn(3).setPreferredWidth(80); // Price
        productTable.getColumnModel().getColumn(4).setPreferredWidth(60); // Stock

        productTable.setRowHeight(80); // Ensure row height accommodates image

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

    // --- Updated Image Renderer ---
    private class ImageRenderer extends DefaultTableCellRenderer {
        public ImageRenderer() {
            super();
            setHorizontalAlignment(JLabel.CENTER); // Center the image/text
            setVerticalAlignment(JLabel.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            // Important: Call super first to get default styling (like selection)
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            JLabel label = (JLabel) c; // Cast to JLabel
            label.setText(""); // Don't show text in the image column
            label.setIcon(null); // Reset icon

            if (value instanceof ImageIcon) {
                ImageIcon icon = (ImageIcon) value;
                if (isIconValid(icon)) { // Use the helper method
                    // Scale the image smoothly to fit the cell height minus some padding
                    int padding = 10;
                    int targetHeight = table.getRowHeight() - padding;
                    int targetWidth = table.getColumnModel().getColumn(column).getWidth() - padding; // Consider width too
                    int size = Math.min(targetHeight, targetWidth); // Use the smaller dimension

                     if (size > 0) {
                         // Only scale if the original is larger than the target size
                         if (icon.getIconWidth() > size || icon.getIconHeight() > size) {
                             Image image = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                             label.setIcon(new ImageIcon(image));
                         } else {
                             label.setIcon(icon); // Use original size if it fits
                         }
                     } else {
                        label.setIcon(icon); // Use original if calculation fails
                     }
                } else {
                    // Handle case where icon is invalid or empty
                    label.setText("No Image");
                    label.setFont(new Font("Arial", Font.ITALIC, 10));
                }
            } else {
                 label.setText("No Image");
                 label.setFont(new Font("Arial", Font.ITALIC, 10));
            }

            // Set background based on selection and row (handled by super call, but explicitly set for clarity)
             if (!isSelected) {
                // Apply alternating row colors if not selected
                label.setBackground(row % 2 == 0 ? ThemeColors.CARD_BG :
                        new Color(ThemeColors.CARD_BG.getRed() - 10,
                                ThemeColors.CARD_BG.getGreen() - 10,
                                ThemeColors.CARD_BG.getBlue() - 10));
                label.setForeground(ThemeColors.TEXT); // Reset foreground for non-selected
            } else {
                // Use table's selection colors
                label.setBackground(table.getSelectionBackground());
                label.setForeground(table.getSelectionForeground());
            }
            label.setOpaque(true); // Ensure background is painted

            return label;
        }
    }

    private JPanel createInventoryPanel() {
         JPanel panel = createTablePanel("Inventory Management");
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
         salesTableModel = new DefaultTableModel(
             new String[]{"Order ID", "Customer", "Product", "Quantity", "Total Price", "Date", "Status"}, 0){
              @Override public boolean isCellEditable(int row, int column) { return false; }
         };
         salesTable = new JTable(salesTableModel);
         styleTable(salesTable);

         // Add filter options
         JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
         filterPanel.setOpaque(false);

         JComboBox<String> statusFilter = new JComboBox<>(
             new String[]{"All", "Processing", "Shipped", "Delivered", "Cancelled", "Completed"}); // Added Completed
         JButton filterButton = new JButton("Filter");
         filterButton.addActionListener(e -> filterSales(statusFilter.getSelectedItem().toString()));

         filterPanel.add(new JLabel("Status:"));
         filterPanel.add(statusFilter);
         filterPanel.add(filterButton);

         panel.add(filterPanel, BorderLayout.NORTH); // Place filter at the top
         panel.add(new JScrollPane(salesTable), BorderLayout.CENTER);
         return panel;
    }

    private JPanel createOrdersPanel() {
         JPanel panel = createTablePanel("Order Management");
         ordersTableModel = new DefaultTableModel(
             new String[]{"Order ID", "Customer", "Total", "Date", "Status", "Cancellation Request"}, 0) {
             @Override
             public boolean isCellEditable(int row, int column) {
                 return false;
             }
         };
         ordersTable = new JTable(ordersTableModel);
         styleTable(ordersTable);

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

    private JPanel createTablePanel(String title) {
        JPanel panel = new JPanel(new BorderLayout(10, 10)); // Add gaps
        panel.setBackground(ThemeColors.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15)); // Add padding

        // Title
        JLabel titleLabel = new JLabel(title, SwingConstants.LEFT); // Align left
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22)); // Slightly smaller
        titleLabel.setForeground(ThemeColors.PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 15, 5)); // Adjust padding
        panel.add(titleLabel, BorderLayout.NORTH);

        return panel;
    }

    // --- Updated Table Styling ---
    private void styleTable(JTable table) {
        table.setBackground(ThemeColors.CARD_BG);
        table.setForeground(ThemeColors.TEXT);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setGridColor(ThemeColors.SECONDARY.darker()); // More distinct grid color
        table.setSelectionBackground(ThemeColors.PRIMARY);
        table.setSelectionForeground(Color.WHITE);
        table.setRowHeight(table == productTable ? 80 : 35); // Keep product row height, others taller
        table.setShowGrid(true); // Keep grid visible
        table.setIntercellSpacing(new Dimension(1, 1)); // Space between cells
        table.setFillsViewportHeight(true); // Table fills scrollpane height
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Allow horizontal scrolling if needed

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(ThemeColors.PRIMARY);
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getWidth(), 40)); // Taller header
         // Add padding to header cells
        TableCellRenderer headerRenderer = header.getDefaultRenderer();
        if (headerRenderer instanceof JLabel) {
            ((JLabel) headerRenderer).setHorizontalAlignment(SwingConstants.LEFT);
            ((JLabel) headerRenderer).setBorder(BorderFactory.createCompoundBorder(
                ((JLabel) headerRenderer).getBorder(),
                BorderFactory.createEmptyBorder(0, 10, 0, 10))
            );
        } else { // Apply padding even if renderer is not JLabel (e.g., custom header renderer)
            header.setDefaultRenderer(new DefaultTableCellRenderer() {
                 @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                     Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                     if (c instanceof JLabel) {
                          ((JLabel)c).setHorizontalAlignment(SwingConstants.LEFT);
                           ((JLabel)c).setBorder(BorderFactory.createCompoundBorder(
                             ((JLabel) c).getBorder(), BorderFactory.createEmptyBorder(0, 10, 0, 10)));
                           c.setBackground(ThemeColors.PRIMARY);
                           c.setForeground(Color.WHITE);
                           c.setFont(new Font("Arial", Font.BOLD, 14));
                     }
                     return c;
                 }
            });
        }


        // Custom Default Renderer for Padding and Alternating Colors
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);

                // Apply padding
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                // Apply alternating row colors if not selected
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? ThemeColors.CARD_BG :
                            new Color(ThemeColors.CARD_BG.getRed() - 10,
                                    ThemeColors.CARD_BG.getGreen() - 10,
                                    ThemeColors.CARD_BG.getBlue() - 10));
                     c.setForeground(ThemeColors.TEXT); // Ensure default text color
                } else {
                    // Use table's selection colors
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                }


                // Default alignment (can be overridden per column)
                setHorizontalAlignment(SwingConstants.LEFT);

                return c;
            }
        };

        // Apply the custom default renderer to all columns initially
        // EXCEPT the image column if it's the product table
        for (int i = 0; i < table.getColumnCount(); i++) {
             if (table == productTable && i == 1) continue; // Skip image column
             table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }


        // --- Specific Column Alignments ---
        // Create renderer instances *outside* the loop/try-catch
        // Use anonymous inner classes directly here to avoid scope issues with notificationsTable
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
             public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                 setHorizontalAlignment(SwingConstants.CENTER);
                 setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Keep padding
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? ThemeColors.CARD_BG : new Color(ThemeColors.CARD_BG.getRed() - 10, ThemeColors.CARD_BG.getGreen() - 10, ThemeColors.CARD_BG.getBlue() - 10));
                    c.setForeground(ThemeColors.TEXT);
                } else {
                     c.setBackground(table.getSelectionBackground());
                     c.setForeground(table.getSelectionForeground());
                }
                 return c;
             }
        };

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer(){
             @Override
             public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                 setHorizontalAlignment(SwingConstants.RIGHT);
                 setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Keep padding
                 if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? ThemeColors.CARD_BG : new Color(ThemeColors.CARD_BG.getRed() - 10, ThemeColors.CARD_BG.getGreen() - 10, ThemeColors.CARD_BG.getBlue() - 10));
                    c.setForeground(ThemeColors.TEXT);
                 } else {
                     c.setBackground(table.getSelectionBackground());
                     c.setForeground(table.getSelectionForeground());
                 }
                 return c;
             }
        };

        TableColumnModel cm = table.getColumnModel();
        try {
             if (table == productTable) {
                 cm.getColumn(0).setCellRenderer(centerRenderer); // ID Center
                 // Image column (1) uses ImageRenderer, already centered
                 cm.getColumn(3).setCellRenderer(rightRenderer); // Price Right
                 cm.getColumn(4).setCellRenderer(centerRenderer); // Stock Center
                 // Set preferred widths after renderers
                 cm.getColumn(0).setPreferredWidth(60);
                 cm.getColumn(1).setPreferredWidth(100);
                 cm.getColumn(2).setPreferredWidth(250); // Name wider
                 cm.getColumn(3).setPreferredWidth(100);
                 cm.getColumn(4).setPreferredWidth(80);
                 cm.getColumn(5).setPreferredWidth(150); // Group
                 cm.getColumn(6).setPreferredWidth(200); // Image Path

             } else if (table == userTable) {
                 cm.getColumn(0).setCellRenderer(centerRenderer); // ID Center
                 cm.getColumn(4).setCellRenderer(centerRenderer); // Points Center
                 cm.getColumn(0).setPreferredWidth(50);
                 cm.getColumn(1).setPreferredWidth(200); // Name
                 cm.getColumn(2).setPreferredWidth(250); // Email
                 cm.getColumn(3).setPreferredWidth(100); // Role
                 cm.getColumn(4).setPreferredWidth(120); // Points

             } else if (table == inventoryTable) {
                  cm.getColumn(0).setCellRenderer(centerRenderer); // ID Center
                  cm.getColumn(2).setCellRenderer(centerRenderer); // Stock Center
                  cm.getColumn(3).setCellRenderer(centerRenderer); // Reorder Center
                  cm.getColumn(0).setPreferredWidth(60);
                  cm.getColumn(1).setPreferredWidth(300); // Name
                  cm.getColumn(2).setPreferredWidth(100);
                  cm.getColumn(3).setPreferredWidth(120);

             } else if (table == salesTable) {
                  cm.getColumn(0).setCellRenderer(centerRenderer); // Order ID Center
                  cm.getColumn(3).setCellRenderer(centerRenderer); // Qty Center
                  cm.getColumn(4).setCellRenderer(rightRenderer); // Price Right
                  cm.getColumn(0).setPreferredWidth(80);
                  cm.getColumn(1).setPreferredWidth(180); // Customer
                  cm.getColumn(2).setPreferredWidth(250); // Product
                  cm.getColumn(3).setPreferredWidth(80);
                  cm.getColumn(4).setPreferredWidth(120);
                  cm.getColumn(5).setPreferredWidth(150); // Date
                  cm.getColumn(6).setPreferredWidth(120); // Status

             } else if (table == ordersTable) {
                  cm.getColumn(0).setCellRenderer(centerRenderer); // Order ID Center
                  cm.getColumn(2).setCellRenderer(rightRenderer); // Total Right
                  cm.getColumn(0).setPreferredWidth(80);
                  cm.getColumn(1).setPreferredWidth(200); // Customer
                  cm.getColumn(2).setPreferredWidth(120);
                  cm.getColumn(3).setPreferredWidth(150); // Date
                  cm.getColumn(4).setPreferredWidth(120); // Status
                  cm.getColumn(5).setPreferredWidth(150); // Cancellation Req
             }
             // --- REMOVED notificationsTable check from here ---
             // Add more else if blocks for other tables if needed
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Error setting column renderers/widths - possibly table model changed? " + e.getMessage());
        }

         table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS); // Resize other columns
    }

    private void loadUsers() {
         try (Connection conn = DBConnection.connect();
              Statement stmt = conn.createStatement();
              ResultSet rs = stmt.executeQuery("SELECT id, name, email, role, loyalty_points FROM customers")) {
             userTableModel.setRowCount(0);
             while (rs.next()) {
                 userTableModel.addRow(new Object[]{
                     rs.getInt("id"),
                     rs.getString("name"),
                     rs.getString("email"),
                     rs.getString("role"),
                     rs.getInt("loyalty_points")
                 });
             }
         } catch (SQLException ex) {
             ex.printStackTrace();
             JOptionPane.showMessageDialog(this, "Error loading users.", "Error", JOptionPane.ERROR_MESSAGE);
         }
    }

    // --- Updated Product Loading with Robust Image Handling ---
    private void loadProducts() {
        // Reset the model (important to clear old image paths/icons)
        productTableModel = new ProductTableModel();
        productTable.setModel(productTableModel);
        // Re-apply styling and renderers after setting the new model
        styleTable(productTable); // Apply general style FIRST
        // THEN set specific renderer for image column
        productTable.getColumnModel().getColumn(1).setCellRenderer(new ImageRenderer());
        productTable.setRowHeight(80); // Ensure row height is set

        // Select the image_path column. This remains the primary source.
        String query = "SELECT id, name, price, stock, group_name, description, image_path FROM products ORDER BY name";

        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // --- Define potential locations ONCE outside the loop ---
            // Check relative to runtime first, then classpath
            String[] relativeBaseDirs = {"images/products/", "src/images/products/"}; // Check in "images/products/" then root, add src path for IDE
            String[] classpathBaseDirs = {"/images/products/", "/"}; // Check in "/images/products/" then root of classpath

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                int stock = rs.getInt("stock");
                String group = rs.getString("group_name");
                String imagePathFromDB = rs.getString("image_path"); // Path from the database
                String description = rs.getString("description");

                ImageIcon icon = null;
                String loadedFromPath = imagePathFromDB; // Keep track of the path that worked

                // ---== Strategy: ==---
                // 1. Try loading using the exact imagePathFromDB (as filesystem, then classpath)
                // 2. If #1 fails, derive a filename from the product 'name' and try loading THAT
                //    from multiple locations (filesystem, then classpath).
                // 3. If #1 and #2 fail, load the default placeholder image.

                // --- Attempt 1 & 2: Use Database Path (imagePathFromDB) ---
                if (imagePathFromDB != null && !imagePathFromDB.isEmpty()) {
                    // Try as Filesystem Path first
                    File dbFile = new File(imagePathFromDB);
                    if (dbFile.exists() && dbFile.isFile()) {
                        try {
                            icon = new ImageIcon(dbFile.toURI().toURL());
                            if (!isIconValid(icon)) icon = null; // Check if image loaded correctly
                        } catch (Exception e) { icon = null; }
                    }

                    // Try as Classpath Resource if filesystem failed
                    if (icon == null) {
                        String resourcePathDB = imagePathFromDB.replace("\\", "/");
                        if (!resourcePathDB.startsWith("/")) resourcePathDB = "/" + resourcePathDB;
                        URL urlDB = getClass().getResource(resourcePathDB);
                        if (urlDB != null) {
                             icon = new ImageIcon(urlDB);
                             if (!isIconValid(icon)) icon = null;
                        }
                    }
                }

                // --- Attempt 3: Derive filename from Product Name (if DB path failed or was null) ---
                if (icon == null && name != null && !name.isEmpty()) {
                    // Create a filename based on convention (replace non-alphanumeric, lowercase, add .png)
                    String derivedFilename = name.replaceAll("[^a-zA-Z0-9.\\-_ ]", "_").toLowerCase() + ".png"; // Allow spaces and hyphens too
                    loadedFromPath = "[Derived] " + derivedFilename; // Indicate it's derived

                    // Try derived name in relative filesystem directories
                    for (String baseDir : relativeBaseDirs) {
                        File derivedFile = new File(baseDir + derivedFilename);
                         if (derivedFile.exists() && derivedFile.isFile()) {
                            try {
                                icon = new ImageIcon(derivedFile.toURI().toURL());
                                if (isIconValid(icon)) {
                                    loadedFromPath = derivedFile.getPath(); // Update path if successful
                                    break; // Found it
                                } else {
                                    icon = null;
                                }
                            } catch (Exception e) { icon = null; }
                        }
                    }

                    // Try derived name as Classpath Resource if filesystem failed
                    if (icon == null) {
                         for (String baseDir : classpathBaseDirs) {
                            String resourcePathDerived = baseDir + derivedFilename;
                            // Ensure leading slash for classpath resource
                            if (!resourcePathDerived.startsWith("/")) resourcePathDerived = "/" + resourcePathDerived;

                            URL urlDerived = getClass().getResource(resourcePathDerived);
                            if (urlDerived != null) {
                                 icon = new ImageIcon(urlDerived);
                                 if (isIconValid(icon)) {
                                     loadedFromPath = resourcePathDerived; // Update path
                                     break; // Found it
                                 } else {
                                     icon = null;
                                 }
                            }
                        }
                    }
                } // End of Attempt 3


                // --- Attempt 4: Fallback to Default Image ---
                if (icon == null) {
                    try {
                        // Ensure 'default_product.png' is in '/images/' inside your resources/classpath
                        URL defaultUrl = getClass().getResource("/images/default_product.png");
                        if (defaultUrl != null) {
                            icon = new ImageIcon(defaultUrl);
                            if (!isIconValid(icon)) icon = new ImageIcon(); // Empty if default is bad
                        } else {
                             System.err.println("ERROR: Default product image '/images/default_product.png' not found!");
                             icon = new ImageIcon(); // Empty icon
                        }
                    } catch (Exception e) {
                         System.err.println("Error loading default product image: " + e.getMessage());
                         icon = new ImageIcon(); // Empty icon on error
                    }
                    loadedFromPath = "[Default Image]";
                }


                // --- Add Row to Table Model ---
                 // Ensure icon is not null before adding
                 if (icon == null) icon = new ImageIcon(); // Assign empty icon if somehow still null

                // Pass the successfully loaded icon and the original DB path
                 productTableModel.addRowWithImage(
                    new Object[]{id, icon, name, price, stock, group, imagePathFromDB != null ? imagePathFromDB : ""}, // Show DB path in the last column
                    imagePathFromDB, // Store the original path string from DB
                    icon             // Store the finally loaded icon itself
                );

                 // Optional: Log which path was successful for debugging
                 // System.out.println("Loaded image for product ID " + id + " ("+name+") from: " + loadedFromPath);

            } // End while loop
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading products: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
             e.printStackTrace();
             JOptionPane.showMessageDialog(this, "An unexpected error occurred while loading products: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Crucial: After loading, re-apply column widths and renderers as the model was reset
        styleTable(productTable);
        productTable.getColumnModel().getColumn(1).setCellRenderer(new ImageRenderer());
        productTable.setRowHeight(80);
        productTable.revalidate();
        productTable.repaint();
    }

    private void loadInventory() {
         try (Connection conn = DBConnection.connect();
              Statement stmt = conn.createStatement();
              // Assuming reorder_level exists
              ResultSet rs = stmt.executeQuery("SELECT p.id, p.name, p.stock, COALESCE(p.reorder_level, 0) as reorder_level FROM products p")) {
             inventoryTableModel.setRowCount(0);
             while (rs.next()) {
                 inventoryTableModel.addRow(new Object[]{
                     rs.getInt("id"),
                     rs.getString("name"),
                     rs.getInt("stock"),
                     rs.getInt("reorder_level") // Use COALESCE for potential NULLs
                 });
             }
         } catch (SQLException ex) {
             ex.printStackTrace();
             JOptionPane.showMessageDialog(this, "Error loading inventory.", "Error", JOptionPane.ERROR_MESSAGE);
         }
    }

    private void loadSales() {
        loadSales("All");
    }

    private void loadSales(String statusFilter) {
         try (Connection conn = DBConnection.connect()) {
             String sql = "SELECT o.id, c.name AS customer_name, p.name AS product_name, oi.quantity, oi.price, o.order_date, o.status " + // Aliased names
                          "FROM orders o " +
                          "JOIN order_items oi ON o.id = oi.order_id " +
                          "JOIN products p ON oi.product_id = p.id " +
                          "JOIN customers c ON o.customer_id = c.id";

             if (!statusFilter.equals("All")) {
                 sql += " WHERE o.status = ?";
             }

             sql += " ORDER BY o.order_date DESC";

             try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                 if (!statusFilter.equals("All")) {
                     stmt.setString(1, statusFilter);
                 }

                 ResultSet rs = stmt.executeQuery();
                 salesTableModel.setRowCount(0);
                 while (rs.next()) {
                     salesTableModel.addRow(new Object[]{
                         rs.getInt("id"),
                         rs.getString("customer_name"),
                         rs.getString("product_name"),
                         rs.getInt("quantity"),
                         rs.getDouble("price") * rs.getInt("quantity"), // Calculate item total price
                         rs.getTimestamp("order_date"), // Use Timestamp for better formatting potentially
                         rs.getString("status")
                     });
                 }
             }
         } catch (SQLException ex) {
             ex.printStackTrace();
             JOptionPane.showMessageDialog(this, "Error loading sales.", "Error", JOptionPane.ERROR_MESSAGE);
         }
    }

    private void loadOrders() {
         try (Connection conn = DBConnection.connect();
              Statement stmt = conn.createStatement();
              ResultSet rs = stmt.executeQuery(
                  "SELECT o.id, c.name, o.total_price, o.order_date, o.status, " +
                  "CASE WHEN o.cancellation_requested = 1 THEN 'Yes' ELSE 'No' END as cancellation_request " +
                  "FROM orders o JOIN customers c ON o.customer_id = c.id " +
                  "ORDER BY o.order_date DESC")) {
             ordersTableModel.setRowCount(0);
             while (rs.next()) {
                 ordersTableModel.addRow(new Object[]{
                     rs.getInt("id"),
                     rs.getString("name"),
                     String.format("₱%.2f", rs.getDouble("total_price")), // Format currency
                     rs.getTimestamp("order_date"), // Use Timestamp
                     rs.getString("status"),
                     rs.getString("cancellation_request")
                 });
             }
         } catch (SQLException ex) {
             ex.printStackTrace();
             JOptionPane.showMessageDialog(this, "Error loading orders.", "Error", JOptionPane.ERROR_MESSAGE);
         }
    }

    // --- Dashboard Metric Calculation Methods ---
    private String getTotalProducts() {
         try (Connection conn = DBConnection.connect();
              Statement stmt = conn.createStatement();
              ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products")) {
             if (rs.next()) {
                 return rs.getString(1);
             }
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return "0";
    }

    private String getTotalOrders() {
         try (Connection conn = DBConnection.connect();
              Statement stmt = conn.createStatement();
              ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM orders")) {
             if (rs.next()) {
                 return rs.getString(1);
             }
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return "0";
    }

    private String getLowStockItems() {
         try (Connection conn = DBConnection.connect();
              Statement stmt = conn.createStatement();
              ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products WHERE stock < COALESCE(reorder_level, 0)")) {
             if (rs.next()) {
                 return rs.getString(1);
             }
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return "0";
    }

    private String getTotalCustomers() {
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM customers WHERE role = 'customer'")) { // Count only customers
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return "0";
    }

    private String getTotalRevenue() {
         try (Connection conn = DBConnection.connect();
              PreparedStatement stmt = conn.prepareStatement(
                  "SELECT SUM(total_price) FROM orders WHERE status IN ('Delivered', 'Completed')")) { // Include Completed

             ResultSet rs = stmt.executeQuery();
             if (rs.next()) {
                 double total = rs.getDouble(1);
                 return "₱" + String.format("%,.2f", total); // Added comma formatting
             }
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return "₱0.00";
    }

    private String getPendingOrders() {
         try (Connection conn = DBConnection.connect();
              Statement stmt = conn.createStatement();
              ResultSet rs = stmt.executeQuery(
                  "SELECT COUNT(*) FROM orders WHERE status IN ('Processing', 'Shipped')")) {
             if (rs.next()) {
                 return rs.getString(1);
             }
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return "0";
    }

    // --- Action Handlers (Add/Edit/Delete/Update/View/Process etc.) ---

    private void addProduct() {
        JDialog dialog = new JDialog(this, "Add New Product", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(ThemeColors.BACKGROUND);
        dialog.setSize(550, 650); // Adjusted size
        dialog.setLocationRelativeTo(this);

        // Main form panel with padding
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS)); // Vertical layout
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(ThemeColors.BACKGROUND);

        // Use GridBagLayout for better field alignment
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(ThemeColors.BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Input fields
        JTextField nameField = new JTextField(20);
        JTextField priceField = new JTextField(10);
        JTextField stockField = new JTextField(5);
         JTextField reorderLevelField = new JTextField(5); // Added Reorder Level
        JTextField groupNameField = new JTextField(15);
        JTextArea descriptionArea = new JTextArea(4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);

        // Image upload components
        JLabel imagePathLabel = new JLabel("No image selected");
        imagePathLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        imagePathLabel.setForeground(ThemeColors.TEXT);
        JButton browseButton = new JButton("Browse...");
        JLabel imagePreviewLabel = new JLabel();
        imagePreviewLabel.setPreferredSize(new Dimension(150, 150)); // Smaller preview
        imagePreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imagePreviewLabel.setBorder(BorderFactory.createLineBorder(ThemeColors.SECONDARY));
        imagePreviewLabel.setBackground(ThemeColors.CARD_BG);
        imagePreviewLabel.setOpaque(true);

        // Store the selected file path
        final File[] selectedImageFile = {null}; // Use array to be modifiable in lambda

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
                    imagePreviewLabel.setText(null); // Remove text if image loads
                } catch (Exception ex) {
                    imagePreviewLabel.setIcon(null);
                    imagePreviewLabel.setText("Preview Error");
                    JOptionPane.showMessageDialog(dialog, "Error loading image preview.", "Preview Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Layout using GridBagLayout
        gbc.gridx = 0; gbc.gridy = 0; fieldsPanel.add(createFormLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; fieldsPanel.add(nameField, gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; // Reset

        gbc.gridx = 0; gbc.gridy++; fieldsPanel.add(createFormLabel("Price (₱):"), gbc);
        gbc.gridx = 1; fieldsPanel.add(priceField, gbc);

        gbc.gridx = 0; gbc.gridy++; fieldsPanel.add(createFormLabel("Stock:"), gbc);
        gbc.gridx = 1; fieldsPanel.add(stockField, gbc);

        gbc.gridx = 0; gbc.gridy++; fieldsPanel.add(createFormLabel("Reorder Lvl:"), gbc); // Reorder Level
        gbc.gridx = 1; fieldsPanel.add(reorderLevelField, gbc);

        gbc.gridx = 0; gbc.gridy++; fieldsPanel.add(createFormLabel("Group Name:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; fieldsPanel.add(groupNameField, gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;

        gbc.gridx = 0; gbc.gridy++; gbc.anchor = GridBagConstraints.NORTHWEST; fieldsPanel.add(createFormLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0; fieldsPanel.add(descriptionScroll, gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weighty = 0.0; gbc.anchor = GridBagConstraints.WEST; gbc.gridwidth = 1; // Reset

        gbc.gridx = 0; gbc.gridy++; fieldsPanel.add(createFormLabel("Image:"), gbc);
        gbc.gridx = 1; fieldsPanel.add(browseButton, gbc);
        gbc.gridx = 2; fieldsPanel.add(imagePathLabel, gbc);

        // Image Preview Panel
        JPanel previewContainer = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Center the preview
        previewContainer.setBackground(ThemeColors.BACKGROUND);
        previewContainer.add(imagePreviewLabel);
        previewContainer.setBorder(BorderFactory.createTitledBorder(
             BorderFactory.createEmptyBorder(10, 0, 10, 0), // Add some vertical spacing
             "Image Preview",
             TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION,
             new Font("Arial", Font.PLAIN, 12), ThemeColors.TEXT
        ));


        formPanel.add(fieldsPanel);
        formPanel.add(previewContainer); // Add preview below fields

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(ThemeColors.BACKGROUND);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton saveButton = new JButton("Save Product");
        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String priceStr = priceField.getText().trim();
                String stockStr = stockField.getText().trim();
                String reorderStr = reorderLevelField.getText().trim();
                String groupName = groupNameField.getText().trim();
                String description = descriptionArea.getText().trim();

                if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty() || groupName.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Name, Price, Stock, and Group Name are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double price = Double.parseDouble(priceStr);
                int stock = Integer.parseInt(stockStr);
                int reorderLevel = reorderStr.isEmpty() ? 0 : Integer.parseInt(reorderStr); // Default 0 if empty

                // Handle image saving
                String finalImagePath = null;
                if (selectedImageFile[0] != null) {
                    File sourceFile = selectedImageFile[0];
                    // --- IMPORTANT: Ensure this directory exists or can be created ---
                    File imagesDir = new File("images/products"); // Relative path
                    if (!imagesDir.exists()) {
                        if (!imagesDir.mkdirs()) {
                            // Handle error: Maybe show a message dialog or log it
                            System.err.println("Failed to create product images directory: " + imagesDir.getAbsolutePath());
                            JOptionPane.showMessageDialog(dialog,
                                "Could not create directory for images.\nPlease ensure permissions allow creating 'images/products'.",
                                "Directory Error", JOptionPane.ERROR_MESSAGE);
                            return; // Stop processing if directory can't be made
                        }
                    }
                    // --- END Directory Check ---

                    String extension = "";
                    int i = sourceFile.getName().lastIndexOf('.');
                    if (i > 0) {
                        extension = sourceFile.getName().substring(i); // .png, .jpg etc.
                    }
                    // Create a more robust filename (e.g., using product name + timestamp)
                    String safeName = name.replaceAll("[^a-zA-Z0-9.\\-_ ]", "_"); // Allow spaces and hyphens too
                    String newFileName = "product_" + safeName + "_" + System.currentTimeMillis() + extension;
                    File destinationFile = new File(imagesDir, newFileName);

                    try {
                        Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        // Store the relative path used by the application for loading later
                        finalImagePath = imagesDir.getPath().replace("\\", "/") + "/" + newFileName; // Use forward slashes
                        System.out.println("Saved image to: " + finalImagePath); // Debug print
                    } catch (IOException ioEx) {
                         // Handle potential file system errors during copy
                        System.err.println("Error copying image file: " + ioEx.getMessage());
                        JOptionPane.showMessageDialog(dialog,
                            "Could not save the image file: " + ioEx.getMessage(),
                            "File Copy Error", JOptionPane.ERROR_MESSAGE);
                         return; // Stop if image cannot be saved
                    }
                }


                if (saveNewProduct(name, price, stock, groupName, description, finalImagePath, reorderLevel)) {
                    loadProducts(); // Refresh table
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this, "Product added successfully!");
                } else {
                     JOptionPane.showMessageDialog(dialog, "Failed to save product to database.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers for Price, Stock, and Reorder Level.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                 ex.printStackTrace();
                 JOptionPane.showMessageDialog(dialog, "An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        // Add components to dialog
        dialog.add(new JScrollPane(formPanel), BorderLayout.CENTER); // Make form scrollable if needed
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

     // Updated saveNewProduct to include reorder level
    private boolean saveNewProduct(String name, double price, int stock,
                                   String groupName, String description, String imagePath, int reorderLevel) {
          // Added reorder_level to SQL
          String sql = "INSERT INTO products (name, price, stock, group_name, description, image_path, reorder_level) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?)";
          try (Connection conn = DBConnection.connect();
               PreparedStatement stmt = conn.prepareStatement(sql)) {

              stmt.setString(1, name);
              stmt.setDouble(2, price);
              stmt.setInt(3, stock);
              stmt.setString(4, groupName);
              stmt.setString(5, description);
              // Handle null image path gracefully
              if (imagePath != null && !imagePath.isEmpty()) { // Check if not empty too
                  stmt.setString(6, imagePath);
              } else {
                  stmt.setNull(6, Types.VARCHAR);
              }
              stmt.setInt(7, reorderLevel); // Set reorder level

              int rowsAffected = stmt.executeUpdate();
              return rowsAffected > 0;

          } catch (SQLException ex) {
              ex.printStackTrace();
              JOptionPane.showMessageDialog(this,
                  "Error adding product to database: " + ex.getMessage(),
                  "Database Error", JOptionPane.ERROR_MESSAGE);
              return false;
          }
      }


    private void editProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product to edit.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get data from the MODEL, not the table view directly
        final int productId = (int) productTableModel.getValueAt(selectedRow, 0); // FIX: Make final
        ImageIcon currentIcon = (ImageIcon) productTableModel.getValueAt(selectedRow, 1); // Get the icon
        String currentName = (String) productTableModel.getValueAt(selectedRow, 2);
        double currentPrice = (double) productTableModel.getValueAt(selectedRow, 3);
        int currentStock = (int) productTableModel.getValueAt(selectedRow, 4);
        String currentGroup = (String) productTableModel.getValueAt(selectedRow, 5);
        String currentImagePath = productTableModel.getImagePath(selectedRow); // Get path from model

        // Fetch description and reorder level from DB as they aren't in the basic table model
        String currentDescription = "";
        int currentReorderLevel = 0;
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT description, reorder_level FROM products WHERE id = ?")) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                currentDescription = rs.getString("description");
                currentReorderLevel = rs.getInt("reorder_level");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching product details: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            // Optionally return or proceed with empty description/reorder level
        }


        JDialog dialog = new JDialog(this, "Edit Product (ID: " + productId + ")", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(ThemeColors.BACKGROUND);
        dialog.setSize(550, 650); // Same size as Add dialog
        dialog.setLocationRelativeTo(this);

        // --- Form structure similar to addProduct ---
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(ThemeColors.BACKGROUND);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(ThemeColors.BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField nameField = new JTextField(currentName, 20);
        JTextField priceField = new JTextField(String.valueOf(currentPrice), 10);
        JTextField stockField = new JTextField(String.valueOf(currentStock), 5);
        JTextField reorderLevelField = new JTextField(String.valueOf(currentReorderLevel), 5); // Edit reorder level
        JTextField groupNameField = new JTextField(currentGroup, 15);
        JTextArea descriptionArea = new JTextArea(currentDescription, 4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);

        // Display filename from path, handle null
        String currentImageFilename = "No image selected";
        if (currentImagePath != null && !currentImagePath.isEmpty()) {
            try {
                currentImageFilename = new File(currentImagePath).getName();
            } catch (Exception e) {
                // Handle potential path issues if needed, but keep default text
            }
        }
        JLabel imagePathLabel = new JLabel(currentImageFilename);
        imagePathLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        JButton browseButton = new JButton("Change Image...");
        JLabel imagePreviewLabel = new JLabel();
        imagePreviewLabel.setPreferredSize(new Dimension(150, 150));
        imagePreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imagePreviewLabel.setBorder(BorderFactory.createLineBorder(ThemeColors.SECONDARY));
        imagePreviewLabel.setBackground(ThemeColors.CARD_BG);
        imagePreviewLabel.setOpaque(true);

        // Use the Icon directly from the table model for the initial preview
        if (currentIcon != null && isIconValid(currentIcon)) {
            Image image = currentIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            imagePreviewLabel.setIcon(new ImageIcon(image));
        } else {
             imagePreviewLabel.setText("No Image");
        }

        final File[] selectedImageFile = {null}; // Holds the *newly* selected file, if any
        // Store the original path to know if it changed
        final String[] originalImagePath = {currentImagePath};

        browseButton.addActionListener(e -> {
             JFileChooser fileChooser = new JFileChooser();
             // ... (file chooser setup as in addProduct) ...
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

        // --- Layout fields using GridBagLayout (same as addProduct) ---
         gbc.gridx = 0; gbc.gridy = 0; fieldsPanel.add(createFormLabel("Name:"), gbc);
         gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; fieldsPanel.add(nameField, gbc);
         gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; // Reset

         gbc.gridx = 0; gbc.gridy++; fieldsPanel.add(createFormLabel("Price (₱):"), gbc);
         gbc.gridx = 1; fieldsPanel.add(priceField, gbc);

         gbc.gridx = 0; gbc.gridy++; fieldsPanel.add(createFormLabel("Stock:"), gbc);
         gbc.gridx = 1; fieldsPanel.add(stockField, gbc);

         gbc.gridx = 0; gbc.gridy++; fieldsPanel.add(createFormLabel("Reorder Lvl:"), gbc);
         gbc.gridx = 1; fieldsPanel.add(reorderLevelField, gbc);

         gbc.gridx = 0; gbc.gridy++; fieldsPanel.add(createFormLabel("Group Name:"), gbc);
         gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; fieldsPanel.add(groupNameField, gbc);
         gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;

         gbc.gridx = 0; gbc.gridy++; gbc.anchor = GridBagConstraints.NORTHWEST; fieldsPanel.add(createFormLabel("Description:"), gbc);
         gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0; fieldsPanel.add(descriptionScroll, gbc);
         gbc.fill = GridBagConstraints.NONE; gbc.weighty = 0.0; gbc.anchor = GridBagConstraints.WEST; gbc.gridwidth = 1;

         gbc.gridx = 0; gbc.gridy++; fieldsPanel.add(createFormLabel("Image:"), gbc);
         gbc.gridx = 1; fieldsPanel.add(browseButton, gbc);
         gbc.gridx = 2; fieldsPanel.add(imagePathLabel, gbc);

        JPanel previewContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        previewContainer.setBackground(ThemeColors.BACKGROUND);
        previewContainer.add(imagePreviewLabel);
         previewContainer.setBorder(BorderFactory.createTitledBorder(
              BorderFactory.createEmptyBorder(10, 0, 10, 0),
              "Image Preview", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION,
              new Font("Arial", Font.PLAIN, 12), ThemeColors.TEXT));


        formPanel.add(fieldsPanel);
        formPanel.add(previewContainer);

        // --- Button panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(ThemeColors.BACKGROUND);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton saveButton = new JButton("Save Changes");
        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String priceStr = priceField.getText().trim();
                String stockStr = stockField.getText().trim();
                String reorderStr = reorderLevelField.getText().trim();
                String groupName = groupNameField.getText().trim();
                String description = descriptionArea.getText().trim();

                if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty() || groupName.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Name, Price, Stock, and Group Name are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double price = Double.parseDouble(priceStr);
                int stock = Integer.parseInt(stockStr);
                int reorderLevel = reorderStr.isEmpty() ? 0 : Integer.parseInt(reorderStr);

                String finalImagePath = originalImagePath[0]; // Start with the original path

                // Check if a *new* image file was selected
                if (selectedImageFile[0] != null) {
                    // --- Save the new image (same logic as addProduct) ---
                    File sourceFile = selectedImageFile[0];
                    File imagesDir = new File("images/products");
                    if (!imagesDir.exists()) {
                         if (!imagesDir.mkdirs()) {
                             System.err.println("Failed to create product images directory: " + imagesDir.getAbsolutePath());
                             JOptionPane.showMessageDialog(dialog,
                                 "Could not create directory for images.",
                                 "Directory Error", JOptionPane.ERROR_MESSAGE);
                             return;
                         }
                    }

                    String extension = "";
                    int i = sourceFile.getName().lastIndexOf('.');
                    if (i > 0) extension = sourceFile.getName().substring(i);
                    String safeName = name.replaceAll("[^a-zA-Z0-9.\\-_ ]", "_"); // Allow spaces and hyphens too
                    String newFileName = "product_" + safeName + "_" + System.currentTimeMillis() + extension;
                    File destinationFile = new File(imagesDir, newFileName);

                    try {
                        Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        finalImagePath = imagesDir.getPath().replace("\\", "/") + "/" + newFileName; // Update path
                        System.out.println("Updated image saved to: " + finalImagePath);

                        // --- Optional: Delete old image file ---
                        if (originalImagePath[0] != null && !originalImagePath[0].equals(finalImagePath)) {
                             File oldFile = new File(originalImagePath[0]);
                             if (oldFile.exists() && !oldFile.isDirectory()) { // Check if it's a file
                                 try {
                                     if (oldFile.delete()) {
                                         System.out.println("Deleted old image file: " + originalImagePath[0]);
                                     } else {
                                         System.err.println("Could not delete old image file: " + originalImagePath[0]);
                                     }
                                 } catch (SecurityException se) {
                                     System.err.println("Security error deleting old image file: " + se.getMessage());
                                 }
                             }
                        }
                        // --- End Optional Delete ---

                    } catch (IOException ioEx) {
                        System.err.println("Error copying updated image file: " + ioEx.getMessage());
                        JOptionPane.showMessageDialog(dialog,
                            "Could not save the updated image file: " + ioEx.getMessage(),
                            "File Copy Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    // --- End Image Saving ---
                }

                // Update the product in the database
                // Pass the final product ID from the outer scope
                if (updateProduct(productId, name, price, stock, groupName, description, finalImagePath, reorderLevel)) {
                    // Reload products to refresh the entire table with new data/images
                    loadProducts();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this, "Product updated successfully!");
                } else {
                     JOptionPane.showMessageDialog(dialog, "Failed to update product in database.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers for Price, Stock, and Reorder Level.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                 ex.printStackTrace();
                 JOptionPane.showMessageDialog(dialog, "An unexpected error occurred during update: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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


    // Updated updateProduct to include description and reorder level
    private boolean updateProduct(int productId, String name, double price, int stock,
                                  String groupName, String description, String imagePath, int reorderLevel) {
        // Added description and reorder_level to UPDATE
        String sql = "UPDATE products SET name = ?, price = ?, stock = ?, group_name = ?, " +
                     "description = ?, image_path = ?, reorder_level = ? WHERE id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setDouble(2, price);
            stmt.setInt(3, stock);
            stmt.setString(4, groupName);
            stmt.setString(5, description); // Set description
            if (imagePath != null && !imagePath.isEmpty()) { // Check if not empty too
                stmt.setString(6, imagePath);
            } else {
                stmt.setNull(6, Types.VARCHAR);
            }
             stmt.setInt(7, reorderLevel); // Set reorder level
             stmt.setInt(8, productId); // WHERE clause

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error updating product in database: " + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }


    private void deleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        final int productId = (int) productTableModel.getValueAt(selectedRow, 0); // FIX: Make final
        String productName = (String) productTableModel.getValueAt(selectedRow, 2);
         String imagePath = productTableModel.getImagePath(selectedRow); // Get path for potential deletion
         final int rowToDelete = selectedRow; // FIX: Make final copy for lambda

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete '" + productName + "' (ID: " + productId + ")?\n" +
            "This action cannot be undone and will remove related order items (check constraints).", // Added warning
            "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // Consider foreign key constraints - deleting product might fail if it exists in orders
            // You might need to handle this more gracefully (e.g., soft delete, prevent deletion, delete order items first)

            try (Connection conn = DBConnection.connect();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM products WHERE id = ?")) {

                stmt.setInt(1, productId); // Use final variable
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                     // Attempt to delete the associated image file
                     if (imagePath != null && !imagePath.isEmpty()) {
                         try {
                             File imageFile = new File(imagePath);
                             if (imageFile.exists() && !imageFile.isDirectory()) { // Check it's a file
                                 if (imageFile.delete()) {
                                     System.out.println("Deleted image file: " + imagePath);
                                 } else {
                                     System.err.println("Failed to delete image file (check permissions/usage): " + imagePath);
                                     // Optional: Warn the user the file wasn't deleted
                                     // JOptionPane.showMessageDialog(this, "Database entry deleted, but could not delete image file:\n" + imagePath,
                                     //                        "File Deletion Warning", JOptionPane.WARNING_MESSAGE);
                                 }
                             } else {
                                 System.out.println("Image file not found or is a directory, skipping deletion: " + imagePath);
                             }
                         } catch (SecurityException se) {
                             System.err.println("Security error deleting image file " + imagePath + ": " + se.getMessage());
                             JOptionPane.showMessageDialog(this, "Could not delete image file due to security restrictions:\n" + imagePath,
                                                    "File Deletion Error", JOptionPane.ERROR_MESSAGE);
                         } catch (Exception e) {
                              System.err.println("Error attempting to delete image file " + imagePath + ": " + e.getMessage());
                         }
                     }

                    // Remove from table model AFTER successful DB deletion
                    // Ensure this runs on EDT if updates are frequent
                     SwingUtilities.invokeLater(() -> {
                          // Check if the row still exists before removing
                          // Add robust check for table size
                           if (productTableModel != null && rowToDelete >= 0 && rowToDelete < productTableModel.getRowCount()) {
                                // Verify the product ID at that row still matches before removing
                                try {
                                    if ((int)productTableModel.getValueAt(rowToDelete, 0) == productId) {
                                        productTableModel.removeRow(rowToDelete); // Use final row index
                                    } else {
                                        // Row content changed unexpectedly, reload the table for safety
                                        System.err.println("Product ID mismatch at row " + rowToDelete + " during deletion. Reloading table.");
                                        loadProducts();
                                    }
                                } catch (ArrayIndexOutOfBoundsException aioobe) {
                                     System.err.println("Row index " + rowToDelete + " out of bounds during deletion. Reloading table.");
                                     loadProducts();
                                }
                           } else {
                               // Row index out of bounds or model is null, reload might be safest
                               System.err.println("Invalid row index " + rowToDelete + " or null model during deletion. Reloading table.");
                               if(productTableModel != null) loadProducts(); // Reload only if model exists
                           }
                     });

                    JOptionPane.showMessageDialog(this, "Product '" + productName + "' deleted successfully!");
                    // No need to call loadProducts() here as we removed the row directly (unless using the reload fallback)
                } else {
                     JOptionPane.showMessageDialog(this, "Product could not be deleted (maybe it was already removed?).", "Deletion Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                 // Provide more specific error for foreign key constraint violation
                if (ex.getMessage().toLowerCase().contains("foreign key constraint")) {
                     JOptionPane.showMessageDialog(this,
                         "Error deleting product: Cannot delete '" + productName + "' because it is referenced in existing orders or other records.\n" +
                         "Consider marking the product as inactive instead.",
                         "Deletion Error (Constraint Violation)", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Error deleting product from database: " + ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

     private void updateStock() {
         int selectedRow = inventoryTable.getSelectedRow();
         if (selectedRow < 0) {
             JOptionPane.showMessageDialog(this, "Please select a product from the Inventory table.", "Selection Error", JOptionPane.WARNING_MESSAGE);
             return;
         }

         final int productId = (int) inventoryTableModel.getValueAt(selectedRow, 0); // FIX: Make final
         String productName = (String) inventoryTableModel.getValueAt(selectedRow, 1);
         int currentStock = (int) inventoryTableModel.getValueAt(selectedRow, 2);
         int currentReorder = (int) inventoryTableModel.getValueAt(selectedRow, 3);
         final int rowToUpdate = selectedRow; // FIX: Make final copy

         // --- Create a more informative dialog ---
         JDialog dialog = new JDialog(this, "Update Stock & Reorder Level", true);
         dialog.setLayout(new BorderLayout());
         dialog.getContentPane().setBackground(ThemeColors.BACKGROUND);
         dialog.setSize(400, 250); // Increased size
         dialog.setLocationRelativeTo(this);

         JPanel panel = new JPanel(new GridBagLayout()); // Use GridBagLayout
         panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
         panel.setBackground(ThemeColors.BACKGROUND);
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.insets = new Insets(5, 5, 5, 5);
         gbc.anchor = GridBagConstraints.WEST;

         // Product Name Label
         gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
         JLabel nameLabel = new JLabel("Product: " + productName + " (ID: " + productId + ")");
         nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
         panel.add(nameLabel, gbc);
         gbc.gridwidth = 1; // Reset

         // Current Stock Label
         gbc.gridx = 0; gbc.gridy++;
         panel.add(new JLabel("Current Stock:"), gbc);
         gbc.gridx = 1;
         JSpinner stockSpinner = new JSpinner(new SpinnerNumberModel(currentStock, 0, 10000, 1));
         panel.add(stockSpinner, gbc);

         // Current Reorder Level Label
         gbc.gridx = 0; gbc.gridy++;
         panel.add(new JLabel("Reorder Level:"), gbc);
         gbc.gridx = 1;
         JSpinner reorderSpinner = new JSpinner(new SpinnerNumberModel(currentReorder, 0, 10000, 1));
         panel.add(reorderSpinner, gbc);


         // --- Buttons ---
         JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
         buttonPanel.setBackground(ThemeColors.BACKGROUND);

         JButton updateButton = new JButton("Update");
         updateButton.addActionListener(e -> {
             int newStock = (int) stockSpinner.getValue();
             int newReorder = (int) reorderSpinner.getValue();
             // Use final productId
             if (updateProductStockAndReorder(productId, newStock, newReorder)) {
                 // Update the inventory table model directly using final row index
                 inventoryTableModel.setValueAt(newStock, rowToUpdate, 2);
                 inventoryTableModel.setValueAt(newReorder, rowToUpdate, 3);

                  // Also update the products table if it's currently visible and loaded
                  if (productTable != null && productTableModel != null) {
                     for (int i = 0; i < productTableModel.getRowCount(); i++) {
                          // Use final productId
                         if ((int) productTableModel.getValueAt(i, 0) == productId) {
                             productTableModel.setValueAt(newStock, i, 4); // Stock is at index 4 in productTableModel
                             // Reorder level isn't typically in product table, but update if it were
                             break;
                         }
                     }
                 }

                 dialog.dispose();
                 JOptionPane.showMessageDialog(this, "Stock and Reorder Level updated successfully!");
             } else {
                  JOptionPane.showMessageDialog(dialog, "Failed to update stock/reorder level in database.", "Database Error", JOptionPane.ERROR_MESSAGE);
             }
         });

         JButton cancelButton = new JButton("Cancel");
         cancelButton.addActionListener(e -> dialog.dispose());

         buttonPanel.add(cancelButton);
         buttonPanel.add(updateButton);

         // Add components to dialog
         dialog.add(panel, BorderLayout.CENTER);
         dialog.add(buttonPanel, BorderLayout.SOUTH);
         dialog.setVisible(true);
     }

    // Updated DB method for stock and reorder level
    private boolean updateProductStockAndReorder(int productId, int newStock, int newReorderLevel) {
        String sql = "UPDATE products SET stock = ?, reorder_level = ? WHERE id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newStock);
            stmt.setInt(2, newReorderLevel);
            stmt.setInt(3, productId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error updating stock/reorder level: " + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void generateRestockList() {
         try (Connection conn = DBConnection.connect();
              Statement stmt = conn.createStatement();
              ResultSet rs = stmt.executeQuery(
                  "SELECT name, stock, COALESCE(reorder_level, 0) as reorder_level FROM products WHERE stock < COALESCE(reorder_level, 0)")) {

             StringBuilder restockList = new StringBuilder();
             restockList.append("--- Products Needing Restock ---\n\n");
             int count = 0;

             while (rs.next()) {
                 count++;
                 restockList.append(String.format("%-30s - Current: %-5d | Reorder At: %-5d\n",
                                                  rs.getString("name"),
                                                  rs.getInt("stock"),
                                                  rs.getInt("reorder_level")));
             }

             if (count == 0) {
                 restockList.append("All products are currently above their reorder levels.");
             }

             JTextArea textArea = new JTextArea(restockList.toString());
             textArea.setEditable(false);
             textArea.setFont(new Font("Monospaced", Font.PLAIN, 13)); // Monospaced for alignment
             textArea.setBackground(ThemeColors.BACKGROUND);
             textArea.setForeground(ThemeColors.TEXT);
             textArea.setRows(15); // Set preferred rows
             textArea.setColumns(60); // Set preferred columns

             JScrollPane scrollPane = new JScrollPane(textArea);
             scrollPane.setPreferredSize(new Dimension(600, 400)); // Set preferred dialog size

             JOptionPane.showMessageDialog(this, scrollPane, "Restock List (" + count + " items)", JOptionPane.INFORMATION_MESSAGE);

         } catch (SQLException ex) {
             ex.printStackTrace();
             JOptionPane.showMessageDialog(this,
                 "Error generating restock list: " + ex.getMessage(),
                 "Error", JOptionPane.ERROR_MESSAGE);
         }
    }

    private void editUser() {
         int selectedRow = userTable.getSelectedRow();
         if (selectedRow < 0) {
             JOptionPane.showMessageDialog(this, "Please select a user to edit.", "Selection Error", JOptionPane.WARNING_MESSAGE);
             return;
         }

         // Get data from the table model
         final int userId = (int) userTableModel.getValueAt(selectedRow, 0); // FIX: Make final
         String currentName = (String) userTableModel.getValueAt(selectedRow, 1);
         String currentEmail = (String) userTableModel.getValueAt(selectedRow, 2);
         String currentRole = (String) userTableModel.getValueAt(selectedRow, 3);
         int currentPoints = (int) userTableModel.getValueAt(selectedRow, 4);
         final int rowToUpdate = selectedRow; // FIX: Make final copy

         JDialog dialog = new JDialog(this, "Edit User (ID: " + userId + ")", true);
         dialog.setLayout(new BorderLayout());
         dialog.getContentPane().setBackground(ThemeColors.BACKGROUND);
         dialog.setSize(450, 300); // Slightly wider
         dialog.setLocationRelativeTo(this);

         JPanel formPanel = new JPanel(new GridBagLayout()); // Use GridBagLayout
         formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
         formPanel.setBackground(ThemeColors.BACKGROUND);
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.insets = new Insets(8, 8, 8, 8); // More padding
         gbc.anchor = GridBagConstraints.WEST;

         JTextField nameField = new JTextField(currentName, 20); // Set width hint
         JTextField emailField = new JTextField(currentEmail, 20);
         JComboBox<String> roleCombo = new JComboBox<>(new String[]{"customer", "admin"});
         roleCombo.setSelectedItem(currentRole);
         // Use JSpinner for points for better number input
         JSpinner pointsSpinner = new JSpinner(new SpinnerNumberModel(currentPoints, 0, 100000, 1));

         gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Name:"), gbc);
         gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; formPanel.add(nameField, gbc);

         gbc.gridx = 0; gbc.gridy++; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0; formPanel.add(new JLabel("Email:"), gbc);
         gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; formPanel.add(emailField, gbc);

         gbc.gridx = 0; gbc.gridy++; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0; formPanel.add(new JLabel("Role:"), gbc);
         gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; formPanel.add(roleCombo, gbc); // Don't fill horizontally

         gbc.gridx = 0; gbc.gridy++; gbc.anchor = GridBagConstraints.WEST; formPanel.add(new JLabel("Loyalty Points:"), gbc);
         gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; formPanel.add(pointsSpinner, gbc);

         // --- Button Panel ---
         JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
         buttonPanel.setBackground(ThemeColors.BACKGROUND);

         JButton saveButton = new JButton("Save Changes");
         saveButton.addActionListener(e -> {
             try {
                 String name = nameField.getText().trim();
                 String email = emailField.getText().trim();
                 String role = (String) roleCombo.getSelectedItem();
                 int points = (int) pointsSpinner.getValue();

                 // Basic validation
                 if (name.isEmpty() || email.isEmpty()) {
                     JOptionPane.showMessageDialog(dialog, "Name and Email cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                     return;
                 }
                 // Simple email format check (can be more robust)
                 if (!email.contains("@") || !email.contains(".")) {
                      JOptionPane.showMessageDialog(dialog, "Please enter a valid email address.", "Input Error", JOptionPane.ERROR_MESSAGE);
                     return;
                 }

                 // Use final userId
                 if (updateUser(userId, name, email, role, points)) {
                     // Update the table model directly using final row index
                      if (rowToUpdate >= 0 && rowToUpdate < userTableModel.getRowCount()) {
                         userTableModel.setValueAt(name, rowToUpdate, 1);
                         userTableModel.setValueAt(email, rowToUpdate, 2);
                         userTableModel.setValueAt(role, rowToUpdate, 3);
                         userTableModel.setValueAt(points, rowToUpdate, 4);
                      } else {
                          loadUsers(); // Reload if row index is invalid
                      }
                     dialog.dispose();
                     JOptionPane.showMessageDialog(this, "User updated successfully!");
                 } else {
                      JOptionPane.showMessageDialog(dialog, "Failed to update user in database.", "Database Error", JOptionPane.ERROR_MESSAGE);
                 }
             } catch (NumberFormatException ex) {
                 // Should not happen with JSpinner, but good practice
                 JOptionPane.showMessageDialog(dialog,
                     "Invalid number format for loyalty points.",
                     "Error", JOptionPane.ERROR_MESSAGE);
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

    private boolean updateUser(int userId, String name, String email, String role, int points) {
         try (Connection conn = DBConnection.connect();
              PreparedStatement stmt = conn.prepareStatement(
                  "UPDATE customers SET name = ?, email = ?, role = ?, loyalty_points = ? WHERE id = ?")) {

             stmt.setString(1, name);
             stmt.setString(2, email);
             stmt.setString(3, role);
             stmt.setInt(4, points);
             stmt.setInt(5, userId);

             int rowsAffected = stmt.executeUpdate();
             return rowsAffected > 0;
         } catch (SQLException ex) {
             ex.printStackTrace();
              // Check for duplicate email error (adjust error code/message check if needed)
             if (ex.getMessage().toLowerCase().contains("duplicate") && ex.getMessage().toLowerCase().contains("email")) {
                   JOptionPane.showMessageDialog(this,
                     "Error updating user: Email address '" + email + "' is already in use.",
                     "Update Error", JOptionPane.ERROR_MESSAGE);
             } else {
                 JOptionPane.showMessageDialog(this,
                     "Error updating user: " + ex.getMessage(),
                     "Database Error", JOptionPane.ERROR_MESSAGE);
             }
             return false;
         }
    }

    private void filterSales(String status) {
        loadSales(status);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to log out?",
            "Logout Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE); // Use question icon
        if (confirm == JOptionPane.YES_OPTION) {
            // Clear any sensitive data if necessary before logging out
            dispose(); // Close the admin frame
            SwingUtilities.invokeLater(LoginFrame::new); // Open login frame on EDT
        }
    }

     // Helper method for form labels (reused in add/edit product/user)
     private JLabel createFormLabel(String text) {
         JLabel label = new JLabel(text);
         label.setFont(new Font("Arial", Font.BOLD, 13)); // Slightly smaller
         label.setForeground(ThemeColors.TEXT);
         return label;
     }

    // --- Methods dependent on lambda fix ---

    private void updateOrderStatus() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an order first.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // FIX: Make ID effectively final for lambda
        final int orderId;
        Object idObj = ordersTableModel.getValueAt(selectedRow, 0);
        if (idObj instanceof Integer) {
            orderId = (Integer) idObj;
        } else {
             JOptionPane.showMessageDialog(this, "Invalid Order ID data.", "Data Error", JOptionPane.ERROR_MESSAGE);
             return;
        }
        final int rowToUpdate = selectedRow; // FIX: Make final copy

        String currentStatus = (String) ordersTableModel.getValueAt(selectedRow, 4);

        JDialog dialog = new JDialog(this, "Update Order Status (ID: " + orderId + ")", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(ThemeColors.BACKGROUND);
        dialog.setSize(400, 180);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(ThemeColors.BACKGROUND);

        JLabel currentLabel = new JLabel("Current Status: " + currentStatus);
        currentLabel.setForeground(ThemeColors.TEXT);
        panel.add(currentLabel);

        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Processing", "Shipped", "Delivered", "Completed", "Cancelled"}); // Possible statuses
        statusCombo.setSelectedItem(currentStatus); // Default to current
        panel.add(statusCombo);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(ThemeColors.BACKGROUND);

        JButton updateButton = new JButton("Update Status");
        updateButton.addActionListener(e -> {
            String newStatus = (String) statusCombo.getSelectedItem();
            // Use final orderId and rowToUpdate inside lambda
            if (updateOrderStatusInDB(orderId, newStatus)) {
                 if (rowToUpdate >= 0 && rowToUpdate < ordersTableModel.getRowCount()) {
                    ordersTableModel.setValueAt(newStatus, rowToUpdate, 4); // Update table model
                 } else {
                    loadOrders(); // Reload if row is invalid
                 }
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Order status updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                // loadOrders(); // Optional: reload all orders if status affects other views
            } else {
                 JOptionPane.showMessageDialog(dialog, "Failed to update order status.", "Database Error", JOptionPane.ERROR_MESSAGE);
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

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error updating order status in database: " + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void viewOrderDetails() {
         int selectedRow = ordersTable.getSelectedRow();
         if (selectedRow < 0) {
             JOptionPane.showMessageDialog(this, "Please select an order to view details.", "Selection Error", JOptionPane.WARNING_MESSAGE);
             return;
         }

         // FIX: Make final for potential later use in lambdas (though not used here currently)
         final int orderId;
         Object idObj = ordersTableModel.getValueAt(selectedRow, 0);
         if (idObj instanceof Integer) {
            orderId = (Integer) idObj;
         } else {
              JOptionPane.showMessageDialog(this, "Invalid Order ID data.", "Data Error", JOptionPane.ERROR_MESSAGE);
              return;
         }

         JDialog dialog = new JDialog(this, "Order Details #" + orderId, true);
         dialog.setLayout(new BorderLayout());
         dialog.getContentPane().setBackground(ThemeColors.BACKGROUND);
         dialog.setSize(600, 450); // Wider and taller for details
         dialog.setLocationRelativeTo(this);

         // --- Top Panel for Customer Info ---
         JPanel infoPanel = new JPanel(new GridLayout(2, 2, 10, 5)); // 2x2 grid
         infoPanel.setBorder(BorderFactory.createCompoundBorder(
             BorderFactory.createTitledBorder(
                 BorderFactory.createLineBorder(ThemeColors.SECONDARY),
                 "Order Information", TitledBorder.LEFT, TitledBorder.TOP,
                 new Font("Arial", Font.BOLD, 14), ThemeColors.PRIMARY),
             BorderFactory.createEmptyBorder(10, 10, 10, 10))
         );
         infoPanel.setBackground(ThemeColors.BACKGROUND);

         // Fetch customer info and order details
         try (Connection conn = DBConnection.connect();
              PreparedStatement custStmt = conn.prepareStatement(
                  "SELECT c.name, c.email, o.order_date, o.status, o.total_price " +
                  "FROM orders o JOIN customers c ON o.customer_id = c.id " +
                  "WHERE o.id = ?")) {

             custStmt.setInt(1, orderId); // Use final ID
             ResultSet custRs = custStmt.executeQuery();

             if (custRs.next()) {
                 infoPanel.add(createFormLabel("Customer:"));
                 infoPanel.add(new JLabel(custRs.getString("name")));
                 infoPanel.add(createFormLabel("Order Date:"));
                 infoPanel.add(new JLabel(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(custRs.getTimestamp("order_date"))));
                 infoPanel.add(createFormLabel("Email:"));
                 infoPanel.add(new JLabel(custRs.getString("email")));
                 infoPanel.add(createFormLabel("Status:"));
                 infoPanel.add(new JLabel(custRs.getString("status")));
                 // You could add total price here too if desired
             }
         } catch (SQLException ex) {
             infoPanel.add(new JLabel("Error loading customer info: " + ex.getMessage()));
         }
         dialog.add(infoPanel, BorderLayout.NORTH);


         // --- Center Panel for Order Items Table ---
         DefaultTableModel itemsModel = new DefaultTableModel(
             new String[]{"Product", "Quantity", "Unit Price", "Item Total"}, 0) {
             @Override public boolean isCellEditable(int row, int column) { return false; }
         };
         JTable detailsTable = new JTable(itemsModel);
         styleTable(detailsTable); // Apply styling
         detailsTable.getColumnModel().getColumn(0).setPreferredWidth(250); // Product name wider
         detailsTable.getColumnModel().getColumn(1).setPreferredWidth(70); // Qty
         detailsTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Unit Price
         detailsTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Item Total


         try (Connection conn = DBConnection.connect();
              PreparedStatement stmt = conn.prepareStatement(
                  "SELECT p.name, oi.quantity, oi.price " +
                  "FROM order_items oi JOIN products p ON oi.product_id = p.id " +
                  "WHERE oi.order_id = ?")) {

             stmt.setInt(1, orderId); // Use final ID
             ResultSet rs = stmt.executeQuery();

             while (rs.next()) {
                 double unitPrice = rs.getDouble("price");
                 int quantity = rs.getInt("quantity");
                 itemsModel.addRow(new Object[]{
                     rs.getString("name"),
                     quantity,
                     String.format("₱%.2f", unitPrice),
                     String.format("₱%.2f", unitPrice * quantity)
                 });
             }
         } catch (SQLException ex) {
             ex.printStackTrace();
             JOptionPane.showMessageDialog(dialog, "Error loading order items: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
         }

         JScrollPane scrollPane = new JScrollPane(detailsTable);
         scrollPane.setBorder(BorderFactory.createLineBorder(ThemeColors.SECONDARY)); // Add border to scrollpane
         dialog.add(scrollPane, BorderLayout.CENTER);

         // --- Bottom Panel for Close Button ---
         JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
         bottomPanel.setBackground(ThemeColors.BACKGROUND);
         JButton closeButton = new JButton("Close");
         closeButton.addActionListener(e -> dialog.dispose());
         bottomPanel.add(closeButton);
         dialog.add(bottomPanel, BorderLayout.SOUTH);

         dialog.setVisible(true);
     }

     private void processCancellation() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an order to process cancellation.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // FIX: Make final for lambda
        final int orderId;
        Object idObj = ordersTableModel.getValueAt(selectedRow, 0);
        if (idObj instanceof Integer) {
           orderId = (Integer) idObj;
        } else {
             JOptionPane.showMessageDialog(this, "Invalid Order ID data.", "Data Error", JOptionPane.ERROR_MESSAGE);
             return;
        }
        final int rowToProcess = selectedRow; // FIX: Make final copy

        String currentStatus = (String) ordersTableModel.getValueAt(selectedRow, 4);
        String cancellationRequest = (String) ordersTableModel.getValueAt(selectedRow, 5);

        if (!cancellationRequest.equalsIgnoreCase("Yes")) {
            JOptionPane.showMessageDialog(this,
                "This order does not have a pending cancellation request.",
                "No Request", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Prevent cancellation if already delivered or cancelled
        if (currentStatus.equalsIgnoreCase("Delivered") || currentStatus.equalsIgnoreCase("Completed") || currentStatus.equalsIgnoreCase("Cancelled")) {
            JOptionPane.showMessageDialog(this,
                "Cannot process cancellation for an order that is already " + currentStatus + ".",
                "Action Not Allowed", JOptionPane.WARNING_MESSAGE);
            // Optionally, automatically deny the request here
            // denyCancellation(orderId, selectedRow);
            return;
        }

        Object[] options = {"Approve Cancellation", "Deny Cancellation", "Cancel Action"};
        int choice = JOptionPane.showOptionDialog(this,
            "Order #" + orderId + " has a cancellation request.\n" +
            "Current Status: " + currentStatus + "\n\n" +
            "Choose an action:",
            "Process Cancellation Request",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[2]); // Default to Cancel Action

        switch (choice) {
            case 0: // Approve
                // Use final variables inside the call if needed (approveCancellation already takes them as args)
                approveCancellation(orderId, rowToProcess);
                break;
            case 1: // Deny
                 // Use final variables inside the call if needed (denyCancellation already takes them as args)
                denyCancellation(orderId, rowToProcess);
                break;
            // case 2 (Cancel Action) or closing dialog does nothing
        }
    }

     private void approveCancellation(int orderId, int tableRow) {
        // orderId and tableRow are passed in, so they are effectively final within this method's scope
        // No lambda issue expected *within* this method unless you add new lambdas inside it.

        Connection conn = null;
        try {
            conn = DBConnection.connect();
            conn.setAutoCommit(false); // Start transaction

            // 1. Update order status to Cancelled and clear request flag
            try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE orders SET status = 'Cancelled', cancellation_requested = 0 WHERE id = ?")) {
                stmt.setInt(1, orderId);
                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated == 0) {
                    throw new SQLException("Order not found or already processed.");
                }
            }

            // 2. Restock items: Iterate through order items and update product stock
            String selectItemsSql = "SELECT product_id, quantity FROM order_items WHERE order_id = ?";
            String updateStockSql = "UPDATE products SET stock = stock + ? WHERE id = ?";

            try (PreparedStatement selectStmt = conn.prepareStatement(selectItemsSql);
                 PreparedStatement updateStmt = conn.prepareStatement(updateStockSql)) {

                selectStmt.setInt(1, orderId);
                ResultSet rs = selectStmt.executeQuery();
                while (rs.next()) {
                    updateStmt.setInt(1, rs.getInt("quantity")); // Quantity to add back
                    updateStmt.setInt(2, rs.getInt("product_id")); // Product ID
                    updateStmt.addBatch(); // Add update to batch
                }
                updateStmt.executeBatch(); // Execute all stock updates
            }

            // 3. Handle potential refund logic here (placeholder)
            System.out.println("LOG: Refund processing initiated for cancelled order #" + orderId);


            conn.commit(); // Commit transaction

            // Update UI after successful transaction (ensure on EDT if needed)
             final int finalTableRow = tableRow; // Make another final copy if needed for SwingUtilities.invokeLater
             SwingUtilities.invokeLater(() -> {
                  if (finalTableRow >= 0 && finalTableRow < ordersTableModel.getRowCount()) {
                      // Verify the order ID at the row still matches before updating
                      if ((int)ordersTableModel.getValueAt(finalTableRow, 0) == orderId) {
                          ordersTableModel.setValueAt("Cancelled", finalTableRow, 4); // Update Status column
                          ordersTableModel.setValueAt("No", finalTableRow, 5);      // Update Request column
                      } else {
                          System.err.println("Order ID mismatch at row " + finalTableRow + " during approve cancellation update. Reloading orders.");
                          loadOrders(); // Reload if row changed or ID doesn't match
                      }
                 } else {
                     System.err.println("Invalid row index " + finalTableRow + " during approve cancellation update. Reloading orders.");
                     loadOrders(); // Reload if row index is invalid
                 }
             });


            JOptionPane.showMessageDialog(this,
                "Cancellation approved for Order #" + orderId + ".\nItems have been restocked.",
                "Cancellation Approved", JOptionPane.INFORMATION_MESSAGE);

            // Refresh inventory and product views if needed
             loadInventory();
             // loadProducts(); // Optional

        } catch (SQLException ex) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException e) { e.printStackTrace(); }
            }
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error approving cancellation for Order #" + orderId + ":\n" + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

     private void denyCancellation(int orderId, int tableRow) {
        // orderId and tableRow are passed in, no lambda issue expected here.
         String sql = "UPDATE orders SET cancellation_requested = 0 WHERE id = ?";
         try (Connection conn = DBConnection.connect();
              PreparedStatement stmt = conn.prepareStatement(sql)) {

             stmt.setInt(1, orderId);
             int rowsAffected = stmt.executeUpdate();

             if (rowsAffected > 0) {
                 // Update UI (ensure on EDT if needed)
                  final int finalTableRow = tableRow;
                  SwingUtilities.invokeLater(() -> {
                      if (finalTableRow >= 0 && finalTableRow < ordersTableModel.getRowCount()) {
                          // Verify the order ID at the row still matches
                          if ((int)ordersTableModel.getValueAt(finalTableRow, 0) == orderId) {
                              ordersTableModel.setValueAt("No", finalTableRow, 5); // Update Request column
                          } else {
                               System.err.println("Order ID mismatch at row " + finalTableRow + " during deny cancellation update. Reloading orders.");
                               loadOrders(); // Reload if row changed or ID doesn't match
                          }
                     } else {
                          System.err.println("Invalid row index " + finalTableRow + " during deny cancellation update. Reloading orders.");
                          loadOrders(); // Reload if row index is invalid
                     }
                  });

                 JOptionPane.showMessageDialog(this,
                     "Cancellation request for Order #" + orderId + " has been denied.\nThe order will proceed with its current status.",
                     "Cancellation Denied", JOptionPane.INFORMATION_MESSAGE);
             } else {
                  JOptionPane.showMessageDialog(this, "Could not find or update the order (it might have been processed already).", "Update Failed", JOptionPane.WARNING_MESSAGE);
             }

         } catch (SQLException ex) {
             ex.printStackTrace();
             JOptionPane.showMessageDialog(this,
                 "Error denying cancellation request for Order #" + orderId + ":\n" + ex.getMessage(),
                 "Database Error", JOptionPane.ERROR_MESSAGE);
         }
     }

    // Helper method to check if an ImageIcon loaded successfully and has valid dimensions
    private boolean isIconValid(ImageIcon icon) {
        if (icon == null) return false;
        // Ensure the image is fully loaded and has a positive width/height
        return icon.getImageLoadStatus() == MediaTracker.COMPLETE && icon.getIconWidth() > 0 && icon.getIconHeight() > 0;
    }

    public static void main(String[] args) {
        try {
            // Apply FlatLaf theme
            UIManager.setLookAndFeel(new FlatDarkLaf());
            // Optional: Customize theme properties further if needed
            // UIManager.put("TitlePane.background", ThemeColors.BACKGROUND);
            // UIManager.put("TitlePane.foreground", ThemeColors.TEXT);
            // ...
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatDarkLaf theme:");
            ex.printStackTrace();
            // Fallback to system L&F if FlatLaf fails
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Failed to set system LookAndFeel.");
            }
        }

        // Run the application on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new AdminFrame().setVisible(true);
        });
    }
}