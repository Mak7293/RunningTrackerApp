package com.example.runningtracker.Adapters

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.runningtracker.R
import com.example.runningtracker.databinding.DailyReportRecyclerItemBinding

import com.example.runningtracker.db.RunningEntity
import com.example.runningtracker.util.Constants

class DailyReportAdapter(
    private val list: List<RunningEntity>,
    private val context: Context):
    RecyclerView.Adapter<DailyReportAdapter.ViewHolder>() {

    class ViewHolder(val binding: DailyReportRecyclerItemBinding):
        RecyclerView.ViewHolder(binding.root)


    private fun Int.toDp(): Int = (this/ Resources.getSystem().displayMetrics.density).toInt()
    private fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val layoutParams = LinearLayout.LayoutParams((parent.width*0.8).toInt()
            , LinearLayout.LayoutParams.WRAP_CONTENT)

        layoutParams.setMargins((15.toDp()).toPx(),0,(40.toDp()).toPx(),0)
        val view = LayoutInflater.from(context).inflate(R.layout.daily_report_recycler_item,
            parent, false)
        view.layoutParams = layoutParams

        return ViewHolder(DailyReportRecyclerItemBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.tvAvgSpeed.text = "Average Speed: ${item.runningAvgSpeedKMH} km/h"
        holder.binding.tvTime.text = "Time: ${getFormattedStringTime(item.runningTimeInMillis)}"
        holder.binding.tvDistance.text = "Distance: ${item.runningDistanceInMeters} m"
        holder.binding.tvCaloriesBurned.text = "Calories Burned: ${item.caloriesBurned}"
        if(item.activity_type == Constants.ACTIVITY_CYCLING) {
            holder.binding.ivActivity.setImageResource(R.drawable.ic_bycicle)
        }else if(item.activity_type == Constants.ACTIVITY_RUN_OR_WALK){
            holder.binding.ivActivity.setImageResource(R.drawable.ic_walking)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
    private fun getFormattedStringTime(timeInMillis: Long): String{
        val timeInSeconds = timeInMillis/1000
        val minute = timeInSeconds/60
        val seconds = if(timeInSeconds - (minute * 60) != 0L) {
            "${timeInSeconds - (minute * 60)}"
        }else{
            "00"
        }
        return "$minute:$seconds"
    }


}