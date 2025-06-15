package model;

public class User {
    private int id;
    private String username;
    private String role;
    private String nama;
    private String jabatan;
    private String foto;

    public User(int id, String username, String role, String nama, String jabatan, String foto) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.nama = nama;
        this.jabatan = jabatan;
        this.foto = foto;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getNama() {
        return nama;
    }

    public String getJabatan() {
        return jabatan;
    }

    public String getFoto() {
        return foto;
    }
}
