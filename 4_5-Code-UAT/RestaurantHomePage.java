import javax.swing.*;
import java.awt.*;
// import java.awt.event.*;
// import java.sql.*;
// import java.util.*;

public class RestaurantHomePage extends JFrame {

    private String username;
    private JPanel topPanel, botPanel;

    public RestaurantHomePage(String username) {
        this.username = username;
        initFrame();    
    }

    private void initFrame() {
        setTitle("Restaurant Home Page");
        setLayout(new BorderLayout());
        // Panels
        topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.setBackground(new Color(0xe7a780));

        botPanel = new JPanel();
        botPanel.setLayout(new BorderLayout());
        botPanel.setBackground(new Color(0x575658));

        // Logo on topPanel
        AppLogo logo = new AppLogo();
        
        // Welcome label under logo
        JLabel welcomeLabel = new JLabel("Welcome, " + username + "!");
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 25));
        welcomeLabel.setForeground(Color.WHITE);

        topPanel.add(logo.getLabel(), BorderLayout.CENTER); 
        topPanel.add(welcomeLabel, BorderLayout.SOUTH); 
        
        add(topPanel, BorderLayout.NORTH);
        add(botPanel, BorderLayout.CENTER);
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
