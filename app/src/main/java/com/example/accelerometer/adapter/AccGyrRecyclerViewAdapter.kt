package com.example.accelerometer.adapter

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.accelerometer.R
import com.example.accelerometer.internal.DbHelperAcc
import com.example.accelerometer.internal.DbHelperGyr
import com.example.accelerometer.model.Accelerometer
import com.example.accelerometer.model.Gyroscope
import kotlinx.android.synthetic.main.item_acc_gyr.view.*

class AccGyrRecyclerViewAdapter(

): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        val TYPE_ACCELEROMETER = 1
        val TYPE_GYROSCOPE = 2
    }

    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return if (oldItem is Accelerometer && newItem is Accelerometer)
                oldItem.x == newItem.x && oldItem.y == newItem.y && oldItem.z == newItem.z
            else if (oldItem is Gyroscope && newItem is Gyroscope)
                oldItem.x == newItem.x && oldItem.y == newItem.y && oldItem.z == newItem.z
            else
                false
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return if (oldItem is Accelerometer && newItem is Accelerometer)
                oldItem.x == newItem.x && oldItem.y == newItem.y && oldItem.z == newItem.z
            else if (oldItem is Gyroscope && newItem is Gyroscope)
                oldItem.x == newItem.x && oldItem.y == newItem.y && oldItem.z == newItem.z
            else
                false
        }

    }

    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return AccGyrViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_acc_gyr,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: ArrayList<Any>) {
        differ.submitList(list)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is AccGyrViewHolder -> {
                holder.bind(differ.currentList[position], this)
            }
        }
    }

    class AccGyrViewHolder
    constructor(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: Any, adapter: AccGyrRecyclerViewAdapter) = with(itemView) {
            if (item is Accelerometer) {
                tv_info.text = "x: ${item.x}\ny: ${item.y}\nz: ${item.z}"
                btn_delete.setOnClickListener {
                    val builder = AlertDialog.Builder(context)
                    builder.setMessage("آیا از حذف اطمینان دارید ؟‌")
                    builder.setCancelable(true)
                    builder.setPositiveButton("بله") { _, _ ->
                        val cp = ArrayList<Accelerometer>()
                        cp.addAll(adapter.differ.currentList as Collection<Accelerometer>)
                        if (cp.remove(item)) {
                            val db = DbHelperAcc(context)
                            if (db.deleteAcc(item.x, item.y, item.z)) {
                                adapter.submitList(cp as ArrayList<Any>)
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                    builder.setNegativeButton("خیر") { dialog, _ -> dialog.cancel() }

                    builder.create().show()
                }
            }else if (item is Gyroscope) {
                tv_info.text = "x: ${item.x}\n$y: ${item.y}\nz: ${item.z}"
                btn_delete.setOnClickListener {
                    val builder = AlertDialog.Builder(context)
                    builder.setMessage("آیا از حذف اطمینان دارید ؟‌")
                    builder.setCancelable(true)
                    builder.setPositiveButton("بله") { _, _ ->
                        val cp = ArrayList<Gyroscope>()
                        cp.addAll(adapter.differ.currentList as Collection<Gyroscope>)
                        if (cp.remove(item)) {
                            val db = DbHelperGyr(context)
                            if (db.deleteGyr(item.x, item.y, item.z)) {
                                adapter.submitList(cp as ArrayList<Any>)
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                    builder.setNegativeButton("خیر") { dialog, _ -> dialog.cancel() }

                    builder.create().show()
                }
            }


        }
    }

}