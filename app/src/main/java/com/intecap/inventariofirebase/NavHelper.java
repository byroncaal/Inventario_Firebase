package com.intecap.inventariofirebase;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NavHelper {

    public static final int INICIO = 0, PRODUCTOS = 1, DETALLE = 2, REPORTE = 3;

    public static void configurar(Activity act, int actual) {
        int[] ids = {R.id.navInicio, R.id.navProductos, R.id.navDetalle, R.id.navReporte};
        for (int i = 0; i < ids.length; i++) {
            TextView tv = act.findViewById(ids[i]);
            if (tv == null) continue;
            if (i == actual) {
                tv.setBackgroundColor(Color.parseColor("#F28C00"));
                tv.setTextColor(Color.WHITE);
            } else {
                final int destino = i;
                tv.setOnClickListener(v -> ir(act, destino));
            }
        }
    }

    private static void ir(Activity act, int destino) {
        switch (destino) {
            case INICIO:
                Intent home = new Intent(act, HomeActivity.class);
                home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                act.startActivity(home);
                act.finish();
                break;
            case PRODUCTOS:
                act.startActivity(new Intent(act, MainActivity.class));
                act.finish();
                break;
            case REPORTE:
                act.startActivity(new Intent(act, ReporteActivity.class));
                act.finish();
                break;
            case DETALLE:
                elegirProducto(act);
                break;
        }
    }

    // Lista los productos para elegir de cuál se verá el detalle
    public static void elegirProducto(Activity act) {
        FirebaseDatabase.getInstance().getReference("productos")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> nombres = new ArrayList<>();
                        List<String> ids = new ArrayList<>();
                        for (DataSnapshot hijo : snapshot.getChildren()) {
                            Producto p = hijo.getValue(Producto.class);
                            if (p != null) {
                                nombres.add(p.getNombre());
                                ids.add(hijo.getKey());
                            }
                        }
                        if (ids.isEmpty()) {
                            Toast.makeText(act, "Primero registra un producto",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        new AlertDialog.Builder(act)
                                .setTitle("Elige un producto")
                                .setItems(nombres.toArray(new String[0]), (d, which) -> {
                                    Intent i = new Intent(act, DetalleActivity.class);
                                    i.putExtra("id", ids.get(which));
                                    act.startActivity(i);
                                    if (!(act instanceof HomeActivity)) act.finish();
                                })
                                .setNegativeButton("Cancelar", null)
                                .show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(act, "Error: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}