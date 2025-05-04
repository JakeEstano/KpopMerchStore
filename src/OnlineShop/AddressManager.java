package OnlineShop;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.ArrayList;

public class AddressManager {
    // Changed from CustomerFrame to Window to allow more flexibility (e.g., CheckoutFrame can be parent)
    // Parent window is used primarily for dialog positioning.
    private Window parentWindow;
    private int customerId;

    // Updated constructor to accept Window
    public AddressManager(Window parentWindow, int customerId) {
        this.parentWindow = parentWindow;
        this.customerId = customerId;
    }

    // Static inner class for Address
    public static class Address {
        private int id;
        private int customerId;
        private String regionId;
        private String provinceId;
        private String municipalityId;
        private String barangayId;
        private String streetAddress; // Renamed from street for clarity
        private boolean isDefault;
        private String regionName;
        private String provinceName;
        private String municipalityName;
        private String barangayName;
        private String recipientName; // Added field
        private String phone;         // Added field

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
            this.recipientName = recipientName; // Initialize
            this.phone = phone;                 // Initialize
        }

        // Overloaded constructor for compatibility if needed (or remove if all calls updated)
         public Address(int id, int customerId, String regionId, String provinceId,
                       String municipalityId, String barangayId, String streetAddress, boolean isDefault,
                       String regionName, String provinceName, String municipalityName, String barangayName) {
              this(id, customerId, regionId, provinceId, municipalityId, barangayId, streetAddress, isDefault,
                  regionName, provinceName, municipalityName, barangayName, "", ""); // Default name/phone
         }

        // Constructor for PaymentFrame compatibility (simple address)
        // Note: This might be insufficient if full address details are needed elsewhere.
        public Address(int id, String streetAddress, String city, String state, String zip, String country) {
             this(id, -1, null, null, null, null, streetAddress, false, country, state, city, "", "", "");
             // Assuming state maps to province, city to municipality, country to region for display purposes
             this.provinceName = state;
             this.municipalityName = city;
             this.regionName = country;
             // Other fields remain null or default
        }

        // Getters
        public int getId() { return id; }
        public int getCustomerId() { return customerId; } // Getter for customerId if needed elsewhere
        public String getRegionId() { return regionId; }
        public String getProvinceId() { return provinceId; }
        public String getMunicipalityId() { return municipalityId; }
        public String getBarangayId() { return barangayId; }
        public String getStreetAddress() { return streetAddress; } // Renamed getter
        public boolean isDefault() { return isDefault; }
        public String getRegionName() { return regionName; }
        public String getProvinceName() { return provinceName; }
        public String getMunicipalityName() { return municipalityName; }
        public String getBarangayName() { return barangayName; }
        public String getRecipientName() { return recipientName; } // Added getter
        public String getPhone() { return phone; }                 // Added getter


        // Used for display in labels/cards
        public String getFullAddressForDisplay() {
             String defaultTag = isDefault ? "<b>[Default]</b> " : "";
             String namePart = (recipientName != null && !recipientName.isEmpty()) ? recipientName + " | " + phone + "<br>" : "";
             String addressPart = String.format("%s, %s, %s<br>%s, %s",
                 streetAddress != null ? streetAddress : "N/A",
                 barangayName != null && !barangayName.isEmpty() ? barangayName : "N/A",
                 municipalityName != null ? municipalityName : "N/A",
                 provinceName != null ? provinceName : "N/A",
                 regionName != null ? regionName : "N/A");
             return "<html>" + defaultTag + namePart + addressPart + "</html>";
        }

         // Simpler format for other uses if needed
         public String getFormattedAddress() {
             return String.format("%s, %s, %s, %s, %s",
                 streetAddress != null ? streetAddress : "",
                 barangayName != null && !barangayName.isEmpty() ? barangayName : "",
                 municipalityName != null ? municipalityName : "",
                 provinceName != null ? provinceName : "",
                 regionName != null ? regionName : "");
         }
    }

    // This method now returns null, as the UI dialog is moved to CheckoutFrame.
    // Keeping the signature for potential future use or internal logic, but it won't show a dialog.
    public Address showAddressSelection() {
         System.err.println("Warning: AddressManager.showAddressSelection() called, but UI dialog is now in CheckoutFrame. Returning default address or null.");
         // Optionally, return the default address directly without UI
         return getDefaultAddress();
         // Or simply return null if no direct selection logic is needed here anymore:
         // return null;
    }

    // Fetches all addresses for the customer
    public List<Address> getCustomerAddresses() {
        List<Address> addresses = new ArrayList<>();
        // Updated query to include barangay_name, recipient_name, phone
        String sql = "SELECT ca.id, ca.customer_id, ca.region_id, ca.province_id, " +
                     "ca.municipality_id, ca.barangay_id, ca.street_address, ca.is_default, " + // Changed street to street_address
                     "tr.region_name, tp.province_name, tm.municipality_name, tb.barangay_name, " + // Added tb.barangay_name
                     "ca.recipient_name, ca.phone " + // Added recipient_name, phone
                     "FROM customer_addresses ca " +
                     "LEFT JOIN table_region tr ON ca.region_id = tr.region_id " +
                     "LEFT JOIN table_province tp ON ca.province_id = tp.province_id " +
                     "LEFT JOIN table_municipality tm ON ca.municipality_id = tm.municipality_id " +
                     "LEFT JOIN table_barangay tb ON ca.barangay_id = tb.barangay_id " + // Join barangay table
                     "WHERE ca.customer_id = ? ORDER BY ca.is_default DESC, ca.id DESC"; // Sort by default, then latest

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                addresses.add(new Address(
                    rs.getInt("id"),
                    rs.getInt("customer_id"),
                    rs.getString("region_id"),
                    rs.getString("province_id"),
                    rs.getString("municipality_id"),
                    rs.getString("barangay_id"),
                    rs.getString("street_address"), // Changed here
                    rs.getBoolean("is_default"),
                    rs.getString("region_name"),
                    rs.getString("province_name"),
                    rs.getString("municipality_name"),
                    rs.getString("barangay_name"), // Get barangay name from result set
                    rs.getString("recipient_name"), // Get recipient name
                    rs.getString("phone")           // Get phone
                ));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
             // Use the static helper for themed JOptionPane
            showThemedJOptionPaneStatic(parentWindow, "Error loading addresses: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return addresses;
    }

    // Fetches the default address directly
    public Address getDefaultAddress() {
         List<Address> addresses = getCustomerAddresses();
         for (Address addr : addresses) {
             if (addr.isDefault()) {
                 return addr;
             }
         }
         // If no default found, return the first one in the list (often the most recent non-default)
         if (!addresses.isEmpty()) {
            System.out.println("No default address found, returning first available address.");
             return addresses.get(0);
         }
         return null; // No addresses found at all
    }


    // Saves a new address to the database
     public boolean saveAddressToDatabase(Address address) {
         Connection conn = null;
         boolean success = false;
         try {
             conn = DBConnection.connect();
             conn.setAutoCommit(false); // Start transaction

              // Updated SQL to include recipient_name and phone
             String sql = "INSERT INTO customer_addresses " +
                          "(customer_id, recipient_name, phone, region_id, province_id, municipality_id, barangay_id, street_address, is_default) " + // Changed street to street_address
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

             try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                 stmt.setInt(1, customerId);
                 stmt.setString(2, address.getRecipientName());
                 stmt.setString(3, address.getPhone());
                 stmt.setString(4, address.getRegionId());
                 stmt.setString(5, address.getProvinceId());
                 stmt.setString(6, address.getMunicipalityId());
                 stmt.setString(7, address.getBarangayId());
                 stmt.setString(8, address.getStreetAddress()); // Changed here
                 stmt.setBoolean(9, address.isDefault());

                 int rowsAffected = stmt.executeUpdate();

                 if (rowsAffected > 0) {
                      if (address.isDefault()) {
                          try (ResultSet rs = stmt.getGeneratedKeys()) {
                              if (rs.next()) {
                                  // Set this new address as default, unsetting others
                                  setAsDefault(conn, rs.getInt(1));
                              }
                          }
                      }
                      conn.commit(); // Commit transaction
                      success = true;
                 } else {
                     conn.rollback(); // Rollback if insert failed
                     System.err.println("ERROR: Insert into customer_addresses affected 0 rows.");
                 }
             } catch (SQLException ex) {
                 if (conn != null) conn.rollback(); // Rollback on specific SQL error
                 throw ex; // Re-throw to be caught by outer catch
             }

         } catch (SQLException ex) {
             ex.printStackTrace();
             showThemedJOptionPaneStatic(parentWindow, "Error saving address: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
         } finally {
             if (conn != null) {
                 try {
                    // Only reset autoCommit if it was successfully set to false
                    if (!conn.getAutoCommit()) {
                         conn.setAutoCommit(true);
                    }
                     conn.close();
                 } catch (SQLException ex) {
                     ex.printStackTrace();
                 }
             }
         }
         return success;
     }


    /**
     * Saves a new address to the database and returns the complete Address object
     * including the generated ID.
     *
     * @param address The Address object containing the details to save (ID field will be ignored).
     * @return The saved Address object with its new ID, or null if the save failed.
     */
    public Address saveAddressToDatabaseAndReturn(Address address) {
        Connection conn = null;
        Address savedAddress = null; // Variable to hold the result

        try {
            conn = DBConnection.connect();
            conn.setAutoCommit(false); // Start transaction

            String sql = "INSERT INTO customer_addresses " +
                         "(customer_id, recipient_name, phone, region_id, province_id, municipality_id, barangay_id, street_address, is_default) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            // Use Statement.RETURN_GENERATED_KEYS to get the new ID
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, customerId);
                stmt.setString(2, address.getRecipientName());
                stmt.setString(3, address.getPhone());
                stmt.setString(4, address.getRegionId());
                stmt.setString(5, address.getProvinceId());
                stmt.setString(6, address.getMunicipalityId());
                stmt.setString(7, address.getBarangayId());
                stmt.setString(8, address.getStreetAddress());
                stmt.setBoolean(9, address.isDefault());

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    int generatedId = -1;
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            generatedId = rs.getInt(1); // Get the generated ID
                        }
                    }

                    if (generatedId != -1) {
                        // If the new address should be default, update others using the transaction connection
                        if (address.isDefault()) {
                            try {
                                setAsDefault(conn, generatedId); // Call private helper within transaction
                            } catch (SQLException e) {
                                // Handle error setting default, might need to rollback
                                System.err.println("Error setting new address as default: " + e.getMessage());
                                conn.rollback(); // Rollback if setting default fails
                                throw e; // Re-throw to be caught by outer catch
                            }
                        }

                        // Create the Address object to return, using the generated ID and names from input
                        // Assumes the input 'address' object already has the correct names resolved
                        savedAddress = new Address(
                            generatedId,                // Use the generated ID
                            customerId,
                            address.getRegionId(),
                            address.getProvinceId(),
                            address.getMunicipalityId(),
                            address.getBarangayId(),
                            address.getStreetAddress(),
                            address.isDefault(),
                            address.getRegionName(),    // Reuse names from input
                            address.getProvinceName(),
                            address.getMunicipalityName(),
                            address.getBarangayName(),
                            address.getRecipientName(),
                            address.getPhone()
                        );

                        conn.commit(); // Commit transaction ONLY if ID retrieved and default set (if needed)
                    } else {
                        // This should not happen if rowsAffected > 0, but handle defensively
                        System.err.println("CRITICAL: Insert succeeded but failed to retrieve generated address ID.");
                        conn.rollback(); // Rollback if ID retrieval failed
                    }
                } else {
                    // Insert failed (0 rows affected)
                    conn.rollback(); // Rollback if insert failed
                    System.err.println("ERROR: Insert into customer_addresses affected 0 rows.");
                }
            } catch (SQLException ex) {
                if (conn != null) {
                    try { conn.rollback(); } catch (SQLException eRollback) { eRollback.printStackTrace(); }
                }
                throw ex; // Re-throw to be caught by outer catch block
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            // Use the static helper method from AddressManager itself for consistency
            AddressManager.showThemedJOptionPaneStatic(parentWindow, "Error saving address: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            savedAddress = null; // Ensure null is returned on error
        } finally {
            if (conn != null) {
                try {
                    // Only reset autoCommit if it was successfully set to false
                    if (!conn.getAutoCommit()) {
                         conn.setAutoCommit(true);
                    }
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
        // Return the newly created Address object (with ID) or null if failed
        return savedAddress;
    }


    // Updates an existing address in the database
    public boolean updateAddressInDatabase(Address address) {
         Connection conn = null;
         boolean success = false;
         try {
             conn = DBConnection.connect();
             conn.setAutoCommit(false); // Start transaction

             // Updated SQL to include recipient_name and phone
             String sql = "UPDATE customer_addresses SET " +
                          "recipient_name = ?, phone = ?, region_id = ?, province_id = ?, municipality_id = ?, " +
                          "barangay_id = ?, street_address = ?, is_default = ? " + // Changed street to street_address
                          "WHERE id = ? AND customer_id = ?";

             try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                 stmt.setString(1, address.getRecipientName());
                 stmt.setString(2, address.getPhone());
                 stmt.setString(3, address.getRegionId());
                 stmt.setString(4, address.getProvinceId());
                 stmt.setString(5, address.getMunicipalityId());
                 stmt.setString(6, address.getBarangayId());
                 stmt.setString(7, address.getStreetAddress()); // Changed here
                 stmt.setBoolean(8, address.isDefault());
                 stmt.setInt(9, address.getId());
                 stmt.setInt(10, customerId); // Use instance variable customerId

                 int rowsAffected = stmt.executeUpdate();

                 if (rowsAffected > 0) {
                     if (address.isDefault()) {
                         // Ensure other addresses are not default
                         setAsDefault(conn, address.getId()); // Use the helper within transaction
                     }
                     conn.commit(); // Commit transaction
                     success = true;
                 } else {
                     conn.rollback(); // Rollback if update failed (wrong ID or customer)
                     System.err.println("WARN: Update failed for address ID " + address.getId() + " (0 rows affected).");
                 }
             } catch (SQLException ex) {
                 if (conn != null) conn.rollback(); // Rollback on specific SQL error
                 throw ex; // Re-throw to be caught by outer catch
             }

         } catch (SQLException ex) {
             ex.printStackTrace();
             showThemedJOptionPaneStatic(parentWindow, "Error updating address: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
         } finally {
             if (conn != null) {
                 try {
                    // Only reset autoCommit if it was successfully set to false
                    if (!conn.getAutoCommit()) {
                         conn.setAutoCommit(true);
                    }
                     conn.close();
                 } catch (SQLException ex) {
                     ex.printStackTrace();
                 }
             }
         }
         return success;
     }


     // Deletes an address from the database
     public boolean deleteAddressFromDatabase(int addressId) {
         Connection conn = null;
         boolean success = false;
         try {
             conn = DBConnection.connect();
             conn.setAutoCommit(false); // Use transaction

             // Optional: Check if the address being deleted is the default.
             boolean wasDefault = false;
             int otherAddressCount = 0;
             String checkDefaultSql = "SELECT is_default FROM customer_addresses WHERE id = ? AND customer_id = ?";
             String countSql = "SELECT COUNT(*) FROM customer_addresses WHERE customer_id = ? AND id != ?";

             try (PreparedStatement checkStmt = conn.prepareStatement(checkDefaultSql);
                  PreparedStatement countStmt = conn.prepareStatement(countSql)) {

                 checkStmt.setInt(1, addressId);
                 checkStmt.setInt(2, customerId);
                 ResultSet rs = checkStmt.executeQuery();
                 if (rs.next()) {
                     wasDefault = rs.getBoolean("is_default");
                 } else {
                     // Address not found for this customer
                     conn.rollback();
                     showThemedJOptionPaneStatic(parentWindow, "Address not found or does not belong to this customer.", "Deletion Failed", JOptionPane.WARNING_MESSAGE);
                     return false;
                 }

                 countStmt.setInt(1, customerId);
                 countStmt.setInt(2, addressId);
                 ResultSet countRs = countStmt.executeQuery();
                 if (countRs.next()) {
                     otherAddressCount = countRs.getInt(1);
                 }

             } catch (SQLException ex) {
                  if (conn != null) conn.rollback();
                  throw ex;
             }

             String sql = "DELETE FROM customer_addresses WHERE id = ? AND customer_id = ?";
             try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                 stmt.setInt(1, addressId);
                 stmt.setInt(2, customerId);
                 int rowsAffected = stmt.executeUpdate();

                 if (rowsAffected > 0) {
                      // If the deleted address was the default, and there are other addresses,
                      // automatically set the most recently added address as the new default.
                      if (wasDefault && otherAddressCount > 0) {
                           String setNewDefaultSql = "UPDATE customer_addresses SET is_default = 1 " +
                                                     "WHERE id = (" + // Find the highest ID among remaining addresses
                                                     "  SELECT id FROM customer_addresses " +
                                                     "  WHERE customer_id = ? ORDER BY id DESC LIMIT 1" +
                                                     ")";
                           try (PreparedStatement setDefaultStmt = conn.prepareStatement(setNewDefaultSql)) {
                               setDefaultStmt.setInt(1, customerId);
                               setDefaultStmt.executeUpdate();
                                System.out.println("DEBUG: Set new default address after deleting the previous default.");
                           } catch (SQLException setDefaultEx) {
                                // Log error but don't necessarily fail the delete - rollback?
                                System.err.println("Error setting new default address after deletion: " + setDefaultEx.getMessage());
                                conn.rollback(); // Rollback if setting new default fails is safer
                                throw setDefaultEx; // Re-throw
                           }
                      }

                     conn.commit(); // Commit transaction
                     success = true;
                 } else {
                     conn.rollback(); // Rollback if delete failed (wrong ID or customer)
                      // Message already shown if address not found earlier
                      if (!wasDefault && otherAddressCount == 0) { // Check if it existed but wasn't found now
                           showThemedJOptionPaneStatic(parentWindow, "Address not found or does not belong to this customer.", "Deletion Failed", JOptionPane.WARNING_MESSAGE);
                      }
                 }
             } catch (SQLException ex) {
                 if (conn != null) conn.rollback(); // Rollback on specific SQL error
                 // Check for foreign key constraints (e.g., address linked to an order)
                  if (ex.getMessage().toLowerCase().contains("foreign key constraint")) {
                     showThemedJOptionPaneStatic(parentWindow, "Cannot delete address. It is linked to one or more orders.", "Deletion Failed", JOptionPane.ERROR_MESSAGE);
                  } else {
                      throw ex; // Re-throw other SQL exceptions
                  }
             }

         } catch (SQLException ex) {
             ex.printStackTrace();
             showThemedJOptionPaneStatic(parentWindow, "Error deleting address: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
         } finally {
             if (conn != null) {
                 try {
                     // Only reset autoCommit if it was successfully set to false
                    if (!conn.getAutoCommit()) {
                         conn.setAutoCommit(true);
                    }
                     conn.close();
                 } catch (SQLException ex) {
                     ex.printStackTrace();
                 }
             }
         }
         return success;
     }


    // Sets a specific address as default, unsetting others for the customer
    // This is now private and requires an active connection (used within transactions)
    private void setAsDefault(Connection conn, int addressId) throws SQLException {
        // First, unset default for all other addresses of this customer
        String unsetSql = "UPDATE customer_addresses SET is_default = FALSE WHERE customer_id = ? AND id != ?";
        try (PreparedStatement stmt = conn.prepareStatement(unsetSql)) {
            stmt.setInt(1, customerId);
            stmt.setInt(2, addressId);
            stmt.executeUpdate();
        }
        // Then, set the specified address as default
        String setSql = "UPDATE customer_addresses SET is_default = TRUE WHERE id = ? AND customer_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(setSql)) {
            stmt.setInt(1, addressId);
            stmt.setInt(2, customerId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                // Should not happen if called correctly within transaction, but log if it does
                System.err.println("WARN: setAsDefault failed to set ID " + addressId + " as default (0 rows affected).");
            }
        }
    }

    // --- Geographic Data Fetching Methods (No change needed for these) ---

    public List<Region> getAllRegions() {
        List<Region> regions = new ArrayList<>();
        // Added filtering to exclude rows where region_id is literally 'region_id' (header row?)
        String sql = "SELECT region_id, region_name FROM table_region WHERE region_id != 'region_id' ORDER BY region_name";
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                regions.add(new Region(rs.getString("region_id"), rs.getString("region_name")));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showThemedJOptionPaneStatic(parentWindow,"Error loading regions: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return regions;
    }

    public List<Province> getProvincesByRegion(String regionId) {
        List<Province> provinces = new ArrayList<>();
         // Added filtering to exclude rows where province_id is literally 'province_id'
        String sql = "SELECT province_id, province_name FROM table_province WHERE region_id = ? AND province_id != 'province_id' ORDER BY province_name";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, regionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                provinces.add(new Province(rs.getString("province_id"), rs.getString("province_name")));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showThemedJOptionPaneStatic(parentWindow, "Error loading provinces: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return provinces;
    }

    public List<Municipality> getCitiesByProvince(String provinceId) {
        List<Municipality> cities = new ArrayList<>();
         // Added filtering to exclude rows where municipality_id is literally 'municipality_id'
        String sql = "SELECT municipality_id, municipality_name FROM table_municipality WHERE province_id = ? AND municipality_id != 'municipality_id' ORDER BY municipality_name";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, provinceId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                cities.add(new Municipality(rs.getString("municipality_id"), rs.getString("municipality_name")));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showThemedJOptionPaneStatic(parentWindow,"Error loading cities: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return cities;
    }

    public List<Barangay> getBarangaysByCity(String cityId) {
        List<Barangay> barangays = new ArrayList<>();
         // Added filtering to exclude rows where barangay_id is literally 'barangay_id'
        String sql = "SELECT barangay_id, barangay_name FROM table_barangay WHERE municipality_id = ? AND barangay_id != 'barangay_id' ORDER BY barangay_name";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cityId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                barangays.add(new Barangay(rs.getString("barangay_id"), rs.getString("barangay_name")));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showThemedJOptionPaneStatic(parentWindow,"Error loading barangays: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return barangays;
    }

    // --- Helper classes for geographic entities (Static inner classes) ---
    public static class Region {
        private String id;
        private String name;

        public Region(String id, String name) { this.id = id; this.name = name; }
        public String getId() { return id; }
        public String getName() { return name; }
        @Override public String toString() { return name; }
         // Implement equals and hashCode if used in Sets or Maps
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Region region = (Region) o;
            return Objects.equals(id, region.id);
        }
        @Override public int hashCode() { return Objects.hash(id); }
    }

    public static class Province {
        private String id;
        private String name;

        public Province(String id, String name) { this.id = id; this.name = name; }
        public String getId() { return id; }
        public String getName() { return name; }
        @Override public String toString() { return name; }
         // Implement equals and hashCode if used in Sets or Maps
         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
             Province province = (Province) o;
             return Objects.equals(id, province.id);
         }
         @Override public int hashCode() { return Objects.hash(id); }
    }

    public static class Municipality {
        private String id;
        private String name;

        public Municipality(String id, String name) { this.id = id; this.name = name; }
        public String getId() { return id; }
        public String getName() { return name; }
        @Override public String toString() { return name; }
         // Implement equals and hashCode if used in Sets or Maps
         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
             Municipality that = (Municipality) o;
             return Objects.equals(id, that.id);
         }
         @Override public int hashCode() { return Objects.hash(id); }
    }

    public static class Barangay {
        private String id;
        private String name;

        public Barangay(String id, String name) { this.id = id; this.name = name; }
        public String getId() { return id; }
        public String getName() { return name; }
        @Override public String toString() { return name; }
         // Implement equals and hashCode if used in Sets or Maps
         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
             Barangay barangay = (Barangay) o;
             return Objects.equals(id, barangay.id);
         }
         @Override public int hashCode() { return Objects.hash(id); }
    }

     // --- Static Helper for Themed JOptionPane ---
     // Now takes a Component parent instead of requiring specific Frame types
     public static void showThemedJOptionPaneStatic(Component parent, String message, String title, int messageType) {
         // Ensure theme settings are applied before showing the dialog
         // These might be redundant if set globally in main(), but ensures consistency
         UIManager.put("OptionPane.background", ThemeColors.DIALOG_BG);
         UIManager.put("Panel.background", ThemeColors.DIALOG_BG); // Affects OptionPane panel
         UIManager.put("OptionPane.messageForeground", ThemeColors.DIALOG_FG);
         UIManager.put("Button.background", ThemeColors.SECONDARY);
         UIManager.put("Button.foreground", Color.WHITE);
         UIManager.put("Button.focus", new Color(ThemeColors.SECONDARY.getRed(), ThemeColors.SECONDARY.getGreen(), ThemeColors.SECONDARY.getBlue(), 180));
         UIManager.put("Button.hoverBackground", ThemeColors.BUTTON_HOVER);
         UIManager.put("Button.pressedBackground", ThemeColors.SECONDARY.darker());

         JOptionPane.showMessageDialog(parent, message, title, messageType);
     }

    // *** REMOVED AddressSelectionDialog inner class. ***
    // *** Its functionality is now integrated into CheckoutFrame.AddressManagementDialog ***

}