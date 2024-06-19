package party.para.plugins

import org.ktorm.entity.EntitySequence
import org.ktorm.schema.BaseTable

fun <E : Any, T : BaseTable<E>> EntitySequence<E, T>.findAll(predicate: (E) -> Boolean): List<E> {
    val resultList = mutableListOf<E>()
    for (item in this) {
        if (predicate(item)) {
            resultList.add(item)
        }
    }
    return resultList
}