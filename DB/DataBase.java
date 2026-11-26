package DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBase
{
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/ParkingDB"; //ywe can edit this depend on where you run MYSQL Server

    public static Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection(DEFAULT_URL, "root", "4444");
    }

    public static void main(String[] args) 
    {
        // Testing 
        try
        {
            Connection temp = getConnection();
            System.out.println("Connected");
            temp.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}
