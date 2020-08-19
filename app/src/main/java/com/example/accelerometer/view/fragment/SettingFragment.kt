package com.example.accelerometer.view.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProviders
import com.example.accelerometer.R
import com.example.accelerometer.internal.ViewModelsFactory
import com.example.accelerometer.view.activity.MainActivity
import com.example.accelerometer.viewModel.MainActivityViewModel
import kotlinx.android.synthetic.main.fragment_setting.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SettingFragment : Fragment() {

    lateinit var viewModel: MainActivityViewModel
    var t = '0'

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel = ViewModelProviders.of(requireActivity(), ViewModelsFactory()).get(MainActivityViewModel::class.java)
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = ArrayList<Int>()
        list.add(1)
        list.add(2)
        val dataAdapter = ArrayAdapter<Int>(requireContext(), android.R.layout.simple_spinner_item, list)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        et_order.adapter = dataAdapter
        if (t == '1') {
            et_rate.setText(viewModel.accRTxt.value.toString())
            et_center_freq.setText(viewModel.accCenterFreq.value.toString())
            et_width_freq.setText(viewModel.accWidthFreq.value.toString())
            et_order.setSelection(viewModel.accOrder.value!! - 1)
            et_time.setText(viewModel.accSaveTime.value.toString())
        }else {
            et_rate.setText(viewModel.gyroRTxt.value.toString())
            et_center_freq.setText(viewModel.gyroCenterFreq.value.toString())
            et_width_freq.setText(viewModel.gyroWidthFreq.value.toString())
            et_order.setSelection(viewModel.gyroOrder.value!! - 1)
            et_time.setText(viewModel.gyroSaveTime.value.toString())
        }

        btn_save.setOnClickListener {
            if (et_time.text.toString().toInt() >= 40) {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("زمان وارد شده بزرگ است امکان پرشدن حافظه وجود دارد، آیا ادامه میدهید؟")
                builder.setCancelable(true)
                builder.setPositiveButton("بله") { _, _ ->
                    save()
                }
                builder.setNegativeButton("خیر") { dialog, _ -> dialog.cancel() }
                builder.create().show()
            } else
                save()
        }

    }

    override fun onResume() {
        super.onResume()
        t = this.tag!![this.tag!!.length - 1]
        if (t == '1') {
            et_rate.setText(viewModel.accRTxt.value.toString())
            et_center_freq.setText(viewModel.accCenterFreq.value.toString())
            et_width_freq.setText(viewModel.accWidthFreq.value.toString())
            et_order.setSelection(viewModel.accOrder.value!! - 1)
            et_time.setText(viewModel.accSaveTime.value.toString())
        }else {
            et_rate.setText(viewModel.gyroRTxt.value.toString())
            et_center_freq.setText(viewModel.gyroCenterFreq.value.toString())
            et_width_freq.setText(viewModel.gyroWidthFreq.value.toString())
            et_order.setSelection(viewModel.gyroOrder.value!! - 1)
            et_time.setText(viewModel.gyroSaveTime.value.toString())
        }
    }

    private fun save() {
        if (t == '1') {//accelerometer
            viewModel.accRTxt.value = et_rate.text.toString().toInt()
            viewModel.accCenterFreq.value = et_center_freq.text.toString().toDouble()
            viewModel.accWidthFreq.value = et_width_freq.text.toString().toDouble()
            viewModel.accOrder.value = et_order.selectedItemPosition + 1
            viewModel.accSaveTime.value = et_time.text.toString().toInt()
            viewModel.accSubmitSetting.value = switch_submit.isChecked
        } else {  //gyroscope
            viewModel.gyroRTxt.value = et_rate.text.toString().toInt()
            viewModel.gyroCenterFreq.value = et_center_freq.text.toString().toDouble()
            viewModel.gyroWidthFreq.value = et_width_freq.text.toString().toDouble()
            viewModel.gyroOrder.value = et_order.selectedItemPosition + 1
            viewModel.gyroSaveTime.value = et_time.text.toString().toInt()
            viewModel.gyroSubmitSetting.value = switch_submit.isChecked
        }

        Toast.makeText(context, "ذخیره شد", Toast.LENGTH_LONG).show()
    }
}