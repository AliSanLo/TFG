package com.example.tfgdeverdad

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EtiquetaListActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    //vatables globales
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EtiquetaAdapter
    private val listaEtiquetas = mutableListOf<Etiqueta>() // nombre corregido
    private lateinit var drawer: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_etiqueta_list)

        //configuración del recyclerview
        recyclerView = findViewById(R.id.recyclerViewTodasEtiquetas)
        //lo pone en vrtical con LinearLayoutManager
        recyclerView.layoutManager = LinearLayoutManager(this)

        //creamos un adapter con un  callback para eliminar una etiqeuta cuando se pulse en su opcion correspondiente
        adapter = EtiquetaAdapter(this, listaEtiquetas) { etiqueta ->
            eliminarEtiqueta(etiqueta)
        }
        //Conecta el recycler con el adapter
        recyclerView.adapter = adapter

        //Llamamos a la funciones que cargan las etiquetas desde Firebase
        cargarEtiquetasDelUsuario()
        setupDrawerAndToolbar()

    }

// _____________MENU_______________
    //activa el boton del menu lateral  lo sincroniza con el toolbar
    private fun setupDrawerAndToolbar() {
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
            drawer = findViewById(R.id.drawer_layout)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbarEt)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) //quita el titulo por defecto del Toolbar

        //crea el boton de hamburguesa y lo conecta con el Drawer
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.open_drawer, R.string.close_drawer
        )
        //sincroniza el botón con el estado del menú
        drawer.addDrawerListener(toggle)
        toggle.syncState()
    }

    fun callSignOut(view: View) {
        signOut()
    }
    //cierre de sesion desde el menu
    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
        Log.d("EtiquetaListActivity", "Cierre de sesión iniciado")
    }

    //seleccion de opciones del menu
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_item_user -> startActivity(Intent(this, UserActivity::class.java))
            R.id.nav_item_Eventos -> startActivity(Intent(this, EventListActivity::class.java))
            R.id.nav_item_Calendar -> startActivity(Intent(this, MainActivity::class.java))
            R.id.nav_item_signOut -> signOut()
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }
//_____________FIN MENU______________

    //saca el ID dle usuario autnticado
    private fun cargarEtiquetasDelUsuario() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
      //Si no hay usuario avisa y cancela
        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }
        //consulta a Firebase las etiquetas que pertenecen a este usuario
        FirebaseFirestore.getInstance().collection("etiquetas")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                listaEtiquetas.clear() //limpia la lista
                for (doc in result) {//recorre el documento buscando por el ID de etiqueta
                    val etiqueta = doc.toObject(Etiqueta::class.java)
                    val etiquetaConId = etiqueta.copy(etiquetaId = doc.id)
                    listaEtiquetas.add(etiquetaConId)

                }
                //Actualiza el adaptador para que se refresqeu al vista
                adapter.notifyDataSetChanged()
            }

            //si falla la consulta, devuelve el error y lo loguea
            .addOnFailureListener { e ->
                Log.e("EtiquetaListActivity", "Error al cargar etiquetas", e)
                Toast.makeText(this, "Error al cargar etiquetas", Toast.LENGTH_SHORT).show()
            }

    }


    //eliminar etiqutas
    private fun eliminarEtiqueta(etiqueta: Etiqueta) {
        val db = FirebaseFirestore.getInstance()
        val etiquetaId = etiqueta.etiquetaId

        if (etiquetaId.isNullOrEmpty()) {
            Toast.makeText(this, "El ID de la etiqueta no es válido", Toast.LENGTH_SHORT).show()
            return
        }

        //verifica que la etiqueta existe en al base de datos para eliminarla
        db.collection("etiquetas").document(etiquetaId).delete()
            .addOnSuccessListener {
                listaEtiquetas.remove(etiqueta) //se elimina
                adapter.notifyDataSetChanged()//se actualiza la vista
                Toast.makeText(this, "Etiqueta eliminada", Toast.LENGTH_SHORT).show()
            }
            //si falla devuvle el error
            .addOnFailureListener { e ->
                Log.e("EtiquetaListActivity", "Error al eliminar etiqueta", e)
                Toast.makeText(this, "Error al eliminar la etiqueta", Toast.LENGTH_SHORT).show()
            }
    }
}
