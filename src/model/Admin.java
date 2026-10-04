/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 * Model untuk tabel `admin`.
 */
public class Admin {

    private int idAdmin;
    private String username;
    private String password;
    private String role;

    public Admin() {
    }

    public Admin(int idAdmin, String username, String password, String role) {
        this.idAdmin = idAdmin;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Constructor tanpa id, dipakai saat mau INSERT data baru
    public Admin(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public int getIdAdmin() {
        return idAdmin;
    }

    public void setIdAdmin(int idAdmin) {
        this.idAdmin = idAdmin;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return username;
    }
}
