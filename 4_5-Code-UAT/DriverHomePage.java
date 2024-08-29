import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class DriverHomePage extends JFrame {
    private JTable ordersTable;
    private DefaultTableModel ordersTableModel;
    private JButton refreshOrders, acceptOrderButton, startDeliveryButton, completeDeliveryButton, aboutButton, logOutButton;
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

        JPanel[] panels = {botPanel, ordersPanel, supportPanel, deliveryPanel};
        for(JPanel panel : panels) {
            panel.setBackground(new Color(0x575658));
        }

        // Setup frame
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createOrdersPanel() {
        JPanel ordersPanel = new JPanel(new BorderLayout());
        TitledBorder border = BorderFactory.createTitledBorder("Available Orders");
        border.setTitleColor(Color.WHITE);
        ordersPanel.setBorder(border);
        ordersPanel.setBackground(new Color(0x575658));

        ordersTableModel = new DefaultTableModel(new Object[]{"Order Number", "Customer", "Customer's Address", "Restaurant", "Total Cost", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        ordersTable = new JTable(ordersTableModel);
        // Set background color for the table and viewport
        ordersTable.setBackground(new Color(0x575658));
        ordersTable.setForeground(Color.WHITE);  // Set text color to white for readability
        ordersTable.setSelectionBackground(new Color(0x2e2e2e));  // Change selection background color
        ordersTable.setSelectionForeground(Color.WHITE);  // Change selection text color

        ordersTable.getTableHeader().setReorderingAllowed(false); // don't allow the table to be reordered
        ordersTable.getTableHeader().setResizingAllowed(false);   // don't allow the table to be resizeable
        ordersTable.getTableHeader().setBackground(new Color(0x2e2e2e)); // Set header background color
        ordersTable.getTableHeader().setForeground(Color.WHITE);  // Set header text color to white

        ordersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = ordersTable.rowAtPoint(e.getPoint());
                acceptOrderButton.setEnabled(selectedRow >= 0);
                if(e.getClickCount() == 2 && selectedRow >= 0) {
                    int orderId = (int) ordersTable.getValueAt(selectedRow, 0);
                    viewOrderItems(orderId);
                }
            }
        });

        ordersTable.getTableHeader().setReorderingAllowed(false); // don't allow the table to be reordered
        ordersTable.getTableHeader().setResizingAllowed(false);   // don't allow the table to be resizeable

        JScrollPane scrollPane = new JScrollPane(ordersTable);
        scrollPane.getViewport().setBackground(new Color(0x575658));

        JPanel actionsPanel = new JPanel();
        actionsPanel.setBackground(new Color(0x575658));
        refreshOrders = new JButton("Refresh");
        acceptOrderButton = new JButton("Accept Order");
        aboutButton = new JButton("Account Info");
        logOutButton = new JButton("Log Out");
        acceptOrderButton.setEnabled(false);

        JButton[] buttons = {refreshOrders, acceptOrderButton, aboutButton, logOutButton};
        for(JButton button : buttons) {
            button.setBackground(Color.WHITE);
            button.setForeground(Color.BLACK);
        }

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
        aboutButton.addActionListener(e -> {
            new AboutInfo(username, "Driver");
        });

        return ordersPanel;
    }

    private void populateOrders() {
        // Query to get current orders for the driver
        String query = "SELECT O.ID, C.USERNAME AS Customer, C.ADDRESS as Address, R.NAME AS Restaurant, O.TOTAL_PRICE, O.STATUS " +
                       "FROM Orders O " +
                       "JOIN Customer C ON O.CUSTOMER_ID = C.ID " +
                       "JOIN Restaurant R ON O.RESTAURANT_ID = R.ID " +
                       "WHERE (O.DRIVER_ID IS NULL OR O.DRIVER_ID = ?) " +
                       "AND O.STATUS = 'Accepted by Restaurant'";

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

    private void viewOrderItems(int orderId) {
        String cart = "";
        String getItems = "SELECT ITEMS as Items FROM ORDERS WHERE ID = ?";
        CredentialsHandler cHandler = new CredentialsHandler();

        try(PreparedStatement getItemsStmt = cHandler.getDBConnection().prepareStatement(getItems)) {
            getItemsStmt.setInt(1, orderId);
            ResultSet rs = getItemsStmt.executeQuery();

            while(rs.next()) {
                cart = rs.getString("Items");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        JOptionPane.showMessageDialog(this, cart, "Order's Items", JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel createSupportPanel() {
        JPanel supportPanel = new JPanel();

        supportPanel.add(aboutButton);
        supportPanel.add(logOutButton);

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
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), Color.WHITE));

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
            label.setForeground(Color.WHITE);
            JLabel value = new JLabel("N/A");
            value.setForeground(Color.WHITE);

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

        JButton[] buttons = {startDeliveryButton, completeDeliveryButton};
        for(JButton button : buttons) {
            button.setBackground(Color.WHITE);
            button.setForeground(Color.BLACK);
        }

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
            startDelivery();
        });

        completeDeliveryButton.addActionListener(e -> {
            // Implement complete delivery logic
            completeDelivery();
            panel.setVisible(false);
        });

        return panel;
    }

    private void startDelivery() {
        String startDeliveryQuery = "UPDATE Orders SET STATUS = 'On the way' WHERE ID = ?";
        CredentialsHandler cHandler = new CredentialsHandler();
        try(PreparedStatement stmt = cHandler.getDBConnection().prepareStatement(startDeliveryQuery)) {
            stmt.setInt(1, orderId);
            int rows = stmt.executeUpdate();
            if(rows > 0) {
                deliveryLabels.get("Status").setText("In Progress");
                startDeliveryButton.setEnabled(false);
                completeDeliveryButton.setEnabled(true);
                JOptionPane.showMessageDialog(DriverHomePage.this, "Delivery Started!");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void completeDelivery() {
        String startDeliveryQuery = "UPDATE Orders SET STATUS = 'Completed' WHERE ID = ?";
        CredentialsHandler cHandler = new CredentialsHandler();
        try(PreparedStatement stmt = cHandler.getDBConnection().prepareStatement(startDeliveryQuery)) {
            stmt.setInt(1, orderId);
            int rows = stmt.executeUpdate();
            if(rows > 0) {
                deliveryLabels.get("Status").setText("Completed");
                startDeliveryButton.setEnabled(true);
                completeDeliveryButton.setEnabled(false);
                JOptionPane.showMessageDialog(DriverHomePage.this, "Delivery Completed!");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
            String updateOrderQuery = "UPDATE Orders SET DRIVER_ID = ?, STATUS = 'Accepted by Driver' WHERE ID = ?";
            PreparedStatement pstmt = dbConnection.prepareStatement(updateOrderQuery);
            pstmt.setInt(1, getDriverId());
            pstmt.setInt(2, orderId);
            pstmt.executeUpdate();
            populateOrders();
            deliveryPanel.setVisible(true);
            deliveryLabels.get("Status").setText("Awaiting Pick Up");
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
}