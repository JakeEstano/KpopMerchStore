package OnlineShop;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Calendar; // Import Calendar
import java.util.Date; // Import Date

// Import necessary AddressManager classes WITHOUT the UI dialog part
import OnlineShop.AddressManager.Address;
import OnlineShop.AddressManager.Region;
import OnlineShop.AddressManager.Province;
import OnlineShop.AddressManager.Municipality;
import OnlineShop.AddressManager.Barangay;

public class CheckoutFrame extends JFrame {
    private int customerId;
    private List<Integer> cartIds; // IDs of items being checked out
    private AddressManager.Address selectedAddress;
    private AddressManager addressManager;
    private CustomerFrame customerFrameInstance; // Reference to potentially update badges if needed

    // --- UI Components ---
    private JPanel mainPanel;
    private JPanel addressPanel;
    private JLabel selectedAddressLabel;
    private JButton changeAddressButton;
    private JPanel productsPanel; // Scrollable panel for product items
    private JPanel summaryPaymentPanel; // Panel containing summary and payment options
    private JLabel itemsSubtotalLabel;
    private JLabel shippingFeeLabel;
    private JLabel orderTotalLabel;
    private JComboBox<String> paymentMethodBox;
    private JPanel paymentFieldsPanel; // Panel to hold dynamic payment fields
    private GridBagConstraints gbcPaymentFields; // GBC for payment fields
    private JButton placeOrderButton;
    private JScrollPane productScrollPane; // Reference to the scroll pane

    // Payment Fields (declared as members for easy access)
    // These are created empty by the helper method. Labels are placed separately.
    private JTextField cardNumberField, cvvField, expiryField;
    private JTextField accountNameField, accountNumberField;
    // No password field for simulated GCash/Maya/PayPal

    // Data storage for items
    private Map<Integer, CartItemData> cartItemDataMap = new HashMap<>(); // cartId -> ItemData

    // --- Inner class to hold Cart Item Data (Keep as is) ---
    private static class CartItemData {
        int cartId;
        int productId;
        String name;
        double price;
        int quantity;
        String imagePath;
        int stock; // Store initial stock for validation
        JPanel panelRef; // Reference to the UI panel for this item
        JLabel itemSubtotalLabelRef; // Reference to its subtotal label
        JSpinner quantitySpinnerRef; // Reference to its quantity spinner

        CartItemData(int cartId, int productId, String name, double price, int quantity, String imagePath, int stock, JPanel panelRef) {
            this.cartId = cartId;
            this.productId = productId;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.imagePath = imagePath;
            this.stock = stock;
            this.panelRef = panelRef;
        }
    }
    // --- End Inner class ---

    public CheckoutFrame(int customerId, List<Integer> cartIds, CustomerFrame customerFrameInstance) {
        this.customerId = customerId;
        this.cartIds = new ArrayList<>(cartIds); // Copy the list
        this.addressManager = new AddressManager(customerFrameInstance, customerId);
        this.customerFrameInstance = customerFrameInstance;

        setTitle("HAMTEO - Checkout");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // *** ADDED for Full Screen ***
        setUndecorated(true); // Remove window borders and title bar
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximize to fill screen
        // *** END ADDED ***

        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 25, 15, 25));
        mainPanel.setBackground(ThemeColors.BACKGROUND);

        // --- Top Panel: Back Button and Title ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JButton backButton = createStyledButton("← Back to Shop", ThemeColors.SECONDARY);
        backButton.addActionListener(e -> goBackToShop());
        JLabel checkoutTitle = new JLabel("Checkout", SwingConstants.CENTER);
        checkoutTitle.setFont(new Font("Arial", Font.BOLD, 28));
        checkoutTitle.setForeground(ThemeColors.PRIMARY);
        topPanel.add(backButton, BorderLayout.WEST);
        topPanel.add(checkoutTitle, BorderLayout.CENTER);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0)); // Add some bottom padding

        // --- NORTH: Address Section ---
        setupAddressSection(); // Keep this setup

        // --- CENTER: Products Section ---
        setupProductsSection(); // Keep this setup
        productScrollPane = new JScrollPane(productsPanel); // Create scroll pane here
        styleScrollPaneStatic(productScrollPane); // Apply custom scrollbar style using STATIC method
        productScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        productScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        productScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        productScrollPane.setBorder(null);
        productScrollPane.getViewport().setBackground(ThemeColors.BACKGROUND); // Match viewport background


        // --- SOUTH: Summary and Payment Section ---
        setupSummaryAndPaymentSection(); // Keep this setup

        // --- Add sections to main panel ---
        mainPanel.add(topPanel, BorderLayout.NORTH); // Add the new top panel
        JPanel centerContentPanel = new JPanel(new BorderLayout(10, 10)); // Panel to hold address and products
        centerContentPanel.setOpaque(false);
        centerContentPanel.add(addressPanel, BorderLayout.NORTH);
        centerContentPanel.add(productScrollPane, BorderLayout.CENTER); // Add the products scroll pane
        mainPanel.add(centerContentPanel, BorderLayout.CENTER); // Add combined center panel
        mainPanel.add(summaryPaymentPanel, BorderLayout.SOUTH); // Add the existing summary/payment panel

        setContentPane(mainPanel);

        // --- Load Initial Data ---
        SwingUtilities.invokeLater(() -> {
            loadInitialAddress();
            loadProductDetails();
            updateSummary();
            // setVisible moved to end of constructor in previous step, keeping it there
             // No need to call pack() when going full screen
             // pack(); // No longer needed for full screen
            setLocationRelativeTo(null); // Center if not full screen (though it will be)
            setVisible(true);
        });
    }

    // --- Setup UI Sections (Keep existing methods, minor adjustments if needed) ---

    private void setupAddressSection() {
        addressPanel = new JPanel(new BorderLayout(10, 0));
        addressPanel.setOpaque(false);
        addressPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(ThemeColors.SECONDARY),
            " Delivery Address ",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 16), ThemeColors.PRIMARY));

        selectedAddressLabel = new JLabel("<html><i>Loading address...</i></html>");
        selectedAddressLabel.setForeground(ThemeColors.TEXT);
        selectedAddressLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        selectedAddressLabel.setBorder(new EmptyBorder(10, 15, 10, 15));
        addressPanel.add(selectedAddressLabel, BorderLayout.CENTER);

        changeAddressButton = createStyledButton("Change", ThemeColors.SECONDARY);
        changeAddressButton.addActionListener(e -> openAddressDialog()); // Changed action
        JPanel changeButtonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        changeButtonWrapper.setOpaque(false);
        changeButtonWrapper.add(changeAddressButton);
        addressPanel.add(changeButtonWrapper, BorderLayout.EAST);
    }

    private void setupProductsSection() {
        productsPanel = new JPanel();
        productsPanel.setLayout(new BoxLayout(productsPanel, BoxLayout.Y_AXIS));
        productsPanel.setBackground(ThemeColors.BACKGROUND);
        productsPanel.setBorder(BorderFactory.createCompoundBorder(
             BorderFactory.createTitledBorder(
                 BorderFactory.createLineBorder(ThemeColors.SECONDARY),
                 " Products Ordered ",
                 TitledBorder.LEFT, TitledBorder.TOP,
                 new Font("Arial", Font.BOLD, 16), ThemeColors.PRIMARY),
             new EmptyBorder(5, 5, 5, 5))
        );

        // Header Row is now added by addCheckoutHeaderRow()
    }

    private void setupSummaryAndPaymentSection() {
        summaryPaymentPanel = new JPanel(new BorderLayout(10, 10));
        summaryPaymentPanel.setOpaque(false);
        summaryPaymentPanel.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeColors.SECONDARY), // Top border
            new EmptyBorder(10, 10, 10, 10) // Padding
        ));

        // Panel for Payment Method and Details
        JPanel paymentDetailsPanel = new JPanel(new BorderLayout(10, 10));
        paymentDetailsPanel.setOpaque(false);

        // Payment Method Selection
        JPanel paymentMethodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        paymentMethodPanel.setOpaque(false);
        paymentMethodPanel.add(createFormLabelStatic("Payment Method:")); // Use static helper
        // *** UPDATED Payment Methods - COD removed ***
        paymentMethodBox = new JComboBox<>(new String[]{
                "Credit/Debit Card", "GCash", "Maya", "PayPal"
        });
        paymentMethodBox.addActionListener(e -> updatePaymentFieldsVisibility());
        styleComboBoxStatic(paymentMethodBox); // Use the STATIC styleComboBox
        paymentMethodPanel.add(paymentMethodBox);
        paymentDetailsPanel.add(paymentMethodPanel, BorderLayout.NORTH);

        // Dynamic Payment Fields Area
        paymentFieldsPanel = new JPanel(new GridBagLayout());
        paymentFieldsPanel.setOpaque(false); // Use main panel background
        paymentFieldsPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        gbcPaymentFields = new GridBagConstraints();
        gbcPaymentFields.insets = new Insets(5, 5, 5, 5); // Spacing around components
        // Default constraints set in addFieldPair

        // Create all possible fields (but don't add them yet) - using static helper
        // These helpers already *don't* put text inside the fields
        cardNumberField = createFormTextFieldStatic(); // Already creates EMPTY field
        cvvField = createFormTextFieldStatic();       // Already creates EMPTY field
        expiryField = createFormTextFieldStatic();      // Already creates EMPTY field
        expiryField.setToolTipText("MM/YY");
        accountNameField = createFormTextFieldStatic(); // Already creates EMPTY field
        accountNumberField = createFormTextFieldStatic(); // Already creates EMPTY field
        // No password field needed for simulated GCash/Maya/PayPal

        updatePaymentFieldsVisibility(); // Initial setup (uses the refined addFieldPair)
        paymentDetailsPanel.add(paymentFieldsPanel, BorderLayout.CENTER);

        // Panel for Order Summary and Place Order button (Right Side)
        JPanel summaryActionPanel = new JPanel();
        summaryActionPanel.setLayout(new BoxLayout(summaryActionPanel, BoxLayout.Y_AXIS));
        summaryActionPanel.setOpaque(false);
        summaryActionPanel.setBorder(new EmptyBorder(0, 20, 0, 0)); // Left padding

        itemsSubtotalLabel = createSummaryLabel("Items Subtotal: ₱0.00");
        shippingFeeLabel = createSummaryLabel("Shipping Fee: ₱0.00");
        orderTotalLabel = createSummaryLabel("Order Total: ₱0.00");
        orderTotalLabel.setFont(new Font("Arial", Font.BOLD, 18)); // Make total larger
        orderTotalLabel.setForeground(ThemeColors.PRIMARY); // Highlight total

        summaryActionPanel.add(createRightAlignedPanel(itemsSubtotalLabel));
        summaryActionPanel.add(createRightAlignedPanel(shippingFeeLabel));
        summaryActionPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        summaryActionPanel.add(createRightAlignedPanel(orderTotalLabel));
        summaryActionPanel.add(Box.createRigidArea(new Dimension(0, 15))); // Spacer before button

        placeOrderButton = createStyledButton("Place Order", ThemeColors.PRIMARY);
        placeOrderButton.setPreferredSize(new Dimension(180, 45));
        placeOrderButton.setAlignmentX(Component.RIGHT_ALIGNMENT); // Align button right within BoxLayout
        placeOrderButton.addActionListener(e -> placeOrder());
        summaryActionPanel.add(placeOrderButton);
        summaryActionPanel.add(Box.createVerticalGlue()); // Push content up


        // Add payment and summary panels to the main summaryPaymentPanel
        summaryPaymentPanel.add(paymentDetailsPanel, BorderLayout.CENTER);
        summaryPaymentPanel.add(summaryActionPanel, BorderLayout.EAST);
    }

    // --- Data Loading and UI Update Methods (Keep or Adapt) ---

    private void loadInitialAddress() {
        try {
            selectedAddress = addressManager.getDefaultAddress();
            if (selectedAddress == null) {
                System.out.println("No default address found, trying first address...");
                List<Address> addresses = addressManager.getCustomerAddresses();
                if (addresses != null && !addresses.isEmpty()) {
                    selectedAddress = addresses.get(0);
                }
            }

            if (selectedAddress != null) {
                displaySelectedAddress();
                updateSummary(); // Recalculate shipping/total
            } else {
                 // No addresses at all, prompt user to add one
                 selectedAddressLabel.setText("<html><i>No addresses found. Please add one.</i></html>");
                 placeOrderButton.setEnabled(false); // Disable checkout
                 // Optionally, immediately open the address dialog
                  SwingUtilities.invokeLater(this::openAddressDialog);
            }

        } catch (Exception e) {
            System.err.println("Error loading initial address: " + e.getMessage());
            e.printStackTrace();
            selectedAddressLabel.setText("<html><i>Could not load address. Error occurred.</i></html>");
            placeOrderButton.setEnabled(false);
        }
    }

     // --- NEW: Opens the Address Management Dialog ---
     private void openAddressDialog() {
         // Use 'this' (the CheckoutFrame) as the parent Window
         // Pass customerFrameInstance as the parent Window to AddressManagementDialog
         AddressManagementDialog dialog = new AddressManagementDialog(customerFrameInstance, customerId, addressManager, selectedAddress);
         dialog.setVisible(true);
         // After the dialog is closed, get the result
         Address newlySelected = dialog.getSelectedAddress();
         if (newlySelected != null) {
             selectedAddress = newlySelected;
             displaySelectedAddress();
             updateSummary(); // Update summary after address change
             placeOrderButton.setEnabled(true); // Ensure button is enabled
         } else if (selectedAddress == null) {
             // Dialog was cancelled and still no address selected
             selectedAddressLabel.setText("<html><i>No address selected. Please add or select an address.</i></html>");
             placeOrderButton.setEnabled(false); // Disable checkout without an address
         }
     }

    private void displaySelectedAddress() {
         if (selectedAddress != null) {
             selectedAddressLabel.setText(selectedAddress.getFullAddressForDisplay());
              // Ensure place order button is enabled if there are items
              placeOrderButton.setEnabled(!cartItemDataMap.isEmpty());
         } else {
             selectedAddressLabel.setText("<html><i>No address selected. Click 'Change' to add or select one.</i></html>");
             placeOrderButton.setEnabled(false); // Disable checkout if no address
         }
    }

    private void addCheckoutHeaderRow() {
        // Header Row (Copied from setupProductsSection, adjust if needed)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30)); // Keep max height constraint
        headerPanel.setPreferredSize(new Dimension(800, 30)); // Set preferred height
        headerPanel.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0, ThemeColors.SECONDARY),
            // Adjust left padding to align with image width + padding
            new EmptyBorder(5, 95, 5, 5) // Image (70) + West padding (10) + East padding (10) + GBC Inset (5) approx = 95
        ));

        JLabel productHeaderLabel = new JLabel("Product");
        JLabel priceHeaderLabel = new JLabel("Unit Price");
        JLabel quantityHeaderLabel = new JLabel("Quantity");
        JLabel subtotalHeaderLabel = new JLabel("Item Subtotal");

        Font headerFont = new Font("Arial", Font.BOLD, 13);
        productHeaderLabel.setFont(headerFont); priceHeaderLabel.setFont(headerFont);
        quantityHeaderLabel.setFont(headerFont); subtotalHeaderLabel.setFont(headerFont);
        productHeaderLabel.setForeground(ThemeColors.TEXT); priceHeaderLabel.setForeground(ThemeColors.TEXT);
        quantityHeaderLabel.setForeground(ThemeColors.TEXT); subtotalHeaderLabel.setForeground(ThemeColors.TEXT);

        JPanel headerContent = new JPanel(new GridBagLayout());
        headerContent.setOpaque(false);
        GridBagConstraints gbcHeader = new GridBagConstraints();
        gbcHeader.insets = new Insets(0, 10, 0, 10); // Horizontal spacing between header labels

        // Column 0: Product Name
        gbcHeader.gridx = 0; gbcHeader.gridy = 0; gbcHeader.weightx = 0.5; // Takes up more space
        gbcHeader.anchor = GridBagConstraints.WEST; gbcHeader.fill = GridBagConstraints.HORIZONTAL;
        headerContent.add(productHeaderLabel, gbcHeader);

        // Column 1: Unit Price
        gbcHeader.gridx = 1; gbcHeader.gridy = 0; gbcHeader.weightx = 0.15; // Smaller weight
        gbcHeader.anchor = GridBagConstraints.WEST; gbcHeader.fill = GridBagConstraints.NONE; // Don't fill horizontally
        headerContent.add(priceHeaderLabel, gbcHeader);

        // Column 2: Quantity
        gbcHeader.gridx = 2; gbcHeader.gridy = 0; gbcHeader.weightx = 0.20; // Moderate weight
        gbcHeader.anchor = GridBagConstraints.CENTER; // Center the label itself
        headerContent.add(quantityHeaderLabel, gbcHeader);

        // Column 3: Item Subtotal
        gbcHeader.gridx = 3; gbcHeader.gridy = 0; gbcHeader.weightx = 0.15; // Smaller weight
        gbcHeader.anchor = GridBagConstraints.EAST; // Align label to the right
        headerContent.add(subtotalHeaderLabel, gbcHeader);

        headerPanel.add(headerContent, BorderLayout.CENTER);
        productsPanel.add(headerPanel); // Add the header panel
        productsPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Add spacer after header
    }

    // --- Keep loadProductDetails, showEmptyCheckoutMessage ---
     private void loadProductDetails() {
        System.out.println("[CHECKOUT DEBUG] === Entering loadProductDetails ===");
        System.out.println("[CHECKOUT DEBUG] Customer ID: " + this.customerId);
        System.out.println("[CHECKOUT DEBUG] Received cartIds List: " + this.cartIds);

        productsPanel.removeAll(); // Clear previous items including old header
        // ADD HEADER ROW USING HELPER
        addCheckoutHeaderRow();

        cartItemDataMap.clear();

        if (cartIds == null || cartIds.isEmpty()) {
            System.out.println("[CHECKOUT DEBUG] cartIds is null or empty. Showing empty message.");
            showEmptyCheckoutMessage();
            return;
        }

        String placeholders = String.join(",", Collections.nCopies(cartIds.size(), "?"));
        String sql = "SELECT c.id AS cart_id, c.quantity, p.id AS product_id, p.name, p.price, p.image_path, p.stock " +
                     "FROM cart c JOIN products p ON c.product_id = p.id " +
                     "WHERE c.id IN (" + placeholders + ") AND c.customer_id = ?";

        System.out.println("[CHECKOUT DEBUG] SQL Query: " + sql);

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean hasItems = false;

        try {
            conn = DBConnection.connect();
            stmt = conn.prepareStatement(sql);
            int paramIndex = 1;
            for (int cartIdParam : cartIds) {
                System.out.println("[CHECKOUT DEBUG] Setting parameter " + paramIndex + " (cart.id): " + cartIdParam);
                stmt.setInt(paramIndex++, cartIdParam);
            }
            System.out.println("[CHECKOUT DEBUG] Setting parameter " + paramIndex + " (cart.customer_id): " + this.customerId);
            stmt.setInt(paramIndex, this.customerId);

            rs = stmt.executeQuery();
            System.out.println("[CHECKOUT DEBUG] SQL query executed.");

            if (!rs.isBeforeFirst() ) {
                System.out.println("[CHECKOUT DEBUG] ResultSet is empty. No matching cart items found for the given IDs and customer.");
            }

            while (rs.next()) {
                hasItems = true;
                int cartId = rs.getInt("cart_id");
                String name = rs.getString("name");
                System.out.println("[CHECKOUT DEBUG] Found item in ResultSet: cart_id=" + cartId + ", name=" + name);

                int productId = rs.getInt("product_id");
                int quantity = rs.getInt("quantity");
                double price = rs.getDouble("price");
                String imagePath = rs.getString("image_path");
                int stock = rs.getInt("stock");

                System.out.println("[CHECKOUT DEBUG] Item Details: quantity=" + quantity + ", stock=" + stock);
                 if (quantity > stock) {
                      System.out.println("[CHECKOUT DEBUG] Stock Check: Quantity (" + quantity + ") > Stock (" + stock + ") for '" + name + "'. Adjusting/Skipping.");
                      int newQuantity = Math.max(0, stock);
                      if (newQuantity == 0) {
                          System.out.println("[CHECKOUT DEBUG] Item '" + name + "' stock is 0. Skipping panel creation.");
                          continue;
                      } else {
                           quantity = newQuantity;
                           System.out.println("[CHECKOUT DEBUG] Adjusted quantity for '" + name + "' to " + quantity + " due to stock limit.");
                      }
                 }

                 System.out.println("[CHECKOUT DEBUG] Creating item panel for: " + name);
                 JPanel itemPanel = createCheckoutItemPanel(cartId, productId, name, price, quantity, imagePath, stock);
                 productsPanel.add(itemPanel); // Add item

                 System.out.println("[CHECKOUT DEBUG] productsPanel component count after adding item: " + productsPanel.getComponentCount());

                 // Add spacer AFTER item (except maybe for the last one?)
                 productsPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Reduced spacer height

                 CartItemData itemData = new CartItemData(cartId, productId, name, price, quantity, imagePath, stock, itemPanel);
                 findAndStoreItemPanelRefs(itemPanel, itemData); // Needs adjustment for new layout
                 cartItemDataMap.put(cartId, itemData);
                 System.out.println("[CHECKOUT DEBUG] Added item panel and data to map for: " + name);
            }
            System.out.println("[CHECKOUT DEBUG] Finished processing ResultSet. hasItems=" + hasItems);

             if (!hasItems || cartItemDataMap.isEmpty()) {
                System.out.println("[CHECKOUT DEBUG] No items processed or map is empty. Showing empty message.");
                showEmptyCheckoutMessage();
            }

        } catch (SQLException e) {
            System.err.println("[CHECKOUT DEBUG] SQLException loading product details: " + e.getMessage());
            e.printStackTrace();
            showEmptyCheckoutMessage("Error loading items.");
        } finally {
             try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
             try { if (stmt != null) stmt.close(); } catch (SQLException ignored) {}
             try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }

        System.out.println("[CHECKOUT DEBUG] Revalidating/Repainting productsPanel.");
        productsPanel.revalidate();
        productsPanel.repaint();

        System.out.println("[CHECKOUT DEBUG] Revalidating/Repainting productScrollPane.");
        if (productScrollPane != null) {
            productScrollPane.revalidate();
            productScrollPane.repaint();
            Container parent = productScrollPane.getParent();
            if (parent != null) {
                parent.revalidate();
                parent.repaint();
            }
            SwingUtilities.invokeLater(() -> productScrollPane.getVerticalScrollBar().setValue(0));
        } else {
            System.err.println("[CHECKOUT DEBUG] productScrollPane is NULL during repaint!");
        }

         System.out.println("[CHECKOUT DEBUG] === Exiting loadProductDetails ===");
    }

    private void showEmptyCheckoutMessage(String... customMessage) {
         productsPanel.removeAll();
         // Re-add header row even when empty? Maybe not.
         // addCheckoutHeaderRow();
         productsPanel.setLayout(new BorderLayout());
         String message = (customMessage.length > 0) ? customMessage[0] : "No items selected for checkout.";
         JLabel emptyLabel = new JLabel(message, SwingConstants.CENTER);
         emptyLabel.setFont(new Font("Arial", Font.ITALIC, 16));
         emptyLabel.setForeground(Color.GRAY);
         productsPanel.add(emptyLabel, BorderLayout.CENTER);
         placeOrderButton.setEnabled(false); // Disable placing order
         productsPanel.revalidate();
         productsPanel.repaint();
    }

    // --- UPDATED: findAndStoreItemPanelRefs to search new layout ---
    private void findAndStoreItemPanelRefs(JPanel itemPanel, CartItemData itemData) {
        // Find the main content panel (should be BorderLayout.CENTER)
        Component centerComp = ((BorderLayout)itemPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (centerComp instanceof JPanel && centerComp.getName() != null && centerComp.getName().equals("itemDetailsGrid")) {
            JPanel detailsGrid = (JPanel) centerComp;
            // Now search within the GridBagLayout panel
            for (Component subComp : detailsGrid.getComponents()) {
                 if (subComp instanceof JSpinner && "quantitySpinner".equals(subComp.getName())) {
                     itemData.quantitySpinnerRef = (JSpinner) subComp;
                 } else if (subComp instanceof JLabel && "itemSubtotalLabel".equals(subComp.getName())) {
                      itemData.itemSubtotalLabelRef = (JLabel) subComp;
                 }
                 // No need to recurse further usually in a flat GridBagLayout
            }
        } else {
             System.err.println("WARN: Could not find 'itemDetailsGrid' panel in itemPanel for " + itemData.name);
             // Fallback to searching all components if structure is unexpected
             recursiveFindRefs(itemPanel, itemData);
        }

        if (itemData.quantitySpinnerRef == null) System.err.println("WARN: Quantity spinner not found for " + itemData.name);
        if (itemData.itemSubtotalLabelRef == null) System.err.println("WARN: Subtotal label not found for " + itemData.name);
    }

    // Recursive helper (fallback)
    private void recursiveFindRefs(Container container, CartItemData itemData) {
         for (Component comp : container.getComponents()) {
            if (comp instanceof JSpinner && "quantitySpinner".equals(comp.getName())) {
                itemData.quantitySpinnerRef = (JSpinner) comp;
            } else if (comp instanceof JLabel && "itemSubtotalLabel".equals(comp.getName())) {
                itemData.itemSubtotalLabelRef = (JLabel) comp;
            } else if (comp instanceof Container) {
                recursiveFindRefs((Container) comp, itemData);
            }
         }
    }


    // --- UPDATED: createCheckoutItemPanel uses GridBagLayout for center/right content ---
     private JPanel createCheckoutItemPanel(int cartId, int productId, String productName, double price, int quantity, String imagePath, int stock) {
        JPanel itemPanel = new JPanel(new BorderLayout(10, 0)); // Reduced vgap
        itemPanel.setBackground(ThemeColors.CARD_BG);
        itemPanel.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0, ThemeColors.BACKGROUND), // Bottom separator thin
            new EmptyBorder(10, 10, 10, 10) // Consistent padding
        ));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90)); // Constrain height more
        itemPanel.setPreferredSize(new Dimension(800, 85)); // Preferred height

        itemPanel.putClientProperty("cartId", cartId);
        itemPanel.putClientProperty("productId", productId);
        itemPanel.putClientProperty("price", price);
        itemPanel.putClientProperty("stock", stock);

        // WEST: Image Label
        JLabel imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(70, 70));
        imageLabel.setBorder(BorderFactory.createLineBorder(ThemeColors.SECONDARY));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        ImageIcon icon = CustomerFrame.loadImageIcon(imagePath, productName);
        if (icon != null && CustomerFrame.isIconValid(icon)) {
            Image scaledImage = icon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
        } else {
            imageLabel.setText("N/A"); imageLabel.setFont(new Font("Arial", Font.ITALIC, 10)); imageLabel.setForeground(Color.GRAY);
        }
        itemPanel.add(imageLabel, BorderLayout.WEST);


        // CENTER: New GridBagLayout panel for details
        JPanel itemDetailsGrid = new JPanel(new GridBagLayout());
        itemDetailsGrid.setName("itemDetailsGrid"); // Name for finding components
        itemDetailsGrid.setOpaque(false); // Use itemPanel's background
        GridBagConstraints gbcDetails = new GridBagConstraints();
        // Consistent insets with header's content panel
        gbcDetails.insets = new Insets(0, 10, 0, 10);
        gbcDetails.gridy = 0; // All items in the same row


        // Column 0: Product Name
        JLabel nameLabel = new JLabel("<html><body style='width: 98%'>" + productName + "</body></html>"); // Use HTML for wrapping
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(ThemeColors.TEXT);
        gbcDetails.gridx = 0; gbcDetails.weightx = 0.5; // Same weight as header
        gbcDetails.fill = GridBagConstraints.HORIZONTAL; // Allow filling horizontally
        gbcDetails.anchor = GridBagConstraints.WEST; // Align text left
        itemDetailsGrid.add(nameLabel, gbcDetails);

        // Column 1: Unit Price
        JLabel unitPriceLabel = new JLabel(String.format("₱%.2f", price));
        unitPriceLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        unitPriceLabel.setForeground(ThemeColors.TEXT);
        gbcDetails.gridx = 1; gbcDetails.weightx = 0.15; // Same weight as header
        gbcDetails.fill = GridBagConstraints.NONE; // Don't stretch label
        gbcDetails.anchor = GridBagConstraints.WEST; // Align text left (aligns with header label)
        itemDetailsGrid.add(unitPriceLabel, gbcDetails);

        // Column 2: Quantity Spinner
        SpinnerModel spinnerModel = new SpinnerNumberModel(quantity, 0, Math.max(quantity, stock), 1);
        JSpinner quantitySpinner = new JSpinner(spinnerModel);
        quantitySpinner.setName("quantitySpinner"); // Name for retrieval
        JFormattedTextField textField = ((JSpinner.DefaultEditor) quantitySpinner.getEditor()).getTextField();
        textField.setColumns(3); textField.setHorizontalAlignment(JTextField.CENTER);
        quantitySpinner.addChangeListener(e -> handleQuantityChange(cartId, (Integer) quantitySpinner.getValue()));
        // Set preferred size to prevent spinner from shrinking too much
        quantitySpinner.setPreferredSize(new Dimension(60, 28));
        gbcDetails.gridx = 2; gbcDetails.weightx = 0.20; // Same weight as header
        gbcDetails.fill = GridBagConstraints.NONE; // Don't stretch spinner
        gbcDetails.anchor = GridBagConstraints.CENTER; // Center the spinner itself in its allocated space
        itemDetailsGrid.add(quantitySpinner, gbcDetails);

        // Column 3: Item Subtotal
        JLabel itemSubtotalLabel = new JLabel(String.format("₱%.2f", price * quantity));
        itemSubtotalLabel.setName("itemSubtotalLabel"); // Name for retrieval
        itemSubtotalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        itemSubtotalLabel.setForeground(ThemeColors.TEXT);
        itemSubtotalLabel.setHorizontalAlignment(SwingConstants.RIGHT); // Align text to the right
        gbcDetails.gridx = 3; gbcDetails.weightx = 0.15; // Same weight as header
        gbcDetails.fill = GridBagConstraints.HORIZONTAL; // Fill cell horizontally
        gbcDetails.anchor = GridBagConstraints.EAST; // Anchor the content (text) to the right edge
        itemDetailsGrid.add(itemSubtotalLabel, gbcDetails);

        // Add the new grid panel to the main item panel's center
        itemPanel.add(itemDetailsGrid, BorderLayout.CENTER);

        return itemPanel;
    }

     private void handleQuantityChange(int cartId, int newQuantity) {
        CartItemData itemData = cartItemDataMap.get(cartId);
        if (itemData == null) return;

        // Validate against stock
        if (newQuantity > itemData.stock) {
             showThemedJOptionPane(
                 "Cannot add more. Only " + itemData.stock + " items available for '" + itemData.name + "'.",
                 "Stock Limit Reached", JOptionPane.WARNING_MESSAGE);
             if (itemData.quantitySpinnerRef != null) {
                 // Use invokeLater to ensure UI update happens after current event processing
                 SwingUtilities.invokeLater(() -> itemData.quantitySpinnerRef.setValue(itemData.stock));
             }
             newQuantity = itemData.stock; // Set quantity to max stock for calculations below
             // Return early to prevent further processing until the spinner value updates
             // Let the spinner's change listener trigger handleQuantityChange again with the corrected value.
             // Update the internal quantity right away though? No, let the spinner trigger it.
             // Just return for now.
             return;
        }

        // Handle removal if quantity becomes 0 or less
        if (newQuantity <= 0) {
             int confirm = JOptionPane.showConfirmDialog(this,
                 "Remove '" + itemData.name + "' from your order?",
                 "Confirm Removal", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                 // Remove UI panel and associated spacer
                 int componentIndex = -1;
                 for (int i = 0; i < productsPanel.getComponentCount(); i++) {
                    if (productsPanel.getComponent(i) == itemData.panelRef) {
                        componentIndex = i;
                        break;
                    }
                 }
                 if (componentIndex != -1) {
                    productsPanel.remove(componentIndex); // Remove the item panel
                    // Check if the next component is a spacer and remove it too
                    // Be careful with index out of bounds
                    if (componentIndex < productsPanel.getComponentCount() && productsPanel.getComponent(componentIndex) instanceof Box.Filler) {
                        productsPanel.remove(componentIndex);
                    }
                 }

                cartItemDataMap.remove(cartId);
                cartIds.remove(Integer.valueOf(cartId));

                productsPanel.revalidate();
                productsPanel.repaint();

                 if (cartItemDataMap.isEmpty()) {
                     showEmptyCheckoutMessage();
                 }
            } else {
                // User cancelled removal, reset spinner to 1
                 if (itemData.quantitySpinnerRef != null) {
                     // Use invokeLater for spinner update
                     SwingUtilities.invokeLater(() -> itemData.quantitySpinnerRef.setValue(1));
                 }
                newQuantity = 1; // Update local quantity for calculation below ONLY if spinner was reset
                 // If spinner is null, we can't reset, so maybe keep newQuantity as 0/negative?
                 // For safety, if spinner is null and user cancels, maybe just do nothing?
                 if (itemData.quantitySpinnerRef == null) {
                     System.err.println("WARN: Cannot reset quantity to 1 as spinner ref is null for " + itemData.name);
                     return; // Exit if we can't reset UI
                 }
            }
        }

         // Update the quantity in our data map (only if it changed and is valid)
         if (itemData.quantity != newQuantity && newQuantity > 0) {
            itemData.quantity = newQuantity;
         } else if (newQuantity <= 0 && itemData.quantitySpinnerRef != null) {
             // If removal was cancelled and spinner reset to 1, update map
             itemData.quantity = 1;
         }


        // Update item subtotal label immediately if quantity is positive
        if (itemData.itemSubtotalLabelRef != null && itemData.quantity > 0) {
             itemData.itemSubtotalLabelRef.setText(String.format("₱%.2f", itemData.price * itemData.quantity));
        } else if (itemData.quantity <= 0) {
            // If item was removed (or removal cancelled but couldn't reset), maybe clear subtotal?
             if (itemData.itemSubtotalLabelRef != null) {
                // itemData.itemSubtotalLabelRef.setText("₱0.00"); // Or leave as is? Let's leave it for now.
             }
        } else {
            System.err.println("WARN: itemSubtotalLabelRef is null for cartId " + cartId + " during quantity change.");
        }

        // Update the overall summary regardless
        updateSummary();
    }


    // --- Update Summary & Shipping Fee Calculation ---
    private void updateSummary() {
        double subtotal = 0;
        for (CartItemData item : cartItemDataMap.values()) {
            subtotal += item.price * item.quantity;
        }

        double shippingFee = getShippingFee(selectedAddress); // Calculate shipping

        double total = subtotal + shippingFee;

        itemsSubtotalLabel.setText(String.format("Items Subtotal: ₱%.2f", subtotal));
        shippingFeeLabel.setText(String.format("Shipping Fee: ₱%.2f", shippingFee));
        orderTotalLabel.setText(String.format("Order Total: ₱%.2f", total));

        // Enable/disable place order button
        placeOrderButton.setEnabled(!cartItemDataMap.isEmpty() && selectedAddress != null && subtotal > 0);
    }

    private double getShippingFee(Address addr) {
        if (addr == null || addr.getRegionId() == null) {
            return 0.0; // Default to 0 if no address or region
        }
        String islandGroup = getIslandGroup(addr.getRegionId());
        System.out.println("DEBUG: Address Region ID: " + addr.getRegionId() + ", Island Group: " + islandGroup);

        switch (islandGroup) {
            case "Luzon": return 65.00;
            case "Visayas": return 80.00;
            case "Mindanao": return 120.00;
            default:
                 System.err.println("Warning: Unknown island group for Region ID: " + addr.getRegionId());
                 return 65.00; // Default fallback
        }
    }

    private String getIslandGroup(String regionId) {
        if (regionId == null) return "Unknown";
        // Using the provided mapping
        switch (regionId) {
            // Luzon
            case "1": case "2": case "3": case "4": case "5": case "6": case "7": case "8": case "14": // NCR is Luzon
            return "Luzon";
            // Visayas
            case "9": case "10": case "11": case "18": // Region 6, 7, 8, NIR
            return "Visayas";
            // Mindanao
            case "12": case "13": case "15": case "16": case "17": case "BARMM": // Region 9, 10, 11, 12, 13(Caraga), BARMM
            return "Mindanao";
            default: return "Unknown";
        }
    }

    // --- Method to add label/field pair with refined constraints ---
    private void addFieldPair(String labelText, Component field, int gridY) {
        // --- Column 0: Add the LABEL ---
        gbcPaymentFields.gridx = 0;
        gbcPaymentFields.gridy = gridY;
        gbcPaymentFields.gridwidth = 1; // Explicitly set gridwidth
        gbcPaymentFields.weightx = 0.0; // Label column should NOT expand horizontally
        gbcPaymentFields.fill = GridBagConstraints.NONE; // Label should not fill its cell space
        gbcPaymentFields.anchor = GridBagConstraints.EAST; // Align label text to the right (towards the field)
        paymentFieldsPanel.add(createFormLabelStatic(labelText), gbcPaymentFields); // Add the label

        // --- Column 1: Add the text FIELD ---
        gbcPaymentFields.gridx = 1;
        gbcPaymentFields.gridy = gridY;
        gbcPaymentFields.gridwidth = 1; // Explicitly set gridwidth
        gbcPaymentFields.weightx = 1.0; // Field column SHOULD expand horizontally to take extra space
        gbcPaymentFields.fill = GridBagConstraints.HORIZONTAL; // Make the field component fill its column horizontally
        gbcPaymentFields.anchor = GridBagConstraints.WEST; // Align the field component to the left edge of its column
        paymentFieldsPanel.add(field, gbcPaymentFields); // Add the actual JTextField/component
    }


    // --- UPDATED Payment Fields Logic (uses refined addFieldPair) ---
    private void updatePaymentFieldsVisibility() {
         paymentFieldsPanel.removeAll();
         String method = (String) paymentMethodBox.getSelectedItem();
         int gridY = 0;

         if ("Credit/Debit Card".equals(method)) {
             // Calls the UPDATED addFieldPair
             addFieldPair("Card Number:", cardNumberField, gridY++);
             addFieldPair("CVV:", cvvField, gridY++);
             addFieldPair("Expiry (MM/YY):", expiryField, gridY++);
         } else if ("GCash".equals(method) || "Maya".equals(method) || "PayPal".equals(method)) {
              // Calls the UPDATED addFieldPair
              addFieldPair(method + " Account Name:", accountNameField, gridY++);
              addFieldPair(method + " Account Number/Email:", accountNumberField, gridY++);
              // No password needed for simulation
         }
         // No fields for COD as it's removed

         // Add glue to push fields up - Reset gridwidth and fill/anchor after loop
         gbcPaymentFields.gridx = 0;
         gbcPaymentFields.gridy = gridY;
         gbcPaymentFields.gridwidth = 2; // Span both columns for the glue
         gbcPaymentFields.weightx = 1.0; // Allow glue to use horizontal space
         gbcPaymentFields.weighty = 1.0; // Allow glue to push everything up
         gbcPaymentFields.fill = GridBagConstraints.BOTH; // Fill vertically and horizontally
         gbcPaymentFields.anchor = GridBagConstraints.CENTER; // Doesn't matter much for glue
         paymentFieldsPanel.add(Box.createVerticalGlue(), gbcPaymentFields);

         // Reset some GBC properties potentially modified by glue for next time
         gbcPaymentFields.weighty = 0.0;
         gbcPaymentFields.fill = GridBagConstraints.HORIZONTAL; // Default fill for fields
         gbcPaymentFields.gridwidth = 1; // Default gridwidth

         paymentFieldsPanel.revalidate();
         paymentFieldsPanel.repaint();
         // No pack/resize needed for full screen
     }


    // --- Final Order Placement Logic (Adapted from PaymentFrame) ---
     private void placeOrder() {
        // 1. Validations
        if (selectedAddress == null) {
            showThemedJOptionPane("Please select a delivery address.", "Address Required", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (cartItemDataMap.isEmpty()) {
             showThemedJOptionPane("Your order is empty.", "Empty Order", JOptionPane.ERROR_MESSAGE);
             return;
        }
        if (!validatePaymentFields()) {
             return; // Validation message shown internally
        }

        // 2. Recalculate final totals (important!)
        double finalSubtotal = 0;
        for (CartItemData item : cartItemDataMap.values()) {
             finalSubtotal += item.price * item.quantity;
        }
        double finalShippingFee = getShippingFee(selectedAddress);
        double finalOrderTotal = finalSubtotal + finalShippingFee;

        // Removed the COD check for zero total, as COD is not an option
        if (finalOrderTotal <= 0) {
            showThemedJOptionPane("Cannot place an order with zero total. Please add items or check quantities.", "Order Error", JOptionPane.ERROR_MESSAGE);
            return;
        }


        // 3. Final Stock Check
        if (!checkFinalStock()) {
             // Message shown internally by checkFinalStock
             return;
        }

        // 4. Database Transaction
        Connection conn = null;
        boolean orderSuccess = false;
        int generatedOrderId = -1;
        String paymentMethod = (String) paymentMethodBox.getSelectedItem();
        String updateStockSql = "UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?";

        try {
            conn = DBConnection.connect();
            conn.setAutoCommit(false);

            // --- a. Verify stock and prepare stock updates (in transaction) ---
            try (PreparedStatement stockUpdateStmt = conn.prepareStatement(updateStockSql)) {
                for (CartItemData itemData : cartItemDataMap.values()) {
                    if (itemData.quantity <= 0) continue; // Skip items with 0 quantity

                    // Check stock again within transaction for safety
                    String checkStockSql = "SELECT stock FROM products WHERE id = ?";
                    int currentStock = -1;
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkStockSql)) {
                        checkStmt.setInt(1, itemData.productId);
                        ResultSet rsStock = checkStmt.executeQuery();
                        if (rsStock.next()) {
                            currentStock = rsStock.getInt("stock");
                        }
                         // Close ResultSet promptly
                         if (rsStock != null) rsStock.close();
                    } // checkStmt closed here
                    if (currentStock < itemData.quantity) {
                        throw new SQLException("Insufficient stock for " + itemData.name + " (Required: " + itemData.quantity + ", Available: " + currentStock + ")");
                    }

                    // Prepare stock update
                    stockUpdateStmt.setInt(1, itemData.quantity); // Quantity to decrement
                    stockUpdateStmt.setInt(2, itemData.productId);
                    stockUpdateStmt.setInt(3, itemData.quantity); // Condition: stock must be >= quantity
                    stockUpdateStmt.addBatch();
                }
                // Execute stock updates
                int[] stockUpdateResults = stockUpdateStmt.executeBatch();
                for (int result : stockUpdateResults) {
                    if (result == Statement.EXECUTE_FAILED || result == 0) { // Check for failure or no rows updated
                        throw new SQLException("Stock update failed for one or more items (likely due to insufficient stock or race condition).");
                    }
                }
            } // stockUpdateStmt closed here


            // --- b. Create Order ---
            // Include shipping fee calculation if applicable
            String insertOrderSQL = "INSERT INTO orders (customer_id, status, order_date, total_price, address_id, shipping_fee, notification_read_status) " +
                                    "VALUES (?, 'Processing', NOW(), ?, ?, ?, 0)"; // Added shipping_fee
            try (PreparedStatement orderStmt = conn.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS)) {
                 orderStmt.setInt(1, customerId);
                 orderStmt.setDouble(2, finalOrderTotal);
                 orderStmt.setInt(3, selectedAddress.getId());
                 orderStmt.setDouble(4, finalShippingFee); // Save calculated shipping fee
                 orderStmt.executeUpdate();

                 ResultSet rsKeys = orderStmt.getGeneratedKeys();
                 if (rsKeys.next()) {
                     generatedOrderId = rsKeys.getInt(1);
                 } else {
                     throw new SQLException("Creating order failed, no ID obtained.");
                 }
                 // Close ResultSet promptly
                 if (rsKeys != null) rsKeys.close();
            } // orderStmt closed here

            // --- c. Add Order Items ---
             String insertItemSQL = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
             try (PreparedStatement itemStmt = conn.prepareStatement(insertItemSQL)) {
                 for (CartItemData itemData : cartItemDataMap.values()) {
                      if (itemData.quantity > 0) {
                          itemStmt.setInt(1, generatedOrderId);
                          itemStmt.setInt(2, itemData.productId);
                          itemStmt.setInt(3, itemData.quantity);
                          itemStmt.setDouble(4, itemData.price);
                          itemStmt.addBatch();
                      }
                 }
                 itemStmt.executeBatch();
             } // itemStmt closed here

             // --- d. Create Payment Record (Handles different methods) ---
             savePaymentRecord(conn, generatedOrderId, paymentMethod);


            // --- e. Clear Processed Cart Items ---
            if (!cartIds.isEmpty()) {
                String deleteSQL = "DELETE FROM cart WHERE id IN (" +
                    String.join(",", Collections.nCopies(cartIds.size(), "?")) + ") AND customer_id = ?";
                try (PreparedStatement clearCartStmt = conn.prepareStatement(deleteSQL)) {
                    int paramIndex = 1;
                    for (int cartId : cartIds) {
                        clearCartStmt.setInt(paramIndex++, cartId);
                    }
                    clearCartStmt.setInt(paramIndex, customerId);
                    clearCartStmt.executeUpdate();
                } // clearCartStmt closed here
            }

            // --- f. Commit Transaction ---
            conn.commit();
            orderSuccess = true;

        } catch (SQLException ex) {
             System.err.println("Order placement transaction failed: " + ex.getMessage());
             ex.printStackTrace();
             if (conn != null) try { conn.rollback(); } catch (SQLException eRollback) { System.err.println("Rollback failed: " + eRollback.getMessage()); eRollback.printStackTrace(); }
             // Provide more specific feedback if possible
             if (ex.getMessage().contains("Insufficient stock")) {
                  showThemedJOptionPane("Order placement failed: " + ex.getMessage() + "\nPlease adjust your order quantity.", "Stock Error", JOptionPane.ERROR_MESSAGE);
                  // Consider reloading product details to show updated stock after rollback
                   SwingUtilities.invokeLater(() -> {
                      loadProductDetails();
                      updateSummary();
                   });
             } else {
                  showThemedJOptionPane("Order placement failed due to a database error: " + ex.getMessage() + "\nPlease try again later.", "Database Error", JOptionPane.ERROR_MESSAGE);
             }
        } finally {
            if (conn != null) {
                try {
                     if (!conn.getAutoCommit()) { // Only change if we set it to false
                         conn.setAutoCommit(true);
                     }
                     conn.close();
                 } catch (SQLException eClose) {
                     System.err.println("Failed to close connection: " + eClose.getMessage());
                     eClose.printStackTrace();
                 }
             }
        }

        // --- Post-Transaction Actions ---
        if (orderSuccess) {
             showThemedJOptionPane("Order placed successfully!\nOrder ID: " + generatedOrderId,
                 "Order Confirmed", JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Close checkout frame

            // Try to update CustomerFrame if it exists
             if (customerFrameInstance != null && customerFrameInstance.isDisplayable()) {
                  customerFrameInstance.updateCartBadge();
                  customerFrameInstance.updateOrderBadge();
                  customerFrameInstance.showCard("Orders"); // Navigate to orders page
                  customerFrameInstance.loadOrders(); // Refresh orders list
                  customerFrameInstance.setVisible(true); // Ensure it's visible
                  customerFrameInstance.toFront();    // Bring it to front
             } else if(customerFrameInstance != null) {
                  // If frame exists but wasn't displayable, make it visible
                  customerFrameInstance.setVisible(true);
                  customerFrameInstance.updateCartBadge();
                  customerFrameInstance.updateOrderBadge();
                  customerFrameInstance.showCard("Orders");
                  customerFrameInstance.loadOrders();
                  customerFrameInstance.toFront();
             }
             else {
                  System.out.println("CustomerFrame instance not available or closed. Order placed, checkout closed.");
             }
        }
    }

    // Helper method to save payment record within the transaction
    private void savePaymentRecord(Connection conn, int orderId, String method) throws SQLException {
        // Match fields with `payments` table schema
        String sql = "INSERT INTO payments (customer_id, order_id, method, card_number, cvv, expiry, account_name, account_number, payment_date, refunded) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), 0)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.setInt(2, orderId);
            stmt.setString(3, method);

            if ("Credit/Debit Card".equals(method)) {
                 stmt.setString(4, cardNumberField.getText());
                 stmt.setString(5, cvvField.getText());
                 stmt.setString(6, expiryField.getText());
                 stmt.setNull(7, Types.VARCHAR); // account_name
                 stmt.setNull(8, Types.VARCHAR); // account_number
            } else if ("GCash".equals(method) || "Maya".equals(method) || "PayPal".equals(method)) {
                stmt.setNull(4, Types.VARCHAR); // card_number
                stmt.setNull(5, Types.VARCHAR); // cvv
                stmt.setNull(6, Types.VARCHAR); // expiry
                stmt.setString(7, accountNameField.getText());
                stmt.setString(8, accountNumberField.getText());
            } else { // Should not happen as COD is removed, but handle defensively
                 stmt.setNull(4, Types.VARCHAR);
                 stmt.setNull(5, Types.VARCHAR);
                 stmt.setNull(6, Types.VARCHAR);
                 stmt.setNull(7, Types.VARCHAR);
                 stmt.setNull(8, Types.VARCHAR);
            }
            stmt.executeUpdate();
        } // stmt closed here
    }

    private boolean checkFinalStock() {
         Connection conn = null;
         PreparedStatement stmt = null;
         ResultSet rs = null;
         boolean allAvailable = true;
         List<String> stockIssues = new ArrayList<>();
         Map<Integer, Integer> latestStock = new HashMap<>(); // Store latest stock levels

         String sql = "SELECT id, name, stock FROM products WHERE id = ?"; // Prepare outside loop

         try {
             conn = DBConnection.connect();
             stmt = conn.prepareStatement(sql); // Create statement once

             for (CartItemData itemData : cartItemDataMap.values()) {
                  if (itemData.quantity <= 0) continue;

                  stmt.setInt(1, itemData.productId); // Set parameter
                  rs = stmt.executeQuery(); // Execute
                  String productName = itemData.name; // Use name from map first

                  if (rs.next()) { // Check if product exists
                      int currentStock = rs.getInt("stock");
                      latestStock.put(itemData.productId, currentStock); // Store latest stock
                      productName = rs.getString("name"); // Update with latest name just in case

                      if (currentStock < itemData.quantity) {
                          allAvailable = false;
                          int availableStock = Math.max(0, currentStock);
                          stockIssues.add(productName + " (Available: " + availableStock + ", You ordered: " + itemData.quantity + ")");
                      }
                  } else { // Product might have been deleted between load and checkout
                      allAvailable = false;
                      stockIssues.add(productName + " (No longer available)");
                      latestStock.put(itemData.productId, 0); // Mark as 0 stock if product vanished
                  }
                  // IMPORTANT: Close ResultSet inside the loop
                  if (rs != null) { rs.close(); }
                  // No need to clear parameters if using the same PreparedStatement object correctly
             }
             // Close statement AFTER the loop finishes
             // No, PreparedStatement should be closed in finally block

             if (!allAvailable) {
                 // Use invokeLater for UI updates from non-EDT thread (if applicable)
                 // or directly if already on EDT
                 SwingUtilities.invokeLater(() -> {
                     List<Integer> idsToRemove = new ArrayList<>();
                     boolean adjustmentsMade = false;

                     // Iterate over a copy to avoid ConcurrentModificationException if removing items
                     for (CartItemData itemData : new ArrayList<>(cartItemDataMap.values())) {
                          int productId = itemData.productId;
                          int currentStock = latestStock.getOrDefault(productId, 0); // Get latest stock

                          if (currentStock < itemData.quantity) {
                              adjustmentsMade = true;
                              int availableStock = Math.max(0, currentStock);
                              if (availableStock == 0) {
                                  // Remove item entirely
                                  removePanelFromUI(itemData.panelRef);
                                  idsToRemove.add(itemData.cartId); // Mark for removal from map/list
                              } else {
                                  // Adjust quantity
                                  itemData.quantity = availableStock;
                                  if (itemData.quantitySpinnerRef != null) {
                                      itemData.quantitySpinnerRef.setValue(availableStock);
                                  } else { System.err.println("Spinner ref null for cartId " + itemData.cartId); }
                                  if (itemData.itemSubtotalLabelRef != null) {
                                      itemData.itemSubtotalLabelRef.setText(String.format("₱%.2f", itemData.price * availableStock));
                                  } else { System.err.println("Subtotal ref null for cartId " + itemData.cartId); }
                              }
                          }
                     }

                     // Remove items marked for removal from map and cartIds list
                     for (int cartId : idsToRemove) {
                         cartItemDataMap.remove(cartId);
                         cartIds.remove(Integer.valueOf(cartId));
                     }

                     if (adjustmentsMade) {
                         productsPanel.revalidate();
                         productsPanel.repaint();
                         updateSummary(); // Recalculate totals after adjustments
                     }

                     // Now show the message summarizing issues
                     StringBuilder message = new StringBuilder("Stock changed for the following items:\n");
                     for (String issue : stockIssues) {
                         message.append("- ").append(issue).append("\n");
                     }
                     message.append("\nYour order has been adjusted. Please review before placing the order again.");
                     showThemedJOptionPane(message.toString(), "Stock Alert", JOptionPane.WARNING_MESSAGE);

                     // Check if cart became empty after adjustments
                     if (cartItemDataMap.isEmpty()) {
                         showEmptyCheckoutMessage("All items removed due to stock issues.");
                     }
                 });
             }

         } catch (SQLException e) {
             System.err.println("Error during final stock check: " + e.getMessage());
             e.printStackTrace(); // Print stack trace
             showThemedJOptionPane("Error checking stock: " + e.getMessage() + ". Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
             return false; // Prevent proceeding on DB error
         } finally {
             // Ensure ALL resources are closed
             try { if (rs != null && !rs.isClosed()) rs.close(); } catch (SQLException ignored) {}
             try { if (stmt != null && !stmt.isClosed()) stmt.close(); } catch (SQLException ignored) {}
             try { if (conn != null && !conn.isClosed()) conn.close(); } catch (SQLException ignored) {}
         }

         return allAvailable; // Return true only if all items had sufficient stock initially
     }

    // Helper to remove a panel from the UI
    private void removePanelFromUI(JPanel panelToRemove) {
        if (panelToRemove == null) return;
        int componentIndex = -1;
        for (int i = 0; i < productsPanel.getComponentCount(); i++) {
            if (productsPanel.getComponent(i) == panelToRemove) {
                componentIndex = i;
                break;
            }
        }
        if (componentIndex != -1) {
            productsPanel.remove(componentIndex);
            // Remove spacer after it if exists
            if (componentIndex < productsPanel.getComponentCount() && productsPanel.getComponent(componentIndex) instanceof Box.Filler) {
                productsPanel.remove(componentIndex);
            }
        }
    }


    // --- UPDATED Field Validation (COD Removed) ---
     private boolean validatePaymentFields() {
        String method = (String) paymentMethodBox.getSelectedItem();

        if ("Credit/Debit Card".equals(method)) {
            // Fields are already empty, labels are separate. Check if user has typed anything.
            if (cardNumberField.getText().trim().isEmpty() ||
                cvvField.getText().trim().isEmpty() ||
                expiryField.getText().trim().isEmpty()) {
                showThemedJOptionPane("Please fill all credit/debit card fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            if (!cardNumberField.getText().trim().matches("\\d{16}")) {
                showThemedJOptionPane("Card number must be 16 digits.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                 cardNumberField.requestFocus(); // Help user find the error
                return false;
            }
            if (!cvvField.getText().trim().matches("\\d{3,4}")) {
                showThemedJOptionPane("CVV must be 3 or 4 digits.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                 cvvField.requestFocus();
                return false;
            }
            if (!expiryField.getText().trim().matches("(0[1-9]|1[0-2])/([0-9]{2})")) {
                 showThemedJOptionPane("Expiry date must be in MM/YY format.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                 expiryField.requestFocus();
                 return false;
             }
             // Check if expiry date is in the future
             try {
                 String[] parts = expiryField.getText().trim().split("/");
                 int month = Integer.parseInt(parts[0]);
                 int year = Integer.parseInt("20" + parts[1]);
                 Calendar now = Calendar.getInstance();
                 Calendar expiryCal = Calendar.getInstance();
                 expiryCal.clear();
                 expiryCal.set(Calendar.YEAR, year);
                 expiryCal.set(Calendar.MONTH, month - 1); // Month is 0-based
                 // Set day to the last day of the month to check expiration correctly
                 expiryCal.set(Calendar.DAY_OF_MONTH, expiryCal.getActualMaximum(Calendar.DAY_OF_MONTH));
                 // Set time to end of day for comparison clarity (optional)
                 expiryCal.set(Calendar.HOUR_OF_DAY, 23);
                 expiryCal.set(Calendar.MINUTE, 59);
                 expiryCal.set(Calendar.SECOND, 59);


                 if (expiryCal.before(now)) {
                     showThemedJOptionPane("Card has expired.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                      expiryField.requestFocus();
                     return false;
                 }
             } catch (Exception e) {
                  showThemedJOptionPane("Invalid expiry date format or calculation error.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                   expiryField.requestFocus();
                  e.printStackTrace(); // Log error
                  return false;
             }

        } else if ("GCash".equals(method) || "Maya".equals(method)) {
             // Fields are already empty, labels are separate. Check if user has typed anything.
            if (accountNameField.getText().trim().isEmpty()) {
                 showThemedJOptionPane("Please fill in the " + method + " Account Name.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                 accountNameField.requestFocus();
                 return false;
             }
            if (accountNumberField.getText().trim().isEmpty()) {
                 showThemedJOptionPane("Please fill in the " + method + " Account Number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                 accountNumberField.requestFocus();
                 return false;
            }
            // Simple validation for PH mobile number format (09xxxxxxxxx)
            if (!accountNumberField.getText().trim().matches("09\\d{9}")) {
                 showThemedJOptionPane("Invalid " + method + " number format (must be 09xxxxxxxxx).", "Validation Error", JOptionPane.WARNING_MESSAGE);
                 accountNumberField.requestFocus();
                 return false;
             }
        } else if ("PayPal".equals(method)) {
             // Fields are already empty, labels are separate. Check if user has typed anything.
              if (accountNameField.getText().trim().isEmpty()) {
                  showThemedJOptionPane("Please fill in the PayPal Account Name.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                  accountNameField.requestFocus();
                  return false;
              }
             if (accountNumberField.getText().trim().isEmpty()) {
                 showThemedJOptionPane("Please fill in the PayPal Email or Phone Number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                 accountNumberField.requestFocus();
                 return false;
             }
             // Basic check if account number looks like an email or phone
             String payPalIdentifier = accountNumberField.getText().trim();
             // Slightly improved check: contains '@' OR starts with '+' and has digits OR is just digits (allows local numbers too)
             if (!payPalIdentifier.contains("@") && !payPalIdentifier.matches("^(\\+?\\d+)$")) {
                  showThemedJOptionPane("Please enter a valid email address or phone number for PayPal.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                  accountNumberField.requestFocus();
                  return false;
             }
        }
        // COD validation is removed
        return true;
    }

    // --- Helpers (Keep or Adapt) ---
    private JPanel createRightAlignedPanel(JLabel label) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setOpaque(false);
        panel.add(label);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, label.getPreferredSize().height + 5));
        return panel;
    }

    private JLabel createSummaryLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(ThemeColors.TEXT);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        return label;
    }

    // Instance method version of createStyledButton used within CheckoutFrame
    private JButton createStyledButton(String text, Color bgColor) {
        return createStyledButtonStatic(text, bgColor); // Delegate to static version
    }

    // --- Static Helper methods moved from CheckoutFrame for reuse ---
     // (Needed by the static inner dialog classes)

    public static JButton createStyledButtonStatic(String text, Color bgColor) {
         JButton button = new JButton(text);
         button.setFont(new Font("Arial", Font.BOLD, 14));
         button.setBackground(bgColor);
         button.setForeground(Color.WHITE);
         button.setFocusPainted(false);
         button.setBorder(new EmptyBorder(10, 20, 10, 20));
         button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

         Color hoverColor = (bgColor == ThemeColors.PRIMARY) ? ThemeColors.BUTTON_HOVER : bgColor.brighter();
         Color originalFg = button.getForeground();
         button.addMouseListener(new MouseAdapter() {
             @Override public void mouseEntered(MouseEvent e) { button.setBackground(hoverColor); }
             @Override public void mouseExited(MouseEvent e) { button.setBackground(bgColor); button.setForeground(originalFg); }
         });
         return button;
     }

      // This method correctly creates an EMPTY text field.
      // Placeholder text is NOT added here. Labels are handled separately.
      public static JTextField createFormTextFieldStatic() {
          JTextField field = new JTextField(15); // Creates an empty field
          field.setFont(new Font("Arial", Font.PLAIN, 14));
          field.setBackground(ThemeColors.CARD_BG);
          field.setForeground(ThemeColors.TEXT);
          field.setCaretColor(ThemeColors.TEXT);
          field.setBorder(BorderFactory.createCompoundBorder(
              BorderFactory.createLineBorder(ThemeColors.SECONDARY, 1),
              new EmptyBorder(8, 8, 8, 8)
          ));
          return field;
      }

       // This method creates the LABEL that goes NEXT TO the text field.
       public static JLabel createFormLabelStatic(String text) {
          JLabel label = new JLabel(text); // The descriptive text (e.g., "Card Number:")
          label.setFont(new Font("Arial", Font.BOLD, 14));
          label.setForeground(ThemeColors.TEXT);
          return label;
      }

       // Keep ONLY the generic static version
       public static <T> JComboBox<T> styleComboBoxStatic(JComboBox<T> comboBox) {
          comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
          comboBox.setBackground(ThemeColors.CARD_BG);
          comboBox.setForeground(ThemeColors.TEXT);
          comboBox.setBorder(BorderFactory.createCompoundBorder(
               BorderFactory.createLineBorder(ThemeColors.SECONDARY, 1),
               BorderFactory.createEmptyBorder(5, 5, 5, 5)
           ));
          // Custom renderer for dropdown items
          comboBox.setRenderer(new DefaultListCellRenderer() {
              @Override
              public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                  super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                  setBackground(isSelected ? ThemeColors.PRIMARY : ThemeColors.CARD_BG);
                  setForeground(isSelected ? Color.WHITE : ThemeColors.TEXT);
                  setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                  return this;
              }
          });
          // Make the main combo box display area opaque for consistent background
          // Note: This might affect look and feel on some systems. Test if needed.
          // ((JComponent) comboBox.getRenderer()).setOpaque(true);
          // comboBox.setOpaque(true); // Might be needed on some LaF

          return comboBox;
      }

      public static void styleScrollPaneStatic(JScrollPane scrollPane) {
         JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
         verticalScrollBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
             @Override
             protected void configureScrollBarColors() {
                 thumbColor = ThemeColors.PRIMARY; // Or ThemeColors.SECONDARY
                 trackColor = ThemeColors.CARD_BG; // Background of the track
                 // Optional: Set colors for hover/drag
                 // thumbHighlightColor = thumbColor.brighter();
                 // thumbDarkShadowColor = thumbColor.darker();
                 // thumbLightShadowColor = thumbColor; // No shadow effect
             }
             @Override protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
             @Override protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
             private JButton createZeroButton() {
                 JButton button = new JButton();
                 Dimension zeroDim = new Dimension(0, 0);
                 button.setPreferredSize(zeroDim); button.setMinimumSize(zeroDim); button.setMaximumSize(zeroDim);
                 return button;
             }
             @Override
             protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                 if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
                 Graphics2D g2 = (Graphics2D) g.create();
                 g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                 // Determine color based on state (optional)
                 // g2.setColor(isDragging ? thumbDarkShadowColor : isThumbRollover() ? thumbHighlightColor : thumbColor);
                 g2.setColor(thumbColor);
                 // Draw a slightly rounded thumb, inset slightly for padding
                 g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4, 8, 8); // Increased rounding
                 g2.dispose();
             }
             @Override
             protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                 g.setColor(trackColor);
                 g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
                 // Optional: Add a border to the track
                 // g.setColor(ThemeColors.SECONDARY);
                 // g.drawRect(trackBounds.x, trackBounds.y, trackBounds.width - 1, trackBounds.height - 1);
             }
         });
         verticalScrollBar.setBackground(ThemeColors.BACKGROUND); // Match main background
         verticalScrollBar.setBorder(null);
         // Increase scrollbar width slightly
         verticalScrollBar.setPreferredSize(new Dimension(12, Integer.MAX_VALUE));


         // Style horizontal scrollbar similarly if needed
         JScrollBar horizontalScrollBar = scrollPane.getHorizontalScrollBar();
         horizontalScrollBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
              @Override protected void configureScrollBarColors() { thumbColor = ThemeColors.PRIMARY; trackColor = ThemeColors.CARD_BG; }
              @Override protected JButton createDecreaseButton(int o) { return createZeroButton(); }
              @Override protected JButton createIncreaseButton(int o) { return createZeroButton(); }
              private JButton createZeroButton() { JButton b=new JButton(); Dimension d=new Dimension(0,0); b.setPreferredSize(d); b.setMinimumSize(d); b.setMaximumSize(d); return b; }
              @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) { if(r.isEmpty()||!scrollbar.isEnabled()) return; Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(thumbColor); g2.fillRoundRect(r.x+2,r.y+2,r.width-4,r.height-4,8,8); g2.dispose(); }
              @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) { g.setColor(trackColor); g.fillRect(r.x,r.y,r.width,r.height); }
         });
         horizontalScrollBar.setBackground(ThemeColors.BACKGROUND);
         horizontalScrollBar.setBorder(null);
         horizontalScrollBar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 12)); // Increase height
     }

      // --- Action for the Back Button ---
     private void goBackToShop() {
         // Add a confirmation since going back from full screen might feel abrupt
         int confirm = JOptionPane.showConfirmDialog(this,
                 "Are you sure you want to cancel checkout and return to the shop?\nItems will remain in your cart.",
                 "Cancel Checkout?",
                 JOptionPane.YES_NO_OPTION,
                 JOptionPane.QUESTION_MESSAGE);

         if (confirm == JOptionPane.YES_OPTION) {
             // Close this checkout frame
             dispose();

             // Show the CustomerFrame again
             if (customerFrameInstance != null) {
                 // *** Ensure CustomerFrame is also maximized if desired, or restored ***
                 // Example: Restore if CustomerFrame wasn't meant to be full screen
                 // if(customerFrameInstance.getExtendedState() == JFrame.MAXIMIZED_BOTH) {
                 //     customerFrameInstance.setExtendedState(JFrame.NORMAL);
                 // }
                 customerFrameInstance.setVisible(true); // Make sure it's visible
                 customerFrameInstance.toFront(); // Bring existing frame to front
                 customerFrameInstance.showCard("Cart"); // Example: Go back to cart or shop
             } else {
                 // Fallback: Recreate if necessary (shouldn't ideally happen)
                 SwingUtilities.invokeLater(() -> new CustomerFrame(customerId).setVisible(true));
             }
         }
     }

     // Instance method version of showThemedJOptionPane
     private void showThemedJOptionPane(String message, String title, int messageType) {
         // Use the static helper from AddressManager to ensure consistency
         AddressManager.showThemedJOptionPaneStatic(this, message, title, messageType);
     }


    // --- Inner Class: Address Management Dialog (No changes needed for this request) ---
    public static class AddressManagementDialog extends JDialog {
        private int customerId;
        private AddressManager addressManager;
        private Address selectedAddress;
        private Address initiallySelectedAddress; // To revert if cancelled

        private JPanel addressesPanel;
        private ButtonGroup addressGroup;
        private Map<Integer, JRadioButton> addressRadioMap = new HashMap<>();
        private JButton useButton, addButton, closeButton;
        private JScrollPane scrollPane; // To manage scrolling


        // Constructor accepts Window (JFrame or JDialog)
        public AddressManagementDialog(Window parent, int customerId, AddressManager manager, Address currentSelection) {
            super(parent, "Manage Delivery Addresses", ModalityType.APPLICATION_MODAL); // Modal
            this.customerId = customerId;
            // Use the manager passed from the caller (CheckoutFrame)
            this.addressManager = manager; // Directly use the passed manager
            // If manager is null, create one? Or throw error? For now, assume caller provides a valid one.
            if (this.addressManager == null) {
                 System.err.println("CRITICAL: AddressManager is null in AddressManagementDialog constructor.");
                 // Optionally throw an exception or handle gracefully
                 // For now, create a fallback:
                 System.out.println("Creating fallback AddressManager for AddressManagementDialog.");
                 // Need a parent window for the fallback manager's dialogs... this could be problematic.
                 // Better to ensure a valid manager is always passed.
                 // For safety, pass 'this' dialog as the parent if creating a fallback.
                 this.addressManager = new AddressManager(this, customerId);
             }
            this.selectedAddress = currentSelection;
            this.initiallySelectedAddress = currentSelection; // Store initial selection

            setLayout(new BorderLayout(10, 10));
            setSize(700, 550); // Adjust size as needed
            setLocationRelativeTo(parent);
            getContentPane().setBackground(ThemeColors.BACKGROUND);
            getRootPane().setBorder(BorderFactory.createLineBorder(ThemeColors.PRIMARY, 2)); // Add a border

            // Title Panel
            JLabel titleLabel = new JLabel("Select or Manage Addresses", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
            titleLabel.setForeground(ThemeColors.PRIMARY);
            titleLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
            add(titleLabel, BorderLayout.NORTH);

            // Address List Panel
            addressesPanel = new JPanel();
            addressesPanel.setLayout(new BoxLayout(addressesPanel, BoxLayout.Y_AXIS));
            addressesPanel.setBackground(ThemeColors.BACKGROUND);
            addressesPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

             scrollPane = new JScrollPane(addressesPanel);
             CheckoutFrame.styleScrollPaneStatic(scrollPane); // Use static version from CheckoutFrame
             scrollPane.setBorder(BorderFactory.createLineBorder(ThemeColors.SECONDARY));
             scrollPane.getViewport().setBackground(ThemeColors.BACKGROUND);
             add(scrollPane, BorderLayout.CENTER);


            // Button Panel
            JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
            bottomPanel.setOpaque(false);
            bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

             // Use static button creator from CheckoutFrame
             addButton = CheckoutFrame.createStyledButtonStatic("Add New Address", ThemeColors.SECONDARY);
             addButton.addActionListener(e -> showAddEditDialog(null)); // null indicates Add mode
            bottomPanel.add(addButton, BorderLayout.WEST);

            JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            actionButtonsPanel.setOpaque(false);
             closeButton = CheckoutFrame.createStyledButtonStatic("Cancel", ThemeColors.CARD_BG);
            closeButton.setForeground(ThemeColors.TEXT);
            closeButton.addActionListener(e -> {
                 selectedAddress = initiallySelectedAddress; // Revert selection on cancel
                 dispose();
            });
             useButton = CheckoutFrame.createStyledButtonStatic("Use Selected Address", ThemeColors.PRIMARY);
             useButton.addActionListener(e -> {
                // selectedAddress is updated by radio button listeners
                 if (selectedAddress == null) {
                       // Use static helper for themed JOptionPane from AddressManager
                       AddressManager.showThemedJOptionPaneStatic(this, "Please select an address first.", "No Selection", JOptionPane.WARNING_MESSAGE);
                 } else {
                    dispose(); // Close dialog, selectedAddress holds the choice
                 }
            });
            actionButtonsPanel.add(closeButton);
            actionButtonsPanel.add(useButton);
            bottomPanel.add(actionButtonsPanel, BorderLayout.EAST);

            add(bottomPanel, BorderLayout.SOUTH);

            loadAndDisplayAddresses(); // Load addresses into the panel
        }

        private void loadAndDisplayAddresses() {
            addressesPanel.removeAll();
            addressGroup = new ButtonGroup();
            addressRadioMap.clear();

            List<Address> addresses = addressManager.getCustomerAddresses();

            if (addresses == null || addresses.isEmpty()) { // Added null check
                addressesPanel.setLayout(new BorderLayout()); // Reset layout for single label
                JLabel noAddressLabel = new JLabel("No saved addresses. Click 'Add New Address'.", SwingConstants.CENTER);
                noAddressLabel.setFont(new Font("Arial", Font.ITALIC, 14));
                noAddressLabel.setForeground(Color.GRAY);
                addressesPanel.add(noAddressLabel, BorderLayout.CENTER);
                useButton.setEnabled(false); // Disable use button if no addresses
            } else {
                 addressesPanel.setLayout(new BoxLayout(addressesPanel, BoxLayout.Y_AXIS)); // Reset layout
                for (Address addr : addresses) {
                    JPanel card = createAddressCard(addr);
                    addressesPanel.add(card);
                    addressesPanel.add(Box.createRigidArea(new Dimension(0, 8))); // Spacer
                }
                // Ensure initial selection is reflected
                 selectCurrentAddressRadio();
                 useButton.setEnabled(selectedAddress != null); // Enable button if an address is selected
            }

            addressesPanel.revalidate();
            addressesPanel.repaint();
             // Scroll to top after loading/refreshing
             SwingUtilities.invokeLater(() -> {
                 if (scrollPane != null && scrollPane.getVerticalScrollBar() != null) {
                     scrollPane.getVerticalScrollBar().setValue(0);
                 }
             });
        }

         private void selectCurrentAddressRadio() {
            if (selectedAddress != null) {
                JRadioButton radioToSelect = addressRadioMap.get(selectedAddress.getId());
                if (radioToSelect != null) {
                    // Need to delay selection slightly sometimes for ButtonGroup to register correctly
                    SwingUtilities.invokeLater(() -> radioToSelect.setSelected(true));
                     System.out.println("DEBUG: Selecting radio for address ID: " + selectedAddress.getId());
                } else {
                    System.err.println("WARN: Radio button not found in map for selected address ID: " + selectedAddress.getId());
                    if(addressGroup != null) addressGroup.clearSelection(); // Clear selection if radio not found
                }
            } else {
                 if(addressGroup != null) addressGroup.clearSelection(); // Clear if no address selected initially
            }
         }


        private JPanel createAddressCard(Address address) {
            JPanel cardPanel = new JPanel(new BorderLayout(10, 5));
            cardPanel.setBackground(ThemeColors.CARD_BG);
            Border line = new LineBorder(ThemeColors.SECONDARY);
            Border padding = new EmptyBorder(10, 10, 10, 10);
            cardPanel.setBorder(new CompoundBorder(line, padding));
            cardPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130)); // Constraint height
            cardPanel.setPreferredSize(new Dimension(600, 110)); // Preferred height

            // Radio Button (West)
            JRadioButton radio = new JRadioButton();
            styleRadioButton(radio); // Use helper method
            addressGroup.add(radio);
            addressRadioMap.put(address.getId(), radio);
            radio.addActionListener(e -> {
                if (radio.isSelected()) {
                    selectedAddress = address; // Update selection when radio is clicked
                    useButton.setEnabled(true); // Enable the use button
                     System.out.println("DEBUG: Address selected via radio: ID " + address.getId());
                }
            });

            JPanel radioWrapper = new JPanel(new GridBagLayout()); // To center vertically
            radioWrapper.setOpaque(false);
            radioWrapper.add(radio);
            cardPanel.add(radioWrapper, BorderLayout.WEST);

            // Address Details (Center)
            JLabel addressLabel = new JLabel(address.getFullAddressForDisplay()); // Already includes HTML
            addressLabel.setForeground(ThemeColors.TEXT);
            addressLabel.setFont(new Font("Arial", Font.PLAIN, 13));
            addressLabel.setVerticalAlignment(SwingConstants.TOP);
            cardPanel.add(addressLabel, BorderLayout.CENTER);

            // Buttons (Edit, Delete) (East)
            JPanel buttonsPanel = new JPanel();
            buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
            buttonsPanel.setOpaque(false);

             // Use static button creator from CheckoutFrame
             JButton editButton = CheckoutFrame.createStyledButtonStatic("Edit", ThemeColors.SECONDARY);
             JButton deleteButton = CheckoutFrame.createStyledButtonStatic("Delete", new Color(200, 50, 50)); // Dark Red


            // Consistent button size
            Dimension prefSize = editButton.getPreferredSize();
            Dimension deletePrefSize = deleteButton.getPreferredSize();
            int uniformWidth = Math.max(prefSize.width, deletePrefSize.width);
            uniformWidth = Math.max(uniformWidth, 80); // Ensure minimum width
            Dimension uniformSize = new Dimension(uniformWidth, prefSize.height);

            editButton.setPreferredSize(uniformSize); editButton.setMaximumSize(uniformSize);
            deleteButton.setPreferredSize(uniformSize); deleteButton.setMaximumSize(uniformSize);
            editButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            deleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);

            editButton.addActionListener(e -> showAddEditDialog(address));
            deleteButton.addActionListener(e -> handleDeleteAddress(address));

            buttonsPanel.add(Box.createVerticalGlue()); // Push to center
            buttonsPanel.add(editButton);
            buttonsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            buttonsPanel.add(deleteButton);
            buttonsPanel.add(Box.createVerticalGlue()); // Push to center

            cardPanel.add(buttonsPanel, BorderLayout.EAST);

            return cardPanel;
        }


        private void showAddEditDialog(Address addressToEdit) {
            // Create a *new* modal dialog for adding/editing
            // Pass 'this' (the AddressManagementDialog) as the parent
             AddressFormDialog formDialog = new AddressFormDialog(this, addressManager, customerId, addressToEdit);
             formDialog.setVisible(true);

            // After the form dialog is closed, refresh the address list in this dialog
            if (formDialog.isSaved()) { // Check if changes were saved
                 Address savedAddr = formDialog.getSavedAddress();
                 // Try to determine the currently selected address ID *before* refreshing
                 int previouslySelectedId = (selectedAddress != null) ? selectedAddress.getId() : -1;

                 loadAndDisplayAddresses(); // Refresh the list

                 // Update selection logic AFTER refreshing the list
                 if (savedAddr != null) {
                     // If an address was saved (new or edit), try to select it
                     selectedAddress = findAddressInListById(savedAddr.getId());
                     // If the saved one was default, make sure it's the current selectedAddress
                     if (selectedAddress != null && selectedAddress.isDefault()) {
                          // Correct, do nothing special
                     } else if (savedAddr.isDefault()) {
                          // If the saved one was default but couldn't be found by ID or wasn't selected,
                          // explicitly fetch the default address again.
                          selectedAddress = addressManager.getDefaultAddress();
                     }
                 } else if (previouslySelectedId != -1) {
                     // If nothing was saved (e.g., edit cancelled), try to re-select the previously selected address
                     selectedAddress = findAddressInListById(previouslySelectedId);
                 } else {
                     // Fallback: if nothing was selected before and nothing saved, select the default
                     selectedAddress = addressManager.getDefaultAddress();
                 }

                 // Ensure the initial selection reflects the potential changes (e.g. if default changed)
                 initiallySelectedAddress = selectedAddress;

                 // Select the corresponding radio button
                 selectCurrentAddressRadio();
                 useButton.setEnabled(selectedAddress != null); // Enable/disable based on final selection
            }
        }

        // Helper to find an address object from the refreshed list by ID
        private Address findAddressInListById(int addressId) {
             if (addressId <= 0) return null; // Invalid ID
             List<Address> currentAddresses = addressManager.getCustomerAddresses(); // Fetch again
             if (currentAddresses == null) return null; // Handle case where list is null
             for (Address addr : currentAddresses) {
                 if (addr.getId() == addressId) {
                     return addr;
                 }
             }
             return null; // Not found
         }

         private void handleDeleteAddress(Address addressToDelete) {
            if (addressToDelete == null) return;

            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this address?\n" + addressToDelete.getFullAddressForDisplay().replaceAll("<[^>]*>", ""), // Clean HTML for prompt
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = addressManager.deleteAddressFromDatabase(addressToDelete.getId());
                if (success) {
                     AddressManager.showThemedJOptionPaneStatic(this,"Address deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    // If the deleted address was the selected one, clear selection and try to select default
                    if (selectedAddress != null && selectedAddress.getId() == addressToDelete.getId()) {
                        selectedAddress = addressManager.getDefaultAddress(); // Try selecting the new default
                        initiallySelectedAddress = selectedAddress; // Update initial selection as well
                    }
                    loadAndDisplayAddresses(); // Refresh the list (will also update radio selection)
                } else {
                    // Error message potentially handled within deleteAddressFromDatabase or show generic
                    AddressManager.showThemedJOptionPaneStatic(this, "Failed to delete address. Please try again.", "Deletion Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        // Style radio button (Helper)
        private void styleRadioButton(JRadioButton r) {
             r.setIcon(new RadioButtonIcon(false));
             r.setSelectedIcon(new RadioButtonIcon(true));
             r.setOpaque(false);
             r.setContentAreaFilled(false);
             r.setBorderPainted(false);
             r.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
             // Add padding/margin if needed
             // r.setMargin(new Insets(2, 2, 2, 2));
        }

        // Custom Radio Button Icon (Helper) - Made static
         private static class RadioButtonIcon implements Icon {
             private final boolean selected;
             private static final int DIAMETER = 16;
             private static final int PADDING = 4; // Padding around icon

             public RadioButtonIcon(boolean selected) { this.selected = selected; }

             @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                 Graphics2D g2 = (Graphics2D) g.create();
                 g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                 // Adjust drawing position based on padding
                 int drawX = x + PADDING / 2;
                 int drawY = y + PADDING / 2;

                 // Outer circle
                 g2.setColor(ThemeColors.SECONDARY);
                 g2.setStroke(new BasicStroke(1.5f)); // Slightly thicker border
                 g2.drawOval(drawX, drawY, DIAMETER, DIAMETER);

                 // Inner circle (if selected)
                 if (selected) {
                     g2.setColor(ThemeColors.PRIMARY);
                     int innerDiameter = DIAMETER / 2;
                     // Center the inner circle
                     g2.fillOval(drawX + (DIAMETER - innerDiameter) / 2, drawY + (DIAMETER - innerDiameter) / 2, innerDiameter, innerDiameter);
                 }
                 g2.dispose();
             }

             @Override public int getIconWidth() { return DIAMETER + PADDING; }
             @Override public int getIconHeight() { return DIAMETER + PADDING; }
         }

        public Address getSelectedAddress() {
            return selectedAddress;
        }

     } // --- End AddressManagementDialog Inner Class ---


    // --- Inner Class: Address Form Dialog (No changes needed for this request) ---
    public static class AddressFormDialog extends JDialog {
        private AddressManager addressManager;
        private int customerId;
        private Address addressToEdit; // Null if adding new
        private boolean isSaved = false;
        private Address savedAddress = null; // Store the saved/updated address

        // Form Components
        private JTextField recipientNameField, phoneField, streetAddressField;
        private JComboBox<Region> regionCombo;
        private JComboBox<Province> provinceCombo;
        private JComboBox<Municipality> cityCombo;
        private JComboBox<Barangay> barangayCombo;
        private JCheckBox defaultAddressCheckbox;
        private JButton saveButton, cancelButton;

        public AddressFormDialog(JDialog parent, AddressManager manager, int custId, Address editAddress) {
            super(parent, (editAddress == null ? "Add New Address" : "Edit Address"), true);
            // Use the AddressManager passed from the parent dialog (AddressManagementDialog)
            this.addressManager = manager;
             if (this.addressManager == null) {
                 System.err.println("CRITICAL: AddressManager is null in AddressFormDialog constructor.");
                 // Create a fallback:
                 System.out.println("Creating fallback AddressManager for AddressFormDialog.");
                 // Pass parent JDialog as parent window
                 this.addressManager = new AddressManager(parent, custId);
             }
            this.customerId = custId;
            this.addressToEdit = editAddress;

            setSize(550, 520); // Adjust size slightly for padding
            setLayout(new BorderLayout(10, 10));
            getContentPane().setBackground(ThemeColors.BACKGROUND);
            setLocationRelativeTo(parent);
            getRootPane().setBorder(BorderFactory.createLineBorder(ThemeColors.PRIMARY));


            // --- Form Panel ---
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(ThemeColors.BACKGROUND);
            formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 8, 8, 8);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            int gridY = 0;

            // Initialize fields - Use static helpers from CheckoutFrame
            recipientNameField = CheckoutFrame.createFormTextFieldStatic(); // Creates EMPTY field
            phoneField = CheckoutFrame.createFormTextFieldStatic();         // Creates EMPTY field
            streetAddressField = CheckoutFrame.createFormTextFieldStatic(); // Creates EMPTY field
            regionCombo = CheckoutFrame.styleComboBoxStatic(new JComboBox<>());
            provinceCombo = CheckoutFrame.styleComboBoxStatic(new JComboBox<>());
            cityCombo = CheckoutFrame.styleComboBoxStatic(new JComboBox<>());
            barangayCombo = CheckoutFrame.styleComboBoxStatic(new JComboBox<>());
            defaultAddressCheckbox = new JCheckBox("Set as default address");
            defaultAddressCheckbox.setBackground(ThemeColors.BACKGROUND);
            defaultAddressCheckbox.setForeground(ThemeColors.TEXT);
            defaultAddressCheckbox.setFont(new Font("Arial", Font.PLAIN, 14)); // Match field font

            // Layout components - Use static helpers from CheckoutFrame to create LABELS
            gbc.gridx = 0; gbc.gridy = gridY; gbc.weightx = 0.0; gbc.fill=GridBagConstraints.NONE; gbc.anchor=GridBagConstraints.EAST; formPanel.add(CheckoutFrame.createFormLabelStatic("Recipient Name:"), gbc); // LABEL
            gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill=GridBagConstraints.HORIZONTAL; gbc.anchor=GridBagConstraints.WEST; formPanel.add(recipientNameField, gbc); gridY++; // FIELD

            gbc.gridx = 0; gbc.gridy = gridY; gbc.weightx = 0.0; gbc.fill=GridBagConstraints.NONE; gbc.anchor=GridBagConstraints.EAST; formPanel.add(CheckoutFrame.createFormLabelStatic("Phone Number:"), gbc);   // LABEL
            gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill=GridBagConstraints.HORIZONTAL; gbc.anchor=GridBagConstraints.WEST; formPanel.add(phoneField, gbc); gridY++; // FIELD

            gbc.gridx = 0; gbc.gridy = gridY; gbc.weightx = 0.0; gbc.fill=GridBagConstraints.NONE; gbc.anchor=GridBagConstraints.EAST; formPanel.add(CheckoutFrame.createFormLabelStatic("Region:"), gbc);          // LABEL
            gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill=GridBagConstraints.HORIZONTAL; gbc.anchor=GridBagConstraints.WEST; regionCombo.addItem(new Region("-1", "-- Select Region --")); addressManager.getAllRegions().forEach(regionCombo::addItem); formPanel.add(regionCombo, gbc); gridY++;

            gbc.gridx = 0; gbc.gridy = gridY; gbc.weightx = 0.0; gbc.fill=GridBagConstraints.NONE; gbc.anchor=GridBagConstraints.EAST; formPanel.add(CheckoutFrame.createFormLabelStatic("Province:"), gbc);        // LABEL
            gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill=GridBagConstraints.HORIZONTAL; gbc.anchor=GridBagConstraints.WEST; provinceCombo.addItem(new Province("-1", "-- Select Province --")); provinceCombo.setEnabled(false); formPanel.add(provinceCombo, gbc); gridY++;

            gbc.gridx = 0; gbc.gridy = gridY; gbc.weightx = 0.0; gbc.fill=GridBagConstraints.NONE; gbc.anchor=GridBagConstraints.EAST; formPanel.add(CheckoutFrame.createFormLabelStatic("City/Municipality:"), gbc);// LABEL
            gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill=GridBagConstraints.HORIZONTAL; gbc.anchor=GridBagConstraints.WEST; cityCombo.addItem(new Municipality("-1", "-- Select City/Municipality --")); cityCombo.setEnabled(false); formPanel.add(cityCombo, gbc); gridY++;

            gbc.gridx = 0; gbc.gridy = gridY; gbc.weightx = 0.0; gbc.fill=GridBagConstraints.NONE; gbc.anchor=GridBagConstraints.EAST; formPanel.add(CheckoutFrame.createFormLabelStatic("Barangay:"), gbc);         // LABEL
            gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill=GridBagConstraints.HORIZONTAL; gbc.anchor=GridBagConstraints.WEST; barangayCombo.addItem(new Barangay("-1", "-- Select Barangay --")); barangayCombo.setEnabled(false); formPanel.add(barangayCombo, gbc); gridY++;

            gbc.gridx = 0; gbc.gridy = gridY; gbc.weightx = 0.0; gbc.fill=GridBagConstraints.NONE; gbc.anchor=GridBagConstraints.EAST; formPanel.add(CheckoutFrame.createFormLabelStatic("Street Address, House No., etc.:"), gbc); // LABEL
            gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill=GridBagConstraints.HORIZONTAL; gbc.anchor=GridBagConstraints.WEST; formPanel.add(streetAddressField, gbc); gridY++; // FIELD

            gbc.gridx = 0; gbc.gridy = gridY; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
            formPanel.add(defaultAddressCheckbox, gbc); gridY++;

            // Add dropdown listeners
            addDropdownListeners();

            // Populate fields if editing
            if (addressToEdit != null) {
                populateFieldsForEdit();
            }

             JScrollPane formScrollPane = new JScrollPane(formPanel);
             CheckoutFrame.styleScrollPaneStatic(formScrollPane); // Use static styler from CheckoutFrame
             formScrollPane.setBorder(null); // Remove border from scrollpane itself
             formScrollPane.getViewport().setBackground(ThemeColors.BACKGROUND); // Match background
             add(formScrollPane, BorderLayout.CENTER);


            // --- Button Panel ---
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10)); // Add gaps
             buttonPanel.setBackground(ThemeColors.BACKGROUND);
             buttonPanel.setBorder(new EmptyBorder(0, 10, 10, 10)); // Adjust padding

             // Use static helpers from CheckoutFrame
             saveButton = CheckoutFrame.createStyledButtonStatic("Save Address", ThemeColors.PRIMARY);
             cancelButton = CheckoutFrame.createStyledButtonStatic("Cancel", ThemeColors.SECONDARY);


            saveButton.addActionListener(e -> saveAddress());
            cancelButton.addActionListener(e -> dispose());

            buttonPanel.add(cancelButton);
            buttonPanel.add(saveButton);
            add(buttonPanel, BorderLayout.SOUTH);
        }

         private void populateFieldsForEdit() {
            if (addressToEdit == null) return; // Safety check

            recipientNameField.setText(addressToEdit.getRecipientName());
            phoneField.setText(addressToEdit.getPhone());
            streetAddressField.setText(addressToEdit.getStreetAddress());
            defaultAddressCheckbox.setSelected(addressToEdit.isDefault());

            // --- Populate and select dropdowns ---
            // Select Region first, which triggers province loading via listener
             if (addressToEdit.getRegionId() != null) {
                 selectComboBoxItemById(regionCombo, addressToEdit.getRegionId());
             } else {
                 regionCombo.setSelectedIndex(0); // Select "-- Select Region --"
                 // Manually disable downstream combos if region is null/invalid
                 provinceCombo.setEnabled(false); provinceCombo.setSelectedIndex(0);
                 cityCombo.setEnabled(false); cityCombo.setSelectedIndex(0);
                 barangayCombo.setEnabled(false); barangayCombo.setSelectedIndex(0);
                 return; // Stop further selection attempts if region is missing
             }


             // --- Chain dependent updates using invokeLater for robustness ---
             // Ensure previous action (region selection and province loading) completes
             SwingUtilities.invokeLater(() -> {
                  if (addressToEdit.getProvinceId() != null && provinceCombo.isEnabled()) {
                       selectComboBoxItemById(provinceCombo, addressToEdit.getProvinceId());
                       // Ensure city loading completes
                       SwingUtilities.invokeLater(() -> {
                           if (addressToEdit.getMunicipalityId() != null && cityCombo.isEnabled()) {
                               selectComboBoxItemById(cityCombo, addressToEdit.getMunicipalityId());
                                // Ensure barangay loading completes
                                SwingUtilities.invokeLater(() -> {
                                    if (addressToEdit.getBarangayId() != null && barangayCombo.isEnabled()) {
                                        selectComboBoxItemById(barangayCombo, addressToEdit.getBarangayId());
                                    } else if (barangayCombo.isEnabled()) {
                                         barangayCombo.setSelectedIndex(0); // Reset if ID is null but combo enabled
                                     }
                                });
                           } else if (cityCombo.isEnabled()) {
                                cityCombo.setSelectedIndex(0); // Reset if ID is null but combo enabled
                                barangayCombo.setEnabled(false); barangayCombo.setSelectedIndex(0); // Disable downstream
                            }
                       });
                  } else if (provinceCombo.isEnabled()) {
                      provinceCombo.setSelectedIndex(0); // Reset if ID is null but combo enabled
                      cityCombo.setEnabled(false); cityCombo.setSelectedIndex(0); // Disable downstream
                      barangayCombo.setEnabled(false); barangayCombo.setSelectedIndex(0);
                  }
             });
         }

        private void addDropdownListeners() {
             // Use ItemListener for more reliable state change detection than ActionListener
             regionCombo.addItemListener(e -> {
                 if (e.getStateChange() == ItemEvent.SELECTED) {
                     updateProvinces();
                 }
             });
             provinceCombo.addItemListener(e -> {
                 if (e.getStateChange() == ItemEvent.SELECTED) {
                     updateCities();
                 }
             });
             cityCombo.addItemListener(e -> {
                 if (e.getStateChange() == ItemEvent.SELECTED) {
                     updateBarangays();
                 }
             });
        }

        private void updateProvinces() {
            Object selectedItem = regionCombo.getSelectedItem();
            // Check if the selected item is a valid Region object and not the placeholder
            if (!(selectedItem instanceof Region) || "-1".equals(((Region) selectedItem).getId())) {
                 provinceCombo.removeAllItems();
                 provinceCombo.addItem(new Province("-1", "-- Select Province --"));
                 provinceCombo.setEnabled(false);
                 updateCities(); // Cascade reset
                 return;
             }

             Region selectedRegion = (Region) selectedItem;
             System.out.println("Region selected: " + selectedRegion.getName() + " (ID: " + selectedRegion.getId() + ")"); // Debug

             List<Province> provinces = addressManager.getProvincesByRegion(selectedRegion.getId());
             provinceCombo.removeAllItems();
             provinceCombo.addItem(new Province("-1", "-- Select Province --"));
             if (provinces != null) {
                 provinces.forEach(provinceCombo::addItem);
                 provinceCombo.setEnabled(true);
             } else {
                 provinceCombo.setEnabled(false);
                  System.err.println("No provinces found for region ID: " + selectedRegion.getId()); // Debug
             }
             provinceCombo.setSelectedIndex(0); // Ensure placeholder is selected initially
             updateCities(); // Cascade update/reset
        }

        private void updateCities() {
            Object selectedItem = provinceCombo.getSelectedItem();
             if (!(selectedItem instanceof Province) || "-1".equals(((Province) selectedItem).getId()) || !provinceCombo.isEnabled()) {
                 cityCombo.removeAllItems();
                 cityCombo.addItem(new Municipality("-1", "-- Select City/Municipality --"));
                 cityCombo.setEnabled(false);
                 updateBarangays(); // Cascade reset
                 return;
             }

             Province selectedProvince = (Province) selectedItem;
             System.out.println("Province selected: " + selectedProvince.getName() + " (ID: " + selectedProvince.getId() + ")"); // Debug

             List<Municipality> cities = addressManager.getCitiesByProvince(selectedProvince.getId());
             cityCombo.removeAllItems();
             cityCombo.addItem(new Municipality("-1", "-- Select City/Municipality --"));
             if (cities != null) {
                 cities.forEach(cityCombo::addItem);
                 cityCombo.setEnabled(true);
             } else {
                 cityCombo.setEnabled(false);
                 System.err.println("No cities found for province ID: " + selectedProvince.getId()); // Debug
             }
              cityCombo.setSelectedIndex(0); // Ensure placeholder is selected initially
             updateBarangays(); // Cascade update/reset
        }

        private void updateBarangays() {
             Object selectedItem = cityCombo.getSelectedItem();
              if (!(selectedItem instanceof Municipality) || "-1".equals(((Municipality) selectedItem).getId()) || !cityCombo.isEnabled()) {
                  barangayCombo.removeAllItems();
                  barangayCombo.addItem(new Barangay("-1", "-- Select Barangay --"));
                  barangayCombo.setEnabled(false);
                  return;
              }

              Municipality selectedCity = (Municipality) selectedItem;
              System.out.println("City selected: " + selectedCity.getName() + " (ID: " + selectedCity.getId() + ")"); // Debug

              List<Barangay> barangays = addressManager.getBarangaysByCity(selectedCity.getId());
              barangayCombo.removeAllItems();
              barangayCombo.addItem(new Barangay("-1", "-- Select Barangay --"));
              if (barangays != null) {
                  barangays.forEach(barangayCombo::addItem);
                  barangayCombo.setEnabled(true);
              } else {
                  barangayCombo.setEnabled(false);
                   System.err.println("No barangays found for city ID: " + selectedCity.getId()); // Debug
              }
               barangayCombo.setSelectedIndex(0); // Ensure placeholder is selected initially
        }

         // Helper to select item in ComboBox by ID
        private <T> void selectComboBoxItemById(JComboBox<T> comboBox, String targetId) {
            if (targetId == null || targetId.isEmpty() || !comboBox.isEnabled()) {
                 System.out.println("DEBUG: Skipping selection for ID " + targetId + " in " + comboBox.getClass().getSimpleName() + " (ID null/empty or combo disabled)");
                 if(comboBox.isEnabled()) comboBox.setSelectedIndex(0); // Reset to placeholder if enabled but ID is bad
                 return;
             }

             ComboBoxModel<T> model = comboBox.getModel();
             if (model.getSize() <= 1) { // Only placeholder exists
                   System.out.println("DEBUG: Skipping selection for ID " + targetId + " in " + comboBox.getClass().getSimpleName() + " (Model empty)");
                  return;
             }

             for (int i = 0; i < model.getSize(); i++) {
                 T item = model.getElementAt(i);
                 if (item == null) continue; // Skip null items if any

                 String itemId = null;
                 // Use instanceof checks for robustness
                  if (item instanceof Region) itemId = ((Region) item).getId();
                  else if (item instanceof Province) itemId = ((Province) item).getId();
                  else if (item instanceof Municipality) itemId = ((Municipality) item).getId();
                  else if (item instanceof Barangay) itemId = ((Barangay) item).getId();
                  else { // Skip if type is unexpected or placeholder
                      continue;
                  }

                 if (itemId != null && itemId.equals(targetId)) {
                      final int indexToSelect = i;
                       System.out.println("DEBUG: Attempting to select index " + indexToSelect + " for ID " + targetId + " in " + comboBox.getClass().getSimpleName());
                       // Ensure the index is valid before setting
                       if (indexToSelect >= 0 && indexToSelect < model.getSize()) {
                           comboBox.setSelectedIndex(indexToSelect);
                           System.out.println("DEBUG: Successfully selected index " + indexToSelect);
                       } else {
                           System.err.println("[AddressFormDialog WARNING] Calculated index " + indexToSelect + " is out of bounds for " + comboBox.getClass().getSimpleName() + " (Size: " + model.getSize() + ")");
                           comboBox.setSelectedIndex(0); // Fallback to placeholder
                       }
                      return; // Found the item
                 }
             }
             // If loop finishes without finding
             System.err.println("[AddressFormDialog WARNING] Could not find item with ID " + targetId + " in " + comboBox.getClass().getSimpleName() + ". Resetting to placeholder.");
             comboBox.setSelectedIndex(0); // Reset to placeholder if not found
         }

        private void saveAddress() {
            // Validation
            String name = recipientNameField.getText().trim();
            String phone = phoneField.getText().trim();
            String street = streetAddressField.getText().trim();
            Object regionItem = regionCombo.getSelectedItem();
            Object provinceItem = provinceCombo.getSelectedItem();
            Object cityItem = cityCombo.getSelectedItem();
            Object barangayItem = barangayCombo.getSelectedItem();

             // --- Improved Validation ---
             StringBuilder errors = new StringBuilder();
             if (name.isEmpty()) errors.append("- Recipient Name is required.\n");
             if (phone.isEmpty()) errors.append("- Phone Number is required.\n");
             else if (!phone.matches("^(09|\\+639)\\d{9}$")) errors.append("- Invalid Philippine phone number format (09xxxxxxxxx or +639xxxxxxxxx).\n");
             if (!(regionItem instanceof Region) || ((Region)regionItem).getId().equals("-1")) errors.append("- Please select a Region.\n");
             if (!(provinceItem instanceof Province) || ((Province)provinceItem).getId().equals("-1")) errors.append("- Please select a Province.\n");
             if (!(cityItem instanceof Municipality) || ((Municipality)cityItem).getId().equals("-1")) errors.append("- Please select a City/Municipality.\n");
             if (!(barangayItem instanceof Barangay) || ((Barangay)barangayItem).getId().equals("-1")) errors.append("- Please select a Barangay.\n");
             if (street.isEmpty()) errors.append("- Street Address details are required.\n");

             if (errors.length() > 0) {
                 AddressManager.showThemedJOptionPaneStatic(this, "Please correct the following:\n" + errors.toString(), "Incomplete Address", JOptionPane.WARNING_MESSAGE);
                 return;
             }

             // Cast items now that they are validated
             Region region = (Region) regionItem;
             Province province = (Province) provinceItem;
             Municipality city = (Municipality) cityItem;
             Barangay barangay = (Barangay) barangayItem;
             // --- End Improved Validation ---

            boolean isDefault = defaultAddressCheckbox.isSelected();

            // Determine ID for update or use 0 for insert
            int addressId = (addressToEdit != null) ? addressToEdit.getId() : 0;

            // Create Address object
            Address newOrUpdatedAddress = new Address(
                addressId,
                customerId,
                region.getId(), province.getId(), city.getId(), barangay.getId(),
                street, isDefault,
                region.getName(), province.getName(), city.getName(), barangay.getName(),
                name, phone
            );

            boolean success = false;
            try {
                if (addressId == 0) {
                    // Add new address - Assume save method handles setting the ID on the passed object or returns the new object/ID
                    Address result = addressManager.saveAddressToDatabaseAndReturn(newOrUpdatedAddress);
                    if (result != null && result.getId() > 0) {
                         savedAddress = result; // Store the object with the new ID
                         isSaved = true;
                         success = true; // Explicitly set success
                    } else {
                         // Try fetching latest if save didn't return ID (less ideal)
                         System.out.println("Save method didn't return ID, attempting refetch...");
                         success = addressManager.saveAddressToDatabase(newOrUpdatedAddress); // Original method call
                         if (success) {
                              List<Address> updatedList = addressManager.getCustomerAddresses();
                              if (updatedList != null && !updatedList.isEmpty()) {
                                  savedAddress = updatedList.get(0); // Assume latest is correct
                                  if (savedAddress.getStreetAddress().equals(street) && savedAddress.getRecipientName().equals(name)) {
                                        isSaved = true;
                                  } else { savedAddress = null; isSaved = false; } // Couldn't confirm
                              } else { savedAddress = null; isSaved = false; } // Fetch failed
                         }
                    }

                } else {
                    // Update existing address
                    success = addressManager.updateAddressInDatabase(newOrUpdatedAddress);
                     if (success) {
                         isSaved = true;
                         savedAddress = newOrUpdatedAddress; // ID is already known
                     }
                }
            } catch (Exception ex) {
                success = false;
                 System.err.println("Error saving/updating address: " + ex.getMessage());
                 ex.printStackTrace();
                 AddressManager.showThemedJOptionPaneStatic(this, "An error occurred while saving the address: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }


            if (isSaved && success) { // Check both flags
                String message = (addressToEdit == null) ? "Address added successfully!" : "Address updated successfully!";
                AddressManager.showThemedJOptionPaneStatic(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else if (!success) { // If success is false, likely an error message was already shown or logged
                 System.err.println("Address save/update failed. Success flag is false.");
                 isSaved = false;
                 savedAddress = null;
                 // Optionally show a generic error if not already shown
                 // AddressManager.showThemedJOptionPaneStatic(this, "Failed to save address.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        public boolean isSaved() {
            return isSaved;
        }
         public Address getSavedAddress() {
             return savedAddress;
         }

    } // --- End AddressFormDialog Inner Class ---


    // Helper needed by static dialogs
    public static class DBConnection {
        private static final String URL = "jdbc:mysql://localhost:3306/kpop_merch_store";
        private static final String USER = "root";
        private static final String PASSWORD = ""; // Replace with your actual password if needed

        public static Connection connect() throws SQLException {
            try {
                // Ensure the driver is registered. Contemporary JDBC drivers often auto-register.
                 Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                System.err.println("MySQL JDBC Driver not found. Make sure the JDBC library is in the classpath.");
                throw new SQLException("JDBC Driver not found", e);
            }
             // Consider adding connection properties for security and performance if needed
             // Properties props = new Properties();
             // props.setProperty("user", USER);
             // props.setProperty("password", PASSWORD);
             // props.setProperty("serverTimezone", "UTC"); // Example property
             // return DriverManager.getConnection(URL, props);
            return DriverManager.getConnection(URL, USER, PASSWORD);
        }
    }

}