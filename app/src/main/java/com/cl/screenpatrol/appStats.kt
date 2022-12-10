package com.cl.screenpatrol

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

import java.time.LocalTime
import java.util.Calendar

class appStats(context: Context) {

    val weekInMilly = ((1000*3600*24) * 7)
    val dayInMilly = (1000*3600*24)
    val hourInMilly = (1000*3600)

    var keysToSpecificAppQuery = mutableListOf<String>(
        "Usage Today",
        "Usage Past Week",
        "Average Daily Usage (Past Week)",
        "Average Daily Usage (Past Month)"
    )


    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    val pm:PackageManager = context.packageManager

    fun returnApp(timeFrame:Int): MutableMap<String, UsageStats> {
        val currentTime = System.currentTimeMillis()
        val usageEvents = usageStatsManager.queryAndAggregateUsageStats(
            currentTime - timeFrame,
            currentTime
        )

        Log.d("Size before filter", "${usageEvents.size}")

       usageEvents.entries.removeIf{it.value.totalTimeInForeground.toInt() < 60000}



      return usageEvents.toList().sortedByDescending {it.second.totalTimeInForeground }.toMap() as MutableMap<String, UsageStats>
    }

    fun getMidNight(): Calendar { // Returns the amount of milliseconds since midnight of current day
        val midnight = Calendar.getInstance()
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);
        return midnight;
    }
    fun getStartOfWeek(): Long { // Returns the amount of milliseconds since the start of the week at midnight on Sunday
        val calendar = Calendar.getInstance()
        val midNight = getMidNight()
        val day = calendar.get(Calendar.DAY_OF_WEEK)
        val fullDaysPassed = day - 1
        return (fullDaysPassed * dayInMilly) + (Calendar.getInstance().timeInMillis - midNight.timeInMillis)

    }

    fun getStartOfWeekMinusToday(): Long { // Returns the time in Milliseconds at the very start of the nearest past sunday
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_WEEK)
        val midnight = getMidNight()
        val fullDaysPassed = day - 1
        return midnight.timeInMillis - (fullDaysPassed * dayInMilly)
    }

    fun getStartOfMonthMinusToday(): Long { // returns the time in Milliseconds at the first day of the month
        val calendar = Calendar.getInstance()
        val midnight = getMidNight()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val fullDaysPassed = day - 1
        return midnight.timeInMillis - (fullDaysPassed * dayInMilly)
    }

    fun getStartOfMonth(): Long { // Returns the number of milliseconds since the start of the month up to realtime
        val calendar = Calendar.getInstance()
        val midnight = getMidNight()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val fullDaysPassed = day - 1
        return (fullDaysPassed * dayInMilly) + (Calendar.getInstance().timeInMillis - midnight.timeInMillis)
    }



    fun printSomeDateValues(){
        val calendar = Calendar.getInstance()
        val midnight = getMidNight()
        val startofweek = getStartOfWeekMinusToday()
        val startofmonth = getStartOfMonthMinusToday()
        var startTime = midnight.timeInMillis
        var time = LocalTime.now()
        var day = calendar.get(Calendar.DAY_OF_MONTH)
        var dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        var month = calendar.get(Calendar.MONTH)
        var date = calendar.get(Calendar.DATE)
        var now = Calendar.getInstance().timeInMillis - midnight.timeInMillis
        var randoList = mutableListOf<Int>(day,dayOfWeek,month,date,)
        randoList.forEach { entry->
            Log.d("DAY_TRIPS", "$entry")
        }
        Log.d("DAY_TRIPS", "Millys at Start of month ${startofmonth} Millys at start of day ${startTime} Now ${now} ") // Now is the amount of millys since the day started
    }

    fun getAppSpecficStats(name: String): MutableList<Long> {
        val usageToday = getUsageToday(name)
        val usageThisWeek = getUsageThisWeek(name)
        val averageUsageThisWeek = getAverageUsageThisWeek(name)
        val averageUsageThisMonth = getAverageUsageThisMonth(name)

        return mutableListOf(usageToday, usageThisWeek, averageUsageThisWeek, averageUsageThisMonth)
        // Log.d("TEST_LOGG", "Name $name $usageToday , ${usageThisWeek / 3600000} hrs, ${(usageThisWeek% 3600000) / 60000} mins , $averageUsageThisWeek , $averageUsageThisMonth")
    }

    fun getUsageToday(name: String): Long {
        val midnight = getMidNight()
        val currentTime = System.currentTimeMillis()
        val usageTodayQuery = usageStatsManager.queryAndAggregateUsageStats(
            midnight.timeInMillis,
            currentTime
        )
        val app = usageTodayQuery[name]
        return if (app != null) {
                app.totalTimeInForeground
        } else{
            0
        }

    }

    fun getUsageThisWeek(name: String): Long {
        val midnight = getMidNight()
        val startOfWeek = getStartOfWeek()
        val days = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
        val currentTime = System.currentTimeMillis()
        val usageThisWeekQuery = usageStatsManager.queryAndAggregateUsageStats(
            currentTime - startOfWeek,
            currentTime
        )
      val app = usageThisWeekQuery[name]

        return if (app != null) {
            if (days != 0) {
                app.totalTimeInForeground / days
            }else{
                0
            }
        } else{
            0
        }

    }

    fun getAverageUsageThisWeek(name: String): Long {
        val startOfTheWeek = getStartOfWeekMinusToday()
        val midnight = getMidNight()
        val days = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
        val usageThisWeekExToday = usageStatsManager.queryAndAggregateUsageStats(
            startOfTheWeek,
            (midnight.timeInMillis - 1) // So its up to but not including the first millisecond of the next day
        )

        val app = usageThisWeekExToday[name]

        return if (app != null) {
            if (days != 0) {
                app.totalTimeInForeground / days
            }else{
                0
            }
        } else{
            0
        }
    }

    fun getAverageUsageThisMonth(name: String): Long{
        val startOfTheMonth = getStartOfMonthMinusToday()
        val midnight = getMidNight()
        val days = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val usageThisMonthExToday = usageStatsManager.queryAndAggregateUsageStats(
            startOfTheMonth,
            (midnight.timeInMillis - 1)
        )
        val app = usageThisMonthExToday[name]

        return if (app != null){
            app.totalTimeInForeground / days
        }else{
            0
        }
    }







}






