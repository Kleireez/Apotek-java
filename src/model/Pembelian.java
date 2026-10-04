/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.Date;

/**
 * Model untuk tabel `pembelian`.
 * Sekarang extends Transaksi (Inheritance).
 */
public class Pembelian extends Transaksi {

    private int idPembelian;
    private int idObat;
    private int idSupplier;

    // Untuk ditampilkan di JTable (hasil JOIN), bukan kolom asli tabel
    private String namaObat;
    private String namaSupplier;

    public Pembelian() {
        super();
    }

    public Pembelian(int idPembelian, int idObat, int idSupplier, int jumlah,
            Date tanggalBeli, int totalHarga) {
        super(jumlah, tanggalBeli, totalHarga);
        this.idPembelian = idPembelian;
        this.idObat = idObat;
        this.idSupplier = idSupplier;
    }

    // Constructor tanpa id, dipakai saat mau INSERT data baru
    public Pembelian(int idObat, int idSupplier, int jumlah, Date tanggalBeli, int totalHarga) {
        super(jumlah, tanggalBeli, totalHarga);
        this.idObat = idObat;
        this.idSupplier = idSupplier;
    }

    public int getIdPembelian() {
        return idPembelian;
    }

    public void setIdPembelian(int idPembelian) {
        this.idPembelian = idPembelian;
    }

    public int getIdObat() {
        return idObat;
    }

    public void setIdObat(int idObat) {
        this.idObat = idObat;
    }

    public int getIdSupplier() {
        return idSupplier;
    }

    public void setIdSupplier(int idSupplier) {
        this.idSupplier = idSupplier;
    }

    // Alias biar nama method-nya sesuai nama kolom di tabel pembelian
    // (di belakangnya tetap manggil field jumlah/tanggal/totalHarga dari Transaksi)
    public Date getTanggalBeli() {
        return getTanggal();
    }

    public void setTanggalBeli(Date tanggalBeli) {
        setTanggal(tanggalBeli);
    }

    public String getNamaObat() {
        return namaObat;
    }

    public void setNamaObat(String namaObat) {
        this.namaObat = namaObat;
    }

    public String getNamaSupplier() {
        return namaSupplier;
    }

    public void setNamaSupplier(String namaSupplier) {
        this.namaSupplier = namaSupplier;
    }

    @Override
    public String getJenisTransaksi() {
        return "Pembelian dari Supplier";
    }
}