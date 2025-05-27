import java.sql.*;
import util.PasswordEncryptor;

public class PasswordMigrator {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/penjadwalan_karyawan";
        String user = "root"; // sesuaikan username db kamu
        String pass = ""; // sesuaikan password db kamu

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            String selectSql = "SELECT id, password FROM akun";
            String updateSql = "UPDATE akun SET password = ? WHERE id = ?";
            PreparedStatement psSelect = conn.prepareStatement(selectSql);
            PreparedStatement psUpdate = conn.prepareStatement(updateSql);

            ResultSet rs = psSelect.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String plainPass = rs.getString("password");

                // encrypt password hanya jika belum terenkripsi
                if (!isEncrypted(plainPass)) {
                    String encryptedPass = PasswordEncryptor.encrypt(plainPass);
                    psUpdate.setString(1, encryptedPass);
                    psUpdate.setInt(2, id);
                    psUpdate.executeUpdate();
                    System.out.println("Password user id " + id + " berhasil dienkripsi.");
                } else {
                    System.out.println("Password user id " + id + " sudah terenkripsi, skip.");
                }
            }
            rs.close();
            psSelect.close();
            psUpdate.close();

            System.out.println("Migrasi password selesai.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Cek apakah password sudah berbentuk base64 AES (kasar)
    private static boolean isEncrypted(String s) {
        // misal: cek apakah string mengandung karakter yang bukan base64 (alphanumeric + + / =)
        return s.matches("^[A-Za-z0-9+/=]+$") && s.length() % 4 == 0;
    }
}
