import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class CredentialsHandler {

    private Connection dbConnection;
    private String url = "jdbc:mysql://localhost:3306/Online_Food_Order_Delivery";
    private String user = "root";
    private String password = "root";

    public CredentialsHandler() {
        try {
            dbConnection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getDBConnection() {
        return dbConnection;
    }
    // Inserts into USER table and returns the generated ID
    private int insertUser(String username, String password, String email, String userType) throws SQLException {
        String regUserQuery = "INSERT INTO USER(USERNAME, PASSWORD, EMAIL, USER_TYPE) VALUES (?, ?, ?, ?);";
        PreparedStatement regUserStatement = dbConnection.prepareStatement(regUserQuery, Statement.RETURN_GENERATED_KEYS);
        regUserStatement.setString(1, username);
        regUserStatement.setString(2, password);
        regUserStatement.setString(3, email);
        regUserStatement.setString(4, userType);

        int userRowsInserted = regUserStatement.executeUpdate();
        // if user inserted successfully
        if (userRowsInserted > 0) {
            // generate the key for the inserting user
            try (ResultSet generatedKeys = regUserStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // return the ID (primary key) of the registration
                }
            }
        }
        regUserStatement.close();
        return -1; // Indicate failure to get ID
    }

    public Map<String, String> loginUser(String email, String password) {
        Map<String, String> result = new HashMap<>();
        try {
            String checkQuery = "SELECT ID, USERNAME, USER_TYPE FROM USER WHERE EMAIL = ? AND PASSWORD = ?;";
            PreparedStatement preparedStatement = dbConnection.prepareStatement(checkQuery);
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                result.put("USER_ID", resultSet.getString("ID"));
                result.put("USER_TYPE", resultSet.getString("USER_TYPE"));
                result.put("USERNAME", resultSet.getString("USERNAME"));
            } else {
                result.put("USER_ID", null);
                result.put("USER_TYPE", null);
                result.put("USERNAME", null);
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean registerCustomer(String username, String password, String email, String address) {
        boolean regStatus = false;
        try {
            if (checkExisting(email)) return false;

            // Start transaction
            dbConnection.setAutoCommit(false);

            // Get the ID of the user that was inserted, and use the same key to register the customer.
            // That way, the user registered as a customer, has the same ID on the User as well as the Customer table.
            // ** Same logic applies for the other registration methods. **
            int userId = insertUser(username, password, email, "Customer");
            if (userId != -1) {
                String regCustomerQuery = "INSERT INTO CUSTOMER(ID, USERNAME, PASSWORD, ADDRESS) VALUES (?, ?, ?, ?);";
                PreparedStatement regCustomerStatement = dbConnection.prepareStatement(regCustomerQuery);
                regCustomerStatement.setInt(1, userId);
                regCustomerStatement.setString(2, username);
                regCustomerStatement.setString(3, password);
                regCustomerStatement.setString(4, address);

                int rowsInserted = regCustomerStatement.executeUpdate();
                if (rowsInserted > 0) {
                    regStatus = true;
                }

                regCustomerStatement.close();
            }

            if (regStatus) {
                dbConnection.commit(); // Commit transaction if successful
            } else {
                dbConnection.rollback(); // Rollback if any part fails
            }

            dbConnection.setAutoCommit(true); // Reset to default

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                dbConnection.rollback(); // Rollback in case of an error
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return regStatus;
    }

    public boolean registerDriver(String username, String password, String email, String phoneNumber) {
        boolean regStatus = false;
        try {
            if (checkExisting(email)) return false;

            dbConnection.setAutoCommit(false);

            int userId = insertUser(username, password, email, "Driver");
            if (userId != -1) {
                String regDriverQuery = "INSERT INTO DRIVER(ID, USERNAME, PASSWORD, PHONE_NUMBER) VALUES (?, ?, ?, ?);";
                PreparedStatement regDriverStatement = dbConnection.prepareStatement(regDriverQuery);
                regDriverStatement.setInt(1, userId);
                regDriverStatement.setString(2, username);
                regDriverStatement.setString(3, password);
                regDriverStatement.setString(4, phoneNumber);

                int rowsInserted = regDriverStatement.executeUpdate();
                if (rowsInserted > 0) {
                    regStatus = true;
                }

                regDriverStatement.close();
            }

            if (regStatus) {
                dbConnection.commit();
            } else {
                dbConnection.rollback();
            }

            dbConnection.setAutoCommit(true);

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                dbConnection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return regStatus;
    }

    public boolean registerRestaurant(String username, String password, String email, String restaurantName, String cuisineType, String location) {
        boolean regStatus = false;
        try {
            if (checkExisting(email)) return false;

            dbConnection.setAutoCommit(false);

            int userId = insertUser(username, password, email, "Restaurant");
            if (userId != -1) {
                String regRestaurantQuery = "INSERT INTO RESTAURANT(ID, USERNAME, PASSWORD, NAME, CUISINE_TYPE, LOCATION) VALUES (?, ?, ?, ?, ?, ?);";
                PreparedStatement regRestaurantStatement = dbConnection.prepareStatement(regRestaurantQuery);
                regRestaurantStatement.setInt(1, userId);
                regRestaurantStatement.setString(2, username);
                regRestaurantStatement.setString(3, password);
                regRestaurantStatement.setString(4, restaurantName);
                regRestaurantStatement.setString(5, cuisineType);
                regRestaurantStatement.setString(6, location);

                int rowsInserted = regRestaurantStatement.executeUpdate();
                if (rowsInserted > 0) {
                    regStatus = true;
                }

                regRestaurantStatement.close();
            }

            if (regStatus) {
                dbConnection.commit();
            } else {
                dbConnection.rollback();
            }

            dbConnection.setAutoCommit(true);

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                dbConnection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return regStatus;
    }

    public boolean checkExisting(String email) throws SQLException {
        String checkQuery = "SELECT * FROM USER WHERE EMAIL = ?";
        PreparedStatement checkAlreadyExisting = dbConnection.prepareStatement(checkQuery);
        checkAlreadyExisting.setString(1, email);

        ResultSet checkSet = checkAlreadyExisting.executeQuery();
        boolean exists = checkSet.isBeforeFirst();

        checkSet.close();
        checkAlreadyExisting.close();

        return exists;
    }
}
