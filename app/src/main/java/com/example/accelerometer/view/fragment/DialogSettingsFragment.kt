package com.example.accelerometer.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.example.accelerometer.R
import com.example.accelerometer.internal.ViewModelsFactory
import com.example.accelerometer.internal.ViewPagerAdapter
import com.example.accelerometer.view.activity.MainActivity
import com.example.accelerometer.viewModel.MainActivityViewModel
import kotlinx.android.synthetic.main.fragment_dialog_settings.*


class DialogSettingsFragment : DialogFragment() {

    private val fragmentsTitle = listOf( "ژیروسکوپ", "شتاب")

    private val settingFragment = SettingFragment()
    private val settingFragment2 = SettingFragment()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)

        return inflater.inflate(R.layout.fragment_dialog_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragments = listOf(settingFragment, settingFragment2)
        val viewPagerAdapter = ViewPagerAdapter(childFragmentManager, fragments, fragmentsTitle)
        view_pager.adapter = viewPagerAdapter
        view_pager.offscreenPageLimit = fragments.size
        view_pager.currentItem = 1
        tab_layout.setupWithViewPager(view_pager)
        val root = tab_layout.getChildAt(0)
        if (root is LinearLayout) {
            root.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
            root.dividerPadding = 10
        }

        btn_close.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}