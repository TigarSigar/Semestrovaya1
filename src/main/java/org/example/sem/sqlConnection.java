package org.example.sem;

import java.sql.*;

import static org.example.sem.HelloController.logger;

public class sqlConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/test";
    private static final String USER = "user";
    private static final String PASSWORD = "user";

    public static void dataBase(String city) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "INSERT INTO cityName (id, name) VALUES (?, ?)";

            deleteCity(conn);

            addCity(conn, sql, city, 1);

        } catch (SQLException e) {
            logger.error("Ошибка при добавлении города '{}': {}", city, e.getMessage());
        }
    }
    public static String savedCity() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "SELECT name FROM cityName WHERE id = 1";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString(1);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка при получении сохраненного города: {}", e.getMessage());
        }
        return null;
    }

    private static void deleteCity(Connection conn) throws SQLException {
        String sql = "DELETE FROM cityName";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            logger.info("Все города удалены из таблицы.");
        }
    }

    private static void addCity(Connection conn, String sql, String name, int id) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setString(2, name);
            pstmt.executeUpdate();
            logger.info("Город '{}' с id={} добавлен.", name, id);
        }
    }

}
