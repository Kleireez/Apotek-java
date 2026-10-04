/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.Date;

/**
 * Model untuk tabel `obat`.
 */
public class Obat {

    private int idObat;
    private int idSupplier;
    private String namaObat;
    private String jenis;
    private int stok;
    private int hargaBeli;
    private int hargaJual;
    private Date tanggalKadaluarsa;

    // Dipakai untuk menampilkan nama supplier di JTable (hasil JOIN),
    // bukan kolom asli di tabel obat
    private String namaSupplier;

    public Obat() {
    }

    public Obat(int idObat, int idSupplier, String namaObat, String jenis, int stok,
            int hargaBeli, int hargaJual, Date tanggalKadaluarsa) {
        this.idObat = idObat;
        this.idSupplier = idSupplier;
        this.namaObat = namaObat;
        this.jenis = jenis;
        this.stok = stok;
        this.hargaBeli = hargaBeli;
        this.hargaJual = hargaJual;
        this.tanggalKadaluarsa = tanggalKadaluarsa;
    }

    // Constructor tanpa id, dipakai saat mau INSERT data baru
    public Obat(int idSupplier, String namaObat, String jenis, int stok,
            int hargaBeli, int hargaJual, Date tanggalKadaluarsa) {
        this.idSupplier = idSupplier;
        this.namaObat = namaObat;
        this.jenis = jenis;
        this.stok = stok;
        this.hargaBeli = hargaBeli;
        this.hargaJual = hargaJual;
        this.tanggalKadaluarsa = tanggalKadaluarsa;
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

    public String getNamaObat() {
        return namaObat;
    }

    public void setNamaObat(String namaObat) {
        this.namaObat = namaObat;
    }

    public String getJenis() {
        return jenis;
    }

    public void setJenis(String jenis) {
        this.jenis = jenis;
    }

    public int getStok() {
        return stok;
    }

    public void setStok(int stok) {
        this.stok = stok;
    }

    public int getHargaBeli() {
        return hargaBeli;
    }

    public void setHargaBeli(int hargaBeli) {
        this.hargaBeli = hargaBeli;
    }

    public int getHargaJual() {
        return hargaJual;
    }

    public void setHargaJual(int hargaJual) {
        this.hargaJual = hargaJual;
    }

    public Date getTanggalKadaluarsa() {
        return tanggalKadaluarsa;
    }

    public void setTanggalKadaluarsa(Date tanggalKadaluarsa) {
        this.tanggalKadaluarsa = tanggalKadaluarsa;
    }

    public String getNamaSupplier() {
        return namaSupplier;
    }

    public void setNamaSupplier(String namaSupplier) {
        this.namaSupplier = namaSupplier;
    }

    @Override
    public String toString() {
        return namaObat;
    }
}