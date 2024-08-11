import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class LogInForm extends JFrame implements ActionListener {
    private JTextField username, unameRegField;
    private JPasswordField passwd, passwdRegField;
    private JCheckBox showPassLogin, showPassReg;
    private JButton loginButton, regButton;
    private JPanel mainPanel, loginPanel, regPanel, dynamicPanel;
    private CardLayout cardLayout;
    private JComboBox<String> userTypeComboBox;

    public LogInForm() {
        super("Log-in Form");
        initForm();
    }

    public void initForm() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Login Form (Simple)
        loginPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));

        JLabel usernameLabel = new JLabel("Username ");
        JLabel passwordLabel = new JLabel("Password ");

        username = new JTextField(12);
        passwd = new JPasswordField(12);
        loginButton = new JButton("Log-In");

        showPassLogin = new JCheckBox("Show Password");
        showPassLogin.setFocusable(false);

        JLabel preSignUpLabel = new JLabel("Don't have an account? ");
        JLabel signUpLabel = new JLabel("<html><u>Sign Up</u></html>");
        signUpLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signUpLabel.setForeground(Color.BLUE);
        signUpLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 13));

        loginPanel.add(usernameLabel);
        loginPanel.add(username);
        loginPanel.add(passwordLabel);
        loginPanel.add(passwd);
        loginPanel.add(showPassLogin);
        loginPanel.add(loginButton);
        loginPanel.add(preSignUpLabel);
        loginPanel.add(signUpLabel);

        showPassLogin.addActionListener(this);
        loginButton.addActionListener(this);

        signUpLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                cardLayout.show(mainPanel, "Registration");
            }
        });

        // Registration Form
        regPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));

        JLabel unameRegLabel = new JLabel("Username ");
        unameRegField = new JTextField(12);
        JLabel passwdRegLabel = new JLabel("Password ");
        passwdRegField = new JPasswordField(12);
        regButton = new JButton("Sign Up");

        showPassReg = new JCheckBox("Show Password");
        showPassReg.setFocusable(false);

        // User type selection
        JLabel userTypeLabel = new JLabel("User Type");
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
        regPanel.add(passwdRegLabel);
        regPanel.add(passwdRegField);
        regPanel.add(showPassReg);
        regPanel.add(userTypeComboBox);
        regPanel.add(dynamicPanel);  // Add dynamic panel to the registration panel
        regPanel.add(backToLoginLabel);
        regPanel.add(regButton);

        showPassReg.addActionListener(this);
        userTypeComboBox.addActionListener(this);
        regButton.addActionListener(this);
        backToLoginLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                cardLayout.show(mainPanel, "Login");
            }
        });

        // Add both panels to the main panel
        mainPanel.add(loginPanel, "Login");
        mainPanel.add(regPanel, "Registration");

        // Add the main panel to the frame
        add(mainPanel);

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 400);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == loginButton) {
            String user = username.getText();
            String pass = passwd.getText();

            System.out.println("Username: " + user);
            System.out.println("Password: " + pass);

            CredentialsHandler ch = new CredentialsHandler();
            boolean res = ch.checkEntry(user, pass);

            if (res)
                JOptionPane.showMessageDialog(this, "Log In Successful");
            else
                JOptionPane.showMessageDialog(this, "User with given credentials not found");
        } else if (ae.getSource() == regButton) {
            String regUser = unameRegField.getText();
            String regPass = passwdRegField.getText();
            String userType = (String) userTypeComboBox.getSelectedItem();

            switch(userType) {
                case "Customer":
                    String address =
            }


            System.out.println("New Username: " + regUser);
            System.out.println("New Password: " + regPass);
            System.out.println("User Type: " + userType);

            if(!regUser.isEmpty() && !regPass.isEmpty()) {

                CredentialsHandler regHandler = new CredentialsHandler();
                boolean regResult = regHandler.registerUser(regUser, regPass);

                if(regResult) {
                    // Handle registration logic based on user type
                    JOptionPane.showMessageDialog(this, "Registration Successful as " + userType);
                    cardLayout.show(mainPanel, "Login");
                } else {
                    JOptionPane.showMessageDialog(this, "Registration failed");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please provide the required credentials");
            }

        } else if (ae.getSource() == userTypeComboBox) {
            String selectedType = (String) userTypeComboBox.getSelectedItem();
            if(selectedType.equals("Select")) {
                dynamicPanel.setVisible(false);
                return;
            }

            SwingUtilities.invokeLater(() -> updateDynamicPanel(selectedType));
        }

        // Handle show password checkbox for login panel
        if (ae.getSource() == showPassLogin) {
            if (showPassLogin.isSelected()) {
                passwd.setEchoChar((char) 0);
            } else {
                passwd.setEchoChar('*');
            }
        }

        // Handle show password checkbox for registration panel
        if (ae.getSource() == showPassReg) {
            if (showPassReg.isSelected()) {
                passwdRegField.setEchoChar((char) 0);
            } else {
                passwdRegField.setEchoChar('*');
            }
        }
    }

    private void updateDynamicPanel(String userType) {
        dynamicPanel.removeAll();  // Clear the panel

        if (userType.equals("Customer")) {
            dynamicPanel.add(new JLabel("Address:"));
            dynamicPanel.add(new JTextField(12));
        } else if (userType.equals("Driver")) {
            dynamicPanel.add(new JLabel("Phone Number:"));
            dynamicPanel.add(new JTextField(12));
        } else if (userType.equals("Restaurant")) {
            JPanel restaurantPanel = new JPanel();
            restaurantPanel.setLayout(new BoxLayout(restaurantPanel, BoxLayout.Y_AXIS));

            restaurantPanel.add(new JLabel("Restaurant Name"));
            restaurantPanel.add(new JTextField(12));
            restaurantPanel.add(new JLabel("Cuisine Type"));
            restaurantPanel.add(new JTextField(12));
            restaurantPanel.add(new JLabel("Location"));
            restaurantPanel.add(new JTextField(12));

            dynamicPanel.add(restaurantPanel);
        }

        dynamicPanel.revalidate();
        dynamicPanel.repaint();
        dynamicPanel.setVisible(true);
    }

    public static void main(String[] args) {
        new LogInForm();
    }
}
