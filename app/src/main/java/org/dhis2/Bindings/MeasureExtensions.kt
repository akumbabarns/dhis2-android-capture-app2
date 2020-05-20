package org.dhis2.Bindings

import android.content.Context
import android.widget.TextView
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.common.BaseNameableObject
import org.hisp.dhis.android.core.dataelement.DataElement
import kotlin.math.max

fun List<BaseNameableObject>.toDisplayNameList(): List<String> {
    return map { it.displayName() ?: it.uid() }
}

fun List<DataElement>.maxLengthLabel(): String {
    return toDisplayNameList().maxBy {
        it.length
    } ?: ""
}

fun List<DataElement>.measureText(context: Context, widthFactor: Int): Triple<String, Int, Int> {
    return toDisplayNameList()
        .calculateWidth(context)
        .calculateHeight(context, widthFactor)
}

fun List<List<CategoryOption>>.measureText(
    context: Context,
    minWidth: Int? = 300
): List<List<Int>> {
    var prevMeasures = emptyList<Int>()
    val recMeasures = mutableListOf<List<Int>>()
    return map { options ->
        val (measures, recalculatedPrevMeasures) = options.toDisplayNameList()
            .calculateEachWidth(context, prevMeasures, minWidth)
        prevMeasures = measures
        if (recalculatedPrevMeasures.isNotEmpty()) {
            recMeasures.add(recalculatedPrevMeasures)
        }
        measures
    }.mapIndexed { index, list ->
        if (index < recMeasures.size) {
            recMeasures[index]
        } else {
            list
        }
    }
}

fun List<String>.calculateWidth(context: Context): Pair<String, Int> {
    var maxLabel = ""
    var minWidth = 0
    forEach { label ->
        TextView(context).apply {
            text = label
            measure(0, 0)
            if (measuredWidth > minWidth) {
                maxLabel = label
                minWidth = measuredWidth
            }
        }
    }
    return Pair(maxLabel, minWidth)
}

fun List<String>.calculateEachWidth(
    context: Context,
    prevMeasures: List<Int>,
    requestedMinWidth: Int? = 300
): Pair<List<Int>, List<Int>> {
    val hasTotal = this.contains("") || this.contains("Total")
    val calculatedWidths = map { label ->
        val calculatedWidth = TextView(context).apply {
            text = label
            measure(0, 0)
            minWidth = measuredWidth
        }.measuredWidth
        max(calculatedWidth, requestedMinWidth!!)
    }
    if (prevMeasures.isEmpty()) {
        return Pair(calculatedWidths, prevMeasures)
    }
    val uniqueOptionNumber = if (!hasTotal) {
        size / prevMeasures.size
    } else {
        (size - 1) / (prevMeasures.size - 1)
    }

    var recalculatedWidths = calculatedWidths
    val recalculatedPrevMeasures = prevMeasures.mapIndexed { prevIndex, width ->
        val totalWidth = if(hasTotal && prevIndex*uniqueOptionNumber == calculatedWidths.size-1){
            width
        }else {
            calculatedWidths.subList(
                prevIndex * uniqueOptionNumber,
                prevIndex * uniqueOptionNumber + uniqueOptionNumber
            ).sum()
        }
        if (width < totalWidth) {
            totalWidth
        } else if (width > totalWidth) {
            recalculatedWidths = recalculatedWidths.mapIndexed { index, currentWidth ->
                if (index >= prevIndex && index < prevIndex + uniqueOptionNumber) {
                    currentWidth + width / uniqueOptionNumber
                } else {
                    currentWidth
                }
            }
            width
        } else {
            width
        }
    }

    return Pair(recalculatedWidths, recalculatedPrevMeasures)
}

fun Pair<String, Int>.calculateHeight(
    context: Context,
    widthFactor: Int
): Triple<String, Int, Int> {
    var minHeight = 0
    val minWidth: Int
    val currentWidth = context.resources.displayMetrics.widthPixels
    if (second > currentWidth / widthFactor) {
        minWidth = currentWidth / widthFactor
        TextView(context).apply {
            width = minWidth
            text = first
            measure(0, 0)
            minHeight = measuredHeight
        }
    } else {
        minWidth = second
    }

    return Triple(first, minWidth, minHeight)
}

fun Pair<String, Int>.calculateHeight(
    context: Context
): Triple<String, Int, Int> {
    var minHeight = 0
    TextView(context).apply {
        width = second
        text = first
        measure(0, 0)
        minHeight = measuredHeight
    }

    return Triple(first, second, minHeight)
}