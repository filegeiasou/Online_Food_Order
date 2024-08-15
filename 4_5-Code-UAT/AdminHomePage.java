import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class AdminHomePage extends JFrame {
    private JTabbedPane tabbedPane;
    private JTable customerTable, driverTable, restaurantTable;
    private DefaultTableModel customerTableModel, driverTableModel, restaurantTableModel;
    Connection dbConnection;

    public AdminHomePage(String username) {
        CredentialsHandler cHandler = new CredentialsHandler();
        dbConnection = cHandler.getDBConnection();

        setTitle("Admin Panel");
        setLayout(new BorderLayout());
        
        AppLogo appLogo = new AppLogo();
        JPanel logoPanel = new JPanel();
        logoPanel.add(appLogo.getLabel());
        logoPanel.setBackground(new Color(0xe7a780));

        tabbedPane = new JTabbedPane();

        // Create and add tabs
        tabbedPane.addTab("Customers", createUserPanel("Customer"));
        tabbedPane.addTab("Drivers", createUserPanel("Driver"));
        tabbedPane.addTab("Restaurants", createUserPanel("Restaurant"));

        add(logoPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(tabbedPane);

        setVisible(true);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private JPanel createUserPanel(String userType) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0x575658)); 
        panel.setBorder(BorderFactory.createLineBorder(new Color(0x575658))); 

        // Table for displaying users
        JTable userTable = new JTable();
        userTable.setBackground(new Color(0x575658)); 
        userTable.setForeground(Color.WHITE); 
        userTable.setGridColor(Color.WHITE);

        // Set table header (ID, Username, etc.) background to black and text to white
        JTableHeader tableHeader = userTable.getTableHeader();
        tableHeader.setBackground(new Color(0x575658)); 
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

        // Set buttons background to black and text to white
        JButton[] buttons = {searchButton, addButton, editButton, deleteButton, refreshButton, logOutButton};
        for (JButton button : buttons) {
            button.setBackground(Color.WHITE);
            button.setForeground(Color.BLACK);
        }
        searchButton.setEnabled(false);

        JLabel searchLabel = new JLabel("Search");
        searchLabel.setForeground(Color.WHITE); 
        controlPanel.add(searchLabel);
        controlPanel.add(searchField);
        controlPanel.add(searchButton);
        controlPanel.add(addButton);
        controlPanel.add(editButton);
        controlPanel.add(deleteButton);
        controlPanel.add(refreshButton);
        controlPanel.add(logOutButton);

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

        populateTable("Customer");
        populateTable("Driver");
        populateTable("Restaurant");

        return panel;
    }

    private void populateTable(String userType) {
        String userQuery = "SELECT ID, USERNAME, EMAIL FROM User WHERE USERNAME IN (SELECT USERNAME FROM " + userType + ")";
        String additionalQuery = "";

        switch (userType) {
            case "Customer":
                additionalQuery = "SELECT ADDRESS FROM Customer WHERE USERNAME = ?";
                break;
            case "Driver":
                additionalQuery = "SELECT PHONE_NUMBER FROM Driver WHERE USERNAME = ?";
                break;
            case "Restaurant":
                additionalQuery = "SELECT NAME FROM Restaurant WHERE USERNAME = ?";
                break;
            default:
                return;
        }

        DefaultTableModel tableModel;
        switch (userType) {
            case "Customer":
                tableModel = customerTableModel;
                break;
            case "Driver":
                tableModel = driverTableModel;
                break;
            case "Restaurant":
                tableModel = restaurantTableModel;
                break;
            default:
                tableModel = null;
        }

        if (tableModel == null) return;

        tableModel.setRowCount(0);

        try (Statement stmt = dbConnection.createStatement(); ResultSet rs = stmt.executeQuery(userQuery)) {
            while (rs.next()) {
                int id = rs.getInt("ID");
                String username = rs.getString("USERNAME");
                String email = rs.getString("EMAIL");

                try (PreparedStatement pstmt = dbConnection.prepareStatement(additionalQuery)) {
                    pstmt.setString(1, username);
                    try (ResultSet additionalRs = pstmt.executeQuery()) {
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
                    }
                }
            }
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
        // Implement add logic here based on userType
        JOptionPane.showMessageDialog(this, "Add new " + userType);
        SwingUtilities.invokeLater(this::launchAddForm);
    }

    private void editUser(String userType, int selectedRow) {
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a " + userType + " to edit");
            return;
        }
        // Implement edit logic here based on userType
        JOptionPane.showMessageDialog(this, "Edit " + userType);
    }

    private void deleteUser(String userType, int selectedRow) {
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a " + userType + " to delete");
            return;
        }

        JTable userTable;
        switch (userType) {
            case "Customer":
                userTable = customerTable;
                break;
            case "Driver":
                userTable = driverTable;
                break;
            case "Restaurant":
                userTable = restaurantTable;
                break;
            default:
                return;
        }

        String id = userTable.getValueAt(selectedRow, 0).toString();

        // Implement delete logic here based on userType
        int confirmDeletion = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this user?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirmDeletion == JOptionPane.YES_OPTION) {
            // Implement the deletion logic here based on ID or any unique identifier
            String deleteUserQuery = "DELETE FROM User WHERE ID = ?";
            try {
                PreparedStatement pstmt = dbConnection.prepareStatement(deleteUserQuery);
                pstmt.setInt(1, Integer.parseInt(id));

                int rowsDeleted = pstmt.executeUpdate();
                if(rowsDeleted > 0) {
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

    private void launchAddForm() {
//        LogInForm addUserForm = new LogInForm();
//        addUserForm.getCardLayout().show(addUserForm.getMainPanel(), "Registration");
//        addUserForm.getDynamicPanel().setVisible(true);
//        addUserForm.handleRegistration();
    }
}