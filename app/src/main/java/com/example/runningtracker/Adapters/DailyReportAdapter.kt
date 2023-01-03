package com.example.runningtracker.Adapters

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
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

        if(list.size>1){
            val layoutParams = LinearLayout.LayoutParams((parent.width*0.85).toInt()
                , LinearLayout.LayoutParams.WRAP_CONTENT)

            layoutParams.setMargins((15.toDp()).toPx(),0,(40.toDp()).toPx(),0)
            val view = LayoutInflater.from(context).inflate(R.layout.daily_report_recycler_item,
                parent, false)
            view.layoutParams = layoutParams

            return ViewHolder(DailyReportRecyclerItemBinding.bind(view))
        }else{
            return ViewHolder(
                DailyReportRecyclerItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.tvAvgSpeed.text = "Average Speed: ${item.runningAvgSpeedKMH} km/h"
        holder.binding.tvTime.text = "Time: ${item.runningTimeInMillis?.let {
            getFormattedStringTime(
                it
            )
        }}"
        holder.binding.tvDistance.text = "Distance: ${item.runningDistanceInMeters} m"
        holder.binding.tvCaloriesBurned.text = "Calories Burned: ${item.caloriesBurned}"
        if(item.activity_type == Constants.ACTIVITY_CYCLING) {
            holder.binding.ivActivity.setImageResource(R.drawable.ic_bycicle)
        }else if(item.activity_type == Constants.ACTIVITY_RUN_OR_WALK){
            holder.binding.ivActivity.setImageResource(R.drawable.ic_walking)
        }
        holder.binding.ivTrack.setImageURI(item.runningImg)
        holder.binding.ivTrack.scaleType = ImageView.ScaleType.CENTER_CROP
        setUiConfig(holder)

    }

    override fun getItemCount(): Int {
        return list.size
    }
    private fun setUiConfig(holder: ViewHolder){
        if(list.size>1){
            holder.binding.tvAvgSpeed.textSize = 14.0f
            holder.binding.tvTime.textSize =  14.0f
            holder.binding.tvDistance.textSize =  14.0f
            holder.binding.tvCaloriesBurned.textSize = 14.0f
            holder.binding.cvImageHolder.layoutParams?.height = 1200.toDp()
            holder.binding.cvImageHolder.layoutParams?.width = 1200.toDp()
            holder.binding.cvImageHolder.requestLayout()
            holder.binding.ivActivity.layoutParams?.height = 375.toDp()
            holder.binding.ivActivity.layoutParams?.width = 3750.toDp()
            holder.binding.ivActivity.requestLayout()
        }
    }
    private fun getFormattedStringTime(timeInMillis: Long): String{
        val timeInSeconds = timeInMillis/1000
        val minute = if (timeInSeconds/60 < 10) "0${timeInSeconds / 60}" else timeInSeconds/60
        val seconds = if(timeInSeconds - ((timeInSeconds/60 ) * 60) != 0L) {
            "${timeInSeconds - ((timeInSeconds/60 ) * 60)}"
        }else{
            "00"
        }
        return "$minute:$seconds"
    }


}