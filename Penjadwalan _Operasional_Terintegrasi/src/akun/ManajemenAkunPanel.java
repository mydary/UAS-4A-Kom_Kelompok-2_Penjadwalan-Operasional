package akun;

import database.DatabaseConnection;
import util.PasswordEncryptor;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ManajemenAkunPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;

    private JTextField tfUsername, tfPassword, tfNama, tfJabatan, tfFoto;
    private JComboBox<String> cbRole;
    private JButton btnTambah, btnEdit, btnSimpan;

    private int selectedId = -1; // id akun yang dipilih untuk edit

    public ManajemenAkunPanel() {
        setLayout(new BorderLayout());
        initUI();
        loadData();
    }

    private void initUI() {
        // Form input
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 5, 5));
        tfUsername = new JTextField();
        tfPassword = new JTextField();
        cbRole = new JComboBox<>(new String[]{"manager", "Kasir", "Crew Ice cream", "Staff Dapur", "Staff Gudang", "Staff Kebersihan"});
        tfNama = new JTextField();
        tfJabatan = new JTextField();
        tfFoto = new JTextField();

        formPanel.add(new JLabel("Username:"));
        formPanel.add(tfUsername);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(tfPassword);
        formPanel.add(new JLabel("Role:"));
        formPanel.add(cbRole);
        formPanel.add(new JLabel("Nama:"));
        formPanel.add(tfNama);
        formPanel.add(new JLabel("Jabatan:"));
        formPanel.add(tfJabatan);
        formPanel.add(new JLabel("Foto (path):"));
        formPanel.add(tfFoto);

        btnTambah = new JButton("Tambah Akun");
        btnEdit = new JButton("Edit Akun");
        btnSimpan = new JButton("Simpan Perubahan");

        btnSimpan.setEnabled(false);
        btnEdit.setEnabled(false);

        btnTambah.addActionListener(e -> tambahAkun());
        btnEdit.addActionListener(e -> mulaiEdit());
        btnSimpan.addActionListener(e -> simpanPerubahan());

        formPanel.add(btnTambah);
        formPanel.add(btnEdit);
        formPanel.add(new JLabel("")); // kosong untuk grid balance
        formPanel.add(btnSimpan);

        add(formPanel, BorderLayout.NORTH);

        // Table akun
        model = new DefaultTableModel(new String[]{"ID", "Username", "Password", "Role", "Nama", "Jabatan", "Foto"}, 0) {
            // supaya kolom ID tidak bisa diedit langsung di tabel
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Saat baris dipilih, enable tombol Edit
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                btnEdit.setEnabled(true);
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
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

            // Insert akun
            String sqlAkun = "INSERT INTO akun (username, password, role) VALUES (?, ?, ?)";
            PreparedStatement psAkun = conn.prepareStatement(sqlAkun, Statement.RETURN_GENERATED_KEYS);
            psAkun.setString(1, username);
            psAkun.setString(2, PasswordEncryptor.encrypt(password)); // ← dienkripsi sebelum simpan
            psAkun.setString(3, role);
            psAkun.executeUpdate();


            ResultSet generatedKeys = psAkun.getGeneratedKeys();
            if (generatedKeys.next()) {
                int idBaru = generatedKeys.getInt(1);

                // Insert profil
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

            // Update akun
            String sqlAkun = "UPDATE akun SET username = ?, password = ?, role = ? WHERE id = ?";
            PreparedStatement psAkun = conn.prepareStatement(sqlAkun);
            psAkun.setString(1, username);
            psAkun.setString(2, PasswordEncryptor.encrypt(password));
            psAkun.setString(3, role);
            psAkun.setInt(4, selectedId);
            psAkun.executeUpdate();

            // Cek apakah profil sudah ada
            String cekProfilSql = "SELECT COUNT(*) FROM profil WHERE id = ?";
            PreparedStatement cekPs = conn.prepareStatement(cekProfilSql);
            cekPs.setInt(1, selectedId);
            ResultSet rs = cekPs.executeQuery();
            boolean profilAda = false;
            if (rs.next()) {
                profilAda = rs.getInt(1) > 0;
            }

            if (profilAda) {
                // Update profil
                String sqlProfil = "UPDATE profil SET nama = ?, jabatan = ?, foto = ? WHERE id = ?";
                PreparedStatement psProfil = conn.prepareStatement(sqlProfil);
                psProfil.setString(1, nama);
                psProfil.setString(2, jabatan);
                psProfil.setString(3, foto);
                psProfil.setInt(4, selectedId);
                psProfil.executeUpdate();
            } else {
                // Insert profil baru
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

            btnTambah.setEnabled(true);
            btnSimpan.setEnabled(false);
            selectedId = -1;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal simpan perubahan!");
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
        selectedId = -1;
        table.clearSelection();
    }
}

