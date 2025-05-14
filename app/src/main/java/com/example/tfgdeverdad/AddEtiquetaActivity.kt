package com.example.tfgdeverdad

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddEtiquetaActivity : AppCompatActivity() {
    private lateinit var tituloInput: EditText
    private lateinit var colorInput: Spinner
    private lateinit var prioridadInput: SeekBar
    private lateinit var stickerInput: Spinner
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

        //selección de colores
        val colores = resources.getStringArray(R.array.colores_etiqueta)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, colores)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        colorInput.adapter = adapter
        val colorHexMap = mapOf(
            "Rojo" to "#FF0000",
            "Verde" to "#00FF00",
            "Azul" to "#0000FF",
            "Amarillo" to "#FFFF00",
            "Morado" to "#800080"
        )


        //seleccion de sticker
        val stickers = resources.getStringArray(R.array.sticker_etiqueta)
        val adapterStickers = ArrayAdapter(this, android.R.layout.simple_spinner_item, stickers)
        adapterStickers.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        stickerInput.adapter = adapterStickers


        val valorPrioridad: TextView = findViewById(R.id.valorPrioridad)

        prioridadInput.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                valorPrioridad.text = (progress + 1).toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })



        guardarBtn.setOnClickListener {
            val colorNombre = colorInput.selectedItem.toString()
            val stickerSeleccionado = stickerInput.selectedItem.toString()

            val etiqueta = Etiqueta(
                titulo = tituloInput.text.toString(),
                color = colorInput.selectedItem.toString(),
                prioridad = prioridadInput.progress + 1,
                sticker = stickerSeleccionado,
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
        valorPrioridad.text = (prioridadInput.progress + 1).toString()

    }
}
