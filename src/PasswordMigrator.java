import java.sql.*;
import util.PasswordEncryptor;

public class PasswordMigrator {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/penjadwalan_karyawan";
        String user = "root"; // sesuaikan dengan username database Anda
        String pass = "";     // sesuaikan dengan password database Anda

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            String selectSql = "SELECT id, username, role, password, password_kedua FROM akun";
            String updateSql = "UPDATE akun SET password = ?, password_kedua = ? WHERE id = ?";
            PreparedStatement psSelect = conn.prepareStatement(selectSql);
            PreparedStatement psUpdate = conn.prepareStatement(updateSql);

            ResultSet rs = psSelect.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String role = rs.getString("role");
                String password = rs.getString("password");
                String passwordKedua = rs.getString("password_kedua");

                // Enkripsi kolom password utama jika belum
                String encryptedPassword = password;
                if (!isEncrypted(password)) {
                    encryptedPassword = PasswordEncryptor.encrypt(password);
                    System.out.println("Password utama user ID " + id + " dienkripsi.");
                } else {
                    System.out.println("Password utama user ID " + id + " sudah terenkripsi, skip.");
                }

                // Enkripsi password_kedua jika role adalah manager
                String encryptedPasswordKedua = passwordKedua;
                if ("manager".equalsIgnoreCase(role)) {
                    if (passwordKedua != null && !passwordKedua.isEmpty() && !isEncrypted(passwordKedua)) {
                        encryptedPasswordKedua = PasswordEncryptor.encrypt(passwordKedua);
                        System.out.println("Password kedua untuk manager ID " + id + " dienkripsi.");
                    } else if (passwordKedua == null || passwordKedua.isEmpty()) {
                        System.out.println("Password kedua kosong untuk manager ID " + id + ", lewati.");
                    } else {
                        System.out.println("Password kedua manager ID " + id + " sudah terenkripsi, skip.");
                    }
                }

                // Simpan kembali (baik password utama atau password kedua)
                psUpdate.setString(1, encryptedPassword);
                psUpdate.setString(2, encryptedPasswordKedua);
                psUpdate.setInt(3, id);
                psUpdate.executeUpdate();
            }

            rs.close();
            psSelect.close();
            psUpdate.close();
            System.out.println("Migrasi selesai.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Cek apakah string sudah base64 dan panjang kelipatan 4
    private static boolean isEncrypted(String s) {
        return s != null && s.matches("^[A-Za-z0-9+/=]+$") && s.length() % 4 == 0;
    }
}
