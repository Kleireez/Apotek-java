package controller;

import database.KoneksiDatabase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import view.ObatView; 

public class ObatController implements CrudInterface {
    
    private Connection conn;
    private ObatView view;

    // Constructor utama
    public ObatController(ObatView view) {
        this.view = view;
        try {
            this.conn = KoneksiDatabase.getConnection(); 
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Koneksi database gagal di ObatController: " + e.getMessage());
        }
    }

    private int ambilSupplierDefault() throws SQLException {
        String sql = "SELECT id_supplier FROM supplier ORDER BY id_supplier LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("id_supplier");
            }
        }
        throw new SQLException("Data supplier masih kosong. Tambahkan supplier terlebih dahulu.");
    }

    @Override
    public boolean tambah() {
        String idObat = view.getTxtIDObat().getText();
        String namaObat = view.getTxtNamaObat().getText();
        String jenis = view.getCmbJenis().getSelectedItem().toString();
        int stok = (int) view.getSpinStok().getValue();
        String hargaBeli = view.getTxtHargaBeli().getText();
        String hargaJual = view.getTxtHargaJual().getText();
        java.util.Date tglKadaluarsa = view.getjDateChooserKadaluarsa().getDate();

        // Validasi input form kosong
        if (idObat.isEmpty() || namaObat.isEmpty() || tglKadaluarsa == null || hargaBeli.isEmpty() || hargaJual.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Semua data form obat harus diisi lengkap!");
            return false;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String tglSql = sdf.format(tglKadaluarsa);

        String sql = "INSERT INTO obat (id_obat, id_supplier, nama_obat, jenis, stok, harga_beli, harga_jual, tanggal_kadaluarsa) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            int idSupplier = ambilSupplierDefault();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(idObat));
            ps.setInt(2, idSupplier);
            ps.setString(3, namaObat);
            ps.setString(4, jenis);
            ps.setInt(5, stok);
            ps.setInt(6, Integer.parseInt(hargaBeli));
            ps.setInt(7, Integer.parseInt(hargaJual));
            ps.setString(8, tglSql);

            int result = ps.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(null, "Data Obat Baru Berhasil Ditambahkan!");
                tampilkan(); // Refresh data JTable otomatis
                reset();     // Bersihkan form inputan
                return true;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Gagal menambah data obat: " + e.getMessage());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "ID, harga beli, dan harga jual harus berupa angka!");
        }
        return false;
    }

    @Override
    public boolean ubah() {
        String idObat = view.getTxtIDObat().getText();
        String namaObat = view.getTxtNamaObat().getText();
        String jenis = view.getCmbJenis().getSelectedItem().toString();
        int stok = (int) view.getSpinStok().getValue();
        String hargaBeli = view.getTxtHargaBeli().getText();
        String hargaJual = view.getTxtHargaJual().getText();
        java.util.Date tglKadaluarsa = view.getjDateChooserKadaluarsa().getDate();

        if (idObat.isEmpty() || namaObat.isEmpty() || tglKadaluarsa == null || hargaBeli.isEmpty() || hargaJual.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Pilih data obat dan lengkapi semua field sebelum mengubah!");
            return false;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String tglSql = sdf.format(tglKadaluarsa);

        String sql = "UPDATE obat SET nama_obat=?, jenis=?, stok=?, harga_beli=?, harga_jual=?, tanggal_kadaluarsa=? WHERE id_obat=?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, namaObat);
            ps.setString(2, jenis);
            ps.setInt(3, stok);
            ps.setInt(4, Integer.parseInt(hargaBeli));
            ps.setInt(5, Integer.parseInt(hargaJual));
            ps.setString(6, tglSql);
            ps.setInt(7, Integer.parseInt(idObat));

            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Data Obat Berhasil Diperbarui!");
            tampilkan();
            reset();
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Gagal mengubah data obat: " + e.getMessage());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "ID, harga beli, dan harga jual harus berupa angka!");
        }
        return false;
    }

    @Override
    public boolean hapus() {
        String idObat = view.getTxtIDObat().getText();
        if (idObat.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Masukkan ID Obat yang ingin dihapus!");
            return false;
        }

        String sql = "DELETE FROM obat WHERE id_obat = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(idObat));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Data Obat Berhasil Dihapus!");
            tampilkan();
            reset();
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Gagal menghapus data obat: " + e.getMessage());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "ID Obat harus berupa angka!");
        }
        return false;
    }

    /**
     * TAMPILKAN: Mengambil seluruh data dari database untuk dimasukkan ke JTable bawah.
     */
    @Override
    public void tampilkan() {
        javax.swing.JTable tabel = view.getTabelObat();
        if (tabel == null || conn == null) {
            return;
        }

        DefaultTableModel model = (DefaultTableModel) tabel.getModel();
        model.setRowCount(0); // Kosongkan data lama di tabel view

        String sql = "SELECT * FROM obat ORDER BY id_obat";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            int no = 1;
            while (rs.next()) {
                Object[] row = {
                    no++,
                    rs.getInt("id_obat"),
                    rs.getString("nama_obat"),
                    rs.getString("jenis"),
                    rs.getInt("stok"),
                    rs.getInt("harga_beli"),
                    rs.getInt("harga_jual"),
                    rs.getDate("tanggal_kadaluarsa")
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Gagal memuat data obat: " + e.getMessage());
        }
    }

    public void reset() {
        view.getTxtIDObat().setText("");
        view.getTxtNamaObat().setText("");
        view.getTxtHargaBeli().setText("");
        view.getTxtHargaJual().setText("");
        
        view.getCmbJenis().setSelectedIndex(0);
        view.getSpinStok().setValue(0);
        
        if (view.getjDateChooserKadaluarsa() != null) {
            view.getjDateChooserKadaluarsa().setDate(null);
            view.getjDateChooserKadaluarsa().setCalendar(null);
            view.getjDateChooserKadaluarsa().repaint();
        }
    }
}
