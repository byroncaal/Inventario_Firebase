package com.intecap.inventariofirebase;

public class Movimiento {
    private String tipo;      // "ENTRADA" o "SALIDA"
    private int cantidad;
    private long fecha;

    public Movimiento() {}

    public Movimiento(String tipo, int cantidad, long fecha) {
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.fecha = fecha;
    }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public long getFecha() { return fecha; }
    public void setFecha(long fecha) { this.fecha = fecha; }
    public static final int STOCK_BAJO = 5;
}