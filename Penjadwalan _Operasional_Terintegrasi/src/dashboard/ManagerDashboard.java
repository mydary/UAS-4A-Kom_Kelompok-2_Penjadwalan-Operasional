package dashboard;

import model.User;
import tugas.TugasCRUDPanel;
import histori.HistoriPanel;
import catatan.CatatanPanel;
import akun.ManajemenAkunPanel;
import database.DatabaseConnection;
import util.PasswordEncryptor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ManagerDashboard extends JFrame {
    private User user;
    private JPanel profilPanel;
    private JPanel sidebar;
    private JPanel contentPanel;
    private JSplitPane splitPane;
    private JPanel kiriPanel;

    private boolean profilVisible = false;
    private boolean sidebarVisible = true;

    private final int SIDEBAR_MAX_WIDTH = 150;
    private final int SIDEBAR_MIN_WIDTH = 0;
    private Timer slideTimer;
    private int sidebarCurrentWidth = SIDEBAR_MAX_WIDTH;

    private JButton btnHamburger;

    public ManagerDashboard(User user) {
        this.user = user;
        setTitle("Dashboard Manager - " + user.getNama());
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initUI();
        if (profilVisible) {
            loadProfil();
        }
    }

    private void initUI() {
        profilPanel = new JPanel();
        profilPanel.setPreferredSize(new Dimension(280, 0));
        profilPanel.setBorder(BorderFactory.createTitledBorder("Profil Anda"));
        profilPanel.setLayout(new BoxLayout(profilPanel, BoxLayout.Y_AXIS));
        profilPanel.setBackground(Color.WHITE);

        sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(SIDEBAR_MAX_WIDTH, 0));
        sidebar.setLayout(new BorderLayout());
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        sidebar.setBackground(Color.LIGHT_GRAY);

        JButton btnProfil = new JButton("Tampilkan Profil");
        btnProfil.setFocusPainted(false);
        btnProfil.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnProfil.setToolTipText("Tampilkan atau sembunyikan panel profil");
        btnProfil.addActionListener(e -> {
            toggleProfilPanel();
            btnProfil.setText(profilVisible ? "Sembunyikan Profil" : "Tampilkan Profil");
        });

        JPanel topSidebarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        topSidebarPanel.setOpaque(false);
        topSidebarPanel.add(btnProfil);

        sidebar.add(topSidebarPanel, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        menuPanel.setOpaque(false);

        JButton btnTugas = new JButton("Manajemen Tugas");
        JButton btnHistori = new JButton("Histori Tugas");
        JButton btnCatatan = new JButton("Catatan Karyawan");
        JButton btnAkun = new JButton("Manajemen Akun");
        JButton btnLogout = new JButton("Logout");

        menuPanel.add(btnTugas);
        menuPanel.add(btnHistori);
        menuPanel.add(btnCatatan);
        menuPanel.add(btnAkun);
        menuPanel.add(btnLogout);

        sidebar.add(menuPanel, BorderLayout.CENTER);

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.setBackground(Color.WHITE);

        kiriPanel = new JPanel(new BorderLayout());
        if (profilVisible) {
            kiriPanel.add(profilPanel, BorderLayout.WEST);
        }
        kiriPanel.add(sidebar, BorderLayout.CENTER);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, kiriPanel, contentPanel);
        splitPane.setDividerLocation(profilVisible ? 430 : SIDEBAR_MAX_WIDTH);
        splitPane.setEnabled(false);
        splitPane.setDividerSize(3);

        add(splitPane, BorderLayout.CENTER);

        JPanel hamburgerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        hamburgerPanel.setPreferredSize(new Dimension(40, 0));
        hamburgerPanel.setBackground(Color.WHITE);

        btnHamburger = new JButton("\u2630");
        btnHamburger.setFocusPainted(false);
        btnHamburger.setMargin(new Insets(2, 6, 2, 6));
        btnHamburger.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHamburger.setToolTipText("Tampilkan / Sembunyikan Menu Sidebar");
        btnHamburger.addActionListener(e -> toggleSidebarMenu());

        hamburgerPanel.add(btnHamburger);

        add(hamburgerPanel, BorderLayout.WEST);

        btnTugas.addActionListener(e -> setContentPanel(new TugasCRUDPanel()));
        btnHistori.addActionListener(e -> setContentPanel(new HistoriPanel()));
        btnCatatan.addActionListener(e -> setContentPanel(new CatatanPanel(user.getId(), true)));
        btnAkun.addActionListener(e -> {
            ManajemenAkunPanel akunPanel = new ManajemenAkunPanel();
            setContentPanel(akunPanel);
            loadProfil();
        });
        btnLogout.addActionListener(e -> {
            dispose();
            new login.LoginFrame().setVisible(true);
        });

        setContentPanel(new TugasCRUDPanel());
    }

    private void toggleProfilPanel() {
        profilVisible = !profilVisible;
        if (profilVisible) {
            kiriPanel.add(profilPanel, BorderLayout.WEST);
            loadProfil();
            splitPane.setDividerLocation(430);
        } else {
            kiriPanel.remove(profilPanel);
            splitPane.setDividerLocation(sidebarVisible ? SIDEBAR_MAX_WIDTH : 0);
        }
        kiriPanel.revalidate();
        kiriPanel.repaint();
    }

    private void toggleSidebarMenu() {
        if (slideTimer != null && slideTimer.isRunning()) {
            return;
        }

        sidebarVisible = !sidebarVisible;
        final int step = 15;
        int delay = 10;

        if (!sidebarVisible) {
            slideTimer = new Timer(delay, null);
            slideTimer.addActionListener(e -> {
                sidebarCurrentWidth -= step;
                if (sidebarCurrentWidth <= SIDEBAR_MIN_WIDTH) {
                    sidebarCurrentWidth = SIDEBAR_MIN_WIDTH;
                    slideTimer.stop();
                }
                sidebar.setPreferredSize(new Dimension(sidebarCurrentWidth, 0));
                kiriPanel.revalidate();
                kiriPanel.repaint();
                splitPane.setDividerLocation(profilVisible ? 280 + sidebarCurrentWidth : sidebarCurrentWidth);
            });
            slideTimer.start();
        } else {
            slideTimer = new Timer(delay, null);
            slideTimer.addActionListener(e -> {
                sidebarCurrentWidth += step;
                if (sidebarCurrentWidth >= SIDEBAR_MAX_WIDTH) {
                    sidebarCurrentWidth = SIDEBAR_MAX_WIDTH;
                    slideTimer.stop();
                }
                sidebar.setPreferredSize(new Dimension(sidebarCurrentWidth, 0));
                kiriPanel.revalidate();
                kiriPanel.repaint();
                splitPane.setDividerLocation(profilVisible ? 280 + sidebarCurrentWidth : sidebarCurrentWidth);
            });
            slideTimer.start();
        }
    }

    private void setContentPanel(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(new JScrollPane(panel), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void loadProfil() {
        profilPanel.removeAll();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT a.username, a.password, a.role, p.nama, p.jabatan, p.foto
                FROM akun a
                LEFT JOIN profil p ON a.id = p.id
                WHERE a.id = ?
            """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, user.getId());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String username = rs.getString("username");
                String encryptedPassword = rs.getString("password");
                String password = PasswordEncryptor.decrypt(encryptedPassword); // DECRYPT di sini
                String role = rs.getString("role");
                String nama = rs.getString("nama");
                String jabatan = rs.getString("jabatan");
                String fotoPath = rs.getString("foto");

                if (fotoPath != null && !fotoPath.isEmpty()) {
                    ImageIcon icon = new ImageIcon(fotoPath);
                    Image img = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
                    JLabel fotoLabel = new JLabel(new ImageIcon(img));
                    fotoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    profilPanel.add(fotoLabel);
                } else {
                    profilPanel.add(Box.createRigidArea(new Dimension(0, 130)));
                }

                profilPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                JLabel lblUsername = new JLabel("Username: " + username);
                JLabel lblPassword = new JLabel("Password: " + password);
                JLabel lblRole = new JLabel("Role: " + role);
                JLabel lblNama = new JLabel("Nama: " + (nama != null ? nama : "-"));
                JLabel lblJabatan = new JLabel("Jabatan: " + (jabatan != null ? jabatan : "-"));

                lblUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
                lblPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
                lblRole.setAlignmentX(Component.LEFT_ALIGNMENT);
                lblNama.setAlignmentX(Component.LEFT_ALIGNMENT);
                lblJabatan.setAlignmentX(Component.LEFT_ALIGNMENT);

                profilPanel.add(lblUsername);
                profilPanel.add(lblPassword);
                profilPanel.add(lblRole);
                profilPanel.add(lblNama);
                profilPanel.add(lblJabatan);
            } else {
                profilPanel.add(new JLabel("Profil tidak ditemukan."));
            }

            profilPanel.revalidate();
            profilPanel.repaint();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal load data profil!");
        }
    }
}
