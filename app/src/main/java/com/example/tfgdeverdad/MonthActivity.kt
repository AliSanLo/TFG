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
import androidx.core.view.GravityCompat
import androidx.core.view.children
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
import com.google.firebase.auth.FirebaseAuth
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale


// Clase DayViewContainer que actúa como contenedor para cada celda de día en el calendario
class DayViewContainer(view: View) : ViewContainer(view) {
    // Encuentra el TextView dentro de la vista
    val textView: TextView = requireNotNull(view.findViewById(R.id.calendarDayText)) {
    }
    lateinit var day: CalendarDay

    init {
        view.setOnClickListener {

            val activity = view.context as MainActivity

            val oldDate = activity.selectedDate
            val newDate = day

            // Si ya había una fecha seleccionada, refrescamos esa celda
            if (oldDate != null && oldDate != newDate) {
                activity.findViewById<CalendarView>(R.id.calendarView).notifyDayChanged(oldDate)
            }

            // Actualizamos la fecha seleccionada
            if (oldDate == newDate) {
                // Si haces clic sobre la misma, la deseleccionamos (opcional)
                activity.selectedDate = null
            } else {
                activity.selectedDate = newDate
            }
            // Mostrar u ocultar el botón de añadir evento
            if (activity.selectedDate != null) {
                activity.addButton.visibility = View.VISIBLE
            } else {
                activity.addButton.visibility = View.GONE

            }


            // Refrescamos la nueva celda
            activity.findViewById<CalendarView>(R.id.calendarView).notifyDayChanged(newDate)

        }
        // Alternativa con ViewBinding:
        // val textView = CalendarDayLayoutBinding.bind(view).calendarDayText
    }
    //var onDayClick: ((CalendarDay) -> Unit)? = null

}

class MainActivity : AppCompatActivity(), OnNavigationItemSelectedListener {

    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    var selectedDate: CalendarDay? = null  //  guarda la fecha seleccionada

    val selectedDates = mutableSetOf<LocalDate>()
    lateinit var addButton: ImageView




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


//Inicialización de las vistas
        val calendarView: CalendarView = findViewById(R.id.calendarView)


//para la vista mensual
        val currentMonth = YearMonth.now() //devuelve el mes y año actuales, sin incluir el día.
        val startMonth =
            currentMonth.minusMonths(24) //Mi calendario comienza 24 meses atrás, es decir, a fecha de hoy en febrero de 2023
        val endMonth =
            currentMonth.plusMonths(60) // Mi calendario incluirá los 60 meses posteriores a fecha de hoy, es decir, febrero de 2030
        val firstDayOfWeek =
            firstDayOfWeekFromLocale() // Establece el primer día de la semana segun la configuracion regional del dispositivo.
        calendarView.setup(
            startMonth,
            endMonth,
            firstDayOfWeek
        ) //inicializa el CalendarView según lo establecido
        calendarView.scrollToMonth(currentMonth) //para que muestre por defecto el mes actual.

//para generar los dias de la semana
        val daysOfWeek = daysOfWeek()
        calendarView.setup(startMonth, endMonth, daysOfWeek.first())

        /* val titlesContainer = findViewById<ViewGroup>(R.id.titlesContainer)
         titlesContainer.children*/ //correccion

        // Accedo al contenedor de los días de la semana dentro de titlesContainer
        val titlesContainer = findViewById<ViewGroup>(R.id.titlesContainer)

        // Verifico que el contenedor 'daysContainer' esté bien referenciado
        val actualContainer = titlesContainer?.findViewById<ViewGroup>(R.id.daysContainer)

        // Me aseguro de que 'actualContainer' no sea nulo y filtra los TextViews
        actualContainer?.children?.filterIsInstance<TextView>() // Asegura que solo seleccionamos TextView
            ?.forEachIndexed { index, textView ->
                val dayOfWeek = daysOfWeek[index]
                val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                textView.text = title
            }


        // Aquí se configura el dayBinder del CalendarView (explico el dayBinder en una linea?)
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            // Se llama solo cuando un nuevo container es necesario
            override fun create(view: View) = DayViewContainer(view)

            // Se llama cada vez que queremos reutilizar un container
            override fun bind(container: DayViewContainer, data: com.kizitonwose.calendar.core.CalendarDay) {

                container.textView.text = data.date.dayOfMonth.toString()
                container.day = data

                if (data.position == DayPosition.MonthDate) {
                    container.textView.setTextColor(Color.DKGRAY)

                    val activity = container.view.context as MainActivity
                    if (activity.selectedDate == data) {
                        container.textView.setBackgroundColor(Color.LTGRAY)
                    } else {
                        container.textView.setBackgroundColor(Color.TRANSPARENT)
                    }

                } else {
                    container.textView.setTextColor(Color.GRAY)
                    container.textView.setBackgroundColor(Color.TRANSPARENT)
                }
            }

        }



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