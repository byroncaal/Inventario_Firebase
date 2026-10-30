package com.intecap.inventariofirebase;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReporteActivity extends AppCompatActivity {

    private DatabaseReference ref;
    private ValueEventListener listener;

    private TextView tvTotalProductos, tvTotalUnidades, tvValor, tvBajas;
    private EditText etBuscar;

    private final List<Producto> todos = new ArrayList<>();
    private final List<String> textos = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reporte);
        NavHelper.configurar(this, NavHelper.REPORTE);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        tvTotalProductos = findViewById(R.id.tvTotalProductos);
        tvTotalUnidades = findViewById(R.id.tvTotalUnidades);
        tvValor = findViewById(R.id.tvValor);
        tvBajas = findViewById(R.id.tvBajas);
        etBuscar = findViewById(R.id.etBuscar);
        ListView lvLista = findViewById(R.id.lvLista);

        // Misma base que usa MainActivity
        ref = FirebaseDatabase.getInstance().getReference("productos");

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, textos);
        lvLista.setAdapter(adapter);

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { filtrar(); }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                todos.clear();
                for (DataSnapshot hijo : snapshot.getChildren()) {
                    Producto p = hijo.getValue(Producto.class);
                    if (p != null) {
                        p.setId(hijo.getKey());
                        todos.add(p);
                    }
                }
                calcular();
                filtrar();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "Error: " + error.getMessage());
            }
        };
        ref.addValueEventListener(listener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listener != null) ref.removeEventListener(listener);
    }

    private void calcular() {
        int unidades = 0;
        double valor = 0;
        StringBuilder bajas = new StringBuilder();

        for (Producto p : todos) {
            unidades += p.getCantidad();
            valor += p.getPrecio() * p.getCantidad();
            if (p.getCantidad() <= Producto.STOCK_BAJO) {
                bajas.append("⚠ ").append(p.getNombre()).append(" — ")
                        .append(p.getCantidad()).append(" u.\n");
            }
        }

        tvTotalProductos.setText(String.valueOf(todos.size()));
        tvTotalUnidades.setText(String.valueOf(unidades));
        tvValor.setText(valor >= 1000
                ? String.format(Locale.US, "Q%.1fk", valor / 1000)
                : String.format(Locale.US, "Q%.0f", valor));
        tvBajas.setText(bajas.length() == 0 ? "Sin alertas" : bajas.toString().trim());
    }

    private void filtrar() {
        String q = etBuscar.getText().toString().trim().toLowerCase(Locale.getDefault());
        textos.clear();
        for (Producto p : todos) {
            String nombre = p.getNombre() == null ? "" : p.getNombre();
            if (q.isEmpty() || nombre.toLowerCase(Locale.getDefault()).contains(q)) {
                textos.add(String.format(Locale.US, "%s — Q%.2f", nombre, p.getPrecio()));
            }
        }
        adapter.notifyDataSetChanged();
    }
}