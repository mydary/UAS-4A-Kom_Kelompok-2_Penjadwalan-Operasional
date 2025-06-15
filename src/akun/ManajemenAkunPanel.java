package akun;

import database.DatabaseConnection;
import util.PasswordEncryptor;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class ManajemenAkunPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;

    private JTextField tfUsername, tfPassword, tfNama, tfJabatan, tfFoto;
    private JComboBox<String> cbRole;
    private JButton btnTambah, btnEdit, btnSimpan, btnHapus;

    private int selectedId = -1;

    public ManajemenAkunPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 15, 15, 15));
        initUI();
        loadData();
    }

    private void initUI() {
        // === FORM PANEL ===
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createTitledBorder("Form Akun"));

        JPanel inputGrid = new JPanel(new GridLayout(6, 2, 10, 10));
        tfUsername = new JTextField();
        tfPassword = new JTextField();
        cbRole = new JComboBox<>(new String[]{"manager", "Kasir", "Crew Ice cream", "Staff Dapur", "Staff Gudang", "Staff Kebersihan"});
        tfNama = new JTextField();
        tfJabatan = new JTextField();
        tfFoto = new JTextField();

        inputGrid.add(new JLabel("Username:"));
        inputGrid.add(tfUsername);
        inputGrid.add(new JLabel("Password:"));
        inputGrid.add(tfPassword);
        inputGrid.add(new JLabel("Role:"));
        inputGrid.add(cbRole);
        inputGrid.add(new JLabel("Nama:"));
        inputGrid.add(tfNama);
        inputGrid.add(new JLabel("Jabatan:"));
        inputGrid.add(tfJabatan);
        inputGrid.add(new JLabel("Foto (path):"));
        inputGrid.add(tfFoto);

        formPanel.add(inputGrid);

        // === BUTTON PANEL ===
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnTambah = new JButton("Tambah");
        btnEdit = new JButton("Edit");
        btnSimpan = new JButton("Simpan");
        btnHapus = new JButton("Hapus");

        btnEdit.setEnabled(false);
        btnSimpan.setEnabled(false);
        btnHapus.setEnabled(false);
        btnHapus.setForeground(Color.RED);

        btnTambah.addActionListener(e -> tambahAkun());
        btnEdit.addActionListener(e -> mulaiEdit());
        btnSimpan.addActionListener(e -> simpanPerubahan());
        btnHapus.addActionListener(e -> hapusAkun());

        buttonPanel.add(btnTambah);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnSimpan);
        buttonPanel.add(btnHapus);

        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(buttonPanel);

        add(formPanel, BorderLayout.NORTH);

        // === TABLE & SCROLLPANE ===
        model = new DefaultTableModel(new String[]{"ID", "Username", "Password", "Role", "Nama", "Jabatan", "Foto"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 1 ? new Color(0xf7e6eb) : Color.WHITE);
                } else {
                    c.setBackground(getSelectionBackground());
                }
                return c;
            }
        };

        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(25);
        table.getColumnModel().getColumn(0).setMaxWidth(25);

        table.getSelectionModel().addListSelectionListener(e -> {
            boolean selected = table.getSelectedRow() != -1;
            if (!e.getValueIsAdjusting() && selected) {
                btnEdit.setEnabled(true);
                btnHapus.setEnabled(true);
            }
        });

        // === CUSTOM HEADER ===
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBackground(Color.decode("#be375f"));
                label.setForeground(Color.WHITE);
                label.setFont(label.getFont().deriveFont(Font.BOLD));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setOpaque(true);
                return label;
            }
        });

        // === SCROLLPANE ===
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Daftar Akun"));

        // Ukuran scrollpane mengikuti jumlah baris (tidak membentang)
        int rowCount = model.getRowCount();
        int rowHeight = table.getRowHeight();
        int tableHeight = rowCount * rowHeight;
        table.setPreferredScrollableViewportSize(new Dimension(table.getPreferredSize().width, tableHeight));

        add(scrollPane, BorderLayout.CENTER);
    }


    private void loadData() {
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT a.id, a.username, a.password, a.role, p.nama, p.jabatan, p.foto
                FROM akun a
                LEFT JOIN profil p ON a.id = p.id
                """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String encryptedPass = rs.getString("password");
                String password = PasswordEncryptor.decrypt(encryptedPass);
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("username"),
                        password,
                        rs.getString("role"),
                        rs.getString("nama"),
                        rs.getString("jabatan"),
                        rs.getString("foto")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal load data akun!");
        }
        // Setelah data dimuat, sesuaikan tinggi tabel agar tidak membentang
        int rowCount = model.getRowCount();
        int rowHeight = table.getRowHeight();
        int tableHeight = rowCount * rowHeight;
        table.setPreferredScrollableViewportSize(new Dimension(table.getPreferredSize().width, tableHeight));
        table.revalidate();

    }

    private void tambahAkun() {
        String username = tfUsername.getText().trim();
        String password = tfPassword.getText().trim();
        String role = cbRole.getSelectedItem().toString();
        String nama = tfNama.getText().trim();
        String jabatan = tfJabatan.getText().trim();
        String foto = tfFoto.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan password wajib diisi!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            String sqlAkun = "INSERT INTO akun (username, password, role) VALUES (?, ?, ?)";
            PreparedStatement psAkun = conn.prepareStatement(sqlAkun, Statement.RETURN_GENERATED_KEYS);
            psAkun.setString(1, username);
            psAkun.setString(2, PasswordEncryptor.encrypt(password));
            psAkun.setString(3, role);
            psAkun.executeUpdate();

            ResultSet generatedKeys = psAkun.getGeneratedKeys();
            if (generatedKeys.next()) {
                int idBaru = generatedKeys.getInt(1);

                String sqlProfil = "INSERT INTO profil (id, nama, jabatan, foto) VALUES (?, ?, ?, ?)";
                PreparedStatement psProfil = conn.prepareStatement(sqlProfil);
                psProfil.setInt(1, idBaru);
                psProfil.setString(2, nama);
                psProfil.setString(3, jabatan);
                psProfil.setString(4, foto);
                psProfil.executeUpdate();

                conn.commit();

                JOptionPane.showMessageDialog(this, "Akun berhasil ditambahkan!");
                clearForm();
                loadData();
            } else {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "Gagal mendapatkan ID akun baru!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal tambah akun!");
        }
    }

    private void mulaiEdit() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih akun dulu di tabel!");
            return;
        }

        selectedId = (int) model.getValueAt(row, 0);
        tfUsername.setText(model.getValueAt(row, 1).toString());
        tfPassword.setText(model.getValueAt(row, 2).toString());
        cbRole.setSelectedItem(model.getValueAt(row, 3).toString());
        tfNama.setText(model.getValueAt(row, 4) == null ? "" : model.getValueAt(row, 4).toString());
        tfJabatan.setText(model.getValueAt(row, 5) == null ? "" : model.getValueAt(row, 5).toString());
        tfFoto.setText(model.getValueAt(row, 6) == null ? "" : model.getValueAt(row, 6).toString());

        btnTambah.setEnabled(false);
        btnEdit.setEnabled(false);
        btnSimpan.setEnabled(true);
    }

    private void simpanPerubahan() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Tidak ada akun yang sedang diedit!");
            return;
        }

        String username = tfUsername.getText().trim();
        String password = tfPassword.getText().trim();
        String role = cbRole.getSelectedItem().toString();
        String nama = tfNama.getText().trim();
        String jabatan = tfJabatan.getText().trim();
        String foto = tfFoto.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan password wajib diisi!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            String sqlAkun = "UPDATE akun SET username = ?, password = ?, role = ? WHERE id = ?";
            PreparedStatement psAkun = conn.prepareStatement(sqlAkun);
            psAkun.setString(1, username);
            psAkun.setString(2, PasswordEncryptor.encrypt(password));
            psAkun.setString(3, role);
            psAkun.setInt(4, selectedId);
            psAkun.executeUpdate();

            String cekProfilSql = "SELECT COUNT(*) FROM profil WHERE id = ?";
            PreparedStatement cekPs = conn.prepareStatement(cekProfilSql);
            cekPs.setInt(1, selectedId);
            ResultSet rs = cekPs.executeQuery();
            boolean profilAda = rs.next() && rs.getInt(1) > 0;

            if (profilAda) {
                String sqlProfil = "UPDATE profil SET nama = ?, jabatan = ?, foto = ? WHERE id = ?";
                PreparedStatement psProfil = conn.prepareStatement(sqlProfil);
                psProfil.setString(1, nama);
                psProfil.setString(2, jabatan);
                psProfil.setString(3, foto);
                psProfil.setInt(4, selectedId);
                psProfil.executeUpdate();
            } else {
                String sqlProfil = "INSERT INTO profil (id, nama, jabatan, foto) VALUES (?, ?, ?, ?)";
                PreparedStatement psProfil = conn.prepareStatement(sqlProfil);
                psProfil.setInt(1, selectedId);
                psProfil.setString(2, nama);
                psProfil.setString(3, jabatan);
                psProfil.setString(4, foto);
                psProfil.executeUpdate();
            }

            conn.commit();

            JOptionPane.showMessageDialog(this, "Perubahan berhasil disimpan!");
            clearForm();
            loadData();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal simpan perubahan!");
        }
    }

    private void hapusAkun() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih akun yang ingin dihapus!");
            return;
        }

        int id = (int) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Apakah yakin ingin menghapus akun ini?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);

                String sqlProfil = "DELETE FROM profil WHERE id = ?";
                PreparedStatement psProfil = conn.prepareStatement(sqlProfil);
                psProfil.setInt(1, id);
                psProfil.executeUpdate();

                String sqlAkun = "DELETE FROM akun WHERE id = ?";
                PreparedStatement psAkun = conn.prepareStatement(sqlAkun);
                psAkun.setInt(1, id);
                psAkun.executeUpdate();

                conn.commit();

                JOptionPane.showMessageDialog(this, "Akun berhasil dihapus!");
                clearForm();
                loadData();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal menghapus akun!");
            }
        }
    }

    private void clearForm() {
        tfUsername.setText("");
        tfPassword.setText("");
        cbRole.setSelectedIndex(0);
        tfNama.setText("");
        tfJabatan.setText("");
        tfFoto.setText("");

        btnTambah.setEnabled(true);
        btnEdit.setEnabled(false);
        btnSimpan.setEnabled(false);
        btnHapus.setEnabled(false);
        selectedId = -1;
        table.clearSelection();
    }
}
