package com.example.tfgdeverdad

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import java.text.SimpleDateFormat
import java.util.Locale

class EditEventActivity : AppCompatActivity() {

    private var eventId: String
    ? = null
    private var userId: String? = null

    // Campos del formulario
    private lateinit var etTitulo: EditText
    private lateinit var etHoraInicio: EditText
    private lateinit var etHoraFin: EditText
    private lateinit var etNotas: EditText
    private lateinit var etSticker: EditText
    private lateinit var etFecha: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Recuperamos el ID del evento del Intent
        eventId = intent.getStringExtra("event_id")
        userId = FirebaseAuth.getInstance().currentUser?.uid

        // Verificamos si el eventId y el userId están disponibles
        if (eventId == null || userId == null) {
            Toast.makeText(this, "Error al cargar el evento", Toast.LENGTH_SHORT).show()
            finish() // Cierra la actividad si no hay eventId o userId
            return
        }

        // Conectamos la clase con el diseño XML
        setContentView(R.layout.activity_edit_event)


        //Flecha para ir a la actividad anterior
        val flechaAtras: ImageView = findViewById(R.id.flechaAtras)
        flechaAtras.setOnClickListener {
            finish()
        }

        // Referenciamos los EditText de la interfaz
        etTitulo = findViewById(R.id.etTitulo)
        etHoraInicio = findViewById(R.id.etHoraInicio)
        etHoraFin = findViewById(R.id.etHoraFin)
        etNotas = findViewById(R.id.etNotas)
        etSticker = findViewById(R.id.etSticker)
        etFecha = findViewById(R.id.etFecha)

        // Cargar el evento de Firebase Firestore y los mete en los EditText
        cargarEvento()

    //botón guardar cambios
        val btnGuardar: Button = findViewById(R.id.btnGuardar)
        btnGuardar.setOnClickListener {
            guardarCambios()
        }
    }
    private fun guardarCambios() {
        //Conectamos con la base de datos
        val db = FirebaseFirestore.getInstance()

        //sacamos los valores escritos por el usuario
        val titulo = etTitulo.text.toString()
        val horaInicio = etHoraInicio.text.toString()
        val horaFin = etHoraFin.text.toString()
        val notas = etNotas.text.toString()
        val sticker = etSticker.text.toString()
        val fechaStr = etFecha.text.toString()

        //Convertimos la fecha a un objeto Date. Si falla devuelvo un null
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaInicio = try {
            sdf.parse(fechaStr)
        } catch (e: Exception) {
            null
        }
        if (fechaInicio == null) {
            Toast.makeText(this, "Formato de fecha incorrecto", Toast.LENGTH_SHORT).show()
            return
        }

        //Map con todos los datos actualizados.
        val updatedData = mapOf(
            "titulo" to titulo,
            "horaInicio" to horaInicio,
            "horaFin" to horaFin,
            "notas" to notas,
            "sticker" to sticker,
            "fechaInicio" to com.google.firebase.Timestamp(fechaInicio) //lo convertimos de Date a Timestamp para Firebase
        )

        //busco el documento en kotlin y lo actualizamos
        db.collection("eventos").document(eventId!!)
            .update(updatedData)
            .addOnSuccessListener {
                Toast.makeText(this, "Evento actualizado", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar cambios", Toast.LENGTH_SHORT).show()
            }
    }

    //Sacar el evento de Firestore
    private fun cargarEvento() {
        // Obtener la referencia de Firestore
        val db = FirebaseFirestore.getInstance()
        //lo buscamos en la coleccion eventos por eventId
        db.collection("eventos").document(eventId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Si el evento existe, mostramos los datos en los campos
                    cargarDatosEvento(document)
                } else {
                    Toast.makeText(this, "Evento no encontrado", Toast.LENGTH_SHORT).show()
                    finish() // Si no se encuentra el evento, cerramos la actividad
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar el evento", Toast.LENGTH_SHORT).show()
                finish() // Si falla la carga, cerramos la actividad
            }
    }

    private fun cargarDatosEvento(document: DocumentSnapshot) {
        // Cargamos los datos del evento desde Firestore
        etTitulo.setText(document.getString("titulo"))
        etHoraInicio.setText(document.getString("horaInicio"))
        etHoraFin.setText(document.getString("horaFin"))
        etNotas.setText(document.getString("notas"))
        etSticker.setText(document.getString("sticker"))

        // Formatear y mostrar la fecha si es posible
        val fechaTimestamp = document.getTimestamp("fechaInicio")
        fechaTimestamp?.toDate()?.let {
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            etFecha.setText(sdf.format(it))
        }
    }
}
