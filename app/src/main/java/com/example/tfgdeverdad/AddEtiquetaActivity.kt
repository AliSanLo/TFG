package com.example.tfgdeverdad

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddEtiquetaActivity : AppCompatActivity() {

    //declaración de variables
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

        //selección de colores. se almacenan en un array para mostrarlo al spinner
        val colores = resources.getStringArray(R.array.colores_etiqueta)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, colores)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        colorInput.adapter = adapter
        //mapeo el nombre de color a su código HEX
        val colorHexMap = mapOf(
            "Rojo" to "#FF0000",
            "Verde" to "#00FF00",
            "Azul" to "#0000FF",
            "Amarillo" to "#FFFF00",
            "Morado" to "#800080"
        )

        //seleccion de sticker. Mismo sistema que con los colores
        val stickers = resources.getStringArray(R.array.sticker_etiqueta)
        val adapterStickers = ArrayAdapter(this, android.R.layout.simple_spinner_item, stickers)
        adapterStickers.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        stickerInput.adapter = adapterStickers

        //prioridad
        val valorPrioridad: TextView = findViewById(R.id.valorPrioridad)

        //configuro un listener para detectar cambios en la barra de prioridad
        prioridadInput.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                valorPrioridad.text = (progress + 1).toString()
            }

            //Cada vez que mueve, actualiza el TexView
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        //Boton guardar
        guardarBtn.setOnClickListener {
            val colorNombre = colorInput.selectedItem.toString()
            val stickerSeleccionado = stickerInput.selectedItem.toString()

            val etiqueta = Etiqueta(
                titulo = tituloInput.text.toString(),
                color = colorInput.selectedItem.toString(),
                prioridad = prioridadInput.progress + 1,
                sticker = stickerSeleccionado,
                userId = FirebaseAuth.getInstance().currentUser!!.uid
            )

            //Se guarda en la base de datos
            FirebaseFirestore.getInstance().collection("etiquetas")
                .add(etiqueta)
                //si se guarda bien muestra un mensaje y cierra la activity
                .addOnSuccessListener {
                    Toast.makeText(this, "Etiqueta guardada 🔖", Toast.LENGTH_SHORT).show()
                    finish()
                }
                //si falla muestra un error y que aparece por pantalla
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }

        }
        valorPrioridad.text = (prioridadInput.progress + 1).toString()
    }
}
