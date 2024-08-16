import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DriverHomePage extends JFrame {
    private JTable ordersTable;
    private DefaultTableModel ordersTableModel;
    private JButton refreshOrders, acceptOrderButton, startDeliveryButton, completeDeliveryButton, contactSupportButton;
    private int orderId;
    private Connection dbConnection;
    private JPanel deliveryPanel, topPanel, botPanel;
    private Map<String, JLabel> deliveryLabels;
    String username;

    public DriverHomePage(String driverUsername) {
        // Initialize database connection
        CredentialsHandler cHandler = new CredentialsHandler();
        dbConnection = cHandler.getDBConnection();
        this.username = driverUsername;
        initFrame();
        populateOrders();
    }

    private void initFrame(){
        setTitle("Driver Page");
        setLayout(new BorderLayout());

        // Top panel for logo and welcome message
        topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.setBackground(new Color(0xe7a780));

        botPanel = new JPanel();
        botPanel.setLayout(new BorderLayout());
        botPanel.setBackground(new Color(0x575658));

        // Logo and welcome label
        AppLogo logo = new AppLogo();
        JLabel welcomeLabel = new JLabel("Welcome, " + username + "!");
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 25));
        welcomeLabel.setForeground(Color.WHITE);

        topPanel.add(logo.getLabel(), BorderLayout.CENTER);
        topPanel.add(welcomeLabel, BorderLayout.SOUTH);

        // Create the main panels
        JPanel ordersPanel = createOrdersPanel();
        JPanel supportPanel = createSupportPanel();
        deliveryPanel = createDeliveryPanel(); // Delivery details panel
        deliveryPanel.setVisible(false);

        // Add panels to the frame
        botPanel.add(ordersPanel, BorderLayout.CENTER);
        botPanel.add(supportPanel, BorderLayout.SOUTH);
        botPanel.add(deliveryPanel, BorderLayout.NORTH); // Place it at the top

        // Add top and bottom panels to the frame
        add(topPanel, BorderLayout.NORTH);
        add(botPanel, BorderLayout.CENTER);

        // Setup frame
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
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
        ordersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = ordersTable.rowAtPoint(e.getPoint());
                acceptOrderButton.setEnabled(selectedRow >= 0);
            }
        });

        JScrollPane scrollPane = new JScrollPane(ordersTable);

        JPanel actionsPanel = new JPanel();
        refreshOrders = new JButton("Refresh");
        acceptOrderButton = new JButton("Accept Order");
        acceptOrderButton.setEnabled(false);

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
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Current Delivery",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), Color.BLUE));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6); // Padding between components
        gbc.anchor = GridBagConstraints.WEST; // Align components to the left

        // Initialize the map to store labels
        deliveryLabels = new HashMap<>();

        // Define and add labels and values to the panel
        String[] labelNames = {
                "Order Number:", "Customer:", "Customer's Address:",
                "Restaurant:", "Total Cost:", "Status:"
        };
        String[] labelKeys = {
                "OrderID", "Customer", "Customer's Address",
                "Restaurant", "Total Cost", "Status"
        };

        for (int i = 0; i < labelNames.length; i++) {
            JLabel label = new JLabel(labelNames[i]);
            JLabel value = new JLabel("N/A");

            gbc.gridx = 0;
            gbc.gridy = i;
            panel.add(label, gbc);

            gbc.gridx = 1;
            panel.add(value, gbc);

            deliveryLabels.put(labelKeys[i], value);
        }

        // Add the Start/Finish Delivery Buttons
        startDeliveryButton = new JButton("Start Delivery");
        completeDeliveryButton = new JButton("Complete Delivery");

        gbc.gridx = 0;
        gbc.gridy = labelNames.length;
        gbc.gridwidth = 1;
        panel.add(startDeliveryButton, gbc);

        gbc.gridx = 1;
        panel.add(completeDeliveryButton, gbc);
        completeDeliveryButton.setEnabled(false); // Initially disabled

        // Add button actions
        startDeliveryButton.addActionListener(e -> {
            // Implement start delivery logic
            deliveryLabels.get("Status").setText("In Progress");
            startDeliveryButton.setEnabled(false);
            completeDeliveryButton.setEnabled(true);
            JOptionPane.showMessageDialog(DriverHomePage.this, "Delivery started!");
        });

        completeDeliveryButton.addActionListener(e -> {
            // Implement complete delivery logic
            deliveryLabels.get("Status").setText("Completed");
            startDeliveryButton.setEnabled(true);
            completeDeliveryButton.setEnabled(false);
            panel.setVisible(false); // Hide panel after completion
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