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
        adapter = EventAdapter(this, listaEventos) { evento ->
            eliminarEvento(evento)
        }
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
                    evento.eventId = doc.id // ✅ aquí le pasas el ID del documento de Firestore
                    listaEventos.add(evento)
                }
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


