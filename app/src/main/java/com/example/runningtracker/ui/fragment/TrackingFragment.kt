package com.example.runningtracker.ui.fragment

import android.Manifest.permission.*
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.runningtracker.R
import com.example.runningtracker.databinding.FragmentTrackingBinding
import com.example.runningtracker.services.TrackingService
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import androidx.lifecycle.Observer
import com.example.runningtracker.model.path.Polyline
import com.example.runningtracker.util.Constants
import com.example.runningtracker.util.Constants.currentOrientation
import com.example.runningtracker.util.PrimaryUtility
import org.osmdroid.views.overlay.Marker
import java.util.*


class TrackingFragment : Fragment() {

    private var binding: FragmentTrackingBinding? = null
    private lateinit var mapController: IMapController
    private var marker: Marker? = null


    //private val viewModel: MainViewModel by viewModels()

    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    var allPolyline = mutableListOf<org.osmdroid.views.overlay.Polyline>()

    /*@set:Inject
    var weight = 80f*/

    private var currentTimeInMillis = 0L

    //private var menu: Menu? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTrackingBinding.inflate(layoutInflater)
        Configuration.getInstance()
            .load(requireContext().applicationContext,
                activity?.getPreferences(Context.MODE_PRIVATE))

        return binding?.root

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ) {
            accessBackgroundLocation.launch(ACCESS_BACKGROUND_LOCATION)
        }
        binding?.osMap?.setTileSource(TileSourceFactory.MAPNIK)
        mapController = binding?.osMap?.controller!!
        binding?.osMap!!.setBuiltInZoomControls(false)
        binding?.osMap!!.setMultiTouchControls(false)

        if(currentOrientation == null) {
            currentOrientation = requireContext().resources.configuration.orientation
        }
        // we need intialize currentOrientaiton with null value somewhere




        binding?.fabPauseStart?.setOnClickListener {
            Toast.makeText(requireContext(),
                "ready for tracking!!",Toast.LENGTH_SHORT).show()
            toggleRun()
        }
        observeLiveData()

    }
    private fun getPositionMarker(): Marker { //Singelton
        if (marker == null) {
            marker = Marker(binding?.osMap)
            marker!!.title = "Here I am"
            marker!!.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker!!.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_location)
            binding?.osMap?.overlays?.add(marker)
        }
        return marker!!
    }
    private val accessBackgroundLocation: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){
                isGranted ->
            if(isGranted){
                /*Toast.makeText(requireContext(),"Permission Granted" +
                        "For access background location", Toast.LENGTH_LONG).show()*/
            }else{
                findNavController().navigate(R.id.action_trackingFragment_to_stepCounterFragment)
                Toast.makeText(requireContext(),"Please allow all time access to this app " +
                        "for proper tracking of your activity", Toast.LENGTH_LONG).show()
            }
        }
    private fun setUpToolbar(){
        (activity as AppCompatActivity).setSupportActionBar(binding?.toolbar)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            binding?.toolbar?.setNavigationIcon(ContextCompat.getDrawable(
                requireContext(), R.drawable.ic_back
            ))
            binding?.toolbar?.setNavigationOnClickListener {
                findNavController().navigate(R.id.action_trackingFragment_to_stepCounterFragment)
                currentOrientation = null
            }
        }
    }
    private fun observeLiveData(){
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })
        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it.polyLines
            addLatestPolyline()
            moveCameraToUserLocation()
            binding?.tvDistance?.text = PrimaryUtility.getFormattedDistance(
                PrimaryUtility.calculateDistance(pathPoints))
            if(currentOrientation != requireContext().resources.configuration.orientation){
                addAllPolyLines()
                currentOrientation = requireContext().resources.configuration.orientation
            }
        })
        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            currentTimeInMillis = it
            val formattedTime = PrimaryUtility.getFormattedStopWatchTime(currentTimeInMillis, true)
            binding?.tvDuration?.text = formattedTime
        })
    }
    private fun toggleRun(){
        if (isTracking){
            //menu?.getItem(0)?.isVisible = true
            sendCommandToService(Constants.ACTION_PAUSE_SERVICE)
        } else{
            sendCommandToService(Constants.ACTION_START_OR_RESUME_SERVICE)
        }
    }
    private fun updateTracking(isTracking: Boolean){
        this.isTracking = isTracking
        if(!isTracking && currentTimeInMillis > 0L){
            binding?.fabPauseStart?.setImageDrawable(
                ContextCompat.getDrawable(requireContext(),R.drawable.ic_start))
            binding?.fabPauseStart?.imageTintList = ColorStateList
                .valueOf(Color.parseColor("#00BB09"))
            //binding?.btnFinishRun?.visibility = View.VISIBLE
        }else if (isTracking){
            binding?.fabPauseStart?.setImageDrawable(
                ContextCompat.getDrawable(requireContext(),R.drawable.ic_stop))
            binding?.fabPauseStart?.imageTintList = ColorStateList
                .valueOf(Color.parseColor("#FF0000"))
            //menu?.getItem(0)?.isVisible = true
            //binding?.btnFinishRun?.visibility = View.GONE
        }
    }
    private fun moveCameraToUserLocation(){
        if (pathPoints.isNotEmpty() && pathPoints.last().latLang.isNotEmpty()){

            mapController.setZoom(Constants.MAP_ZOOM)
            mapController.animateTo(pathPoints.last().latLang.last())
            getPositionMarker().position = pathPoints.last().latLang.last()
        }
    }
    /*private fun zoomToSeeWholeTrack(){
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints){
            for(pos in polyline){
                bounds.include(pos)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                binding?.mapView!!.width,
                binding?.mapView!!.height,
                (binding?.mapView!!.height * 0.05f).toInt()
            )
        )
    }*/
    /*private fun endRunAndSaveToDb(){
        map?.snapshot {  bmp  ->
            var distanceInMeters = 0
            for(polyline in pathPoints){
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }
            val avgSpeed = round((distanceInMeters / 1000f) /
                    (currentTimeInMillis / 1000f / 60 / 60) * 10) / 10f
            val dateTimestamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
            val run = Run(bmp, dateTimestamp, avgSpeed, distanceInMeters
                , currentTimeInMillis, caloriesBurned)
            viewModel.insertRun(run)

            Snackbar.make(
                requireView(),
                "Run saved successfully",
                Snackbar.LENGTH_LONG).show()
            stopRun()
        }
    }*/
    private fun addAllPolyLines(){
        for(polyline in pathPoints){
            val line = org.osmdroid.views.overlay.Polyline()
            line.apply {
                outlinePaint.color = Color.RED
                outlinePaint.strokeWidth = 10f
                setPoints(polyline.latLang)
                allPolyline.add(this)
            }

            binding?.osMap?.overlayManager?.add(line)
        }
    }
    private fun addLatestPolyline(){
        if(pathPoints.isNotEmpty() && pathPoints.last().latLang.size > 1){
            val line = org.osmdroid.views.overlay.Polyline()
            line.apply {
                outlinePaint.color = Color.RED
                outlinePaint.strokeWidth = 10f
                setPoints(pathPoints.last().latLang)
                allPolyline.add(this)
            }
            binding?.osMap?.overlayManager?.add(line)
        }
    }
    private fun sendCommandToService(action: String): Intent =
        Intent(requireContext(),TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    /*override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu,menu)
        this.menu = menu
    }*/

    //we can change visibility of menu item with onPrepareOptionsMenu
    /*override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if(currentTimeInMillis > 0L){
            this.menu?.getItem(0)?.isVisible = true
        }
    }*/

    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.miCancelTracking  ->   {
                showCancelTrackingDialog()

            }
        }
        return super.onOptionsItemSelected(item)
    }*/
    /*private fun showCancelTrackingDialog(){
        CancelTrackingDialog().apply {
            setYesListener {
                stopRun()
            }
        }.show(parentFragmentManager,CANCEL_TRACKING_DIALOG_TAG)
    }*/
    /*private fun stopRun(){
        binding?.tvTimer?.text = "00:00:00:00"
        sendCommandToService(Constants.ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }*/

    override fun onResume() {
        super.onResume()
        binding?.osMap?.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding?.osMap?.onPause()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.osMap?.onDetach()
        binding = null

    }
}