package OnlineShop;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.List; // Explicit import for clarity
import java.util.ArrayList; // Explicit import for clarity
import java.util.Map; // Explicit import
import java.util.HashMap; // Explicit import

public class AddressManager {
    private CustomerFrame customerFrame; // Should ideally be JFrame for broader use
    private int customerId;

    public AddressManager(CustomerFrame customerFrame, int customerId) {
        this.customerFrame = customerFrame;
        this.customerId = customerId;
    }

    // --- Address Data Class ---
    public static class Address {
        private int id;
        private int customerId;
        private String regionId;
        private String provinceId;
        private String municipalityId;
        private String barangayId;
        private String streetAddress;
        private boolean isDefault;
        private String regionName;
        private String provinceName;
        private String municipalityName;
        private String barangayName;
        private String recipientName; // Added field
        private String phone;         // Added field

        // Required for placeholder usage in CustomerFrame
        public Address(int id, String street, String city, String state, String postalCode, String country) {
            this.id = id;
            this.customerId = -1; // Indicate placeholder
            this.streetAddress = street;
            this.municipalityName = city; // Map simple structure to detailed
            this.provinceName = state;
            // postalCode and country are not directly used in the main Address structure but stored here for the placeholder
            this.regionId = null; this.provinceId = null; this.municipalityId = null; this.barangayId = null;
            this.isDefault = false; this.regionName = null; this.barangayName = null;
            this.recipientName = null; this.phone = null;
        }

        public Address(int id, int customerId, String regionId, String provinceId,
                       String municipalityId, String barangayId, String streetAddress, boolean isDefault,
                       String regionName, String provinceName, String municipalityName, String barangayName,
                       String recipientName, String phone) { // Added parameters
            this.id = id;
            this.customerId = customerId;
            this.regionId = regionId;
            this.provinceId = provinceId;
            this.municipalityId = municipalityId;
            this.barangayId = barangayId;
            this.streetAddress = streetAddress;
            this.isDefault = isDefault;
            this.regionName = regionName;
            this.provinceName = provinceName;
            this.municipalityName = municipalityName;
            this.barangayName = barangayName;
            this.recipientName = recipientName; // Added assignment
            this.phone = phone;                 // Added assignment
        }

        // Getters
        public int getId() { return id; }
        public int getCustomerId() { return customerId; } // Added getter
        public String getRegionId() { return regionId; }
        public String getProvinceId() { return provinceId; }
        public String getMunicipalityId() { return municipalityId; }
        public String getBarangayId() { return barangayId; }
        public String getStreetAddress() { return streetAddress; }
        public boolean isDefault() { return isDefault; }
        public String getRegionName() { return regionName; }
        public String getProvinceName() { return provinceName; }
        public String getMunicipalityName() { return municipalityName; }
        public String getBarangayName() { return barangayName; }
        public String getRecipientName() { return recipientName; } // Added getter
        public String getPhone() { return phone; }                 // Added getter

        // Placeholder getter for simple formatted address (used in CustomerFrame placeholder)
        public String getFormattedAddress() {
             if (this.customerId == -1) { // Check if it's a placeholder
                return String.format("%s, %s, %s", streetAddress, municipalityName, provinceName); // Adjust format as needed
             } else {
                 // Use the detailed address format
                 return getFullAddressForDisplay().replaceAll("<br>", ", ").replaceAll("\\<.*?\\>", ""); // Remove HTML tags for simple format
             }
        }

        // Updated getFullAddress to include name and phone for display card
        public String getFullAddressForDisplay() {
            String recipientPart = (recipientName != null && !recipientName.isEmpty() && !recipientName.startsWith("[")) ? "<b>Recipient:</b> " + recipientName + "<br>" : "";
            String phonePart = (phone != null && !phone.isEmpty() && !phone.startsWith("[")) ? "<b>Phone:</b> " + phone + "<br>" : "";
            String barangayPart = (barangayName != null && !barangayName.isEmpty() && !barangayName.startsWith("[")) ? barangayName + ", " : "";
            String streetPart = (streetAddress != null && !streetAddress.isEmpty() && !streetAddress.startsWith("[")) ? streetAddress + ", " : "";
            String municipalityPart = (municipalityName != null && !municipalityName.startsWith("[")) ? municipalityName + ", " : "";
            String provincePart = (provinceName != null && !provinceName.startsWith("[")) ? provinceName + ", " : "";
            String regionPart = (regionName != null && !regionName.startsWith("[")) ? regionName : "";

            // Combine address parts, cleaning up potential leading/trailing commas/spaces
            String addressDetails = String.format("%s%s%s%s%s",
                streetPart, barangayPart, municipalityPart, provincePart, regionPart).trim();
            if (addressDetails.endsWith(",")) {
                addressDetails = addressDetails.substring(0, addressDetails.length() - 1).trim();
            }
             // Handle empty address details gracefully
             if (addressDetails.isEmpty()) {
                addressDetails = "[Address details incomplete]";
             }
            // Combine all parts for the final display string
            return recipientPart + phonePart + addressDetails;
        }

         @Override
         public String toString() {
             // toString can remain simpler, e.g., for logging or internal use
             String basicAddress = String.format("%s, %s, %s, %s, %s",
                 streetAddress != null ? streetAddress : "",
                 barangayName != null ? barangayName : "",
                 municipalityName != null ? municipalityName : "",
                 provinceName != null ? provinceName : "",
                 regionName != null ? regionName : "");
             // Clean up multiple commas from potentially null parts
             basicAddress = basicAddress.replaceAll("(,\\s*)+", ", ").replaceAll("^,\\s*|\\s*,$", "");
             return basicAddress + (isDefault ? " (Default)" : "");
         }
    } // End Address Class

    // --- Public Methods ---
    public Address showAddressSelection() {
        Address selectedAddress = null;
        try {
            List<Address> addresses = getCustomerAddresses();
            AddressSelectionDialog dialog = new AddressSelectionDialog(customerFrame, addresses);
            dialog.setVisible(true); // This makes the dialog modal and blocks until closed
            selectedAddress = dialog.getSelectedAddress(); // Get the result after the dialog is closed
        } catch (HeadlessException he) {
             System.err.println("[AddressManager ERROR] Headless environment detected. Cannot show dialog.");
             // Handle headless environment appropriately, maybe return a default or throw exception
        } catch (Exception e) {
             System.err.println("[AddressManager ERROR] Error showing address dialog: " + e.getMessage());
             e.printStackTrace();
             // Use invokeLater to ensure message dialog is shown on EDT
             SwingUtilities.invokeLater(() -> showThemedJOptionPaneStatic(customerFrame,
                 "Could not display address selection: " + e.getMessage(),
                 "Dialog Error", JOptionPane.ERROR_MESSAGE));
        }
        return selectedAddress;
    }

    // --- Database Interaction Methods ---
    private List<Address> getCustomerAddresses() {
        List<Address> addresses = new ArrayList<>();
        String sql = "SELECT ca.id, ca.customer_id, ca.region_id, ca.province_id, " +
                     "ca.municipality_id, ca.barangay_id, ca.street_address, ca.is_default, " +
                     "ca.recipient_name, ca.phone, " + // Include recipient_name and phone
                     "tr.region_name, tp.province_name, tm.municipality_name, tb.barangay_name " +
                     "FROM customer_addresses ca " +
                     "LEFT JOIN table_region tr ON ca.region_id = tr.region_id " +
                     "LEFT JOIN table_province tp ON ca.province_id = tp.province_id " +
                     "LEFT JOIN table_municipality tm ON ca.municipality_id = tm.municipality_id " +
                     "LEFT JOIN table_barangay tb ON ca.barangay_id = tb.barangay_id " +
                     "WHERE ca.customer_id = ? ORDER BY ca.is_default DESC, ca.id";
        System.out.println("[AddressManager DEBUG] Fetching addresses for customer ID: " + customerId);

        Connection conn = null; // Declare connection outside try-with-resources for manual closing
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.connect(); // Use your DBConnection class
            if (conn == null) {
                throw new SQLException("Failed to establish database connection.");
            }
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, customerId);
            rs = stmt.executeQuery();
            int count = 0;
            while (rs.next()) {
                count++;
                 String regionName = rs.getString("region_name") != null ? rs.getString("region_name") : "[Unknown Region]";
                 String provinceName = rs.getString("province_name") != null ? rs.getString("province_name") : "[Unknown Province]";
                 String municipalityName = rs.getString("municipality_name") != null ? rs.getString("municipality_name") : "[Unknown Municipality]";
                 String barangayName = rs.getString("barangay_name") != null ? rs.getString("barangay_name") : "[Unknown Barangay]";
                 String streetAddress = rs.getString("street_address") != null ? rs.getString("street_address") : "[No Street Address]";
                 String recipientName = rs.getString("recipient_name") != null ? rs.getString("recipient_name") : "";
                 String phone = rs.getString("phone") != null ? rs.getString("phone") : "";

                addresses.add(new Address(
                    rs.getInt("id"), rs.getInt("customer_id"),
                    rs.getString("region_id"), rs.getString("province_id"), rs.getString("municipality_id"), rs.getString("barangay_id"),
                    streetAddress, rs.getBoolean("is_default"),
                    regionName, provinceName, municipalityName, barangayName,
                    recipientName, phone
                ));
            }
            System.out.println("[AddressManager DEBUG] Finished fetching. Total addresses found: " + count);
        } catch (SQLException ex) {
            handleSqlError("Error loading addresses", ex);
        } catch(Exception e) {
            System.err.println("[AddressManager ERROR] Unexpected error during address loading: " + e.getMessage());
            e.printStackTrace();
        } finally {
             // Close resources manually in the finally block
             try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
             try { if (stmt != null) stmt.close(); } catch (SQLException ignored) {}
             // Use the closeDbConnection helper method
             closeDbConnection(conn);
        }
        System.out.println("[AddressManager DEBUG] getCustomerAddresses returning list size: " + addresses.size());
        return addresses;
    }

    private void saveAddressToDatabase(Address address) {
        Connection conn = null;
        PreparedStatement unsetStmt = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.connect();
             if (conn == null) throw new SQLException("DB Connection failed");
            conn.setAutoCommit(false);
            if (address.isDefault()) {
                unsetStmt = conn.prepareStatement(
                    "UPDATE customer_addresses SET is_default = FALSE WHERE customer_id = ? AND is_default = TRUE");
                unsetStmt.setInt(1, customerId);
                unsetStmt.executeUpdate();
            }
            String sql = "INSERT INTO customer_addresses " +
                         "(customer_id, region_id, province_id, municipality_id, barangay_id, street_address, is_default, recipient_name, phone) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, customerId);
            stmt.setString(2, address.getRegionId());
            stmt.setString(3, address.getProvinceId());
            stmt.setString(4, address.getMunicipalityId());
            stmt.setString(5, address.getBarangayId());
            stmt.setString(6, address.getStreetAddress());
            stmt.setBoolean(7, address.isDefault());
            stmt.setString(8, address.getRecipientName());
            stmt.setString(9, address.getPhone());
            stmt.executeUpdate();
            conn.commit();

        } catch (SQLException ex) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException e) { e.printStackTrace(); }
            }
            handleSqlError("Error saving address", ex);
        } finally {
             try { if (unsetStmt != null) unsetStmt.close(); } catch (SQLException ignored) {}
             try { if (stmt != null) stmt.close(); } catch (SQLException ignored) {}
            closeDbConnection(conn); // Handles setAutoCommit(true) and close()
        }
    }

    private boolean updateAddressInDatabase(Address address) {
        Connection conn = null;
        PreparedStatement unsetStmt = null;
        PreparedStatement stmt = null;
        boolean success = false;
        try {
            conn = DBConnection.connect();
             if (conn == null) throw new SQLException("DB Connection failed");
            conn.setAutoCommit(false);
            if (address.isDefault()) {
                unsetStmt = conn.prepareStatement(
                    "UPDATE customer_addresses SET is_default = FALSE WHERE customer_id = ? AND id != ? AND is_default = TRUE");
                unsetStmt.setInt(1, customerId);
                unsetStmt.setInt(2, address.getId());
                unsetStmt.executeUpdate();
            }
            String sql = "UPDATE customer_addresses SET " +
                         "region_id = ?, province_id = ?, municipality_id = ?, barangay_id = ?, " +
                         "street_address = ?, is_default = ?, recipient_name = ?, phone = ? " +
                         "WHERE id = ? AND customer_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, address.getRegionId());
            stmt.setString(2, address.getProvinceId());
            stmt.setString(3, address.getMunicipalityId());
            stmt.setString(4, address.getBarangayId());
            stmt.setString(5, address.getStreetAddress());
            stmt.setBoolean(6, address.isDefault());
            stmt.setString(7, address.getRecipientName());
            stmt.setString(8, address.getPhone());
            stmt.setInt(9, address.getId());
            stmt.setInt(10, customerId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                conn.commit(); success = true;
                System.out.println("[AddressManager DEBUG] Successfully updated address ID: " + address.getId());
            } else {
                conn.rollback();
                 System.err.println("[AddressManager WARNING] Update failed or address not found for ID: " + address.getId());
            }
        } catch (SQLException ex) {
             if (conn != null) {
                 try { conn.rollback(); } catch (SQLException e) { e.printStackTrace(); }
             }
            handleSqlError("Error updating address ID " + address.getId(), ex);
        } finally {
             try { if (unsetStmt != null) unsetStmt.close(); } catch (SQLException ignored) {}
             try { if (stmt != null) stmt.close(); } catch (SQLException ignored) {}
            closeDbConnection(conn); // Handles setAutoCommit(true) and close()
        }
        return success;
    }

    private boolean deleteAddressFromDatabase(int addressId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean success = false;
        try {
            conn = DBConnection.connect();
             if (conn == null) throw new SQLException("DB Connection failed");
            String sql = "DELETE FROM customer_addresses WHERE id = ? AND customer_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, addressId);
            stmt.setInt(2, customerId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                success = true;
                System.out.println("[AddressManager DEBUG] Successfully deleted address ID: " + addressId);
            } else {
                System.err.println("[AddressManager WARNING] Delete failed or address not found for ID: " + addressId + " and customer ID: " + customerId);
            }
        } catch (SQLException ex) {
            handleSqlError("Error deleting address ID " + addressId, ex);
        } finally {
             try { if (stmt != null) stmt.close(); } catch (SQLException ignored) {}
            closeDbConnection(conn); // Use helper to close connection
        }
        return success;
    }

    // --- Geographic Data Loading Methods ---
     public List<Region> getAllRegions() {
         List<Region> regions = new ArrayList<>();
         String sql = "SELECT region_id, region_name FROM table_region WHERE region_id != 'region_id' ORDER BY region_name";
         try (Connection conn = DBConnection.connect();
              Statement stmt = conn.createStatement();
              ResultSet rs = stmt.executeQuery(sql)) {
             while (rs.next()) { regions.add(new Region(rs.getString("region_id"), rs.getString("region_name"))); }
         } catch (SQLException ex) { System.err.println("[AddressManager ERROR] loading regions: " + ex.getMessage()); ex.printStackTrace(); }
         return regions;
     }
     public List<Province> getProvincesByRegion(String regionId) {
         List<Province> provinces = new ArrayList<>();
          String sql = "SELECT province_id, province_name FROM table_province WHERE region_id = ? AND province_id != 'province_id' ORDER BY province_name";
         try (Connection conn = DBConnection.connect();
              PreparedStatement stmt = conn.prepareStatement(sql)) {
             stmt.setString(1, regionId);
             ResultSet rs = stmt.executeQuery();
             while (rs.next()) { provinces.add(new Province(rs.getString("province_id"), rs.getString("province_name"))); }
         } catch (SQLException ex) { System.err.println("[AddressManager ERROR] loading provinces for region " + regionId + ": " + ex.getMessage()); ex.printStackTrace(); }
         return provinces;
     }
     public List<Municipality> getCitiesByProvince(String provinceId) {
         List<Municipality> cities = new ArrayList<>();
         String sql = "SELECT municipality_id, municipality_name FROM table_municipality WHERE province_id = ? AND municipality_id != 'municipality_id' ORDER BY municipality_name";
         try (Connection conn = DBConnection.connect();
              PreparedStatement stmt = conn.prepareStatement(sql)) {
             stmt.setString(1, provinceId);
             ResultSet rs = stmt.executeQuery();
             while (rs.next()) { cities.add(new Municipality(rs.getString("municipality_id"), rs.getString("municipality_name"))); }
         } catch (SQLException ex) { System.err.println("[AddressManager ERROR] loading cities for province " + provinceId + ": " + ex.getMessage()); ex.printStackTrace(); }
         return cities;
     }
     public List<Barangay> getBarangaysByCity(String cityId) {
         List<Barangay> barangays = new ArrayList<>();
         String sql = "SELECT barangay_id, barangay_name FROM table_barangay WHERE municipality_id = ? AND barangay_id != 'barangay_id' ORDER BY barangay_name";
         try (Connection conn = DBConnection.connect();
              PreparedStatement stmt = conn.prepareStatement(sql)) {
             stmt.setString(1, cityId);
             ResultSet rs = stmt.executeQuery();
             while (rs.next()) { barangays.add(new Barangay(rs.getString("barangay_id"), rs.getString("barangay_name"))); }
         } catch (SQLException ex) { System.err.println("[AddressManager ERROR] loading barangays for city " + cityId + ": " + ex.getMessage()); ex.printStackTrace(); }
         return barangays;
     }

    // --- Helper classes for geographic entities ---
    public static class Region { String id, name; public Region(String i, String n){id=i;name=n;} public String getId(){return id;} public String getName(){return name;} @Override public String toString(){return name;} }
    public static class Province { String id, name; public Province(String i, String n){id=i;name=n;} public String getId(){return id;} public String getName(){return name;} @Override public String toString(){return name;} }
    public static class Municipality { String id, name; public Municipality(String i, String n){id=i;name=n;} public String getId(){return id;} public String getName(){return name;} @Override public String toString(){return name;} }
    public static class Barangay { String id, name; public Barangay(String i, String n){id=i;name=n;} public String getId(){return id;} public String getName(){return name;} @Override public String toString(){return name;} }


    // --- Utility Methods ---
    private void handleSqlError(String context, SQLException ex) {
        System.err.println("[AddressManager ERROR] " + context + ": " + ex.getMessage() + " (SQLState: " + ex.getSQLState() + ", ErrorCode: " + ex.getErrorCode() + ")");
        ex.printStackTrace();
        // Ensure dialog shows on EDT
        SwingUtilities.invokeLater(() -> showThemedJOptionPaneStatic(customerFrame,
            context + ". Please check logs or contact support.\nError: " + ex.getMessage(),
            "Database Error", JOptionPane.ERROR_MESSAGE));
    }

    private void closeDbConnection(Connection conn) {
        if (conn != null) {
            try {
                // Reset auto-commit only if it was changed (i.e., false)
                if (!conn.getAutoCommit()) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                System.err.println("[AddressManager WARNING] Error resetting auto-commit on close: " + ex.getMessage());
            } finally {
                 try {
                     conn.close(); // Always attempt to close
                 } catch (SQLException ex) {
                      System.err.println("[AddressManager ERROR] Failed to close DB connection: " + ex.getMessage());
                     ex.printStackTrace();
                 }
            }
        }
    }

    // Static version for use outside the inner class instance if needed
     private static void showThemedJOptionPaneStatic(Component parent, String message, String title, int messageType) {
         // UIManager settings applied in main should handle this,
         // but setting them here again ensures the dialog is themed correctly,
         // especially if UIManager defaults were changed elsewhere.
         UIManager.put("OptionPane.background", ThemeColors.DIALOG_BG);
         UIManager.put("Panel.background", ThemeColors.DIALOG_BG);
         UIManager.put("OptionPane.messageForeground", ThemeColors.DIALOG_FG);
         UIManager.put("Button.background", ThemeColors.SECONDARY);
         UIManager.put("Button.foreground", Color.WHITE);
         JOptionPane.showMessageDialog(parent, message, title, messageType);
     }


    // --- Inner Dialog Class with Theming ---
    private class AddressSelectionDialog extends JDialog {
        // --- Form Components ---
        // (Separate instances are created for Add vs Edit panels)

        // --- Selection Panel Components ---
        private JPanel addressesPanel;
        private JButton selectButton;
        private ButtonGroup addressGroup;
        private Map<Integer, JRadioButton> addressRadioMap = new HashMap<>();

        // --- Data ---
        private List<Address> customerAddresses;
        private Address selectedAddress = null; // Initialize to null


        public AddressSelectionDialog(JFrame parent, List<Address> addresses) {
            super(parent, "Select or Add Delivery Address", true);
            this.customerAddresses = (addresses != null) ? new ArrayList<>(addresses) : new ArrayList<>();
            this.addressGroup = new ButtonGroup();
            setLayout(new BorderLayout());
            setSize(700, 650);
            setLocationRelativeTo(parent);
            getContentPane().setBackground(ThemeColors.BACKGROUND);

            JTabbedPane tabbedPane = new JTabbedPane();
            styleTabbedPane(tabbedPane);

            JPanel selectionPanel = createSelectionPanel();
            JPanel addPanel = createAddPanel();

            tabbedPane.addTab("Select Address", selectionPanel);
            tabbedPane.addTab("Add New Address", addPanel);
            add(tabbedPane, BorderLayout.CENTER);

            // Ensure UI updates happen on the Event Dispatch Thread
            SwingUtilities.invokeLater(this::postInitSetup);
        }

        private void postInitSetup() {
            buildAddressListUI(); // Build the list content
            findAndSetInitialSelection(); // Set the default selection
             // Trigger layout update for the scroll pane containing the addresses
            Container scrollPane = SwingUtilities.getAncestorOfClass(JScrollPane.class, addressesPanel);
            if (scrollPane instanceof JScrollPane) {
                ((JScrollPane) scrollPane).getViewport().setViewPosition(new Point(0,0)); // Scroll to top
                scrollPane.revalidate();
                scrollPane.repaint();
            } else {
                // Fallback if scrollpane isn't found directly
                addressesPanel.revalidate();
                addressesPanel.repaint();
            }
        }

        private JPanel createSelectionPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(ThemeColors.BACKGROUND);
            panel.setBorder(new EmptyBorder(10, 10, 10, 10));

            addressesPanel = new JPanel();
            addressesPanel.setLayout(new BoxLayout(addressesPanel, BoxLayout.Y_AXIS));
            addressesPanel.setBackground(ThemeColors.BACKGROUND);

            // Wrap addressesPanel in another panel ONLY if necessary for layout purposes.
            // Using BorderLayout.NORTH helps keep items packed at the top.
            JPanel addressListWrapper = new JPanel(new BorderLayout());
            addressListWrapper.setBackground(ThemeColors.BACKGROUND);
            addressListWrapper.add(addressesPanel, BorderLayout.NORTH);

            JScrollPane scrollPane = new JScrollPane(addressListWrapper); // Scroll the wrapper
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setBorder(new LineBorder(ThemeColors.SECONDARY));
            scrollPane.getViewport().setBackground(ThemeColors.BACKGROUND);
            styleScrollPane(scrollPane);
            panel.add(scrollPane, BorderLayout.CENTER);

            selectButton = createStyledButton("Use Selected Address", ThemeColors.PRIMARY);
            selectButton.addActionListener(e -> {
                // The selectedAddress is updated by the radio button listeners
                if (selectedAddress != null) {
                    dispose(); // Close the dialog
                } else {
                    showThemedJOptionPane("Please select an address.", "No Address Selected", JOptionPane.WARNING_MESSAGE);
                }
            });
             // Disable initially if no address is selected
             selectButton.setEnabled(selectedAddress != null);

            JPanel buttonPanel = createButtonPanel();
            buttonPanel.add(selectButton);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            return panel;
        }

        private void buildAddressListUI() {
            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeLater(this::buildAddressListUI);
                return;
            }
            System.out.println("[AddressManager DEBUG] buildAddressListUI started. Address count: " + customerAddresses.size());

            addressesPanel.removeAll(); // Clear previous items
            addressGroup = new ButtonGroup(); // New group for new radio buttons
            addressRadioMap.clear(); // Clear the map

            if (customerAddresses.isEmpty()) {
                JLabel noAddressLabel = new JLabel("No saved addresses. Please add one in the 'Add New Address' tab.");
                noAddressLabel.setForeground(ThemeColors.TEXT);
                noAddressLabel.setHorizontalAlignment(SwingConstants.CENTER);
                // Center the label if it's the only component
                addressesPanel.setLayout(new BorderLayout());
                addressesPanel.add(noAddressLabel, BorderLayout.CENTER);
            } else {
                 // Reset layout manager if it was changed for the empty message
                 if (!(addressesPanel.getLayout() instanceof BoxLayout)) {
                     addressesPanel.setLayout(new BoxLayout(addressesPanel, BoxLayout.Y_AXIS));
                 }
                for (Address address : customerAddresses) {
                    try {
                        JPanel addressCard = createAddressCard(address);
                        JRadioButton radio = findRadioButton(addressCard);
                        if (radio != null) {
                            addressRadioMap.put(address.getId(), radio);
                            addressGroup.add(radio);
                            final Address currentAddress = address; // Capture for listener
                             // Update selectedAddress and enable button when a radio is clicked
                             radio.addActionListener(e -> {
                                selectedAddress = currentAddress;
                                selectButton.setEnabled(true); // Enable the 'Use' button
                             });
                        } else {
                            System.err.println("[AddressManager WARNING] Radio button not found for ID: " + address.getId());
                        }
                        addressesPanel.add(addressCard);
                        addressesPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Spacer
                    } catch (Exception e) {
                         System.err.printf("[AddressManager ERROR] creating address card for ID %d: %s%n", address.getId(), e.getMessage());
                         e.printStackTrace();
                    }
                }
            }
            System.out.println("[AddressManager DEBUG] buildAddressListUI finished. Map size: " + addressRadioMap.size());

            // Revalidate and repaint after modifying components
            addressesPanel.revalidate();
            addressesPanel.repaint();
            Container parent = addressesPanel.getParent();
            if (parent instanceof JComponent) {
                ((JComponent) parent).revalidate();
                ((JComponent) parent).repaint();
            }
             Container scrollParent = SwingUtilities.getAncestorOfClass(JScrollPane.class, addressesPanel);
             if (scrollParent instanceof JScrollPane) {
                  ((JScrollPane)scrollParent).revalidate();
                  ((JScrollPane)scrollParent).repaint();
             }
        }

        private JRadioButton findRadioButton(Container container) {
             for (Component comp : container.getComponents()) {
                if (comp instanceof JRadioButton) return (JRadioButton) comp;
                if (comp instanceof Container) {
                    JRadioButton found = findRadioButton((Container) comp);
                    if (found != null) return found;
                }
            } return null;
        }

        private JPanel createAddressCard(Address address) {
            JPanel cardPanel = new JPanel(new BorderLayout(10, 10));
            cardPanel.setBackground(ThemeColors.CARD_BG);
            Border line = new LineBorder(ThemeColors.SECONDARY);
            Border padding = new EmptyBorder(10, 10, 10, 10);
            cardPanel.setBorder(new CompoundBorder(line, padding));
            cardPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 135)); // Constraint height

            JRadioButton radio = new JRadioButton();
            styleRadioButton(radio);
            JPanel radioWrapper = new JPanel(new GridBagLayout()); // To center radio vertically
            radioWrapper.setOpaque(false);
            radioWrapper.add(radio);
            cardPanel.add(radioWrapper, BorderLayout.WEST);

            // Using HTML for basic formatting and line breaks
            JLabel addressLabel = new JLabel("<html><body style='width: 380px;'>" + // Constrain width for wrapping
                (address.isDefault() ? "<b style='color:" + colorToHex(ThemeColors.PRIMARY) + ";'>Default Address</b><br>" : "") +
                address.getFullAddressForDisplay() + "</body></html>");
            addressLabel.setForeground(ThemeColors.TEXT);
            addressLabel.setVerticalAlignment(SwingConstants.TOP); // Align text to top
            cardPanel.add(addressLabel, BorderLayout.CENTER);

            JPanel buttonsPanel = new JPanel();
            buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
            buttonsPanel.setOpaque(false);

            JButton editButton = createStyledButton("Edit", ThemeColors.SECONDARY);
            JButton deleteButton = createStyledButton("Delete", new Color(180, 40, 40)); // Distinct delete color

            // Button Size Consistency Fix
            Border smallButtonBorder = new EmptyBorder(5, 15, 5, 15);
            editButton.setBorder(smallButtonBorder);
            deleteButton.setBorder(smallButtonBorder);
            Dimension editPref = editButton.getPreferredSize();
            Dimension deletePref = deleteButton.getPreferredSize();
            int maxWidth = Math.max(editPref.width, deletePref.width);
            Dimension uniformSize = new Dimension(maxWidth, editPref.height);
            editButton.setPreferredSize(uniformSize);
            editButton.setMaximumSize(uniformSize);
            deleteButton.setPreferredSize(uniformSize);
            deleteButton.setMaximumSize(uniformSize);
            editButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            deleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);

            final Address addressRef = address; // Final reference for listeners
            editButton.addActionListener(e -> showEditAddressDialog(addressRef));
            deleteButton.addActionListener(e -> handleDeleteAddress(addressRef));

            buttonsPanel.add(Box.createVerticalGlue()); // Push buttons to center
            buttonsPanel.add(editButton);
            buttonsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            buttonsPanel.add(deleteButton);
            buttonsPanel.add(Box.createVerticalGlue()); // Push buttons to center

            cardPanel.add(buttonsPanel, BorderLayout.EAST);

            return cardPanel;
        }


         private static class RadioButtonIcon implements Icon {
             private final boolean selected; private static final int DIAMETER = 16;
             public RadioButtonIcon(boolean selected) { this.selected = selected; }
             @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                 Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                 g2.setColor(ThemeColors.SECONDARY); g2.drawOval(x, y, DIAMETER, DIAMETER);
                 if (selected) { g2.setColor(ThemeColors.PRIMARY); int innerDiameter = DIAMETER / 2; g2.fillOval(x + (DIAMETER - innerDiameter) / 2, y + (DIAMETER - innerDiameter) / 2, innerDiameter, innerDiameter); }
                 g2.dispose();
             }
             @Override public int getIconWidth() { return DIAMETER + 4; }
             @Override public int getIconHeight() { return DIAMETER + 4; }
         }

        // --- Create Add Panel ---
        private JPanel createAddPanel() {
            JPanel panel = new JPanel(new BorderLayout()); panel.setBackground(ThemeColors.BACKGROUND);
            JPanel formPanel = new JPanel(new GridBagLayout()); formPanel.setBackground(ThemeColors.BACKGROUND); formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(8, 8, 8, 8); gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL; int gridY = 0;

            JTextField addRecipientNameField = styleTextField(new JTextField());
            JTextField addPhoneField = styleTextField(new JTextField());
            JComboBox<Region> addRegionCombo = styleComboBox(new JComboBox<>());
            JComboBox<Province> addProvinceCombo = styleComboBox(new JComboBox<>());
            JComboBox<Municipality> addCityCombo = styleComboBox(new JComboBox<>());
            JComboBox<Barangay> addBarangayCombo = styleComboBox(new JComboBox<>());
            JTextField addStreetAddressField = styleTextField(new JTextField());
            JRadioButton addDefaultAddressRadio = new JRadioButton(); styleRadioButton(addDefaultAddressRadio);

            // Layout components...
            gbc.gridx = 0; gbc.gridy = gridY; formPanel.add(createFormLabel("Recipient Name:"), gbc); gbc.gridx = 1; gbc.weightx = 1.0; formPanel.add(addRecipientNameField, gbc); gbc.weightx = 0.0; gridY++;
            gbc.gridx = 0; gbc.gridy = gridY; formPanel.add(createFormLabel("Phone Number:"), gbc); gbc.gridx = 1; gbc.weightx = 1.0; formPanel.add(addPhoneField, gbc); gbc.weightx = 0.0; gridY++;
            gbc.gridx = 0; gbc.gridy = gridY; formPanel.add(createFormLabel("Region:"), gbc); gbc.gridx = 1; gbc.weightx = 1.0; addRegionCombo.addItem(new Region("-1", "-- Select Region --")); getAllRegions().forEach(addRegionCombo::addItem); formPanel.add(addRegionCombo, gbc); gbc.weightx = 0.0; gridY++;
            gbc.gridx = 0; gbc.gridy = gridY; formPanel.add(createFormLabel("Province:"), gbc); gbc.gridx = 1; gbc.weightx = 1.0; addProvinceCombo.addItem(new Province("-1", "-- Select Province --")); addProvinceCombo.setEnabled(false); formPanel.add(addProvinceCombo, gbc); gbc.weightx = 0.0; gridY++;
            gbc.gridx = 0; gbc.gridy = gridY; formPanel.add(createFormLabel("City/Municipality:"), gbc); gbc.gridx = 1; gbc.weightx = 1.0; addCityCombo.addItem(new Municipality("-1", "-- Select City/Municipality --")); addCityCombo.setEnabled(false); formPanel.add(addCityCombo, gbc); gbc.weightx = 0.0; gridY++;
            gbc.gridx = 0; gbc.gridy = gridY; formPanel.add(createFormLabel("Barangay:"), gbc); gbc.gridx = 1; gbc.weightx = 1.0; addBarangayCombo.addItem(new Barangay("-1", "-- Select Barangay --")); addBarangayCombo.setEnabled(false); formPanel.add(addBarangayCombo, gbc); gbc.weightx = 0.0; gridY++;
            gbc.gridx = 0; gbc.gridy = gridY; formPanel.add(createFormLabel("Street Address:"), gbc); gbc.gridx = 1; gbc.weightx = 1.0; formPanel.add(addStreetAddressField, gbc); gbc.weightx = 0.0; gridY++;
            gbc.gridx = 0; gbc.gridy = gridY; formPanel.add(createFormLabel("Set as default:"), gbc); gbc.gridx = 1; gbc.fill = GridBagConstraints.NONE; formPanel.add(addDefaultAddressRadio, gbc); gbc.fill = GridBagConstraints.HORIZONTAL;

            addAddDropdownListeners(addRegionCombo, addProvinceCombo, addCityCombo, addBarangayCombo);

            JScrollPane scrollPane = new JScrollPane(formPanel); scrollPane.setBorder(new LineBorder(ThemeColors.SECONDARY)); scrollPane.getViewport().setBackground(ThemeColors.BACKGROUND); styleScrollPane(scrollPane);
            panel.add(scrollPane, BorderLayout.CENTER);

            JButton saveBtn = createStyledButton("Save Address", ThemeColors.PRIMARY);
            saveBtn.addActionListener(e -> saveNewAddress(addRecipientNameField, addPhoneField, addRegionCombo, addProvinceCombo, addCityCombo, addBarangayCombo, addStreetAddressField, addDefaultAddressRadio));
            JPanel savePanel = createButtonPanel(); savePanel.add(saveBtn);
            panel.add(savePanel, BorderLayout.SOUTH);

            return panel;
        }

        // --- Listeners for Add Panel Dropdowns ---
        private void addAddDropdownListeners(JComboBox<Region> r, JComboBox<Province> p, JComboBox<Municipality> m, JComboBox<Barangay> b) { r.addActionListener(e -> updateAddProvinces(r, p, m, b)); p.addActionListener(e -> updateAddCities(p, m, b)); m.addActionListener(e -> updateAddBarangays(m, b)); }
        private void updateAddProvinces(JComboBox<Region> r, JComboBox<Province> p, JComboBox<Municipality> m, JComboBox<Barangay> b) { Region s = (Region) r.getSelectedItem(); p.removeAllItems(); p.addItem(new Province("-1", "-- Select Province --")); m.removeAllItems(); m.addItem(new Municipality("-1", "-- Select City/Municipality --")); b.removeAllItems(); b.addItem(new Barangay("-1", "-- Select Barangay --")); if (s != null && !s.getId().equals("-1")) { getProvincesByRegion(s.getId()).forEach(p::addItem); p.setEnabled(true); m.setEnabled(false); b.setEnabled(false); } else { p.setEnabled(false); m.setEnabled(false); b.setEnabled(false); } }
        private void updateAddCities(JComboBox<Province> p, JComboBox<Municipality> m, JComboBox<Barangay> b) { Province s = (Province) p.getSelectedItem(); m.removeAllItems(); m.addItem(new Municipality("-1", "-- Select City/Municipality --")); b.removeAllItems(); b.addItem(new Barangay("-1", "-- Select Barangay --")); if (s != null && !s.getId().equals("-1")) { getCitiesByProvince(s.getId()).forEach(m::addItem); m.setEnabled(true); b.setEnabled(false); } else { m.setEnabled(false); b.setEnabled(false); } }
        private void updateAddBarangays(JComboBox<Municipality> m, JComboBox<Barangay> b) { Municipality s = (Municipality) m.getSelectedItem(); b.removeAllItems(); b.addItem(new Barangay("-1", "-- Select Barangay --")); if (s != null && !s.getId().equals("-1")) { getBarangaysByCity(s.getId()).forEach(b::addItem); b.setEnabled(true); } else { b.setEnabled(false); } }


        // --- Save New Address ---
        private void saveNewAddress(JTextField nameFld, JTextField phoneFld, JComboBox<Region> rCombo, JComboBox<Province> pCombo,
                                   JComboBox<Municipality> mCombo, JComboBox<Barangay> bCombo, JTextField streetFld, JRadioButton defaultRadioBtn) {
            String recipientName = nameFld.getText().trim(); String phone = phoneFld.getText().trim();
            Region region = (Region) rCombo.getSelectedItem(); Province province = (Province) pCombo.getSelectedItem(); Municipality city = (Municipality) mCombo.getSelectedItem(); Barangay barangay = (Barangay) bCombo.getSelectedItem();
            String street = streetFld.getText().trim();

            if (recipientName.isEmpty() || phone.isEmpty() || region == null || region.getId().equals("-1") || province == null || province.getId().equals("-1") || city == null || city.getId().equals("-1") || barangay == null || barangay.getId().equals("-1") || street.isEmpty()) {
                showThemedJOptionPane("Please fill in all address fields, including Recipient Name and Phone.", "Incomplete Address", JOptionPane.WARNING_MESSAGE); return;
            }
            if (!phone.matches("^[\\d\\s+-]+$")) { showThemedJOptionPane("Please enter a valid phone number.", "Invalid Phone", JOptionPane.WARNING_MESSAGE); return; }

            Address newAddress = new Address(0, customerId, region.getId(), province.getId(), city.getId(), barangay.getId(), street, defaultRadioBtn.isSelected(), region.getName(), province.getName(), city.getName(), barangay.getName(), recipientName, phone);
            saveAddressToDatabase(newAddress);

            SwingUtilities.invokeLater(() -> {
                showThemedJOptionPane("Address saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshAddressSelectionPanel();
                resetAddAddressForm(nameFld, phoneFld, rCombo, streetFld, defaultRadioBtn);
                // Switch back to the selection tab after adding
                Container parent = getParent();
                 while(parent != null && !(parent instanceof JDialog)) {
                    parent = parent.getParent();
                 }
                 if (parent instanceof JDialog) {
                     Component content = ((JDialog)parent).getContentPane().getComponent(0);
                     if (content instanceof JTabbedPane) {
                        ((JTabbedPane)content).setSelectedIndex(0);
                     }
                 }
            });
        }

        private void resetAddAddressForm(JTextField nameFld, JTextField phoneFld, JComboBox<Region> rCombo, JTextField streetFld, JRadioButton defaultRadioBtn) {
            SwingUtilities.invokeLater(() -> { nameFld.setText(""); phoneFld.setText(""); rCombo.setSelectedIndex(0); /* Dropdowns linked to region will reset automatically */ streetFld.setText(""); defaultRadioBtn.setSelected(false); });
        }

        // --- Handle Delete Address Action ---
        private void handleDeleteAddress(Address addressToDelete) {
             // Set UIManager properties specifically for this JOptionPane instance
             UIManager.put("OptionPane.background", ThemeColors.DIALOG_BG);
             UIManager.put("Panel.background", ThemeColors.DIALOG_BG);
             UIManager.put("OptionPane.messageForeground", ThemeColors.DIALOG_FG);
             UIManager.put("Button.background", ThemeColors.SECONDARY);
             UIManager.put("Button.foreground", Color.WHITE);

            int confirmation = JOptionPane.showConfirmDialog( this,
                "Are you sure you want to delete this address?\n" + addressToDelete.getFullAddressForDisplay().replaceAll("<br>", "\n").replaceAll("<.?b>", ""), // Clean HTML for display
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            // Reset UIManager properties if necessary (usually not required if set globally)
            // UIManager.put("OptionPane.background", originalBg); // Example

            if (confirmation == JOptionPane.YES_OPTION) {
                boolean deleteSuccess = deleteAddressFromDatabase(addressToDelete.getId());
                if (deleteSuccess) {
                    showThemedJOptionPane("Address deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    if (selectedAddress != null && selectedAddress.getId() == addressToDelete.getId()) { selectedAddress = null; }
                    refreshAddressSelectionPanel(); // Refresh list UI
                } // Error message handled in delete method
            }
        }

        // --- refreshAddressSelectionPanel ---
        private void refreshAddressSelectionPanel() {
             customerAddresses = getCustomerAddresses(); // Reload data
             buildAddressListUI(); // Rebuild the UI components
             findAndSetInitialSelection(); // Reset the selection (usually to default or none)

             // Revalidate the container holding the address cards
             addressesPanel.revalidate();
             addressesPanel.repaint();
             // Revalidate the scroll pane ancestor
             Container scrollPane = SwingUtilities.getAncestorOfClass(JScrollPane.class, addressesPanel);
             if (scrollPane != null) {
                 scrollPane.revalidate();
                 scrollPane.repaint();
                 // Scroll to top after refresh
                 ((JScrollPane) scrollPane).getViewport().setViewPosition(new Point(0,0));
             }
         }


        // --- findAndSetInitialSelection ---
        private void findAndSetInitialSelection() {
            if (!SwingUtilities.isEventDispatchThread()) { SwingUtilities.invokeLater(this::findAndSetInitialSelection); return; }
            System.out.println("[AddressManager DEBUG] findAndSetInitialSelection started.");

            selectedAddress = null; // Reset selected address
            Address defaultAddr = null;
            for (Address addr : customerAddresses) { if (addr.isDefault()) { defaultAddr = addr; break; } }

            if (defaultAddr != null) {
                 selectedAddress = defaultAddr; // Set selected to default
                 JRadioButton defaultRadio = addressRadioMap.get(defaultAddr.getId());
                 if (defaultRadio != null) {
                    defaultRadio.setSelected(true); // Select the radio button
                    System.out.println("[AddressManager DEBUG] Default radio for ID " + defaultAddr.getId() + " selected.");
                    selectButton.setEnabled(true); // Enable the 'Use' button
                 } else {
                    System.err.println("[AddressManager WARNING] Could not find radio button in map for default ID: " + defaultAddr.getId());
                     if(addressGroup != null) addressGroup.clearSelection(); // Clear if radio not found
                    selectButton.setEnabled(false); // Disable if default can't be selected
                 }
            } else {
                 if(addressGroup != null) addressGroup.clearSelection(); // No default, clear selection
                 System.out.println("[AddressManager DEBUG] No default address found, cleared selection.");
                 selectButton.setEnabled(false); // Disable button if no default selected
            }
            System.out.println("[AddressManager DEBUG] findAndSetInitialSelection finished. Selected ID: " + (selectedAddress != null ? selectedAddress.getId() : "null"));
        }


        // --- Show Edit Address Dialog ---
        private void showEditAddressDialog(Address addressToEdit) {
            JDialog editDialog = new JDialog(this, "Edit Address", true); editDialog.setSize(700, 550); editDialog.setLayout(new BorderLayout()); editDialog.getContentPane().setBackground(ThemeColors.BACKGROUND); editDialog.setLocationRelativeTo(this);

            JPanel editFormPanel = new JPanel(new GridBagLayout()); editFormPanel.setBackground(ThemeColors.BACKGROUND); editFormPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            GridBagConstraints gbcEdit = new GridBagConstraints(); gbcEdit.insets = new Insets(8, 8, 8, 8); gbcEdit.anchor = GridBagConstraints.WEST; gbcEdit.fill = GridBagConstraints.HORIZONTAL; int gridYEdit = 0;

            JTextField editRecipientNameField = styleTextField(new JTextField()); JTextField editPhoneField = styleTextField(new JTextField()); JComboBox<Region> editRegionCombo = styleComboBox(new JComboBox<>()); JComboBox<Province> editProvinceCombo = styleComboBox(new JComboBox<>()); JComboBox<Municipality> editCityCombo = styleComboBox(new JComboBox<>()); JComboBox<Barangay> editBarangayCombo = styleComboBox(new JComboBox<>()); JTextField editStreetAddressField = styleTextField(new JTextField()); JRadioButton editDefaultAddressRadio = new JRadioButton(); styleRadioButton(editDefaultAddressRadio);

            // Layout components... (Same structure as Add Panel)
            gbcEdit.gridx = 0; gbcEdit.gridy = gridYEdit; editFormPanel.add(createFormLabel("Recipient Name:"), gbcEdit); gbcEdit.gridx = 1; gbcEdit.weightx = 1.0; editFormPanel.add(editRecipientNameField, gbcEdit); gbcEdit.weightx = 0.0; gridYEdit++;
            gbcEdit.gridx = 0; gbcEdit.gridy = gridYEdit; editFormPanel.add(createFormLabel("Phone Number:"), gbcEdit); gbcEdit.gridx = 1; gbcEdit.weightx = 1.0; editFormPanel.add(editPhoneField, gbcEdit); gbcEdit.weightx = 0.0; gridYEdit++;
            gbcEdit.gridx = 0; gbcEdit.gridy = gridYEdit; editFormPanel.add(createFormLabel("Region:"), gbcEdit); gbcEdit.gridx = 1; gbcEdit.weightx = 1.0; editFormPanel.add(editRegionCombo, gbcEdit); gbcEdit.weightx = 0.0; gridYEdit++;
            gbcEdit.gridx = 0; gbcEdit.gridy = gridYEdit; editFormPanel.add(createFormLabel("Province:"), gbcEdit); gbcEdit.gridx = 1; gbcEdit.weightx = 1.0; editFormPanel.add(editProvinceCombo, gbcEdit); gbcEdit.weightx = 0.0; gridYEdit++;
            gbcEdit.gridx = 0; gbcEdit.gridy = gridYEdit; editFormPanel.add(createFormLabel("City/Municipality:"), gbcEdit); gbcEdit.gridx = 1; gbcEdit.weightx = 1.0; editFormPanel.add(editCityCombo, gbcEdit); gbcEdit.weightx = 0.0; gridYEdit++;
            gbcEdit.gridx = 0; gbcEdit.gridy = gridYEdit; editFormPanel.add(createFormLabel("Barangay:"), gbcEdit); gbcEdit.gridx = 1; gbcEdit.weightx = 1.0; editFormPanel.add(editBarangayCombo, gbcEdit); gbcEdit.weightx = 0.0; gridYEdit++;
            gbcEdit.gridx = 0; gbcEdit.gridy = gridYEdit; editFormPanel.add(createFormLabel("Street Address:"), gbcEdit); gbcEdit.gridx = 1; gbcEdit.weightx = 1.0; editFormPanel.add(editStreetAddressField, gbcEdit); gbcEdit.weightx = 0.0; gridYEdit++;
            gbcEdit.gridx = 0; gbcEdit.gridy = gridYEdit; editFormPanel.add(createFormLabel("Set as default:"), gbcEdit); gbcEdit.gridx = 1; gbcEdit.fill = GridBagConstraints.NONE; editFormPanel.add(editDefaultAddressRadio, gbcEdit); gbcEdit.fill = GridBagConstraints.HORIZONTAL;


            editRecipientNameField.setText(addressToEdit.getRecipientName()); editPhoneField.setText(addressToEdit.getPhone()); editStreetAddressField.setText(addressToEdit.getStreetAddress()); editDefaultAddressRadio.setSelected(addressToEdit.isDefault());
            addEditDropdownListeners(editRegionCombo, editProvinceCombo, editCityCombo, editBarangayCombo);
            // Populate dropdowns *after* adding listeners to ensure correct cascading load
            populateEditDropdowns(addressToEdit, editRegionCombo, editProvinceCombo, editCityCombo, editBarangayCombo);


            JButton updateButton = createStyledButton("Update Address", ThemeColors.PRIMARY);
            updateButton.addActionListener(e -> {
                String editedRecipientName = editRecipientNameField.getText().trim(); String editedPhone = editPhoneField.getText().trim(); Region editedRegion = (Region) editRegionCombo.getSelectedItem(); Province editedProvince = (Province) editProvinceCombo.getSelectedItem(); Municipality editedCity = (Municipality) editCityCombo.getSelectedItem(); Barangay editedBarangay = (Barangay) editBarangayCombo.getSelectedItem(); String editedStreet = editStreetAddressField.getText().trim();
                if (editedRecipientName.isEmpty() || editedPhone.isEmpty() || editedRegion == null || editedRegion.getId().equals("-1") || editedProvince == null || editedProvince.getId().equals("-1") || editedCity == null || editedCity.getId().equals("-1") || editedBarangay == null || editedBarangay.getId().equals("-1") || editedStreet.isEmpty()) { showThemedJOptionPane("Please fill in all address fields.", "Incomplete Address", JOptionPane.WARNING_MESSAGE); return; }
                if (!editedPhone.matches("^[\\d\\s+-]+$")) { showThemedJOptionPane("Please enter a valid phone number.", "Invalid Phone", JOptionPane.WARNING_MESSAGE); return; }
                Address updatedAddress = new Address(addressToEdit.getId(), customerId, editedRegion.getId(), editedProvince.getId(), editedCity.getId(), editedBarangay.getId(), editedStreet, editDefaultAddressRadio.isSelected(), editedRegion.getName(), editedProvince.getName(), editedCity.getName(), editedBarangay.getName(), editedRecipientName, editedPhone);
                boolean updateSuccess = updateAddressInDatabase(updatedAddress);
                if (updateSuccess) { showThemedJOptionPane("Address updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE); editDialog.dispose(); refreshAddressSelectionPanel(); }
            });
            JButton cancelButton = createStyledButton("Cancel", ThemeColors.SECONDARY); cancelButton.addActionListener(e -> editDialog.dispose());
            JPanel editButtonPanel = createButtonPanel(); editButtonPanel.add(updateButton); editButtonPanel.add(cancelButton);

            JScrollPane editScrollPane = new JScrollPane(editFormPanel); styleScrollPane(editScrollPane); editScrollPane.setBorder(new LineBorder(ThemeColors.SECONDARY)); editScrollPane.getViewport().setBackground(ThemeColors.BACKGROUND);
            editDialog.add(editScrollPane, BorderLayout.CENTER);
            editDialog.add(editButtonPanel, BorderLayout.SOUTH);
            editDialog.setVisible(true);
        }

        // --- Edit Dropdown Listeners/Updaters ---
        private void addEditDropdownListeners(JComboBox<Region> r, JComboBox<Province> p, JComboBox<Municipality> m, JComboBox<Barangay> b) { addAddDropdownListeners(r, p, m, b); }
        // updateEditProvinces, updateEditCities, updateEditBarangays reuse the Add panel logic

         // --- Populate Edit Dropdowns ---
        private void populateEditDropdowns(Address addr, JComboBox<Region> rCombo, JComboBox<Province> pCombo, JComboBox<Municipality> mCombo, JComboBox<Barangay> bCombo) {
             rCombo.removeAllItems(); rCombo.addItem(new Region("-1", "-- Select Region --"));
             List<Region> regions = getAllRegions();
             regions.forEach(rCombo::addItem);
             selectComboBoxItemById(rCombo, addr.getRegionId()); // Select the current region

             // Trigger cascade after selection (or manually if needed)
              // Selection should trigger the listener added in addEditDropdownListeners
              // If selection happens before listener attachment, manually call update:
              if (pCombo.getItemCount() <= 1 && rCombo.getSelectedIndex() > 0) {
                  updateAddProvinces(rCombo, pCombo, mCombo, bCombo);
              }
             selectComboBoxItemById(pCombo, addr.getProvinceId());

             if (mCombo.getItemCount() <= 1 && pCombo.getSelectedIndex() > 0) {
                  updateAddCities(pCombo, mCombo, bCombo);
              }
             selectComboBoxItemById(mCombo, addr.getMunicipalityId());

             if (bCombo.getItemCount() <= 1 && mCombo.getSelectedIndex() > 0) {
                  updateAddBarangays(mCombo, bCombo);
              }
             selectComboBoxItemById(bCombo, addr.getBarangayId());
         }

         // Helper to select item in ComboBox by ID
        private <T> void selectComboBoxItemById(JComboBox<T> comboBox, String targetId) {
            if (targetId == null || targetId.isEmpty() || !comboBox.isEnabled() || comboBox.getItemCount() <= 1) return;
             for (int i = 0; i < comboBox.getItemCount(); i++) {
                 T item = comboBox.getItemAt(i); String itemId = null;
                 if (item instanceof Region) itemId = ((Region) item).getId(); else if (item instanceof Province) itemId = ((Province) item).getId(); else if (item instanceof Municipality) itemId = ((Municipality) item).getId(); else if (item instanceof Barangay) itemId = ((Barangay) item).getId();
                 if (itemId != null && itemId.equals(targetId)) { comboBox.setSelectedIndex(i); return; }
             }
             System.err.println("[AddressManager WARNING] Could not find item with ID " + targetId + " in " + comboBox.getClass().getSimpleName());
         }

        // --- getSelectedAddress ---
        public Address getSelectedAddress() { return selectedAddress; }

        // --- Theming Helpers ---
        private JLabel createFormLabel(String text) { JLabel l=new JLabel(text); l.setFont(new Font("Arial", Font.BOLD, 14)); l.setForeground(ThemeColors.TEXT); return l; }
        private <T extends JComboBox<?>> T styleComboBox(T cb) { cb.setFont(new Font("Arial", Font.PLAIN, 14)); cb.setBackground(ThemeColors.CARD_BG); cb.setForeground(ThemeColors.TEXT); cb.setBorder(new LineBorder(ThemeColors.SECONDARY)); cb.setRenderer(new DefaultListCellRenderer() { @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) { super.getListCellRendererComponent(l, v, i, s, f); setBackground(s ? ThemeColors.PRIMARY : ThemeColors.CARD_BG); setForeground(s ? Color.WHITE : ThemeColors.TEXT); return this; } }); return cb; }
        private <T extends JTextField> T styleTextField(T f) { f.setFont(new Font("Arial", Font.PLAIN, 14)); f.setBackground(ThemeColors.CARD_BG); f.setForeground(ThemeColors.TEXT); f.setCaretColor(ThemeColors.TEXT); f.setBorder(new CompoundBorder(new LineBorder(ThemeColors.SECONDARY, 1), new EmptyBorder(5, 8, 5, 8))); return f; }
        private void styleRadioButton(JRadioButton r) { r.setForeground(ThemeColors.TEXT); r.setBackground(ThemeColors.BACKGROUND); r.setIcon(new RadioButtonIcon(false)); r.setSelectedIcon(new RadioButtonIcon(true)); r.setOpaque(false); r.setContentAreaFilled(false); r.setBorderPainted(false); r.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
        private JButton createStyledButton(String text, Color bg) { JButton b = new JButton(text); b.setFont(new Font("Arial", Font.BOLD, 14)); b.setBackground(bg); b.setForeground(Color.WHITE); b.setFocusPainted(false); b.setBorder(new EmptyBorder(10, 25, 10, 25)); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); b.addMouseListener(new MouseAdapter() { Color o = bg; @Override public void mouseEntered(MouseEvent e) { b.setBackground(ThemeColors.BUTTON_HOVER.equals(bg) ? bg.brighter() : ThemeColors.BUTTON_HOVER); } @Override public void mouseExited(MouseEvent e) { b.setBackground(o); } }); return b; } // Adjusted hover slightly for delete button
        private JPanel createButtonPanel() { JPanel p=new JPanel(new FlowLayout(FlowLayout.RIGHT)); p.setBackground(ThemeColors.BACKGROUND); p.setBorder(new EmptyBorder(10, 0, 5, 5)); return p; }
        private String colorToHex(Color c) { return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue()); }
        private void styleTabbedPane(JTabbedPane tp) { tp.setBackground(ThemeColors.BACKGROUND); tp.setForeground(ThemeColors.TEXT); UIManager.put("TabbedPane.selectedForeground", ThemeColors.PRIMARY); UIManager.put("TabbedPane.foreground", ThemeColors.TEXT); UIManager.put("TabbedPane.contentAreaColor", ThemeColors.BACKGROUND); UIManager.put("TabbedPane.selectedBackground", ThemeColors.CARD_BG); UIManager.put("TabbedPane.unselectedBackground", ThemeColors.BACKGROUND); UIManager.put("TabbedPane.borderHightlightColor", ThemeColors.SECONDARY); UIManager.put("TabbedPane.darkShadow", ThemeColors.BACKGROUND); UIManager.put("TabbedPane.light", ThemeColors.BACKGROUND); UIManager.put("TabbedPane.shadow", ThemeColors.BACKGROUND); }
        private void styleScrollPane(JScrollPane sp) { sp.getVerticalScrollBar().setUnitIncrement(16); JScrollBar vsb = sp.getVerticalScrollBar(); vsb.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() { @Override protected void configureScrollBarColors() { thumbColor = ThemeColors.PRIMARY; trackColor = ThemeColors.CARD_BG; } @Override protected JButton createDecreaseButton(int o) { return createZeroButton(); } @Override protected JButton createIncreaseButton(int o) { return createZeroButton(); } private JButton createZeroButton() { JButton b = new JButton(); Dimension z = new Dimension(0, 0); b.setPreferredSize(z); b.setMinimumSize(z); b.setMaximumSize(z); return b; } }); vsb.setBackground(ThemeColors.BACKGROUND); vsb.setBorder(null); }
        // Modified to use the static, themed JOptionPane helper
        private void showThemedJOptionPane(String message, String title, int messageType) {
            AddressManager.showThemedJOptionPaneStatic(this, message, title, messageType);
        }

    } // End AddressSelectionDialog Inner Class
}