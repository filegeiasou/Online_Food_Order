import javax.swing.*;

public class DriverHomePage extends JFrame {
    public DriverHomePage(String username) {
        setTitle("Driver Home Page");
        JLabel welcomeLabel = new JLabel("Welcome, " + username + "!");
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        add(welcomeLabel);

        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
