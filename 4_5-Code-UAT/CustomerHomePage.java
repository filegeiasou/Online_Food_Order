import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import javax.swing.border.*;

public class CustomerHomePage extends JFrame implements ActionListener {

    private JButton viewOrdersButton, infoButton, LogoutButton;
    private String username;
    private JList<String> restaurantList;
    private DefaultListModel<String> listModel;
    JPanel topPanel, botPanel;

    public CustomerHomePage(String username) {

        this.username = username;
        listModel = new DefaultListModel<>();
        restaurantList = new JList<>(listModel);
        initFrame();

        // Load restaurants from database
        loadRestaurants();

        // Open restaurant page on double click
        restaurantList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Double-click detected
                    int index = restaurantList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        String selectedRestaurant = restaurantList.getModel().getElementAt(index);
                        String restaurantName = selectedRestaurant.split(" ")[0];
                        new RestaurantPage(restaurantName, username);
                    }
                }
            }
        });
    }

    private void initFrame() {
        setTitle("Customer Home Page");
        setLayout(new BorderLayout());

        topPanel = new JPanel();
        botPanel = new JPanel();
        JPanel buttonPanel = new JPanel();
        JPanel listPanel = new JPanel(); // Panel to hold the label and scroll pane together

        JLabel welcomeLabel = new JLabel("Welcome, " + username + "!");

        // Buttons
        viewOrdersButton = new JButton("View Orders");
        viewOrdersButton.setBackground(Color.WHITE);
        viewOrdersButton.setForeground(Color.BLACK);
        viewOrdersButton.setFocusable(false);
        viewOrdersButton.addActionListener(this);

        infoButton = new JButton("Account Info");
        infoButton.setBackground(Color.WHITE);
        infoButton.setForeground(Color.BLACK);
        infoButton.setFocusable(false);
        infoButton.addActionListener(this);

        LogoutButton = new JButton("Logout");
        LogoutButton.setBackground(Color.WHITE);
        LogoutButton.setForeground(Color.BLACK);
        LogoutButton.setFocusable(false);
        LogoutButton.addActionListener(this);

        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 25));
        welcomeLabel.setForeground(Color.WHITE);

        // Logo
        AppLogo logo = new AppLogo();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(logo.getLabel(), BorderLayout.CENTER);
        topPanel.add(welcomeLabel, BorderLayout.SOUTH);
        topPanel.setBackground(new Color(0xe7a780));

        botPanel.setLayout(new BorderLayout());
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.setBackground(new Color(0x575658));
        buttonPanel.add(viewOrdersButton);
        buttonPanel.add(infoButton);
        buttonPanel.add(LogoutButton);

        botPanel.add(buttonPanel, BorderLayout.NORTH);
        botPanel.setBackground(new Color(0x575658));

        // Available Restaurants Label
        JLabel availableRestaurantsLabel = new JLabel("Available Restaurants");
        availableRestaurantsLabel.setFont(new Font("Arial", Font.BOLD, 18));
        availableRestaurantsLabel.setForeground(new Color(0xe7a780));
        availableRestaurantsLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Restaurant List
        restaurantList.setFont(new Font("Arial", Font.PLAIN, 18));
        restaurantList.setBackground(new Color(0x575658));
        restaurantList.setForeground(Color.WHITE);

        JScrollPane listScrollPane = new JScrollPane(restaurantList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        listScrollPane.setPreferredSize(new Dimension(450, 300));
        listScrollPane.setBorder(BorderFactory.createLineBorder(new Color(0x575658)));
        listScrollPane.setBackground(new Color(0x575658));
        listScrollPane.getViewport().setBackground(new Color(0x575658));

        listPanel.setLayout(new BorderLayout());
        listPanel.setBackground(new Color(0x575658)); // Match background color
        listPanel.add(availableRestaurantsLabel, BorderLayout.NORTH);
        listPanel.add(listScrollPane, BorderLayout.CENTER);

        botPanel.add(listPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(botPanel, BorderLayout.CENTER);

        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadRestaurants() {
        try {
            CredentialsHandler cHandler = new CredentialsHandler();
            String query = "SELECT * FROM Restaurant";
            Statement stmt = cHandler.getDBConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String restaurantName = rs.getString("NAME");
                String location = rs.getString("LOCATION");
                String cuisine = rs.getString("CUISINE_TYPE");
                int rating = rs.getInt("RATING");

                String restaurantInfo = restaurantName + " | " + location + " | " + cuisine + " | " + rating;

                listModel.addElement(restaurantInfo);
            }

            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == viewOrdersButton) {
            new OrdersPage(username);
        }
        if (e.getSource() == infoButton) {
            new AboutInfo(username);
        }
        if(e.getSource() == LogoutButton) {
            dispose();
            new LogInForm();
        }
    }

    public String getUserName() {
        return username;
    }
}

class AboutInfo extends JFrame {
    JTextField passwordField, emailField, addressField, userTypeField;
    private String username;

    public AboutInfo(String username) {
        this.username = username;
        initFrame();
        retrieveCustomerInfo();
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

        JLabel usernameLabel = new JLabel("Username: "); usernameLabel.setForeground(Color.WHITE);
        JLabel passwordLabel = new JLabel("Password: "); passwordLabel.setForeground(Color.WHITE);
        JLabel emailLabel = new JLabel("Email: "); emailLabel.setForeground(Color.WHITE);
        JLabel addressLabel = new JLabel("Address: "); addressLabel.setForeground(Color.WHITE);

        JTextField usernameField = new JTextField(20); usernameField.setEditable(false); usernameField.setFocusable(false);
        usernameField.setText(username);
        passwordField = new JTextField(20); passwordField.setEditable(false); passwordField.setFocusable(false);
        addressField = new JTextField(20); addressField.setEditable(false); addressField.setFocusable(false);
        emailField = new JTextField(20);  emailField.setEditable(false); emailField.setFocusable(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        addToGrid(usernameLabel, usernameField, botPanel, gbc, 0);
        addToGrid(passwordLabel, passwordField, botPanel, gbc, 1);
        addToGrid(emailLabel, emailField, botPanel, gbc, 2);
        addToGrid(addressLabel, addressField, botPanel, gbc, 3);

        add(topPanel, BorderLayout.NORTH);
        add(botPanel, BorderLayout.CENTER);

        setSize(350, 350);
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

    private void retrieveCustomerInfo() {
        try{
            CredentialsHandler cHandler = new CredentialsHandler();
            String query = "SELECT PASSWORD, ADDRESS FROM Customer WHERE USERNAME = ?";
            PreparedStatement pstmt = cHandler.getDBConnection().prepareStatement(query);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String passwd = rs.getString("PASSWORD");
                String address = rs.getString("ADDRESS");

                passwordField.setText(passwd);
                addressField.setText(address);
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

class RestaurantPage extends JFrame {
    String restaurantName, customerUsername;
    int restaurantID;
    JPanel menuPanel;  // Panel to display menu items
    ArrayList<JCheckBox> checkBoxes; // List to keep track of checkboxes
    ArrayList<String> cart; // List to store selected items
    double totalPrice = 0;
    JButton addToCartButton, viewCartButton; // JButton is declared here in order to be accessed from other methods

    public RestaurantPage(String restaurantName, String customerUsername) {
        // Initialize lists
        checkBoxes = new ArrayList<>();
        cart = new ArrayList<>();
        // Retrieve Menu Items
        this.restaurantName = restaurantName;
        this.customerUsername = customerUsername;
        initFrame();
        retrieveMenuItems();
    }

    private void initFrame(){
        setTitle(restaurantName);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(0xe7a780));
        AppLogo logo = new AppLogo();
        topPanel.add(logo.getLabel());

        JPanel botPanel = new JPanel(new BorderLayout());
        botPanel.setBackground(new Color(0x575658));

        JLabel infoLabel = new JLabel("Welcome to " + restaurantName);
        infoLabel.setFont(new Font("Arial", Font.BOLD, 18));
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoLabel.setForeground(Color.WHITE);
        botPanel.add(infoLabel, BorderLayout.NORTH);

        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(0x575658));  // Match the background color

        JScrollPane scrollPane = new JScrollPane(menuPanel);
        botPanel.add(scrollPane, BorderLayout.CENTER);

        // Add "Add to Cart" button
//        addToCartButton = new JButton("Add to Cart");
//        addToCartButton.addActionListener(e -> addToCart());
//        addToCartButton.setEnabled(false); // start with the button locked. This will update if the user selected an item
//        botPanel.add(addToCartButton, BorderLayout.SOUTH);

        viewCartButton = new JButton("View Cart");
        viewCartButton.setEnabled(false);
        viewCartButton.addActionListener(e -> showCartItems());
        botPanel.add(viewCartButton, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(botPanel, BorderLayout.CENTER);

        setSize(500, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void retrieveMenuItems() {
        try {
            CredentialsHandler cHandler = new CredentialsHandler();
            String query = "SELECT ID FROM Restaurant WHERE NAME = ?";
            PreparedStatement pstmt = cHandler.getDBConnection().prepareStatement(query);
            pstmt.setString(1, restaurantName);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                restaurantID = rs.getInt("ID");
            }

            // Retrieve the menu categories
            addMenuCategory(cHandler, "Appetizer");
            addMenuCategory(cHandler, "Main");
            addMenuCategory(cHandler, "Dessert");
            addMenuCategory(cHandler, "Drinks");

            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addMenuCategory(CredentialsHandler cHandler, String category) {
        try {
            String query = "SELECT NAME, PRICE FROM Menu WHERE RESTAURANT_ID = ? AND CATEGORY = ?";
            PreparedStatement pstmt = cHandler.getDBConnection().prepareStatement(query);
            pstmt.setInt(1, restaurantID);
            pstmt.setString(2, category);
            ResultSet rs = pstmt.executeQuery();

            boolean hasItems = false;

            JPanel categoryPanel = new JPanel();
            categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.Y_AXIS));
            categoryPanel.setBackground(new Color(0x575658)); 
            TitledBorder border = BorderFactory.createTitledBorder(category);
            border.setTitleColor(Color.WHITE);
            categoryPanel.setBorder(border);

            while (rs.next()) {
                hasItems = true;
                String itemName = rs.getString("NAME");
                double itemPrice = rs.getDouble("PRICE");
                addMenuItem(categoryPanel, itemName, itemPrice, category);
            }

            if(hasItems) {
                menuPanel.add(categoryPanel);
                menuPanel.add(Box.createRigidArea(new Dimension(0, 10))); 
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addMenuItem(JPanel categoryPanel, String name, double price, String category) {
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setBackground(new Color(0x575658));
        itemPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JCheckBox checkBox = new JCheckBox(name + " - €" + String.format("%.2f", price));
        checkBox.setBackground(new Color(0x575658));
        checkBox.setForeground(Color.WHITE);
        checkBox.setFont(new Font("Arial", Font.BOLD, 14));

        checkBox.addItemListener(e -> addToCart());

        checkBoxes.add(checkBox);

        itemPanel.add(checkBox, BorderLayout.CENTER);
        categoryPanel.add(itemPanel);
    }

    private void addToCart() {
        cart.clear();
        totalPrice = 0;
        for (JCheckBox checkBox : checkBoxes) {
            if (checkBox.isSelected()) {
                String[] parts = checkBox.getText().split(" - €");
                cart.add(checkBox.getText());
                totalPrice += Double.parseDouble(parts[1].split(" ")[0]);
            }
        }

        viewCartButton.setEnabled(!cart.isEmpty());
    }

    private void showCartItems() {
        StringBuilder cartContent = null;
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your cart is empty.", "Cart", JOptionPane.INFORMATION_MESSAGE);
        } else {
            cartContent = new StringBuilder("Items in your cart:\n");
            for (String item : cart) {
                cartContent.append(item).append("\n");
            }
//            JOptionPane.showMessageDialog(this, cartContent.toString(), "Cart", JOptionPane.INFORMATION_MESSAGE);
        }

        int result = JOptionPane.showOptionDialog(
                this, // parent component
                cartContent.toString(), // message
                "Cart", // title
                JOptionPane.YES_NO_OPTION, // type of options
                JOptionPane.INFORMATION_MESSAGE, // type of message
                null,
                new Object[]{"Finish Order", "Add More Items"}, // options
                "Add More Items" // initial value
        );

        if (result == JOptionPane.YES_OPTION) {
            finishOrder();
        }
    }

    private void finishOrder() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your cart is empty. Please add items before finishing the order.", "Empty Cart", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // add the order to the database
        addOrderToDB();

        // show a confirmation message
        JOptionPane.showMessageDialog(this, "Your order has been placed successfully!", "Order Confirmation", JOptionPane.INFORMATION_MESSAGE);

        cart.clear(); // clear cart
        totalPrice = 0; // reset price

        dispose();
    }

    private void addOrderToDB() {
        try {
            CredentialsHandler cHandler = new CredentialsHandler();

            String query = "SELECT ID FROM CUSTOMER WHERE USERNAME = ?";
            PreparedStatement pstmt = cHandler.getDBConnection().prepareStatement(query);
            pstmt.setString(1, customerUsername);
            ResultSet rs = pstmt.executeQuery();
            int customerID = -1;
            while(rs.next()) {
                customerID = rs.getInt("ID");
            }

            query = "INSERT INTO Orders(CUSTOMER_ID, RESTAURANT_ID, QUANTITY, TOTAL_PRICE, STATUS) VALUES (?, ?, ?, ?, ?)";
            pstmt = cHandler.getDBConnection().prepareStatement(query);
            pstmt.setInt(1, customerID);
            pstmt.setInt(2, restaurantID);
            pstmt.setInt(3, cart.size());
            pstmt.setDouble(4, totalPrice);
            pstmt.setString(5, "Awaiting Confirmation");
            pstmt.executeUpdate();

            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

class OrdersPage extends JFrame {
    String customerUsername;
    JPanel ordersContainer;

    public OrdersPage(String username) {
        this.customerUsername = username;
        initFrame();
        retrieveOrders();
    }

    private void initFrame(){
        setTitle("Orders");
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(0xe7a780));
        AppLogo logo = new AppLogo();
        topPanel.add(logo.getLabel());

        ordersContainer = new JPanel();
        ordersContainer.setLayout(new GridLayout(0, 1, 10, 10));
        ordersContainer.setBackground(new Color(0x575658));

        JScrollPane scrollPane = new JScrollPane(ordersContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel botPanel = new JPanel(new BorderLayout());
        botPanel.setBackground(new Color(0x575658));
        JLabel infoLabel = new JLabel("Orders for " + customerUsername);
        infoLabel.setFont(new Font("Arial", Font.BOLD, 18));
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoLabel.setForeground(Color.WHITE);
        botPanel.add(infoLabel, BorderLayout.NORTH);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(botPanel, BorderLayout.SOUTH);

        setSize(500, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void retrieveOrders() {
        try {
            CredentialsHandler cHandler = new CredentialsHandler();

            String query = "SELECT ID FROM CUSTOMER WHERE USERNAME = ?";
            PreparedStatement pstmt = cHandler.getDBConnection().prepareStatement(query);
            pstmt.setString(1, customerUsername);
            ResultSet rs = pstmt.executeQuery();
            int customerID = -1;
            if (rs.next()) {
                customerID = rs.getInt("ID");
            }

            // Now retrieve the orders associated with the customer ID
            query = "SELECT * FROM Orders WHERE CUSTOMER_ID = ?";
            pstmt = cHandler.getDBConnection().prepareStatement(query);
            pstmt.setInt(1, customerID);
            rs = pstmt.executeQuery();

            int orderCount = 0;

            while (rs.next()) {
                orderCount++;
                int restaurantID = rs.getInt("RESTAURANT_ID");

                // Retrieve the restaurant name
                pstmt = cHandler.getDBConnection().prepareStatement("SELECT NAME FROM Restaurant WHERE ID = ?");
                pstmt.setInt(1, restaurantID);
                ResultSet restaurantRS = pstmt.executeQuery();
                String restaurantName = "";
                if (restaurantRS.next()) {
                    restaurantName = restaurantRS.getString("NAME");
                }

                int quantity = rs.getInt("QUANTITY");
                double totalPrice = rs.getDouble("TOTAL_PRICE");
                String status = rs.getString("STATUS");

                // Create and add the order panel to the orders container
                JPanel orderPanel = createOrderPanel(orderCount, restaurantName, quantity, totalPrice, status);
                ordersContainer.add(orderPanel);
            }

            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private JPanel createOrderPanel(int orderCount, String restaurantName, int quantity, double totalPrice, String status) {
        JPanel orderPanel = new JPanel();
        orderPanel.setLayout(new BoxLayout(orderPanel, BoxLayout.Y_AXIS));
        orderPanel.setBackground(new Color(0x424242));
        orderPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        orderPanel.setPreferredSize(new Dimension(150, 100));

        JLabel orderCountLabel = new JLabel("Order: " + orderCount);
        JLabel restaurantNameLabel = new JLabel("Restaurant: " + restaurantName);
        JLabel quantityLabel = new JLabel("Quantity: " + quantity);
        JLabel totalPriceLabel = new JLabel("Total Price: €" + String.format("%.2f", totalPrice));
        JLabel statusLabel = new JLabel("Status: " + status);

        orderCountLabel.setForeground(Color.WHITE);
        restaurantNameLabel.setForeground(Color.WHITE);
        quantityLabel.setForeground(Color.WHITE);
        totalPriceLabel.setForeground(Color.WHITE);
        statusLabel.setForeground(Color.WHITE);

        orderPanel.add(orderCountLabel);
        orderPanel.add(restaurantNameLabel);
        orderPanel.add(quantityLabel);
        orderPanel.add(totalPriceLabel);
        orderPanel.add(statusLabel);

        return orderPanel;
    }
}