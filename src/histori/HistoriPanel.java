package histori;

import database.DatabaseConnection;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.File;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.Font;
import java.io.FileOutputStream;
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
        tabPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 10));
        ButtonGroup btnGroup = new ButtonGroup();

        for (String role : roles) {
            JToggleButton btn = new JToggleButton(role);
            btn.setFocusPainted(false);
            btn.setPreferredSize(new Dimension(120, 28));
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(0xbe375f));
            if (role.equals(selectedRole)) {
                btn.setSelected(true);
                btn.setBackground(Color.WHITE);
                btn.setForeground(new Color(0xbe375f));
            }

            btn.addActionListener(e -> {
                selectedRole = role;
                loadHistoriByRole(selectedRole);
                resetSearch();
                highlightSelectedButton();
            });

            btnGroup.add(btn);
            tabPanel.add(btn);
        }

        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        JTextField tfSearch = new JTextField(15);
        inputPanel.add(new JLabel("Cari Judul:"));
        inputPanel.add(tfSearch);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        JButton btnCari = new JButton("Cari");
        JButton btnHapus = new JButton("Hapus");
        JButton btnExport = new JButton("Export PDF");
        btnExport.setFont(new java.awt.Font("Poppins", java.awt.Font.PLAIN, 12));
        btnExport.setBackground(new Color(0x2D9CDB));
        btnExport.setForeground(Color.WHITE);
        btnExport.setFocusPainted(false);
        btnExport.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        buttonPanel.add(btnCari);
        buttonPanel.add(btnHapus);
        buttonPanel.add(btnExport);

        searchPanel.add(inputPanel);
        searchPanel.add(buttonPanel);

        btnCari.addActionListener(e -> {
            String keyword = tfSearch.getText().trim();
            if (btnCari.getText().equals("Cari")) {
                if (!keyword.isEmpty()) {
                    loadHistoriByRoleAndKeyword(selectedRole, keyword);
                    btnCari.setText("Batal");
                    tfSearch.setEnabled(false);
                } else {
                    JOptionPane.showMessageDialog(this, "Masukkan kata kunci pencarian!");
                }
            } else {
                tfSearch.setText("");
                tfSearch.setEnabled(true);
                btnCari.setText("Cari");
                loadHistoriByRole(selectedRole);
            }
        });

        btnHapus.addActionListener(e -> hapusHistoriDipilih());
        btnExport.addActionListener(e -> exportToPDF());

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        northPanel.add(tabPanel);
        northPanel.add(Box.createVerticalStrut(10));
        northPanel.add(searchPanel);

        add(northPanel, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{
                "No", "Judul", "Deskripsi", "Nama Karyawan",
                "Status", "Tanggal Tugas", "Waktu Tugas", "Waktu Selesai", "ID_HISTORI"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(22);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(220, 220, 220));
        table.setBackground(Color.WHITE);
        table.setForeground(Color.BLACK);
        table.setSelectionBackground(new Color(0xF7E6EB));
        table.setSelectionForeground(Color.BLACK);
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(30);
        table.getColumnModel().getColumn(2).setPreferredWidth(250);
        table.removeColumn(table.getColumnModel().getColumn(8));

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(0xbe375f));
        header.setForeground(Color.WHITE);
        header.setFont(new java.awt.Font("Poppins", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        centerPanel.add(scrollPane, BorderLayout.NORTH);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void highlightSelectedButton() {
        for (Component comp : tabPanel.getComponents()) {
            if (comp instanceof JToggleButton btn) {
                if (btn.getText().equals(selectedRole)) {
                    btn.setBackground(Color.white);
                    btn.setForeground(new Color(0xbe375f));
                } else {
                    btn.setBackground(Color.white);;
                    btn.setForeground(new Color(0xbe375f));
                }
            }
        }
    }

    private void resetSearch() {
        Component[] northComponents = ((JPanel) getComponent(0)).getComponents();
        for (Component c : northComponents) {
            if (c instanceof JPanel searchPanel) {
                for (Component comp : searchPanel.getComponents()) {
                    if (comp instanceof JTextField tf) {
                        tf.setText("");
                        tf.setEnabled(true);
                    }
                    if (comp instanceof JButton btn) {
                        if (btn.getText().equals("Batal")) {
                            btn.setText("Cari");
                        }
                    }
                }
            }
        }
    }

    private void loadHistoriByRoleAndKeyword(String role, String keyword) {
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT h.id as histori_id, t.judul, t.deskripsi, p.nama, h.status, h.waktu_selesai,
                       t.tanggal_tugas, t.waktu_tugas
                FROM histori_tugas h
                JOIN tugas t ON h.id_tugas = t.id
                JOIN profil p ON h.id_user = p.id
                JOIN akun a ON h.id_user = a.id
                WHERE a.role = ? AND t.judul LIKE ?
                ORDER BY h.waktu_selesai DESC
            """;
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
                        rs.getString("nama"),
                        rs.getString("status"),
                        rs.getDate("tanggal_tugas"),
                        rs.getTime("waktu_tugas"),
                        rs.getTimestamp("waktu_selesai"),
                        rs.getInt("histori_id")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadHistoriByRole(String role) {
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT h.id as histori_id, t.judul, t.deskripsi, p.nama, h.status, h.waktu_selesai,
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
            int no = 1;
            while (rs.next()) {
                model.addRow(new Object[]{
                        no++,
                        rs.getString("judul"),
                        rs.getString("deskripsi"),
                        rs.getString("nama"),
                        rs.getString("status"),
                        rs.getDate("tanggal_tugas"),
                        rs.getTime("waktu_tugas"),
                        rs.getTimestamp("waktu_selesai"),
                        rs.getInt("histori_id")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void hapusHistoriDipilih() {
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Pilih setidaknya satu histori untuk dihapus.");
            return;
        }
        int konfirmasi = JOptionPane.showConfirmDialog(this,
                "Yakin ingin menghapus histori yang dipilih?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (konfirmasi != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM histori_tugas WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            for (int row : selectedRows) {
                int historiId = (int) model.getValueAt(row, 8);
                ps.setInt(1, historiId);
                ps.addBatch();
            }
            ps.executeBatch();
            JOptionPane.showMessageDialog(this, "Histori berhasil dihapus.");
            loadHistoriByRole(selectedRole);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void exportToPDF() {
        try {
            Document document = new Document();
            String folderPath = System.getProperty("user.home") + "/Documents/TaskFlowPDF";
            new File(folderPath).mkdirs();

            String filePath = folderPath + "/" + selectedRole.replace(" ", "_") + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 16, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font tableFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 11);

            Paragraph title = new Paragraph("Histori Tugas - " + selectedRole, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 3, 3, 2, 2, 2, 2});
            String[] headers = {"No", "Judul", "Deskripsi", "Nama", "Status", "Tgl Tugas", "Waktu Selesai"};

            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, tableFont));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = """
                    SELECT t.judul, t.deskripsi, p.nama, h.status, t.tanggal_tugas, t.waktu_tugas, h.waktu_selesai
                    FROM histori_tugas h
                    JOIN tugas t ON h.id_tugas = t.id
                    JOIN profil p ON h.id_user = p.id
                    JOIN akun a ON h.id_user = a.id
                    WHERE a.role = ?
                    ORDER BY h.waktu_selesai DESC
                """;
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, selectedRole);
                ResultSet rs = ps.executeQuery();
                int no = 1;

                while (rs.next()) {
                    table.addCell(String.valueOf(no++));
                    table.addCell(rs.getString("judul"));
                    table.addCell(rs.getString("deskripsi"));
                    table.addCell(rs.getString("nama"));
                    table.addCell(rs.getString("status"));
                    table.addCell(rs.getDate("tanggal_tugas") + " " + rs.getTime("waktu_tugas"));
                    table.addCell(rs.getTimestamp("waktu_selesai").toString());
                }
            }

            document.add(table);
            document.close();

            Object[] options = {"Buka File", "OK"};
            int choice = JOptionPane.showOptionDialog(this,
                    "PDF berhasil diekspor ke:\n" + filePath,
                    "Export Sukses",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[1]);

            if (choice == JOptionPane.YES_OPTION) {
                try {
                    Desktop.getDesktop().open(new java.io.File(filePath));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Gagal membuka file PDF.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal export PDF histori.");
        }
    }
}
