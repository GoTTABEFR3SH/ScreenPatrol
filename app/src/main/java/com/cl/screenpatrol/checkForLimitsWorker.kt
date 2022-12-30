package com.cl.screenpatrol

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

import kotlin.math.roundToInt


class checkForLimitsWorker(private var context:Context, params:WorkerParameters): CoroutineWorker(context,params){
    val CHANNEL_ID = "ScreenPatrol_Limits"
    val NOTIFICATION_ID = 101
    val pm = applicationContext.packageManager

    override suspend fun doWork(): Result {

        return try {
            val appStats = appStats(context)
            val db = AppDataBase.getInstance(context)
            val dao = db.usageDao()
            val appList = dao.getAll()
            createNotificationChannel(context)

            appList.forEach { entry ->
                if(entry.limit == true){
                    val timeUsed = appStats.getUsageToday(entry.id)
                    Log.d("What???", "$timeUsed ${entry.id}")
                    val intial = usageStats(entry.id,entry.limit, entry.hit, entry.timeLimit, timeUsed)
                    dao.update(intial)
                    if (entry.hit == true && timeUsed < entry.timeLimit!!){
                        val upDated = usageStats(entry.id,entry.limit,false, entry.timeLimit, timeUsed)
                        dao.update(upDated)
                    } else if (entry.hit == false && timeUsed > entry.timeLimit!!){
                       // Notification goes here
                        entry.timeUsed?.let { appStats.formatMilliseconds(it) }?.let {
                            sendNotification(
                                context,
                                getName(entry.id) as String,
                                entry.id,
                                it
                            )
                        }
                        val upDated = usageStats(entry.id, entry.limit, true, entry.timeLimit, timeUsed)
                        dao.update(upDated)
                    }
                    Log.d("limitsWorkerBoy", "${entry} \n")
                }
            }
            Result.success()
        }catch (t:Throwable){
            Log.d("limitsWorkerBoy", "It failed")
            Result.failure()
        }
    }

    private fun createNotificationChannel(context: Context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "Notification Title"
            val descriptionText = "Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun sendNotification(context: Context, name:String, packageName: String, timeUsed:appStats.formatedMilliseconds){
        val checkLimitsIntent = Intent(context, AppSpecificScreen::class.java)
            .putExtra("Name", packageName)
        val checkLimitsIntentPending: PendingIntent? = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(checkLimitsIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.yeppty)
            .setContentTitle("Daily Limit Hit for $name! ")
            .setContentIntent(checkLimitsIntentPending)
        (if (timeUsed.hours < 1 ){
            builder.setContentText("You have used $name for ${timeUsed.mins.roundToInt()} Mins")
        } else {
            builder.setContentText("You have used $name for ${timeUsed.hours.roundToInt()} Hrs ${timeUsed.mins.roundToInt()}Mins")

        }).priority = NotificationCompat.PRIORITY_DEFAULT
        with(NotificationManagerCompat.from(context)){
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun getName(packageName: String): CharSequence {
        val ai: ApplicationInfo = pm.getApplicationInfo(
            packageName,
            PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
        )
        return pm.getApplicationLabel(ai)
    }
}