package com.example.accelerometer.view.activity

import android.Manifest
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.accelerometer.R
import com.example.accelerometer.view.fragment.DialogListFragment
import com.example.accelerometer.view.fragment.DialogSettingsFragment
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.File


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        lateinit var file: File
    }

    val REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 2005

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        nav_view.setNavigationItemSelectedListener(this)

        iv_menu.setOnClickListener {
                drawer_layout.openDrawer(Gravity.RIGHT)
            }

        file = File(Environment.getExternalStorageDirectory(),"Accelerometer")
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(WRITE_EXTERNAL_STORAGE),
                    REQUEST_STORAGE_WRITE_ACCESS_PERMISSION)

            }
        }

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_STORAGE_WRITE_ACCESS_PERMISSION) {
            if (grantResults[0] == 0 || grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (!file.exists())  {
                    val t = file.mkdir()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.nav_acc -> {
                DialogListFragment().show(supportFragmentManager, "acc")
            }
            R.id.nav_gyr -> {
                DialogListFragment().show(supportFragmentManager, "gyr")
            }
            R.id.nav_setting -> {
                DialogSettingsFragment().show(supportFragmentManager, "settings")
            }
            R.id.nav_exit -> {
                this.finish()
            }
        }
        menuItem.isCheckable = false
        drawer_layout.closeDrawer(Gravity.RIGHT)
        return true
    }
}