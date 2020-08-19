package com.example.accelerometer.internal

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteStatement
import android.util.Log
import com.example.accelerometer.model.Accelerometer

class DbHelperAcc (context: Context?) : SQLiteOpenHelper(context,
    DATABASE_NAME, null, 1) {
    companion object{
        const val DATABASE_NAME = "accelerometer.db"
        const val TABLE_NAME = "accelerometer"
        const val COLUMN_X = "x"
        const val COLUMN_Y = "y"
        const val COLUMN_Z = "z"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            "create table " + "$TABLE_NAME ($COLUMN_X text not null, " +
                    "$COLUMN_Y text not null, $COLUMN_Z text not null, primary key($COLUMN_X, $COLUMN_Y, $COLUMN_Z))"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun save(acc : Accelerometer): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_X, acc.x.toString())
        contentValues.put(COLUMN_Y, acc.y.toString())
        contentValues.put(COLUMN_Z, acc.z.toString())
        return db.insert(TABLE_NAME, null, contentValues) > 0
    }

    fun getAll(): ArrayList<Accelerometer> {
        val array = ArrayList<Accelerometer>()
        val db = this.readableDatabase
        val c = db.rawQuery("select * from $TABLE_NAME", null)
        c.moveToFirst()
        while (!c.isAfterLast) {
            val x = c.getString(c.getColumnIndex(COLUMN_X))
            val y = c.getString(c.getColumnIndex(COLUMN_Y))
            val z = c.getString(c.getColumnIndex(COLUMN_Z))
            val acc = Accelerometer(x.toDouble(), y.toDouble(), z.toDouble())
            array.add(acc)
            c.moveToNext()
        }
        c.close()
        return array
    }

    fun deleteAcc(x: Double, y: Double, z: Double): Boolean {
        val db = this.writableDatabase
        return db.delete(
            TABLE_NAME,
            "$COLUMN_X = ? AND $COLUMN_Y = ? AND $COLUMN_Z = ?",
            arrayOf(x.toString(), y.toString(), z.toString())
        ) > 0

    }

}