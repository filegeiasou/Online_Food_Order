import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class RestaurantHomePage extends JFrame implements ActionListener {

    private String username;
    private JButton infoButton, LogoutButton;
    private JPanel topPanel, botPanel, buttonPanel;

    public RestaurantHomePage(String username) {
        this.username = username;
        initFrame();    
    }

    private void initFrame() {
        setTitle("Restaurant Home Page");
        setLayout(new BorderLayout());
        // Panels
        topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.setBackground(new Color(0xe7a780));

        botPanel = new JPanel();
        botPanel.setLayout(new BorderLayout());
        botPanel.setBackground(new Color(0x575658));

        // Logo on topPanel
        AppLogo logo = new AppLogo();
        
        // Welcome label under logo
        JLabel welcomeLabel = new JLabel("Welcome, " + username + "!");
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 25));
        welcomeLabel.setForeground(Color.WHITE);

        topPanel.add(logo.getLabel(), BorderLayout.CENTER); 
        topPanel.add(welcomeLabel, BorderLayout.SOUTH); 

        // Buttons 
        infoButton = new JButton("Account Info");
        LogoutButton = new JButton("Logout");

        JButton[] buttons = {infoButton, LogoutButton};
        for (JButton button : buttons) {
            button.setBackground(Color.WHITE);
            button.setForeground(Color.BLACK);
            button.setFocusable(false); 
            button.addActionListener(this);
        }

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.setBackground(new Color(0x575658));
        buttonPanel.add(infoButton);
        buttonPanel.add(LogoutButton);

        botPanel.add(buttonPanel, BorderLayout.NORTH);
        
        add(topPanel, BorderLayout.NORTH);
        add(botPanel, BorderLayout.CENTER);
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == infoButton) {
            new AboutInfoRES(username);
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

class AboutInfoRES extends JFrame {
    private String username;
    private JTextField passwordField, emailField, nameField, locationField, cuisineField, ratingField;;

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
        JLabel nameLabel = new JLabel("Name: "); 
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
        try{
            CredentialsHandler cHandler = new CredentialsHandler();
            String query = "SELECT * FROM Restaurant WHERE USERNAME = ?";
            PreparedStatement pstmt = cHandler.getDBConnection().prepareStatement(query);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String passwd = rs.getString("PASSWORD");
                String name = rs.getString("NAME");
                String locatio = rs.getString("LOCATION");
                String cuisine = rs.getString("CUISINE_TYPE");
                String rating = rs.getString("RATING");

                passwordField.setText(passwd);
                nameField.setText(name);
                locationField.setText(locatio);
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

            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}