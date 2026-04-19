package com.example.micerdito.ui.decorators

import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan

/**
 * DECORADOR - EventDecorator
 * Actúa como un modificador visual para el componente del calendario.
 * Se encarga de dibujar un indicador circular debajo de las fechas que
 * contienen transacciones o eventos registrados.
 *
 * @param color Valor entero que representa el color del indicador visual.
 * @param dates Colección de fechas específicas que requieren ser marcadas.
 */
class EventDecorator(private val color: Int, dates: Collection<CalendarDay>) : DayViewDecorator {

    // Conversión de la colección a una tabla hash para garantizar búsquedas de alto rendimiento
    private val dates: HashSet<CalendarDay> = HashSet(dates)

    /**
     * Determina si una fecha específica del calendario coincide con nuestra lista de eventos.
     * Esta función es llamada internamente por la vista del calendario al renderizar cada día.
     */
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day)
    }

    /**
     * Aplica la modificación visual a la celda del calendario.
     * Inserta un punto con un radio de 10 píxeles utilizando el color especificado.
     */
    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(10f, color))
    }
}