package com.example.micerdito.ui.decorators

import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan

/**
 * DECORADOR - EventDecorator:
 * Pinta un punto de color (DotSpan) debajo del número del día en el calendario.
 * @param color Color del punto (Ej: Color.RED o Color.BLUE).
 * @param dates Colección de días (CalendarDay) que deben ser marcados.
 */
class EventDecorator(private val color: Int, dates: Collection<CalendarDay>) : DayViewDecorator {

    private val dates: HashSet<CalendarDay> = HashSet(dates)

    // Comprueba si el día que está dibujando el calendario está en nuestra lista
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day)
    }

    // Si está en la lista, le añade el punto (Radio de 8f, puedes ajustarlo)
    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(10f, color))
    }
}