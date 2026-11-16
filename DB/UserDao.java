package DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao
{
    /* One row of the users table. carNumber is the plate of the user's first car (or null). */
    public static class UserAccount
    {
        private int id;
        private String username;
        private String role;
        private String carNumber;

        public UserAccount(int id, String username, String role, String carNumber)
        {
            this.id = id;
            this.username = username;
            this.role = role;
            this.carNumber = carNumber;
        }

        public int getId()
        {
            return id;
        }

        public String getUsername()
        {
            return username;
        }

        public String getRole()
        {
            return role;
        }

        public String getCarNumber()
        {
            return carNumber;
        }
    }

    public UserAccount findByUsername(String username)
    {
        String sql = "SELECT u.id, u.username, u.role, "
                   + "(SELECT TOP 1 c.plate_number FROM cars c WHERE c.user_id = u.id ORDER BY c.id) AS car_number "
                   + "FROM users u WHERE u.username = ?";

        try 
        {
            Connection con = DataBase.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
   
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery())
            {
                if (!rs.next())
                {
                    return null;
                }
                return new UserAccount(rs.getInt("id"), rs.getString("username"),rs.getString("role"), rs.getString("car_number"));
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public boolean checkPassword(String username, String password)
    {
        String sql = "SELECT password_hash FROM users WHERE username = ?";

        try (Connection con = DataBase.getConnection(); PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery())
            {
                if (!rs.next())
                {
                    return false;
                }
                // compared in Java, because SQL Server ignores upper/lower case when it compares text
                return rs.getString("password_hash").equals(password);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public boolean createUser(String username, String password, String role)
    {
        if (!role.equals("user") && !role.equals("monitor"))
        {
            throw new IllegalArgumentException("role must be \"user\" or \"monitor\"");
        }

        String sql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)";

        try (Connection con = DataBase.getConnection();PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);
            ps.executeUpdate();
            return true;
        }
        catch (SQLException e)
        {
            if (e.getErrorCode() == 2627 || e.getErrorCode() == 2601)
            {
                return false;
            }
            throw new RuntimeException(e);
        }
    }
}