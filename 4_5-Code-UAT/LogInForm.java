
import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.awt.*;

public class LogInForm extends JFrame implements ActionListener {
    private JTextField username, unameRegField;
    private JPasswordField passwd, passwdRegField;
    private JButton loginButton, regButton;
    private JCheckBox showPassLogin, showPassReg;
    private JPanel mainPanel, loginPanel, regPanel;
    private CardLayout cardLayout;

    public LogInForm() {
        super("Log-in Form");
        initForm();
    }

    public void initForm() {
        cardLayout = new CardLayout(); // we need the card layout so we can have a lot of panels in the same window
        mainPanel = new JPanel(cardLayout);

        // Login Form
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
        regPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));

        JLabel unameRegLabel = new JLabel("Username ");
        unameRegField = new JTextField(12);

        JLabel passwdRegLabel = new JLabel("Password ");
        passwdRegField = new JPasswordField(12);

        showPassReg = new JCheckBox("Show Password");
        regButton = new JButton("Sign Up");

        JLabel backToLoginLabel = new JLabel("<html><u>Back to Log In</u></html>");
        backToLoginLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backToLoginLabel.setForeground(Color.BLUE);
        backToLoginLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 13));

        regPanel.add(unameRegLabel);
        regPanel.add(unameRegField);
        regPanel.add(passwdRegLabel);
        regPanel.add(passwdRegField);
        regPanel.add(showPassReg);
        showPassReg.addActionListener(this);
        regPanel.add(regButton);
        regPanel.add(backToLoginLabel);

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
        setSize(300, 250);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == loginButton) {
            String user = username.getText();
            String pass = passwd.getText(); // Use getPassword instead of getText for JPasswordField

            System.out.println("Username: " + user);
            System.out.println("Password: " + pass);

            CredentialsHandler loginHandler = new CredentialsHandler();
            boolean res = loginHandler.checkEntry(user, pass);

            if (res)
                JOptionPane.showMessageDialog(this, "Log In Successful");
            else
                JOptionPane.showMessageDialog(this, "User with given credentials not found");
        } else if (ae.getSource() == regButton) {
            String regUser = unameRegField.getText();
            String regPass = passwdRegField.getText();

            System.out.println("New Username: " + regUser);
            System.out.println("New Password: " + regPass);

            CredentialsHandler regHandler = new CredentialsHandler();
            boolean regResult = regHandler.registerUser(regUser, regPass);

            if(regResult) {
                JOptionPane.showMessageDialog(this, "Registration Successful");
                cardLayout.show(mainPanel, "Login");
            } else {
                JOptionPane.showMessageDialog(this, "Registration Failed");
            }
        }

        if (showPassLogin.isSelected()) {
            passwd.setEchoChar((char) 0);
        } else {
            passwd.setEchoChar('*');
        }

        if (showPassReg.isSelected()) {
            passwdRegField.setEchoChar((char) 0);
        } else {
            passwdRegField.setEchoChar('*');
        }
    }

    public static void main(String[] args) {
        new LogInForm();
    }
}
