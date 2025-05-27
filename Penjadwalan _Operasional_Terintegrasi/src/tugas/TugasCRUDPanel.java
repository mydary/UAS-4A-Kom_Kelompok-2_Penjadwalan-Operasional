package tugas;

import com.toedter.calendar.JDateChooser;
import database.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TugasCRUDPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JTextField tfJudul, tfDeskripsi;
    private JDateChooser dateChooser;
    private JSpinner timeSpinner;

    private JPanel tabPanel;
    private JButton btnTambah, btnEdit, btnHapus;
    private String[] roles = {"Kasir", "Crew Ice cream", "Staff Dapur", "Staff Gudang", "Staff Kebersihan"};
    private String selectedRole = roles[0];
    private int selectedId = -1;

    public TugasCRUDPanel() {
        setLayout(new BorderLayout());
        initUI();
        loadData(selectedRole);
    }

    private void initUI() {
        // Panel tab role
        tabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ButtonGroup btnGroup = new ButtonGroup();
        for (String role : roles) {
            JToggleButton btn = new JToggleButton(role);
            btn.setFocusPainted(false);
            if (role.equals(selectedRole)) btn.setSelected(true);
            btn.addActionListener(e -> {
                selectedRole = role;
                loadData(selectedRole);
                clearForm();
            });
            btnGroup.add(btn);
            tabPanel.add(btn);
        }
        add(tabPanel, BorderLayout.NORTH);

        // Form input tugas
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 5, 5));
        tfJudul = new JTextField();
        tfDeskripsi = new JTextField();

        dateChooser = new JDateChooser();
        // Set tanggal default satu kali saat init UI
        dateChooser.setDate(new Date());
        dateChooser.setDateFormatString("yyyy-MM-dd");

        timeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);
        timeSpinner.setValue(new Date());

        formPanel.add(new JLabel("Judul:"));
        formPanel.add(tfJudul);
        formPanel.add(new JLabel("Deskripsi:"));
        formPanel.add(tfDeskripsi);
        formPanel.add(new JLabel("Tanggal Tugas:"));
        formPanel.add(dateChooser);
        formPanel.add(new JLabel("Waktu Tugas:"));
        formPanel.add(timeSpinner);

        btnTambah = new JButton("Tambah Tugas");
        btnEdit = new JButton("Simpan Perubahan");
        btnHapus = new JButton("Hapus Tugas");

        btnEdit.setEnabled(false);
        btnHapus.setEnabled(false);

        btnTambah.addActionListener(e -> tambahTugas());
        btnEdit.addActionListener(e -> updateTugas());
        btnHapus.addActionListener(e -> hapusTugas());

        formPanel.add(btnTambah);
        formPanel.add(btnEdit);
        formPanel.add(new JLabel());
        formPanel.add(btnHapus);

        add(formPanel, BorderLayout.CENTER);

        // Tabel tugas dengan multi selection
        model = new DefaultTableModel(new String[]{"ID", "Judul", "Deskripsi", "Role", "Tanggal", "Waktu"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table = new JTable(model);
        // Ubah ke multi selection
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int[] selectedRows = table.getSelectedRows();
                if (selectedRows.length == 1) {
                    selectedId = (int) model.getValueAt(selectedRows[0], 0);
                    tfJudul.setText(model.getValueAt(selectedRows[0], 1).toString());
                    tfDeskripsi.setText(model.getValueAt(selectedRows[0], 2).toString());
                    btnEdit.setEnabled(true);
                    btnTambah.setEnabled(false);
                } else {
                    selectedId = -1;
                    tfJudul.setText("");
                    tfDeskripsi.setText("");
                    btnEdit.setEnabled(false);
                    btnTambah.setEnabled(true);
                }
                btnHapus.setEnabled(selectedRows.length > 0);
            }
        });

        add(new JScrollPane(table), BorderLayout.SOUTH);
    }

    private void tambahTugas() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO tugas (judul, deskripsi, role, tanggal_tugas, waktu_tugas) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, tfJudul.getText());
            ps.setString(2, tfDeskripsi.getText());
            ps.setString(3, selectedRole);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");
            ps.setString(4, df.format(dateChooser.getDate()));
            ps.setString(5, tf.format((Date) timeSpinner.getValue()));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Tugas berhasil ditambahkan!");
            clearForm();
            loadData(selectedRole);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal menambah tugas.");
        }
    }

    private void updateTugas() {
        if (selectedId == -1) return;
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE tugas SET judul = ?, deskripsi = ?, tanggal_tugas = ?, waktu_tugas = ? WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, tfJudul.getText());
            ps.setString(2, tfDeskripsi.getText());
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");
            ps.setString(3, df.format(dateChooser.getDate()));
            ps.setString(4, tf.format((Date) timeSpinner.getValue()));
            ps.setInt(5, selectedId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Tugas diperbarui.");
            clearForm();
            loadData(selectedRole);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void hapusTugas() {
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Pilih setidaknya satu tugas untuk dihapus.");
            return;
        }
        int konfirmasi = JOptionPane.showConfirmDialog(this,
                "Yakin ingin menghapus " + selectedRows.length + " tugas?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (konfirmasi != JOptionPane.YES_OPTION) return;
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM tugas WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            for (int row : selectedRows) {
                int id = (int) model.getValueAt(row, 0);
                ps.setInt(1, id);
                ps.addBatch();
            }
            ps.executeBatch();
            JOptionPane.showMessageDialog(this, "Tugas berhasil dihapus.");
            clearForm();
            loadData(selectedRole);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadData(String role) {
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM tugas WHERE role = ? ORDER BY tanggal_tugas DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("judul"),
                        rs.getString("deskripsi"),
                        rs.getString("role"),
                        rs.getDate("tanggal_tugas"),
                        rs.getTime("waktu_tugas")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearForm() {
        tfJudul.setText("");
        tfDeskripsi.setText("");
        // Jangan reset tanggal agar tidak mengganggu input berulang untuk hari yang sama
        timeSpinner.setValue(new Date());
        selectedId = -1;
        btnTambah.setEnabled(true);
        btnEdit.setEnabled(false);
        btnHapus.setEnabled(false);
        table.clearSelection();
    }
}
