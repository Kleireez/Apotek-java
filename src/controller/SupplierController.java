package controller;

import database.KoneksiDatabase;
import view.SupplierView;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class SupplierController implements CrudInterface {
    
    private SupplierView view;

    public SupplierController(SupplierView view) {
        this.view = view;
    }

    @Override
    public boolean tambah() {
        String nama = view.getTxtNamaSupplier().getText();
        String alamat = view.getTxtAlamat().getText();
        String telp = view.getTxtNoTelp().getText();

        // Validasi input kosong
        if (nama.isEmpty() || alamat.isEmpty() || telp.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Semua field harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        String sql = "INSERT INTO supplier (nama_supplier, alamat, no_telepon) VALUES (?, ?, ?)";
        try (Connection conn = KoneksiDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, nama);
            ps.setString(2, alamat);
            ps.setString(3, telp);
            
            int hasil = ps.executeUpdate();
            if (hasil > 0) {
                JOptionPane.showMessageDialog(view, "Data Supplier berhasil ditambahkan!");
                reset();
                tampilkan(); // Refresh tabel setelah ditambah
                return true;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Gagal menambah data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    @Override
    public boolean ubah() {
        int baris = view.getTableSupplier().getSelectedRow();
        if (baris == -1) {
            JOptionPane.showMessageDialog(view, "Pilih data di tabel terlebih dahulu yang ingin diubah!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        int modelRow = view.getTableSupplier().convertRowIndexToModel(baris);
        String idSupplier = view.getTableSupplier().getModel().getValueAt(modelRow, 1).toString();
        
        String namaBaru = view.getTxtNamaSupplier().getText();
        String alamat = view.getTxtAlamat().getText();
        String telp = view.getTxtNoTelp().getText();

        if (namaBaru.isEmpty() || alamat.isEmpty() || telp.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Semua field harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        String sql = "UPDATE supplier SET nama_supplier = ?, alamat = ?, no_telepon = ? WHERE id_supplier = ?";
        try (Connection conn = KoneksiDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, namaBaru);
            ps.setString(2, alamat);
            ps.setString(3, telp);
            ps.setInt(4, Integer.parseInt(idSupplier));
            
            int hasil = ps.executeUpdate();
            if (hasil > 0) {
                JOptionPane.showMessageDialog(view, "Data Supplier berhasil diubah!");
                reset();
                tampilkan(); // Refresh tabel setelah diubah
                return true;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Gagal mengubah data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    @Override
    public boolean hapus() {
        int baris = view.getTableSupplier().getSelectedRow();
        if (baris == -1) {
            JOptionPane.showMessageDialog(view, "Pilih data di tabel terlebih dahulu yang ingin dihapus!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        int modelRow = view.getTableSupplier().convertRowIndexToModel(baris);
        String idSupplier = view.getTableSupplier().getModel().getValueAt(modelRow, 1).toString();
        String nama = view.getTableSupplier().getModel().getValueAt(modelRow, 2).toString();
        
        int konfirmasi = JOptionPane.showConfirmDialog(view, "Yakin ingin menghapus supplier '" + nama + "'?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (konfirmasi == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM supplier WHERE id_supplier = ?";
            try (Connection conn = KoneksiDatabase.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                ps.setInt(1, Integer.parseInt(idSupplier));
                
                int hasil = ps.executeUpdate();
                if (hasil > 0) {
                    JOptionPane.showMessageDialog(view, "Data Supplier berhasil dihapus!");
                    reset();
                    tampilkan(); // Refresh tabel setelah dihapus
                    return true;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(view, "Gagal menghapus data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return false;
    }

    @Override
    public void tampilkan() {
        DefaultTableModel model = (DefaultTableModel) view.getTableSupplier().getModel();
        model.setColumnIdentifiers(new Object[]{"No", "ID Supplier", "Nama Supplier", "Alamat", "No. Telepon"});
        model.setRowCount(0); // Membersihkan isi data lama pada tabel di aplikasi
        
        String sql = "SELECT id_supplier, nama_supplier, alamat, no_telepon FROM supplier ORDER BY id_supplier";
        try (Connection conn = KoneksiDatabase.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            int no = 1;
            while (rs.next()) {
                model.addRow(new Object[]{
                    no++,
                    rs.getInt("id_supplier"),
                    rs.getString("nama_supplier"), // Sesuaikan dengan nama kolom di tabel MySQL kamu
                    rs.getString("alamat"),
                    rs.getString("no_telepon")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Gagal memuat data ke tabel: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void reset() {
        view.getTxtNamaSupplier().setText("");
        view.getTxtAlamat().setText("");
        view.getTxtNoTelp().setText("");
        view.getTableSupplier().clearSelection();
    }
}
