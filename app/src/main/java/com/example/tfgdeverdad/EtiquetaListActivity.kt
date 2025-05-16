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
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EtiquetaAdapter
    private val listaEtiquetas = mutableListOf<Etiqueta>() // nombre corregido
    private lateinit var drawer: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_etiqueta_list)

        recyclerView = findViewById(R.id.recyclerViewTodasEtiquetas)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = EtiquetaAdapter(this, listaEtiquetas) { etiqueta ->
            eliminarEtiqueta(etiqueta)
        }
        recyclerView.adapter = adapter

        cargarEtiquetasDelUsuario()
        setupDrawerAndToolbar()

    }

    private fun setupDrawerAndToolbar() {
        drawer = findViewById(R.id.drawer_layout)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbarEt)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.open_drawer, R.string.close_drawer
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()
    }

    fun callSignOut(view: View) {
        signOut()
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
        Log.d("EtiquetaListActivity", "Cierre de sesión iniciado")
    }

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

    private fun cargarEtiquetasDelUsuario() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseFirestore.getInstance().collection("etiquetas")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                listaEtiquetas.clear()
                for (doc in result) {
                    val etiqueta = doc.toObject(Etiqueta::class.java)
                    val etiquetaConId = etiqueta.copy(etiquetaId = doc.id)
                    listaEtiquetas.add(etiquetaConId)

                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("EtiquetaListActivity", "Error al cargar etiquetas", e)
                Toast.makeText(this, "Error al cargar etiquetas", Toast.LENGTH_SHORT).show()
            }

    }

    private fun eliminarEtiqueta(etiqueta: Etiqueta) {
        val db = FirebaseFirestore.getInstance()
        val etiquetaId = etiqueta.etiquetaId

        if (etiquetaId.isNullOrEmpty()) {
            Toast.makeText(this, "El ID de la etiqueta no es válido", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("etiquetas").document(etiquetaId).delete()
            .addOnSuccessListener {
                listaEtiquetas.remove(etiqueta)
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Etiqueta eliminada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("EtiquetaListActivity", "Error al eliminar etiqueta", e)
                Toast.makeText(this, "Error al eliminar la etiqueta", Toast.LENGTH_SHORT).show()
            }
    }
}
