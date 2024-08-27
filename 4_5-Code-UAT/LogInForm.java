import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.Map;
import javax.swing.*;

public class LogInForm extends JFrame implements ActionListener {
    private JTextField email, unameRegField, emailRegField;
    private JPasswordField passwd, passwdRegField;
    private JCheckBox showPassLogin, showPassReg;
    private JButton loginButton, regButton;
    private JPanel mainPanel, loginPanel, regPanel, dynamicPanel;
    private CardLayout cardLayout;
    private JComboBox<String> userTypeComboBox;
    JTextField addressField, phoneNumberField, restaurantNameField, cuisineTypeField, locationField;

    public LogInForm() {
        super("Log-in Form");
        initForm();
    }

    public void initForm() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        addressField = new JTextField(12);
        phoneNumberField = new JTextField(12);
        restaurantNameField = new JTextField(12);
        cuisineTypeField = new JTextField(12);
        locationField = new JTextField(12);
       
        initLoginPanel();
        initRegistrationPanel();

        // add both panels to the main panel
        mainPanel.add(loginPanel, "Login");
        mainPanel.add(regPanel, "Registration");

        // add main panel to the frame
        add(mainPanel);

        setupPanel();
    }

    private void setupPanel() {
        setLocationRelativeTo(null); // in order for the window to be a bit more to the center
        //setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 550);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == regButton) {
            handleRegistration();
        } else if (ae.getSource() == userTypeComboBox) {
            String selectedType = (String) userTypeComboBox.getSelectedItem();
            if (selectedType != null && !selectedType.equals("Select")) {
                SwingUtilities.invokeLater(() -> updateDynamicPanel(selectedType));
            } else {
                dynamicPanel.setVisible(false);
            }
        }

        // Handle show password checkbox for login panel
        if (ae.getSource() == showPassLogin) {
            passwd.setEchoChar(showPassLogin.isSelected() ? (char)0 : '●');
        }

        // Handle show password checkbox for registration panel
        if (ae.getSource() == showPassReg) {
            passwd.setEchoChar(showPassReg.isSelected() ? (char)0 : '●');
        }
    }

    private void initLoginPanel() {
        loginPanel = new JPanel();
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(0xe7a780));
        JPanel botPanel = new JPanel(new GridBagLayout()); 
        botPanel.setBackground(new Color(0x575658));
    
        AppLogo logo = new AppLogo();
        topPanel.add(logo.getLabel());
    
        JLabel emailLabel = new JLabel("E-mail");
        emailLabel.setForeground(Color.WHITE);
        JLabel passwordLabel = new JLabel("Password ");
        passwordLabel.setForeground(Color.WHITE);
    
        email = new JTextField(15);
        passwd = new JPasswordField(15);
        loginButton = new JButton("Log-In");
        loginButton.setFocusable(false);
        loginButton.setBackground(Color.WHITE);
        loginButton.setForeground(Color.BLACK);
        loginButton.addActionListener(e -> handleLogin());

        showPassLogin = new JCheckBox("Show Password");
        showPassLogin.setForeground(Color.WHITE);
        showPassLogin.setBackground(botPanel.getBackground());
        showPassLogin.setFocusable(false);
    
        JLabel preSignUpLabel = new JLabel("Don't have an account? ");
        preSignUpLabel.setForeground(Color.WHITE);
        JLabel signUpLabel = new JLabel("<html><u>Sign Up</u></html>");
        signUpLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signUpLabel.setForeground(new Color(0x0099FF));
        signUpLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 13));
    
        // Set up GridBagConstraints 
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        addToGrid(emailLabel, email, botPanel, gbc,0, 0);
        addToGrid(passwordLabel, passwd, botPanel, gbc,0, 1);

        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        botPanel.add(showPassLogin, gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST; 
        botPanel.add(loginButton, gbc);
    
        JPanel signUpPanel = new JPanel();
        signUpPanel.add(preSignUpLabel);
        signUpPanel.add(signUpLabel);
        signUpPanel.setBackground(botPanel.getBackground()); 
    
        gbc.gridx = 1; gbc.gridy = 4; gbc.anchor = GridBagConstraints.CENTER;
        botPanel.add(signUpPanel, gbc);
    
        loginPanel.setLayout(new BorderLayout());
        loginPanel.add(topPanel, BorderLayout.NORTH);
        loginPanel.add(botPanel, BorderLayout.CENTER);
    
        // Add listeners
        showPassLogin.addActionListener(this);
        loginButton.addActionListener(this);
        signUpLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                cardLayout.show(mainPanel, "Registration");
            }
        });
    }    

    private void addToGrid(JLabel label, JTextField field, JPanel panel, GridBagConstraints gbc, int x, int y) {
        for (int i = x; i < 2; i++) {
            gbc.gridx = i; 
            gbc.gridy = y;
            gbc.anchor = (i == 0) ? GridBagConstraints.EAST : GridBagConstraints.WEST;
            panel.add((i == 0) ? label : field, gbc);
        }
    }

    private void initRegistrationPanel() {

        regPanel = new JPanel();
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(0xe7a780));
        JPanel botPanel = new JPanel(new GridBagLayout()); 
        botPanel.setBackground(new Color(0x575658));

        AppLogo logo = new AppLogo();
        topPanel.add(logo.getLabel());

        JLabel unameRegLabel = new JLabel("Username ");
        unameRegLabel.setForeground(Color.WHITE);
        unameRegField = new JTextField(15);

        JLabel emailRegLabel = new JLabel("E-mail");
        emailRegLabel.setForeground(Color.WHITE);
        emailRegField = new JTextField(15);

        JLabel passwdRegLabel = new JLabel("Password ");
        passwdRegLabel.setForeground(Color.WHITE);
        passwdRegField = new JPasswordField(15);

        regButton = new JButton("Sign Up");
        regButton.setFocusable(false);
        regButton.setBackground(Color.WHITE);
        regButton.setForeground(Color.BLACK);

        showPassReg = new JCheckBox("Show Password");
        showPassReg.setForeground(Color.WHITE);
        showPassReg.setBackground(botPanel.getBackground());
        showPassReg.setFocusable(false);
        
        // User type selection
        String[] userTypes = {"Select", "Customer", "Driver", "Restaurant"};
        userTypeComboBox = new JComboBox<>(userTypes);

        dynamicPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));
        dynamicPanel.setVisible(false);  // Initially hidden, will show based on user type selection
        dynamicPanel.setBackground(botPanel.getBackground());

        JLabel backToLoginLabel = new JLabel("<html><u>Back to Log In</u></html>");
        backToLoginLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backToLoginLabel.setForeground(Color.BLUE);
        backToLoginLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 13));

        // Set up GridBagConstraints 
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        addToGrid(unameRegLabel, unameRegField, botPanel, gbc, 0, 0);
        addToGrid(emailRegLabel, emailRegField, botPanel, gbc, 0, 1);
        addToGrid(passwdRegLabel, passwdRegField, botPanel, gbc, 0, 2);

        gbc.gridx = 2; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        botPanel.add(showPassReg, gbc);

        gbc.gridx = 1; gbc.gridy = 3; gbc.anchor = GridBagConstraints.WEST;
        botPanel.add(userTypeComboBox, gbc);

        gbc.gridx = 1; gbc.gridy = 4; gbc.anchor = GridBagConstraints.CENTER;
        botPanel.add(dynamicPanel, gbc); // Add dynamic panel to the registration panel

        JPanel signUpPanel = new JPanel();
        signUpPanel.add(backToLoginLabel);
        signUpPanel.add(regButton);
        signUpPanel.setBackground(botPanel.getBackground());
    
        gbc.gridx = 1; gbc.gridy = 5; gbc.anchor = GridBagConstraints.CENTER;
        botPanel.add(signUpPanel, gbc);

        regPanel.setLayout(new BorderLayout());
        regPanel.add(topPanel, BorderLayout.NORTH);
        regPanel.add(botPanel, BorderLayout.CENTER);

        // Add listeners
        showPassReg.addActionListener(this);
        userTypeComboBox.addActionListener(this);
        regButton.addActionListener(this);
        backToLoginLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                cardLayout.show(mainPanel, "Login");
            }
        });
    }

    private boolean registerUserByType(String userType, String username, String password, String email) {
        CredentialsHandler regHandler = new CredentialsHandler();

        try {
            if (regHandler.checkExisting(email)) {
                JOptionPane.showMessageDialog(this, "User already exists");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        switch (userType) {
            case "Customer":
                if(!addressField.getText().isEmpty())
                    return regHandler.registerCustomer(username, password, email, addressField.getText());
                else JOptionPane.showMessageDialog(this, "Please fill in the Address Field");
                break;
            case "Driver":
                // different if statements because it prints two different messages depending
                if(!phoneNumberField.getText().isEmpty())
                    if(phoneNumberField.getText().matches("[0-9]{10}"))
                        return regHandler.registerDriver(username, password, email, phoneNumberField.getText());
                    else JOptionPane.showMessageDialog(this, "Phone number must be 10 digits");
                else JOptionPane.showMessageDialog(this, "Please fill in the Phone Number Field");
                break;
            case "Restaurant":
                if(!restaurantNameField.getText().isEmpty() && !cuisineTypeField.getText().isEmpty() && !locationField.getText().isEmpty())
                    return regHandler.registerRestaurant(username, password, email, restaurantNameField.getText(), cuisineTypeField.getText(), locationField.getText());
                else JOptionPane.showMessageDialog(this, "You need to fill in the appropriate fields");
                break;
            default:
                break;
        }

        return false;
    }

    private void handleRegistration() {
        String regUser = unameRegField.getText();
        String regPass = new String(passwdRegField.getPassword());
        String regEmail = emailRegField.getText();
        String userType = (String) userTypeComboBox.getSelectedItem();

        if (userType == null || userType.equals("Select")) {
            JOptionPane.showMessageDialog(this, "Please select a user type");
            return;
        }

        if (regUser.isEmpty() || regPass.isEmpty() || regEmail.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please provide the required credentials");
            return;
        }

        if (!regEmail.matches("^(.+)@(.+)$")) {
            JOptionPane.showMessageDialog(this, "Wrong E-mail format (e.g example@gmail.com)");
            return;
        }

        boolean registrationSuccessful = registerUserByType(userType, regUser, regPass, regEmail);

        if (registrationSuccessful) {
            JOptionPane.showMessageDialog(this, "Registration Successful. Pressing OK will take you to the log-in page");
            cardLayout.show(mainPanel, "Login");
        }
    }

    private void updateDynamicPanel(String userType) {
        dynamicPanel.removeAll();  // Clear the panel

        switch (userType) {
            case "Customer":
                JLabel addressLabel = new JLabel("Address"); addressLabel.setForeground(Color.WHITE);
                dynamicPanel.add(addressLabel);
                dynamicPanel.add(addressField);
                break;
            case "Driver":
                JLabel phoneLabel = new JLabel("Phone Number");phoneLabel.setForeground(Color.WHITE);
                dynamicPanel.add(phoneLabel);
                dynamicPanel.add(phoneNumberField);
                break;
            case "Restaurant":
                JPanel restaurantPanel = new JPanel();
                restaurantPanel.setLayout(new BoxLayout(restaurantPanel, BoxLayout.Y_AXIS));
                restaurantPanel.setBackground(new Color(0x575658));

                JLabel restaurantNameLabel = new JLabel("Restaurant Name");restaurantNameLabel.setForeground(Color.WHITE);
                JLabel cuisineTypeLabel = new JLabel("Cuisine Type");cuisineTypeLabel.setForeground(Color.WHITE);
                JLabel locationLabel = new JLabel("Location");locationLabel.setForeground(Color.WHITE);
                restaurantPanel.add(restaurantNameLabel);
                restaurantPanel.add(restaurantNameField);
                restaurantPanel.add(cuisineTypeLabel);
                restaurantPanel.add(cuisineTypeField);
                restaurantPanel.add(locationLabel);
                restaurantPanel.add(locationField);

                dynamicPanel.add(restaurantPanel);
                break;
        }

        dynamicPanel.revalidate(); // needed in order for the GUI to work properly
        dynamicPanel.repaint();
        dynamicPanel.setVisible(true);
    }

    private void handleLogin() {
        String userEmail = email.getText();
        String pass = new String(passwd.getPassword());

        CredentialsHandler ch = new CredentialsHandler();
        Map<String, String> loginResult = ch.loginUser(userEmail, pass);

        String userId = loginResult.get("USER_ID");
        String userType = loginResult.get("USER_TYPE");
        String username = loginResult.get("USERNAME");

        if (userId != null && !userId.isEmpty() && userType != null && !userType.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Logged in successfully");
            launchHomePage(userType, username);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Username/Password");
        }
    }

    private void launchHomePage(String userType, String username) {
        dispose();
        switch (userType) {
            case "Customer":
                // logic for customer home page
                new CustomerHomePage(username);
                break;
            case "Driver":
                new DriverHomePage(username);
                // logic for driver home page
                break;
            case "Restaurant":
                new RestaurantHomePage(username);
                // logic for restaurant home page
                break;
            case "Admin":
                new AdminHomePage(username);
                break;
            default:
                JOptionPane.showMessageDialog(this, "Unknown User Type");
                break;
        }
    }
}