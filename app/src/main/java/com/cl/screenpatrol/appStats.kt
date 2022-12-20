package com.cl.screenpatrol

import android.annotation.SuppressLint
import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import com.github.mikephil.charting.data.BarEntry
import java.text.SimpleDateFormat
import java.time.Duration

import java.util.Calendar
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

class appStats(context: Context) {
    data class usageTimes(
        var UsageToday: Long = 0,
        var UsageThisWeek: Long = 0,
        var averageUsageThisWeek: Long = 0,

    )

    val weekInMilly = ((1000*3600*24) * 7)
    val dayInMilly = (1000*3600*24)
    val hourInMilly = (1000*3600)

    var keysToSpecificAppQuery = mutableListOf<String>(
        "Usage Today",
        "Usage Past Week",
        "Average Daily Usage (Past Week)"
    )

    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    val pm:PackageManager = context.packageManager

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

    fun getTimeInMillySinceLastSunday(): Long { // Returns the time in Milliseconds at the very start of the nearest past sunday
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_WEEK)
        val midnight = getMidNight()
        val fullDaysPassed = day - 1
        return midnight.timeInMillis - (fullDaysPassed * dayInMilly)
    }

    fun getStartTimeInMillyOnFirstDayOfMonth(): Long { // returns the time in Milliseconds at the first day of the month
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

    fun getAppList(): MutableList<String> {
        val appList:List<ApplicationInfo> = pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
        var appFilteredList:MutableList<String> = mutableListOf()
        appList.forEach { entry ->
            if (entry.flags and ApplicationInfo.FLAG_SYSTEM == 0 ||
                entry.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0) {
                if (entry.packageName != null) {
                    appFilteredList.add(entry.packageName)
                }
            }
        }
        return appFilteredList

    }

    fun printSomeValues(){
        val appList:List<ApplicationInfo> = pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
        var appFilteredList:MutableList<ApplicationInfo> = mutableListOf()
        appList.forEach { entry ->
            if (entry.flags and ApplicationInfo.FLAG_SYSTEM == 0 ||
                    entry.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP == 0) {
                appFilteredList.add(entry)
            }
        }
        Log.d("APP_LISTT", "Size of appList ${appList.size} Size of appFilteredList ${appFilteredList.size}")
    }

    private fun getTimeInMillyOfSpecificDateAndTime(day:Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DAY_OF_MONTH, day)
        return calendar.timeInMillis

    }

    @SuppressLint("SuspiciousIndentation")
    fun getUsageByDayOfMonth(name:String, day: Int, month: Int, year: Int, hour: Int){
        val allEvents = mutableListOf<UsageEvents.Event>()
        val targetDate = getTimeInMillyOfSpecificDateAndTime(day)
        var num:Long = 0
        val event = usageStatsManager.queryEvents(targetDate, (targetDate + dayInMilly))

        while (event.hasNextEvent()){
          val currentEvent = UsageEvents.Event()
            event.getNextEvent(currentEvent)
            if(currentEvent.packageName == name){
                if(currentEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED || currentEvent.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                          allEvents.add(currentEvent)
                }
              }
            }
        for (x in 0 until allEvents.size){
            if (x == 0 && allEvents[x].eventType == UsageEvents.Event.ACTIVITY_PAUSED){
                num += allEvents[x].timeStamp - targetDate
                Log.d("retard", "hour ${hour} step ${1} num ${(num/ 3600000)} hrs ${(num% 3600000) / 60000} mins")
            }else if (allEvents[x].eventType == UsageEvents.Event.ACTIVITY_PAUSED){
                num += allEvents[x].timeStamp - allEvents[x - 1].timeStamp
                Log.d("retard", "hour ${hour} step ${2} num ${(num/ 3600000)} hrs ${(num% 3600000) / 60000} mins total ${num}")
            }else if (x == (allEvents.size - 1) && allEvents[x].eventType == UsageEvents.Event.ACTIVITY_RESUMED){
                num += (targetDate + hourInMilly) - allEvents[x].timeStamp
                Log.d("retard", "hour ${hour} step ${3} num ${(num/ 3600000)} hrs ${(num% 3600000) / 60000} mins")
            }
        }
        allEvents.forEach { entry ->
            Log.d("asscheeks", "${entry.eventType} Hour ${hour} timestamp ${entry.timeStamp}")
        }
        Log.d("TRIGGER_NIGGER", "Hour ${hour} Time ${(num/ 3600000)} hrs ${(num% 3600000) / 60000} mins SIZE ${allEvents.size}")
    }

    fun getUsageOfAllApps(startTime:Long, endTime:Long, appList: MutableList<String>): Map<String, Long> {

        val allEvents = mutableListOf<UsageEvents.Event>()
        val usageOfAllApps = mutableMapOf<String, Long>()
        val event = usageStatsManager.queryEvents(startTime, endTime)
        var events = 0
        while (event.hasNextEvent()){
            val currentEvent = UsageEvents.Event()
            event.getNextEvent(currentEvent)
                if(currentEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED || currentEvent.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                    allEvents.add(currentEvent)
            }
        }

            for (x in 0 until appList.size){
                usageOfAllApps.put(appList[x], 0)
                for (y in 0 until allEvents.size){
                    if (appList[x] == allEvents[y].packageName){
                        if (y == 0 && allEvents[y].eventType == UsageEvents.Event.ACTIVITY_PAUSED){
                            usageOfAllApps.put(appList[x], (usageOfAllApps[appList[x]]!! + (allEvents[y].timeStamp - startTime))  )

                        }else if (allEvents[y].eventType == UsageEvents.Event.ACTIVITY_PAUSED && allEvents[y - 1].eventType != 2){
                            usageOfAllApps.put(appList[x], (usageOfAllApps[appList[x]]!! + (allEvents[y].timeStamp - allEvents[y - 1].timeStamp)))
                            if(allEvents[y].packageName == "com.google.android.youtube"){
                                Log.d("getusageofallapps", "${allEvents[y].packageName} num ${usageOfAllApps.get("com.google.android.youtube")}  timestamp ${allEvents[y].timeStamp} previous ${allEvents[y - 1].eventType}")
                            }
                        }else if (y == allEvents.size - 1 && allEvents[y].eventType == UsageEvents.Event.ACTIVITY_RESUMED){
                            usageOfAllApps.put(appList[x], (usageOfAllApps[appList[x]]!! + (endTime - allEvents[y].timeStamp)))
                        }
                    }
                }
            }
            for(x in 0 until allEvents.size){
                if (allEvents[x].packageName == "com.google.android.youtube" ){
                    events += 1
                }
            }
            usageOfAllApps.entries.removeIf { it.value <= 6000 }
        Log.d("USAGE_BY_DONG", "all apps size ${events} ")

        return usageOfAllApps.toList().sortedByDescending { it.second }.toMap()
    }

    fun getUsageOfSpecificApp(
        name: String,
        startTime: Long,
        endTime: Long,
    ): Long {

        var num = 0L
        val allEvents = mutableListOf<UsageEvents.Event>()
        val event = usageStatsManager.queryEvents(startTime, endTime)

        while (event.hasNextEvent()) {
            val currentEvent = UsageEvents.Event()
            event.getNextEvent(currentEvent)
            if (currentEvent.packageName == name) {
                if (currentEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED || currentEvent.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                    allEvents.add(currentEvent)
                }
            }
        }


        Log.d("USAGE_BY_DONG", "sepcific app size ${name} ${allEvents.size} ")
        for (x in 0 until allEvents.size) {
            if (x == 0 && allEvents[x].eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                num += (allEvents[x].timeStamp - startTime)
                Log.d("USAGE_BY_NIG", "sepcific app queue 1 ${name} num ${num}  timestamp ${allEvents[x].timeStamp} ")
            } else if (allEvents[x].eventType == UsageEvents.Event.ACTIVITY_PAUSED && allEvents[x - 1].eventType != 2) {
                num += (allEvents[x].timeStamp - allEvents[x - 1].timeStamp)
                Log.d("getspecificapp", "${name} num ${num}  timestamp ${allEvents[x].timeStamp} previous ${allEvents[x - 1].eventType} ")
            } else if (x == (allEvents.size - 1) && allEvents[x].eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                Log.d("USAGE_BY_NIG", "sepcific app queue ${name} num ${num} endtime ${endTime} timestamp ${allEvents[x].timeStamp} ")
                num += (endTime - allEvents[x].timeStamp)
            }
        }

        return num
    }

    fun getUsagesWAveragesForSpecificApp(name: String): usageTimes {
        val usage:usageTimes = usageTimes()
        val calendar = Calendar.getInstance()
        val midNight = getMidNight().timeInMillis
        val rightNow = System.currentTimeMillis()
        val startOfMonth = getStartTimeInMillyOnFirstDayOfMonth()
        val startOfWeek = getTimeInMillySinceLastSunday()
        val daysThisWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysThisMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val test = Calendar.getInstance()
        test.set(Calendar.DAY_OF_MONTH, daysThisMonth + 15)

        usage.UsageToday = getUsageOfSpecificApp(name, midNight, rightNow )
        Log.d("Calendar$$$", "${getTimeInMillyOfSpecificDateAndTime((daysThisMonth - 1) - 30)}")
        usage.UsageThisWeek = getUsageOfSpecificApp(name, startOfWeek, rightNow)
        if (daysThisWeek == 0){
            usage.averageUsageThisWeek = 0

        }else{
            usage.averageUsageThisWeek = usage.UsageThisWeek / daysThisWeek

        }

        return usage
    }



    @OptIn(ExperimentalTime::class)
    fun getUsageDayByDayInWeek(name: String): MutableList<Long> {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val usageValuesByDay = mutableListOf<Long>()
        val daysList: List<String> = listOf<String>("Sun", "Mon", "Tues", "Weds", "Thurs", "Fri", "Sat")
        var dayIt = getTimeInMillySinceLastSunday()
        for(x in 0..6){
            if(x == 0){
                Log.d("getspecificapp", "-----------------------------------------------")
                usageValuesByDay.add(getUsageOfSpecificApp(name, dayIt, (dayIt + dayInMilly) - 1))
                dayIt+= dayInMilly
                Log.d("getspecificapp", "-----------------------------------------------")
            }else if(x == dayOfWeek){
                usageValuesByDay.add(getUsageOfSpecificApp(name, dayIt, System.currentTimeMillis()))
            }else if (x > dayOfWeek){
                usageValuesByDay.add(0)
            } else{
                usageValuesByDay.add(getUsageOfSpecificApp(name, dayIt, (dayIt + dayInMilly) - 1))
                dayIt += dayInMilly
            }

        }

        usageValuesByDay.forEach { entry ->



        }

        return usageValuesByDay
    }

    data class formatedMilliseconds(
        var hours:Float = 0f,
        var mins:Float = 0f,
        var secs:Float = 0f
            )

    fun formatMilliseconds(entry: Long): formatedMilliseconds {
        val formatted = formatedMilliseconds()
        formatted.hours = (entry / hourInMilly).toFloat()
        formatted.mins = ((entry % hourInMilly) / 60000).toFloat()
        formatted.secs = (((entry % hourInMilly) % 60000) / 1000).toFloat()

        Log.d("USAGE_BY_DAY", "${formatted.hours} H ${formatted.mins} m ${formatted.secs} s")
        return formatted
    }


}






