import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class DriverHomePage extends JFrame {
    private JTable ordersTable;
    private DefaultTableModel ordersTableModel;
    private JButton refreshOrders, acceptOrderButton, startDeliveryButton, completeDeliveryButton, contactSupportButton;
    private Timer refreshTimer;
    private int selectedOrderId;
    String username;

    Connection dbConnection;

    public DriverHomePage(String driverUsername) {
        // Initialize database connection
        CredentialsHandler cHandler = new CredentialsHandler();
        dbConnection = cHandler.getDBConnection();
        this.username = driverUsername;

        setTitle("Driver Page");
        setLayout(new BorderLayout());

        // Orders Panel
        JPanel ordersPanel = new JPanel(new BorderLayout());
        ordersPanel.setBorder(BorderFactory.createTitledBorder("Current Orders"));

        ordersTableModel = new DefaultTableModel(new Object[]{"Order ID", "Customer", "Restaurant", "Total Cost", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        ordersTable = new JTable(ordersTableModel);
        JScrollPane scrollPane = new JScrollPane(ordersTable);

        ordersPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel actionsPanel = new JPanel();
        refreshOrders = new JButton("Refresh");
        acceptOrderButton = new JButton("Accept Order");
        startDeliveryButton = new JButton("Start Delivery");
        completeDeliveryButton = new JButton("Complete Delivery");

        // Disable "Accept Order" button initially
        acceptOrderButton.setEnabled(false);

        actionsPanel.add(refreshOrders);
        actionsPanel.add(acceptOrderButton);
        actionsPanel.add(startDeliveryButton);
        actionsPanel.add(completeDeliveryButton);

        ordersPanel.add(actionsPanel, BorderLayout.SOUTH);

        add(ordersPanel, BorderLayout.CENTER);

        // Support Panel
        JPanel supportPanel = new JPanel();
        contactSupportButton = new JButton("Contact Support");
        JButton logOutButton = new JButton("Log Out");

        supportPanel.add(contactSupportButton);
        supportPanel.add(logOutButton);
        add(supportPanel, BorderLayout.SOUTH);

        // Add action listeners
        refreshOrders.addActionListener(e -> populateOrders());

        acceptOrderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedOrderId > 0) {
                    // Implement accept order logic
                    acceptOrder(selectedOrderId);
                    JOptionPane.showMessageDialog(DriverHomePage.this, "Order accepted!");
                } else {
                    JOptionPane.showMessageDialog(DriverHomePage.this, "Please select an order to accept.");
                }
            }
        });

        startDeliveryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Implement start delivery logic
                JOptionPane.showMessageDialog(DriverHomePage.this, "Delivery started!");
            }
        });

        completeDeliveryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Implement complete delivery logic
                JOptionPane.showMessageDialog(DriverHomePage.this, "Delivery completed!");
            }
        });

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

        // Add a ListSelectionListener to the table
        ordersTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                // Only enable the button if a row is selected
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = ordersTable.getSelectedRow();
                    if (selectedRow != -1) {
                        selectedOrderId = (int) ordersTable.getValueAt(selectedRow, 0);
                        acceptOrderButton.setEnabled(true);
                    } else {
                        selectedOrderId = -1;
                        acceptOrderButton.setEnabled(false);
                    }
                }
            }
        });

        populateOrders();

        // Set up timer to refresh orders every 1 seconds (1000 milliseconds)
        // refreshTimer = new Timer(1000, e -> populateOrders());
        // refreshTimer.start();

        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void populateOrders() {
        // Query to get current orders for the driver
        String query = "SELECT O.ID, C.USERNAME AS Customer, R.NAME AS Restaurant, O.TOTAL_PRICE, O.STATUS " +
                "FROM Orders O " +
                "JOIN Customer C ON O.CUSTOMER_ID = C.ID " +
                "JOIN Restaurant R ON O.RESTAURANT_ID = R.ID " +
                "WHERE O.DRIVER_ID IS NULL OR O.DRIVER_ID = ?";

        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setInt(1, getDriverId()); // Replace with method to get driver ID
            try (ResultSet rs = pstmt.executeQuery()) {
                ordersTableModel.setRowCount(0);
                while (rs.next()) {
                    int orderId = rs.getInt("ID");
                    ordersTableModel.addRow(new Object[]{
                            orderId,
                            rs.getString("Customer"),
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

    private void acceptOrder(int orderId) {
        try {
            String changeStatusQuery = "UPDATE Orders SET STATUS = 'Accepted', DRIVER_ID = ? WHERE ID = ?";
            PreparedStatement changeStatusStmt = dbConnection.prepareStatement(changeStatusQuery);
            changeStatusStmt.setInt(1, getDriverId()); // Set the driver ID here
            changeStatusStmt.setInt(2, orderId);
            changeStatusStmt.executeUpdate();
            populateOrders();

        } catch (SQLException e) {
            throw new RuntimeException(e);
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
