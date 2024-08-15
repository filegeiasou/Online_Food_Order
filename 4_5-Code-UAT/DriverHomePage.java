import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DriverHomePage extends JFrame {
    private JTable ordersTable;
    private DefaultTableModel ordersTableModel;
    private JButton refreshOrders, acceptOrderButton, startDeliveryButton, completeDeliveryButton, contactSupportButton;
    private Timer refreshTimer;
    private int orderId;
    private Connection dbConnection;
    private JPanel deliveryPanel;
    private Map<String, JLabel> deliveryLabels;
    String username;

    public DriverHomePage(String driverUsername) {
        // Initialize database connection
        CredentialsHandler cHandler = new CredentialsHandler();
        dbConnection = cHandler.getDBConnection();
        this.username = driverUsername;

        setTitle("Driver Page");
        setLayout(new BorderLayout());

        // Create the main panels
        JPanel ordersPanel = createOrdersPanel();
        JPanel supportPanel = createSupportPanel();
        deliveryPanel = createDeliveryPanel(); // Delivery details panel
        deliveryPanel.setVisible(false);

        // Add panels to the frame
        add(ordersPanel, BorderLayout.CENTER);
        add(supportPanel, BorderLayout.SOUTH);
        add(deliveryPanel, BorderLayout.NORTH); // Place it at the top

        // Setup frame
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);

        populateOrders();
    }

    private JPanel createOrdersPanel() {
        JPanel ordersPanel = new JPanel(new BorderLayout());
        ordersPanel.setBorder(BorderFactory.createTitledBorder("Available Orders"));

        ordersTableModel = new DefaultTableModel(new Object[]{"Order Number", "Customer", "Customer's Address", "Restaurant", "Total Cost", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        ordersTable = new JTable(ordersTableModel);
        JScrollPane scrollPane = new JScrollPane(ordersTable);

        JPanel actionsPanel = new JPanel();
        refreshOrders = new JButton("Refresh");
        acceptOrderButton = new JButton("Accept Order");

        actionsPanel.add(refreshOrders);
        actionsPanel.add(acceptOrderButton);
        
        ordersPanel.add(scrollPane, BorderLayout.CENTER);
        ordersPanel.add(actionsPanel, BorderLayout.SOUTH);

        refreshOrders.addActionListener(e -> populateOrders());
        acceptOrderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = ordersTable.getSelectedRow();
                if (selectedRow != -1) {
                    orderId = (Integer) ordersTable.getValueAt(selectedRow, 0);
                    acceptOrder(orderId);
                } else {
                    JOptionPane.showMessageDialog(DriverHomePage.this, "Please select an order to accept.");
                }
            }
        });

        return ordersPanel;
    }

    private void populateOrders() {
        // Query to get current orders for the driver
        String query = "SELECT O.ID, C.USERNAME AS Customer, C.ADDRESS as Address, R.NAME AS Restaurant, O.TOTAL_PRICE, O.STATUS " +
                "FROM Orders O " +
                "JOIN Customer C ON O.CUSTOMER_ID = C.ID " +
                "JOIN Restaurant R ON O.RESTAURANT_ID = R.ID " +
                "WHERE O.DRIVER_ID IS NULL OR O.DRIVER_ID = ?";

        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setInt(1, getDriverId()); // Replace with method to get driver ID
            try (ResultSet rs = pstmt.executeQuery()) {
                ordersTableModel.setRowCount(0);
                while (rs.next()) {
                    ordersTableModel.addRow(new Object[]{
                            rs.getInt("ID"),
                            rs.getString("Customer"),
                            rs.getString("Address"),
                            rs.getString("Restaurant"),
                            rs.getString("TOTAL_PRICE") + " €",
                            rs.getString("STATUS")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private JPanel createSupportPanel() {
        JPanel supportPanel = new JPanel();
        contactSupportButton = new JButton("Contact Support");
        JButton logOutButton = new JButton("Log Out");

        supportPanel.add(contactSupportButton);
        supportPanel.add(logOutButton);

        contactSupportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openContactSupportForm();
            }
        });

        logOutButton.addActionListener(e -> {
            dispose();
            new LogInForm();
        });

        return supportPanel;
    }

    private JPanel createDeliveryPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Current Delivery",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), Color.BLUE));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets( 6, 6, 6, 6); // Add padding between components
        gbc.anchor = GridBagConstraints.WEST; // Align components to the left

        // Initialize the map to store labels
        deliveryLabels = new HashMap<>();

        // Define and add labels and values to the panel
        JLabel orderIdLabel = new JLabel("Order Number:");
        JLabel orderIdValue = new JLabel("N/A");
        JLabel customerLabel = new JLabel("Customer:");
        JLabel customerValue = new JLabel("N/A");
        JLabel addressLabel = new JLabel("Customer's Address");
        JLabel addressValue = new JLabel("N/A");
        JLabel restaurantLabel = new JLabel("Restaurant:");
        JLabel restaurantValue = new JLabel("N/A");
        JLabel totalCostLabel = new JLabel("Total Cost:");
        JLabel totalCostValue = new JLabel("N/A");
        JLabel deliveryStatusLabel = new JLabel("Status:");
        JLabel deliveryStatusValue = new JLabel("N/A");

        // Add Order Number Label and Value
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        panel.add(orderIdLabel, gbc);
        gbc.gridx = 1;
        panel.add(orderIdValue, gbc);
        deliveryLabels.put("OrderID", orderIdValue);

        // Add Customer Label and Value
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(customerLabel, gbc);
        gbc.gridx = 1;
        panel.add(customerValue, gbc);
        deliveryLabels.put("Customer", customerValue);

        // Add Restaurant Label and Value
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(addressLabel, gbc);
        gbc.gridx = 1;
        panel.add(addressValue, gbc);
        deliveryLabels.put("Customer's Address", addressValue);

        // Add Total Cost Label and Value
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(restaurantLabel, gbc);
        gbc.gridx = 1;
        panel.add(restaurantValue, gbc);
        deliveryLabels.put("Restaurant", restaurantValue);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(totalCostLabel, gbc);
        gbc.gridx = 1;
        panel.add(totalCostValue, gbc);
        deliveryLabels.put("Total Cost", totalCostValue);

        // Add Status Label and Value
        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(deliveryStatusLabel, gbc);
        gbc.gridx = 1;
        panel.add(deliveryStatusValue, gbc);
        deliveryLabels.put("Status", deliveryStatusValue);

        // Add Start Delivery Button
        JButton startButton = new JButton("Start Delivery");
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1; // Span only one cell
        panel.add(startButton, gbc);

        // Add Complete Delivery Button
        JButton completeButton = new JButton("Complete Delivery");
        gbc.gridx = 1;
        gbc.gridy = 6;
        completeButton.setEnabled(false);
        panel.add(completeButton, gbc);

        // Add button actions
        startButton.addActionListener(e -> {
            // Implement start delivery logic
            deliveryLabels.get("Status").setText("In Progress");
            startButton.setEnabled(false);
            completeButton.setEnabled(true);
            JOptionPane.showMessageDialog(DriverHomePage.this, "Delivery started!");
        });

        completeButton.addActionListener(e -> {
            // Implement complete delivery logic
            deliveryLabels.get("Status").setText("Completed");
            panel.setVisible(false);
            JOptionPane.showMessageDialog(DriverHomePage.this, "Delivery completed!");
        });

        return panel;
    }

    private void updateDeliveryPanel(int orderId) {
        try {
            // Query to get the selected order details
            String query = "SELECT O.ID, C.USERNAME AS Customer, C.ADDRESS as Address, R.NAME AS Restaurant, O.TOTAL_PRICE, O.STATUS " +
                    "FROM Orders O " +
                    "JOIN Customer C ON O.CUSTOMER_ID = C.ID " +
                    "JOIN Restaurant R ON O.RESTAURANT_ID = R.ID " +
                    "WHERE O.ID = ?";

            PreparedStatement pstmt = dbConnection.prepareStatement(query);
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Update the components with actual data
                deliveryLabels.get("OrderID").setText(String.valueOf(rs.getInt("ID")));
                deliveryLabels.get("Customer").setText(rs.getString("Customer"));
                deliveryLabels.get("Customer's Address").setText(rs.getString("Address"));
                deliveryLabels.get("Restaurant").setText(rs.getString("Restaurant"));
                deliveryLabels.get("Total Cost").setText(rs.getString("TOTAL_PRICE") + " €");
                deliveryLabels.get("Status").setText(rs.getString("STATUS"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void acceptOrder(int orderId) {
        try {
            String updateOrderQuery = "UPDATE Orders SET DRIVER_ID = ?, STATUS = 'Accepted' WHERE ID = ?";
            PreparedStatement pstmt = dbConnection.prepareStatement(updateOrderQuery);
            pstmt.setInt(1, getDriverId());
            pstmt.setInt(2, orderId);
            pstmt.executeUpdate();
            populateOrders();
            deliveryPanel.setVisible(true);
            acceptOrderButton.setEnabled(false);
            updateDeliveryPanel(orderId); // Update delivery details panel

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getDriverId() {
        // Method to get driver ID based on username
        try {
            String retrieveIQuery = "SELECT ID FROM DRIVER WHERE USERNAME = ?";
            PreparedStatement stmt = dbConnection.prepareStatement(retrieveIQuery);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                return rs.getInt("ID");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 1; // Replace with actual implementation
    }

    private void openContactSupportForm() {
        // Implement contact support form logic here
        JOptionPane.showMessageDialog(this, "Open contact support form!");
    }
}
