package com.example.tfgdeverdad

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekCalendarView
import com.kizitonwose.calendar.view.WeekDayBinder
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale


// Clase DayViewContainer que actúa como contenedor para cada celda de día en el calendario
class DayViewContainer(view: View) : ViewContainer(view) {
    // Encuentra el TextView dentro de la vista
     val textView: TextView = requireNotNull(view.findViewById(R.id.calendarDayText)) {
    }

    // Alternativa con ViewBinding:
    // val textView = CalendarDayLayoutBinding.bind(view).calendarDayText


}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

//Inicialización de las vis tas
        val calendarView: CalendarView = findViewById(R.id.calendarView)


//para la vista mensual
        val currentMonth = YearMonth.now() //devuelve el mes y año actuales, sin incluir el día.
        val startMonth = currentMonth.minusMonths(24) //Mi calendario comienza 24 meses atrás, es decir, a fecha de hoy en febrero de 2023
        val endMonth = currentMonth.plusMonths(60) // Mi calendario incluirá los 60 meses posteriores a fecha de hoy, es decir, febrero de 2030
        val firstDayOfWeek = firstDayOfWeekFromLocale() // Establece el primer día de la semana segun la configuracion regional del dispositivo.
        calendarView.setup(startMonth, endMonth, firstDayOfWeek) //inicializa el CalendarView según lo establecido
        calendarView.scrollToMonth(currentMonth) //para que muestre por defecto el mes actual.

//para generar los dias de la semana
        val daysOfWeek = daysOfWeek()
         calendarView.setup(startMonth, endMonth, daysOfWeek.first())

        val titlesContainer = findViewById<ViewGroup>(R.id.titlesContainer)
        titlesContainer.children
            .map { it as TextView }
            .forEachIndexed { index, textView ->
                val dayOfWeek = daysOfWeek[index]
                val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                textView.text = title
            }




        // Aquí se configura el dayBinder del CalendarView (explico el dayBinder en una linea?)
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            // Called only when a new container is needed.
            override fun create(view: View) = DayViewContainer(view)

            // Called every time we need to reuse a container.
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.textView.text = data.date.dayOfMonth.toString()
            }
        }



    }
}