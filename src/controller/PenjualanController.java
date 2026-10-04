package controller;

import database.KoneksiDatabase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import view.PenjualanView;

public class PenjualanController implements CrudInterface {
    
    private Connection conn;
    private PenjualanView view;

    public PenjualanController(PenjualanView view) {
        this.view = view;
        try {
            this.conn = KoneksiDatabase.getConnection(); 
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Koneksi database gagal: " + e.getMessage());
        }
        muatComboBox();

        DefaultTableModel cartModel = (DefaultTableModel) view.getTabelDetail().getModel();
        cartModel.setRowCount(0);
        updateRingkasan();
        view.getCmbNamaObat().addActionListener(e -> isiHargaJualDariPilihan());
    }

    private int ambilId(String nilaiCombo) {
        return Integer.parseInt(nilaiCombo.split(" - ")[0].trim());
    }

    private int idTransaksiBerikutnya() throws SQLException {
        String sql = "SELECT COALESCE(MAX(id_penjualan), 0) + 1 AS next_id FROM penjualan";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("next_id") : 1;
        }
    }

    /**
     * Mengambil daftar Obat terbaru dari database dan mengisi ulang
     * ComboBox "Nama Obat" di form. Dipanggil di constructor, jadi
     * setiap kali PenjualanView dibuka datanya otomatis up-to-date.
     */
    public void muatComboBox() {
        if (conn == null) {
            return;
        }

        DefaultComboBoxModel<String> modelObat = new DefaultComboBoxModel<>();
        modelObat.addElement("--Pilih Obat--");
        try (PreparedStatement ps = conn.prepareStatement("SELECT id_obat, nama_obat FROM obat ORDER BY nama_obat");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                modelObat.addElement(rs.getInt("id_obat") + " - " + rs.getString("nama_obat"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Gagal memuat daftar obat: " + e.getMessage());
        }
        view.getCmbNamaObat().setModel(modelObat);
    }

    private void isiHargaJualDariPilihan() {
        if (conn == null) {
            view.getTxtHargaJual().setText("");
            return;
        }

        Object selected = view.getCmbNamaObat().getSelectedItem();
        if (selected == null || selected.toString().startsWith("--Pilih")) {
            view.getTxtHargaJual().setText("");
            return;
        }

        try {
            int idObat = ambilId(selected.toString());
            String sql = "SELECT harga_jual FROM obat WHERE id_obat = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idObat);
                try (ResultSet rs = ps.executeQuery()) {
                    view.getTxtHargaJual().setText(rs.next() ? String.valueOf(rs.getInt("harga_jual")) : "");
                }
            }
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Gagal mengambil harga jual obat: " + e.getMessage());
        }
    }

    private void updateRingkasan() {
        DefaultTableModel model = (DefaultTableModel) view.getTabelDetail().getModel();
        int totalItem = 0;
        int totalBayar = 0;

        for (int i = 0; i < model.getRowCount(); i++) {
            Object jumlahValue = model.getValueAt(i, 3);
            Object subtotalValue = model.getValueAt(i, 4);
            if (jumlahValue == null || subtotalValue == null) {
                continue;
            }

            try {
                totalItem += Integer.parseInt(jumlahValue.toString());
                totalBayar += Integer.parseInt(subtotalValue.toString());
            } catch (NumberFormatException e) {
                // Abaikan baris yang belum valid supaya label total tetap bisa tampil.
            }
        }

        view.setRingkasanPenjualan(totalItem, totalBayar);
    }

    private boolean isiDetailDariFormJikaKosong(DefaultTableModel model) {
        if (model.getRowCount() > 0) {
            return true;
        }

        String selectedObat = view.getCmbNamaObat().getSelectedItem() == null
                ? "" : view.getCmbNamaObat().getSelectedItem().toString();
        String hargaJualStr = view.getTxtHargaJual().getText().trim();
        int jumlah = (int) view.getSpinJumlah().getValue();

        if (selectedObat.isEmpty() || selectedObat.startsWith("--Pilih")) {
            JOptionPane.showMessageDialog(null, "Pilih obat terlebih dahulu atau klik Tambah untuk mengisi detail!");
            return false;
        }
        if (hargaJualStr.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Isi harga jual terlebih dahulu!");
            return false;
        }
        if (jumlah <= 0) {
            JOptionPane.showMessageDialog(null, "Jumlah harus lebih dari 0!");
            return false;
        }

        try {
            int hargaJual = Integer.parseInt(hargaJualStr);
            int subtotal = hargaJual * jumlah;
            model.addRow(new Object[]{
                model.getRowCount() + 1,
                selectedObat,
                hargaJual,
                jumlah,
                subtotal
            });
            updateRingkasan();
            return true;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Harga jual harus berupa angka!");
            return false;
        }
    }

    /**
     * Dipanggil saat pengguna mengetik ID Penjualan lalu pindah fokus/tekan
     * Enter di form. Mengambil seluruh item transaksi dengan id_penjualan
     * tsb dari database dan mengisi tanggal + tabel "Detail Penjualan"
     * (keranjang, jTable1) supaya bisa langsung diedit dan disimpan lewat
     * tombol "Ubah", bukan mulai dari form kosong.
     */
    public void muatUntukEdit(int idPenjualan) {
        if (conn == null) {
            return;
        }

        String sql = "SELECT p.id_obat, p.jumlah, p.tanggal_jual, p.total_harga, o.nama_obat "
                + "FROM penjualan p "
                + "JOIN obat o ON p.id_obat = o.id_obat "
                + "WHERE p.id_penjualan = ? "
                + "ORDER BY p.id_obat";

        DefaultTableModel model = (DefaultTableModel) view.getTabelDetail().getModel();
        model.setRowCount(0);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPenjualan);
            try (ResultSet rs = ps.executeQuery()) {
                boolean pertama = true;
                while (rs.next()) {
                    int idObat = rs.getInt("id_obat");
                    String namaObat = rs.getString("nama_obat");
                    int jumlah = rs.getInt("jumlah");
                    int totalHarga = rs.getInt("total_harga");
                    int hargaJual = jumlah != 0 ? totalHarga / jumlah : 0;

                    model.addRow(new Object[]{
                        model.getRowCount() + 1,
                        idObat + " - " + namaObat,
                        hargaJual,
                        jumlah,
                        totalHarga
                    });

                    if (pertama) {
                        view.getjDateChooserTanggal().setDate(rs.getDate("tanggal_jual"));
                        pertama = false;
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Gagal memuat data transaksi penjualan: " + e.getMessage());
            return;
        }

        updateRingkasan();

        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "Data transaksi penjualan #" + idPenjualan + " tidak ditemukan.");
        }
    }

    @Override
    public boolean tambah() {
        String selectedObat = view.getCmbNamaObat().getSelectedItem() == null
                ? "" : view.getCmbNamaObat().getSelectedItem().toString();
        String hargaJualStr = view.getTxtHargaJual().getText().trim();
        int jumlah = (int) view.getSpinJumlah().getValue();

        if (selectedObat.isEmpty() || selectedObat.startsWith("--Pilih")) {
            JOptionPane.showMessageDialog(null, "Pilih obat terlebih dahulu!");
            return false;
        }
        if (hargaJualStr.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Isi harga jual terlebih dahulu!");
            return false;
        }
        if (jumlah <= 0) {
            JOptionPane.showMessageDialog(null, "Jumlah harus lebih dari 0!");
            return false;
        }

        int hargaJual;
        try {
            hargaJual = Integer.parseInt(hargaJualStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Harga jual harus berupa angka!");
            return false;
        }

        int subtotal = hargaJual * jumlah;
        DefaultTableModel model = (DefaultTableModel) view.getTabelDetail().getModel();
        model.addRow(new Object[]{
            model.getRowCount() + 1,
            selectedObat,
            hargaJual,
            jumlah,
            subtotal
        });

        view.getCmbNamaObat().setSelectedIndex(0);
        view.getTxtHargaJual().setText("");
        view.getSpinJumlah().setValue(0);
        updateRingkasan();
        return true;
    }

    @Override
    public boolean ubah() {
        String idPenjualan = view.getTxtIDPenjualan().getText().trim();
        java.util.Date tanggal = view.getjDateChooserTanggal().getDate();
        DefaultTableModel model = (DefaultTableModel) view.getTabelDetail().getModel();

        if (idPenjualan.isEmpty() || tanggal == null) {
            JOptionPane.showMessageDialog(null, "Isi ID Penjualan dan Tanggal terlebih dahulu!");
            return false;
        }
        if (!isiDetailDariFormJikaKosong(model)) {
            return false;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String tglSql = sdf.format(tanggal);
        int idPenjualanInt;
        try {
            idPenjualanInt = Integer.parseInt(idPenjualan);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "ID Penjualan harus berupa angka!");
            return false;
        }

        String sqlRestoreStok = "UPDATE obat o "
                + "JOIN penjualan p ON o.id_obat = p.id_obat "
                + "SET o.stok = o.stok + p.jumlah "
                + "WHERE p.id_penjualan = ?";
        String sqlDelete = "DELETE FROM penjualan WHERE id_penjualan = ?";
        String sqlInsert = "INSERT INTO penjualan (id_penjualan, id_obat, jumlah, tanggal_jual, total_harga) VALUES (?, ?, ?, ?, ?)";
        String sqlKurangiStok = "UPDATE obat SET stok = stok - ? WHERE id_obat = ? AND stok >= ?";

        try {
            conn.setAutoCommit(false);

            PreparedStatement psRestore = conn.prepareStatement(sqlRestoreStok);
            psRestore.setInt(1, idPenjualanInt);
            psRestore.executeUpdate();
            
            PreparedStatement psDel = conn.prepareStatement(sqlDelete);
            psDel.setInt(1, idPenjualanInt);
            psDel.executeUpdate();
            
            PreparedStatement psIns = conn.prepareStatement(sqlInsert);
            PreparedStatement psStok = conn.prepareStatement(sqlKurangiStok);
            for (int i = 0; i < model.getRowCount(); i++) {
                int idObat = ambilId(model.getValueAt(i, 1).toString()); 
                int jumlah = Integer.parseInt(model.getValueAt(i, 3).toString());
                int subtotal = Integer.parseInt(model.getValueAt(i, 4).toString());

                psStok.setInt(1, jumlah);
                psStok.setInt(2, idObat);
                psStok.setInt(3, jumlah);
                if (psStok.executeUpdate() == 0) {
                    conn.rollback();
                    JOptionPane.showMessageDialog(null, "Stok obat tidak cukup untuk item: " + model.getValueAt(i, 1));
                    return false;
                }

                psIns.setInt(1, idPenjualanInt);
                psIns.setInt(2, idObat);
                psIns.setInt(3, jumlah);
                psIns.setString(4, tglSql);
                psIns.setInt(5, subtotal);
                psIns.addBatch();
            }
            psIns.executeBatch();
            conn.commit();
            
            JOptionPane.showMessageDialog(null, "Transaksi Penjualan Berhasil Diubah!");
            model.setRowCount(0);
            reset();
            updateRingkasan();
            muatComboBox();
            return true;
        } catch (SQLException | NumberFormatException e) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            JOptionPane.showMessageDialog(null, "Gagal mengubah transaksi: " + e.getMessage());
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @Override
    public boolean hapus() {
        String idPenjualan = view.getTxtIDPenjualan().getText().trim();
        if (idPenjualan.isEmpty()) {
            JOptionPane.showMessageDialog(null, "ID Penjualan harus diisi untuk menghapus transaksi!");
            return false;
        }

        String sqlRestoreStok = "UPDATE obat o "
                + "JOIN penjualan p ON o.id_obat = p.id_obat "
                + "SET o.stok = o.stok + p.jumlah "
                + "WHERE p.id_penjualan = ?";
        String sql = "DELETE FROM penjualan WHERE id_penjualan = ?";
        try {
            conn.setAutoCommit(false);
            int idPenjualanInt = Integer.parseInt(idPenjualan);

            PreparedStatement psRestore = conn.prepareStatement(sqlRestoreStok);
            psRestore.setInt(1, idPenjualanInt);
            psRestore.executeUpdate();

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idPenjualanInt);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                conn.commit();
                JOptionPane.showMessageDialog(null, "Data Transaksi Penjualan Berhasil Dihapus!");
                reset();
                ((DefaultTableModel) view.getTabelDetail().getModel()).setRowCount(0);
                updateRingkasan();
                return true;
            }
            conn.rollback();
        } catch (SQLException | NumberFormatException e) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            JOptionPane.showMessageDialog(null, "Gagal menghapus data transaksi: " + e.getMessage());
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
        return false;
    }

    @Override
    public void tampilkan() {
        // CATATAN: Di PenjualanView, jTable1 (getTabelDetail()) adalah tabel
        // "Detail Penjualan" untuk membangun transaksi baru (keranjang),
        // BUKAN tabel riwayat penjualan. Karena itu tabel ini sengaja TIDAK
        // diisi otomatis dari database saat form dibuka - kalau diisi dengan
        // data lama, baris-baris tersebut akan ikut ter-insert ulang saat
        // tombol Simpan ditekan.
        //
        // Kalau nanti dibuatkan tabel/riwayat penjualan terpisah di
        // PenjualanView (mis. getTabelRiwayat()), query berikut bisa dipakai:
        //
        // SELECT p.id_penjualan, o.nama_obat, p.tanggal_jual, p.jumlah, p.total_harga
        // FROM penjualan p JOIN obat o ON p.id_obat = o.id_obat
        // ORDER BY p.id_penjualan DESC
        if (conn == null) {
            System.out.println("Database tidak aktif: riwayat penjualan tidak dapat dimuat.");
        }
    }

    public boolean simpan() {
        String idPenjualan = view.getTxtIDPenjualan().getText().trim();
        java.util.Date tanggal = view.getjDateChooserTanggal().getDate();
        DefaultTableModel model = (DefaultTableModel) view.getTabelDetail().getModel();

        if (tanggal == null) {
            JOptionPane.showMessageDialog(null, "Tanggal penjualan harus diisi!");
            return false;
        }
        if (!isiDetailDariFormJikaKosong(model)) {
            return false;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String tglSql = sdf.format(tanggal);

        String sql = "INSERT INTO penjualan (id_penjualan, id_obat, jumlah, tanggal_jual, total_harga) VALUES (?, ?, ?, ?, ?)";
        String sqlStok = "UPDATE obat SET stok = stok - ? WHERE id_obat = ? AND stok >= ?";

        try {
            int idPenjualanInt = idPenjualan.isEmpty()
                    ? idTransaksiBerikutnya()
                    : Integer.parseInt(idPenjualan);
            view.getTxtIDPenjualan().setText(String.valueOf(idPenjualanInt));

            conn.setAutoCommit(false); 
            PreparedStatement ps = conn.prepareStatement(sql);
            PreparedStatement psStok = conn.prepareStatement(sqlStok);

            for (int i = 0; i < model.getRowCount(); i++) {
                int idObat = ambilId(model.getValueAt(i, 1).toString()); 
                int jumlah = Integer.parseInt(model.getValueAt(i, 3).toString());
                int subtotal = Integer.parseInt(model.getValueAt(i, 4).toString());

                psStok.setInt(1, jumlah);
                psStok.setInt(2, idObat);
                psStok.setInt(3, jumlah);
                if (psStok.executeUpdate() == 0) {
                    conn.rollback();
                    JOptionPane.showMessageDialog(null, "Stok obat tidak cukup untuk item: " + model.getValueAt(i, 1));
                    return false;
                }

                ps.setInt(1, idPenjualanInt);
                ps.setInt(2, idObat);
                ps.setInt(3, jumlah);
                ps.setString(4, tglSql);
                ps.setInt(5, subtotal);
                ps.addBatch();
            }

            ps.executeBatch();
            conn.commit();
            JOptionPane.showMessageDialog(null, "Transaksi Penjualan Berhasil Disimpan ke Database!");
            reset();
            model.setRowCount(0);
            updateRingkasan();
            muatComboBox();
            return true;
            
        } catch (SQLException | NumberFormatException e) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            JOptionPane.showMessageDialog(null, "Gagal menyimpan transaksi: " + e.getMessage());
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public void reset() {

        view.getTxtIDPenjualan().setText("");
        view.getjDateChooserTanggal().setDate(new java.util.Date());
      
        view.getCmbNamaObat().setSelectedIndex(0);   
        view.getTxtHargaJual().setText("");           
        view.getSpinJumlah().setValue(0);     
        updateRingkasan();
    }
}
