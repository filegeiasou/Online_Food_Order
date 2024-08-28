import javax.swing.*;
import java.awt.*;
import java.sql.*;


public class AboutInfo extends JFrame {
    private String username;
    private String userType;
    private JTextField passwordField, emailField;
    private JTextField nameField = null, locationField = null, cuisineField = null, ratingField = null, addressField = null, phoneField = null;
    Connection dbConnection;

    public AboutInfo(String username, String userType) {
        this.username = username;
        this.userType = userType;
        initFrame();
        CredentialsHandler cHandler = new CredentialsHandler();
        dbConnection = cHandler.getDBConnection();
        switch(userType){
            case "Customer":
                retrieveCustomerInfo();
            break;
            case "Driver":
                retrieveDriverInfo();
            break;
            case "Restaurant":
                retrieveRestaurantInfo();
            break;
        }
        retrieveUserInfo();
    }

    private void initFrame() {
        setTitle(userType + " Information");
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
        JLabel nameLabel = null, locationLabel = null, cuisineLabel = null, ratingLabel = null, addressLabel = null, phoneLabel = null;

        JTextField usernameField = new JTextField(20);
        usernameField.setText(username);
        passwordField = new JTextField(20);
        emailField = new JTextField(20);
        
        switch(userType){
            case "Customer":
                addressLabel = new JLabel("Address: ");
                addressField = new JTextField(20);
            break;
            case "Restaurant":
                nameLabel = new JLabel("Restaurant Name: ");
                locationLabel = new JLabel("Location: ");
                cuisineLabel = new JLabel("Cuisine: ");
                ratingLabel = new JLabel("Rating: ");
                nameField = new JTextField(20);
                locationField = new JTextField(20);
                cuisineField = new JTextField(20);
                ratingField = new JTextField(20);
            break;
            case "Driver":
                phoneLabel = new JLabel("Phone Number: ");
                phoneField = new JTextField(20);  
        }
       
        JLabel[] labels = {usernameLabel, passwordLabel, emailLabel, nameLabel, locationLabel, cuisineLabel, ratingLabel, addressLabel, phoneLabel};
        for (JLabel label : labels) {
            if(label != null)
                label.setForeground(Color.WHITE);
        }

        JTextField[] fields = {usernameField, passwordField, emailField, nameField, locationField, cuisineField, ratingField, addressField, phoneField};
        for (JTextField field : fields) {
            if(field != null){
                field.setEditable(false);
                field.setFocusable(false);
            }
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        addToGrid(usernameLabel, usernameField, botPanel, gbc, 0);
        addToGrid(passwordLabel, passwordField, botPanel, gbc, 1);
        addToGrid(emailLabel, emailField, botPanel, gbc, 2);
        switch(userType){
            case "Customer":
                addToGrid(addressLabel, addressField, botPanel, gbc, 3);
            break;
            case "Driver":
                addToGrid(phoneLabel, phoneField, botPanel, gbc, 3);
            break;
            case "Restaurant":
                addToGrid(nameLabel, nameField, botPanel, gbc, 3);
                addToGrid(locationLabel, locationField, botPanel, gbc, 4);
                addToGrid(cuisineLabel, cuisineField, botPanel, gbc, 5);
                addToGrid(ratingLabel, ratingField, botPanel, gbc, 6);
            break;
            
        }
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

    private void retrieveCustomerInfo() {
        try {
            CredentialsHandler cHandler = new CredentialsHandler();
            String query = "SELECT * FROM Customer WHERE USERNAME = ?";
            PreparedStatement pstmt = cHandler.getDBConnection().prepareStatement(query);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String address = rs.getString("ADDRESS");
                addressField.setText(address);
            }

            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void retrieveDriverInfo() {
        try {
            CredentialsHandler cHandler = new CredentialsHandler();
            String query = "SELECT * FROM Driver WHERE USERNAME = ?";
            PreparedStatement pstmt = cHandler.getDBConnection().prepareStatement(query);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String phone = rs.getString("PHONE_NUMBER");
                phoneField.setText(phone);
            }

            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void retrieveRestaurantInfo() {
        try {
            
            String query = "SELECT * FROM Restaurant WHERE USERNAME = ?";
            PreparedStatement pstmt = dbConnection.prepareStatement(query);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("NAME");
                String location = rs.getString("LOCATION");
                String cuisine = rs.getString("CUISINE_TYPE");
                String rating = rs.getString("RATING");

                nameField.setText(name);
                locationField.setText(location);
                cuisineField.setText(cuisine);
                ratingField.setText(rating);
            }

            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void retrieveUserInfo(){
        try {
            CredentialsHandler cHandler = new CredentialsHandler();
            String query = "SELECT EMAIL, PASSWORD FROM User WHERE USERNAME = ?";
            PreparedStatement pstmt = cHandler.getDBConnection().prepareStatement(query);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String email = rs.getString("EMAIL");
                String passwd = rs.getString("PASSWORD");
                emailField.setText(email);
                passwordField.setText(passwd);
            }

            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}