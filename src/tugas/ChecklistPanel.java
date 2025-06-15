package tugas;

import database.DatabaseConnection;
import model.User;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class ChecklistPanel extends JPanel {
    private User user;
    private JPanel tugasPanel;
    private JTextField tfSearch;
    private String keywordTerakhir = "";

    public ChecklistPanel(User user) {
        this.user = user;
        setLayout(new BorderLayout());

        // === Header ===
        JPanel headerPanel = new JPanel(new BorderLayout());

        JLabel lblTitle = new JLabel("Daftar Tugas", SwingConstants.CENTER);
        lblTitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 18));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        headerPanel.add(lblTitle, BorderLayout.CENTER);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 10));
        tfSearch = new JTextField(15);
        JButton btnCari = new JButton("Cari");
        JButton btnExport = new JButton("Export PDF");

        searchPanel.add(new JLabel("Cari Judul:"));
        searchPanel.add(tfSearch);
        searchPanel.add(btnCari);
        searchPanel.add(btnExport);

        headerPanel.add(searchPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        btnCari.addActionListener(e -> {
            String keyword = tfSearch.getText().trim();
            if (btnCari.getText().equals("Cari")) {
                if (!keyword.isEmpty()) {
                    keywordTerakhir = keyword;
                    loadTugas(keyword);
                    btnCari.setText("Batal");
                    tfSearch.setEnabled(false);
                } else {
                    JOptionPane.showMessageDialog(this, "Masukkan kata kunci pencarian!");
                }
            } else {
                tfSearch.setText("");
                tfSearch.setEnabled(true);
                btnCari.setText("Cari");
                keywordTerakhir = "";
                loadTugas("");
            }
        });

        btnExport.addActionListener(e -> exportToPDF());

        tugasPanel = new JPanel();
        tugasPanel.setLayout(new BoxLayout(tugasPanel, BoxLayout.Y_AXIS));
        tugasPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        add(new JScrollPane(tugasPanel), BorderLayout.CENTER);

        loadTugas("");
    }

    private void loadTugas(String keyword) {
        tugasPanel.removeAll();
        Set<Integer> tugasSelesai = getTugasDenganStatus("selesai");
        Set<Integer> tugasTidakSelesai = getTugasDenganStatus("tidak selesai");

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM tugas WHERE role = ? AND judul LIKE ? ORDER BY tanggal_tugas ASC, waktu_tugas ASC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, user.getJabatan());
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();

            int nomor = 1;
            while (rs.next()) {
                int idTugas = rs.getInt("id");
                String judul = rs.getString("judul");
                String deskripsi = rs.getString("deskripsi");
                Date tanggal = rs.getDate("tanggal_tugas");
                Time waktu = rs.getTime("waktu_tugas");

                String teksTugas = nomor++ + ". " + judul + " - " + deskripsi + " (Tanggal: " + tanggal + ", Jam: " + waktu + ")";
                JLabel labelTugas = new JLabel(teksTugas);
                labelTugas.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));

                JButton btnSelesai = createStyledButton("Selesaikan", new Color(100, 149, 237), new Color(65, 105, 225));
                JButton btnTidakSelesai = createStyledButton("Tidak Selesai", new Color(220, 53, 69), new Color(200, 35, 51));

                JPanel panelButton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                panelButton.add(btnSelesai);
                panelButton.add(btnTidakSelesai);

                JPanel panel = new JPanel(new BorderLayout(10, 5));
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(200, 200, 200)),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
                panel.setBackground(new Color(250, 250, 250));
                panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

                if (tugasSelesai.contains(idTugas) || tugasTidakSelesai.contains(idTugas)) {
                    labelTugas.setEnabled(false);
                    btnSelesai.setEnabled(false);
                    btnTidakSelesai.setEnabled(false);
                    panel.setBackground(new Color(240, 240, 240));
                } else {
                    btnSelesai.addActionListener(e -> {
                        simpanHistori(idTugas, "selesai");
                        labelTugas.setEnabled(false);
                        btnSelesai.setEnabled(false);
                        btnTidakSelesai.setEnabled(false);
                        panel.setBackground(new Color(240, 240, 240));
                    });

                    btnTidakSelesai.addActionListener(e -> {
                        simpanHistori(idTugas, "tidak selesai");
                        labelTugas.setEnabled(false);
                        btnSelesai.setEnabled(false);
                        btnTidakSelesai.setEnabled(false);
                        panel.setBackground(new Color(240, 240, 240));
                    });
                }

                panel.add(labelTugas, BorderLayout.CENTER);
                panel.add(panelButton, BorderLayout.EAST);
                panel.setAlignmentX(Component.LEFT_ALIGNMENT);

                tugasPanel.add(panel);
                tugasPanel.add(Box.createVerticalStrut(8));
            }

            tugasPanel.revalidate();
            tugasPanel.repaint();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private JButton createStyledButton(String text, Color bgColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private Set<Integer> getTugasDenganStatus(String status) {
        Set<Integer> hasil = new HashSet<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id_tugas FROM histori_tugas WHERE id_user = ? AND status = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, user.getId());
            ps.setString(2, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                hasil.add(rs.getInt("id_tugas"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hasil;
    }

    private void simpanHistori(int idTugas, String status) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO histori_tugas (id_tugas, id_user, status, waktu_selesai) VALUES (?, ?, ?, NOW())";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idTugas);
            ps.setInt(2, user.getId());
            ps.setString(3, status);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Status tugas dicatat: " + status);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void exportToPDF() {
        try {
            Document document = new Document();
            String folderPath = System.getProperty("user.home") + "/Documents/TaskFlowPDF";
            new File(folderPath).mkdirs();
            String filePath = folderPath + "/" + user.getUsername() + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 16, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font tableFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 12);

            Paragraph title = new Paragraph("Daftar Tugas - " + user.getJabatan(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 4, 3, 2});

            table.addCell(new PdfPCell(new Phrase("No", tableFont)));
            table.addCell(new PdfPCell(new Phrase("Judul", tableFont)));
            table.addCell(new PdfPCell(new Phrase("Tanggal & Waktu", tableFont)));
            table.addCell(new PdfPCell(new Phrase("Status", tableFont)));

            Set<Integer> selesai = getTugasDenganStatus("selesai");
            Set<Integer> tidakSelesai = getTugasDenganStatus("tidak selesai");

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "SELECT * FROM tugas WHERE role = ? AND judul LIKE ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, user.getJabatan());
                ps.setString(2, "%" + keywordTerakhir + "%");
                ResultSet rs = ps.executeQuery();

                int no = 1;
                while (rs.next()) {
                    int idTugas = rs.getInt("id");
                    String judul = rs.getString("judul");
                    String deskripsi = rs.getString("deskripsi");
                    Date tanggal = rs.getDate("tanggal_tugas");
                    Time waktu = rs.getTime("waktu_tugas");

                    String status;
                    if (selesai.contains(idTugas)) {
                        status = "✓ Selesai";
                    } else if (tidakSelesai.contains(idTugas)) {
                        status = "✗ Tidak Selesai";
                    } else {
                        status = "- Belum Dikerjakan";
                    }

                    table.addCell(String.valueOf(no++));
                    table.addCell(judul + " - " + deskripsi);
                    table.addCell(tanggal + " " + waktu);
                    table.addCell(status);
                }
            }

            document.add(table);
            document.close();

            Object[] options = {"Buka File", "Tutup"};
            int choice = JOptionPane.showOptionDialog(this,
                    "PDF berhasil diekspor ke:\n" + filePath,
                    "Export PDF Sukses",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (choice == JOptionPane.YES_OPTION) {
                Desktop.getDesktop().open(new java.io.File(filePath));
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal export ke PDF.");
        }
    }
}
