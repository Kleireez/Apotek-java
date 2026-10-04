/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

/**
 * Interface kontrak untuk semua Controller yang menangani CRUD
 * (Create, Read, Update, Delete).
 *
 * Semua Controller (ObatController, SupplierController,
 * PembelianController, PenjualanController) wajib implements
 * interface ini, supaya semua controller punya struktur method
 * yang seragam.
 */
public interface CrudInterface {

    /**
     * Menambah data baru ke database.
     * Return true kalau berhasil, false kalau gagal.
     */
    boolean tambah();

    /**
     * Mengubah/update data yang sudah ada di database.
     * Return true kalau berhasil, false kalau gagal.
     */
    boolean ubah();

    /**
     * Menghapus data dari database.
     * Return true kalau berhasil, false kalau gagal.
     */
    boolean hapus();

    /**
     * Mengambil semua data dari database
     * (biasanya dipakai untuk mengisi JTable).
     */
    void tampilkan();
}