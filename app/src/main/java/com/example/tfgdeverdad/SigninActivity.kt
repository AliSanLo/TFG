package com.example.tfgdeverdad

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class SigninActivity : AppCompatActivity() {

    private lateinit var regNombre: EditText
    private lateinit var regEmail: EditText
    private lateinit var regPassword: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        regNombre = findViewById(R.id.nombre)
        regEmail = findViewById(R.id.regEmail)
        regPassword = findViewById(R.id.regContraseña)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    fun signIn(view: View) {
        val nombre = regNombre.text.toString().trim()
        val email = regEmail.text.toString().trim()
        val password = regPassword.text.toString().trim()

        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    val dateRegister = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                    val userMap = hashMapOf(
                        "nombre" to nombre,
                        "email" to email,
                        "dateRegister" to dateRegister
                    )

                    // Usamos el UID como ID del documento para que no se repita
                    userId?.let {
                        db.collection("users").document(it).set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registro completado", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Error al registrar: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
