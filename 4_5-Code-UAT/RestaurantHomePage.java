import javax.swing.table.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.border.*;

public class RestaurantHomePage extends JFrame implements ActionListener {

    private final String username;
    private JButton refreshButton, menuButton, infoButton, logoutButton;
    private JPanel topPanel, botPanel, buttonPanel, bottomPanel;
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
        logoutButton = new JButton("Log Out");

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(0x575658));

        JButton[] buttons = {menuButton, infoButton, logoutButton};
        for (JButton button : buttons) {
            button.setBackground(Color.WHITE);
            button.setForeground(Color.BLACK);
            button.setFocusable(false);
            button.addActionListener(this);
            buttonPanel.add(button);
        }

        // Bottom panel with refresh button
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(new Color(0x575658));

        refreshButton.setBackground(Color.WHITE);
        refreshButton.setForeground(Color.BLACK);
        refreshButton.setFocusable(false);
        refreshButton.addActionListener(this);
        bottomPanel.add(refreshButton);

        // Configure JTable
        ordersTable.setFont(new Font("Arial", Font.PLAIN, 14));
        ordersTable.setBackground(new Color(0x575658));
        ordersTable.setForeground(Color.WHITE);
        ordersTable.setSelectionBackground(new Color(0x4a4a4a));
        ordersTable.setSelectionForeground(Color.WHITE);
        ordersTable.getTableHeader().setReorderingAllowed(false); // don't allow the table to be reordered
        ordersTable.getTableHeader().setResizingAllowed(false);   // don't allow the table to be resizable

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

        // Create a JPanel to hold the orders table with a titled border
        JPanel ordersTablePanel = new JPanel(new BorderLayout());
        ordersTablePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.WHITE), "Incoming Orders",
                TitledBorder.CENTER, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 18), Color.WHITE));
        ordersTablePanel.setBackground(new Color(0x575658)); // Set background to match botPanel
        ordersTablePanel.add(tableScrollPane, BorderLayout.CENTER);

        // Add the ordersTablePanel to the center of botPanel
        botPanel.add(ordersTablePanel, BorderLayout.CENTER);

        // Add the buttonPanel to the top of botPanel
        botPanel.add(buttonPanel, BorderLayout.NORTH);

        // Add top and bottom panels to the frame
        add(topPanel, BorderLayout.NORTH);
        add(botPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

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
            // Handle Accept Order logic
            acceptOrder(orderId);
            loadOrders(restaurantName);
            JOptionPane.showMessageDialog(this, "Order Accepted");
        } else if(result == JOptionPane.NO_OPTION) {
            // Handle Decline Order logic
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
                                "       C.USERNAME as CustomerName," +
                                "       O.QUANTITY as Quantity," +
                                "       O.TOTAL_PRICE as TotalCost," +
                                "       O.STATUS as Status " +
                                "FROM ORDERS O " +
                                "JOIN RESTAURANT R ON O.RESTAURANT_ID = R.ID " +
                                "JOIN CUSTOMER C ON O.CUSTOMER_ID = C.ID " +
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
            new MenuPage(username);
        }

        if (e.getSource() == infoButton) {
            new AboutInfo(username, "Restaurant");
        }

        if (e.getSource() == logoutButton) {
            dispose();
            new LogInForm();
        }
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

class MenuPage extends JFrame implements ActionListener {

    private String username;
    private JPanel botPanel, menuPanel;
    private JLabel selectedItemLabel;
    private JButton addMenuItemButton, deleteMenuItemButton;

    public MenuPage(String username) {
        this.username = username;
        initFrame();
        retrieveMenu();
    }

    private void initFrame() {
        setTitle("Menu Page");
        setLayout(new BorderLayout());

        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(new Color(0xe7a780));
        AppLogo logo = new AppLogo();
        logoPanel.add(logo.getLabel());

        botPanel = new JPanel(new BorderLayout());
        botPanel.setBackground(new Color(0x575658));

        addMenuItemButton = new JButton("Add Item");
        deleteMenuItemButton = new JButton("Delete Item");

        JButton[] buttons = {addMenuItemButton, deleteMenuItemButton};
        for (JButton button : buttons) {
            button.setBackground(Color.WHITE);
            button.setForeground(Color.BLACK);
            button.setFocusable(false);
            button.addActionListener(this);
        }

        deleteMenuItemButton.setEnabled(false);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(0x575658));
        buttonPanel.add(addMenuItemButton);
        buttonPanel.add(deleteMenuItemButton);

        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(0x575658));
        JScrollPane scrollPane = new JScrollPane(menuPanel);

        botPanel.add(scrollPane, BorderLayout.CENTER);
        botPanel.add(buttonPanel, BorderLayout.NORTH);

        add(logoPanel, BorderLayout.NORTH);
        add(botPanel, BorderLayout.CENTER);

        setSize(500, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void retrieveMenu() {
        try {
            CredentialsHandler cHandler = new CredentialsHandler();
            String query = "SELECT NAME, PRICE, CATEGORY FROM Menu WHERE RESTAURANT_ID = (SELECT ID FROM RESTAURANT WHERE USERNAME = ?)";
            PreparedStatement pstmt = cHandler.getDBConnection().prepareStatement(query);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            Map<String, List<String>> menuMap = new HashMap<>(); // Map to store the menu items by category

            while (rs.next()) {
                String itemName = rs.getString("NAME");
                String itemPrice = rs.getString("PRICE");
                String itemCategory = rs.getString("CATEGORY");

                menuMap.computeIfAbsent(itemCategory, k -> new ArrayList<>()).add(itemName + " - €" + itemPrice); // If the category is new, create a new list
            }

            pstmt.close();
            displayMenu(menuMap);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayMenu(Map<String, List<String>> menuMap) {
        menuPanel.setLayout(new GridLayout(0, 1)); 
    
        for (Map.Entry<String, List<String>> entry : menuMap.entrySet()) {
            String category = entry.getKey();
            List<String> items = entry.getValue();
    
            JPanel categoryPanel = new JPanel();
            categoryPanel.setLayout(new GridLayout(items.size(), 1));
            categoryPanel.setBackground(new Color(0x575658));
            TitledBorder border = BorderFactory.createTitledBorder(category);
            border.setTitleColor(Color.WHITE);
            categoryPanel.setBorder(border);
    
            for (String item : items) {
                JLabel itemLabel = new JLabel(item);
                itemLabel.setFont(new Font("Arial", Font.BOLD, 14));
                itemLabel.setForeground(Color.WHITE);
                itemLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (selectedItemLabel != null) {
                            selectedItemLabel.setForeground(Color.WHITE); // Reset previous selection
                        }
                        selectedItemLabel = itemLabel;
                        selectedItemLabel.setForeground(new Color(0xe7a780)); // Highlight selected item
                        deleteMenuItemButton.setEnabled(true); // Enable Delete button
                        if (e.getClickCount() == 2) {
                            editMenuItem();
                        }
                    }
                });
    
                itemLabel.addMouseMotionListener(new MouseMotionAdapter() {
                    @Override
                    public void mouseMoved(MouseEvent e) {
                        if (itemLabel != selectedItemLabel) {
                            itemLabel.setForeground(new Color(0xF3B99B)); // Highlight on hover
                        }
                    }
                });
    
                itemLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (itemLabel != selectedItemLabel) {
                            itemLabel.setForeground(Color.WHITE); // Reset color when not hovering
                        }
                    }
                });
    
                categoryPanel.add(itemLabel);
            }
    
            menuPanel.add(categoryPanel);
        }
    
        menuPanel.revalidate();
        menuPanel.repaint();
    }    

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Add Item")) {
            addMenuItem();
        }

        if (e.getActionCommand().equals("Delete Item")) {
            deleteItem();
        }
         // Refresh the menu
        menuPanel.removeAll();
        retrieveMenu();
        menuPanel.revalidate();
    }

    private void addMenuItem() {
        String itemName = JOptionPane.showInputDialog(this, "Enter the name of the item: ");
        double itemPrice = Double.parseDouble(JOptionPane.showInputDialog(this, "Enter the price of the item: "));
        String itemCategory = JOptionPane.showInputDialog(this, "Enter the category of the item: ");

        try {
            CredentialsHandler cHandler = new CredentialsHandler();
            String query = "INSERT INTO Menu (NAME, PRICE, CATEGORY, RESTAURANT_ID) VALUES (?, ?, ?, (SELECT ID FROM RESTAURANT WHERE USERNAME = ?))";
            PreparedStatement pstmt = cHandler.getDBConnection().prepareStatement(query);
            pstmt.setString(1, itemName);
            pstmt.setDouble(2, itemPrice);
            pstmt.setString(3, itemCategory);
            pstmt.setString(4, username);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void editMenuItem() {
        if (selectedItemLabel == null) return;
        String itemDetails = selectedItemLabel.getText();
        String itemName = itemDetails.split(" - ")[0];
        String itemPrice = itemDetails.split(" - ")[1].replace("€", "").trim();
        String itemCategory = "";

        CredentialsHandler cHandler = new CredentialsHandler();

        try {
            String query = "SELECT M.CATEGORY as Category " +
                           "FROM MENU M " +
                           "JOIN RESTAURANT R ON R.ID = M.RESTAURANT_ID " +
                           "WHERE M.NAME = ? ";
            PreparedStatement getCategory = cHandler.getDBConnection().prepareStatement(query);
            getCategory.setString(1, itemName);
            ResultSet rs = getCategory.executeQuery();
            if(rs.next()) {
                itemCategory = rs.getString("Category");
            }

            getCategory.close();
        } catch (SQLException sqe) {
            sqe.printStackTrace();
        }

        // Create and show the dialog
        EditItemDialog editDialog = new EditItemDialog(this, itemName, itemPrice, itemCategory);
        editDialog.setVisible(true);

        if (editDialog.isConfirmed()) {
            String newName = editDialog.getItemName();
            double newPrice = editDialog.getItemPrice();
            String newCategory = editDialog.getItemCategory();

            if(editDialog.validateInput()) {
                try {
                    String query = "UPDATE Menu SET NAME = ?, PRICE = ?, CATEGORY = ? WHERE NAME = ? AND RESTAURANT_ID = (SELECT ID FROM RESTAURANT WHERE USERNAME = ?)";
                    PreparedStatement pstmt = cHandler.getDBConnection().prepareStatement(query);
                    pstmt.setString(1, newName);
                    pstmt.setDouble(2, newPrice);
                    pstmt.setString(3, newCategory);
                    pstmt.setString(4, itemName);
                    pstmt.setString(5, username);
                    pstmt.executeUpdate();
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        // Refresh the menu after editing
        menuPanel.removeAll();
        retrieveMenu();
        menuPanel.revalidate();
    }

    private void deleteItem() {
        if (selectedItemLabel == null) return;
        String itemDetails = selectedItemLabel.getText();
        String itemName = itemDetails.split(" - ")[0];

        try {
            CredentialsHandler cHandler = new CredentialsHandler();
            String query = "DELETE FROM Menu WHERE NAME = ? AND RESTAURANT_ID = (SELECT ID FROM RESTAURANT WHERE USERNAME = ?)";
            PreparedStatement pstmt = cHandler.getDBConnection().prepareStatement(query);
            pstmt.setString(1, itemName);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

class EditItemDialog extends JDialog {

    private JTextField nameField, categoryField;
    private JSpinner priceField;
    private JButton saveButton, cancelButton;
    // private String originalName;
    private boolean confirmed;

    public EditItemDialog(Frame parent, String itemName, String itemPrice, String itemCategory) {
        super(parent, "Edit Menu Item", true);
        // this.originalName = itemName;
        this.confirmed = false;

        setLayout(new GridLayout(4, 2));

        add(new JLabel("Item Name"));
        nameField = new JTextField(itemName);
        add(nameField);

        add(new JLabel("Item Price"));
        SpinnerNumberModel priceModel = new SpinnerNumberModel(Double.parseDouble(itemPrice), 0.0, Double.MAX_VALUE, 0.1);
        priceField = new JSpinner(priceModel);

        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(priceField, "#.##");
        JFormattedTextField textField = editor.getTextField();
        textField.setHorizontalAlignment(SwingConstants.LEFT); // Align text to the left
        priceField.setFont(new Font("Arial", Font.PLAIN, 14));
        priceField.setEditor(editor);

        add(priceField);

        add(new JLabel("Item Category"));
        categoryField = new JTextField(itemCategory);
        add(categoryField);

        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");

        add(saveButton);
        add(cancelButton);

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmed = true;
                setVisible(false);
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmed = false;
                setVisible(false);
            }
        });

        setLocationRelativeTo(parent);
        setSize(200, 150);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getItemName() {
        return nameField.getText();
    }

    public double getItemPrice() {
        return (double) priceField.getValue();
    }

    public String getItemCategory() {
        return categoryField.getText();
    }

    public boolean validateInput() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Item Name field can't be empty", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (categoryField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Category field can't be empty", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }
}