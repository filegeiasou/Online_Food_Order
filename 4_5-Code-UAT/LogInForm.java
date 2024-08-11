import java.io.IOException;

import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

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

        // JLabel loginlabel = new JLabel("Log-In Page");
        // loginlabel.setFont(new Font("Arial", Font.BOLD, 22));
        
        JLabel usernameLabel = new JLabel("Username ");
        JLabel passwordLabel = new JLabel("Password ");

        username = new JTextField(12);
        passwd = new JPasswordField(12);
        loginButton = new JButton("Log-In");

        showPass = new JCheckBox("Show Password");
        showPass.setFocusable(false);

        // add(loginlabel);
        // add credentials labels and fields
        add(usernameLabel);
        add(username);
        add(passwordLabel);
        add(passwd);

        // add the show password heckbox to the frame and the listener
        add(showPass);
        showPass.addActionListener(this);
        
        // add log-in button to the frame and the listener
        add(loginButton);
        loginButton.addActionListener(this);
        
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 250);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent ae)
    {
        if(ae.getSource() == loginButton) {
            // username.getText();
            // passwd.getPassword();

            System.out.println("Username: " + username.getText());
            System.out.println("Password: " + passwd.getText());
            //! getText is depracated but getPassword hashes the password.
            //!  So if we do .toString we get a different result.
        }

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