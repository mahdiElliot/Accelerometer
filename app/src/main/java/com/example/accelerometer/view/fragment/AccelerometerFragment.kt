package com.example.accelerometer.view.fragment

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.accelerometer.R
import com.example.accelerometer.internal.BandPassFilter
import com.example.accelerometer.internal.DbHelperAcc
import com.example.accelerometer.internal.ViewModelsFactory
import com.example.accelerometer.model.Accelerometer
import com.example.accelerometer.view.activity.MainActivity
import com.example.accelerometer.viewModel.MainActivityViewModel
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.fragment_accelerometer.*
import uk.me.berndporr.iirj.Butterworth
import java.io.File
import java.io.FileWriter
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class AccelerometerFragment : Fragment(), SensorEventListener {

    lateinit var sensorManager: SensorManager
    lateinit var accSensor : Sensor

    lateinit var viewModel: MainActivityViewModel
    var x = 0.0
    var y = 0.0
    var z = 0.0
    lateinit var thread: Thread
//    var plotData = true
    var run = true
    var rate = 1000.0 / 50
    var center = 0.5
    var width = 100.0
    var order = 2

    val butterWorth = Butterworth()
    lateinit var timer: CountDownTimer
    var now = 0.toLong()

    lateinit var writer: FileWriter
    lateinit var dbHelper: DbHelperAcc
    var fileName = ""

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(requireActivity(), ViewModelsFactory()).get(MainActivityViewModel::class.java)

        return inflater.inflate(R.layout.fragment_accelerometer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        run = true
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)

        if (::accSensor.isInitialized)
            sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL)

        chart.description.isEnabled = true
        chart.description.text = "test chart"
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setDrawGridBackground(false)
        chart.setPinchZoom(true)
        chart.setBackgroundColor(Color.WHITE)


        val data = LineData()
        data.setValueTextColor(Color.WHITE)
        chart.data = data

        val l = chart.legend
        l.form = Legend.LegendForm.LINE
        l.textColor = Color.BLACK

        val xl = chart.xAxis
        xl.textColor = Color.BLACK
        xl.setDrawGridLines(true)
        xl.setAvoidFirstLastClipping(true)
        xl.isEnabled = true

        val leftAxis = chart.axisLeft
        leftAxis.textColor = Color.BLACK
        leftAxis.setDrawGridLines(true)
        leftAxis.axisMaximum = 6f
        leftAxis.axisMinimum = -4f

        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false
        chart.axisLeft.setDrawGridLines(false)
        chart.xAxis.setDrawGridLines(false)
        chart.setDrawBorders(false)
//        startPlot()

        btn_up.setOnClickListener {
            leftAxis.axisMaximum += 2
            leftAxis.axisMinimum += 2
        }

        btn_down.setOnClickListener {
            leftAxis.axisMaximum -= 2
            leftAxis.axisMinimum -= 2
        }

        btn_save.setOnClickListener {
            dbHelper = DbHelperAcc(context)
            if (viewModel.isOnTimer.value == true) {
                timer.cancel()
                viewModel.isOnTimer.value = false
                writer.flush()
                writer.close()
                tv_timer.visibility = View.GONE
                btn_save.setImageResource(R.drawable.ic_save)
            } else {
                btn_save.setImageResource(R.drawable.ic_close_white)
                tv_timer.text = viewModel.accSaveTime.value.toString()
                tv_timer.visibility = View.VISIBLE
                val formatter = SimpleDateFormat("yyyy_MM_dd")
                val now = Date()

                fileName = "acc_${formatter.format(now)}_${now.time}.txt"
                val f = File(MainActivity.file, fileName)
                f.createNewFile()
                writer = FileWriter(f)
                if (::timer.isInitialized) timer.cancel()
                viewModel.isOnTimer.value = true
                val startTime = viewModel.accSaveTime.value!! * 1000
                timer = object : CountDownTimer(startTime.toLong(), rate.toLong()) {
                    override fun onTick(millisUntilFinished: Long) {
                        onTimer(millisUntilFinished)
                    }

                    override fun onFinish() {
                        viewModel.isOnTimer.value = false
                        tv_timer.visibility = View.GONE
                        btn_save.setImageResource(R.drawable.ic_save)
                        writer.flush()
                        writer.close()
                        showDialog()
                        timer.cancel()

                    }
                }.start()
            }
        }

        btn_gyroscope.setOnClickListener {
            if (::thread.isInitialized) {
                thread.interrupt()
                run = false
            }
            if (::writer.isInitialized){
                writer.flush()
                writer.close()
            }
            if (::timer.isInitialized) {
                timer.cancel()
                viewModel.isOnTimer.value = false
            }
            findNavController().navigate(R.id.action_AccelerometerFragment_to_GyroscopeFragment)
        }

    }

    private fun createSet(color: Int, name: String): LineDataSet {
        val set = LineDataSet(null, name)
        set.axisDependency = YAxis.AxisDependency.LEFT
        set.setDrawCircles(false)
        set.lineWidth = 3f
        set.color = color
        set.mode = LineDataSet.Mode.CUBIC_BEZIER
        set.cubicIntensity = 0.2f
        return set
    }

    private fun addEntry(event : SensorEvent) {
        val data = chart.data
        if (data != null) {
            var set = data.getDataSetByIndex(0)
            var set2 = data.getDataSetByIndex(1)
            var set3 = data.getDataSetByIndex(2)

            if (set == null) {
                set = createSet(Color.BLUE, "x")
                data.addDataSet(set)
            }
            if (set2 == null) {
                set2 = createSet(Color.RED, "y")
                data.addDataSet(set2)
            }
            if (set3 == null) {
                set3 = createSet(Color.GREEN, "z")
                data.addDataSet(set3)
            }

            x = event.values[0].toDouble()
            y = event.values[1].toDouble()
            z = event.values[2].toDouble()

            viewModel.accSubmitSetting.observe(viewLifecycleOwner, Observer {
                if (it) {
                    butterWorth.bandPass(order, rate, center, width)
                    val x2 = butterWorth.filter(x)
                    butterWorth.bandPass(order, rate, center, width)
                    val y2 = butterWorth.filter(y)
                    butterWorth.bandPass(order, rate, center, width)
                    val z2 = butterWorth.filter(z)
                    data.addEntry(Entry(set.entryCount.toFloat(), x2.toFloat()), 0)
                    data.addEntry(Entry(set2.entryCount.toFloat(), y2.toFloat()), 1)
                    data.addEntry(Entry(set3.entryCount.toFloat(), z2.toFloat()), 2)
                } else {
                    data.addEntry(Entry(set.entryCount.toFloat(), x.toFloat()), 0)
                    data.addEntry(Entry(set2.entryCount.toFloat(), y.toFloat()), 1)
                    data.addEntry(Entry(set3.entryCount.toFloat(), z.toFloat()), 2)
                }
            })

            data.notifyDataChanged()
            chart.notifyDataSetChanged()
            chart.setMaxVisibleValueCount(150)
            chart.moveViewToX(data.entryCount.toFloat())
        }
    }

//    private fun startPlot() {
//        if (::thread.isInitialized) {
//            thread.interrupt()
//            run = false
//        }
//        thread = Thread(Runnable {
//            run = true
//            while (run) {
//                plotData = true
//                try {
//                    Thread.sleep((rate).toLong())
//                }catch (e: InterruptedException) {
//                    e.printStackTrace()
//                }
//            }
//        })
//        thread.start()
//    }

    private fun onTimer(millisUntilFinished: Long) {
        viewModel.timeMilli.value = millisUntilFinished
        tv_timer.text = (millisUntilFinished / 1000).toString()
        dbHelper.save(Accelerometer(x, y, z))

        try {
            writer.append("x: $x    y: $y    z: $z\n")
        }catch (e: Exception) {
            Toast.makeText(context, "مشکل در ذخیره سازی، متوقف شد", Toast.LENGTH_LONG).show()
            tv_timer.visibility = View.GONE
            btn_save.setImageResource(R.drawable.ic_save)
            viewModel.isOnTimer.value = false
            writer.flush()
            writer.close()
            timer.cancel()
            return
        }
    }


    override fun onResume() {
        super.onResume()
        run = true
        sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL)
        viewModel.accRTxt.observe(viewLifecycleOwner, Observer { Rtxt ->
            rate = 1000.0 / Rtxt
        })
        viewModel.accCenterFreq.observe(viewLifecycleOwner, Observer { cen ->
            center = cen
        })

        viewModel.accWidthFreq.observe(viewLifecycleOwner, Observer { w ->
            width = w
        })

        viewModel.accOrder.observe(viewLifecycleOwner, Observer { o ->
            order = o
        })

        if (viewModel.isOnTimer.value == true) {
            if (::timer.isInitialized)  timer.cancel()
            if (!::dbHelper.isInitialized)  dbHelper = DbHelperAcc(context)
            val dif = SystemClock.elapsedRealtime() - viewModel.timeNow.value!!
            val startTime = viewModel.timeMilli.value!! - dif
            timer = object : CountDownTimer(startTime, rate.toLong()) {
                override fun onFinish() {
                    viewModel.isOnTimer.value = false
                    tv_timer.visibility = View.GONE
                    btn_save.setImageResource(R.drawable.ic_save)
                    writer.flush()
                    writer.close()
                    showDialog()
                    timer.cancel()
                }
                override fun onTick(millisUntilFinished: Long) {
                    onTimer(millisUntilFinished)
                }

            }.start()
        }
//        startPlot()
    }

    override fun onPause() {
        super.onPause()
        if (::thread.isInitialized) {
            thread.interrupt()
            run = false
        }

        viewModel.timeNow.value = SystemClock.elapsedRealtime()

        sensorManager.unregisterListener(this)
    }

    override fun onStop() {
        viewModel.timeNow.value = SystemClock.elapsedRealtime()

        super.onStop()
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent?) {
//        if (plotData) {
//            addEntry(event!!)
//            plotData = false
//        }
        if (SystemClock.elapsedRealtime() - now >= rate) {
            now = SystemClock.elapsedRealtime()
            addEntry(event!!)
        }

    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        if (::thread.isInitialized) {
            thread.interrupt()
            run = false
        }
        if (::writer.isInitialized){
            writer.flush()
            writer.close()
        }
        if (::timer.isInitialized) {
            timer.cancel()
            viewModel.isOnTimer.value = false
        }
        super.onDestroy()
    }

    private fun showDialog() {
        val alertDialog: AlertDialog? = requireActivity().let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton("تایید", { dialog, id -> })
            }

            builder.setMessage("در فایل $fileName در پوشه Accelerometer ذخیره شد")
            builder.create()
        }

        alertDialog?.show()
    }

}