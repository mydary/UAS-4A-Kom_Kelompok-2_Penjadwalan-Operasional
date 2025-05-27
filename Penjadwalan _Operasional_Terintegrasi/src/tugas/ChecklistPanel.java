package tugas;

import database.DatabaseConnection;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class ChecklistPanel extends JPanel {
    private User user;
    private JPanel tugasPanel;

    public ChecklistPanel(User user) {
        this.user = user;
        setLayout(new BorderLayout());
        tugasPanel = new JPanel();
        tugasPanel.setLayout(new BoxLayout(tugasPanel, BoxLayout.Y_AXIS));
        add(new JScrollPane(tugasPanel), BorderLayout.CENTER);
        loadTugas();
    }

    private void loadTugas() {
        tugasPanel.removeAll();
        Set<Integer> tugasSelesai = getTugasSelesai();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM tugas WHERE role = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, user.getJabatan());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int idTugas = rs.getInt("id");
                String judul = rs.getString("judul");
                String deskripsi = rs.getString("deskripsi");
                Date tanggal = rs.getDate("tanggal_tugas");
                Time waktu = rs.getTime("waktu_tugas");

                JCheckBox checkBox = new JCheckBox(judul + " - " + deskripsi + " (Tanggal: " + tanggal + ", Jam: " + waktu + ")");
                JButton btnSelesai = new JButton("Selesaikan");
                JPanel panel = new JPanel(new BorderLayout());
                panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

                if (tugasSelesai.contains(idTugas)) {
                    checkBox.setSelected(true);
                    checkBox.setEnabled(false);
                    btnSelesai.setEnabled(false);
                    btnSelesai.setVisible(false);
                    panel.setBackground(new Color(230, 230, 230));
                } else {
                    btnSelesai.addActionListener(e -> {
                        if (checkBox.isSelected()) {
                            simpanHistori(idTugas);
                            checkBox.setEnabled(false);
                            btnSelesai.setEnabled(false);
                            panel.setBackground(new Color(230, 230, 230));
                        } else {
                            JOptionPane.showMessageDialog(this, "Checklist dulu tugasnya!");
                        }
                    });
                }

                panel.add(checkBox, BorderLayout.WEST);
                panel.add(btnSelesai, BorderLayout.EAST);
                tugasPanel.add(panel);
            }

            tugasPanel.revalidate();
            tugasPanel.repaint();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Set<Integer> getTugasSelesai() {
        Set<Integer> selesai = new HashSet<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id_tugas FROM histori_tugas WHERE id_user = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, user.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                selesai.add(rs.getInt("id_tugas"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return selesai;
    }

    private void simpanHistori(int idTugas) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO histori_tugas (id_user, id_tugas, bukti, waktu_selesai) VALUES (?, ?, ?, NOW())";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, user.getId());
            ps.setInt(2, idTugas);
            ps.setString(3, "Terselesaikan");
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Tugas ditandai selesai!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
