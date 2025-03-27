package com.example.tfgdeverdad

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TermsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_terms)

        val toolbarTextView: TextView = findViewById(R.id.TermsTitle)  // Asegúrate de que el ID sea correcto
        toolbarTextView.text = getString(R.string.terms)  // Cambiar el texto dinámicamente si es necesario
    }
}