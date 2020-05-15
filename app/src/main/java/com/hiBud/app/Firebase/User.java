package com.hiBud.app.Firebase;

public class User {

    private String fotoPerfilURL;
    private String nombre;
    private String correo;
    private long fechaDeNacimiento;
    private String genero;

    public User() {
    }

    public String getFotoPerfilURL() {
        return fotoPerfilURL;
    }

    public void setFotoPerfilURL(String fotoPerfilURL) {
        this.fotoPerfilURL = fotoPerfilURL;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void setFechaDeNacimiento(long fechaDeNacimiento) {
        this.fechaDeNacimiento = fechaDeNacimiento;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }
}
