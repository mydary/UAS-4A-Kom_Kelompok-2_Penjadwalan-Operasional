package catatan;

import database.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class CatatanPanel extends JPanel {
    private JTextArea areaCatatan;
    private JButton btnKirim;
    private int userId;
    private boolean isManager;
    private JPanel listPanel;
    private String[] roles = {"Kasir", "Crew Ice Cream", "Staff Dapur", "Staff Gudang", "Staff Kebersihan"};
    private String selectedRole = roles[0]; // default

    public CatatanPanel(int userId, boolean isManager) {
        this.userId = userId;
        this.isManager = isManager;
        setLayout(new BorderLayout());

        if (isManager) {
            initManagerPanel();
        } else {
            initKaryawanPanel();
        }
    }

    private void initKaryawanPanel() {
        areaCatatan = new JTextArea(5, 30);
        btnKirim = new JButton("Kirim Catatan");

        btnKirim.addActionListener(e -> kirimCatatan());

        add(new JScrollPane(areaCatatan), BorderLayout.CENTER);
        add(btnKirim, BorderLayout.SOUTH);
    }

    private void kirimCatatan() {
        String catatan = areaCatatan.getText().trim();
        if (catatan.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Isi catatan tidak boleh kosong!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO catatan (id_user, isi) VALUES (?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setString(2, catatan);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Catatan berhasil dikirim!");
            areaCatatan.setText("");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initManagerPanel() {
        // Tab navigasi horizontal
        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ButtonGroup group = new ButtonGroup();

        for (String role : roles) {
            JToggleButton button = new JToggleButton(role);
            if (role.equals(selectedRole)) button.setSelected(true);
            button.addActionListener(e -> {
                selectedRole = role;
                showDaftarCatatan(selectedRole);
            });
            group.add(button);
            tabPanel.add(button);
        }

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        add(tabPanel, BorderLayout.NORTH);
        add(new JScrollPane(listPanel), BorderLayout.CENTER);

        // Load awal
        showDaftarCatatan(selectedRole);
    }

    private void showDaftarCatatan(String role) {
        listPanel.removeAll();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                    SELECT c.id, c.isi, c.waktu
                    FROM catatan c
                    JOIN akun a ON c.id_user = a.id
                    JOIN profil p ON a.id = p.id
                    WHERE p.jabatan = ?
                    ORDER BY c.waktu DESC
                    """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();

            int count = 1;
            while (rs.next()) {
                int idCatatan = rs.getInt("id");
                String isi = rs.getString("isi");
                Timestamp waktu = rs.getTimestamp("waktu");

                JPanel catatanItem = new JPanel(new BorderLayout());
                catatanItem.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

                JTextArea note = new JTextArea(
                        "Catatan #" + count + " - " + waktu.toString() + "\n\n" + isi
                );
                note.setWrapStyleWord(true);
                note.setLineWrap(true);
                note.setEditable(false);
                note.setBackground(new Color(245, 245, 245));
                note.setBorder(BorderFactory.createLineBorder(Color.GRAY));

                JButton btnHapus = new JButton("Hapus");
                btnHapus.addActionListener(e -> {
                    int konfirmasi = JOptionPane.showConfirmDialog(this,
                            "Yakin ingin menghapus catatan ini?",
                            "Konfirmasi Hapus",
                            JOptionPane.YES_NO_OPTION);
                    if (konfirmasi == JOptionPane.YES_OPTION) {
                        hapusCatatan(idCatatan);
                        showDaftarCatatan(selectedRole); // refresh setelah hapus
                    }
                });

                catatanItem.add(note, BorderLayout.CENTER);
                catatanItem.add(btnHapus, BorderLayout.EAST);

                listPanel.add(catatanItem);
                count++;
            }
            listPanel.revalidate();
            listPanel.repaint();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void hapusCatatan(int idCatatan) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM catatan WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idCatatan);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Catatan berhasil dihapus!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal menghapus catatan.");
        }
    }
}
