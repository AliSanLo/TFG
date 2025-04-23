package com.example.tfgdeverdad

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class AddEventActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_event)



        // Inicialización del Toolbar y Drawer
        initToolBar()
        initNavigationView()

        val tituloEvento = findViewById<EditText>(R.id.TituloEvento)
        val etFechaStart = findViewById<EditText>(R.id.StartDate)
        val etFechaEnd = findViewById<EditText>(R.id.EndDate)
        val editTextHoraInic = findViewById<EditText>(R.id.HoraInic)
        val editTextHoraFin = findViewById<EditText>(R.id.HoraFin)
        val notasAdic = findViewById<EditText>(R.id.Notas)
        val stickerSelector = findViewById<MaterialAutoCompleteTextView>(R.id.stickerSelector)

        //Stickers
        val stickers = listOf(
            "🎉 Fiesta",
            "🐱 Gato",
            "🔥 Fuego",
            "🌟 Estrella",
            "📚 Estudio",
            "💻 Código",
            "🧠 Focus",
            "🎮 Break",
            "📝 Tareas"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, stickers)
        stickerSelector.setAdapter(adapter)
        val stickerElegido = stickerSelector.text.toString()

        //Guardar eventos en Firebase
        val evento = hashMapOf(
            "titulo" to tituloEvento.text.toString(),
            "fechaInicio" to etFechaStart.text.toString(),
            "fechaFin" to etFechaEnd.text.toString(),
            "horaInicio" to editTextHoraInic.text.toString(),
            "horaFin" to editTextHoraFin.text.toString(),
            "notas" to notasAdic.text.toString(),
            "sticker" to stickerSelector.text.toString()
        )

        FirebaseFirestore.getInstance().collection("eventos")
            .add(evento)
            .addOnSuccessListener { doc -> Log.d("Firebase", "Evento guardado: ${doc.id}") }
            .addOnFailureListener { e -> Log.e("Firebase", "Error: ", e) }


        //Título de la pagina
        val titulo = findViewById<TextView>(R.id.titulo1)
        val paint = titulo.paint
        val width = paint.measureText(titulo.text.toString())
        val textShader = LinearGradient(
            0f, 0f, width, titulo.textSize,
            intArrayOf(Color.parseColor("#8E2DE2"), Color.parseColor("#4A00E0")),
            null, Shader.TileMode.CLAMP
        )
        titulo.paint.shader = textShader



        etFechaStart.setOnClickListener {
            val calendario = Calendar.getInstance()

            val year = calendario.get(Calendar.YEAR)
            val month = calendario.get(Calendar.MONTH)
            val day = calendario.get(Calendar.DAY_OF_MONTH)
            val datePicker = DatePickerDialog(
                this,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth, // <--- Estilo clásico
                { _, selectedYear, selectedMonth, selectedDay ->
                    val fechaElegida = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    etFechaStart.setText(fechaElegida)

                },
                year, month, day

            )
            datePicker.window?.setBackgroundDrawableResource(android.R.color.transparent)

            datePicker.show()
        }

        etFechaEnd.setOnClickListener {
            val calendario = Calendar.getInstance()

            val year = calendario.get(Calendar.YEAR)
            val month = calendario.get(Calendar.MONTH)
            val day = calendario.get(Calendar.DAY_OF_MONTH)
            val datePicker = DatePickerDialog(
                this,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth, // <--- Estilo clásico
                { _, selectedYear, selectedMonth, selectedDay ->
                    val fechaElegida = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    etFechaEnd.setText(fechaElegida)

                },
                year, month, day

            )
            datePicker.window?.setBackgroundDrawableResource(android.R.color.transparent)

            datePicker.show()
        }

        editTextHoraInic.setOnClickListener {
            val calendario = Calendar.getInstance()
            val hora = calendario.get(Calendar.HOUR_OF_DAY)
            val minuto = calendario.get(Calendar.MINUTE)

            val timePicker = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    val horaFormateada = String.format("%02d:%02d", hourOfDay, minute)
                    editTextHoraInic.setText(horaFormateada)
                },
                hora, minuto, true
            )
            timePicker.show()
        }

        editTextHoraFin.setOnClickListener {
            val calendario = Calendar.getInstance()
            val hora = calendario.get(Calendar.HOUR_OF_DAY)
            val minuto = calendario.get(Calendar.MINUTE)

            val timePicker = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    val horaFormateada = String.format("%02d:%02d", hourOfDay, minute)
                    editTextHoraFin.setText(horaFormateada)
                },
                hora, minuto, true
            )
            timePicker.show()
        }
        val btnGuardar = findViewById<Button>(R.id.GuardarEvento)
        btnGuardar.setOnClickListener {
            guardarEvento()
        }
    }

    private fun initToolBar() {
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)

        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.empty, R.string.empty
        )

        drawer.addDrawerListener(toggle)
        toggle.syncState()
    }



    //Quitar la siguiente funcion si no hago cabecera de menu
    private fun initNavigationView() {
        val navigationView: NavigationView =
            findViewById(R.id.nav_view) //conectamos con el laytout correspondiente por el id
        navigationView.setNavigationItemSelectedListener(this) //para reconocer cuando se está haciendo click en un elemento del menu y generar acciones

        val headerView: View = LayoutInflater.from(this).inflate(
            R.layout.nav_header_main,
            navigationView,
            false
        )//cada vez que el usuario entre y salga
        //se borra el header y vuelve a ponerse con los datos cargados para que esté siempre actualizado cada vez que abrimos y cerramos
        navigationView.removeHeaderView(headerView)
        navigationView.addHeaderView(headerView)

    }

    fun callSignOut(view: View) {
        signOut()
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
        Log.d("MainActivity", "Cierre de sesión iniciado")

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_item_signOut -> signOut()

        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun guardarEvento() {
        val titulo = findViewById<EditText>(R.id.TituloEvento).text.toString().trim()
        val fechaInicio = findViewById<EditText>(R.id.StartDate).text.toString().trim()
        val fechaFin = findViewById<EditText>(R.id.EndDate).text.toString().trim()
        val horaInicio = findViewById<EditText>(R.id.HoraInic).text.toString().trim()
        val horaFin = findViewById<EditText>(R.id.HoraFin).text.toString().trim()
        val notas = findViewById<EditText>(R.id.Notas).text.toString().trim()
        val sticker = findViewById<AutoCompleteTextView>(R.id.stickerSelector).text.toString().trim()

        // Validación rápida
        if (titulo.isEmpty() || fechaInicio.isEmpty() ) {
            Toast.makeText(this, "Por favor, completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val evento = hashMapOf(
            "titulo" to titulo,
            "fechaInicio" to fechaInicio,
            "fechaFin" to fechaFin,
            "horaInicio" to horaInicio,
            "horaFin" to horaFin,
            "notas" to notas,
            "sticker" to sticker
        )

        FirebaseFirestore.getInstance().collection("eventos")
            .add(evento)
            .addOnSuccessListener { doc ->
                Toast.makeText(this, "Evento guardado con éxito 🎉", Toast.LENGTH_SHORT).show()
                Log.d("Firebase", "Evento guardado con ID: ${doc.id}")
                finish() // opcional: cerrar la actividad
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("Firebase", "Error al guardar evento", e)
            }
    }



}