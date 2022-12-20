package com.cl.screenpatrol

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity() {
   private lateinit var adapt: listAdapter
   private lateinit var context: Context
   private lateinit var appStats: appStats
   private lateinit var appMap:MutableMap<String, Long>
   val calendar = Calendar.getInstance()
   override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this
    /*   val requestPermissionLauncher =
           registerForActivityResult(
               ActivityResultContracts.RequestPermission()
           ) { isGranted: Boolean ->
               if (isGranted) {
                   Log.d("Permission_yay", "Permission is great buddy")
                   // Permission is granted. Continue the action or workflow in your
                   // app.
               } else {
                   Log.d("Permission_Nah", "Permission is fucked buddy")
                   startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS) )
                   }
               }


       when{
           ContextCompat.checkSelfPermission(
               this,
               android.Manifest.permission.PACKAGE_USAGE_STATS
           ) == PackageManager.PERMISSION_GRANTED-> {
               Log.d("Permission_Granted", "Permission was granted sweet")
           }
           else-> {
               Log.d("Permission_Denied", "Permission is fucked buddy")
               requestPermissionLauncher.launch(android.Manifest.permission.PACKAGE_USAGE_STATS)
           }
       }
*/
        val name = "com.snapchat.android"

       appStats = appStats(context)

     /*  val usageOfApp = appStats.getUsageOfSpecificApp(name, appStats.getMidNight().timeInMillis - appStats.dayInMilly, appStats.getMidNight().timeInMillis)
       val usages = appStats.getUsagesWAveragesForSpecificApp(name)
       val start = appStats.getMidNight().timeInMillis - appStats.dayInMilly
       val appList = appStats.getAppList()*/
       adapt = listAdapter(appStats, context = this)
       //appStats.getUsageDayByDayInWeek(name)

       //val num = 5

    //    appStats.printSomeValues()
  //     appStats.getUsageByDayOfMonth("com.google.android.youtube", num,calendar.get(Calendar.MONTH),calendar.get(Calendar.YEAR),0)

        val refernce_to = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvApps)
        refernce_to.adapter = adapt
        refernce_to.layoutManager = LinearLayoutManager(this)
        val ref = this
        val ex = usageStats("test1", "Fug dude")
        val db = Room.databaseBuilder(
            applicationContext,
            AppDataBase::class.java, "usage"
        ).build()




       lifecycleScope.launch(Dispatchers.IO){

               val usageDoa = db.usageDao()
               val test = usageDoa.getUsageStat("test1")
               Log.d("DATABASEONLYFF", test)
           }
       }





    private fun navTOAppSpecificPage(){
        val intent = Intent(this, AppSpecificScreen::class.java)
        startActivity(intent)

    }
}