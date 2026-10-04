/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.Date;


public abstract class Transaksi {

    protected int jumlah;
    protected Date tanggal;
    protected int totalHarga;

    public Transaksi() {
    }

    public Transaksi(int jumlah, Date tanggal, int totalHarga) {
        this.jumlah = jumlah;
        this.tanggal = tanggal;
        this.totalHarga = totalHarga;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }

    public Date getTanggal() {
        return tanggal;
    }

    public void setTanggal(Date tanggal) {
        this.tanggal = tanggal;
    }

    public int getTotalHarga() {
        return totalHarga;
    }

    public void setTotalHarga(int totalHarga) {
        this.totalHarga = totalHarga;
    }

    /**
     * Method abstrak, WAJIB di-override di setiap subclass
     * dengan jenis transaksi masing-masing.
     * Ini contoh polimorfisme: method sama, hasil beda-beda
     * tergantung objeknya Pembelian atau Penjualan.
     */
    public abstract String getJenisTransaksi();
}
