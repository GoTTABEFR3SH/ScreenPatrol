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
import androidx.recyclerview.widget.LinearLayoutManager

class MainActivity : AppCompatActivity() {
   private lateinit var adapt: listAdapter
   private lateinit var context: Context
   private lateinit var appStats: appStats
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

        appStats = appStats(context)
       appStats.getAppSpecficStats("com.snapchat.android")
       adapt = listAdapter(appStats, context = this)



        val refernce_to = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvApps)
        refernce_to.adapter = adapt
        refernce_to.layoutManager = LinearLayoutManager(this)

 /*      adapt.setOnItemClickListener(object : listAdapter.onItemClickListener{
           override fun onItemClick(position: Int) {

               navTOAppSpecificPage()
           }
       }
       )*/
   }



    private fun navTOAppSpecificPage(){
        val intent = Intent(this, AppSpecificScreen::class.java)
        startActivity(intent)

    }
}