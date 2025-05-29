package com.example.tfgdeverdad

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.icu.text.SimpleDateFormat
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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale

class AddEventActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    //variables para menjar el menu lateral y el icono de hamburguesa
    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_event)

        // llamada de las funciones del Toolbar y menu
        initToolBar()
        initNavigationView()

       //guardo las referencias a los elementos del layour
        val tituloEvento = findViewById<EditText>(R.id.TituloEvento)
        val etFechaStart = findViewById<EditText>(R.id.StartDate)
        val etFechaEnd = findViewById<EditText>(R.id.EndDate)
        val editTextHoraInic = findViewById<EditText>(R.id.HoraInic)
        val editTextHoraFin = findViewById<EditText>(R.id.HoraFin)
        val notasAdic = findViewById<EditText>(R.id.Notas)
        val stickerSelector = findViewById<TextView>(R.id.stickerSelector)

        //Lista de Stickers
        val stickers = listOf(
            "🎉",
            "🐱",
            "🔥",
            "🌟",
            "📚",
            "💻",
            "🧠",
            "🎮",
            "📝"
        )


        // Al hacer clic en el TextView de sticker, se abre el AlertDialog
        stickerSelector.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Selecciona un Sticker")

            // Usamos un ArrayAdapter para crear una lista de stickers
            //con esta funcion lambda le digo qué hacer cuando el usuario selecciona un tem de la lista
            builder.setItems(stickers.toTypedArray()) { _, which -> //which se refiere al indicd del item que srlrcciono el usuario
                //Cuando el usuario pulse un sticker, pilla el que esté en "esta" posicion de la listay pnlo en el textView
                val selectedSticker = stickers[which]
                stickerSelector.text =
                    selectedSticker  // Actualizamos el TextView con el sticker seleccionado
            }

            //se crea y muestra el AlertDialog
            val dialog = builder.create()
            dialog.show()
        }

        val titulo = findViewById<TextView>(R.id.titulo1)

        //Al pulsar el campo de fecha de inicio se abre un DatePichDialog con la fecha actual por defecto
        etFechaStart.setOnClickListener {
            val calendario = Calendar.getInstance()

            //obtiene el año mes y dia actuales
            val year = calendario.get(Calendar.YEAR)
            val month = calendario.get(Calendar.MONTH)
            val day = calendario.get(Calendar.DAY_OF_MONTH)

            //Creas el dialogo y guarda la fecha en el cmapo correcpondiente
            val datePicker = DatePickerDialog(
                this,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val fechaElegida = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    etFechaStart.setText(fechaElegida)
                }, year, month, day)

            //fondo transparente
            datePicker.window?.setBackgroundDrawableResource(android.R.color.transparent)
            //lo muestra
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
                }, year, month, day
            )
            datePicker.window?.setBackgroundDrawableResource(android.R.color.transparent)
            datePicker.show()
        }

        //TimePickerDialog con la hora actual
        editTextHoraInic.setOnClickListener {
            val calendario = Calendar.getInstance()

            //hora y minuto actuales
            val hora = calendario.get(Calendar.HOUR_OF_DAY)
            val minuto = calendario.get(Calendar.MINUTE)

            //Creo el time picker y se guarda la hora formateada como HH:mm
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

        //guarda los datos en Firestore
        val btnGuardar = findViewById<Button>(R.id.GuardarEvento)
        btnGuardar.setOnClickListener {
            guardarEvento()

        }

 //_____________ETIQUETAS_____________

        //AutoCompleteTextView para mostrar las etiqeutas personalizadas
        val selectorEtiqueta = findViewById<MaterialAutoCompleteTextView>(R.id.selectorEtiqueta)
        val etiquetasList = mutableListOf<String>()

        //obtiene instancia de Firestore y el UID del usuario autenticado
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        //Consulta todas las etiquetas del usuario y las carga en el campo de autocompletado
        db.collection("etiquetas")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val nombreEtiqueta = document.getString("nombre")
                    if (!nombreEtiqueta.isNullOrEmpty()) {
                        etiquetasList.add(nombreEtiqueta)
                    }
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, etiquetasList)
                selectorEtiqueta.setAdapter(adapter)
            }

            //si falla la consulta muestra un mensaje de error
            .addOnFailureListener { e ->
                Log.e("AddEventActivity", "Error al cargar etiquetas: ", e)
                Toast.makeText(this, "No se pudieron cargar las etiquetas", Toast.LENGTH_SHORT).show()
            }



    }
//____________FIN ETIQUETAS____________

//________TOOLBAR Y NAVEGACIÓN____________
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
//________FIN TOOLBAR Y NAVEGACIÓN____________-
    fun callSignOut(view: View) {
        signOut()
    }

    //cierre de sesión
    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
        Log.d("MainActivity", "Cierre de sesión iniciado")
    }

    //el usuario pulsa cerrar sesión en el menu
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_item_signOut -> signOut()

        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    //recoge lls datos introducidos y los guarda en Firebase
    private fun guardarEvento() {

        val titulo = findViewById<EditText>(R.id.TituloEvento).text.toString().trim()
        val fechaInicio = findViewById<EditText>(R.id.StartDate).text.toString().trim()
        val fechaFin = findViewById<EditText>(R.id.EndDate).text.toString().trim()
        val horaInicio = findViewById<EditText>(R.id.HoraInic).text.toString().trim()
        val horaFin = findViewById<EditText>(R.id.HoraFin).text.toString().trim()
        val notas = findViewById<EditText>(R.id.Notas).text.toString().trim()
        val sticker = findViewById<AutoCompleteTextView>(R.id.stickerSelector).text.toString().trim()

        val etiquetaSeleccionada = findViewById<MaterialAutoCompleteTextView>(R.id.selectorEtiqueta).text.toString().trim()


        // Validación rápida
        if (titulo.isEmpty() || fechaInicio.isEmpty() ) {
            Toast.makeText(this, "Por favor, completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }
        // Transformamos la fecha para que sea parseable en el calendario (yyyy-MM-dd)
        val inputFormat = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val outputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Convertir la fecha de inicio de String a Date
        val fechaInicioDate = inputFormat.parse(fechaInicio)
        // Convertimos la fecha de inicio a Timestamp
        val fechaInicioTimestamp = fechaInicioDate?.let { Timestamp(it) }

        // Aquí procesamos la fecha de fin si está presente
        val fechaFinTimestamp = if (fechaFin.isNotEmpty()) {
            val fechaFinDate = inputFormat.parse(fechaFin)
            fechaFinDate?.let { Timestamp(it) } // se convierte a Timestamp
        } else {
            null // Si no se registra fecha de fin, queda el valor como null
        }


        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Creamos el objeto evento
        val evento = hashMapOf(
            "titulo" to titulo,
            "fechaInicio" to fechaInicioTimestamp,
            "fechaFin" to fechaFinTimestamp,
            "horaInicio" to horaInicio,
            "horaFin" to horaFin,
            "notas" to notas,
            "sticker" to sticker,
            "etiqueta" to etiquetaSeleccionada,
            "userId" to userId
        )

        // Guardamos el evento en Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("eventos")
            .add(evento)
            .addOnSuccessListener {
                Toast.makeText(this, "Evento guardado correctamente", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("AddEventActivity", "Error al guardar evento", e)
            }

    }



}