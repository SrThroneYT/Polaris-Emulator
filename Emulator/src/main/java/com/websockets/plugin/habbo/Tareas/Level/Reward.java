package com.websockets.plugin.habbo.Tareas.Level;

public class Reward {

    public String name;
    public int exist;
    public int cantidad;
    public String title;
    public String descripcion;

    public Reward(String name, int exist, int cantidad, String title, String descripcion) {
        this.name = name;
        this.exist = exist;
        this.cantidad = cantidad;
        this.title = title;
        this.descripcion = descripcion;
    }

    public String getName() {
        return name;
    }

    // Alias compatible para simplificar el flujo con "nombre"
    public String getNombre() {
        return name;
    }

    public int getExist() {
        return exist;
    }

    // Alias compatible para simplificar el flujo con "item_exist"
    public int getItemExist() {
        return exist;
    }

    public int getCantidad() {
        return cantidad;
    }

    public String getTitle() {
        return title;
    }

    public String getDescripcion() {
        return descripcion;
    }

    // --- SETTERS ---

    public void setName(String name) {
        this.name = name;
    }

    // Setter alias para coincidir con el controlador
    public void setNombre(String nombre) {
        this.name = nombre;
    }

    public void setExist(int exist) {
        this.exist = exist;
    }

    // Setter alias para coincidir con el controlador
    public void setItemExist(int itemExist) {
        this.exist = itemExist;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}