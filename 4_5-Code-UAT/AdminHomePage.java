
import com.mysql.cj.log.Log;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminHomePage extends JFrame {
    private JTabbedPane tabbedPane;
    private JTable customerTable, driverTable, restaurantTable;
    private DefaultTableModel customerTableModel, driverTableModel, restaurantTableModel;

    Connection dbConnection;
    String url = "jdbc:mysql://localhost:3306/Online_Food_Order_Delivery";
    String user = "root";
    String password = "root";

    public AdminHomePage(String username) {
        try {
            dbConnection = DriverManager.getConnection(url, user, password);
        } catch (SQLException se) {
            se.printStackTrace();
        }

        setTitle("Admin Panel");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        AppLogo appLogo = new AppLogo();

        JPanel logoPanel = new JPanel();
        logoPanel.add(appLogo.getLabel());

        tabbedPane = new JTabbedPane();

        // Create and add tabs
        tabbedPane.addTab("Customers", createUserPanel("Customer"));
        tabbedPane.addTab("Drivers", createUserPanel("Driver"));
        tabbedPane.addTab("Restaurants", createUserPanel("Restaurant"));

        setLayout(new BorderLayout());
        logoPanel.setBackground(new Color(0xe7a780));
        add(logoPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        add(tabbedPane);

        setVisible(true);
    }

    private JPanel createUserPanel(String userType) {
        JPanel panel = new JPanel(new BorderLayout());

        // Table for displaying users
        JTable userTable = new JTable();
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

        // Control panel with buttons and search bar
        JPanel controlPanel = new JPanel();
        JTextField searchField = new JTextField(15);
        JButton searchButton = new JButton("Search");
        JButton addButton = new JButton("Add");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        JButton refreshButton = new JButton("Refresh");
        JButton logOutButton = new JButton("Log Out");

        searchButton.setEnabled(false);

        controlPanel.add(new JLabel("Search"));
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
        String query;

        switch (userType) {
            case "Customer":
                query = "SELECT U.ID, U.USERNAME, U.EMAIL, C.ADDRESS FROM User U JOIN Customer C ON U.ID = C.ID";
                break;
            case "Driver":
                query = "SELECT U.ID, U.USERNAME, U.EMAIL, D.PHONE_NUMBER FROM User U JOIN Driver D ON U.ID = D.ID";
                break;
            case "Restaurant":
                query = "SELECT U.ID, U.USERNAME, U.EMAIL, R.NAME FROM User U JOIN Restaurant R ON U.ID = R.ID";
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

        try (Statement stmt = dbConnection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
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
        // Implement delete logic here based on userType
        JOptionPane.showMessageDialog(this, "Delete " + userType);
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
