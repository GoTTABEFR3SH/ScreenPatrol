package com.cl.screenpatrol

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.screenpatrol.R.*
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import kotlin.math.roundToInt

class AppSpecificScreen : AppCompatActivity() {
    private lateinit var context: Context
    private lateinit var appStats: appStats
    private lateinit var adapt: appSpecificListAdapter
    private lateinit var appName:String

    lateinit var barChart:BarChart
    lateinit var set: BarDataSet
    lateinit var data: BarData
    var entries:MutableList<BarEntry> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(layout.activity_app_specific_screen)
        val intent = intent
        appName = intent.getStringExtra("Name").toString()

        context = this
        appStats = appStats(context)

        barChart = findViewById<BarChart>(R.id.barChartView)
        barstuff()





        val ai: ApplicationInfo = appStats.pm.getApplicationInfo(
            appName!!,
            android.content.pm.PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
        )

        title = appStats.pm.getApplicationLabel(ai)

        adapt = appSpecificListAdapter(appName,appStats, context = context)

        val ref_to = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvSpecificApp)
        ref_to.adapter = adapt
        ref_to.layoutManager = LinearLayoutManager(this)
    }

    fun barstuff(){
        val usage = appStats.getUsageDayByDayInWeek(appName)
        val formatted = appStats.formatMilliseconds(usage.max())
        for(x in 0 until usage.size){
            entries.add(BarEntry(x.toFloat(),usage[x].toFloat().roundToInt().toFloat()))
            Log.d("barStuff", "${usage[x].toFloat().roundToInt().toFloat()}")
        }

        formatData(usage)


    }

    class barValueFormatter() : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return if (value < 3600000) {
                "${((value % 3600000) / 60000).roundToInt()}m"
            }else{
                "${(value / 3600000).roundToInt()}h ${((value % 3600000) / 60000).roundToInt()}m"
            }
        }
    }



    class MyXAxisFormatter : ValueFormatter() {
        private val days = arrayOf("Su","Mo", "Tu", "Wed", "Th", "Fr", "Sa")
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return days.getOrNull(value.toInt()) ?: value.toString()
        }
    }


    fun formatData(usage:MutableList<Long>){
        barChart.xAxis.valueFormatter = MyXAxisFormatter()
        barChart.xAxis.textColor = Color.WHITE
        barChart.axisRight.isEnabled = false
        barChart.axisLeft.isEnabled = false
        barChart.xAxis.setDrawAxisLine(true)
        barChart.xAxis.setDrawGridLines(true)

        set = BarDataSet(entries, "")
        set.valueFormatter = barValueFormatter()
        set.valueTextColor = Color.WHITE
        set.valueTextSize = 10f
        set.color = Color.LTGRAY
        data = BarData(set)
        data.setBarWidth(0.7f); // set custom bar width
        barChart.setData(data);




        barChart.setFitBars(false); // make the x-axis fit exactly all bars
        barChart.invalidate(); // refresh



        Log.d("TITTY", " Label ${barChart.axisRight.labelCount} Axis Max ${barChart.axisLeft.axisMaximum}")
    }
}

