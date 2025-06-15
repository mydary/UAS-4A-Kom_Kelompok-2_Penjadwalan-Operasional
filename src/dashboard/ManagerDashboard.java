package dashboard;

import com.formdev.flatlaf.FlatLightLaf;
import login.LoginFrame;
import model.User;
import tugas.TugasCRUDPanel;
import histori.HistoriPanel;
import catatan.CatatanPanel;
import akun.ManajemenAkunPanel;
import database.DatabaseConnection;
import util.RoundedButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ManagerDashboard extends JFrame {
    private User user;
    private JPanel sidebar;
    private JPanel contentPanel;
    private JSplitPane splitPane;
    private JPanel kiriPanel;
    private boolean sidebarVisible = true;
    private final int SIDEBAR_MAX_WIDTH = 160;
    private final int SIDEBAR_MIN_WIDTH = 0;
    private Timer slideTimer;
    private int sidebarCurrentWidth = SIDEBAR_MAX_WIDTH;
    private JButton btnHamburger;
    private util.XamppManager xamppManager;
    private final Color backgroundColor = new Color(255, 250, 250); // putih salju
    private final Color buttonColor = Color.decode("#be375f");
    private final Color buttonHoverColor = new Color(150, 0, 0);    // merah gelap

    public ManagerDashboard(User user, util.XamppManager xamppManager) {
        this.user = user;
        this.xamppManager = xamppManager;

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.err.println("Gagal load FlatLaf.");
        }

        this.user = user;
        setTitle("Dashboard Manager - " + user.getNama());
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        UIManager.put("Label.font", new Font("Poppins", Font.PLAIN, 13));
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/resources/TaskFlow.png"));
            setIconImage(icon.getImage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        initUI();
    }

    private void initUI() {
        sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(SIDEBAR_MAX_WIDTH, 0));
        sidebar.setBackground(backgroundColor);

        // Panel atas sidebar
        RoundedButton btnProfil = createMenuButton("👤  Tampilkan Profil");
        btnProfil.addActionListener(e -> tampilkanProfilDialog());

        JPanel topSidebarPanel = new JPanel();
        topSidebarPanel.setLayout(new BoxLayout(topSidebarPanel, BoxLayout.Y_AXIS));
        topSidebarPanel.setOpaque(false);
        topSidebarPanel.add(Box.createVerticalStrut(10));
        topSidebarPanel.add(btnProfil);

        sidebar.add(topSidebarPanel, BorderLayout.NORTH);

        // Panel menu dengan BoxLayout
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);

        RoundedButton btnTugas = createMenuButton("✍️  Susun Tugas");
        RoundedButton btnHistori = createMenuButton("📃  Histori Tugas");
        RoundedButton btnCatatan = createMenuButton("📝  Catatan");
        RoundedButton btnAkun = createMenuButton("👥  Akun");
        RoundedButton btnLogout = createMenuButton("🚪  Logout");

        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(wrapFixedButton(btnTugas));
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(wrapFixedButton(btnHistori));
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(wrapFixedButton(btnCatatan));
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(wrapFixedButton(btnAkun));
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(wrapFixedButton(btnLogout));
        menuPanel.add(Box.createVerticalGlue());

        sidebar.add(menuPanel, BorderLayout.CENTER);

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(backgroundColor);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        kiriPanel = new JPanel(new BorderLayout());
        kiriPanel.add(sidebar, BorderLayout.CENTER);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, kiriPanel, contentPanel);
        splitPane.setDividerLocation(SIDEBAR_MAX_WIDTH);
        splitPane.setEnabled(false);
        splitPane.setDividerSize(3);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);

        JPanel hamburgerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        hamburgerPanel.setPreferredSize(new Dimension(40, 0));
        hamburgerPanel.setBackground(backgroundColor);

        btnHamburger = new JButton("☰");
        btnHamburger.setFocusPainted(false);
        btnHamburger.setMargin(new Insets(2, 6, 2, 6));
        btnHamburger.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHamburger.setToolTipText("Tampilkan / Sembunyikan Sidebar");
        btnHamburger.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        btnHamburger.setForeground(buttonColor);
        btnHamburger.setFont(new Font("Poppins", Font.BOLD, 18));
        btnHamburger.setOpaque(true);
        btnHamburger.setContentAreaFilled(true);
        btnHamburger.addActionListener(e -> toggleSidebarMenu());

        hamburgerPanel.add(btnHamburger);
        add(hamburgerPanel, BorderLayout.WEST);

        // Aksi tombol menu
        btnTugas.addActionListener(e -> setContentPanel(new TugasCRUDPanel()));
        btnHistori.addActionListener(e -> setContentPanel(new HistoriPanel()));
        btnCatatan.addActionListener(e -> setContentPanel(new CatatanPanel(user.getId(), true)));
        btnAkun.addActionListener(e -> setContentPanel(new ManajemenAkunPanel()));
        btnLogout.addActionListener(e -> {
            dispose();
            new LoginFrame(xamppManager).setVisible(true);
        });

        setContentPanel(new TugasCRUDPanel());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (xamppManager != null) {
                    xamppManager.stopXampp();
                }
            }
        });

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

    private RoundedButton createMenuButton(String text) {
        RoundedButton button = new RoundedButton(text, 40);
        button.setBackground(buttonColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Poppins", Font.BOLD, 13));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);

        Dimension fixedSize = new Dimension(160, 40);
        button.setPreferredSize(fixedSize);
        button.setMaximumSize(fixedSize);
        button.setMinimumSize(fixedSize);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(buttonHoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(buttonColor);
            }
        });

        return button;
    }

    private void toggleSidebarMenu() {
        if (slideTimer != null && slideTimer.isRunning()) return;

        sidebarVisible = !sidebarVisible;
        final int step = 15;
        int delay = 10;

        slideTimer = new Timer(delay, null);
        slideTimer.addActionListener(e -> {
            sidebarCurrentWidth += sidebarVisible ? step : -step;
            if (sidebarVisible && sidebarCurrentWidth >= SIDEBAR_MAX_WIDTH) {
                sidebarCurrentWidth = SIDEBAR_MAX_WIDTH;
                slideTimer.stop();
            } else if (!sidebarVisible && sidebarCurrentWidth <= SIDEBAR_MIN_WIDTH) {
                sidebarCurrentWidth = SIDEBAR_MIN_WIDTH;
                slideTimer.stop();
            }

            sidebar.setPreferredSize(new Dimension(sidebarCurrentWidth, 0));
            kiriPanel.revalidate();
            kiriPanel.repaint();
            splitPane.setDividerLocation(sidebarCurrentWidth);
        });
        slideTimer.start();
    }

    private void setContentPanel(JPanel panel) {
        contentPanel.removeAll();
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void tampilkanProfilDialog() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
            SELECT a.role, p.nama, p.jabatan, p.foto
            FROM akun a
            LEFT JOIN profil p ON a.id = p.id
            WHERE a.id = ?
            """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, user.getId());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                String nama = rs.getString("nama");
                String jabatan = rs.getString("jabatan");
                String fotoPath = rs.getString("foto");

                StringBuilder message = new StringBuilder();
                message.append("Nama    : ").append(nama != null ? nama : "-").append("\n");
                message.append("Jabatan : ").append(jabatan != null ? jabatan : "-").append("\n");
                message.append("Role    : ").append(role != null ? role : "-");

                ImageIcon icon = null;
                if (fotoPath != null && !fotoPath.isEmpty()) {
                    ImageIcon original = new ImageIcon(fotoPath);
                    Image scaled = original.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                    icon = new ImageIcon(scaled);
                }

                showCustomStyledDialog(message.toString(), "Profil Manager", icon);

            } else {
                showCustomStyledDialog("Profil tidak ditemukan.", "Error", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showCustomStyledDialog("Gagal load data profil!", "Error", null);
        }
    }

    private void showCustomStyledDialog(String message, String title, Icon icon) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.decode("#be375f"));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        if (icon != null) {
            JLabel imageLabel = new JLabel(icon);
            imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(imageLabel);
            panel.add(Box.createRigidArea(new Dimension(0, 15)));
        }

        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setOpaque(false);
        textArea.setForeground(Color.white);
        textArea.setFont(new Font("Poppins", Font.BOLD, 13));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        textArea.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        panel.add(textArea);

        UIManager.put("OptionPane.background", Color.decode("#be375f"));
        UIManager.put("Panel.background", Color.decode("#be375f"));
        UIManager.put("Button.background", new Color(0xf9ad00));
        UIManager.put("Button.foreground", Color.decode("#be375f"));
        UIManager.put("Button.font", new Font("Poppins", Font.BOLD, 12));
        UIManager.put("OptionPane.messageForeground", Color.white);

        JOptionPane.showMessageDialog(null, panel, title, JOptionPane.PLAIN_MESSAGE);

        // Reset UIManager ke default
        UIManager.put("OptionPane.background", null);
        UIManager.put("Panel.background", null);
        UIManager.put("Button.background", null);
        UIManager.put("Button.foreground", null);
        UIManager.put("Button.font", null);
        UIManager.put("OptionPane.messageForeground", null);
    }
}
