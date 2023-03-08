package com.rohan.sonybulbtimelapse.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.chaquo.python.PyException
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.material.slider.Slider
import com.rohan.sonybulbtimelapse.databinding.FragmentHomeBinding
import kotlinx.coroutines.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private var cameraObject: PyObject? = null
    private var isCamConnected: Boolean = false
    private var isTlRunning: Boolean = false

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        println("Loading Home fragment")

        if (! Python.isStarted()) {
            Python.start(AndroidPlatform(requireContext()))
        }

//        val homeViewModel =
//            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val py = Python.getInstance()

        // Contents
        val btnConnect: Button = binding.connectBtn
        val btnStart: Button = binding.startBtn
        val btnStop: Button = binding.stopBtn
        val frameCountSlider: Slider = binding.frameCountSlider
        val shutterSpeedSlider: Slider = binding.shutterSpeedSlider
        val intervalSlider: Slider = binding.intervalSlider

        btnConnect.setOnClickListener {
            Toast.makeText(requireContext(), "Connecting...", Toast.LENGTH_LONG).show()
            try {
                cameraObject = connectCamera(py)

                Toast.makeText(requireContext(), "Connected!", Toast.LENGTH_SHORT).show()
                btnConnect.text = "Connected"

                isCamConnected = true
                btnStart.isEnabled = true
                btnStop.isEnabled = false
                btnConnect.isEnabled = false
            }
            catch (e: Exception) {
                onCreateView(inflater, container, null)
            }
        }

        btnStart.setOnClickListener {
            isTlRunning = true

            Toast.makeText(requireContext(), "Starting timelapse...", Toast.LENGTH_LONG).show()

            this.activity?.runOnUiThread {
                btnStart.isEnabled = false
                btnStop.isEnabled = true

                frameCountSlider.isEnabled = false
                shutterSpeedSlider.isEnabled = false
                intervalSlider.isEnabled = false
            }

            Thread(
                Runnable {

                    startTimelapse(
                        cameraObject,
                        frameCountSlider.value.toInt(),
                        shutterSpeedSlider.value.toInt(),
                        intervalSlider.value.toInt(),
                        inflater, container
                    )

                    this.activity?.runOnUiThread {
                        binding.startBtn.isEnabled = true
                        binding.stopBtn.isEnabled = false

                        binding.frameCountSlider.isEnabled = true
                        binding.shutterSpeedSlider.isEnabled = true
                        binding.intervalSlider.isEnabled = true

                        isTlRunning = false

                        Toast.makeText(requireContext(), "Completed!", Toast.LENGTH_LONG).show()
                    }

                }
            ).start()

        }

        btnStop.setOnClickListener {
            if (isTlRunning) {
                Toast.makeText(requireContext(), "Stopping...", Toast.LENGTH_SHORT).show()
                btnStop.isEnabled = false
                isTlRunning = false
            }
        }

        val root: View = binding.root

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isCamConnected) {
            cameraObject?.callAttr("terminate")
        }
        _binding = null
    }

    fun connectCamera(py: Python): PyObject? {
        val sonyAPI = py.getModule("sonyAPI")
        val cameraClass = sonyAPI["Camera"]
        val cameraObject = cameraClass?.call()

        try {
            cameraObject?.callAttr("connect")
            cameraObject?.callAttr("setup")
        }
        catch (e: PyException) {
            println("pyexception - $e")
            if (e.toString().contains("NoCameraFoundException", ignoreCase = true)) {
                Toast.makeText(requireContext(), "No cameras found!", Toast.LENGTH_LONG).show()
            }
            else {
                Toast.makeText(requireContext(), "Sony API Error!", Toast.LENGTH_LONG).show()
            }
            throw Exception()
        }
        catch (e: Exception) {
            Toast.makeText(requireContext(), "Error!", Toast.LENGTH_LONG).show()
            throw Exception()
        }

        return cameraObject
    }

    fun updateTV(textViewObj: TextView, text: String) {
        this.activity?.runOnUiThread(Runnable {
            textViewObj.text = text
        })
    }

    fun singleBulbExposure(cameraObj: PyObject?, shutterSpeed: Int): Boolean {
        cameraObj?.callAttr("start_bulb_shooting")
        Thread.sleep(shutterSpeed * 1000L)
        cameraObj?.callAttr("stop_bulb_shooting")
        return true
    }

    fun startTimelapse(cameraObject: PyObject?, shots: Int, shutterSpeed: Int, interval: Int, inflater: LayoutInflater, container: ViewGroup?) {

        for (i in 1..shots) {
            if (isTlRunning) {

                println("Shooting $i out of ${shots} frames.")
                updateTV(binding.statusLabel, "Shooting $i out of ${shots} frames")

                singleBulbExposure(cameraObject, shutterSpeed)

                Thread.sleep(interval * 1000L)
            }
            else {
                this.activity?.runOnUiThread {
                    onCreateView(inflater, container, null)
                    updateTV(binding.statusLabel, "")
                }
            }
        }
    }

    fun resetView(inflater: LayoutInflater, container: ViewGroup?) {
        this.activity?.runOnUiThread(Runnable {
            onCreateView(inflater, container, null)
        })
    }
}