package com.example.runningtracker.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.runningtracker.databinding.BottomSheetBinding
import com.example.runningtracker.util.Constants
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MaterialBottomSheet : BottomSheetDialogFragment() {

    private var bottomSheetBinding: BottomSheetBinding? = null

    @Inject
    lateinit var sharedPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bottomSheetBinding = BottomSheetBinding.inflate(layoutInflater)
        return bottomSheetBinding?.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding?.ivActivityCycling?.setOnClickListener {
            sharedPref
                .edit()
                .putString(Constants.Activity_Type, Constants.ACTIVITY_BICYCLING)
                .apply()
            Toast.makeText(requireContext(),"Bicycling!!", Toast.LENGTH_SHORT).show()
            dismiss()
        }
        bottomSheetBinding?.ivActivityRunning?.setOnClickListener {
            sharedPref
                .edit()
                .putString(Constants.Activity_Type, Constants.ACTIVITY_RUN_OR_WALK)
                .apply()
            Toast.makeText(requireContext(),"Running!!", Toast.LENGTH_SHORT).show()
            dismiss()
        }
        dialog?.setCancelable(false)

    }


    companion object {
        const val TAG = "ModalBottomSheet"
    }

}