
import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CredentialsHandler {

    private boolean exists = false;
//    private String username, password;

    public CredentialsHandler() {
    }

    public boolean checkEntry(String username, String password) {
        try {
            Connection dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/Online_Food_Order_Delivery", "root", "tsomis");

            String checkQuery = "SELECT * FROM USER WHERE USERNAME = ? AND PASSWORD = ?;";

            PreparedStatement preparedStatement = dbConnection.prepareStatement(checkQuery);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()) {
                System.out.println("User " + resultSet.getString("USERNAME") + " exists with password: " + resultSet.getString("PASSWORD"));
                exists = true;
            }

            resultSet.close();
            preparedStatement.close();
            dbConnection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return exists;
    }

    public boolean registerUser (String username, String password) {
        boolean regStatus = false;

        try {
            Connection dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/Online_Food_Order_Delivery", "root", "tsomis");

            String checkDuplicateQuery = "SELECT * FROM USER WHERE USERNAME = ? AND PASSWORD = ?;";
            PreparedStatement preparedStatement = dbConnection.prepareStatement(checkDuplicateQuery);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();
            if(!resultSet.isBeforeFirst()) {
                // if the user does not already exist
                String regQuery = "INSERT INTO USER VALUES (?, ?);";
                PreparedStatement insertStatement = dbConnection.prepareStatement(regQuery);
                insertStatement.setString(1, username);
                insertStatement.setString(2, password);

                int rowsInserted = insertStatement.executeUpdate();
                if(rowsInserted > 0) {
                    regStatus = true;
                }

                insertStatement.close();
            }

            resultSet.close();
            preparedStatement.close();
            dbConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return regStatus;
    }
}
