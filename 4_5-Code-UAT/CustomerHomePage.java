import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class CustomerHomePage extends JFrame implements ActionListener {

    JButton viewOrdersButton, infoButton, LogoutButton;
    String username;
    JList<String> restaurantList;
    DefaultListModel<String> listModel;

    public CustomerHomePage(String username) {
        setTitle("Customer Home Page");
        setLayout(new BorderLayout());
        this.username = username;

        JPanel topPanel = new JPanel();
        JPanel botPanel = new JPanel();
        JLabel welcomeLabel = new JLabel("Welcome, " + username + "!");

        // Buttons
        viewOrdersButton = new JButton("View Orders");
        infoButton = new JButton("Account Info");
        LogoutButton = new JButton("Logout");
        viewOrdersButton.addActionListener(this);
        infoButton.addActionListener(this);
        LogoutButton.addActionListener(this);

        ImageIcon icon = new ImageIcon("4_5-Code-UAT/logo.png");
        icon = new ImageIcon(icon.getImage().getScaledInstance(100, 90, Image.SCALE_DEFAULT));
        JLabel imageLabel = new JLabel(icon);

        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 25));
        welcomeLabel.setForeground(Color.WHITE);

        topPanel.setLayout(new BorderLayout());
        topPanel.add(imageLabel, BorderLayout.CENTER); 
        topPanel.add(welcomeLabel, BorderLayout.SOUTH); 
        topPanel.setBackground(new Color(0xe7a780));

        botPanel.setLayout(new FlowLayout());
        botPanel.add(viewOrdersButton);
        botPanel.add(infoButton);
        botPanel.add(LogoutButton);
        botPanel.setBackground(new Color(0x575658));

        // Adding the list of restaurants
        listModel = new DefaultListModel<>();
        restaurantList = new JList<>(listModel);
        JScrollPane listScrollPane = new JScrollPane(restaurantList);
        listScrollPane.setPreferredSize(new Dimension(450, 300));
        botPanel.add(listScrollPane);

        // Load restaurants from database
        loadRestaurants();

        // Open restaurant page on double click
        restaurantList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Double-click detected
                    int index = restaurantList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        String selectedRestaurant = restaurantList.getModel().getElementAt(index);
                        new RestaurantPage(selectedRestaurant);
                    }
                }
            }
        });

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
            String query = "SELECT NAME FROM Restaurant";
            Statement stmt = cHandler.dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String restaurantName = rs.getString("name");
                listModel.addElement(restaurantName);
            }
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
}

class RestaurantPage extends JFrame {
    public RestaurantPage(String restaurantName) {
        setTitle(restaurantName);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(0xe7a780));
        ImageIcon icon = new ImageIcon("4_5-Code-UAT/logo.png");
        icon = new ImageIcon(icon.getImage().getScaledInstance(100, 90, Image.SCALE_DEFAULT));
        JLabel imageLabel = new JLabel(icon);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topPanel.add(imageLabel);

        JPanel botpanel = new JPanel();
        botpanel.setBackground(new Color(0x575658));
        JLabel infoLabel = new JLabel("Welcome to " + restaurantName);
        infoLabel.setFont(new Font("Arial", Font.BOLD, 18));
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoLabel.setForeground(Color.WHITE);
        botpanel.add(infoLabel);

        add(topPanel, BorderLayout.NORTH);
        add(botpanel, BorderLayout.CENTER);

        setSize(300, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
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
        JLabel userTypeLabel = new JLabel("User Type: "); userTypeLabel.setForeground(Color.WHITE);

        JTextField usernameField = new JTextField(20); usernameField.setEditable(false);
        usernameField.setText(username);
        passwordField = new JTextField(20); passwordField.setEditable(false);
        addressField = new JTextField(20); addressField.setEditable(false);
        emailField = new JTextField(20);  emailField.setEditable(false);
        userTypeField = new JTextField(20); userTypeField.setEditable(false);
    
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        addToGrid(usernameLabel, usernameField, botPanel, gbc, 0);
        addToGrid(passwordLabel, passwordField, botPanel, gbc, 1);
        addToGrid(emailLabel, emailField, botPanel, gbc, 2);
        addToGrid(addressLabel, addressField, botPanel, gbc, 3);
        addToGrid(userTypeLabel, userTypeField, botPanel, gbc, 4);

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
            String query = "SELECT ID, USERNAME, PASSWORD, ADDRESS FROM Customer WHERE USERNAME = ?";
            PreparedStatement pstmt = cHandler.dbConnection.prepareStatement(query);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String passwd = rs.getString("PASSWORD");
                String address = rs.getString("ADDRESS");

                passwordField.setText(passwd);
                addressField.setText(address);
            }

            query = "SELECT EMAIL, USER_TYPE FROM User WHERE USERNAME = ?";
            pstmt = cHandler.dbConnection.prepareStatement(query);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                String email = rs.getString("EMAIL");
                String userType = rs.getString("USER_TYPE");
                emailField.setText(email);
                userTypeField.setText(userType);
            }

            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

class OrdersPage extends JFrame {
    public OrdersPage(String username) {
        setTitle("Orders");
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(0xe7a780));
        AppLogo logo = new AppLogo();
        topPanel.add(logo.getLabel());

        JPanel botpanel = new JPanel();
        botpanel.setBackground(new Color(0x575658));
        JLabel infoLabel = new JLabel("Orders for " + username);
        infoLabel.setFont(new Font("Arial", Font.BOLD, 18));
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoLabel.setForeground(Color.WHITE);
        botpanel.add(infoLabel);

        add(topPanel, BorderLayout.NORTH);
        add(botpanel, BorderLayout.CENTER);

        setSize(300, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}