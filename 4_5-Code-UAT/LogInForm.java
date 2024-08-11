import java.io.IOException;

import java.awt.FlowLayout;
import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LogInForm extends JFrame implements ActionListener {
    private JTextField username;
    private JPasswordField passwd;
    private JCheckBox showPass;
    private JButton loginButton;

    public LogInForm () throws IOException {
        super("Log-in Window");
        initForm();
    }

    public void initForm() {
        // setLayout(new FlowLayout());
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 20));


        JLabel usernameLabel = new JLabel("Username ");
        JLabel passwordLabel = new JLabel("Password ");

        username = new JTextField(12);
        passwd = new JPasswordField(12);
        loginButton = new JButton("Log-In");

        showPass = new JCheckBox("Show Password");
        showPass.setFocusable(false);

        add(usernameLabel);
        add(username);
        add(passwordLabel);
        add(passwd);
        add(showPass);
        showPass.addActionListener(this);
        add(loginButton);

    
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 250);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent ae)
    {
        if(showPass.isSelected())
            passwd.setEchoChar((char) 0);
        else passwd.setEchoChar('*');
    }

    public static void main(String[] args) {
        try {
            new LogInForm();
        } catch(IOException ioe) {
            System.err.println("Could not start log in page");
            ioe.printStackTrace();
        }
    }
}