package login;

import java.awt.Image;
import database.DatabaseConnection;
import dashboard.ManagerDashboard;
import dashboard.KaryawanDashboard;
import model.User;
import util.PasswordEncryptor;
import util.RoundedTextField;
import util.RoundedPasswordField;
import util.RoundedButton;
import com.formdev.flatlaf.FlatLightLaf;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;
import util.XamppManager;

public class LoginFrame extends JFrame {
    private XamppManager xamppManager;
    private RoundedTextField tfUsername;
    private RoundedPasswordField pfPassword;
    private JComboBox<String> comboRole;
    private Timer globalReminderTimer;
    private RoundedButton btnLogin;
    private JButton btnLupaPassword;

    public LoginFrame(XamppManager xamppManager) {
        this.xamppManager = xamppManager;
        FlatLightLaf.setup();
        setTitle("Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
        startGlobalReminderChecker();
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/resources/TaskFlow.png"));
            setIconImage(icon.getImage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initUI() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        panel.setBackground(Color.decode("#be375f"));

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setOpaque(false);

        ImageIcon logoIcon = new ImageIcon(getClass().getResource("/resources/logo.png"));
        Image scaledLogo = logoIcon.getImage().getScaledInstance(150, 59, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledLogo), JLabel.CENTER);
        panel.add(logoLabel, BorderLayout.NORTH);

        tfUsername = new RoundedTextField(20, 30);
        tfUsername.setForeground(Color.decode("#be375f"));
        tfUsername.setBackground(Color.white);

        pfPassword = new RoundedPasswordField(30);
        pfPassword.setForeground(Color.decode("#be375f"));
        pfPassword.setBackground(Color.white);

        comboRole = new JComboBox<>(new String[]{
                "Manager", "Kasir", "Crew Ice cream", "Staff Dapur", "Staff Gudang", "Staff Kebersihan"
        });
        comboRole.setFont(new Font("Poppins", Font.BOLD, 12));
        comboRole.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        comboRole.setForeground(Color.decode("#be375f"));
        comboRole.setBackground(Color.white);

        formPanel.add(new JLabel("Username:") {{
            setForeground(Color.white);
            setFont(new Font("Poppins", Font.BOLD, 12));
        }});
        formPanel.add(tfUsername);

        formPanel.add(new JLabel("Password:") {{
            setForeground(Color.white);
            setFont(new Font("Poppins", Font.BOLD, 12));
        }});
        formPanel.add(pfPassword);

        formPanel.add(new JLabel("Role:") {{
            setForeground(Color.white);
            setFont(new Font("Poppins", Font.BOLD, 12));
        }});
        formPanel.add(comboRole);

        btnLogin = new RoundedButton("Login", 30);
        btnLogin.setBackground(new Color(0xf9ad00));
        btnLogin.addActionListener(e -> login());
        formPanel.add(new JLabel());
        formPanel.add(btnLogin);

        btnLupaPassword = new JButton("<HTML><U>Lupa Password Manager?</U></HTML>");
        btnLupaPassword.addActionListener(e -> handleLupaPasswordManager());
        btnLupaPassword.setForeground(Color.white);
        btnLupaPassword.setBorderPainted(false);
        btnLupaPassword.setContentAreaFilled(false);
        btnLupaPassword.setFocusPainted(false);
        btnLupaPassword.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        formPanel.add(new JLabel());
        formPanel.add(btnLupaPassword);

        panel.add(formPanel, BorderLayout.CENTER);
        add(panel);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (xamppManager != null) {
                    xamppManager.stopXampp();
                }
            }
        });
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

                    showCustomStyledDialog("Login berhasil sebagai " + selectedRole, "Login");
                    if (globalReminderTimer != null) globalReminderTimer.cancel();

                    if ("Manager".equalsIgnoreCase(selectedRole)) {
                        new ManagerDashboard(user, xamppManager).setVisible(true);
                    } else {
                        new KaryawanDashboard(user, xamppManager).setVisible(true);
                    }

                    dispose();
                } else {
                    showCustomStyledDialog("Password salah!", "Login Gagal");
                }
            } else {
                showCustomStyledDialog("Login gagal! Username atau role salah.", "Login Gagal");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showCustomStyledDialog("Terjadi kesalahan saat login.", "Error");
        }
    }

    private void handleLupaPasswordManager() {
        // Simpan konfigurasi asli
        Color oldOptionPaneBg = UIManager.getColor("OptionPane.background");
        Color oldPanelBg = UIManager.getColor("Panel.background");
        Color oldButtonBg = UIManager.getColor("Button.background");
        Color oldButtonFg = UIManager.getColor("Button.foreground");
        Color oldMessageFg = UIManager.getColor("OptionPane.messageForeground");

        // Panel custom
        JPanel panel = new JPanel();
        panel.setBackground(Color.decode("#be375f"));
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel("Masukkan password kedua (cadangan):");
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Poppins", Font.PLAIN, 12));

        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setBackground(Color.WHITE);
        passwordField.setForeground(Color.decode("#be375f"));
        passwordField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        panel.add(label, BorderLayout.NORTH);
        panel.add(passwordField, BorderLayout.CENTER);

        // Atur sementara UIManager
        UIManager.put("OptionPane.background", Color.decode("#be375f"));
        UIManager.put("Panel.background", Color.decode("#be375f"));
        UIManager.put("Button.background", new Color(0xf9ad00));
        UIManager.put("Button.foreground", Color.decode("#be375f"));
        UIManager.put("OptionPane.messageForeground", Color.WHITE);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Login Darurat Manager",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        // Pulihkan nilai UIManager ke semula
        UIManager.put("OptionPane.background", oldOptionPaneBg);
        UIManager.put("Panel.background", oldPanelBg);
        UIManager.put("Button.background", oldButtonBg);
        UIManager.put("Button.foreground", oldButtonFg);
        UIManager.put("OptionPane.messageForeground", oldMessageFg);

        if (result == JOptionPane.OK_OPTION) {
            String inputCadangan = new String(passwordField.getPassword());
            if (inputCadangan.isEmpty()) return;

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = """
                SELECT a.id, a.username, a.role, a.password_kedua, p.nama, p.jabatan, p.foto
                FROM akun a
                JOIN profil p ON a.id = p.id
                WHERE a.role = 'manager'
                LIMIT 1
            """;
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String encryptedBackupPassword = rs.getString("password_kedua");
                    String decryptedBackupPassword = PasswordEncryptor.decrypt(encryptedBackupPassword);

                    if (inputCadangan.equals(decryptedBackupPassword)) {
                        User user = new User(
                                rs.getInt("id"),
                                rs.getString("username"),
                                rs.getString("role"),
                                rs.getString("nama"),
                                rs.getString("jabatan"),
                                rs.getString("foto")
                        );
                        JOptionPane.showMessageDialog(this, "Login darurat berhasil.");
                        new ManagerDashboard(user, xamppManager).setVisible(true);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, """
                        Password kedua salah!
                        Hubungi developer: daryikhsan21@gmail.com
                    """);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Data akun manager tidak ditemukan.");
                }

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat verifikasi.");
            }
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

    private void mainkanSuaraNotifikasi() {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(
                    getClass().getResource("/resources/notifikasi.wav")
            );
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tampilkanNotifikasi(String title, String message) {
        if (!SystemTray.isSupported()) return;
        mainkanSuaraNotifikasi();

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

    private void showCustomStyledDialog(String message, String title) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.decode("#be375f"));
        contentPanel.setBorder(BorderFactory.createLineBorder(Color.white, 2));

        JLabel label = new JLabel("<html><div style='text-align: center;'>" + message.replace("\n", "<br>") + "</div></html>", SwingConstants.CENTER);
        label.setForeground(Color.white);
        label.setFont(new Font("Poppins", Font.BOLD, 13));
        contentPanel.add(label, BorderLayout.CENTER);

        JButton okButton = new JButton("OK");
        okButton.setBackground(new Color(0xf9ad00));
        okButton.setForeground(Color.decode("#be375f"));
        okButton.setFont(new Font("Poppins", Font.BOLD, 12));
        okButton.setFocusPainted(false);
        okButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.decode("#be375f"));
        buttonPanel.add(okButton);

        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(contentPanel);
        dialog.setVisible(true);
    }

    private String showCustomStyledInputDialog(String prompt) {
        JDialog dialog = new JDialog(this, "Verifikasi", true);
        dialog.setSize(350, 160);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.decode("#be375f"));
        panel.setBorder(BorderFactory.createLineBorder(Color.white, 2));

        JLabel label = new JLabel(prompt);
        label.setForeground(Color.white);
        label.setFont(new Font("Poppins", Font.PLAIN, 13));

        JTextField textField = new JTextField();
        textField.setBackground(Color.white);
        textField.setForeground(Color.decode("#be375f"));
        textField.setFont(new Font("Poppins", Font.PLAIN, 12));

        JButton okButton = new JButton("OK");
        okButton.setBackground(new Color(0xf9ad00));
        okButton.setForeground(Color.decode("#be375f"));
        okButton.setFont(new Font("Poppins", Font.BOLD, 12));
        okButton.setFocusPainted(false);
        okButton.addActionListener(e -> dialog.dispose());

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBackground(Color.decode("#be375f"));
        inputPanel.add(label, BorderLayout.NORTH);
        inputPanel.add(textField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.decode("#be375f"));
        buttonPanel.add(okButton);

        panel.add(inputPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setVisible(true);

        return textField.getText();
    }
}
