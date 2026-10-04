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
import view.PembelianView; // Pastikan package view kamu sudah sesuai

public class PembelianController implements CrudInterface {

    private Connection conn;
    private PembelianView view;

    public PembelianController(PembelianView view) {
        this.view = view;
        try {
            this.conn = KoneksiDatabase.getConnection();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Koneksi database gagal di PembelianController: " + e.getMessage());
        }
        muatComboBox();

        // Tabel "Detail Pembelian" awalnya berisi baris kosong bawaan
        // template NetBeans - bersihkan supaya keranjang mulai kosong.
        DefaultTableModel cartModel = (DefaultTableModel) view.getTabelDetail().getModel();
        cartModel.setRowCount(0);
    }

    private int ambilId(String nilaiCombo) {
        return Integer.parseInt(nilaiCombo.split(" - ")[0].trim());
    }

    private int idTransaksiBerikutnya() throws SQLException {
        String sql = "SELECT COALESCE(MAX(id_pembelian), 0) + 1 AS next_id FROM pembelian";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("next_id") : 1;
        }
    }

    private String ambilIdPembelianDariFormAtauRiwayat() {
        String idPembelian = view.getTxtIDPembelian().getText().trim();
        if (!idPembelian.isEmpty() || view.getTabelRiwayat().getSelectedRow() == -1) {
            return idPembelian;
        }

        int viewRow = view.getTabelRiwayat().getSelectedRow();
        int modelRow = view.getTabelRiwayat().convertRowIndexToModel(viewRow);
        idPembelian = view.getTabelRiwayat().getModel().getValueAt(modelRow, 1).toString();
        view.getTxtIDPembelian().setText(idPembelian);
        return idPembelian;
    }

    private boolean isiDetailDariFormJikaKosong(DefaultTableModel model) {
        if (model.getRowCount() > 0) {
            return true;
        }

        String selectedObat = view.getCmbObat().getSelectedItem() == null
                ? "" : view.getCmbObat().getSelectedItem().toString();
        String hargaBeliStr = view.getTxtHargaBeli().getText().trim();
        int jumlah = (int) view.getSpinJumlah().getValue();

        if (selectedObat.isEmpty() || selectedObat.startsWith("--Pilih")) {
            JOptionPane.showMessageDialog(null, "Pilih obat terlebih dahulu atau klik Tambah untuk mengisi detail!");
            return false;
        }
        if (hargaBeliStr.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Isi harga beli terlebih dahulu!");
            return false;
        }
        if (jumlah <= 0) {
            JOptionPane.showMessageDialog(null, "Jumlah harus lebih dari 0!");
            return false;
        }

        try {
            int hargaBeli = Integer.parseInt(hargaBeliStr);
            int subtotal = hargaBeli * jumlah;
            model.addRow(new Object[]{
                model.getRowCount() + 1,
                selectedObat,
                hargaBeli,
                jumlah,
                subtotal
            });
            view.getTxtSubtotal().setText(String.valueOf(subtotal));
            return true;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Harga beli harus berupa angka!");
            return false;
        }
    }

    /**
     * Mengambil daftar Obat dan Supplier terbaru dari database, lalu
     * mengisi ulang ComboBox "Obat" dan "Supplier" di form.
     * Setiap kali PembelianView dibuka (mis. setelah menambah data obat
     * atau supplier baru), data di dropdown otomatis ikut ter-update
     * karena method ini dipanggil di constructor.
     */
    public void muatComboBox() {
        if (conn == null) {
            return;
        }

        // --- ComboBox Obat: format "id - nama_obat" ---
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
        view.getCmbObat().setModel(modelObat);

        // --- ComboBox Supplier: format "id - nama_supplier" ---
        DefaultComboBoxModel<String> modelSupplier = new DefaultComboBoxModel<>();
        modelSupplier.addElement("--Pilih Supplier--");
        try (PreparedStatement ps = conn.prepareStatement("SELECT id_supplier, nama_supplier FROM supplier ORDER BY nama_supplier");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                modelSupplier.addElement(rs.getInt("id_supplier") + " - " + rs.getString("nama_supplier"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Gagal memuat daftar supplier: " + e.getMessage());
        }
        view.getCmbSupplier().setModel(modelSupplier);
    }

    /**
     * Tombol "Tambah": menambahkan Obat + Harga Beli + Jumlah yang sedang
     * dipilih di form sebagai satu baris item ke tabel "Detail Pembelian"
     * (keranjang). Belum menyentuh database - baru disimpan permanen saat
     * tombol "Simpan" ditekan.
     */
    @Override
    public boolean tambah() {
        String selectedObat = view.getCmbObat().getSelectedItem() == null
                ? "" : view.getCmbObat().getSelectedItem().toString();
        String hargaBeliStr = view.getTxtHargaBeli().getText();
        int jumlah = (int) view.getSpinJumlah().getValue();

        if (selectedObat.isEmpty() || selectedObat.startsWith("--Pilih")) {
            JOptionPane.showMessageDialog(null, "Pilih obat terlebih dahulu!");
            return false;
        }
        if (hargaBeliStr.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Isi harga beli terlebih dahulu!");
            return false;
        }
        if (jumlah <= 0) {
            JOptionPane.showMessageDialog(null, "Jumlah harus lebih dari 0!");
            return false;
        }

        int hargaBeli;
        try {
            hargaBeli = Integer.parseInt(hargaBeliStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Harga beli harus berupa angka!");
            return false;
        }

        int subtotal = hargaBeli * jumlah;

        DefaultTableModel model = (DefaultTableModel) view.getTabelDetail().getModel();
        model.addRow(new Object[]{
            model.getRowCount() + 1,
            selectedObat, // disimpan dengan format "id - nama" supaya id_obat bisa diambil lagi saat Simpan
            hargaBeli,
            jumlah,
            subtotal
        });

        view.getTxtSubtotal().setText(String.valueOf(subtotal));
        return true;
    }

    @Override
    public boolean ubah() {
        String idPembelian = ambilIdPembelianDariFormAtauRiwayat();
        java.util.Date tanggal = view.getjDateChooserTanggalBeli().getDate();
        String selectedSupplier = view.getCmbSupplier().getSelectedItem() == null
                ? "" : view.getCmbSupplier().getSelectedItem().toString();
        DefaultTableModel model = (DefaultTableModel) view.getTabelDetail().getModel();

        if (idPembelian.isEmpty() || tanggal == null || selectedSupplier.isEmpty() || selectedSupplier.startsWith("--Pilih")) {
            JOptionPane.showMessageDialog(null, "Isi ID Pembelian, Tanggal, dan Supplier terlebih dahulu!");
            return false;
        }
        if (!isiDetailDariFormJikaKosong(model)) {
            return false;
        }

        int idSupplier;
        int idPembelianInt;
        try {
            idSupplier = ambilId(selectedSupplier);
            idPembelianInt = Integer.parseInt(idPembelian);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "ID Pembelian atau data supplier tidak valid!");
            return false;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String tglSql = sdf.format(tanggal);

        String sqlRestoreStok = "UPDATE obat o "
                + "JOIN pembelian p ON o.id_obat = p.id_obat "
                + "SET o.stok = o.stok - p.jumlah "
                + "WHERE p.id_pembelian = ?";
        String sqlDelete = "DELETE FROM pembelian WHERE id_pembelian = ?";
        String sqlInsert = "INSERT INTO pembelian (id_pembelian, id_obat, id_supplier, jumlah, tanggal_beli, total_harga) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlStok = "UPDATE obat SET stok = stok + ?, harga_beli = ? WHERE id_obat = ?";

        try {
            conn.setAutoCommit(false);

            PreparedStatement psRestore = conn.prepareStatement(sqlRestoreStok);
            psRestore.setInt(1, idPembelianInt);
            psRestore.executeUpdate();

            PreparedStatement psDelete = conn.prepareStatement(sqlDelete);
            psDelete.setInt(1, idPembelianInt);
            int deletedRows = psDelete.executeUpdate();
            if (deletedRows == 0) {
                conn.rollback();
                JOptionPane.showMessageDialog(null, "ID Pembelian tidak ditemukan.");
                return false;
            }

            PreparedStatement psInsert = conn.prepareStatement(sqlInsert);
            PreparedStatement psStok = conn.prepareStatement(sqlStok);
            for (int i = 0; i < model.getRowCount(); i++) {
                int idObat = ambilId(model.getValueAt(i, 1).toString());
                int hargaBeli = Integer.parseInt(model.getValueAt(i, 2).toString());
                int jumlah = Integer.parseInt(model.getValueAt(i, 3).toString());
                int subtotal = Integer.parseInt(model.getValueAt(i, 4).toString());

                psInsert.setInt(1, idPembelianInt);
                psInsert.setInt(2, idObat);
                psInsert.setInt(3, idSupplier);
                psInsert.setInt(4, jumlah);
                psInsert.setString(5, tglSql);
                psInsert.setInt(6, subtotal);
                psInsert.addBatch();

                psStok.setInt(1, jumlah);
                psStok.setInt(2, hargaBeli);
                psStok.setInt(3, idObat);
                psStok.addBatch();
            }

            psInsert.executeBatch();
            psStok.executeBatch();
            conn.commit();

            JOptionPane.showMessageDialog(null, "Data Transaksi Pembelian Berhasil Diubah!");
            reset();
            tampilkan();
            muatComboBox();
            return true;
        } catch (SQLException | NumberFormatException e) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            JOptionPane.showMessageDialog(null, "Gagal mengubah transaksi pembelian: " + e.getMessage());
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @Override
    public boolean hapus() {
        String idPembelian = ambilIdPembelianDariFormAtauRiwayat();
        if (idPembelian.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Isi ID Pembelian atau pilih transaksi di tabel riwayat untuk menghapus!");
            return false;
        }

        String sql = "DELETE FROM pembelian WHERE id_pembelian = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(idPembelian));
            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(null, "Data Transaksi Pembelian Berhasil Dihapus!");
                tampilkan();
                reset();
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "ID Pembelian tidak ditemukan.");
            }
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Gagal menghapus data transaksi: " + e.getMessage());
        }
        return false;
    }

    /**
     * Dipanggil saat baris tabel "Riwayat Pembelian" (jTable1) diklik.
     * Mengambil seluruh item transaksi dengan id_pembelian tsb dari database,
     * lalu mengisi form (ID, Tanggal, Supplier) dan tabel "Detail Pembelian"
     * (keranjang, jTable2) supaya bisa langsung diedit dan disimpan lewat
     * tombol "Ubah" - sama seperti perilaku klik-untuk-edit pada Data Obat.
     */
    public void muatUntukEdit(int idPembelian) {
        if (conn == null) {
            return;
        }

        String sql = "SELECT p.id_obat, p.id_supplier, p.jumlah, p.tanggal_beli, p.total_harga, "
                + "o.nama_obat, s.nama_supplier "
                + "FROM pembelian p "
                + "JOIN obat o ON p.id_obat = o.id_obat "
                + "JOIN supplier s ON p.id_supplier = s.id_supplier "
                + "WHERE p.id_pembelian = ? "
                + "ORDER BY p.id_obat";

        DefaultTableModel model = (DefaultTableModel) view.getTabelDetail().getModel();
        model.setRowCount(0);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPembelian);
            try (ResultSet rs = ps.executeQuery()) {
                boolean pertama = true;
                while (rs.next()) {
                    int idObat = rs.getInt("id_obat");
                    String namaObat = rs.getString("nama_obat");
                    int jumlah = rs.getInt("jumlah");
                    int totalHarga = rs.getInt("total_harga");
                    int hargaBeli = jumlah != 0 ? totalHarga / jumlah : 0;

                    model.addRow(new Object[]{
                        model.getRowCount() + 1,
                        idObat + " - " + namaObat,
                        hargaBeli,
                        jumlah,
                        totalHarga
                    });

                    if (pertama) {
                        view.getTxtIDPembelian().setText(String.valueOf(idPembelian));
                        view.getjDateChooserTanggalBeli().setDate(rs.getDate("tanggal_beli"));
                        int idSupplier = rs.getInt("id_supplier");
                        view.getCmbSupplier().setSelectedItem(idSupplier + " - " + rs.getString("nama_supplier"));
                        pertama = false;
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Gagal memuat data transaksi pembelian: " + e.getMessage());
            return;
        }

        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "Data transaksi pembelian #" + idPembelian + " tidak ditemukan.");
        }
    }

    /**
     * Memuat tabel "Riwayat Pembelian" (jTable1) dari database.
     * CATATAN: ini BUKAN tabel keranjang/detail (jTable2) - lihat getTabelDetail()
     * vs getTabelRiwayat() di PembelianView.
     */
    @Override
    public void tampilkan() {
        if (conn == null) {
            System.out.println("Gagal memuat riwayat pembelian: Database tidak aktif.");
            return;
        }

        javax.swing.JTable tabelRiwayat = view.getTabelRiwayat();
        if (tabelRiwayat == null) return;

        DefaultTableModel model = (DefaultTableModel) tabelRiwayat.getModel();
        model.setRowCount(0); // Bersihkan sisa data lama di tabel UI

        // SQL Query untuk mengambil data nota, nama supplier, total item, dan total harga
        String sql = "SELECT p.id_pembelian, s.nama_supplier, p.tanggal_beli, "
                + "SUM(p.jumlah) AS total_item, SUM(p.total_harga) AS total_bayar "
                + "FROM pembelian p "
                + "JOIN supplier s ON p.id_supplier = s.id_supplier "
                + "GROUP BY p.id_pembelian, s.nama_supplier, p.tanggal_beli "
                + "ORDER BY p.id_pembelian DESC";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            int no = 1;

            while (rs.next()) {
                Object[] row = {
                    no++,
                    rs.getInt("id_pembelian"),
                    rs.getString("nama_supplier"),
                    rs.getDate("tanggal_beli"),
                    rs.getInt("total_item"),
                    rs.getInt("total_bayar")
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Gagal memuat riwayat pembelian: " + e.getMessage());
        }
    }

    /**
     * Menyimpan seluruh baris di tabel "Detail Pembelian" (keranjang, jTable2)
     * ke database sebagai transaksi pembelian (id_pembelian yang sama, banyak
     * baris id_obat). id_supplier diambil dari ComboBox Supplier (berlaku
     * untuk seluruh baris di transaksi ini).
     */
    public boolean simpan() {
        String idPembelian = view.getTxtIDPembelian().getText().trim();
        java.util.Date tanggal = view.getjDateChooserTanggalBeli().getDate();
        String selectedSupplier = view.getCmbSupplier().getSelectedItem() == null
                ? "" : view.getCmbSupplier().getSelectedItem().toString();
        DefaultTableModel model = (DefaultTableModel) view.getTabelDetail().getModel();

        if (tanggal == null) {
            JOptionPane.showMessageDialog(null, "Tanggal pembelian harus diisi!");
            return false;
        }
        if (selectedSupplier.isEmpty() || selectedSupplier.startsWith("--Pilih")) {
            JOptionPane.showMessageDialog(null, "Pilih supplier terlebih dahulu!");
            return false;
        }
        if (!isiDetailDariFormJikaKosong(model)) {
            return false;
        }

        int idSupplier;
        int idPembelianInt;
        try {
            idSupplier = ambilId(selectedSupplier);
            idPembelianInt = idPembelian.isEmpty() ? idTransaksiBerikutnya() : Integer.parseInt(idPembelian);
            view.getTxtIDPembelian().setText(String.valueOf(idPembelianInt));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "ID Pembelian atau data supplier tidak valid!");
            return false;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Gagal membuat ID Pembelian otomatis: " + e.getMessage());
            return false;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String tglSql = sdf.format(tanggal);

        // Struktur tabel pembelian: id_pembelian, id_obat, id_supplier, jumlah, tanggal_beli, total_harga
        String sql = "INSERT INTO pembelian (id_pembelian, id_obat, id_supplier, jumlah, tanggal_beli, total_harga) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlStok = "UPDATE obat SET stok = stok + ?, harga_beli = ? WHERE id_obat = ?";

        try {
            conn.setAutoCommit(false); // Mode transaksi database aman
            PreparedStatement ps = conn.prepareStatement(sql);
            PreparedStatement psStok = conn.prepareStatement(sqlStok);

            // Kolom tabel Detail Pembelian: 0=No, 1=Nama Obat ("id - nama"), 2=Harga, 3=Jumlah, 4=Subtotal, 5=Aksi
            for (int i = 0; i < model.getRowCount(); i++) {
                String namaObatCell = model.getValueAt(i, 1).toString();
                int idObat = ambilId(namaObatCell);
                int hargaBeli = Integer.parseInt(model.getValueAt(i, 2).toString());
                int jumlah = Integer.parseInt(model.getValueAt(i, 3).toString());
                int subtotal = Integer.parseInt(model.getValueAt(i, 4).toString());

                ps.setInt(1, idPembelianInt);
                ps.setInt(2, idObat);
                ps.setInt(3, idSupplier);
                ps.setInt(4, jumlah);
                ps.setString(5, tglSql);
                ps.setInt(6, subtotal);
                ps.addBatch();

                psStok.setInt(1, jumlah);
                psStok.setInt(2, hargaBeli);
                psStok.setInt(3, idObat);
                psStok.addBatch();
            }

            ps.executeBatch();
            psStok.executeBatch();
            conn.commit();
            JOptionPane.showMessageDialog(null, "Transaksi Pembelian Berhasil Disimpan ke Database!");

            model.setRowCount(0); // Kosongkan keranjang
            reset();              // Kosongkan form input
            tampilkan();          // Refresh riwayat
            muatComboBox();       // Refresh dropdown (stok obat dsb bisa berubah nantinya)
            return true;

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            JOptionPane.showMessageDialog(null, "Gagal menyimpan transaksi: " + e.getMessage());
            return false;
        } catch (NumberFormatException e) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            JOptionPane.showMessageDialog(null, "Data pada tabel keranjang tidak valid: " + e.getMessage());
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public void reset() {
        view.getTxtIDPembelian().setText("");
        view.getTxtHargaBeli().setText("");
        view.getTxtSubtotal().setText("");

        view.getjDateChooserTanggalBeli().setDate(null);
        view.getCmbSupplier().setSelectedIndex(0);
        view.getCmbObat().setSelectedIndex(0);
        view.getSpinJumlah().setValue(0);

        // Kosongkan juga keranjang "Detail Pembelian"
        DefaultTableModel model = (DefaultTableModel) view.getTabelDetail().getModel();
        model.setRowCount(0);
    }
}
