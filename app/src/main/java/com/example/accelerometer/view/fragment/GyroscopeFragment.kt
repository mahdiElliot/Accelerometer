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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.accelerometer.R
import com.example.accelerometer.internal.DbHelperGyr
import com.example.accelerometer.internal.ViewModelsFactory
import com.example.accelerometer.model.Gyroscope
import com.example.accelerometer.view.activity.MainActivity
import com.example.accelerometer.viewModel.MainActivityViewModel
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.fragment_gyroscope.*
import kotlinx.android.synthetic.main.fragment_gyroscope.btn_down
import kotlinx.android.synthetic.main.fragment_gyroscope.btn_save
import kotlinx.android.synthetic.main.fragment_gyroscope.btn_submit
import kotlinx.android.synthetic.main.fragment_gyroscope.btn_up
import kotlinx.android.synthetic.main.fragment_gyroscope.chart
import kotlinx.android.synthetic.main.fragment_gyroscope.et_rate
import kotlinx.android.synthetic.main.fragment_gyroscope.tv_timer
import uk.me.berndporr.iirj.Butterworth
import java.io.File
import java.io.FileWriter
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class GyroscopeFragment : Fragment(), SensorEventListener {

    lateinit var gyroscope: Sensor
    lateinit var sensorManager: SensorManager

    lateinit var viewModel: MainActivityViewModel

    var x = 0.0
    var y = 0.0
    var z = 0.0
    lateinit var thread: Thread
    var plotData = true
    var run = true
    var rate = 1000.0 / 1
    val butterWorth = Butterworth()
    var center = 0.5
    var width = 100.0
    var order = 2

    lateinit var timer: CountDownTimer

    lateinit var writer: FileWriter
    lateinit var dbHelper: DbHelperGyr

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel = ViewModelProviders.of(requireActivity(), ViewModelsFactory()).get(
            MainActivityViewModel::class.java)
        return inflater.inflate(R.layout.fragment_gyroscope, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        et_rate.setText(viewModel.gyroRTxt.value.toString())
        rate = 1000.0 / viewModel.gyroRTxt.value!!
        center = viewModel.gyroCenterFreq.value!!
        width = viewModel.gyroWidthFreq.value!!
        order = viewModel.gyroOrder.value!!
        run = true

        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)

        if (::gyroscope.isInitialized)
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL)

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
        leftAxis.axisMaximum = 10f
        leftAxis.axisMinimum = -3f

        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false
        chart.axisLeft.setDrawGridLines(false)
        chart.xAxis.setDrawGridLines(false)
        chart.setDrawBorders(false)
        startPlot()


        btn_up.setOnClickListener {
            leftAxis.axisMaximum += 2
            leftAxis.axisMinimum += 2
        }

        btn_down.setOnClickListener {
            leftAxis.axisMaximum -= 2
            leftAxis.axisMinimum -= 2
        }

        btn_submit.setOnClickListener {
            val r = et_rate.text.toString().toInt()
            viewModel.gyroRTxt.value = r
            rate = 1000.0 / r
            startPlot()
        }

        btn_acc.setOnClickListener {
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
            requireActivity().onBackPressed()
        }

        btn_save.setOnClickListener {
            dbHelper = DbHelperGyr(context)
//            if (dbHelper.save(Accelerometer(x, y, z))) {
//                val alertDialog: AlertDialog? = requireActivity().let {
//                    val builder = AlertDialog.Builder(it)
//                    builder.apply {
//                        setPositiveButton("تایید", { dialog, id ->
//                            Toast.makeText(context, "ذخیره شد", Toast.LENGTH_LONG).show()
//                        })
//                    }
//
//                    builder.setMessage("x: $x  y: $y  z: $z")
//                    builder.create()
//                }
//
//                alertDialog?.show()
//            }
            if (viewModel.isOnTimer.value == true) {
                timer.cancel()
                viewModel.isOnTimer.value = false
                tv_timer.visibility = View.GONE
                btn_save.setImageResource(R.drawable.ic_save)
            } else {
                btn_save.setImageResource(R.drawable.ic_close_white)
                tv_timer.text = viewModel.gyroSaveTime.value.toString()
                tv_timer.visibility = View.VISIBLE
                val formatter = SimpleDateFormat("yyyy_MM_dd")
                val now = Date()
                val fileName = formatter.format(now)
                val f = File(MainActivity.file, "gyro_${fileName}_${now.time}.txt")
                f.createNewFile()
                writer = FileWriter(f)
                if (::timer.isInitialized) timer.cancel()
                viewModel.isOnTimer.value = true
                val startTime = viewModel.gyroSaveTime.value!! * 1000
                timer = object : CountDownTimer(startTime.toLong(), 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        onTimer(millisUntilFinished)
                    }

                    override fun onFinish() {
                        viewModel.isOnTimer.value = false
                        tv_timer.visibility = View.GONE
                        btn_save.setImageResource(R.drawable.ic_save)
                        writer.flush()
                        writer.close()
                        timer.cancel()
                    }
                }.start()
            }

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
            butterWorth.bandPass(order, rate, center, width)
            val x2 = butterWorth.filter(x)
            butterWorth.bandPass(order, rate, center, width)
            val y2 = butterWorth.filter(y)
            butterWorth.bandPass(order, rate, center, width)
            val z2 = butterWorth.filter(z)
            data.addEntry(Entry(set.entryCount.toFloat(), x2.toFloat()), 0)
            data.addEntry(Entry(set2.entryCount.toFloat(), y2.toFloat()), 1)
            data.addEntry(Entry(set3.entryCount.toFloat(), z2.toFloat()), 2)
            data.notifyDataChanged()
            chart.notifyDataSetChanged()
            chart.setMaxVisibleValueCount(150)
            chart.moveViewToX(data.entryCount.toFloat())
        }
    }

    private fun startPlot() {
        if (::thread.isInitialized) {
            thread.interrupt()
            run = false
        }
        thread = Thread(Runnable {
            run = true
            while (run) {
                plotData = true
                try {
                    Thread.sleep((rate).toLong())
                }catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        })
        thread.start()
    }



    override fun onResume() {
        super.onResume()
        run = true
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL)
        viewModel.gyroSubmitSetting.observe(viewLifecycleOwner, Observer {
            if (it) {
                viewModel.gyroRTxt.observe(viewLifecycleOwner, Observer { Rtxt ->
                    et_rate.setText(Rtxt.toString())
                    rate = 1000.0 / Rtxt
                })
                viewModel.gyroCenterFreq.observe(viewLifecycleOwner, Observer { cen ->
                    center = cen
                })

                viewModel.gyroWidthFreq.observe(viewLifecycleOwner, Observer { w ->
                    width = w
                })

                viewModel.gyroOrder.observe(viewLifecycleOwner, Observer { o ->
                    order = o
                })
            }
        })

        if (viewModel.isOnTimer.value == true) {
            if (::timer.isInitialized)  timer.cancel()
            if (!::dbHelper.isInitialized)  dbHelper = DbHelperGyr(context)
            val dif = SystemClock.elapsedRealtime() - viewModel.timeNow.value!!
            val startTime = viewModel.timeMilli.value!! - dif
            timer = object : CountDownTimer(startTime, 1000) {
                override fun onFinish() {
                    viewModel.isOnTimer.value = false
                    tv_timer.visibility = View.GONE
                    btn_save.setImageResource(R.drawable.ic_save)
                    writer.flush()
                    writer.close()
                    timer.cancel()
                }
                override fun onTick(millisUntilFinished: Long) {
                    onTimer(millisUntilFinished)
                }
            }.start()
        }

        startPlot()
    }

    private fun onTimer(millisUntilFinished: Long) {
        viewModel.timeMilli.value = millisUntilFinished
        tv_timer.text = (millisUntilFinished / 1000).toString()
        dbHelper.save(Gyroscope(x, y, z))

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

    override fun onPause() {
        super.onPause()
        if (::thread.isInitialized) {
            thread.interrupt()
            run = false
        }
        sensorManager.unregisterListener(this)

        viewModel.timeNow.value = SystemClock.elapsedRealtime()
        if (::timer.isInitialized) {
            timer.cancel()
            viewModel.isOnTimer.value = false
        }
    }

    override fun onStop() {
        viewModel.timeNow.value = SystemClock.elapsedRealtime()
        if (::timer.isInitialized) {
            timer.cancel()
            viewModel.isOnTimer.value = false
        }
        super.onStop()
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

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent?) {
        if (plotData) {
            addEntry(event!!)
            plotData = false
        }
    }

}