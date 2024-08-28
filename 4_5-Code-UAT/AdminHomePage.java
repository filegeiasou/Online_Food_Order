import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class AdminHomePage extends JFrame {
    private JTabbedPane tabbedPane;
    private String username;
    private JTable customerTable, driverTable, restaurantTable;
    private DefaultTableModel customerTableModel, driverTableModel, restaurantTableModel;
    Connection dbConnection;

    public AdminHomePage(String username) {
        this.username = username;
        CredentialsHandler cHandler = new CredentialsHandler();
        dbConnection = cHandler.getDBConnection();

        setTitle("Admin Panel");
        setLayout(new BorderLayout());
        
        AppLogo appLogo = new AppLogo();
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(new Color(0xe7a780));

        JLabel welcomeLabel = new JLabel("Welcome, " + username + "!");
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 25));
        welcomeLabel.setForeground(Color.WHITE);
        logoPanel.add(appLogo.getLabel(), BorderLayout.CENTER);
        logoPanel.add(welcomeLabel, BorderLayout.SOUTH);

        tabbedPane = new JTabbedPane();

        // Create and add tabs
        tabbedPane.addTab("Customers", createUserPanel("Customer"));
        tabbedPane.addTab("Drivers", createUserPanel("Driver"));
        tabbedPane.addTab("Restaurants", createUserPanel("Restaurant"));

        add(logoPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(tabbedPane);

        setVisible(true);
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private JPanel createUserPanel(String userType) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0x575658)); 
        panel.setBorder(BorderFactory.createLineBorder(new Color(0x575658))); 

        // Table for displaying users
        JTable userTable = new JTable();
        userTable.setSelectionBackground(new Color(0x2e2e2e));  // Change selection background color
        userTable.setBackground(new Color(0x575658)); 
        userTable.setForeground(Color.WHITE); 
        userTable.setGridColor(Color.WHITE);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Set table header (ID, Username, etc.) background to black and text to white
        JTableHeader tableHeader = userTable.getTableHeader();
        tableHeader.setBackground(new Color(0x2e2e2e)); 
        tableHeader.setForeground(Color.WHITE);

        DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"ID", "Username", "Email", "Details"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable.setModel(tableModel);

        // Assign table and model based on user type
        switch (userType) {
            case "Customer":
                customerTable = userTable;
                customerTableModel = tableModel;
                break;
            case "Driver":
                driverTable = userTable;
                driverTableModel = tableModel;
                break;
            case "Restaurant":
                restaurantTable = userTable;
                restaurantTableModel = tableModel;
                break;
        }

        // Scroll pane for the table
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.getViewport().setBackground(new Color(0x575658)); 

        // Control panel with buttons and search bar
        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(new Color(0x575658)); 
        controlPanel.setForeground(Color.WHITE); 

        JTextField searchField = new JTextField(15);
        searchField.setForeground(new Color(0x575658)); 
        searchField.setBackground(Color.WHITE); 

        JButton searchButton = new JButton("Search");
        JButton addButton = new JButton("Add");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        JButton refreshButton = new JButton("Refresh");
        JButton logOutButton = new JButton("Log Out");
        JButton aboutButton = new JButton("Account Info"); 

        JLabel searchLabel = new JLabel("Search");
        searchLabel.setForeground(Color.WHITE); 
        controlPanel.add(searchLabel);
        controlPanel.add(searchField);

        // Set buttons background to black and text to white
        JButton[] buttons = {searchButton, addButton, editButton, deleteButton, refreshButton, aboutButton, logOutButton,};
        for (JButton button : buttons) {
            button.setBackground(Color.WHITE);
            button.setForeground(Color.BLACK);
            controlPanel.add(button);
        }
        searchButton.setEnabled(false);

        // Add components to the main panel
        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        // The methods below are running on multiple instances inside the app (different tabs)
        // That's why we use the invokeLater methods, because if not,
        // then it won't know which instance is calling the function and it won't work
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> updateSearchButtonState(searchField, searchButton));
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> updateSearchButtonState(searchField, searchButton));
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> updateSearchButtonState(searchField, searchButton));
            }

            private void updateSearchButtonState(JTextField searchField, JButton searchButton) {
                // if the search field is not empty, then enable the search button
                // else restore the initial table with the unfiltered results
                if(!searchField.getText().trim().isEmpty()) {
                    searchButton.setEnabled(true);
                } else populateTable(userType);
            }
        });

        searchButton.addActionListener(e -> searchUser(userType, searchField));
        addButton.addActionListener(e -> addUser(userType));
        editButton.addActionListener(e -> editUser(userType, userTable.getSelectedRow()));
        deleteButton.addActionListener(e -> deleteUser(userType, userTable.getSelectedRow()));
        refreshButton.addActionListener(e -> refreshTable(userType));
        logOutButton.addActionListener(e -> {
            dispose();
            new LogInForm();
        });
        aboutButton.addActionListener(e -> {
            new AboutInfo(username, "Admin");
        });

        populateTable("Customer");
        populateTable("Driver");
        populateTable("Restaurant");

        return panel;
    }

    private void populateTable(String userType) {
        String userQuery = "";
        String additionalQuery = "";
        DefaultTableModel tableModel = null;

        switch (userType) {
            case "Customer":
                userQuery = "SELECT ID, USERNAME, EMAIL FROM User WHERE USERNAME IN (SELECT USERNAME FROM Customer)";
                additionalQuery = "SELECT ADDRESS FROM Customer WHERE USERNAME = ?";
                tableModel = customerTableModel;
                break;
            case "Driver":
                userQuery = "SELECT ID, USERNAME, EMAIL FROM User WHERE USERNAME IN (SELECT USERNAME FROM Driver)";
                additionalQuery = "SELECT PHONE_NUMBER FROM Driver WHERE USERNAME = ?";
                tableModel = driverTableModel;
                break;
            case "Restaurant":
                userQuery = "SELECT ID, USERNAME, EMAIL FROM User WHERE USERNAME IN (SELECT USERNAME FROM Restaurant)";
                additionalQuery = "SELECT NAME FROM Restaurant WHERE USERNAME = ?";
                tableModel = restaurantTableModel;
                break;
            default:
                return;
        }

        if (tableModel == null) return;

        tableModel.setRowCount(0);

        try {
            PreparedStatement pstmt1 = dbConnection.prepareStatement(userQuery); 
            ResultSet rs = pstmt1.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("ID");
                String username = rs.getString("USERNAME");
                String email = rs.getString("EMAIL");

                PreparedStatement pstmt = dbConnection.prepareStatement(additionalQuery);
                pstmt.setString(1, username);
                ResultSet additionalRs = pstmt.executeQuery();
                if (additionalRs.next()) {
                    Object[] row = new Object[4];
                    row[0] = id;
                    row[1] = username;
                    row[2] = email;

                    switch (userType) {
                        case "Customer":
                            row[3] = additionalRs.getString("ADDRESS");
                            break;
                        case "Driver":
                            row[3] = additionalRs.getString("PHONE_NUMBER");
                            break;
                        case "Restaurant":
                            row[3] = additionalRs.getString("NAME");
                            break;
                        default:
                            row[3] = "";
                    }
                    tableModel.addRow(row);
                } 
            pstmt.close(); 
            }
        pstmt1.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void searchUser(String userType, JTextField searchField) {
        String searchTerm = searchField.getText();
        String query = "";

        switch (userType) {
            case "Customer":
                query = "SELECT U.ID, U.USERNAME, U.EMAIL, C.ADDRESS FROM User U JOIN Customer C ON U.ID = C.ID WHERE U.USERNAME = ? OR U.EMAIL = ?";
                break;
            case "Driver":
                query = "SELECT U.ID, U.USERNAME, U.EMAIL, D.PHONE_NUMBER FROM User U JOIN Driver D ON U.ID = D.ID WHERE U.USERNAME = ? OR U.EMAIL = ?";
                break;
            case "Restaurant":
                query = "SELECT U.ID, U.USERNAME, U.EMAIL, R.NAME FROM User U JOIN Restaurant R ON U.ID = R.ID WHERE U.USERNAME = ? OR U.EMAIL = ?";
                break;
            default:
                return;
        }

        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, searchTerm);
            pstmt.setString(2, searchTerm);
            try (ResultSet rs = pstmt.executeQuery()) {
                DefaultTableModel tableModel;
                switch (userType) {
                    case "Customer":
                        tableModel = customerTableModel;
                        break;
                    case "Driver":
                        tableModel = driverTableModel;
                        break;
                    case "Restaurant":
                        tableModel =  restaurantTableModel;
                        break;
                    default:
                        tableModel = null;
                }

                if (tableModel == null) return;

                tableModel.setRowCount(0);

                while (rs.next()) {
                    Object[] row = new Object[4];
                    row[0] = rs.getInt("ID");
                    row[1] = rs.getString("USERNAME");
                    row[2] = rs.getString("EMAIL");
                    switch (userType) {
                        case "Customer":
                            row[3] = rs.getString("ADDRESS");
                            break;
                        case "Driver":
                            row[3] = rs.getString("PHONE_NUMBER");
                            break;
                        case "Restaurant":
                            row[3] = rs.getString("NAME");
                            break;
                        default:
                            row[3] = "";
                    }

                    tableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addUser(String userType) {
        String username = JOptionPane.showInputDialog(this, "Enter username:");
        String password = JOptionPane.showInputDialog(this, "Enter password:");
        String email = JOptionPane.showInputDialog(this, "Enter email:");
        String address = "";
        String phoneNumber = "";
        String name = "";
        String cuisineType = "";
        String location = "";

        if(username == null || password == null|| email == null) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields");
            return;
        }

        if (!email.matches("^(.+)@(.+)$")) {
            JOptionPane.showMessageDialog(this, "Wrong E-mail format (e.g example@gmail.com)");
            return;
        }

        String query = "INSERT INTO User (USERNAME, PASSWORD, EMAIL, USER_TYPE) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement pstmt = dbConnection.prepareStatement(query);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            pstmt.setString(4, userType);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted < 0) {
                JOptionPane.showMessageDialog(this, "Failed to add user");
                return;
            }
        
            switch(userType){
                case "Customer":
                    address = JOptionPane.showInputDialog(this, "Enter address:");
                    query = "INSERT INTO Customer (USERNAME, PASSWORD, ADDRESS) VALUES (?,?,?)";
                    break;
                case "Driver":
                    phoneNumber = JOptionPane.showInputDialog(this, "Enter phone number:");
                    query = "INSERT INTO Driver (USERNAME, PASSWORD, PHONE_NUMBER) VALUES (?,?,?)";
                    break;
                case "Restaurant":
                    name = JOptionPane.showInputDialog(this, "Enter name:");
                    location = JOptionPane.showInputDialog(this, "Enter location:");
                    cuisineType = JOptionPane.showInputDialog(this, "Enter cuisine type:");
                    query = "INSERT INTO Restaurant(USERNAME, PASSWORD, NAME, LOCATION, CUISINE_TYPE, RATING) VALUES (?,?,?,?,?,0)";
                    break;
            }

            if(address == null || phoneNumber == null || name == null || cuisineType == null || location == null) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields");
                return;
            }

            pstmt = dbConnection.prepareStatement(query);
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            switch(userType){
                case "Customer":
                    pstmt.setString(3, address);
                    break;
                case "Driver":
                    pstmt.setString(3, phoneNumber);
                    break;
                case "Restaurant":
                    pstmt.setString(3, name);
                    pstmt.setString(4, location);
                    pstmt.setString(5, cuisineType);
                    break;
            }

            rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(this, "User added successfully");
                refreshTable(userType);
            }
            
            pstmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } 
    }

    private void editUser(String userType, int selectedRow) {
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a " + userType + " to edit");
            return;
        }
    
        JTable table;
        String username = JOptionPane.showInputDialog(this, "Enter new username:");
        String email = JOptionPane.showInputDialog(this, "Enter new email:");
        String address = "", phoneNumber = "", name = "", cuisineType = "", location = "";
        String query2 = null;
    
        switch (userType) {
            case "Customer":
                table = customerTable;
                address = JOptionPane.showInputDialog(this, "Enter new address:");
                query2 = "UPDATE Customer SET ADDRESS = ? WHERE ID = ?";
                break;
            case "Driver":
                table = driverTable;
                phoneNumber = JOptionPane.showInputDialog(this, "Enter new phone number:");
                query2 = "UPDATE Driver SET PHONE_NUMBER = ? WHERE ID = ?";
                break;
            case "Restaurant":
                table = restaurantTable;
                name = JOptionPane.showInputDialog(this, "Enter new name:");
                location = JOptionPane.showInputDialog(this, "Enter new location:");
                cuisineType = JOptionPane.showInputDialog(this, "Enter new cuisine type:");
                query2 = "UPDATE Restaurant SET NAME = ?, LOCATION = ?, CUISINE_TYPE = ? WHERE ID = ?";
                break;
            default:
                return;
        }
    
        String id = table.getValueAt(selectedRow, 0).toString();
        String query1 = "UPDATE User SET USERNAME = ?, EMAIL = ? WHERE ID = ?";
    
        try {
            PreparedStatement pstmt = dbConnection.prepareStatement(query1);
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setInt(3, Integer.parseInt(id));
    
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                pstmt = dbConnection.prepareStatement(query2);
                int i = 2; // Default for Customer/Driver
                switch (userType) {
                    case "Customer":
                        pstmt.setString(1, address);
                        break;
                    case "Driver":
                        pstmt.setString(1, phoneNumber);
                        break;
                    case "Restaurant":
                        pstmt.setString(1, name);
                        pstmt.setString(2, location);
                        pstmt.setString(3, cuisineType);
                        i = 4; // Adjust for Restaurant
                        break;
                }
                pstmt.setInt(i, Integer.parseInt(id));
                rows = pstmt.executeUpdate();
    
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "User updated successfully");
                    refreshTable(userType);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }    

    private void deleteUser(String userType, int selectedRow) {
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a " + userType + " to delete");
            return;
        }
    
        JTable table;
        switch (userType) {
            case "Customer":
                table = customerTable;
                break;
            case "Driver":
                table = driverTable;
                break;
            case "Restaurant":
                table = restaurantTable;
                break;
            default:
                return;
        }
    
        String id = table.getValueAt(selectedRow, 0).toString();
    
        int confirmDeletion = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this user?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirmDeletion == JOptionPane.YES_OPTION) {
            String deleteUserQuery = "DELETE FROM User WHERE ID = ?";
            try {
                PreparedStatement pstmt = dbConnection.prepareStatement(deleteUserQuery);
                pstmt.setInt(1, Integer.parseInt(id));
    
                int rowsDeleted = pstmt.executeUpdate();
                if(rowsDeleted > 0) {
                    JOptionPane.showMessageDialog(this, "User deleted successfully");
                    refreshTable(userType);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    private void refreshTable(String userType) {
        populateTable(userType);
    }
}