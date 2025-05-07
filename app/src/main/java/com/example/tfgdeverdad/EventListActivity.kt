package com.example.tfgdeverdad

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tfgdeverdad.Event
import com.example.tfgdeverdad.EventAdapter
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EventListActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EventAdapter
    private val listaEventos = mutableListOf<Event>()
    private lateinit var drawer: DrawerLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_list)

        recyclerView = findViewById(R.id.recyclerViewTodosEventos)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = EventAdapter(this, listaEventos) { evento ->
            eliminarEvento(evento)
        }
        recyclerView.adapter = adapter

        cargarEventosDelUsuario()
        initNavigationView()





        // Configuración del ícono de hamburguesa
        // Obtén las referencias del DrawerLayout y la Toolbar
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)

        // Configura la Toolbar como la ActionBar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Configura el ActionBarDrawerToggle para controlar el ícono de hamburguesa
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState() // Sincroniza el estado del ícono de hamburguesa



    }

    private fun initToolBar() {
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        drawer =
            findViewById(R.id.drawer_layout) //metemos el DrawerLayout de Activity_month, controlando asi el elemento raiz del activity_month (el menu)
        val toggle =
            ActionBarDrawerToggle( //Aqui creamos una palanca con la que decimos que en este activity (this), tenemos este layout(drawer),
                // con este toolbar (toolbar), y de inicio quierp que se vea la frase bar_title y para cerrarlo muestra la frase de navigation_drawer_close
                this, drawer, toolbar, R.string.empty, R.string.empty
            )

        drawer.addDrawerListener(toggle)
        toggle.syncState()

    }


    //Quitar la siguiente funcion si no hago cabecera de menu
    private fun initNavigationView() {
        var navigationView: NavigationView =
            findViewById(R.id.nav_view) //conectamos con el laytout correspondiente por el id
        navigationView.setNavigationItemSelectedListener(this) //para reconocer cuando se está haciendo click en un elemento del menu y generar acciones

        var headerView: View = LayoutInflater.from(this).inflate(
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

    //Menu desplegable
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_item_user -> {
                val intent = Intent (this, UserActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_item_Eventos -> {
                val intent = Intent(this, EventListActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_item_Calendar -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_item_signOut -> signOut()
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }




    private fun cargarEventosDelUsuario() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseFirestore.getInstance().collection("eventos")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                listaEventos.clear()
                for (doc in result) {
                    val evento = doc.toObject(Event::class.java)
                    evento.eventId = doc.id // ✅ aquí le pasas el ID del documento de Firestore
                    listaEventos.add(evento)
                }

                // Ordena los eventos por fecha de inicio (de más antiguos a más nuevos)
                listaEventos.sortWith(compareBy { it.fechaInicio })
                
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("TodosEventosActivity", "Error al cargar eventos", e)
                Toast.makeText(this, "Error al cargar eventos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun eliminarEvento(evento: Event) {
        val db = FirebaseFirestore.getInstance()

        val eventId = evento.eventId
        if (eventId.isNullOrEmpty()) {
            Toast.makeText(this, "El ID del evento no es válido", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("eventos").document(eventId).delete()
            .addOnSuccessListener {
                listaEventos.remove(evento)
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Evento eliminado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("EventListActivity", "Error al eliminar evento", e)
                Toast.makeText(this, "Error al eliminar el evento", Toast.LENGTH_SHORT).show()
            }
    }


}
