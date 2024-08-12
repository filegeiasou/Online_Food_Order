import javax.swing.*;

public class RestaurantHomePage extends JFrame {
    public RestaurantHomePage(String username) {
        setTitle("Restaurant Home Page");
        JLabel welcomeLabel = new JLabel("Welcome, " + username + "!");
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        add(welcomeLabel);

        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
