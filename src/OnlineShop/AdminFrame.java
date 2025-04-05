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
    private JTable userTable, productTable, inventoryTable, salesTable, supplierTable;
    private DefaultTableModel userTableModel, productTableModel, inventoryTableModel, salesTableModel, supplierTableModel;

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

        // Add panels to mainPanel
        mainPanel.add(dashboardPanel, "Dashboard");
        mainPanel.add(usersPanel, "Users");
        mainPanel.add(productsPanel, "Products");
        mainPanel.add(inventoryPanel, "Inventory");
        mainPanel.add(salesPanel, "Sales");
        mainPanel.add(suppliersPanel, "Suppliers");

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
        JPanel navButtons = new JPanel(new GridLayout(7, 1, 10, 10));
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

        JButton logoutButton = createNavButton("Logout");
        logoutButton.addActionListener(e -> logout());

        navButtons.add(dashboardButton);
        navButtons.add(usersButton);
        navButtons.add(productsButton);
        navButtons.add(inventoryButton);
        navButtons.add(salesButton);
        navButtons.add(suppliersButton);
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
        userTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Role"}, 0);
        userTable = new JTable(userTableModel);
        styleTable(userTable);
        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createProductsPanel() {
        JPanel panel = createTablePanel("Product Management");
        productTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Price", "Stock", "Category"}, 0);
        productTable = new JTable(productTableModel);
        styleTable(productTable);
        panel.add(new JScrollPane(productTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createInventoryPanel() {
        JPanel panel = createTablePanel("Inventory Management");
        inventoryTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Stock"}, 0);
        inventoryTable = new JTable(inventoryTableModel);
        styleTable(inventoryTable);
        panel.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSalesPanel() {
        JPanel panel = createTablePanel("Sales Reports");
        salesTableModel = new DefaultTableModel(new String[]{"Order ID", "Customer ID", "Product", "Quantity", "Total Price", "Date"}, 0);
        salesTable = new JTable(salesTableModel);
        styleTable(salesTable);
        panel.add(new JScrollPane(salesTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSuppliersPanel() {
        JPanel panel = createTablePanel("Supplier Management");
        supplierTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Contact"}, 0);
        supplierTable = new JTable(supplierTableModel);
        styleTable(supplierTable);
        panel.add(new JScrollPane(supplierTable), BorderLayout.CENTER);
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

    private void loadUsers() {
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, email, role FROM customers")) {
            userTableModel.setRowCount(0);
            while (rs.next()) {
                userTableModel.addRow(new Object[]{rs.getInt("id"), rs.getString("name"), rs.getString("email"), rs.getString("role")});
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
                productTableModel.addRow(new Object[]{rs.getInt("id"), rs.getString("name"), rs.getDouble("price"), rs.getInt("stock"), rs.getString("category")});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading products.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadInventory() {
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, stock FROM products")) {
            inventoryTableModel.setRowCount(0);
            while (rs.next()) {
                inventoryTableModel.addRow(new Object[]{rs.getInt("id"), rs.getString("name"), rs.getInt("stock")});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading inventory.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSales() {
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT o.id, o.customer_id, p.name, oi.quantity, o.total_price, o.order_date " +
                 "FROM orders o " +
                 "JOIN order_items oi ON o.id = oi.order_id " +
                 "JOIN products p ON oi.product_id = p.id")) {
            salesTableModel.setRowCount(0);
            while (rs.next()) {
                salesTableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getInt("customer_id"),
                    rs.getString("name"),
                    rs.getInt("quantity"),
                    rs.getDouble("total_price"),
                    rs.getDate("order_date")
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
             ResultSet rs = stmt.executeQuery("SELECT id, name, contact FROM suppliers")) {
            supplierTableModel.setRowCount(0);
            while (rs.next()) {
                supplierTableModel.addRow(new Object[]{rs.getInt("id"), rs.getString("name"), rs.getString("contact")});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading suppliers.", "Error", JOptionPane.ERROR_MESSAGE);
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
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products WHERE stock < 10")) {
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
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT SUM(total_price) FROM orders")) {
            if (rs.next()) {
                return "$" + rs.getString(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return "$0";
    }

    private String getPendingOrders() {
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM orders WHERE status = 'Pending'")) {
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return "0";
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