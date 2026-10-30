package com.intecap.inventariofirebase;

public class Producto {
    public static final int STOCK_BAJO = 5;
    private String id;
    private String nombre;
    private String descripcion;
    private double precio;
    private int cantidad;      // stock actual
    private int stockMinimo;   // umbral de alerta

    // Requerido por Firebase
    public Producto() {}

    // Constructor original (se mantiene por compatibilidad)
    public Producto(String id, double precio, String nombre, int cantidad) {
        this(id, precio, nombre, "", cantidad, 0);
    }

    public Producto(String id, double precio, String nombre, String descripcion,
                    int cantidad, int stockMinimo) {
        this.id = id;
        this.precio = precio;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.stockMinimo = stockMinimo;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }
}