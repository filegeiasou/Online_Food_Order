
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;

public class CredentialsHandler {
    public CredentialsHandler(String username, String password) {
        checkEntry(username, password);
    }

    private void checkEntry(String username, String password) {
        Connection dbConnection = DriverManager.getConnection();
    }
}
