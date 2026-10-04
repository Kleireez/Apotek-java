/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.Date;

/**
 * Model untuk tabel `penjualan`.
 * Sekarang extends Transaksi (Inheritance).
 */
public class Penjualan extends Transaksi {

    private int idPenjualan;
    private int idObat;

    // Untuk ditampilkan di JTable (hasil JOIN), bukan kolom asli tabel
    private String namaObat;

    public Penjualan() {
        super();
    }

    public Penjualan(int idPenjualan, int idObat, int jumlah, Date tanggalJual, int totalHarga) {
        super(jumlah, tanggalJual, totalHarga);
        this.idPenjualan = idPenjualan;
        this.idObat = idObat;
    }

    // Constructor tanpa id, dipakai saat mau INSERT data baru
    public Penjualan(int idObat, int jumlah, Date tanggalJual, int totalHarga) {
        super(jumlah, tanggalJual, totalHarga);
        this.idObat = idObat;
    }

    public int getIdPenjualan() {
        return idPenjualan;
    }

    public void setIdPenjualan(int idPenjualan) {
        this.idPenjualan = idPenjualan;
    }

    public int getIdObat() {
        return idObat;
    }

    public void setIdObat(int idObat) {
        this.idObat = idObat;
    }

    // Alias biar nama method-nya sesuai nama kolom di tabel penjualan
    // (di belakangnya tetap manggil field jumlah/tanggal/totalHarga dari Transaksi)
    public Date getTanggalJual() {
        return getTanggal();
    }

    public void setTanggalJual(Date tanggalJual) {
        setTanggal(tanggalJual);
    }

    public String getNamaObat() {
        return namaObat;
    }

    public void setNamaObat(String namaObat) {
        this.namaObat = namaObat;
    }

    @Override
    public String getJenisTransaksi() {
        return "Penjualan ke Customer";
    }
}