package DB;

import Parking_Classes.*;
import java.sql.*;
import java.time.LocalDateTime;
import  java.util.*;

public class ParkingDao
{
    /* Helper used only while reading the lots query. */
    private static class LotRow
    {
        String name;
        String addr;
        List<String> codes = new ArrayList<>();
        Set<String> taken = new HashSet<>();
    }

    /*
     * Build every lot with all its slots, using your ParkingManager.
     * A slot with is_taken = 1 is marked taken and kept out of the available list,
     * so the objects match the database.
     */
    public List<Parking> loadAllLots()
    {
        String sql = "SELECT l.id, l.name, l.address, s.space_code, s.is_taken "
                   + "FROM parking_lots l LEFT JOIN park_spaces s ON s.lot_id = l.id "
                   + "ORDER BY l.id, s.id";

        Map<Integer, LotRow> rows = new LinkedHashMap<>();

        try (Connection con = DataBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
            {
                LotRow row = rows.get(rs.getInt("id"));
                if (row == null)
                {
                    row = new LotRow();
                    row.name = rs.getString("name");
                    row.addr = rs.getString("address");
                    rows.put(rs.getInt("id"), row);
                }

                String code = rs.getString("space_code");  // null when the lot has no slots
                if (code != null)
                {
                    row.codes.add(code);
                    if (rs.getBoolean("is_taken"))
                    {
                        row.taken.add(code);
                    }
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        List<Parking> lots = new ArrayList<>();
        for (LotRow row : rows.values())
        {
            lots.add(buildLot(row.name, row.addr, row.codes, row.taken));
        }
        return lots;
    }

    /*
     * Creates the lot, then marks the taken slots using only methods your classes already have.
     * (package-private on purpose, so it can be tested without a database)
     */
    static Parking buildLot(String name, String addr, List<String> codes, Set<String> takenCodes)
    {
        ParkingManager lot = new ParkingManager(name, addr, codes.toArray(new String[0]));

        // Every slot starts free, in the same order as codes. Take them all out one by one:
        // a taken slot stays out (and is marked taken), a free slot is remembered.
        List<ParkSpace> free = new ArrayList<>();
        for (int i = 0; i < codes.size(); i++)
        {
            ParkSpace space = lot.takeParkSpace();

            if (takenCodes.contains(space.getID()))
            {
                space.setIsTaken(true);
            }
            else
            {
                free.add(space);
            }
        }

        // addAvailableParkSpace adds at the front, so add backwards to keep the original order
        for (int i = free.size() - 1; i >= 0; i--)
        {
            lot.addAvailableParkSpace(free.get(i));
        }

        return lot;
    }

    /* Returns the car (with its balance, not parked), or null if the plate is unknown. */
    public Car loadCar(String number)
    {
        String sql = "SELECT plate_number, owner_name, car_type, balance FROM cars WHERE plate_number = ?";

        try (Connection con = DataBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1, number);

            try (ResultSet rs = ps.executeQuery())
            {
                if (!rs.next())
                {
                    return null;
                }
                return new Car(rs.getString("car_type"), rs.getString("owner_name"),
                               rs.getString("plate_number"), rs.getInt("balance"));
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    /*
     * Cars that are parked right now. Each row is {carNumber, lotName, spaceId, sessionId, startTime}.
     * ParkingService uses this at startup to put the cars back in their slots.
     */
    public List<String[]> loadOpenSessions()
    {
        String sql = "SELECT c.plate_number, l.name, sp.space_code, s.id, s.start_time "
                   + "FROM sessions s "
                   + "JOIN cars c ON c.id = s.car_id "
                   + "JOIN park_spaces sp ON sp.id = s.space_id "
                   + "JOIN parking_lots l ON l.id = sp.lot_id "
                   + "WHERE s.end_time IS NULL";

        List<String[]> list = new ArrayList<>();

        try (Connection con = DataBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
            {
                list.add(new String[] {
                    rs.getString(1),
                    rs.getString(2),
                    rs.getString(3),
                    String.valueOf(rs.getInt(4)),
                    rs.getTimestamp(5).toLocalDateTime().toString()
                });
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return list;
    }

    public void saveSpaceStatus(String lotName, String spaceId, boolean taken)
    {
        String sql = "UPDATE park_spaces SET is_taken = ? "
                   + "WHERE space_code = ? AND lot_id = (SELECT id FROM parking_lots WHERE name = ?)";

        try (Connection con = DataBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setBoolean(1, taken);
            ps.setString(2, spaceId);
            ps.setString(3, lotName);

            if (ps.executeUpdate() == 0)
            {
                throw new IllegalArgumentException("No slot " + spaceId + " in lot " + lotName);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    /* Opens a session (end_time stays NULL) and returns its id. */
    public int startSession(String carNumber, String lotName, String spaceId, LocalDateTime start)
    {
        String sql = "INSERT INTO sessions (space_id, car_id, start_time) "
                   + "SELECT sp.id, c.id, ? "
                   + "FROM park_spaces sp "
                   + "JOIN parking_lots l ON l.id = sp.lot_id "
                   + "CROSS JOIN cars c "
                   + "WHERE l.name = ? AND sp.space_code = ? AND c.plate_number = ?";

        try (Connection con = DataBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            ps.setTimestamp(1, Timestamp.valueOf(start));
            ps.setString(2, lotName);
            ps.setString(3, spaceId);
            ps.setString(4, carNumber);

            if (ps.executeUpdate() == 0)
            {
                throw new IllegalArgumentException("Unknown car " + carNumber + " or slot " + spaceId + " in " + lotName);
            }

            try (ResultSet keys = ps.getGeneratedKeys())
            {
                keys.next();
                return keys.getInt(1);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void endSession(int sessionId, LocalDateTime end, int fare)
    {
        String sql = "UPDATE sessions SET end_time = ?, fare = ? WHERE id = ? AND end_time IS NULL";

        try (Connection con = DataBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setTimestamp(1, Timestamp.valueOf(end));
            ps.setInt(2, fare);
            ps.setInt(3, sessionId);
            ps.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void updateBalance(String carNumber, int newBalance)
    {
        String sql = "UPDATE cars SET balance = ? WHERE plate_number = ?";

        try (Connection con = DataBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setInt(1, newBalance);
            ps.setString(2, carNumber);
            ps.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    /* Finished visits of one car, newest first. Each row is {lotName, spaceId, start, end, fare}. */
    public List<String[]> getHistory(String carNumber)
    {
        String sql = "SELECT l.name, sp.space_code, s.start_time, s.end_time, s.fare "
                   + "FROM sessions s "
                   + "JOIN cars c ON c.id = s.car_id "
                   + "JOIN park_spaces sp ON sp.id = s.space_id "
                   + "JOIN parking_lots l ON l.id = sp.lot_id "
                   + "WHERE c.plate_number = ? AND s.end_time IS NOT NULL "
                   + "ORDER BY s.start_time DESC";

        List<String[]> list = new ArrayList<>();

        try (Connection con = DataBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1, carNumber);

            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    list.add(new String[] {
                        rs.getString(1),
                        rs.getString(2),
                        rs.getTimestamp(3).toLocalDateTime().toString(),
                        rs.getTimestamp(4).toLocalDateTime().toString(),
                        String.valueOf(rs.getInt(5))
                    });
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return list;
    }
}