package com.intecap.inventariofirebase;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    private static final String USUARIO = "admin";
    private static final String CLAVE = "1234";

    private EditText etUsuario, etClave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Si ya inició sesión antes, pasa directo al Home
        SharedPreferences sp = getSharedPreferences("sesion", MODE_PRIVATE);
        if (sp.getBoolean("activa", false)) {
            irAlHome();
            return;
        }

        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        etUsuario = findViewById(R.id.etUsuario);
        etClave = findViewById(R.id.etClave);
        findViewById(R.id.btnIngresar).setOnClickListener(v -> ingresar());
    }

    private void ingresar() {
        String u = etUsuario.getText().toString().trim();
        String c = etClave.getText().toString();

        etUsuario.setError(null);
        etClave.setError(null);

        if (u.isEmpty()) {
            etUsuario.setError("Ingresa el usuario");
            return;
        }
        if (c.isEmpty()) {
            etClave.setError("Ingresa la contraseña");
            return;
        }
        if (!u.equals(USUARIO) || !c.equals(CLAVE)) {
            etClave.setError("Usuario o contraseña incorrectos");
            return;
        }

        getSharedPreferences("sesion", MODE_PRIVATE)
                .edit().putBoolean("activa", true).apply();
        irAlHome();
    }

    private void irAlHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}