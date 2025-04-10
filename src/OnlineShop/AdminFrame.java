package OnlineShop;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import com.formdev.flatlaf.FlatDarkLaf;

public class AdminFrame extends JFrame {
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JTable userTable, productTable, inventoryTable, salesTable, supplierTable, ordersTable;
    private DefaultTableModel userTableModel, productTableModel, inventoryTableModel, 
                            salesTableModel, supplierTableModel, ordersTableModel;

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
        JPanel suppliersPanel = createSuppliersPanel();
        JPanel ordersPanel = createOrdersPanel();

        // Add panels to mainPanel
        mainPanel.add(dashboardPanel, "Dashboard");
        mainPanel.add(usersPanel, "Users");
        mainPanel.add(productsPanel, "Products");
        mainPanel.add(inventoryPanel, "Inventory");
        mainPanel.add(salesPanel, "Sales");
        mainPanel.add(suppliersPanel, "Suppliers");
        mainPanel.add(ordersPanel, "Orders");

        add(mainPanel, BorderLayout.CENTER);
        add(createNavigationBar(), BorderLayout.WEST);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createNavigationBar() {
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(ThemeColors.BACKGROUND);
        navBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        navBar.setPreferredSize(new Dimension(200, getHeight()));

        // Logo
        JLabel logo = new JLabel("Admin Panel", SwingConstants.CENTER);
        logo.setFont(new Font("Malgun Gothic", Font.BOLD, 20));
        logo.setForeground(ThemeColors.PRIMARY);
        navBar.add(logo, BorderLayout.NORTH);

        // Navigation buttons
        JPanel navButtons = new JPanel(new GridLayout(8, 1, 10, 10));
        navButtons.setOpaque(false);
        navButtons.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JButton dashboardButton = createNavButton("Dashboard");
        dashboardButton.addActionListener(e -> cardLayout.show(mainPanel, "Dashboard"));

        JButton usersButton = createNavButton("Users");
        usersButton.addActionListener(e -> {
            loadUsers();
            cardLayout.show(mainPanel, "Users");
        });

        JButton productsButton = createNavButton("Products");
        productsButton.addActionListener(e -> {
            loadProducts();
            cardLayout.show(mainPanel, "Products");
        });

        JButton inventoryButton = createNavButton("Inventory");
        inventoryButton.addActionListener(e -> {
            loadInventory();
            cardLayout.show(mainPanel, "Inventory");
        });

        JButton salesButton = createNavButton("Sales");
        salesButton.addActionListener(e -> {
            loadSales();
            cardLayout.show(mainPanel, "Sales");
        });

        JButton suppliersButton = createNavButton("Suppliers");
        suppliersButton.addActionListener(e -> {
            loadSuppliers();
            cardLayout.show(mainPanel, "Suppliers");
        });

        JButton ordersButton = createNavButton("Orders");
        ordersButton.addActionListener(e -> {
            loadOrders();
            cardLayout.show(mainPanel, "Orders");
        });

        JButton logoutButton = createNavButton("Logout");
        logoutButton.addActionListener(e -> logout());

        navButtons.add(dashboardButton);
        navButtons.add(usersButton);
        navButtons.add(productsButton);
        navButtons.add(inventoryButton);
        navButtons.add(salesButton);
        navButtons.add(suppliersButton);
        navButtons.add(ordersButton);
        navButtons.add(logoutButton);

        navBar.add(navButtons, BorderLayout.CENTER);
        return navBar;
    }

    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(ThemeColors.TEXT);
        button.setBackground(ThemeColors.CARD_BG);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(ThemeColors.PRIMARY);
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

    private JPanel createDashboardPanel() {
        JPanel dashboardPanel = new JPanel(new BorderLayout());
        dashboardPanel.setBackground(ThemeColors.BACKGROUND);

        // Title
        JLabel titleLabel = new JLabel("Admin Dashboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(ThemeColors.PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        dashboardPanel.add(titleLabel, BorderLayout.NORTH);

        // Metrics grid
        JPanel metricsPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        metricsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        metricsPanel.setBackground(ThemeColors.BACKGROUND);

        // Add metric cards
        metricsPanel.add(createMetricCard("Total Products", getTotalProducts()));
        metricsPanel.add(createMetricCard("Total Orders", getTotalOrders()));
        metricsPanel.add(createMetricCard("Low Stock Items", getLowStockItems()));
        metricsPanel.add(createMetricCard("Total Customers", getTotalCustomers()));
        metricsPanel.add(createMetricCard("Total Revenue", getTotalRevenue()));
        metricsPanel.add(createMetricCard("Pending Orders", getPendingOrders()));

        dashboardPanel.add(metricsPanel, BorderLayout.CENTER);
        return dashboardPanel;
    }

    private JPanel createMetricCard(String title, String value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(ThemeColors.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.SECONDARY, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(ThemeColors.TEXT);

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setForeground(ThemeColors.PRIMARY);

        card.add(titleLabel, BorderLayout.NORTH);
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
        productTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Price", "Stock", "Category"}, 0);
        productTable = new JTable(productTableModel);
        styleTable(productTable);
        
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

    private JPanel createSuppliersPanel() {
        JPanel panel = createTablePanel("Supplier Management");
        supplierTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Contact", "Products Supplied"}, 0);
        supplierTable = new JTable(supplierTableModel);
        styleTable(supplierTable);
        
        // Add buttons for supplier management
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        
        JButton addSupplierButton = new JButton("Add Supplier");
        addSupplierButton.addActionListener(e -> addSupplier());
        
        JButton editSupplierButton = new JButton("Edit Supplier");
        editSupplierButton.addActionListener(e -> editSupplier());
        
        buttonPanel.add(addSupplierButton);
        buttonPanel.add(editSupplierButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        panel.add(new JScrollPane(supplierTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createOrdersPanel() {
        JPanel panel = createTablePanel("Order Management");
        ordersTableModel = new DefaultTableModel(new String[]{"Order ID", "Customer", "Total", "Date", "Status", "Cancellation Request"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
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
        table.setGridColor(ThemeColors.BACKGROUND);
        table.setSelectionBackground(ThemeColors.PRIMARY);
        table.setSelectionForeground(Color.WHITE);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.getTableHeader().setBackground(ThemeColors.SECONDARY);
        table.getTableHeader().setForeground(Color.WHITE);
    }

    // ========== DATABASE OPERATIONS ==========

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
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, price, stock, category FROM products")) {
            productTableModel.setRowCount(0);
            while (rs.next()) {
                productTableModel.addRow(new Object[]{
                    rs.getInt("id"), 
                    rs.getString("name"), 
                    rs.getDouble("price"), 
                    rs.getInt("stock"), 
                    rs.getString("category")
                });
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

    private void loadSuppliers() {
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT s.id, s.name, s.contact, GROUP_CONCAT(p.name SEPARATOR ', ') as products " +
                 "FROM suppliers s " +
                 "LEFT JOIN products p ON s.id = p.supplier_id " +
                 "GROUP BY s.id")) {
            supplierTableModel.setRowCount(0);
            while (rs.next()) {
                supplierTableModel.addRow(new Object[]{
                    rs.getInt("id"), 
                    rs.getString("name"), 
                    rs.getString("contact"),
                    rs.getString("products") != null ? rs.getString("products") : "None"
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading suppliers.", "Error", JOptionPane.ERROR_MESSAGE);
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
        try (Connection conn = DBConnection.connect()) {
            // First try with total_price column
            try {
                PreparedStatement stmt = conn.prepareStatement(
                    "SELECT SUM(total_price) FROM orders WHERE status = 'Delivered'");
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return "$" + rs.getString(1);
                }
            } catch (SQLException e) {
                // If that fails, calculate it from order_items
                PreparedStatement stmt = conn.prepareStatement(
                    "SELECT SUM(oi.price * oi.quantity) " +
                    "FROM order_items oi " +
                    "JOIN orders o ON oi.order_id = o.id " +
                    "WHERE o.status = 'Delivered'");
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return "$" + rs.getString(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return "$0";
    }

    private String getPendingOrders() {
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM orders WHERE status = 'Processing'")) {
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return "0";
    }

    // ========== ORDER MANAGEMENT FUNCTIONS ==========

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

        if (choice == 0) { // Approve
            approveCancellation(orderId, selectedRow);
        } else if (choice == 1) { // Deny
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

    // ========== PRODUCT MANAGEMENT FUNCTIONS ==========

    private void addProduct() {
        JDialog dialog = new JDialog(this, "Add New Product", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(ThemeColors.BACKGROUND);
        dialog.setSize(400, 400);

        JPanel formPanel = new JPanel(new GridLayout(7, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(ThemeColors.BACKGROUND);

        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField stockField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextField descriptionField = new JTextField();
        JTextField supplierField = new JTextField();

        formPanel.add(new JLabel("Product Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Price:"));
        formPanel.add(priceField);
        formPanel.add(new JLabel("Stock:"));
        formPanel.add(stockField);
        formPanel.add(new JLabel("Category:"));
        formPanel.add(categoryField);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(descriptionField);
        formPanel.add(new JLabel("Supplier ID:"));
        formPanel.add(supplierField);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText();
                double price = Double.parseDouble(priceField.getText());
                int stock = Integer.parseInt(stockField.getText());
                String category = categoryField.getText();
                String description = descriptionField.getText();
                int supplierId = Integer.parseInt(supplierField.getText());

                if (saveNewProduct(name, price, stock, category, description, supplierId)) {
                    loadProducts();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this, "Product added successfully!");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter valid numbers for price, stock, and supplier ID", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(saveButton, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private boolean saveNewProduct(String name, double price, int stock, 
                                 String category, String description, int supplierId) {
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO products (name, price, stock, category, description, supplier_id) " +
                 "VALUES (?, ?, ?, ?, ?, ?)")) {
            
            stmt.setString(1, name);
            stmt.setDouble(2, price);
            stmt.setInt(3, stock);
            stmt.setString(4, category);
            stmt.setString(5, description);
            stmt.setInt(6, supplierId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error adding product: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void editProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product first", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int productId = (int) productTableModel.getValueAt(selectedRow, 0);
        String currentName = (String) productTableModel.getValueAt(selectedRow, 1);
        double currentPrice = (double) productTableModel.getValueAt(selectedRow, 2);
        int currentStock = (int) productTableModel.getValueAt(selectedRow, 3);
        String currentCategory = (String) productTableModel.getValueAt(selectedRow, 4);

        JDialog dialog = new JDialog(this, "Edit Product", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(ThemeColors.BACKGROUND);
        dialog.setSize(400, 300);

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(ThemeColors.BACKGROUND);

        JTextField nameField = new JTextField(currentName);
        JTextField priceField = new JTextField(String.valueOf(currentPrice));
        JTextField stockField = new JTextField(String.valueOf(currentStock));
        JTextField categoryField = new JTextField(currentCategory);

        formPanel.add(new JLabel("Product Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Price:"));
        formPanel.add(priceField);
        formPanel.add(new JLabel("Stock:"));
        formPanel.add(stockField);
        formPanel.add(new JLabel("Category:"));
        formPanel.add(categoryField);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText();
                double price = Double.parseDouble(priceField.getText());
                int stock = Integer.parseInt(stockField.getText());
                String category = categoryField.getText();

                if (updateProduct(productId, name, price, stock, category)) {
                    productTableModel.setValueAt(name, selectedRow, 1);
                    productTableModel.setValueAt(price, selectedRow, 2);
                    productTableModel.setValueAt(stock, selectedRow, 3);
                    productTableModel.setValueAt(category, selectedRow, 4);
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this, "Product updated successfully!");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter valid numbers for price and stock", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(saveButton, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private boolean updateProduct(int productId, String name, double price, int stock, String category) {
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE products SET name = ?, price = ?, stock = ?, category = ? WHERE id = ?")) {
            
            stmt.setString(1, name);
            stmt.setDouble(2, price);
            stmt.setInt(3, stock);
            stmt.setString(4, category);
            stmt.setInt(5, productId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
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
        String productName = (String) productTableModel.getValueAt(selectedRow, 1);

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

    // ========== INVENTORY MANAGEMENT FUNCTIONS ==========

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

    // ========== USER MANAGEMENT FUNCTIONS ==========

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

    // ========== SUPPLIER MANAGEMENT FUNCTIONS ==========

    private void addSupplier() {
        JDialog dialog = new JDialog(this, "Add New Supplier", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(ThemeColors.BACKGROUND);
        dialog.setSize(400, 300);

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(ThemeColors.BACKGROUND);

        JTextField nameField = new JTextField();
        JTextField contactField = new JTextField();
        JTextField addressField = new JTextField();

        formPanel.add(new JLabel("Supplier Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Contact:"));
        formPanel.add(contactField);
        formPanel.add(new JLabel("Address:"));
        formPanel.add(addressField);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            String name = nameField.getText();
            String contact = contactField.getText();
            String address = addressField.getText();

            if (saveNewSupplier(name, contact, address)) {
                loadSuppliers();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Supplier added successfully!");
            }
        });

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(saveButton, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private boolean saveNewSupplier(String name, String contact, String address) {
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO suppliers (name, contact, address) VALUES (?, ?, ?)")) {
            
            stmt.setString(1, name);
            stmt.setString(2, contact);
            stmt.setString(3, address);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error adding supplier: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void editSupplier() {
        int selectedRow = supplierTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a supplier first", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int supplierId = (int) supplierTableModel.getValueAt(selectedRow, 0);
        String currentName = (String) supplierTableModel.getValueAt(selectedRow, 1);
        String currentContact = (String) supplierTableModel.getValueAt(selectedRow, 2);

        JDialog dialog = new JDialog(this, "Edit Supplier", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(ThemeColors.BACKGROUND);
        dialog.setSize(400, 300);

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(ThemeColors.BACKGROUND);

        JTextField nameField = new JTextField(currentName);
        JTextField contactField = new JTextField(currentContact);

        formPanel.add(new JLabel("Supplier Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Contact:"));
        formPanel.add(contactField);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            String name = nameField.getText();
            String contact = contactField.getText();

            if (updateSupplier(supplierId, name, contact)) {
                supplierTableModel.setValueAt(name, selectedRow, 1);
                supplierTableModel.setValueAt(contact, selectedRow, 2);
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Supplier updated successfully!");
            }
        });

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(saveButton, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private boolean updateSupplier(int supplierId, String name, String contact) {
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE suppliers SET name = ?, contact = ? WHERE id = ?")) {
            
            stmt.setString(1, name);
            stmt.setString(2, contact);
            stmt.setInt(3, supplierId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error updating supplier: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // ========== UTILITY METHODS ==========

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