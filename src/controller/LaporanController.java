package controller;

import database.KoneksiDatabase;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import view.LaporanView;

public class LaporanController {

    private final LaporanView view;

    public LaporanController(LaporanView view) {
        this.view = view;
        reset();
    }

    public void tampilkan() {
        String jenisLaporan = getJenisLaporan();
        if (jenisLaporan == null) {
            return;
        }

        Date[] rentang = getRentangTanggal();
        if (rentang == null) {
            return;
        }

        DefaultTableModel model = (DefaultTableModel) view.getTableLaporan().getModel();
        model.setRowCount(0);

        String sql = buildSql(jenisLaporan);
        try (Connection conn = KoneksiDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            isiParameterTanggal(ps, jenisLaporan, rentang[0], rentang[1]);

            try (ResultSet rs = ps.executeQuery()) {
                int no = 1;
                while (rs.next()) {
                    model.addRow(new Object[]{
                        no++,
                        rs.getDate("tanggal"),
                        rs.getString("deskripsi"),
                        rs.getObject("jumlah"),
                        rs.getObject("total")
                    });
                }
            }

            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(view, "Tidak ada data untuk filter laporan ini.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Gagal memuat laporan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void cetak() {
        String jenisLaporan = getJenisLaporan();
        if (jenisLaporan == null) {
            return;
        }

        Date[] rentang = getRentangTanggal();
        if (rentang == null) {
            return;
        }

        File template = cariFileReport(jenisLaporan);
        if (template == null) {
            JOptionPane.showMessageDialog(view,
                    "File report tidak ditemukan di folder src/reports atau build/classes/reports.",
                    "Error JasperReports", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            cetakDenganJasper(template, jenisLaporan, rentang[0], rentang[1]);
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(view,
                    "Library JasperReports belum ada di project.\n"
                    + "Tambahkan semua jar JasperReports ke Libraries NetBeans, lalu jalankan lagi.",
                    "Library JasperReports Belum Terpasang", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            Throwable penyebab = akarMasalah(e);
            JOptionPane.showMessageDialog(view,
                    "Gagal mencetak dengan JasperReports: " + pesanError(penyebab)
                    + "\n\nSebagai cadangan, aplikasi akan membuka dialog print tabel.",
                    "Error JasperReports", JOptionPane.WARNING_MESSAGE);
            cetakTabel();
        }
    }

    public void reset() {
        view.getCmbJenisLaporan().setSelectedIndex(0);
        view.getTanggalAwal().setDate(new java.util.Date());
        view.getTanggalAkhir().setDate(new java.util.Date());

        DefaultTableModel model = (DefaultTableModel) view.getTableLaporan().getModel();
        model.setRowCount(0);
    }

    private String getJenisLaporan() {
        Object selected = view.getCmbJenisLaporan().getSelectedItem();
        String jenisLaporan = selected == null ? "" : selected.toString();
        if (jenisLaporan.equals("Pilih Laporan") || jenisLaporan.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Silakan pilih jenis laporan terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return jenisLaporan;
    }

    private Date[] getRentangTanggal() {
        java.util.Date awal = view.getTanggalAwal().getDate();
        java.util.Date akhir = view.getTanggalAkhir().getDate();

        if (awal == null || akhir == null) {
            JOptionPane.showMessageDialog(view, "Tanggal awal dan tanggal akhir harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (awal.after(akhir)) {
            JOptionPane.showMessageDialog(view, "Tanggal awal tidak boleh lebih besar dari tanggal akhir!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        return new Date[]{new Date(awal.getTime()), new Date(akhir.getTime())};
    }

    private String buildSql(String jenisLaporan) {
        if (jenisLaporan.equalsIgnoreCase("Laporan Obat")) {
            return "SELECT tanggal_kadaluarsa AS tanggal, "
                    + "CONCAT(nama_obat, ' (', jenis, ')') AS deskripsi, "
                    + "stok AS jumlah, harga_jual AS total "
                    + "FROM obat "
                    + "WHERE tanggal_kadaluarsa BETWEEN ? AND ? "
                    + "ORDER BY tanggal_kadaluarsa ASC, nama_obat ASC";
        }
        if (jenisLaporan.equalsIgnoreCase("Laporan Supplier")) {
            return "SELECT NULL AS tanggal, "
                    + "CONCAT(nama_supplier, ' - ', alamat) AS deskripsi, "
                    + "NULL AS jumlah, no_telepon AS total "
                    + "FROM supplier ORDER BY nama_supplier ASC";
        }
        if (jenisLaporan.equalsIgnoreCase("Laporan Pembelian")) {
            return "SELECT p.tanggal_beli AS tanggal, "
                    + "CONCAT(o.nama_obat, ' (Supplier: ', s.nama_supplier, ')') AS deskripsi, "
                    + "p.jumlah AS jumlah, p.total_harga AS total "
                    + "FROM pembelian p "
                    + "JOIN obat o ON p.id_obat = o.id_obat "
                    + "JOIN supplier s ON p.id_supplier = s.id_supplier "
                    + "WHERE p.tanggal_beli BETWEEN ? AND ? "
                    + "ORDER BY p.tanggal_beli DESC, p.id_pembelian DESC";
        }
        if (jenisLaporan.equalsIgnoreCase("Laporan Penjualan")) {
            return "SELECT p.tanggal_jual AS tanggal, "
                    + "o.nama_obat AS deskripsi, "
                    + "p.jumlah AS jumlah, p.total_harga AS total "
                    + "FROM penjualan p "
                    + "JOIN obat o ON p.id_obat = o.id_obat "
                    + "WHERE p.tanggal_jual BETWEEN ? AND ? "
                    + "ORDER BY p.tanggal_jual DESC, p.id_penjualan DESC";
        }
        return "";
    }

    private void isiParameterTanggal(PreparedStatement ps, String jenisLaporan, Date awal, Date akhir) throws Exception {
        if (jenisLaporan.equalsIgnoreCase("Laporan Supplier")) {
            return;
        }
        ps.setDate(1, awal);
        ps.setDate(2, akhir);
    }

    private String namaFileReport(String jenisLaporan) {
        if (jenisLaporan.equalsIgnoreCase("Laporan Obat")) {
            return "laporan_obat";
        }
        if (jenisLaporan.equalsIgnoreCase("Laporan Supplier")) {
            return "laporan_supplier";
        }
        if (jenisLaporan.equalsIgnoreCase("Laporan Pembelian")) {
            return "laporan_pembelian";
        }
        return "laporan_penjualan";
    }

    private File cariFileReport(String jenisLaporan) {
        String nama = namaFileReport(jenisLaporan);
        String[] folderReport = {
            "src/reports/",
            "build/classes/reports/",
            "reports/"
        };

        for (String folder : folderReport) {
            File jasper = new File(folder + nama + ".jasper");
            if (jasper.exists()) {
                return jasper;
            }
        }
        for (String folder : folderReport) {
            File jrxml = new File(folder + nama + ".jrxml");
            if (jrxml.exists()) {
                return jrxml;
            }
        }
        return null;
    }

    private void cetakDenganJasper(File template, String jenisLaporan, Date awal, Date akhir) throws Exception {
        Class<?> compileManagerClass = Class.forName("net.sf.jasperreports.engine.JasperCompileManager");
        Class<?> fillManagerClass = Class.forName("net.sf.jasperreports.engine.JasperFillManager");
        Class<?> viewerClass = Class.forName("net.sf.jasperreports.view.JasperViewer");
        Class<?> jasperReportClass = Class.forName("net.sf.jasperreports.engine.JasperReport");
        Class<?> jasperPrintClass = Class.forName("net.sf.jasperreports.engine.JasperPrint");

        Map<String, Object> parameter = new HashMap<>();

        parameter.put("JUDUL_LAPORAN", jenisLaporan);
        parameter.put("TANGGAL_AWAL", new java.sql.Date(awal.getTime()));
        parameter.put("TANGGAL_AKHIR", new java.sql.Date(akhir.getTime()));
        parameter.put("PERIODE", formatPeriode(awal, akhir));

        Object jasperPrint;
        try (Connection conn = KoneksiDatabase.getConnection()) {
            if (template.getName().toLowerCase().endsWith(".jasper")) {
                Method fillReport = fillManagerClass.getMethod("fillReport", String.class, Map.class, Connection.class);
                jasperPrint = fillReport.invoke(null, template.getAbsolutePath(), parameter, conn);
            } else {
                Method compileReport = compileManagerClass.getMethod("compileReport", String.class);
                Object jasperReport = compileReport.invoke(null, template.getAbsolutePath());

                Method fillReport = fillManagerClass.getMethod("fillReport", jasperReportClass, Map.class, Connection.class);
                jasperPrint = fillReport.invoke(null, jasperReport, parameter, conn);
            }
        }

        Method viewReport = viewerClass.getMethod("viewReport", jasperPrintClass, boolean.class);
        viewReport.invoke(null, jasperPrint, false);
    }

    private Throwable akarMasalah(Throwable e) {
        Throwable hasil = e;
        while (hasil instanceof InvocationTargetException && ((InvocationTargetException) hasil).getTargetException() != null) {
            hasil = ((InvocationTargetException) hasil).getTargetException();
        }
        while (hasil.getCause() != null && hasil.getCause() != hasil) {
            hasil = hasil.getCause();
        }
        return hasil;
    }

    private String pesanError(Throwable e) {
        if (e == null) {
            return "tidak diketahui";
        }
        String pesan = e.getMessage();
        if (pesan == null || pesan.trim().isEmpty()) {
            return e.getClass().getName();
        }
        return pesan;
    }

    private String formatPeriode(Date awal, Date akhir) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        return sdf.format(awal) + " - " + sdf.format(akhir);
    }

    private void cetakTabel() {
        if (view.getTableLaporan().getRowCount() == 0) {
            JOptionPane.showMessageDialog(view, "Tidak ada data yang bisa dicetak. Tampilkan data terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String judulLaporan = view.getCmbJenisLaporan().getSelectedItem().toString();
            MessageFormat header = new MessageFormat(judulLaporan + " - Sistem Apotek");
            MessageFormat footer = new MessageFormat("Halaman {0,number,integer}");
            boolean berhasil = view.getTableLaporan().print(JTable.PrintMode.FIT_WIDTH, header, footer);

            if (berhasil) {
                JOptionPane.showMessageDialog(view, "Laporan berhasil dicetak!");
            } else {
                JOptionPane.showMessageDialog(view, "Proses cetak dibatalkan.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Gagal mencetak laporan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
