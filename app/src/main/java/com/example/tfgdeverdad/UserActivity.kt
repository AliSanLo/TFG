package com.example.tfgdeverdad

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class UserActivity : AppCompatActivity() {

    private lateinit var edtCurrentPassword: EditText
    private lateinit var edtNewPassword: EditText
    private lateinit var btnChangePassword: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var user: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        edtCurrentPassword = findViewById(R.id.edtCurrentPassword)
        edtNewPassword = findViewById(R.id.edtNewPassword)
        btnChangePassword = findViewById(R.id.btnChangePassword)

        mAuth = FirebaseAuth.getInstance()
        user = mAuth.currentUser!!

        btnChangePassword.setOnClickListener {
            val currentPassword = edtCurrentPassword.text.toString().trim()
            val newPassword = edtNewPassword.text.toString().trim()

            if (currentPassword.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                cambiarContraseña(currentPassword, newPassword)
            }
        }

        //Flecha para ir a la actividad anterior
        val flechaAtras: ImageView = findViewById(R.id.flechaAtras)
        flechaAtras.setOnClickListener {
            finish()
        }
    }

    private fun cambiarContraseña(currentPassword: String, newPassword: String) {
        // Reautenticar al usuario con la contraseña actual
        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

        user.reauthenticate(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Reautenticación exitosa, ahora cambiamos la contraseña
                    user.updatePassword(newPassword)
                        .addOnCompleteListener { passwordUpdateTask ->
                            if (passwordUpdateTask.isSuccessful) {
                                Toast.makeText(this, "Contraseña cambiada con éxito", Toast.LENGTH_SHORT).show()
                                finish()  // Cierra la actividad y regresa a la anterior
                            } else {
                                Toast.makeText(this, "Error al cambiar la contraseña", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Contraseña actual incorrecta", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
