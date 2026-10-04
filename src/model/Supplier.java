/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 * Model untuk tabel `supplier`.
 */
public class Supplier {

    private int idSupplier;
    private String namaSupplier;
    private String alamat;
    private String noTelepon;

    public Supplier() {
    }

    public Supplier(int idSupplier, String namaSupplier, String alamat, String noTelepon) {
        this.idSupplier = idSupplier;
        this.namaSupplier = namaSupplier;
        this.alamat = alamat;
        this.noTelepon = noTelepon;
    }

    // Constructor tanpa id, dipakai saat mau INSERT data baru
    public Supplier(String namaSupplier, String alamat, String noTelepon) {
        this.namaSupplier = namaSupplier;
        this.alamat = alamat;
        this.noTelepon = noTelepon;
    }

    public int getIdSupplier() {
        return idSupplier;
    }

    public void setIdSupplier(int idSupplier) {
        this.idSupplier = idSupplier;
    }

    public String getNamaSupplier() {
        return namaSupplier;
    }

    public void setNamaSupplier(String namaSupplier) {
        this.namaSupplier = namaSupplier;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public String getNoTelepon() {
        return noTelepon;
    }

    public void setNoTelepon(String noTelepon) {
        this.noTelepon = noTelepon;
    }

    @Override
    public String toString() {
        return namaSupplier;
    }
}