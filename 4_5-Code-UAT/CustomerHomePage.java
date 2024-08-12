import javax.swing.*;
import java.awt.*;

public class CustomerHomePage extends JFrame {
    public CustomerHomePage(String username) {
        setTitle("Customer Home Page");

        JLabel welcomeLabel = new JLabel("Welcome, " + username + "!");
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 13));

        add(welcomeLabel);

        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
