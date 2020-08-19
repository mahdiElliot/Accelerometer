package com.example.accelerometer.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.accelerometer.R
import com.example.accelerometer.adapter.AccGyrRecyclerViewAdapter
import com.example.accelerometer.internal.DbHelperAcc
import com.example.accelerometer.internal.DbHelperGyr
import kotlinx.android.synthetic.main.fragment_dialog_list.*

class DialogListFragment : DialogFragment() {

    lateinit var recyclerViewAdapter: AccGyrRecyclerViewAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return inflater.inflate(R.layout.fragment_dialog_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_close.setOnClickListener {
            dismiss()
        }

        recyclerViewAdapter =
            AccGyrRecyclerViewAdapter()
        if (this.tag == "acc") {
            val db = DbHelperAcc(context)
            val accs = db.getAll()
            rv_list.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                adapter = this@DialogListFragment.recyclerViewAdapter
                this@DialogListFragment.recyclerViewAdapter.submitList(accs as ArrayList<Any>)
            }
        } else {
            val db = DbHelperGyr(context)
            val gyr = db.getAll()
            rv_list.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                adapter = this@DialogListFragment.recyclerViewAdapter
                this@DialogListFragment.recyclerViewAdapter.submitList(gyr as ArrayList<Any>)
            }
        }

    }

    override fun onStart() {
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        super.onStart()
    }
}