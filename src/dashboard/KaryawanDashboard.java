package dashboard;

import model.User;
import tugas.ChecklistPanel;
import catatan.CatatanPanel;
import login.LoginFrame;
import database.DatabaseConnection;
import com.formdev.flatlaf.FlatLightLaf;
import util.RoundedButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;
import javax.sound.sampled.*;

public class KaryawanDashboard extends JFrame {
    private final User user;
    private Timer reminderTimer;
    private boolean sidebarVisible = true;
    private util.XamppManager xamppManager;
    private JPanel sidebar;
    private JPanel contentPanel;
    private JPanel topBar;

    public KaryawanDashboard(User user, util.XamppManager xamppManager) {
        this.user = user;
        this.xamppManager = xamppManager;
        FlatLightLaf.setup();
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/resources/TaskFlow.png"));
            setIconImage(icon.getImage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        setTitle("Dashboard Karyawan - " + user.getNama());
        setSize(900, 550);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initUI();
        startReminderChecker();
        setResizable(false);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (reminderTimer != null) reminderTimer.cancel();
            }
        });
    }

    private void initUI() {
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(180, getHeight()));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        sidebar.setBackground(Color.WHITE);

        String[] buttonLabels = { "Kerjakan Tugas", "Catatan ke Manager", "Profil", "Logout" };
        JButton[] buttons = new JButton[buttonLabels.length];

        for (int i = 0; i < buttonLabels.length; i++) {
            buttons[i] = createSidebarButton(buttonLabels[i]);
            sidebar.add(wrapFixedButton(buttons[i]));
            sidebar.add(Box.createVerticalStrut(10));
        }

        contentPanel = new JPanel(new BorderLayout());

        JButton toggleButton = new JButton("≡");
        toggleButton.setFont(new Font("Poppins", Font.BOLD, 16));
        toggleButton.setForeground(Color.decode("#be375f"));
        toggleButton.putClientProperty("JButton.buttonType", "toolBarButton");

        topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topBar.add(toggleButton);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        toggleButton.addActionListener(e -> toggleSidebar());

        buttons[0].addActionListener(e -> setContent(new ChecklistPanel(user)));
        buttons[1].addActionListener(e -> setContent(new CatatanPanel(user.getId(), false)));
        buttons[2].addActionListener(e -> showProfil());
        buttons[3].addActionListener(e -> {
            if (reminderTimer != null) reminderTimer.cancel();
            dispose();
            new LoginFrame(xamppManager).setVisible(true);
        });

        setContent(new ChecklistPanel(user));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (reminderTimer != null) reminderTimer.cancel();
                if (xamppManager != null) xamppManager.stopXampp();
            }
        });

    }

    private JButton createSidebarButton(String text) {
        RoundedButton button = new RoundedButton(text, 40);
        button.setBackground(Color.decode("#be375f"));
        button.setForeground(Color.white);
        button.setPreferredSize(new Dimension(160, 40));
        button.setMaximumSize(new Dimension(160, 40));
        button.setMinimumSize(new Dimension(160, 40));
        return button;
    }

    private JPanel wrapFixedButton(JButton button) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setOpaque(false);
        panel.add(Box.createHorizontalGlue());
        panel.add(button);
        panel.add(Box.createHorizontalGlue());
        return panel;
    }

    private void setContent(JComponent component) {
        contentPanel.removeAll();
        contentPanel.add(topBar, BorderLayout.NORTH);
        contentPanel.add(component, BorderLayout.CENTER);
        refreshContent();
    }

    private void showProfil() {
        JDialog dialog = new JDialog(this, "Profil Karyawan", true);
        dialog.setSize(300, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel();
        panel.setBackground(Color.decode("#be375f"));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        Font poppinsFont = new Font("Poppins", Font.BOLD, 14);

        if (user.getFoto() != null && !user.getFoto().isEmpty()) {
            try {
                ImageIcon icon = new ImageIcon(user.getFoto());
                Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                JLabel imgLabel = new JLabel(new ImageIcon(img));
                imgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(imgLabel);
                panel.add(Box.createVerticalStrut(15));
            } catch (Exception e) {
                JLabel errorLabel = new JLabel("Gagal memuat foto.");
                errorLabel.setForeground(Color.white);
                errorLabel.setFont(poppinsFont);
                errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(errorLabel);
            }
        }

        JLabel namaLabel = new JLabel("Nama: " + user.getNama());
        namaLabel.setForeground(Color.white);
        namaLabel.setFont(poppinsFont);
        namaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel jabatanLabel = new JLabel("Jabatan: " + user.getJabatan());
        jabatanLabel.setForeground(Color.white);
        jabatanLabel.setFont(poppinsFont);
        jabatanLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(namaLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(jabatanLabel);

        JButton closeButton = new JButton("Tutup");
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeButton.setBackground(Color.white);
        closeButton.setForeground(Color.decode("#be375f"));
        closeButton.setFocusPainted(false);
        closeButton.setFont(poppinsFont);
        closeButton.addActionListener(e -> dialog.dispose());

        panel.add(Box.createVerticalStrut(20));
        panel.add(closeButton);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void refreshContent() {
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void toggleSidebar() {
        final int step = 10;
        final int delay = 10;
        final int width = 180;

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int currentWidth = sidebar.getPreferredSize().width;

            @Override
            public void run() {
                if (sidebarVisible) {
                    currentWidth -= step;
                    if (currentWidth <= 0) {
                        sidebar.setPreferredSize(new Dimension(0, sidebar.getHeight()));
                        sidebar.setVisible(false);
                        sidebarVisible = false;
                        refreshSidebar(timer);
                    } else {
                        sidebar.setPreferredSize(new Dimension(currentWidth, sidebar.getHeight()));
                        refreshSidebar(null);
                    }
                } else {
                    sidebar.setVisible(true);
                    currentWidth += step;
                    if (currentWidth >= width) {
                        sidebar.setPreferredSize(new Dimension(width, sidebar.getHeight()));
                        sidebarVisible = true;
                        refreshSidebar(timer);
                    } else {
                        sidebar.setPreferredSize(new Dimension(currentWidth, sidebar.getHeight()));
                        refreshSidebar(null);
                    }
                }
            }
        }, 0, delay);
    }

    private void refreshSidebar(Timer timerToCancel) {
        SwingUtilities.invokeLater(() -> {
            sidebar.revalidate();
            sidebar.repaint();
            if (timerToCancel != null) timerToCancel.cancel();
        });
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
        String sql = """
                SELECT judul, deskripsi
                FROM tugas
                WHERE role = ? AND tanggal_tugas = CURDATE()
                AND TIME(waktu_tugas) BETWEEN SUBTIME(CURTIME(), '00:01:00') AND ADDTIME(CURTIME(), '00:01:00')
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getJabatan());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String judul = rs.getString("judul");
                    String deskripsi = rs.getString("deskripsi");
                    new Thread(() -> tampilkanNotifikasi("Tugas: " + judul, deskripsi)).start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mainkanSuaraNotifikasi() {
        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(
                getClass().getResource("/resources/notifikasi.wav"))) {
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tampilkanNotifikasi(String title, String message) {
        if (!SystemTray.isSupported()) return;

        SystemTray tray = SystemTray.getSystemTray();
        TrayIcon trayIcon = new TrayIcon(
                Toolkit.getDefaultToolkit().createImage(""),
                "Notifikasi Tugas"
        );
        trayIcon.setImageAutoSize(true);

        try {
            tray.add(trayIcon);
            new Thread(() -> {
                mainkanSuaraNotifikasi();
                trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
            }).start();

            Thread.sleep(5000);
            tray.remove(trayIcon);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
