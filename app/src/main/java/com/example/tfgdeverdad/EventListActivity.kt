package com.example.tfgdeverdad

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tfgdeverdad.Event
import com.example.tfgdeverdad.EventAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EventListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EventAdapter
    private val listaEventos = mutableListOf<Event>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_list)

        recyclerView = findViewById(R.id.recyclerViewTodosEventos)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = EventAdapter(listaEventos)
        recyclerView.adapter = adapter

        cargarEventosDelUsuario()
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
                    listaEventos.add(evento)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("TodosEventosActivity", "Error al cargar eventos", e)
                Toast.makeText(this, "Error al cargar eventos", Toast.LENGTH_SHORT).show()
            }
    }
}
