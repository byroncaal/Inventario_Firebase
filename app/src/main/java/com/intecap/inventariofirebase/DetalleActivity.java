package com.intecap.inventariofirebase;

import static android.content.ContentValues.TAG;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DetalleActivity extends AppCompatActivity {

    private String productoId;
    private DatabaseReference refProducto, refMovs;
    private ValueEventListener listenerProducto, listenerMovs;

    private TextView tvNombre, tvInfo;
    private EditText etCantidad;
    private RadioButton rbEntrada;

    private final List<String> textos = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detalle);
        NavHelper.configurar(this, NavHelper.DETALLE);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        productoId = getIntent().getStringExtra("id");
        if (productoId == null) {
            finish();
            return;
        }

        tvNombre = findViewById(R.id.tvNombre);
        tvInfo = findViewById(R.id.tvInfo);
        etCantidad = findViewById(R.id.etCantidad);
        rbEntrada = findViewById(R.id.rbEntrada);
        ListView lvMovimientos = findViewById(R.id.lvMovimientos);

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        refProducto = db.getReference("productos").child(productoId);
        refMovs = db.getReference("movimientos").child(productoId);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, textos);
        lvMovimientos.setAdapter(adapter);

        findViewById(R.id.btnRegistrar).setOnClickListener(v -> registrar());
    }

    @Override
    protected void onStart() {
        super.onStart();
        escucharProducto();
        escucharMovimientos();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listenerProducto != null) refProducto.removeEventListener(listenerProducto);
        if (listenerMovs != null) refMovs.removeEventListener(listenerMovs);
    }

    private void escucharProducto() {
        listenerProducto = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Producto p = snapshot.getValue(Producto.class);
                if (p == null) {          // el producto fue eliminado
                    finish();
                    return;
                }
                tvNombre.setText(p.getNombre());
                tvInfo.setText(String.format(Locale.US, "Q%.2f · %d unidades",
                        p.getPrecio(), p.getCantidad()));
                tvInfo.setTextColor(p.getCantidad() <= Producto.STOCK_BAJO
                        ? Color.parseColor("#C62828") : Color.parseColor("#333333"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "Error: " + error.getMessage());
            }
        };
        refProducto.addValueEventListener(listenerProducto);
    }

    private void escucharMovimientos() {
        listenerMovs = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                textos.clear();
                for (DataSnapshot hijo : snapshot.getChildren()) {
                    Movimiento m = hijo.getValue(Movimiento.class);
                    if (m == null) continue;
                    boolean entrada = "ENTRADA".equals(m.getTipo());
                    String linea = (entrada ? "+" : "-") + m.getCantidad()
                            + " · " + (entrada ? "Compra" : "Venta")
                            + " · " + sdf.format(new Date(m.getFecha()));
                    textos.add(0, linea);   // el más reciente arriba
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "Error: " + error.getMessage());
            }
        };
        refMovs.orderByChild("fecha").limitToLast(50).addValueEventListener(listenerMovs);
    }

    private void registrar() {
        int cant;
        try {
            cant = Integer.parseInt(etCantidad.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ingresa una cantidad válida", Toast.LENGTH_SHORT).show();
            return;
        }
        if (cant <= 0) {
            Toast.makeText(this, "La cantidad debe ser mayor que 0", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean entrada = rbEntrada.isChecked();
        int delta = entrada ? cant : -cant;

        refProducto.child("cantidad").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData data) {
                Integer actual = data.getValue(Integer.class);
                if (actual == null) return Transaction.success(data); // Firebase reintenta con el valor real
                int nuevo = actual + delta;
                if (nuevo < 0) return Transaction.abort();            // stock insuficiente
                data.setValue(nuevo);
                return Transaction.success(data);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed,
                                   @Nullable DataSnapshot snapshot) {
                if (error != null) {
                    Log.d(TAG, "Error: " + error.getMessage());
                    Toast.makeText(DetalleActivity.this, "Error: " + error.getMessage(),
                            Toast.LENGTH_LONG).show();
                } else if (!committed) {
                    Toast.makeText(DetalleActivity.this, "Stock insuficiente",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Movimiento m = new Movimiento(entrada ? "ENTRADA" : "SALIDA",
                            cant, System.currentTimeMillis());
                    refMovs.push().setValue(m);
                    etCantidad.setText("");
                    Toast.makeText(DetalleActivity.this, "Movimiento registrado",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}