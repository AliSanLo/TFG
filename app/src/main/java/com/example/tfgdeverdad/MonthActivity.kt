package com.example.tfgdeverdad

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.children
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tfgdeverdad.EventAdapter
import com.google.android.material.navigation.NavigationView
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale


// Clase DayViewContainer que actúa como contenedor para cada celda de día en el calendario
class DayViewContainer(view: View) : ViewContainer(view) {
    // Encuentra el TextView dentro de la vista
    val dayText: TextView = view.findViewById(R.id.calendarDayText)
    val textView: TextView = requireNotNull(view.findViewById(R.id.calendarDayText))
    val eventTextView: TextView = requireNotNull(view.findViewById(R.id.eventTextView))
    val eventStickerTextView: TextView = TextView(view.context)

    lateinit var day: CalendarDay

    init {

        view.setOnClickListener {

            val activity = view.context as MainActivity
            val oldDate = activity.selectedDate
            val newDate = day
            val eventTextView: TextView = requireNotNull(view.findViewById(R.id.eventTextView))

            activity.selectedDate?.let { date ->
                activity.updateEventList(date.date) // ¡Aquí se actualiza el RecyclerView!
            }


            // Si ya había una fecha seleccionada, refrescamos esa celda
            if (oldDate != null && oldDate != newDate) {
                activity.findViewById<CalendarView>(R.id.calendarView).notifyDayChanged(oldDate)
            }

            // Actualizamos la fecha seleccionada
            if (oldDate == newDate) {
                // Si haces clic sobre la misma, la deseleccionamos
                activity.selectedDate = null
            } else {
                activity.selectedDate = newDate
            }
            // Mostrar u ocultar el botón de añadir evento
            activity.addButton.visibility = if (activity.selectedDate != null) View.VISIBLE else View.GONE

            // Refrescamos la nueva celda
            activity.findViewById<CalendarView>(R.id.calendarView).notifyDayChanged(newDate)

        }
        // Seteo de sticker como texto
        eventStickerTextView.setTextColor(Color.RED)  // Cambia el color del sticker
        eventStickerTextView.textSize = 14f  // Puedes ajustar el tamaño del sticker
        (view as ViewGroup).addView(eventStickerTextView)
    }
    //var onDayClick: ((CalendarDay) -> Unit)? = null


}

class MainActivity : AppCompatActivity(), OnNavigationItemSelectedListener {

    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    var selectedDate: CalendarDay? = null  //  guarda la fecha seleccionada

    val selectedDates = mutableSetOf<LocalDate>()
    lateinit var addButton: ImageView


    //Personalizamos la clase Event para poder transformar los datos de Firebase y mostrarlos en el calendario
    data class Event(
        val titulo: String = "",
        val fechaInicio: Timestamp? = null,
        val fechaFin: Timestamp? = null,
        val horaInicio: String = "",
        val horaFin: String = "",
        val notas: String = "",
        val sticker: String = ""
        )

    val eventsMap = mutableMapOf<LocalDate, MutableList<Event>>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_month)

        //iniciar menu
        initToolBar()
        initNavigationView()

//Iniciar boton aniadir evento
        addButton = findViewById(R.id.addButton)
        addButton.visibility = View.GONE // Oculto al inicio

        addButton.setOnClickListener {
            val intent = Intent(this, AddEventActivity::class.java)
            startActivity(intent)
        }


//Inicialización de las vistas
        val calendarView: CalendarView = findViewById(R.id.calendarView)


    //para la vista mensual
        val currentMonth = YearMonth.now() // Devuelve el mes y año actuales, sinincluir el dia
        val startMonth = currentMonth.minusMonths(12) // MI calendario comienza 12 meses atras, es decir en 2023
        val endMonth = currentMonth.plusMonths(12) // Incluirá 60 meses posteriores a la fecha de hoy
        val firstDayOfWeek =  firstDayOfWeekFromLocale()// O el que prefieras

        calendarView.post { // Usamos post() para asegurarnos de que el calendario está completamente cargado antes de desplazarlo
            calendarView.scrollToMonth(currentMonth) // Desplazar al mes actual
        }



        //para que aparezca el nombre del mes
        val monthTitle: TextView = findViewById(R.id.monthTitle)

        //Para que se vea le nombre del mes
        calendarView.monthScrollListener = { month ->
            val mes = month.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()).replaceFirstChar { it.uppercase() }
            val anio = month.yearMonth.year
            monthTitle.text = "$mes $anio"
        }

        /* val titlesContainer = findViewById<ViewGroup>(R.id.titlesContainer)
         titlesContainer.children*/ //correccion

        // Accedo al contenedor de los días de la semana dentro de titlesContainer
        val titlesContainer = findViewById<ViewGroup>(R.id.titlesContainer)

        // Verifico que el contenedor 'daysContainer' esté bien referenciado
        val actualContainer = titlesContainer?.findViewById<ViewGroup>(R.id.daysContainer)

        val daysOfWeek = daysOfWeek(DayOfWeek.MONDAY)

        calendarView.setup(startMonth, endMonth, daysOfWeek.first())

        // Me aseguro de que 'actualContainer' no sea nulo y filtra los TextViews
        actualContainer?.children?.filterIsInstance<TextView>() // Asegura que solo seleccionamos TextView
            ?.forEachIndexed { index, textView ->
                val dayOfWeek = daysOfWeek[index]
                val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                textView.text = title
            }


        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser!!.uid  // Sabemos que no es null

        // Después de cargar los eventos desde Firebase
        db.collection("eventos")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                // Itera sobre los documentos y agrega los eventos al mapa según su fecha
                for (document in result) {
                    val evento = document.toObject(Event::class.java)
                    val fechaInicio: LocalDate? = evento.fechaInicio?.toDate()?.toInstant()?.atZone(java.time.ZoneId.systemDefault())?.toLocalDate()

                    if (fechaInicio != null) {
                        eventsMap.getOrPut(fechaInicio) { mutableListOf() }.add(evento)
                    }
                }

                // Asegura que la actualización del calendario se haga después de que el calendario haya sido renderizado
                calendarView.post {
                    eventsMap.keys.forEach { date ->
                        val calendarDay = CalendarDay(date, DayPosition.MonthDate)
                        calendarView.notifyDayChanged(calendarDay)
                    }
                }

                // Aquí llamamos a updateEventList para que los eventos se muestren en la lista
                selectedDate?.let { updateEventList(it.date) }
            }
            .addOnFailureListener { e -> Log.e("MainActivity", "Error cargando eventos", e) }

        // Aquí se configura el dayBinder del CalendarView (explico el dayBinder en una linea?)
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            // Se llama solo cuando un nuevo container es necesario
            override fun create(view: View) = DayViewContainer(view)
            // Se llama cada vez que queremos reutilizar un container
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.textView.text = data.date.dayOfMonth.toString()
                container.day = data
                container.dayText.text = data.date.dayOfMonth.toString()

                val activity = container.view.context as MainActivity
                val events = activity.eventsMap[data.date] ?: emptyList()

                if (data.position == DayPosition.MonthDate) {
                    container.textView.setTextColor(Color.DKGRAY)

                    container.view.background = null
                    container.textView.setBackgroundColor(Color.TRANSPARENT)
                    container.textView.setTextColor(Color.DKGRAY)
                    container.eventTextView.visibility = View.GONE
                    container.eventStickerTextView.visibility = View.GONE

                    when {
                        activity.selectedDate == data -> {
                            container.textView.setBackgroundResource(R.drawable.selected_day_background)
                            container.textView.setTextColor(Color.WHITE)
                        }

                        data.date == LocalDate.now() -> {
                            container.view.setBackgroundResource(R.drawable.border_current_day)
                        }

                        events.isNotEmpty() -> {
                            container.view.background = ContextCompat.getDrawable(this@MainActivity, R.drawable.rounded_background)
                            container.eventStickerTextView.text = events.first().sticker
                            container.eventTextView.text = if (events.size > 1) "${events.first().horaInicio} +" else events.first().horaInicio
                            container.eventTextView.visibility = View.VISIBLE
                            container.eventStickerTextView.visibility = View.VISIBLE
                        }
                    }

                } else {
                    container.textView.setTextColor(Color.GRAY)
                    container.textView.setBackgroundColor(Color.TRANSPARENT)
                    container.view.background = null
                    container.eventTextView.visibility = View.GONE
                    container.eventStickerTextView.visibility = View.GONE
                }
            }


        }


    }


    fun updateEventList(selectedDate: LocalDate) {
        val events = eventsMap[selectedDate] ?: emptyList()
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewEventos)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = EventAdapter(events)
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_item_signOut -> signOut()

        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }



}

class EventAdapter(private val events: List<MainActivity.Event>) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.titleTextView.text = event.titulo
        holder.timeTextView.text = "${event.horaInicio} - ${event.horaFin}"
        holder.notesTextView.text = event.notas
    }

    override fun getItemCount(): Int = events.size

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.eventTitle)
        val timeTextView: TextView = itemView.findViewById(R.id.eventTime)
        val notesTextView: TextView = itemView.findViewById(R.id.eventNotes)
    }
}