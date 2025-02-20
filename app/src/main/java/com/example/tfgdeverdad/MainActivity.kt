package com.example.simplelogin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tfgdeverdad.LoginActivity
import com.example.tfgdeverdad.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Iniciar las referencias de los botones
        val loginButton = findViewById<Button>(R.id.login_button)
        val registerButton = findViewById<Button>(R.id.register_button)

        // Acción para "Iniciar Sesión"
        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent) // Iniciar la nueva actividad
        }

        // Acción para "Registrarse"
        /*
        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)

        }
        */
    }
}
