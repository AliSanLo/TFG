import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.tfg.R
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var eventInput: EditText
    private lateinit var addEventButton: Button
    private lateinit var selectedDateText: TextView
    private lateinit var eventListView: ListView

    private val eventsMap = HashMap<String, MutableList<String>>()
    private var selectedDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar vistas
        calendarView = findViewById(R.id.calendar_view)
        addEventButton = findViewById(R.id.add_event_button)
        selectedDateText = findViewById(R.id.selected_date)
        eventListView = findViewById(R.id.event_list)

        // Formato para la fecha seleccionada
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Manejar selección de fecha en el calendario
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = dateFormat.format(Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }.time)
            selectedDateText.text = "Eventos para: $selectedDate"
            updateEventList()
        }

        // Agregar eventos
        addEventButton.setOnClickListener {
            val event = eventInput.text.toString()
            if (event.isNotEmpty() && selectedDate.isNotEmpty()) {
                val events = eventsMap[selectedDate] ?: mutableListOf()
                events.add(event)
                eventsMap[selectedDate] = events
                eventInput.text.clear()
                updateEventList()
            } else {
                Toast.makeText(this, "Selecciona una fecha y escribe un evento", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Actualizar la lista de eventos para la fecha seleccionada
    private fun updateEventList() {
        val events = eventsMap[selectedDate] ?: mutableListOf()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, events)
        eventListView.adapter = adapter
    }
}
