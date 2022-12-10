package com.cl.screenpatrol

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.screenpatrol.R.*

class AppSpecificScreen : AppCompatActivity() {
    private lateinit var context: Context
    private lateinit var appStats: appStats
    private lateinit var adapt: appSpecificListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_app_specific_screen)
        var intent = intent
        var appName = intent.getStringExtra("Name")

        context = this
        appStats = appStats(context)

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
}