
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class CredentialsHandler {

    Connection dbConnection;

    public CredentialsHandler() {
        try {
            dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/Online_Food_Order_Delivery", "root", "tsomis");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean insertUser(String username, String password, String email, String userType) throws SQLException {
        String regUserQuery = "INSERT INTO USER(USERNAME, PASSWORD, EMAIL, USER_TYPE) VALUES (?, ?, ?, ?);";
        PreparedStatement regUserStatement = dbConnection.prepareStatement(regUserQuery);
        regUserStatement.setString(1, username);
        regUserStatement.setString(2, password);
        regUserStatement.setString(3, email);
        regUserStatement.setString(4, userType);

        int userRowsInserted = regUserStatement.executeUpdate();
        regUserStatement.close();

        return userRowsInserted > 0;
    }

    public Map<String, String> loginUser(String username, String password) {
        Map<String, String> result = new HashMap<>();
        try {
            // Fetch user details with matching username and password
            String checkQuery = "SELECT ID, USER_TYPE FROM USER WHERE EMAIL = ? AND PASSWORD = ?;";
            PreparedStatement preparedStatement = dbConnection.prepareStatement(checkQuery);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();

            // Check if there's more than one match
            if (resultSet.next()) {
                // Assuming the first result is correct or handle ambiguity
                result.put("USER_ID", resultSet.getString("ID"));
                result.put("USER_TYPE", resultSet.getString("USER_TYPE"));
            } else {
                // In this case the user was not found
                result.put("USER_ID", null);
                result.put("USER_TYPE", null);
            }

            resultSet.close();
            preparedStatement.close();
            dbConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

//    public boolean registerUser (String username, String password) {
//        boolean regStatus = false;
//
//        try {
//            Connection dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/Online_Food_Order_Delivery", "root", "tsomis");
//
//            String checkExistingQuery = "SELECT * FROM USER WHERE USERNAME = ? AND PASSWORD = ?;";
//            PreparedStatement preparedStatement = dbConnection.prepareStatement(checkExistingQuery);
//            preparedStatement.setString(1, username);
//            preparedStatement.setString(2, password);
//
//            ResultSet resultSet = preparedStatement.executeQuery();
//            if(!resultSet.isBeforeFirst()) {
//                // if the user does not already exist
//                String regQuery = "INSERT INTO USER VALUES (?, ?);";
//                PreparedStatement insertStatement = dbConnection.prepareStatement(regQuery);
//                insertStatement.setString(1, username);
//                insertStatement.setString(2, password);
//
//                int rowsInserted = insertStatement.executeUpdate();
//                if(rowsInserted > 0) {
//                    regStatus = true;
//                }
//
//                insertStatement.close();
//            }
//
//            resultSet.close();
//            preparedStatement.close();
//            dbConnection.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        return regStatus;
//    }

    public boolean registerCustomer(String username, String password, String email, String address) {
        boolean regStatus = false;

        try {
            if(checkExisting(email)) return false;

            if(insertUser(username, password, email, "Customer")) {

                // If user was successfully inserted, insert into the CUSTOMER table
                String regCustomerQuery = "INSERT INTO CUSTOMER(USERNAME, PASSWORD, ADDRESS) VALUES (?, ?, ?);";
                PreparedStatement regCustomerStatement = dbConnection.prepareStatement(regCustomerQuery);
                regCustomerStatement.setString(1, username);
                regCustomerStatement.setString(2, password);
                regCustomerStatement.setString(3, address);

                int RowsInserted = regCustomerStatement.executeUpdate();
                if (RowsInserted > 0) {
                    regStatus = true;
                }

                regCustomerStatement.close();
            }

            dbConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return regStatus;
    }

    public boolean registerDriver (String username, String password, String email, String phoneNumber) {
        boolean regStatus = false;

        try {
            if(checkExisting(email)) return false;

            if(insertUser(username, password, email, "Driver")) {
                // If user was successfully inserted, insert into the CUSTOMER table
                String regDriverQuery = "INSERT INTO DRIVER(USERNAME, PASSWORD, PHONE_NUMBER) VALUES (?, ?, ?);";
                PreparedStatement regDriverStatement = dbConnection.prepareStatement(regDriverQuery);
                regDriverStatement.setString(1, username);
                regDriverStatement.setString(2, password);
                regDriverStatement.setString(3, phoneNumber);

                int RowsInserted = regDriverStatement.executeUpdate();
                if (RowsInserted > 0) {
                    regStatus = true;
                }

                regDriverStatement.close();
            }

            dbConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return regStatus;
    }

    public boolean registerRestaurant (String username, String password, String email, String restaurantName, String cuisineType, String location) {
        boolean regStatus = false;

        try {
            if(checkExisting(email)) return false;

            if(insertUser(username, password, email, "Restaurant")) {
                // If user was successfully inserted, insert into the CUSTOMER table
                String regRestaurantQuery = "INSERT INTO RESTAURANT(USERNAME, PASSWORD, NAME, CUISINE_TYPE, LOCATION) VALUES (?, ?, ?, ?, ?);";
                PreparedStatement regRestaurantStatement = dbConnection.prepareStatement(regRestaurantQuery);
                regRestaurantStatement.setString(1, username);
                regRestaurantStatement.setString(2, password);
                regRestaurantStatement.setString(3, restaurantName);
                regRestaurantStatement.setString(4, cuisineType);
                regRestaurantStatement.setString(5, location);

                int RowsInserted = regRestaurantStatement.executeUpdate();
                if (RowsInserted > 0) {
                    regStatus = true;
                }

                regRestaurantStatement.close();
            }

            dbConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return regStatus;
    }

    public boolean checkExisting(String email) throws SQLException {
        Connection dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/Online_Food_Order_Delivery", "root", "tsomis");

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
