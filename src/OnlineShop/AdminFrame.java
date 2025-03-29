package OnlineShop;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import com.formdev.flatlaf.FlatIntelliJLaf;

public class AdminFrame extends JFrame {
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JTable userTable, productTable, inventoryTable, salesTable, supplierTable;
    private DefaultTableModel userTableModel, productTableModel, inventoryTableModel, salesTableModel, supplierTableModel;

    public AdminFrame() {
        setTitle("Admin Panel");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // CardLayout for switching between views
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Dashboard Panel (Default View)
        JPanel dashboardPanel = createDashboardPanel();
        mainPanel.add(dashboardPanel, "Dashboard");

        // Users Panel
        JPanel usersPanel = createUsersPanel();
        mainPanel.add(usersPanel, "Users");

        // Products Panel
        JPanel productsPanel = createProductsPanel();
        mainPanel.add(productsPanel, "Products");

        // Inventory Panel
        JPanel inventoryPanel = createInventoryPanel();
        mainPanel.add(inventoryPanel, "Inventory");

        // Sales Panel
        JPanel salesPanel = createSalesPanel();
        mainPanel.add(salesPanel, "Sales");

        // Suppliers Panel
        JPanel suppliersPanel = createSuppliersPanel();
        mainPanel.add(suppliersPanel, "Suppliers");

        // Add mainPanel to the frame
        add(mainPanel, BorderLayout.CENTER);

        // Navigation Panel
        JPanel navPanel = createNavigationPanel();
        add(navPanel, BorderLayout.WEST);

        setVisible(true);
    }

    private JPanel createDashboardPanel() {
        JPanel dashboardPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        dashboardPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        dashboardPanel.setBackground(new Color(173, 216, 230)); // Pastel Blue

        // Key Metrics Cards
        JPanel totalProductsCard = createMetricCard("Total Products", getTotalProducts());
        JPanel totalOrdersCard = createMetricCard("Total Orders", getTotalOrders());
        JPanel lowStockCard = createMetricCard("Low Stock Items", getLowStockItems());
        JPanel totalCustomersCard = createMetricCard("Total Customers", getTotalCustomers());
        JPanel totalRevenueCard = createMetricCard("Total Revenue", getTotalRevenue());
        JPanel pendingOrdersCard = createMetricCard("Pending Orders", getPendingOrders());

        dashboardPanel.add(totalProductsCard);
        dashboardPanel.add(totalOrdersCard);
        dashboardPanel.add(lowStockCard);
        dashboardPanel.add(totalCustomersCard);
        dashboardPanel.add(totalRevenueCard);
        dashboardPanel.add(pendingOrdersCard);

        return dashboardPanel;
    }

    private JPanel createMetricCard(String title, String value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(255, 182, 193)); // Pastel Pink
        card.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        valueLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createUsersPanel() {
        JPanel usersPanel = new JPanel(new BorderLayout());
        usersPanel.setBackground(new Color(173, 216, 230)); // Pastel Blue

        userTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Role"}, 0);
        userTable = new JTable(userTableModel);
        loadUsers();

        JScrollPane scrollPane = new JScrollPane(userTable);
        usersPanel.add(scrollPane, BorderLayout.CENTER);

        return usersPanel;
    }

    private JPanel createProductsPanel() {
        JPanel productsPanel = new JPanel(new BorderLayout());
        productsPanel.setBackground(new Color(173, 216, 230)); // Pastel Blue

        productTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Price", "Stock", "Category"}, 0);
        productTable = new JTable(productTableModel);
        loadProducts();

        JScrollPane scrollPane = new JScrollPane(productTable);
        productsPanel.add(scrollPane, BorderLayout.CENTER);

        return productsPanel;
    }

    private JPanel createInventoryPanel() {
        JPanel inventoryPanel = new JPanel(new BorderLayout());
        inventoryPanel.setBackground(new Color(173, 216, 230)); // Pastel Blue

        inventoryTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Stock"}, 0);
        inventoryTable = new JTable(inventoryTableModel);
        loadInventory();

        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        inventoryPanel.add(scrollPane, BorderLayout.CENTER);

        return inventoryPanel;
    }

    private JPanel createSalesPanel() {
        JPanel salesPanel = new JPanel(new BorderLayout());
        salesPanel.setBackground(new Color(173, 216, 230)); // Pastel Blue

        salesTableModel = new DefaultTableModel(new String[]{"Order ID", "Customer ID", "Product", "Quantity", "Total Price", "Date"}, 0);
        salesTable = new JTable(salesTableModel);
        loadSales();

        JScrollPane scrollPane = new JScrollPane(salesTable);
        salesPanel.add(scrollPane, BorderLayout.CENTER);

        return salesPanel;
    }

    private JPanel createSuppliersPanel() {
        JPanel suppliersPanel = new JPanel(new BorderLayout());
        suppliersPanel.setBackground(new Color(173, 216, 230)); // Pastel Blue

        supplierTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Contact"}, 0);
        supplierTable = new JTable(supplierTableModel);
        loadSuppliers();

        JScrollPane scrollPane = new JScrollPane(supplierTable);
        suppliersPanel.add(scrollPane, BorderLayout.CENTER);

        return suppliersPanel;
    }

    private JPanel createNavigationPanel() {
        JPanel navPanel = new JPanel(new GridLayout(8, 1, 10, 10));
        navPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        navPanel.setBackground(new Color(255, 223, 186)); // Pastel Yellow

        JButton dashboardButton = createNavButton("Dashboard");
        dashboardButton.addActionListener(e -> cardLayout.show(mainPanel, "Dashboard"));

        JButton usersButton = createNavButton("Manage Users");
        usersButton.addActionListener(e -> {
            loadUsers();
            cardLayout.show(mainPanel, "Users");
        });

        JButton productsButton = createNavButton("Manage Products");
        productsButton.addActionListener(e -> {
            loadProducts();
            cardLayout.show(mainPanel, "Products");
        });

        JButton inventoryButton = createNavButton("Manage Inventory");
        inventoryButton.addActionListener(e -> {
            loadInventory();
            cardLayout.show(mainPanel, "Inventory");
        });

        JButton salesButton = createNavButton("Sales Reports");
        salesButton.addActionListener(e -> {
            loadSales();
            cardLayout.show(mainPanel, "Sales");
        });

        JButton suppliersButton = createNavButton("Manage Suppliers");
        suppliersButton.addActionListener(e -> {
            loadSuppliers();
            cardLayout.show(mainPanel, "Suppliers");
        });

        JButton logoutButton = createNavButton("Logout");
        logoutButton.addActionListener(e -> logout());

        navPanel.add(dashboardButton);
        navPanel.add(usersButton);
        navPanel.add(productsButton);
        navPanel.add(inventoryButton);
        navPanel.add(salesButton);
        navPanel.add(suppliersButton);
        navPanel.add(logoutButton);

        return navPanel;
    }

    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(255, 182, 193)); // Pastel Pink
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        return button;
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
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            new LoginFrame();
            dispose();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        new AdminFrame();
    }
}