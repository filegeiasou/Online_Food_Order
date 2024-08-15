import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class RestaurantHomePage extends JFrame implements ActionListener {

    private String username;
    private JButton menuButton, infoButton, LogoutButton;
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

        // Buttons
        menuButton = new JButton("Menu");
        infoButton = new JButton("Account Info");
        LogoutButton = new JButton("Logout");

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.setBackground(new Color(0x575658));

        JButton[] buttons = {menuButton, infoButton, LogoutButton, };
        for (JButton button : buttons) {
            button.setBackground(Color.WHITE);
            button.setForeground(Color.BLACK);
            button.setFocusable(false);
            button.addActionListener(this);
            buttonPanel.add(button);
        }
        botPanel.add(buttonPanel, BorderLayout.NORTH);     

        // Configure JTable
        ordersTable.setFont(new Font("Arial", Font.PLAIN, 18));
        ordersTable.setBackground(new Color(0x575658));
        ordersTable.setForeground(Color.WHITE);
        ordersTable.setSelectionBackground(new Color(0x4a4a4a));
        ordersTable.setSelectionForeground(Color.WHITE);

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
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private String getRestaurantName(String username) {
        String restaurantName = null;
        String query = "SELECT R.NAME " +
                "FROM RESTAURANT R " +
                "WHERE USERNAME = ?";
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
        String retrieveOrders = "SELECT O.ID as OrderId, U.USERNAME as CustomerName " +
                                "FROM ORDERS O " +
                                "JOIN RESTAURANT R ON O.RESTAURANT_ID = R.ID " +
                                "JOIN USER U ON O.CUSTOMER_ID = U.ID " +
                                "WHERE R.NAME = ? " +
                                "AND O.STATUS = 'Awaiting Confirmation'";

        CredentialsHandler cHandler = new CredentialsHandler();
        try (PreparedStatement pstmt = cHandler.getDBConnection().prepareStatement(retrieveOrders)) {
            pstmt.setString(1, restaurantName); // Set the restaurant name in the query
            try (ResultSet rs = pstmt.executeQuery()) {
                orderTableModel.clearOrders(); // Clear previous orders
                while (rs.next()) {
                    int orderId = rs.getInt("OrderId");
                    String customerName = rs.getString("CustomerName");

                    System.out.println("Order ID: " + orderId + ", Customer Name: " + customerName);
                    Order order = new Order(orderId, customerName);
                    orderTableModel.addOrder(order);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {
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
    private final String[] columnNames = {"Order ID", "Customer Name"};

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
        fireTableDataChanged();
    }
}

class Order {
    private final int orderId;
    private final String customerName;

    public Order(int orderId, String customerName) {
        this.orderId = orderId;
        this.customerName = customerName;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getCustomerName() {
        return customerName;
    }
}

class AboutInfoRES extends JFrame {
    private String username;
    private JTextField passwordField, emailField, nameField, locationField, cuisineField, ratingField;
    ;

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
