
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

    JTextField addressField = new JTextField(12);
    JTextField phoneNumberField = new JTextField(12);
    JTextField restaurantNameField = new JTextField(12);
    JTextField cuisineTypeField = new JTextField(12);
    JTextField locationField = new JTextField(12);

    public LogInForm() {
        super("Log-in Form");
        initForm();
    }

    public void initForm() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

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
        pack();
        setLocationRelativeTo(null); // in order for the window to be a bit more to the center
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 400);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == loginButton) {
            handleLogin();
        } else if (ae.getSource() == regButton) {
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
            if (showPassLogin.isSelected())
                passwd.setEchoChar((char) 0);
            else passwd.setEchoChar('*');
        }

        // Handle show password checkbox for registration panel
        if (ae.getSource() == showPassReg) {
            if (showPassReg.isSelected())
                passwdRegField.setEchoChar((char) 0);
            else passwdRegField.setEchoChar('*');
        }
    }

    private void initLoginPanel() {
        loginPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));

        JLabel emailLabel = new JLabel("E-mail        ");
        JLabel passwordLabel = new JLabel("Password ");

        email = new JTextField(15);
        passwd = new JPasswordField(15);
        loginButton = new JButton("Log-In");

        showPassLogin = new JCheckBox("Show Password");
        showPassLogin.setFocusable(false);

        JLabel preSignUpLabel = new JLabel("Don't have an account? ");
        JLabel signUpLabel = new JLabel("<html><u>Sign Up</u></html>");
        signUpLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signUpLabel.setForeground(Color.BLUE);
        signUpLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 13));

        loginPanel.add(emailLabel);
        loginPanel.add(email);
        loginPanel.add(passwordLabel);
        loginPanel.add(passwd);
        loginPanel.add(showPassLogin);
        loginPanel.add(loginButton);
        loginPanel.add(preSignUpLabel);
        loginPanel.add(signUpLabel);

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

    private void initRegistrationPanel() {
        regPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));

        JLabel unameRegLabel = new JLabel("Username ");
        unameRegField = new JTextField(15);

        JLabel emailRegLabel = new JLabel("E-mail        ");
        emailRegField = new JTextField(15);

        JLabel passwdRegLabel = new JLabel("Password ");
        passwdRegField = new JPasswordField(15);

        regButton = new JButton("Sign Up");
        showPassReg = new JCheckBox("Show Password");
        showPassReg.setFocusable(false);

        // User type selection
        String[] userTypes = {"Select", "Customer", "Driver", "Restaurant"};
        userTypeComboBox = new JComboBox<>(userTypes);

        dynamicPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));
        dynamicPanel.setVisible(false);  // Initially hidden, will show based on user type selection

        JLabel backToLoginLabel = new JLabel("<html><u>Back to Log In</u></html>");
        backToLoginLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backToLoginLabel.setForeground(Color.BLUE);
        backToLoginLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 13));

        regPanel.add(unameRegLabel);
        regPanel.add(unameRegField);
        regPanel.add(emailRegLabel);
        regPanel.add(emailRegField);
        regPanel.add(passwdRegLabel);
        regPanel.add(passwdRegField);
        regPanel.add(showPassReg);
        regPanel.add(userTypeComboBox);
        regPanel.add(dynamicPanel);  // Add dynamic panel to the registration panel
        regPanel.add(backToLoginLabel);
        regPanel.add(regButton);

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
        String regPass = passwdRegField.getText();
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
                dynamicPanel.add(new JLabel("Address:"));
                dynamicPanel.add(addressField);
                break;
            case "Driver":
                dynamicPanel.add(new JLabel("Phone Number:"));
                dynamicPanel.add(phoneNumberField);
                break;
            case "Restaurant":
                JPanel restaurantPanel = new JPanel();
                restaurantPanel.setLayout(new BoxLayout(restaurantPanel, BoxLayout.Y_AXIS));

                restaurantPanel.add(new JLabel("Restaurant Name"));
                restaurantPanel.add(restaurantNameField);
                restaurantPanel.add(new JLabel("Cuisine Type"));
                restaurantPanel.add(cuisineTypeField);
                restaurantPanel.add(new JLabel("Location"));
                restaurantPanel.add(locationField);

                dynamicPanel.add(restaurantPanel);
                break;
        }

        dynamicPanel.revalidate();
        dynamicPanel.repaint();
        dynamicPanel.setVisible(true);
    }

    private void handleLogin() {
        String userEmail = email.getText();
        String pass = passwd.getText();

        CredentialsHandler ch = new CredentialsHandler();
        Map<String, String> loginResult = ch.loginUser(userEmail, pass);

        String userId = loginResult.get("USER_ID");
        String userType = loginResult.get("USER_TYPE");

        if (userId != null && !userId.isEmpty() && userType != null && !userType.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Logged in successfully");

            switch (userType) {
                case "Customer":
                    // logic for customer home page
                    break;
                case "Driver":
                    // logic for driver home page
                    break;
                case "Restaurant":
                    // logic for restaurant home page
                    break;
                default:
                    JOptionPane.showMessageDialog(this, "Unknown User Type");
                    break;
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Username/Password");
        }
    }
}
