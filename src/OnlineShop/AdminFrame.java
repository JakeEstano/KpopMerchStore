package OnlineShop;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import com.formdev.flatlaf.FlatDarkLaf;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

public class AdminFrame extends JFrame {
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JTable userTable, productTable, inventoryTable, salesTable, supplierTable, ordersTable;
    private DefaultTableModel userTableModel, productTableModel, inventoryTableModel, 
                            salesTableModel, supplierTableModel, ordersTableModel;


    private class ProductTableModel extends DefaultTableModel {
        private List<String> imagePaths = new ArrayList<>();

        public ProductTableModel() {
            super(new String[]{"ID", "Image", "Name", "Price", "Stock", "Group", "Actions"}, 0);
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return column == 1 ? ImageIcon.class : Object.class;
        }

        public void addRowWithImage(Object[] rowData, String imagePath) {
            super.addRow(rowData);
            imagePaths.add(imagePath);
        }

        public String getImagePath(int row) {
            return imagePaths.get(row);
        }

        public void setImagePath(int row, String path) {
            imagePaths.set(row, path);
        }

        @Override
        public void removeRow(int row) {
            super.removeRow(row);
            imagePaths.remove(row);
        }
    }

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
            ImageIcon icon = new ImageIcon(getClass().getResource("/icons/" + text.toLowerCase() + ".png"));
            button.setIcon(icon);
            button.setHorizontalTextPosition(SwingConstants.CENTER);
            button.setVerticalTextPosition(SwingConstants.CENTER);
            button.setIconTextGap(8);
        } catch (Exception e) {
            // Continue without icon if not found
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
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(ThemeColors.BUTTON_HOVER);
                button.setForeground(Color.WHITE);
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(ThemeColors.CARD_BG);
                button.setForeground(ThemeColors.TEXT);
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        return button;
    }

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

        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(ThemeColors.BACKGROUND);

        // Metrics panel
        JPanel metricsPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        metricsPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        metricsPanel.setBackground(ThemeColors.BACKGROUND);

        metricsPanel.add(createMetricCard("Total Products", getTotalProducts(), "images/products_icon.png"));
        metricsPanel.add(createMetricCard("Total Orders", getTotalOrders(), "images/orders_icon.png"));
        metricsPanel.add(createMetricCard("Low Stock Items", getLowStockItems(), "images/warning_icon.png"));
        metricsPanel.add(createMetricCard("Total Customers", getTotalCustomers(), "images/customers_icon.png"));
        metricsPanel.add(createMetricCard("Total Revenue", getTotalRevenue(), "images/revenue_icon.png"));
        metricsPanel.add(createMetricCard("Pending Orders", getPendingOrders(), "images/pending_icon.png"));

        contentPanel.add(metricsPanel, BorderLayout.NORTH);

        // Real-time Order Notifications Panel
        JPanel notificationsPanel = new JPanel(new BorderLayout());
        notificationsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(ThemeColors.SECONDARY),
            "New Order Notifications",
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

        JTable notificationsTable = new JTable(notificationsModel);
        styleTable(notificationsTable);
        notificationsTable.setRowHeight(30);

        // Load initial notifications
        loadOrderNotifications(notificationsModel);

        // Auto-refresh every 15 seconds
        Timer refreshTimer = new Timer(15000, e -> {
            int previousRowCount = notificationsModel.getRowCount();
            loadOrderNotifications(notificationsModel);

            // Show visual alert if new orders arrived
            if (notificationsModel.getRowCount() > previousRowCount) {
                Toolkit.getDefaultToolkit().beep();
                notificationsPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(ThemeColors.PRIMARY, 2),
                    "NEW ORDERS! (" + (notificationsModel.getRowCount() - previousRowCount) + ")",
                    TitledBorder.LEFT,
                    TitledBorder.TOP,
                    new Font("Arial", Font.BOLD, 16),
                    ThemeColors.PRIMARY
                ));

                // Reset border after 3 seconds
                new Timer(3000, ev -> {
                    notificationsPanel.setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(ThemeColors.SECONDARY),
                        "New Order Notifications",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Arial", Font.BOLD, 16),
                        ThemeColors.PRIMARY
                    ));
                    ((Timer)ev.getSource()).stop();
                }).start();
            }
        });
        refreshTimer.start();

        // Add view button
        JButton viewOrdersButton = new JButton("View All Orders");
        viewOrdersButton.addActionListener(e -> {
            cardLayout.show(mainPanel, "Orders");
            loadOrders();
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(viewOrdersButton);

        notificationsPanel.add(new JScrollPane(notificationsTable), BorderLayout.CENTER);
        notificationsPanel.add(buttonPanel, BorderLayout.SOUTH);

        contentPanel.add(notificationsPanel, BorderLayout.CENTER);
        dashboardPanel.add(contentPanel, BorderLayout.CENTER);

        return dashboardPanel;
    }
    
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
                "Error loading order notifications",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createMetricCard(String title, String value, String iconPath) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(ThemeColors.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 1, 1, 1, ThemeColors.SECONDARY),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Top panel with icon and title
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topPanel.setOpaque(false);

        JLabel icon = new JLabel(new ImageIcon(iconPath));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(ThemeColors.TEXT);

        topPanel.add(icon);
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
        userTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Role", "Loyalty Points"}, 0);
        userTable = new JTable(userTableModel);
        styleTable(userTable);
        
        // Add edit button
        JButton editUserButton = new JButton("Edit User");
        editUserButton.addActionListener(e -> editUser());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(editUserButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createProductsPanel() {
        JPanel panel = createTablePanel("Product Management");
        productTableModel = new ProductTableModel();
        productTable = new JTable(productTableModel);
        styleTable(productTable);

        // Set custom renderer for image column
        productTable.getColumnModel().getColumn(1).setCellRenderer(new ImageRenderer());
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

        panel.add(buttonPanel, BorderLayout.SOUTH);
        panel.add(new JScrollPane(productTable), BorderLayout.CENTER);
        return panel;
    }

    private class ImageRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = new JLabel();
            if (value != null) {
                ImageIcon icon = (ImageIcon) value;
                Image image = icon.getImage().getScaledInstance(
                    table.getRowHeight(), table.getRowHeight(), Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(image));
            }
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setOpaque(true);
            if (isSelected) {
                label.setBackground(table.getSelectionBackground());
            } else {
                label.setBackground(row % 2 == 0 ? ThemeColors.CARD_BG : 
                    new Color(ThemeColors.CARD_BG.getRed() - 10, 
                             ThemeColors.CARD_BG.getGreen() - 10, 
                             ThemeColors.CARD_BG.getBlue() - 10));
            }
            return label;
        }
    }

    private JPanel createInventoryPanel() {
        JPanel panel = createTablePanel("Inventory Management");
        inventoryTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Stock", "Reorder Level"}, 0);
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
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        panel.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSalesPanel() {
        JPanel panel = createTablePanel("Sales Reports");
        salesTableModel = new DefaultTableModel(new String[]{"Order ID", "Customer", "Product", "Quantity", "Total Price", "Date", "Status"}, 0);
        salesTable = new JTable(salesTableModel);
        styleTable(salesTable);
        
        // Add filter options
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setOpaque(false);
        
        JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All", "Processing", "Shipped", "Delivered", "Cancelled"});
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
        ordersTableModel = new DefaultTableModel(new String[]{"Order ID", "Customer", "Total", "Date", "Status", "Cancellation Request"}, 0) {
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
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        panel.add(new JScrollPane(ordersTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTablePanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeColors.BACKGROUND);

        // Title
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(ThemeColors.PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        return panel;
    }

    private void styleTable(JTable table) {
        table.setBackground(ThemeColors.CARD_BG);
        table.setForeground(ThemeColors.TEXT);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setGridColor(ThemeColors.SECONDARY);
        table.setSelectionBackground(ThemeColors.PRIMARY);
        table.setSelectionForeground(Color.WHITE);
        table.setRowHeight(30);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 1));

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(ThemeColors.PRIMARY);
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);

        // Alternating row colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, 
                    isSelected, hasFocus, row, column);

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? ThemeColors.CARD_BG : 
                        new Color(ThemeColors.CARD_BG.getRed() - 10, 
                                 ThemeColors.CARD_BG.getGreen() - 10, 
                                 ThemeColors.CARD_BG.getBlue() - 10));
                }
                return c;
            }
        });
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

    private void loadProducts() {
        productTableModel = new ProductTableModel();
        productTable.setModel(productTableModel);

        try (Connection conn = DBConnection.connect()) {
            boolean hasImageColumn = false;
            try (ResultSet columns = conn.getMetaData().getColumns(null, null, "products", "image_path")) {
                hasImageColumn = columns.next();
            }

            String query = hasImageColumn 
                ? "SELECT id, name, price, stock, group_name, image_path FROM products"
                : "SELECT id, name, price, stock, group_name FROM products";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    double price = rs.getDouble("price");
                    int stock = rs.getInt("stock");
                    String group = rs.getString("group_name");
                    String imagePath = hasImageColumn ? rs.getString("image_path") : null;

                    ImageIcon icon = (imagePath != null && !imagePath.isEmpty()) 
                        ? new ImageIcon(imagePath) 
                        : new ImageIcon("images/default_product.png");

                    ((ProductTableModel)productTableModel).addRowWithImage(
                        new Object[]{id, icon, name, price, stock, group, ""},
                        imagePath
                    );
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading products.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadInventory() {
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT p.id, p.name, p.stock, p.reorder_level FROM products p")) {
            inventoryTableModel.setRowCount(0);
            while (rs.next()) {
                inventoryTableModel.addRow(new Object[]{
                    rs.getInt("id"), 
                    rs.getString("name"), 
                    rs.getInt("stock"),
                    rs.getInt("reorder_level")
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
            String sql = "SELECT o.id, c.name, p.name, oi.quantity, o.total_price, o.order_date, o.status " +
                         "FROM orders o " +
                         "JOIN order_items oi ON o.id = oi.order_id " +
                         "JOIN products p ON oi.product_id = p.id " +
                         "JOIN customers c ON o.customer_id = c.id";
            
            if (!statusFilter.equals("All")) {
                sql += " WHERE o.status = ?";
            }
            
            sql += " ORDER BY o.order_date DESC";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            if (!statusFilter.equals("All")) {
                stmt.setString(1, statusFilter);
            }
            
            ResultSet rs = stmt.executeQuery();
            salesTableModel.setRowCount(0);
            while (rs.next()) {
                salesTableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("c.name"),
                    rs.getString("p.name"),
                    rs.getInt("quantity"),
                    rs.getDouble("total_price"),
                    rs.getDate("order_date"),
                    rs.getString("status")
                });
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
                    rs.getDouble("total_price"),
                    rs.getDate("order_date"),
                    rs.getString("status"),
                    rs.getString("cancellation_request")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading orders.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

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
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products WHERE stock < reorder_level")) {
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
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM customers")) {
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
                 "SELECT SUM(total_price) FROM orders WHERE status = 'Delivered'")) {

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double total = rs.getDouble(1);
                return "₱" + String.format("%.2f", total);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return "₱0";
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

    private void updateOrderStatus() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an order first", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int orderId = (int) ordersTableModel.getValueAt(selectedRow, 0);
        String currentStatus = (String) ordersTableModel.getValueAt(selectedRow, 4);

        JDialog dialog = new JDialog(this, "Update Order Status", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(ThemeColors.BACKGROUND);
        dialog.setSize(400, 200);

        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(ThemeColors.BACKGROUND);

        panel.add(new JLabel("Current Status: " + currentStatus));
        
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Processing", "Shipped", "Delivered"});
        panel.add(statusCombo);

        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(e -> {
            String newStatus = (String) statusCombo.getSelectedItem();
            if (updateOrderStatusInDB(orderId, newStatus)) {
                ordersTableModel.setValueAt(newStatus, selectedRow, 4);
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Order status updated successfully!");
            }
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(updateButton, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private boolean updateOrderStatusInDB(int orderId, String newStatus) {
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE orders SET status = ? WHERE id = ?")) {
            
            stmt.setString(1, newStatus);
            stmt.setInt(2, orderId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error updating order status: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void viewOrderDetails() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an order first", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int orderId = (int) ordersTableModel.getValueAt(selectedRow, 0);

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT p.name, oi.quantity, oi.price, (oi.quantity * oi.price) as total " +
                 "FROM order_items oi " +
                 "JOIN products p ON oi.product_id = p.id " +
                 "WHERE oi.order_id = ?")) {
            
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            DefaultTableModel model = new DefaultTableModel(
                new String[]{"Product", "Quantity", "Unit Price", "Total"}, 0);
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("name"),
                    rs.getInt("quantity"),
                    rs.getDouble("price"),
                    rs.getDouble("total")
                });
            }

            JDialog dialog = new JDialog(this, "Order Details #" + orderId, true);
            dialog.setLayout(new BorderLayout());
            dialog.getContentPane().setBackground(ThemeColors.BACKGROUND);
            dialog.setSize(500, 300);

            JTable detailsTable = new JTable(model);
            styleTable(detailsTable);
            
            dialog.add(new JScrollPane(detailsTable), BorderLayout.CENTER);
            
            // Add customer info
            try (PreparedStatement custStmt = conn.prepareStatement(
                "SELECT c.name, c.email, o.order_date, o.status " +
                "FROM orders o JOIN customers c ON o.customer_id = c.id " +
                "WHERE o.id = ?")) {
                
                custStmt.setInt(1, orderId);
                ResultSet custRs = custStmt.executeQuery();
                
                if (custRs.next()) {
                    JPanel infoPanel = new JPanel(new GridLayout(4, 2, 5, 5));
                    infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    infoPanel.setBackground(ThemeColors.BACKGROUND);
                    
                    infoPanel.add(new JLabel("Customer:"));
                    infoPanel.add(new JLabel(custRs.getString("name")));
                    
                    infoPanel.add(new JLabel("Email:"));
                    infoPanel.add(new JLabel(custRs.getString("email")));
                    
                    infoPanel.add(new JLabel("Order Date:"));
                    infoPanel.add(new JLabel(custRs.getDate("order_date").toString()));
                    
                    infoPanel.add(new JLabel("Status:"));
                    infoPanel.add(new JLabel(custRs.getString("status")));
                    
                    dialog.add(infoPanel, BorderLayout.NORTH);
                }
            }

            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading order details: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void processCancellation() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an order first", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int orderId = (int) ordersTableModel.getValueAt(selectedRow, 0);
        String currentStatus = (String) ordersTableModel.getValueAt(selectedRow, 4);
        String cancellationRequest = (String) ordersTableModel.getValueAt(selectedRow, 5);

        if (!cancellationRequest.equals("Yes")) {
            JOptionPane.showMessageDialog(this, 
                "This order doesn't have a cancellation request", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (currentStatus.equals("Delivered")) {
            JOptionPane.showMessageDialog(this, 
                "Cannot cancel an already delivered order", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Object[] options = {"Approve Cancellation", "Deny Cancellation", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this,
            "How would you like to process this cancellation request?",
            "Process Cancellation",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[2]);

        if (choice == 0) {
            approveCancellation(orderId, selectedRow);
        } else if (choice == 1) {
            denyCancellation(orderId, selectedRow);
        }
    }

    private void approveCancellation(int orderId, int tableRow) {
        try (Connection conn = DBConnection.connect()) {
            conn.setAutoCommit(false);
            
            try {
                // 1. Update order status to Cancelled
                try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE orders SET status = 'Cancelled', cancellation_requested = 0 WHERE id = ?")) {
                    stmt.setInt(1, orderId);
                    stmt.executeUpdate();
                }
                
                // 2. Restock items
                try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE products p " +
                    "JOIN order_items oi ON p.id = oi.product_id " +
                    "SET p.stock = p.stock + oi.quantity " +
                    "WHERE oi.order_id = ?")) {
                    stmt.setInt(1, orderId);
                    stmt.executeUpdate();
                }
                
                // 3. Process refund if payment was made
                try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE payments SET refunded = 1 WHERE order_id = ?")) {
                    stmt.setInt(1, orderId);
                    stmt.executeUpdate();
                }
                
                conn.commit();
                
                // Update UI
                ordersTableModel.setValueAt("Cancelled", tableRow, 4);
                ordersTableModel.setValueAt("No", tableRow, 5);
                
                JOptionPane.showMessageDialog(this, 
                    "Cancellation approved and items restocked. Refund processed if applicable.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error processing cancellation: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void denyCancellation(int orderId, int tableRow) {
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE orders SET cancellation_requested = 0 WHERE id = ?")) {
            
            stmt.setInt(1, orderId);
            stmt.executeUpdate();
            
            // Update UI
            ordersTableModel.setValueAt("No", tableRow, 5);
            
            JOptionPane.showMessageDialog(this, 
                "Cancellation request denied. Order will proceed as normal.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error denying cancellation: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addProduct() {
        JDialog dialog = new JDialog(this, "Add New Product", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(ThemeColors.BACKGROUND);
        dialog.setSize(600, 600);

        // Main form panel with padding
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(ThemeColors.BACKGROUND);

        // Form fields panel
        JPanel fieldsPanel = new JPanel(new GridLayout(0, 2, 15, 15));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));
        fieldsPanel.setBackground(ThemeColors.BACKGROUND);

        // Input fields
        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField stockField = new JTextField();
        JTextField groupNameField = new JTextField();
        JTextArea descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        JTextField supplierField = new JTextField();

        // Image upload components
        JLabel imagePathLabel = new JLabel("No image selected");
        imagePathLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JButton browseButton = new JButton("Browse...");
        JLabel imagePreviewLabel = new JLabel();
        imagePreviewLabel.setPreferredSize(new Dimension(200, 200));
        imagePreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imagePreviewLabel.setBorder(BorderFactory.createLineBorder(ThemeColors.SECONDARY));

        // Store the selected file path
        String[] selectedImagePath = {null};

        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Product Image");
            fileChooser.setAcceptAllFileFilterUsed(false);

            // Filter for image files
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Image files", "jpg", "jpeg", "png", "gif");
            fileChooser.addChoosableFileFilter(filter);

            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                selectedImagePath[0] = selectedFile.getAbsolutePath();
                imagePathLabel.setText(selectedFile.getName());

                // Display preview
                ImageIcon icon = new ImageIcon(selectedImagePath[0]);
                Image image = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                imagePreviewLabel.setIcon(new ImageIcon(image));
            }
        });

        // Add fields to form
        fieldsPanel.add(createFormLabel("Product Name:"));
        fieldsPanel.add(nameField);
        fieldsPanel.add(createFormLabel("Price:"));
        fieldsPanel.add(priceField);
        fieldsPanel.add(createFormLabel("Stock:"));
        fieldsPanel.add(stockField);
        fieldsPanel.add(createFormLabel("Group Name:"));
        fieldsPanel.add(groupNameField);
        fieldsPanel.add(createFormLabel("Description:"));
        fieldsPanel.add(descriptionScroll);
        fieldsPanel.add(createFormLabel("Product Image:"));
        fieldsPanel.add(browseButton);
        fieldsPanel.add(createFormLabel("Selected Image:"));
        fieldsPanel.add(imagePathLabel);

        formPanel.add(fieldsPanel);

        // Image preview panel
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(BorderFactory.createTitledBorder("Image Preview"));
        previewPanel.add(imagePreviewLabel, BorderLayout.CENTER);
        previewPanel.setBackground(ThemeColors.BACKGROUND);
        formPanel.add(previewPanel);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(ThemeColors.BACKGROUND);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton saveButton = new JButton("Save Product");
        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText();
                double price = Double.parseDouble(priceField.getText());
                int stock = Integer.parseInt(stockField.getText());
                String groupName = groupNameField.getText();
                String description = descriptionArea.getText();
                int supplierId = Integer.parseInt(supplierField.getText());

                // Validate required fields
                if (name.isEmpty() || groupName.isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                        "Product name and group name are required", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Handle image
                String imagePath = null;
                if (selectedImagePath[0] != null) {
                    // Create images directory if it doesn't exist
                    File imagesDir = new File("images/products");
                    if (!imagesDir.exists()) {
                        boolean dirsCreated = imagesDir.mkdirs();
                        if (!dirsCreated) {
                            throw new IOException("Failed to create directory: " + imagesDir.getAbsolutePath());
                        }
                    }

                    // Copy the selected image to our directory
                    File source = new File(selectedImagePath[0]);
                    String extension = selectedImagePath[0].substring(selectedImagePath[0].lastIndexOf("."));
                    String newFileName = "product_" + System.currentTimeMillis() + extension;
                    File destination = new File(imagesDir, newFileName);

                    Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    // Store relative path in database
                    imagePath = "images/products/" + newFileName;
                }

                if (saveNewProduct(name, price, stock, groupName, description, imagePath)) {
                    loadProducts();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this, "Product added successfully!");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter valid numbers for price and stock", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                    "Error saving product image: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(saveButton);

        // Add components to dialog
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private boolean saveNewProduct(String name, double price, int stock, 
                                    String groupName, String description, String imagePath) {
           Connection conn = null;
           try {
               conn = DBConnection.connect();
               conn.setAutoCommit(false); // Start transaction

               // Check for image column
               boolean hasImageColumn = false;
               try (ResultSet columns = conn.getMetaData().getColumns(null, null, "products", "image_path")) {
                   hasImageColumn = columns.next();
               }

               String sql;
               if (hasImageColumn) {
                   sql = "INSERT INTO products (name, price, stock, group_name, description, image_path) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
               } else {
                   sql = "INSERT INTO products (name, price, stock, group_name, description) " +
                        "VALUES (?, ?, ?, ?, ?)";
               }

               try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                   stmt.setString(1, name);
                   stmt.setDouble(2, price);
                   stmt.setInt(3, stock);
                   stmt.setString(4, groupName);
                   stmt.setString(5, description);

                   if (hasImageColumn) {
                       stmt.setString(6, imagePath != null ? imagePath : "");
                   }

                   int rowsAffected = stmt.executeUpdate();

                   if (rowsAffected > 0) {
                       conn.commit(); // Commit transaction
                       return true;
                   } else {
                       conn.rollback(); // Rollback if no rows affected
                       return false;
                   }
               }
           } catch (SQLException ex) {
               if (conn != null) {
                   try {
                       conn.rollback(); // Rollback on error
                   } catch (SQLException e) {
                       e.printStackTrace();
                   }
               }
               ex.printStackTrace();
               JOptionPane.showMessageDialog(this, 
                   "Error adding product: " + ex.getMessage(), 
                   "Error", JOptionPane.ERROR_MESSAGE);
               return false;
           } finally {
               if (conn != null) {
                   try {
                       conn.setAutoCommit(true); // Reset auto-commit
                       conn.close();
                   } catch (SQLException e) {
                       e.printStackTrace();
                   }
               }
           }
       }

    private void editProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product first", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int productId = (int) productTableModel.getValueAt(selectedRow, 0);
        String currentName = (String) productTableModel.getValueAt(selectedRow, 2);
        double currentPrice = (double) productTableModel.getValueAt(selectedRow, 3);
        int currentStock = (int) productTableModel.getValueAt(selectedRow, 4);
        String currentGroup = (String) productTableModel.getValueAt(selectedRow, 5);
        String currentImagePath = ((ProductTableModel)productTableModel).getImagePath(selectedRow);

        JDialog dialog = new JDialog(this, "Edit Product", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(ThemeColors.BACKGROUND);
        dialog.setSize(600, 500);

        // Main form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(ThemeColors.BACKGROUND);

        // Fields panel
        JPanel fieldsPanel = new JPanel(new GridLayout(0, 2, 15, 15));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));
        fieldsPanel.setBackground(ThemeColors.BACKGROUND);

        // Input fields
        JTextField nameField = new JTextField(currentName);
        JTextField priceField = new JTextField(String.valueOf(currentPrice));
        JTextField stockField = new JTextField(String.valueOf(currentStock));
        JTextField groupNameField = new JTextField(currentGroup);

        // Image components
        JLabel imagePathLabel = new JLabel(currentImagePath != null ? 
            new File(currentImagePath).getName() : "No image selected");
        imagePathLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JButton browseButton = new JButton("Browse...");
        JLabel imagePreviewLabel = new JLabel();
        imagePreviewLabel.setPreferredSize(new Dimension(200, 200));
        imagePreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imagePreviewLabel.setBorder(BorderFactory.createLineBorder(ThemeColors.SECONDARY));

        if (currentImagePath != null) {
            ImageIcon icon = new ImageIcon(currentImagePath);
            Image image = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            imagePreviewLabel.setIcon(new ImageIcon(image));
        }

        String[] selectedImagePath = {currentImagePath};

        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Product Image");
            fileChooser.setAcceptAllFileFilterUsed(false);
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Image files", "jpg", "jpeg", "png", "gif");
            fileChooser.addChoosableFileFilter(filter);

            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                selectedImagePath[0] = selectedFile.getAbsolutePath();
                imagePathLabel.setText(selectedFile.getName());

                ImageIcon icon = new ImageIcon(selectedImagePath[0]);
                Image image = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                imagePreviewLabel.setIcon(new ImageIcon(image));
            }
        });

        // Add fields to form
        fieldsPanel.add(createFormLabel("Product Name:"));
        fieldsPanel.add(nameField);
        fieldsPanel.add(createFormLabel("Price:"));
        fieldsPanel.add(priceField);
        fieldsPanel.add(createFormLabel("Stock:"));
        fieldsPanel.add(stockField);
        fieldsPanel.add(createFormLabel("Group Name:"));
        fieldsPanel.add(groupNameField);
        fieldsPanel.add(createFormLabel("Product Image:"));
        fieldsPanel.add(browseButton);
        fieldsPanel.add(createFormLabel("Selected Image:"));
        fieldsPanel.add(imagePathLabel);

        formPanel.add(fieldsPanel);

        // Image preview panel
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(BorderFactory.createTitledBorder("Image Preview"));
        previewPanel.add(imagePreviewLabel, BorderLayout.CENTER);
        previewPanel.setBackground(ThemeColors.BACKGROUND);
        formPanel.add(previewPanel);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(ThemeColors.BACKGROUND);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton saveButton = new JButton("Save Changes");
        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText();
                double price = Double.parseDouble(priceField.getText());
                int stock = Integer.parseInt(stockField.getText());
                String groupName = groupNameField.getText();

                // Validate required fields
                if (name.isEmpty() || groupName.isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                        "Product name and group name are required", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Handle image update
                String newImagePath = selectedImagePath[0];
                if (updateProduct(productId, name, price, stock, groupName, newImagePath)) {
                    // Update the table model
                    productTableModel.setValueAt(name, selectedRow, 2);
                    productTableModel.setValueAt(price, selectedRow, 3);
                    productTableModel.setValueAt(stock, selectedRow, 4);
                    productTableModel.setValueAt(groupName, selectedRow, 5);

                    // Update the image
                    ImageIcon icon = (newImagePath != null) 
                        ? new ImageIcon(newImagePath) 
                        : new ImageIcon("images/default_product.png");
                    productTableModel.setValueAt(icon, selectedRow, 1);
                    ((ProductTableModel)productTableModel).setImagePath(selectedRow, newImagePath);

                    dialog.dispose();
                    JOptionPane.showMessageDialog(this, "Product updated successfully!");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter valid numbers for price and stock", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(saveButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // Helper method to create consistent form labels
    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(ThemeColors.TEXT);
        return label;
    }
    private boolean updateProduct(int productId, String name, double price, int stock, 
                                String groupName, String imagePath) {
         try (Connection conn = DBConnection.connect()) {
             boolean hasImageColumn = false;
             try (ResultSet columns = conn.getMetaData().getColumns(null, null, "products", "image_path")) {
                 hasImageColumn = columns.next();
             }

             String sql;
             if (hasImageColumn) {
                 sql = "UPDATE products SET name = ?, price = ?, stock = ?, group_name = ?, image_path = ? WHERE id = ?";
             } else {
                 sql = "UPDATE products SET name = ?, price = ?, stock = ?, group_name = ? WHERE id = ?";
             }

             try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                 stmt.setString(1, name);
                 stmt.setDouble(2, price);
                 stmt.setInt(3, stock);
                 stmt.setString(4, groupName);

                 if (hasImageColumn) {
                     stmt.setString(5, imagePath);
                     stmt.setInt(6, productId);
                 } else {
                     stmt.setInt(5, productId);
                 }

                 int rowsAffected = stmt.executeUpdate();
                 return rowsAffected > 0;
             }
         } catch (SQLException ex) {
             ex.printStackTrace();
             JOptionPane.showMessageDialog(this, 
                 "Error updating product: " + ex.getMessage(), 
                 "Error", JOptionPane.ERROR_MESSAGE);
             return false;
         }
     }

    private void deleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product first", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int productId = (int) productTableModel.getValueAt(selectedRow, 0);
        String productName = (String) productTableModel.getValueAt(selectedRow, 2);

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete " + productName + "? This cannot be undone.", 
            "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM products WHERE id = ?")) {
                
                stmt.setInt(1, productId);
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    productTableModel.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(this, "Product deleted successfully!");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                    "Error deleting product: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateStock() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product first", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int productId = (int) inventoryTableModel.getValueAt(selectedRow, 0);
        String productName = (String) inventoryTableModel.getValueAt(selectedRow, 1);
        int currentStock = (int) inventoryTableModel.getValueAt(selectedRow, 2);

        JDialog dialog = new JDialog(this, "Update Stock", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(ThemeColors.BACKGROUND);
        dialog.setSize(300, 200);

        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(ThemeColors.BACKGROUND);

        panel.add(new JLabel("Updating stock for: " + productName));
        
        JSpinner stockSpinner = new JSpinner(new SpinnerNumberModel(currentStock, 0, 10000, 1));
        panel.add(stockSpinner);

        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(e -> {
            int newStock = (int) stockSpinner.getValue();
            if (updateProductStock(productId, newStock)) {
                inventoryTableModel.setValueAt(newStock, selectedRow, 2);
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Stock updated successfully!");
            }
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(updateButton, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private boolean updateProductStock(int productId, int newStock) {
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE products SET stock = ? WHERE id = ?")) {
            
            stmt.setInt(1, newStock);
            stmt.setInt(2, productId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error updating stock: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void generateRestockList() {
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT name, stock, reorder_level FROM products WHERE stock < reorder_level")) {
            
            StringBuilder restockList = new StringBuilder();
            restockList.append("Products needing restock:\n\n");
            
            while (rs.next()) {
                restockList.append(rs.getString("name"))
                          .append(" - Current: ")
                          .append(rs.getInt("stock"))
                          .append(", Reorder at: ")
                          .append(rs.getInt("reorder_level"))
                          .append("\n");
            }
            
            JTextArea textArea = new JTextArea(restockList.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("Arial", Font.PLAIN, 14));
            textArea.setBackground(ThemeColors.BACKGROUND);
            textArea.setForeground(ThemeColors.TEXT);
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(400, 300));
            
            JOptionPane.showMessageDialog(this, scrollPane, "Restock List", JOptionPane.INFORMATION_MESSAGE);
            
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
            JOptionPane.showMessageDialog(this, "Please select a user first", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int userId = (int) userTableModel.getValueAt(selectedRow, 0);
        String currentName = (String) userTableModel.getValueAt(selectedRow, 1);
        String currentEmail = (String) userTableModel.getValueAt(selectedRow, 2);
        String currentRole = (String) userTableModel.getValueAt(selectedRow, 3);
        int currentPoints = (int) userTableModel.getValueAt(selectedRow, 4);

        JDialog dialog = new JDialog(this, "Edit User", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(ThemeColors.BACKGROUND);
        dialog.setSize(400, 300);

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(ThemeColors.BACKGROUND);

        JTextField nameField = new JTextField(currentName);
        JTextField emailField = new JTextField(currentEmail);
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"customer", "admin"});
        roleCombo.setSelectedItem(currentRole);
        JTextField pointsField = new JTextField(String.valueOf(currentPoints));

        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Role:"));
        formPanel.add(roleCombo);
        formPanel.add(new JLabel("Loyalty Points:"));
        formPanel.add(pointsField);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText();
                String email = emailField.getText();
                String role = (String) roleCombo.getSelectedItem();
                int points = Integer.parseInt(pointsField.getText());

                if (updateUser(userId, name, email, role, points)) {
                    userTableModel.setValueAt(name, selectedRow, 1);
                    userTableModel.setValueAt(email, selectedRow, 2);
                    userTableModel.setValueAt(role, selectedRow, 3);
                    userTableModel.setValueAt(points, selectedRow, 4);
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this, "User updated successfully!");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter valid numbers for loyalty points", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(saveButton, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);
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
            JOptionPane.showMessageDialog(this, 
                "Error updating user: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void filterSales(String status) {
        loadSales(status);
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
            System.err.println("Failed to initialize FlatDarkLaf");
            ex.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            new AdminFrame();
        });
    }
}