import javax.security.auth.login.CredentialException;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class RestaurantHomePage extends JFrame implements ActionListener {

    private final String username;
    private JButton refreshButton, menuButton, infoButton, LogoutButton;
    private JPanel topPanel, botPanel, buttonPanel;
    private OrderTableModel orderTableModel;
    private JTable ordersTable;

    public RestaurantHomePage(String username) {
        this.username = username;
        orderTableModel = new OrderTableModel();
        ordersTable = new JTable(orderTableModel);
        initFrame();

        // Retrieve the restaurant name for the given username
        String restaurantName = getRestaurantName(username);
        if (restaurantName != null) {
            loadOrders(restaurantName); // Load orders based on the restaurant name
        } else {
            System.err.println("Restaurant name could not be found for username: " + username);
        }
    }

    private void initFrame() {
        setTitle("Restaurant Home Page");
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

        // Initialize Buttons
        refreshButton = new JButton("Refresh");
        menuButton = new JButton("Menu");
        infoButton = new JButton("Account Info");
        LogoutButton = new JButton("Log Out");

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.setBackground(new Color(0x575658));

        JButton[] buttons = {refreshButton, menuButton, infoButton, LogoutButton};
        for (JButton button : buttons) {
            button.setBackground(Color.WHITE);
            button.setForeground(Color.BLACK);
            button.setFocusable(false);
            button.addActionListener(this);
            buttonPanel.add(button);
        }
        botPanel.add(buttonPanel, BorderLayout.NORTH);

        // Configure JTable
        ordersTable.setFont(new Font("Arial", Font.PLAIN, 14));
        ordersTable.setBackground(new Color(0x575658));
        ordersTable.setForeground(Color.WHITE);
        ordersTable.setSelectionBackground(new Color(0x4a4a4a));
        ordersTable.setSelectionForeground(Color.WHITE);
        ordersTable.getTableHeader().setReorderingAllowed(false); // dont allow the table to be reordered

        ordersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    int row = ordersTable.rowAtPoint(e.getPoint());
                    if(row >= 0) {
                        viewOrderItems();
                    }
                }
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(ordersTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScrollPane.setPreferredSize(new Dimension(450, 300));
        tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(0x575658)));
        tableScrollPane.getViewport().setBackground(new Color(0x575658));

        // Add the scroll pane to the center of botPanel
        botPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Add top and bottom panels to the frame
        add(topPanel, BorderLayout.NORTH);
        add(botPanel, BorderLayout.CENTER);

        // Frame settings
        setSize(800, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void viewOrderItems() {
        String cart = "";
        int orderId = 0;

        String restaurantName = getRestaurantName(username);

        // Retrieve the items of the order
        String retrieveOrders = "SELECT O.ID as OrderId, O.ITEMS as Items " +
                                "FROM ORDERS O " +
                                "JOIN RESTAURANT R ON O.RESTAURANT_ID = R.ID " +
                                "JOIN USER U ON O.CUSTOMER_ID = U.ID " +
                                "WHERE R.NAME = ? " +
                                "AND O.STATUS = 'Awaiting Confirmation' OR O.STATUS = 'Accepted by Restaurant'";
        CredentialsHandler cHandler = new CredentialsHandler();
        try (PreparedStatement pstmt = cHandler.getDBConnection().prepareStatement(retrieveOrders)) {
            pstmt.setString(1, restaurantName);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                orderId = rs.getInt("OrderId");
                cart = rs.getString("Items");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        int result = JOptionPane.showOptionDialog(
                this, // parent component
                cart, // message
                "Order Details", // title
                JOptionPane.YES_NO_OPTION, // type of options
                JOptionPane.INFORMATION_MESSAGE, // type of message
                null,
                new Object[]{"Accept Order", "Decline Order"}, // options
                "" // initial value
        );

        if (result == JOptionPane.YES_OPTION) {
            // Implement accept order logic
            acceptOrder(orderId);
            loadOrders(restaurantName);
            JOptionPane.showMessageDialog(this, "Order Accepted");
        } else if(result == JOptionPane.NO_OPTION) {
            declineOrder(orderId);
            loadOrders(restaurantName);
            JOptionPane.showMessageDialog(this, "Order Declined");
        }
    }

    private void acceptOrder(int orderId) {
        CredentialsHandler cHandler = new CredentialsHandler();
        String setDeclineStatus = "UPDATE Orders SET STATUS = 'Accepted by Restaurant' WHERE ID = ? ";
        try(PreparedStatement declineStatement = cHandler.getDBConnection().prepareStatement(setDeclineStatus)) {
            declineStatement.setInt(1, orderId);
            declineStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void declineOrder(int orderId) {
        CredentialsHandler cHandler = new CredentialsHandler();
        String setDeclineStatus = "UPDATE Orders SET STATUS = 'Declined by Restaurant' WHERE ID = ? ";
        try(PreparedStatement declineStatement = cHandler.getDBConnection().prepareStatement(setDeclineStatus)) {
            declineStatement.setInt(1, orderId);
            declineStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getRestaurantName(String username) {
        String restaurantName = null;
        String query = "SELECT R.NAME FROM RESTAURANT R WHERE USERNAME = ?";

        CredentialsHandler cHandler = new CredentialsHandler();
        try (PreparedStatement pstmt = cHandler.getDBConnection().prepareStatement(query)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    restaurantName = rs.getString("NAME");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return restaurantName;
    }

    private void loadOrders(String restaurantName) {
        String retrieveOrders = "SELECT O.ID as OrderId," +
                                "       U.USERNAME as CustomerName," +
                                "       O.QUANTITY as Quantity," +
                                "       O.TOTAL_PRICE as TotalCost," +
                                "       O.STATUS as Status " +
                                "FROM ORDERS O " +
                                "JOIN RESTAURANT R ON O.RESTAURANT_ID = R.ID " +
                                "JOIN USER U ON O.CUSTOMER_ID = U.ID " +
                                "WHERE R.NAME = ? ";

        CredentialsHandler cHandler = new CredentialsHandler();
        try (PreparedStatement pstmt = cHandler.getDBConnection().prepareStatement(retrieveOrders)) {
            pstmt.setString(1, restaurantName); // Set the restaurant name in the query
            try (ResultSet rs = pstmt.executeQuery()) {
                orderTableModel.clearOrders(); // Clear previous orders
                while (rs.next()) {
                    int orderId = rs.getInt("OrderId");
                    String customerName = rs.getString("CustomerName");
                    int quantity = rs.getInt("Quantity");
                    float totalCost = rs.getFloat("TotalCost");
                    String status = rs.getString("Status");

                    Order order = new Order(orderId, customerName, quantity, totalCost, status);
                    orderTableModel.addOrder(order);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == refreshButton) {
            String restaurantName = getRestaurantName(username);
            if(restaurantName != null)
                loadOrders(restaurantName);
            else System.err.println("Restaurant couldn't be found with the given name");
        }

        if(e.getSource() == menuButton) {
            // Implement Menu Logic
        }

        if (e.getSource() == infoButton) {
            new AboutInfoRES(username);
        }

        if (e.getSource() == LogoutButton) {
            dispose();
            new LogInForm();
        }
    }

    public String getUserName() {
        return username;
    }
}

class OrderTableModel extends AbstractTableModel {
    private final List<Order> orders;
    private final String[] columnNames = {"Order ID", "Customer Name", "Quantity", "Total Cost", "Status"};

    public OrderTableModel() {
        this.orders = new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        return orders.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Order order = orders.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return order.getOrderId();
            case 1:
                return order.getCustomerName();
            case 2:
                return order.getQuantity();
            case 3:
                return order.getTotalCost();
            case 4:
                return order.getStatus();
            default:
                return null;
        }
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    public void addOrder(Order order) {
        orders.add(order);
        fireTableRowsInserted(orders.size() - 1, orders.size() - 1);
    }

    public void clearOrders() {
        orders.clear();
    }
}

class Order {
    private final int orderId, quantity;
    private final String customerName, status;
    private final float totalCost;

    public Order(int orderId, String customerName, int quantity, float totalCost, String status) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.quantity = quantity;
        this.totalCost = totalCost;
        this.status = status;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getTotalCost() {
        return totalCost + " €";
    }

    public String getStatus() {
        return status;
    }
}

class AboutInfoRES extends JFrame {
    private String username;
    private JTextField passwordField, emailField, nameField, locationField, cuisineField, ratingField;

    public AboutInfoRES(String username) {
        this.username = username;
        initFrame();
        retrieveRestaurantInfo();
    }

    private void initFrame() {
        setTitle("Customer Information");
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(0xe7a780));
        AppLogo logo = new AppLogo();
        topPanel.add(logo.getLabel());

        JPanel botPanel = new JPanel(new GridBagLayout());
        botPanel.setBackground(new Color(0x575658));

        JLabel usernameLabel = new JLabel("Username: ");
        JLabel passwordLabel = new JLabel("Password: ");
        JLabel emailLabel = new JLabel("Email: ");
        JLabel nameLabel = new JLabel("Restaurant Name: ");
        JLabel locationLabel = new JLabel("Location: ");
        JLabel cuisineLabel = new JLabel("Cuisine: ");
        JLabel ratingLabel = new JLabel("Rating: ");

        JLabel[] labels = {usernameLabel, passwordLabel, emailLabel, nameLabel, locationLabel, cuisineLabel, ratingLabel};
        for (JLabel label : labels) {
            label.setForeground(Color.WHITE);
        }

        JTextField usernameField = new JTextField(20);
        usernameField.setText(username);
        passwordField = new JTextField(20);
        emailField = new JTextField(20);
        nameField = new JTextField(20);
        locationField = new JTextField(20);
        cuisineField = new JTextField(20);
        ratingField = new JTextField(20);

        JTextField[] fields = {usernameField, passwordField, emailField, nameField, locationField, cuisineField, ratingField};
        for (JTextField field : fields) {
            field.setEditable(false);
            field.setFocusable(false);
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        addToGrid(usernameLabel, usernameField, botPanel, gbc, 0);
        addToGrid(passwordLabel, passwordField, botPanel, gbc, 1);
        addToGrid(emailLabel, emailField, botPanel, gbc, 2);
        addToGrid(nameLabel, nameField, botPanel, gbc, 3);
        addToGrid(locationLabel, locationField, botPanel, gbc, 4);
        addToGrid(cuisineLabel, cuisineField, botPanel, gbc, 5);
        addToGrid(ratingLabel, ratingField, botPanel, gbc, 6);

        add(topPanel, BorderLayout.NORTH);
        add(botPanel, BorderLayout.CENTER);

        setSize(400, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void addToGrid(JLabel label, JTextField field, JPanel panel, GridBagConstraints gbc, int y) {
        for (int i = 0; i < 2; i++) {
            gbc.gridx = i;
            gbc.gridy = y;
            gbc.anchor = (i == 0) ? GridBagConstraints.EAST : GridBagConstraints.WEST;
            panel.add((i == 0) ? label : field, gbc);
        }
    }

    private void retrieveRestaurantInfo() {
        try {
            CredentialsHandler cHandler = new CredentialsHandler();
            String query = "SELECT * FROM Restaurant WHERE USERNAME = ?";
            PreparedStatement pstmt = cHandler.getDBConnection().prepareStatement(query);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String passwd = rs.getString("PASSWORD");
                String name = rs.getString("NAME");
                String location = rs.getString("LOCATION");
                String cuisine = rs.getString("CUISINE_TYPE");
                String rating = rs.getString("RATING");

                passwordField.setText(passwd);
                nameField.setText(name);
                locationField.setText(location);
                cuisineField.setText(cuisine);
                ratingField.setText(rating);
            }

            query = "SELECT EMAIL, USER_TYPE FROM User WHERE USERNAME = ?";
            pstmt = cHandler.getDBConnection().prepareStatement(query);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                String email = rs.getString("EMAIL");
                emailField.setText(email);
            }

            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
