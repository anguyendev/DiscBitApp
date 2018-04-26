package com.anguyendev.discbit.models

import com.jjoe64.graphview.series.DataPointInterface

data class GraphDataPoint(private val xPoint: Double,
                          private val yPoint: Double): DataPointInterface {
    override fun getX() = xPoint
    override fun getY() = yPoint
}