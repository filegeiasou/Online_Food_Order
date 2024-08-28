import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import javax.swing.border.*;

public class CustomerHomePage extends JFrame implements ActionListener {

    private JButton viewOrdersButton, infoButton, LogoutButton;
    private String username;
    private JPanel restaurantPanel;
    JPanel topPanel, botPanel;

    public CustomerHomePage(String username) {
        this.username = username;
        initFrame();

        // Load restaurants from database
        loadRestaurants();
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

        // Create restaurant panel to hold rounded labels
        restaurantPanel = new JPanel();
        restaurantPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        restaurantPanel.setBackground(new Color(0x575658));

        JScrollPane scrollPane = new JScrollPane(restaurantPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        listPanel.setLayout(new BorderLayout());
        listPanel.setBackground(new Color(0x575658)); // Match background color
        listPanel.add(scrollPane, BorderLayout.CENTER);

        botPanel.add(listPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(botPanel, BorderLayout.CENTER);

        setSize(800, 700);
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

                // Create a rounded panel for each restaurant
                RoundedPanel restaurantLabel = new RoundedPanel(new GridLayout(0, 1));
                restaurantLabel.setPreferredSize(new Dimension(250, 80));
                restaurantLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

                JLabel nameLabel = new JLabel(restaurantName);
                nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
                nameLabel.setForeground(Color.WHITE);

                JLabel locationLabel = new JLabel("Location: " + location);
                locationLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                locationLabel.setForeground(Color.WHITE);

                JLabel cuisineLabel = new JLabel("Cuisine Type: " + cuisine);
                cuisineLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                cuisineLabel.setForeground(Color.WHITE);

                JLabel ratingLabel = new JLabel("Rating: " + rating);
                ratingLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                ratingLabel.setForeground(Color.WHITE);
                restaurantLabel.setBackground(new Color(0x7A7A7B));

                restaurantLabel.add(nameLabel);
                restaurantLabel.add(locationLabel);
                restaurantLabel.add(cuisineLabel);
                restaurantLabel.add(ratingLabel);

                restaurantLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 1) {
                            new RestaurantPage(restaurantName, username);
                        }
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        restaurantLabel.setBackground(new Color(0xe7a780)); // Highlight on hover
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        restaurantLabel.setBackground(new Color(0x7A7A7B)); // Default color on exit
                    }
                });

                restaurantPanel.add(restaurantLabel);
                restaurantPanel.add(Box.createRigidArea(new Dimension(0, 15))); // Add space between panels
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
            new AboutInfo(username,"Customer");
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

// Custom RoundedPanel class
class RoundedPanel extends JPanel {
    private int cornerRadius = 25;

    public RoundedPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
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

    private String[] retrieveMenuCategories(CredentialsHandler cHandler){
        try {
            String query = "SELECT DISTINCT CATEGORY FROM Menu WHERE RESTAURANT_ID = ?";
            PreparedStatement pstmt = cHandler.getDBConnection().prepareStatement(query);
            pstmt.setInt(1, restaurantID);
            ResultSet rs = pstmt.executeQuery();

            ArrayList<String> categories = new ArrayList<>();
            while (rs.next()) {
                categories.add(rs.getString("CATEGORY"));
            }

            pstmt.close();
            return categories.toArray(new String[0]);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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

            // Retrieve and add menu Categories
            String[] categories = retrieveMenuCategories(cHandler);
            for(String category:categories) {
                addMenuCategory(cHandler, category);
            }

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
        }

        int result = JOptionPane.showOptionDialog(
                this,             // parent component
                cartContent.toString(),          // message
                "Cart",                          // title
                JOptionPane.YES_NO_OPTION,       // type of options
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

            StringBuilder itemsBuilder = new StringBuilder();
            for(String item:cart) {
                if(!itemsBuilder.isEmpty()) {
                    itemsBuilder.append("\n");
                }
                itemsBuilder.append(item);
            }

            String items = itemsBuilder.toString();

            query = "INSERT INTO Orders(CUSTOMER_ID, RESTAURANT_ID, QUANTITY, ITEMS, TOTAL_PRICE, STATUS) VALUES (?, ?, ?, ?, ?, ?)";
            pstmt = cHandler.getDBConnection().prepareStatement(query);
            pstmt.setInt(1, customerID);
            pstmt.setInt(2, restaurantID);
            pstmt.setInt(3, cart.size());
            pstmt.setString(4, items);
            pstmt.setDouble(5, totalPrice);
            pstmt.setString(6, "Awaiting Confirmation");
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
        ordersContainer.setLayout(new GridBagLayout());
        ordersContainer.setBackground(new Color(0x575658));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5); // Add padding around components

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

        setSize(450, 450);
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

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 5); // Add padding around components

            int row = 0;
            while (rs.next()) {
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
                String items = rs.getString("ITEMS");

                int orderId = rs.getInt("ID");

                // Create and add the order panel to the orders container
                RoundedPanel orderPanel = createOrderPanel(orderId, restaurantName, quantity, totalPrice, status, items);
                gbc.gridx = 0;
                gbc.gridy = row++;
                ordersContainer.add(orderPanel, gbc);
            }

            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private RoundedPanel createOrderPanel(int orderId, String restaurantName, int quantity, double totalPrice, String status, String items) {
        RoundedPanel orderPanel = new RoundedPanel(new GridLayout(0, 1));
        orderPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        orderPanel.setLayout(new BoxLayout(orderPanel, BoxLayout.Y_AXIS));
        orderPanel.setBackground(new Color(0x7A7A7B));
        orderPanel.setPreferredSize(new Dimension(400, 100));

        orderPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(null, items, "Order's Items", JOptionPane.INFORMATION_MESSAGE);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                orderPanel.setBackground(new Color(0xe7a780));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                orderPanel.setBackground(new Color(0x7A7A7B));
            }
        });

        JLabel orderCountLabel = new JLabel("Order ID: " + orderId);
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