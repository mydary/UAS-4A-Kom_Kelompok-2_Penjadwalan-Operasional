package catatan;

import database.DatabaseConnection;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.File;
import java.io.FileOutputStream;
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
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        Color merahTua = Color.decode("#be375f");

        // Judul besar
        JLabel labelJudul = new JLabel("Catatan ke Manager");
        labelJudul.setFont(new java.awt.Font("Poppins", java.awt.Font.BOLD, 20));
        labelJudul.setForeground(merahTua);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        add(labelJudul, gbc);

        // Deskripsi miring
        JLabel labelDeskripsi = new JLabel("<html><i>Catatan ini diperuntukan sebagai masukan terkait tugas yang diberikan oleh manager dan alasan jika ada tugas yang tidak terselesaikan</i></html>");
        labelDeskripsi.setFont(new java.awt.Font("Poppins", java.awt.Font.ITALIC, 13));
        labelDeskripsi.setForeground(merahTua);
        gbc.gridy++;
        add(labelDeskripsi, gbc);

        // Garis pemisah
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setPreferredSize(new Dimension(400, 2));
        separator.setForeground(merahTua);
        gbc.gridy++;
        add(separator, gbc);

        // Text area catatan
        areaCatatan = new JTextArea(10, 30);
        areaCatatan.setLineWrap(true);
        areaCatatan.setWrapStyleWord(true);
        areaCatatan.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        areaCatatan.setForeground(Color.GRAY);
        areaCatatan.setText("Ketik catatan...");
        areaCatatan.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (areaCatatan.getText().equals("Ketik catatan...")) {
                    areaCatatan.setText("");
                    areaCatatan.setForeground(Color.BLACK);
                }
            }

            public void focusLost(java.awt.event.FocusEvent e) {
                if (areaCatatan.getText().isEmpty()) {
                    areaCatatan.setForeground(Color.GRAY);
                    areaCatatan.setText("Ketik catatan...");
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(areaCatatan);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        scrollPane.setMaximumSize(new Dimension(400, 200));

        gbc.gridy++;
        add(scrollPane, gbc);

        // Spasi
        gbc.gridy++;
        add(Box.createVerticalStrut(15), gbc);

        // Tombol kirim
        btnKirim = new JButton("Kirim Catatan");
        btnKirim.setBackground(merahTua);
        btnKirim.setForeground(Color.WHITE);
        btnKirim.setFocusPainted(false);
        btnKirim.setFont(new java.awt.Font("Poppins", java.awt.Font.PLAIN, 14));
        btnKirim.addActionListener(e -> kirimCatatan());
        gbc.gridy++;
        add(btnKirim, gbc);
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
        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tabPanel.setBackground(Color.white);
        ButtonGroup group = new ButtonGroup();

        for (String role : roles) {
            JToggleButton button = new JToggleButton(role);

            button.setBackground(Color.WHITE);
            button.setForeground(new Color(0xbe375f));
            button.setFont(new java.awt.Font("Poppins", java.awt.Font.PLAIN, 12));
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

            button.addChangeListener(e -> {
                if (button.isSelected()) {
                    button.setBackground(Color.WHITE);
                    button.setForeground(new Color(0xbe375f));
                } else {
                    button.setBackground(Color.WHITE);
                    button.setForeground(new Color(0xbe375f));
                }
            });

            if (role.equals(selectedRole)) button.setSelected(true);
            button.addActionListener(e -> {
                selectedRole = role;
                showDaftarCatatan(selectedRole);
            });

            group.add(button);
            tabPanel.add(button);
        }

        JButton btnExport = new JButton("Export PDF");
        btnExport.setFont(new java.awt.Font("Poppins", java.awt.Font.PLAIN, 12));
        btnExport.setBackground(new Color(0x2D9CDB));
        btnExport.setForeground(Color.WHITE);
        btnExport.setFocusPainted(false);
        btnExport.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        btnExport.addActionListener(e -> exportToPDF());
        tabPanel.add(Box.createHorizontalStrut(20));
        tabPanel.add(btnExport);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(0xf9ad00));
        scrollPane.setBackground(new Color(0xf9ad00));


        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(new Color(0xf9ad00));
        wrapperPanel.add(scrollPane, BorderLayout.CENTER);

        add(tabPanel, BorderLayout.NORTH);
        add(wrapperPanel, BorderLayout.CENTER);

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

                JPanel catatanItem = new JPanel(new BorderLayout(10, 10));
                catatanItem.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                catatanItem.setBackground(Color.WHITE);

                String isiFull = "Catatan #" + count + " - " + waktu.toString() + "\n\n" + isi;

                JTextArea note = new JTextArea(isiFull);
                note.setWrapStyleWord(true);
                note.setLineWrap(true);
                note.setEditable(false);
                note.setFont(new java.awt.Font("Poppins", java.awt.Font.PLAIN, 12));
                note.setBackground(new Color(245, 245, 245));
                note.setForeground(Color.DARK_GRAY);
                note.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                JScrollPane scrollNote = new JScrollPane(note);
                scrollNote.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                scrollNote.setPreferredSize(new Dimension(500, 120));
                scrollNote.setMaximumSize(new Dimension(500, 120));
                scrollNote.setMinimumSize(new Dimension(500, 120));
                scrollNote.setAlignmentX(Component.LEFT_ALIGNMENT);

                JButton btnHapus = new JButton("X");
                btnHapus.setToolTipText("Hapus catatan ini");
                btnHapus.setFont(new java.awt.Font("Poppins", java.awt.Font.BOLD, 12));
                btnHapus.setBackground(Color.RED);
                btnHapus.setForeground(Color.WHITE);
                btnHapus.setFocusPainted(false);
                btnHapus.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                btnHapus.setPreferredSize(new Dimension(50, 30));

                btnHapus.addActionListener(e -> {
                    int konfirmasi = JOptionPane.showConfirmDialog(this,
                            "Yakin ingin menghapus catatan ini?",
                            "Konfirmasi Hapus",
                            JOptionPane.YES_NO_OPTION);
                    if (konfirmasi == JOptionPane.YES_OPTION) {
                        hapusCatatan(idCatatan);
                        showDaftarCatatan(selectedRole);
                    }
                });

                catatanItem.add(scrollNote, BorderLayout.CENTER);
                catatanItem.add(btnHapus, BorderLayout.EAST);
                catatanItem.setMaximumSize(new Dimension(520, 140));

                listPanel.add(catatanItem);
                listPanel.add(Box.createVerticalStrut(10));
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
            com.itextpdf.text.Font bodyFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 12);

            Paragraph title = new Paragraph("Catatan Karyawan - " + selectedRole, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = """
                SELECT p.nama, c.isi, c.waktu
                FROM catatan c
                JOIN akun a ON c.id_user = a.id
                JOIN profil p ON a.id = p.id
                WHERE p.jabatan = ?
                ORDER BY c.waktu DESC
                """;
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, selectedRole);
                ResultSet rs = ps.executeQuery();

                int no = 1;
                while (rs.next()) {
                    String nama = rs.getString("nama");
                    String isi = rs.getString("isi");
                    Timestamp waktu = rs.getTimestamp("waktu");

                    Paragraph entry = new Paragraph(
                            no++ + ". " + nama + " - " + waktu.toString() + "\n" + isi + "\n\n",
                            bodyFont
                    );
                    document.add(entry);
                }
            }

            document.close();

            // Tombol untuk dialog
            Object[] options = {"Buka File", "Tutup"};
            int pilihan = JOptionPane.showOptionDialog(this,
                    "Catatan berhasil diekspor ke:\n" + filePath,
                    "Export PDF Sukses",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (pilihan == JOptionPane.YES_OPTION) {
                // Buka file PDF dengan aplikasi default OS
                try {
                    java.awt.Desktop.getDesktop().open(new java.io.File(filePath));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Gagal membuka file.\n" + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal mengekspor catatan ke PDF.");
        }
    }


}
