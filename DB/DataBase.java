package DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBase
{
    private static final String DEFAULT_URL =
        "jdbc:sqlserver://localhost:1433;databaseName=ParkingDB;encrypt=true;trustServerCertificate=true"; //you can edit this depend on where you run MYSQL Server

    public static Connection getConnection() throws SQLException
    {
        String url = System.getenv("PARKING_DB_URL");
        String user = System.getenv("PARKING_DB_USER");
        String password = System.getenv("PARKING_DB_PASSWORD");

        if (url == null || url.isBlank())
        {
            url = DEFAULT_URL;
        }
        if (user == null || password == null)
        {
            throw new SQLException("Set the PARKING_DB_USER and PARKING_DB_PASSWORD environment variables.");
        }

        return DriverManager.getConnection(url, user, password);
    }
}
