package com.example.tfgdeverdad

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import com.example.tfgdeverdad.Event
import com.example.tfgdeverdad.EventAdapter
import com.firebase.ui.auth.data.model.User
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

    lateinit var day: CalendarDay //día actual qeu representa este contenedor

    init {
        //Al hacer click en el día, actualizamos seleccion y refrescamos vistas
        view.setOnClickListener {

            val activity = view.context as MainActivity
            val oldDate = activity.selectedDate
            val newDate = day
            val eventTextView: TextView = requireNotNull(view.findViewById(R.id.eventTextView))

            // Si ya había una fecha seleccionada, refrescamos esa celda
            if (oldDate != null && oldDate != newDate) {
                activity.findViewById<CalendarView>(R.id.calendarView).notifyDayChanged(oldDate)
            }

            // Actualizamos la fecha seleccionada
            if (oldDate == newDate) {
                // Si haces clic sobre la misma fecha, la deseleccionamos
                activity.selectedDate = null
            } else {
                activity.selectedDate = newDate
            }
            // Mostrar u ocultar el botón de añadir evento
            activity.addButton.visibility = if (activity.selectedDate != null) View.VISIBLE else View.GONE

            // Refrescamos la nueva celda para actualizar su apariencia
            activity.findViewById<CalendarView>(R.id.calendarView).notifyDayChanged(newDate)
        }
        // configuro el sticker del evento: color y tamaño
        eventStickerTextView.setTextColor(Color.RED)
        eventStickerTextView.textSize = 14f
        (view as ViewGroup).addView(eventStickerTextView) //casteo de view a ViewFGroup para poder añadir una vista hija al ViewGroup
    }

}

class MainActivity : AppCompatActivity(), OnNavigationItemSelectedListener {

    private lateinit var drawer: DrawerLayout
    var selectedDate: CalendarDay? = null  // fecha seleccionada
    private lateinit var toggle: ActionBarDrawerToggle

    lateinit var addButton: ImageView //Boton para añadir eventos, se muestra sólo si hay fecha seleccionada
    lateinit var tagButton: ImageView //Botón para añadir etiqueta

    //Mapa para guardar eventos según su fecha
    val eventsMap = mutableMapOf<LocalDate, MutableList<Event>>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_month)

        //iniciar menu lateral y la barra superior
        initToolBar()
        initNavigationView()


        //_______BOTONES_________

        //Boton añdir evento, oculto al prrincipio porque no hay día seleccionado
        addButton = findViewById(R.id.addButton)
        addButton.visibility = View.GONE // Oculto al inicio

        //Al pulsar abre la actividad para añadir un nuevo evento
        addButton.setOnClickListener {
            val intent = Intent(this, AddEventActivity::class.java)
            startActivity(intent)
        }
        //Botón para vertodos los eventos
        val btnVerTodos = findViewById<TextView>(R.id.btnVerTodosEventos)
        btnVerTodos.setOnClickListener {
            val intent = Intent(this, EventListActivity::class.java)
            startActivity(intent)
        }

        //boton añdir etiqueta
       tagButton = findViewById(R.id.addTag)
        tagButton.setOnClickListener {
            val intent = Intent(this, AddEtiquetaActivity::class.java)
            startActivity(intent)
        }

    // Boton listar etiquetas
        val listTagButton = findViewById<ImageView>(R.id.listTag)
        listTagButton.setOnClickListener {
            val intent = Intent(this, EtiquetaListActivity::class.java)
            startActivity(intent)
        }

    //Inicialización de las vistas
        val calendarView: CalendarView = findViewById(R.id.calendarView)

//_________FIN BOTONES___________


//______GESTIÓN CALENDARIO________

    //para la vista mensual.
        val currentMonth = YearMonth.now() // Devuelve el mes y año actuales, sinincluir el dia
        val startMonth = currentMonth.minusMonths(12) // MI calendario comienza 12 meses atras, es decir en 2024
        val endMonth = currentMonth.plusMonths(12) // Incluirá 60 meses posteriores a la fecha de hoy
        val firstDayOfWeek =  firstDayOfWeekFromLocale()//

        calendarView.post { // Usamos post() para asegurarnos de que el calendario está completamente cargado antes de desplazarlo
            calendarView.scrollToMonth(currentMonth) // Desplazar al mes actual
        }

        //para que aparezca el nombre del mes visible en la are superior de la pantalla
        val monthTitle: TextView = findViewById(R.id.monthTitle)


        //Para que se vea el nombre del mes
        calendarView.monthScrollListener = { month -> //función lambda. Se dispara cada vvez que se hace scroll y cambia de mes. Month es el nuevo mes que se muestra

            /*me da el mes como un enum y lo convierte a un nombre legible con getDisplayName,
            donde pide el nombre entero "enero" y no "ene", y usa el nombre del dispositivo
            para traducirlo. Pone la primera letra en mayuscula*/
            val mes = month.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()).replaceFirstChar { it.uppercase() }
            val anio = month.yearMonth.year
            monthTitle.text = "$mes $anio"
        }

        // Accedo al contenedor de los días de la semana dentro de titlesContainer
        val titlesContainer = findViewById<ViewGroup>(R.id.titlesContainer)

        // Verifico que el contenedor 'daysContainer' esté bien referenciado
        val actualContainer = titlesContainer?.findViewById<ViewGroup>(R.id.daysContainer)

        val daysOfWeek = daysOfWeek(DayOfWeek.MONDAY)

    //configura el calendario con los valores establecidos anteriormente
        calendarView.setup(startMonth, endMonth, daysOfWeek.first())

        //Me quedo con los TextViews que representan los dias de la semana
        // Me aseguro de que 'actualContainer' no sea nulo.
        // Accedse a todos los hijos de esta vista, los filtra y devuelve solo los de tipo TextView
        actualContainer?.children?.filterIsInstance<TextView>()
            ?.forEachIndexed { index, textView -> //recorre casda text view,devuelve el index para relacionarlo con el día
                val dayOfWeek = daysOfWeek[index]
                val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()) //convierte el enum DayofWeek a su nombre en formato corto en local
                textView.text = title //atribuimos ese texto al TextView correspondiente
            }

//_________FIN GESTION CALENDARIO__________

//_______BASE DE DATOS__________

        //Cargamoslos eventos guardados en Firestore para el usuario actual
        val db = FirebaseFirestore.getInstance()
        //accede al usuario actualmente logueado
        val userId = FirebaseAuth.getInstance().currentUser!!.uid  // Sabemos que no es null

        // Después de cargar los eventos desde Firebase
        db.collection("eventos")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result -> //result sera una lista de documentos que cumplen con el filtro
                for (document in result) { //Itera sobre los documentos y agrega los eventos al mapa según su fecha
                    val evento = document.toObject(Event::class.java)
                    //convertimos los datos de firebase al formato necesario
                    val fechaInicio: LocalDate? = evento.fechaInicio?.toDate()?.toInstant()?.atZone(java.time.ZoneId.systemDefault())?.toLocalDate()

                    //si la fecha es valida mete el evento en el mapa agrupado por fecha.
                    if (fechaInicio != null) {
                        eventsMap.getOrPut(fechaInicio) { mutableListOf() }.add(evento)
                    }
                }

                // Asegura que la actualización del calendario se haga después de que el calendario haya sido renderizado
                calendarView.post { //Espera a que el calendario esté renderizado en pantalla antes de actualzarlo
                    eventsMap.keys.forEach { date -> //recorre todasa las fechas donde hay eventos
                        val calendarDay = CalendarDay(date, DayPosition.MonthDate)//Crea un objeto para esa fecha en e calendario
                        calendarView.notifyDayChanged(calendarDay)//notifica los cambios al calendario para que lo actualice
                    }
                }
            }
            .addOnFailureListener { e -> Log.e("MainActivity", "Error cargando eventos", e) }
//____________FIN BASE DE DATOS______________


//___________DAY BINDER______________
        // Aquí se configura el dayBinder del CalendarView (explico el dayBinder en una linea?)
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {

            // Se llama solo cuando un nuevo container es necesario
            override fun create(view: View) = DayViewContainer(view)

            // Se llama cada vez que queremos reutilizar un container
            //Rellena o reutiliza la vista de un día en el calendario
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.textView.text = data.date.dayOfMonth.toString()//muestra el numerodel dia
                container.day = data//guarda data por si se necesita luego
                container.dayText.text = data.date.dayOfMonth.toString() //actualiza el texto principal

                val activity = container.view.context as MainActivity // se castea el contexr para acceder a MainActivty
                val events = activity.eventsMap[data.date] ?: emptyList() //busca si hay eventos para esa fecha en el eventsMap.

                //si el día pertenece al mes actualmente visible
                if (data.position == DayPosition.MonthDate) {
                    container.textView.setTextColor(Color.DKGRAY)
                    //Configuración de día dentro del mes visible
                    container.view.background = null
                    container.textView.setBackgroundColor(Color.TRANSPARENT)
                    container.textView.setTextColor(Color.DKGRAY)
                    container.eventTextView.visibility = View.GONE
                    container.eventStickerTextView.visibility = View.GONE

                    when {
                        activity.selectedDate == data -> {
                           //Dia seleccionado: fondo de color y letras en blanco
                            container.textView.setBackgroundResource(R.drawable.selected_day_background)
                            container.textView.setTextColor(Color.WHITE)
                        }

                        events.isNotEmpty() && data.date == LocalDate.now() -> {
                           //Día con eventos tiene un fondo distinto y muestra el sticker + hora
                            container.view.setBackgroundResource(R.drawable.layer_current_day_event)
                            container.eventStickerTextView.text = events.first().sticker
                            container.eventTextView.text = if (events.size > 1) "${events.first().horaInicio} +" else events.first().horaInicio
                            container.eventTextView.visibility = View.VISIBLE
                            container.eventStickerTextView.visibility = View.VISIBLE
                        }

                        events.isNotEmpty() -> {
                            //Día con evento con fondo redondeado y mostrando detalles
                            container.view.background = ContextCompat.getDrawable(this@MainActivity, R.drawable.rounded_background)
                            container.eventStickerTextView.text = events.first().sticker
                            container.eventTextView.text = if (events.size > 1) "${events.first().horaInicio} +" else events.first().horaInicio
                            container.eventTextView.visibility = View.VISIBLE
                            container.eventStickerTextView.visibility = View.VISIBLE
                        }

                        data.date == LocalDate.now() -> {
                           //Fecha actual tiene un borde especial para situarnos en le calendario
                            container.view.setBackgroundResource(R.drawable.border_current_day)
                        }
                    }

                } else {
                    //Días que no pertenecen al mes visible con el texto en gria y sin fondo
                    container.textView.setTextColor(Color.GRAY)
                    container.textView.setBackgroundColor(Color.TRANSPARENT)
                    container.view.background = null
                    container.eventTextView.visibility = View.GONE
                    container.eventStickerTextView.visibility = View.GONE
                }
            }
        }

//___________FIN DAY BINDER_______________

    }


//____________MENU______________

    //Configuracion de la toolbar con el botón par abrir y cerrar el menú lateral
    private fun initToolBar() {
        //busca el Toolbar desde layoyut y o convierte en la barra de acciones de la activity
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout) //metemos el DrawerLayout de Activity_month, controlando asi el elemento raiz del activity_month (el menu)
        val toggle = ActionBarDrawerToggle( //Aqui creamos una palanca con la que decimos que en este activity (this), tenemos este layout(drawer),
                // con este toolbar (toolbar), y de inicio quierp que se vea la frase bar_title y para cerrarlo muestra la frase de navigation_drawer_close
                this, drawer, toolbar, R.string.empty, R.string.empty
            )

        //hace que el DrawerLayout escuche al toggle.
        drawer.addDrawerListener(toggle)
        toggle.syncState() //al abrir el menu se actualiza la animación y el estado
    }

//Configura los listener para los clicks en menu
private fun initNavigationView() {
        var navigationView: NavigationView = findViewById(R.id.nav_view) //conectamos con el laytout correspondiente por el id
        navigationView.setNavigationItemSelectedListener(this) //para reconocer cuando se está haciendo click en un elemento del menu y generar acciones

        var headerView: View = LayoutInflater.from(this).inflate(R.layout.nav_header_main, navigationView, false)
    }

    //Funcion para cerrar sesión
    fun callSignOut(view: View) {
        signOut()
    }
    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
        Log.d("MainActivity", "Cierre de sesión iniciado")

    }

    //Configuracion del menu desplegable
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

//________________FIN MENU_________________



//Carga los eventos guardados en Firestore y los mete en un map
    fun cargarEventosDesdeFirestore() {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        eventsMap.clear()  // Muy importante: limpiar antes para evitar duplicados
        db.collection("eventos")
            .whereEqualTo("userId", userId) //pide todos los eventos cuyo userId sea el mismo que el del usuario actual
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val evento = document.toObject(Event::class.java) //convierte cada documentp en objeto Event
                    val fechaInicio = evento.fechaInicio?.toDate()
                        ?.toInstant()?.atZone(java.time.ZoneId.systemDefault())?.toLocalDate() //Convierte las fechas de Firebase a Localdate
                    if (fechaInicio != null) {
                        eventsMap.getOrPut(fechaInicio) { mutableListOf() }.add(evento)
                    }
                }

                // Refrescar calendario al llamar a notifyDayChanged() para mostrar en el calendario los nuevos eventos
                findViewById<CalendarView>(R.id.calendarView).post {  //aasefura qu el codigo se ejecute después de que el calendario se haya renderizado.
                    eventsMap.keys.forEach { date ->
                        val calendarDay = CalendarDay(date, DayPosition.MonthDate)
                        findViewById<CalendarView>(R.id.calendarView).notifyDayChanged(calendarDay) //por cada fecha con eventos se ejecuta esta linea para redibujar ese día
                    }
                }

            }
    }

    //Llama a cargarEventos deFirestore() para cada vez que vuelves a esta activity se refresquen los datos
    override fun onResume() {
        super.onResume()
        cargarEventosDesdeFirestore()  // ¡Reload al volver de EditEventActivity!
    }

    //Para el menu. Maneja el click en el icono de la hamburguesa
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) { //si toggle lo reconoce, lo maneja
            return true
        }
        return super.onOptionsItemSelected(item)// si no lo reconoce lo pasa al sistema por si hay otros items de menu en la toolbar
    }

    //Devuelve el código HEX de un color. Tiene valores por defecto para evitar crasheos
    fun getColorHex(nombreColor: String): String {
        return when (nombreColor.lowercase()) {
            "rojo" -> "#FF0000"
            "verde" -> "#00FF00"
            "azul" -> "#0000FF"
            "amarillo" -> "#FFFF00"
            "morado" -> "#800080"
            else -> "#757575"
        }
    }

}

//Encargada de mostrar cada Evento
class EventAdapter(private val context: Context, private val events: List<Event>, private val onDeleteClick: (Event) -> Unit) :

    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    //Infla el layout del evento
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    //Se asignan los datos del evento de esa posición a los elementos de la vista
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {

        val event = events[position]

        holder.titleTextView.text = event.titulo
        holder.timeTextView.text = "${event.horaInicio} - ${event.horaFin}"
        holder.notesTextView.text = event.notas

        //muestra titulo, horas y notas del evento
        val fechaFormateada = event.fechaInicio?.toDate()?.let {
            val formato = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            formato.format(it)
        } ?: "Sin fecha"
        //convierte la fecha a texto legible
        holder.dateTextView.text = fechaFormateada

        //muestra el sticker
        holder.stickerTextView.text = event.sticker ?: "📌" // sticker por defecto

        // Configurar el botón de eliminar
        holder.btnEliminar.setOnClickListener {
            onDeleteClick(event)
        }

        // Configurar el botón de editar
        holder.btnEditar.setOnClickListener {
            if (event.eventId != null) {
                val intent = Intent(context, EditEventActivity::class.java)
                intent.putExtra("event_id", event.eventId)  // Pasa el id del evento
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Error: ID de evento no disponible", Toast.LENGTH_SHORT).show()
            }
        }

    }

    //Dice cuántos items hay que mostrar
    override fun getItemCount(): Int = events.size

    //Guarda referencias a las vistas del item_event para no tener que usar findViewById todo el rato
    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.eventTitle)
        val timeTextView: TextView = itemView.findViewById(R.id.eventTime)
        val notesTextView: TextView = itemView.findViewById(R.id.eventNotes)
        val dateTextView: TextView = itemView.findViewById(R.id.eventDate)
        val stickerTextView: TextView = itemView.findViewById(R.id.eventSticker)
        val btnEliminar: ImageView = itemView.findViewById(R.id.deleteEvent)
        val btnEditar: ImageView = itemView.findViewById(R.id.editEvent)

    }
}
fun getColorHex(nombreColor: String): String {
    return when (nombreColor.lowercase()) {
        "rojo" -> "#FF0000"
        "verde" -> "#00FF00"
        "azul" -> "#0000FF"
        "amarillo" -> "#FFFF00"
        "morado" -> "#800080"
        else -> "#757575"
    }
}

// Se encarga de mostrar las etiquetas
class EtiquetaAdapter(
    private val context: Context,
    private val etiquetas: List<Etiqueta>,
    private val onDeleteClick: (Etiqueta) -> Unit
) : RecyclerView.Adapter<EtiquetaAdapter.EtiquetaViewHolder>() {

    //Infla el layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EtiquetaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tag, parent, false)
        return EtiquetaViewHolder(view)
    }

    //Configura la apriencia de las etiquetas
    override fun onBindViewHolder(holder: EtiquetaViewHolder, position: Int) {
        val etiqueta = etiquetas[position]

        holder.tituloTextView.text = etiqueta.titulo
        holder.stickerTextView.text = etiqueta.sticker

        //  Si el color no es válido, log de error
        val colorHex = getColorHex(etiqueta.color ?: "") // Usamos la función externa para obtener color
        try {
            holder.colorView.setBackgroundColor(Color.parseColor(colorHex))
        } catch (e: IllegalArgumentException) {
            Log.e("EtiquetaAdapter", "Color inválido: ${etiqueta.color}", e)
            holder.colorView.setBackgroundColor(Color.GRAY) // Color de respaldo
        }

        holder.prioridadTextView.text = etiqueta.prioridad.toString()

        holder.btnEliminar.setOnClickListener {
            Log.d("EtiquetaAdapter", "Click en eliminar: ${etiqueta.titulo}, ID: ${etiqueta.etiquetaId}")
            onDeleteClick(etiqueta)
        }
    }

    override fun getItemCount(): Int = etiquetas.size

    class EtiquetaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tituloTextView: TextView = itemView.findViewById(R.id.tagTitle)
        val stickerTextView: TextView = itemView.findViewById(R.id.tagSticker)
        val colorView: View = itemView.findViewById(R.id.tagColor)
        val prioridadTextView: TextView = itemView.findViewById(R.id.tagPrioridad)
        val btnEliminar: ImageView = itemView.findViewById(R.id.deleteTag)
    }
}