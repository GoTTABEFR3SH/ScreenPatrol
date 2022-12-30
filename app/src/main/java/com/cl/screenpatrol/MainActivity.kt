package com.cl.screenpatrol

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Binder
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext


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

       val checkForLimits = PeriodicWorkRequestBuilder<checkForLimitsWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "CheckLimits",
                ExistingPeriodicWorkPolicy.KEEP,
                checkForLimits)
       val my = OneTimeWorkRequestBuilder<checkForLimitsWorker>().build()
       val man = WorkManager.getInstance(this)
       man.enqueue(my)


       val name = "com.snapchat.android"

       appStats = appStats(context)


       checkNotificationPermission()



   }

    override fun onResume() {
        super.onResume()
        usagePermission()
        Log.d("NOTHOME", "HERE")
    }

    private fun navTOAppSpecificPage(){
        val intent = Intent(this, AppSpecificScreen::class.java)
        startActivity(intent)

    }

    private fun checkUsageStatsPermission(): Boolean {
        val appOps = context
            .getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            "android:get_usage_stats",
            Process.myUid(), context.packageName
        )
       return mode == AppOpsManager.MODE_ALLOWED
    }
    private fun checkNotificationPermission(){
        when {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {

            }
            shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS) -> {
            val snack = Snackbar.make(findViewById(R.id.activity_main), "This application needs permission" +
                    "to show notifications so you will know when daily limits are hit", Snackbar.LENGTH_LONG)
                snack.show()
        }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 0
                )
            }
        }
    }

    private fun usagePermission(){

        if (!checkUsageStatsPermission()){

            val view = layoutInflater.inflate(R.layout.requestusage,null)
            val builder = AlertDialog.Builder(context)
                .setView(view)

            val alertDialog = builder.create()
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val confirmButton = view.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.grant_button)
            val cancelButton = view.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.dismiss_button)
            val bodyText = view.findViewById<TextView>(R.id.request_tv)
            bodyText.text = buildString {
                append("ScreenPatrol needs permission ")
                append("to collect usage stats data in order to function. All of this data is stored locally and will not be shared")
                append(" with anyone, to enable tap Grant and scroll to ScreenPatrol then tap to enable")
            }
            confirmButton.setOnClickListener{
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                alertDialog.cancel()




            }
            cancelButton.setOnClickListener{
                finish()
            }
            alertDialog.show()

        }else{

            adapt = listAdapter(appStats, context = this)
            val refernce_to = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvApps)
            refernce_to.adapter = adapt
            refernce_to.layoutManager = LinearLayoutManager(this)
        }

    }

}