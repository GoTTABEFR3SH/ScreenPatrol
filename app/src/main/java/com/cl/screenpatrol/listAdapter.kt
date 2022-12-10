package com.cl.screenpatrol

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.provider.Settings.Global.getString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView

class listAdapter(
    private var appStats:appStats,
    var keys: List<String> = appStats.returnApp(appStats.weekInMilly).toList().map { it.first },
    private var values:List<Long> = appStats.returnApp(appStats.weekInMilly).toList().map { it.second.totalTimeInForeground },
    private val context: Context

): RecyclerView.Adapter<listAdapter.appStatsViewHolder>(){
    class appStatsViewHolder(itemView: View /*, clickListener: onItemClickListener*/): RecyclerView.ViewHolder(itemView){
        val timeUsedText:TextView
        val appNameText:TextView
        val appLogoImage:ImageView
        init {
            appNameText = itemView.findViewById(R.id.appName)
            timeUsedText = itemView.findViewById(R.id.timeUsed)
            appLogoImage = itemView.findViewById(R.id.appLogo)
           /* itemView.setOnClickListener {
                clickListener.onItemClick(bindingAdapterPosition)
            }*/
        }

    }

/*    private lateinit var mListener: onItemClickListener

    interface onItemClickListener{
        fun onItemClick(position: Int){

        }
    }



    fun setOnItemClickListener(clickListener: onItemClickListener){
        mListener = clickListener
    }
*/
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): appStatsViewHolder {
       return appStatsViewHolder(
           LayoutInflater.from(parent.context).inflate(
               R.layout.app_list_item,
               parent,
               false
           ),
          // mListener
       )
    }


    override fun onBindViewHolder(holder: appStatsViewHolder, position: Int) {
        val ai: ApplicationInfo = appStats.pm.getApplicationInfo(
            keys[position],
            android.content.pm.PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
        )
        val icon:Drawable = context.packageManager.getApplicationIcon(keys[position])
        holder.appLogoImage.setImageDrawable(icon)
        holder.appNameText.text = appStats.pm.getApplicationLabel(ai)
        holder.timeUsedText.text = context.getString(R.string.formattedAppTime, (values[position] / 3600000), ((values[position]% 3600000) / 60000))
        holder.itemView.setOnClickListener {
            val intent = Intent(context, AppSpecificScreen::class.java)
            intent.putExtra("Name", keys[position])
            context.startActivity(intent)
        }
    }
    override fun getItemCount(): Int {
        return values.size
    }
}