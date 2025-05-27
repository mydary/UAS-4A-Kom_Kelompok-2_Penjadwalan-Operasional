package dashboard;

import model.User;
import tugas.ChecklistPanel;
import catatan.CatatanPanel;
import login.LoginFrame;
import database.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.TrayIcon;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.sql.*;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class KaryawanDashboard extends JFrame {
    private User user;
    private Timer reminderTimer;

    public KaryawanDashboard(User user) {
        this.user = user;
        setTitle("Dashboard Karyawan - " + user.getNama());
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initUI();
        startReminderChecker();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (reminderTimer != null) reminderTimer.cancel();
            }
        });
    }

    private void initUI() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(4, 1));

        JButton btnTugas = new JButton("Kerjakan Tugas");
        JButton btnCatatan = new JButton("Catatan ke Manager");
        JButton btnProfil = new JButton("Profil");
        JButton btnLogout = new JButton("Logout");

        JPanel contentPanel = new JPanel(new BorderLayout());

        sidebar.add(btnTugas);
        sidebar.add(btnCatatan);
        sidebar.add(btnProfil);
        sidebar.add(btnLogout);

        btnTugas.addActionListener(e -> {
            contentPanel.removeAll();
            contentPanel.add(new ChecklistPanel(user), BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();
        });

        btnCatatan.addActionListener(e -> {
            contentPanel.removeAll();
            contentPanel.add(new CatatanPanel(user.getId(), false), BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();
        });

        btnProfil.addActionListener(e -> {
            contentPanel.removeAll();
            JPanel profile = new JPanel();
            profile.setLayout(new BoxLayout(profile, BoxLayout.Y_AXIS));
            profile.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            profile.add(new JLabel("Nama: " + user.getNama()));
            profile.add(new JLabel("Jabatan: " + user.getJabatan()));

            String fotoPath = user.getFoto();
            if (fotoPath != null && !fotoPath.isEmpty()) {
                try {
                    ImageIcon icon = new ImageIcon(fotoPath);
                    Image img = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
                    JLabel fotoLabel = new JLabel(new ImageIcon(img));
                    fotoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    profile.add(Box.createVerticalStrut(10));
                    profile.add(fotoLabel);
                } catch (Exception ex) {
                    profile.add(new JLabel("Gagal memuat foto."));
                }
            } else {
                profile.add(new JLabel("Tidak ada foto."));
            }

            contentPanel.add(profile, BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();
        });

        btnLogout.addActionListener(e -> {
            if (reminderTimer != null) reminderTimer.cancel();
            dispose();
            new LoginFrame().setVisible(true);
        });

        setLayout(new BorderLayout());
        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    private void startReminderChecker() {
        reminderTimer = new Timer(true);
        reminderTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                cekTugasDanNotifikasi();
            }
        }, 0, 60_000);
    }

    private void cekTugasDanNotifikasi() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT judul, deskripsi
                FROM tugas
                WHERE role = ? AND tanggal_tugas = CURDATE()
                AND TIME(waktu_tugas) BETWEEN SUBTIME(CURTIME(), '00:01:00') AND ADDTIME(CURTIME(), '00:01:00')
            """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, user.getJabatan());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String judul = rs.getString("judul");
                String deskripsi = rs.getString("deskripsi");

                // Simpan ke variabel final supaya aman dari delay
                final String finalJudul = judul;
                final String finalDeskripsi = deskripsi;

                // Jalankan notifikasi di thread terpisah
                new Thread(() -> tampilkanNotifikasi("Tugas: " + finalJudul, finalDeskripsi)).start();
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
