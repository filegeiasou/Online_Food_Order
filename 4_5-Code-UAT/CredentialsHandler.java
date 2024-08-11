
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CredentialsHandler {
    public CredentialsHandler(String username, String password) {
        checkEntry(username, password);
    }

    private void checkEntry(String username, String password) {
        try {
            Connection dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/Online_Food_Order_Delivery", "root", "tsomis");

            String checkQuery = "SELECT * FROM USER WHERE USERNAME = ? AND PASSWORD = ?";

            PreparedStatement preparedStatement = dbConnection.prepareStatement(checkQuery);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()) {
                System.out.println("Results retrieved successfully");
                System.out.println("Username: " + resultSet.getString("USERNAME") + " with password: " + resultSet.getString("PASSWORD"));
            }

            resultSet.close();
            preparedStatement.close();
            dbConnection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
