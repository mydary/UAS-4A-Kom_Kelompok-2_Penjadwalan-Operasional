package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Konfigurasi koneksi database
    private static final String DB_URL = "jdbc:mysql://localhost:3306/penjadwalan_karyawan";
    private static final String DB_USER = "root"; // sesuaikan dengan user MySQL kamu
    private static final String DB_PASSWORD = ""; // sesuaikan jika ada password

    private static Connection connection;

    // Method untuk mendapatkan koneksi tunggal (singleton)
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("Koneksi ke database berhasil.");
            } catch (ClassNotFoundException e) {
                System.err.println("Driver JDBC tidak ditemukan.");
                e.printStackTrace();
            } catch (SQLException e) {
                System.err.println("Gagal koneksi ke database.");
                e.printStackTrace();
                throw e;
            }
        }
        return connection;
    }
}
