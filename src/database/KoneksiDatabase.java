package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class KoneksiDatabase {

    private static final String URL = "jdbc:mysql://localhost:3306/apotek_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static Connection connection;

    private KoneksiDatabase() {
    }

    /**
     * Mengambil koneksi ke database. Kalau koneksi belum ada atau sudah
     * tertutup, akan dibuat koneksi baru.
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null,
                    "Driver MySQL tidak ditemukan.\n"
                    + "Pastikan mysql-connector-j sudah ditambahkan ke Library project.",
                    "Error Driver", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Gagal konek ke database:\n" + e.getMessage(),
                    "Error Koneksi", JOptionPane.ERROR_MESSAGE);
        }
        return connection;
    }

    /**
     * Menutup koneksi (opsional, dipanggil misalnya saat aplikasi ditutup).
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Bisa dipakai untuk test koneksi cepat tanpa buka form apapun
    public static void main(String[] args) {
        if (getConnection() != null) {
            System.out.println("Koneksi database BERHASIL!");
        } else {
            System.out.println("Koneksi database GAGAL.");
        }
    }
}