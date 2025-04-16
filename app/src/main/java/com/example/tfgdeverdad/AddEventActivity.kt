package com.example.tfgdeverdad

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
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


        val etFechaStart = findViewById<EditText>(R.id.StartDate)
        val etFechaEnd = findViewById<EditText>(R.id.EndDate)

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
}