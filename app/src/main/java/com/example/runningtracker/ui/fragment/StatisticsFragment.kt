package com.example.runningtracker.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.runningtracker.Adapters.StatisticsAdapter
import com.example.runningtracker.R
import com.example.runningtracker.databinding.FragmentStatisticsBinding
import com.example.runningtracker.db.RunningEntity
import com.example.runningtracker.models.day.Day
import com.example.runningtracker.ui.MainActivity
import com.example.runningtracker.ui.view_model.StatisticsViewModel
import com.example.runningtracker.util.Constants
import com.example.runningtracker.util.NavUtils
import com.example.runningtracker.util.Theme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class StatisticsFragment : Fragment() {

    private var binding: FragmentStatisticsBinding? = null
    private val viewModel: StatisticsViewModel by viewModels()


    @Inject
    lateinit var sdf: SimpleDateFormat

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentStatisticsBinding.inflate(layoutInflater)
        binding?.rvStatistics?.adapter?.notifyDataSetChanged()
        return binding?.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Theme.setUpStatusBarColorForUnderApi29Light(requireActivity())
        getMapOfActivityInSpecificDate(viewModel)
        Log.d("backstack",fragmentManager?.backStackEntryCount.toString())
    }

    private fun getMapOfActivityInSpecificDate(vm: StatisticsViewModel) {
        //val days = mutableListOf<Day>()
        val days = mutableListOf<Pair<Date,MutableList<RunningEntity>>>()

        val dates = mutableSetOf<Date>()
        for (i in MainActivity.run) {
            i.date?.let {
                dates.add(it)
            }
        }
        Log.d("date set", dates.toString())
        //val datesList = dates.toMutableList()
        lifecycleScope.launch(Dispatchers.IO) {
            for (i in dates) {
                //val d = Day(mutableMapOf())
                val list = vm.getTotalActivityInSpecificDay(i).toMutableList()
                //val a = mutableMapOf(i to list)
                //d.day = a
                //days.add(d)
                days.add(Pair(i,list))
            }
            Log.d("Day-ssss", days.toString())
            withContext(Dispatchers.Main){
                setupRecyclerView(days)
                animateRecyclerView()
            }
        }
    }
    private fun animateRecyclerView(){
        try {
            binding?.rvStatistics?.layoutParams?.height = LayoutParams.MATCH_PARENT
            val transition = ChangeBounds()
            transition.interpolator = AnticipateOvershootInterpolator(0.5f)
            transition.duration = 1000L
            transition.let {
                TransitionManager.beginDelayedTransition(binding?.rvStatistics, it)
            }
        }catch (e: java.lang.NullPointerException){
            binding?.rvStatistics?.layoutParams?.height = LayoutParams.MATCH_PARENT
            binding?.rvStatistics?.requestLayout()
        }
    }
    private fun setupRecyclerView(days: MutableList<Pair<Date, MutableList<RunningEntity>>>){
        if (days.isNotEmpty()){
            binding?.tvContent?.visibility = View.INVISIBLE
            binding?.rvStatistics?.visibility = View.VISIBLE
            val adapter = StatisticsAdapter(requireActivity(),days, sdf
            ){  day ->
                goToDetailsFragment(day)
            }
            binding?.rvStatistics?.layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.VERTICAL,false)
            binding?.rvStatistics?.adapter = adapter

        }else{
            binding?.tvContent?.visibility = View.VISIBLE
            binding?.rvStatistics?.visibility = View.INVISIBLE
        }
    }
    private fun goToDetailsFragment(day: Pair<Date, MutableList<RunningEntity>>){
        Log.d("detailDay",day.toString())
        val mDay = Day(day)
        val bundle = Bundle().apply {
            putSerializable("day",mDay)
        }
        findNavController().navigate(
            R.id.action_statisticsFragment_to_dailyReportDetailFragment
            ,bundle,NavUtils.navOptions(activity as MainActivity)[Constants.SLIDE_TOP])
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}
