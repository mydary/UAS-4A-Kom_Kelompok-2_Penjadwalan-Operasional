package histori;

import database.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class HistoriPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JPanel tabPanel;
    private String[] roles = {"Kasir", "Crew Ice cream", "Staff Dapur", "Staff Gudang", "Staff Kebersihan"};
    private String selectedRole = roles[0];

    public HistoriPanel() {
        setLayout(new BorderLayout());
        initUI();
        loadHistoriByRole(selectedRole);
    }

    private void initUI() {
        tabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ButtonGroup btnGroup = new ButtonGroup();

        for (String role : roles) {
            JToggleButton btn = new JToggleButton(role);
            btn.setFocusPainted(false);
            if (role.equals(selectedRole)) btn.setSelected(true);
            btn.addActionListener(e -> {
                selectedRole = role;
                loadHistoriByRole(selectedRole);
            });
            btnGroup.add(btn);
            tabPanel.add(btn);
        }

        add(tabPanel, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{
                "ID Tugas", "Judul", "Deskripsi", "Nama Karyawan",
                "Status", "Tanggal Tugas", "Waktu Tugas", "Waktu Selesai"
        }, 0);

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void loadHistoriByRole(String role) {
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT t.id, t.judul, t.deskripsi, p.nama, h.bukti, h.waktu_selesai,
                       t.tanggal_tugas, t.waktu_tugas
                FROM histori_tugas h
                JOIN tugas t ON h.id_tugas = t.id
                JOIN profil p ON h.id_user = p.id
                JOIN akun a ON h.id_user = a.id
                WHERE a.role = ?
                ORDER BY h.waktu_selesai DESC
            """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("judul"),
                        rs.getString("deskripsi"),
                        rs.getString("nama"),
                        rs.getString("bukti"),
                        rs.getDate("tanggal_tugas"),
                        rs.getTime("waktu_tugas"),
                        rs.getTimestamp("waktu_selesai")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
