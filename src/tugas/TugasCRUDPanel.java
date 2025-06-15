package tugas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.DefaultTableCellRenderer;
import com.toedter.calendar.JDateChooser;
import database.DatabaseConnection;
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
    private boolean sedangCari = false;

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
        tabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        ButtonGroup btnGroup = new ButtonGroup();
        for (String role : roles) {
            JToggleButton btn = new JToggleButton(role);
            btn.setFocusPainted(false);
            btn.setOpaque(true);
            btn.setBackground(Color.white);
            btn.setForeground(new Color(0xbe375f));
            btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            if (role.equals(selectedRole)) {
                btn.setSelected(true);
                btn.setBackground(Color.white);
                btn.setForeground(new Color(0xbe375f));
            }

            btn.addActionListener(e -> {
                selectedRole = role;
                loadData(selectedRole);
                clearForm();
                // Reset semua tombol ke style default
                for (Component comp : tabPanel.getComponents()) {
                    if (comp instanceof JToggleButton toggleBtn) {
                        toggleBtn.setBackground(Color.white);
                        toggleBtn.setForeground(new Color(0xbe375f));
                    }
                }
                // Set tombol yang dipilih dengan style aktif
                btn.setBackground(Color.white);
                btn.setForeground(new Color(0xbe375f));
            });

            btnGroup.add(btn);
            tabPanel.add(btn);
        }


        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JTextField tfSearch = new JTextField(15);
        JButton btnCariBatal = new JButton("Cari");

        btnCariBatal.addActionListener(e -> {
            if (!sedangCari) {
                String keyword = tfSearch.getText().trim();
                if (!keyword.isEmpty()) {
                    searchData(selectedRole, keyword);
                    sedangCari = true;
                    btnCariBatal.setText("Batal");
                    tfSearch.setEnabled(false);
                }
            } else {
                tfSearch.setText("");
                tfSearch.setEnabled(true);
                loadData(selectedRole);
                sedangCari = false;
                btnCariBatal.setText("Cari");
            }
        });

        searchPanel.add(new JLabel("Cari Judul:"));
        searchPanel.add(tfSearch);
        searchPanel.add(btnCariBatal);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(tabPanel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Form panel dengan GridBagLayout agar lebih fleksibel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        formPanel.setBackground(Color.white);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; // Tambahkan ini agar komponen bisa melebar

        tfJudul = new JTextField(20); // Ukuran preferensi awal (opsional)
        tfDeskripsi = new JTextField(20);
        dateChooser = new JDateChooser();
        dateChooser.setDate(new Date());
        dateChooser.setDateFormatString("yyyy-MM-dd");

        timeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);
        timeSpinner.setValue(new Date());

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; formPanel.add(new JLabel("Judul:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; formPanel.add(tfJudul, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; formPanel.add(new JLabel("Deskripsi:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; formPanel.add(tfDeskripsi, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; formPanel.add(new JLabel("Tanggal Tugas:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; formPanel.add(dateChooser, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; formPanel.add(new JLabel("Waktu Tugas:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; formPanel.add(timeSpinner, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0)); // jarak antar tombol 15px
        btnTambah = new JButton("Tambah Tugas");
        btnEdit = new JButton("Simpan Perubahan");
        btnHapus = new JButton("Hapus Tugas");
        btnHapus.setForeground(Color.red);

// (Opsional) atur ukuran dan warna tombol agar seragam
        Dimension btnSize = new Dimension(140, 30);
        btnTambah.setPreferredSize(btnSize);
        btnEdit.setPreferredSize(btnSize);
        btnHapus.setPreferredSize(btnSize);
        buttonPanel.setBackground(Color.white);
        buttonPanel.add(btnTambah);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnHapus);

// Tambahkan ke formPanel
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonPanel, gbc);

// === Button Pulihkan di bawah tombol-tombol ===
        row++;
        gbc.gridy = row;
        JButton btnPulihkan = new JButton("Pulihkan Tugas Sebelumnya");
        btnPulihkan.setPreferredSize(new Dimension(250, 30));
        btnPulihkan.setForeground(Color.blue);
        formPanel.add(btnPulihkan, gbc);
        btnPulihkan.addActionListener(e -> pulihkanTugas());

// === Listener & state ===
        btnEdit.setEnabled(false);
        btnHapus.setEnabled(false);
        btnTambah.addActionListener(e -> tambahTugas());
        btnEdit.addActionListener(e -> updateTugas());
        btnHapus.addActionListener(e -> hapusTugas());

        add(formPanel, BorderLayout.CENTER);


        model = new DefaultTableModel(new String[]{"No", "Judul", "Deskripsi", "Role", "Tanggal", "Waktu", "ID"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(model);
        table.removeColumn(table.getColumnModel().getColumn(6));
        // Atur lebar kolom
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(10);  // Kolom "No" (lebih kecil)
        columnModel.getColumn(1).setPreferredWidth(150); // Kolom "Judul"
        columnModel.getColumn(2).setPreferredWidth(250); // Kolom "Deskripsi" (lebih besar)
        columnModel.getColumn(3).setPreferredWidth(100); // Kolom "Role"
        columnModel.getColumn(4).setPreferredWidth(100); // Kolom "Tanggal"
        columnModel.getColumn(5).setPreferredWidth(80);  // Kolom "Waktu"


        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int[] selectedRows = table.getSelectedRows();
                if (selectedRows.length == 1) {
                    selectedId = (int) model.getValueAt(selectedRows[0], 6);
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

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 30, 10));
        add(scrollPane, BorderLayout.SOUTH);
        // Header warna: #be375f dan teks putih
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(0xbe375f));  // Background header
        header.setForeground(Color.WHITE);          // Teks header
        header.setFont(header.getFont().deriveFont(Font.BOLD));

// Alternating row color: #f7e6eb setiap baris genap
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(Color.WHITE);
                    } else {
                        c.setBackground(new Color(0xf7e6eb));
                    }
                } else {
                    c.setBackground(table.getSelectionBackground());
                }
                return c;
            }
        });

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
                int id = (int) model.getValueAt(row, 6); // ambil ID dari kolom tersembunyi
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

    private void pulihkanTugas() {
        int konfirmasi = JOptionPane.showConfirmDialog(this,
                "Tugas dari hari sebelumnya akan dipulihkan untuk role " + selectedRole + ". Lanjutkan?",
                "Konfirmasi",
                JOptionPane.YES_NO_OPTION);
        if (konfirmasi != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
            INSERT INTO tugas (judul, deskripsi, role, tanggal_tugas, waktu_tugas)
            SELECT judul, deskripsi, role, tanggal_tugas, waktu_tugas
            FROM tugas
            WHERE role = ? AND tanggal_tugas = (
                SELECT MAX(tanggal_tugas) FROM tugas WHERE role = ? AND tanggal_tugas < CURDATE()
            )
            """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, selectedRole);
            ps.setString(2, selectedRole);
            int affected = ps.executeUpdate();

            if (affected > 0) {
                JOptionPane.showMessageDialog(this, "Berhasil memulihkan " + affected + " tugas.");
            } else {
                JOptionPane.showMessageDialog(this, "Tidak ada tugas yang bisa dipulihkan.");
            }

            loadData(selectedRole);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memulihkan tugas.");
        }
    }



    private void loadData(String role) {
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM tugas WHERE role = ? ORDER BY tanggal_tugas ASC, waktu_tugas ASC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            int no = 1;
            while (rs.next()) {
                model.addRow(new Object[]{
                        no++,
                        rs.getString("judul"),
                        rs.getString("deskripsi"),
                        rs.getString("role"),
                        rs.getDate("tanggal_tugas"),
                        rs.getTime("waktu_tugas"),
                        rs.getInt("id")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        int rowHeight = table.getRowHeight();
        int rowCount = table.getRowCount();
        int tableHeight = rowHeight * rowCount;

// Tambahkan sedikit padding jika ingin
        table.setPreferredScrollableViewportSize(new Dimension(
                table.getPreferredScrollableViewportSize().width,
                tableHeight > 0 ? tableHeight : rowHeight * 3  // minimal 3 baris supaya tidak terlalu kecil
        ));

        table.revalidate();  // paksa layout agar refresh

    }


    private void searchData(String role, String keyword) {
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM tugas WHERE role = ? AND judul LIKE ? ORDER BY tanggal_tugas DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, role);
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            int no = 1;
            while (rs.next()) {
                model.addRow(new Object[]{
                        no++,
                        rs.getString("judul"),
                        rs.getString("deskripsi"),
                        rs.getString("role"),
                        rs.getDate("tanggal_tugas"),
                        rs.getTime("waktu_tugas"),
                        rs.getInt("id")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearForm() {
        tfJudul.setText("");
        tfDeskripsi.setText("");
        timeSpinner.setValue(new Date());
        selectedId = -1;
        btnTambah.setEnabled(true);
        btnEdit.setEnabled(false);
        btnHapus.setEnabled(false);
        table.clearSelection();
    }
}
