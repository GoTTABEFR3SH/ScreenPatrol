package com.cl.screenpatrol

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class appSpecificListAdapter(
    private var name:String,
    private var appStats:appStats,
    private var usage: appStats.usageTimes = appStats.getUsagesWAveragesForSpecificApp(name),
    private var keys: List<String> = appStats.keysToSpecificAppQuery,
    private var values:List<Long> = listOf(usage.UsageToday, usage.UsageThisWeek, usage.averageUsageThisWeek),
    private val context: Context



): RecyclerView.Adapter<appSpecificListAdapter.specificAppStatsViewHolder>(){
    class specificAppStatsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val timeUsedText: TextView
        val timeUsed:TextView
        init {
            timeUsedText = itemView.findViewById(R.id.textField1)
            timeUsed = itemView.findViewById(R.id.textField2)
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): specificAppStatsViewHolder {
        return specificAppStatsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.app_specific_list_item,
                parent,
                false
            ),

        )
    }


    override fun onBindViewHolder(holder: specificAppStatsViewHolder, position: Int) {
        holder.timeUsedText.text = keys[position]
        holder.timeUsed.text = context.getString(R.string.formattedAppTime, (values[position] / 3600000), ((values[position]% 3600000) / 60000))


    }
    override fun getItemCount(): Int {
        return values.size
    }
}