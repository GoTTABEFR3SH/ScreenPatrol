package com.cl.screenpatrol

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.NumberPicker
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.screenpatrol.R.layout
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.math.roundToInt

class AppSpecificScreen : AppCompatActivity() {
    private val CHANNEL_ID = "channel_id_01"
    private val notificationId = 69
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
        createNotificationChannel()
        setContentView(layout.activity_app_specific_screen)
        val intent = intent
        appName = intent.getStringExtra("Name").toString()

        context = this
        appStats = appStats(context)

        barChart = findViewById<BarChart>(R.id.barChartView)
        barstuff()


        val removeLimitButton = findViewById<Button>(R.id.RemoveLimit_bttn)
        removeLimitButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO){
                val db = AppDataBase.getInstance(context)
                val dao = db.usageDao()
                val usageStat = dao.getUsageStat(appName)
                if(usageStat != null){
                if (usageStat.limit == true){
                    usageStat.limit = false
                    dao.update(usageStat)
                   Handler(Looper.getMainLooper()).post{
                       Toast.makeText(applicationContext, "Limit has been removed...", Toast.LENGTH_SHORT)
                           .show()
                   }
                }else{
                    Handler(Looper.getMainLooper()).post{
                        Toast.makeText(applicationContext, "Limit has already been removed...", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                }else{
                    Handler(Looper.getMainLooper()).post{
                        Toast.makeText(applicationContext, "App does not have a limit...", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
        val setTimerButton = findViewById<Button>(R.id.button_limit)
        setTimerButton.transformationMethod = null
        setTimerButton.setOnClickListener(){
            val view = layoutInflater.inflate(R.layout.number_picker,null)
            val builder = AlertDialog.Builder(context)
                .setView(view)

            val alertDialog = builder.create()
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val confirmButton = view.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.confirmLimit_button)
            val cancelButton = view.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.cancelLimit_button)


            val hourPicker = view.findViewById<NumberPicker>(R.id.hour_picker)
            val minPicker = view.findViewById<NumberPicker>(R.id.min_picker)

            hourPicker.maxValue = 24
            hourPicker.minValue = 0
            minPicker.maxValue = 59
            minPicker.minValue = 1

            confirmButton.setOnClickListener(){
                val hour = hourPicker.value
                val min = minPicker.value
                lifecycleScope.launch(Dispatchers.IO){
                    addLimit(appName, formatLimit(hour,min))
                }
                Log.d("POPOVERBUTTON", "Hour $hour Min $min")
                alertDialog.cancel()
            }
            cancelButton.setOnClickListener(){
                alertDialog.cancel()
                Log.d("POPOVERBUTTON","Cancel Clicked")
            }


            alertDialog.show()
        }



        val ai: ApplicationInfo = appStats.pm.getApplicationInfo(
            appName!!,
            android.content.pm.PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
        )

        title = appStats.pm.getApplicationLabel(ai)

        adapt = appSpecificListAdapter(appName,appStats, context = context)

        val ref_to = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvSpecificApp)
        ref_to.adapter = adapt
        ref_to.layoutManager = LinearLayoutManager(this)

        ref_to.addItemDecoration(
            DividerItemDecoration(
                baseContext,
                LinearLayoutManager.VERTICAL
            )
        )

    }

    fun barstuff(){
        val usage = appStats.getUsageDayByDayInWeek(appName)
        for(x in 0 until usage.size){
            entries.add(BarEntry(x.toFloat(),usage[x].toFloat()))
            Log.d("barStuff", "${usage[x].toFloat()}")
        }

        formatData(usage)


    }

    class barValueFormatter() : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return if (value < 3600000) {
                "${kotlin.math.floor((value % 3600000) / 60000).toInt()}m"
            }else{
                "${kotlin.math.floor(value / 3600000).toInt()}h ${kotlin.math.floor((value % 3600000) / 60000).toInt()}m"
            } //(values[position] / 3600000), ((values[position]% 3600000) / 60000)
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
        set.color = Color.WHITE
        data = BarData(set)
        data.setBarWidth(0.7f); // set custom bar width
        barChart.setData(data);




        barChart.setFitBars(false); // make the x-axis fit exactly all bars
        barChart.invalidate(); // refresh



        Log.d("TITTY", " Label ${barChart.axisRight.labelCount} Axis Max ${barChart.axisLeft.axisMaximum}")
    }

    fun formatLimit(hours:Int, mins:Int): Int {
        return (hours * appStats.hourInMilly) + (mins * appStats.minInMilly)
    }
    suspend fun addLimit(name:String, time: Int){
        val db = AppDataBase.getInstance(context)
        val usageDao = db.usageDao()
        val usageStats = usageStats(name, true, false,time.toLong(),appStats.getUsageOfSpecificApp(
            name,
            appStats.getMidNight().timeInMillis,
            System.currentTimeMillis()))
        val doesLimitExist = usageDao.getUsageStat(name)
        if (usageDao.getUsageStat(name) != null){
            usageDao.update(usageStats)
        }else{
            usageDao.insert(usageStats)
        }

    }
    suspend fun updateLimit(name:String, time: Int){
        val db = AppDataBase.getInstance(context)
        val usageDao = db.usageDao()
        val usageStats = usageStats(name, true, false,time.toLong(),appStats.getUsageOfSpecificApp(
            name,
            appStats.getMidNight().timeInMillis,
            System.currentTimeMillis()))
        usageDao.update(usageStats)
    }

    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "Notification Title"
            val descriptionText = "Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}

