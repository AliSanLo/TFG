package com.example.tfgdeverdad

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddEtiquetaActivity : AppCompatActivity() {
    private lateinit var tituloInput: EditText
    private lateinit var colorInput: EditText
    private lateinit var prioridadInput: SeekBar
    private lateinit var stickerInput: EditText
    private lateinit var urgenteCheck: CheckBox
    private lateinit var guardarBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_etiqueta)

        tituloInput = findViewById(R.id.TituloEt)
        colorInput = findViewById(R.id.ColorEt)
        prioridadInput = findViewById(R.id.Prioridad)
        stickerInput = findViewById(R.id.StickerEt)
        urgenteCheck = findViewById(R.id.checkboxUrgente)
        guardarBtn = findViewById(R.id.btnGuardarEtiqueta)

        guardarBtn.setOnClickListener {
            val etiqueta = Etiqueta(
                titulo = tituloInput.text.toString(),
                color = colorInput.text.toString(),
                prioridad = prioridadInput.progress + 1,
                sticker = stickerInput.text.toString(),
                urgente = urgenteCheck.isChecked,
                userId = FirebaseAuth.getInstance().currentUser!!.uid
            )

            FirebaseFirestore.getInstance().collection("etiquetas")
                .add(etiqueta)
                .addOnSuccessListener {
                    Toast.makeText(this, "Etiqueta guardada 🔖", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al guardar 😢", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
