package login;

import database.DatabaseConnection;
import dashboard.ManagerDashboard;
import dashboard.KaryawanDashboard;
import model.User;
import util.PasswordEncryptor;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;

public class LoginFrame extends JFrame {
    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private JComboBox<String> comboRole;
    private Timer globalReminderTimer;

    public LoginFrame() {
        setTitle("Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        startGlobalReminderChecker();
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        tfUsername = new JTextField();
        pfPassword = new JPasswordField();
        comboRole = new JComboBox<>(new String[]{
                "Manager", "Kasir", "Crew Ice cream", "Staff Dapur", "Staff Gudang", "Staff Kebersihan"
        });

        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(new JLabel("Username:"));
        panel.add(tfUsername);
        panel.add(new JLabel("Password:"));
        panel.add(pfPassword);
        panel.add(new JLabel("Role:"));
        panel.add(comboRole);

        JButton btnLogin = new JButton("Login");
        btnLogin.addActionListener(e -> login());
        panel.add(new JLabel());
        panel.add(btnLogin);

        add(panel);
    }

    private void login() {
        String username = tfUsername.getText().trim();
        String inputPassword = new String(pfPassword.getPassword());
        String selectedRole = comboRole.getSelectedItem().toString();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT a.id, a.username, a.password, a.role, p.nama, p.jabatan, p.foto
                FROM akun a
                JOIN profil p ON a.id = p.id
                WHERE a.username = ? AND a.role = ?
            """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, selectedRole);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String encryptedPassword = rs.getString("password");
                String decryptedPassword = PasswordEncryptor.decrypt(encryptedPassword);

                if (inputPassword.equals(decryptedPassword)) {
                    User user = new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("role"),
                            rs.getString("nama"),
                            rs.getString("jabatan"),
                            rs.getString("foto")
                    );

                    JOptionPane.showMessageDialog(this, "Login berhasil sebagai " + selectedRole);

                    if (globalReminderTimer != null) globalReminderTimer.cancel();

                    if ("Manager".equalsIgnoreCase(selectedRole)) {
                        new ManagerDashboard(user).setVisible(true);
                    } else {
                        new KaryawanDashboard(user).setVisible(true);
                    }

                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Password salah!");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Login gagal! Username atau role salah.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat login.");
        }
    }

    private void startGlobalReminderChecker() {
        globalReminderTimer = new Timer(true);
        globalReminderTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                cekSemuaTugasDanNotifikasi();
            }
        }, 0, 60_000);
    }

    private void cekSemuaTugasDanNotifikasi() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT judul, deskripsi, role
                FROM tugas
                WHERE tanggal_tugas = CURDATE()
                AND TIME(waktu_tugas) BETWEEN SUBTIME(CURTIME(), '00:01:00') AND ADDTIME(CURTIME(), '00:01:00')
            """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String judul = rs.getString("judul");
                String deskripsi = rs.getString("deskripsi");
                String role = rs.getString("role");

                final String title = "Tugas untuk " + role + ": " + judul;
                final String message = deskripsi;

                new Thread(() -> tampilkanNotifikasi(title, message)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tampilkanNotifikasi(String title, String message) {
        if (!SystemTray.isSupported()) return;

        SystemTray tray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().createImage("");
        TrayIcon trayIcon = new TrayIcon(image, "Notifikasi Tugas");
        trayIcon.setImageAutoSize(true);

        try {
            tray.add(trayIcon);
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
            Thread.sleep(5000);
            tray.remove(trayIcon);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
