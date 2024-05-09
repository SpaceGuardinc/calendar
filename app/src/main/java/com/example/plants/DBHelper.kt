package com.example.plants

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.plants.model.SelectedData
import com.example.plants.model.Vegetable

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // LiveData для списка овощей
    private val vegetablesLiveData = MutableLiveData<List<Vegetable>>()

    // Получение LiveData для списка овощей
    fun getVegetablesLiveData(): LiveData<List<Vegetable>> {
        return vegetablesLiveData
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d("DBHelper", "Creating database tables...")
        val CREATE_TABLE_VEGETABLES = ("CREATE TABLE " + TABLE_VEGETABLES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_IS_FAVORITE + " INTEGER DEFAULT 0" + ")")
        db.execSQL(CREATE_TABLE_VEGETABLES)

        val CREATE_TABLE_ACTIONS = ("CREATE TABLE " + TABLE_ACTIONS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_VEGETABLE_ID + " INTEGER,"
                + COLUMN_ACTION_NAME + " TEXT,"
                + COLUMN_DATE + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_VEGETABLE_ID + ") REFERENCES " + TABLE_VEGETABLES + "(" + COLUMN_ID + ")" + ")")
        db.execSQL(CREATE_TABLE_ACTIONS)

        val CREATE_TABLE_CALENDAR = ("CREATE TABLE " + TABLE_CALENDAR + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_DATE + " TEXT,"
                + COLUMN_WORK_TYPE + " TEXT" + ")")
        db.execSQL(CREATE_TABLE_CALENDAR)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ACTIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CALENDAR")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_VEGETABLES")
        onCreate(db)
    }

    fun addSelectedDate(date: String, workType: String): Long {
        Log.d("DBHelper", "Adding selected date: $date, work type: $workType")
        val values = ContentValues().apply {
            put(COLUMN_DATE, date)
            put(COLUMN_WORK_TYPE, workType)
        }
        val db = this.writableDatabase
        val id = db.insertWithOnConflict(TABLE_CALENDAR, null, values, SQLiteDatabase.CONFLICT_IGNORE)
        db.close()
        return id
    }

    @SuppressLint("Range")
    fun getSelectedDates(): List<String> {
        Log.d("DBHelper", "Getting selected dates from database...")
        val selectedDates = mutableListOf<String>()
        val selectQuery = "SELECT $COLUMN_DATE FROM $TABLE_CALENDAR"
        val db = this.readableDatabase
        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: Exception) {
            Log.e("DBHelper", "Error while trying to get selected dates from database", e)
            return selectedDates
        }

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE))
                selectedDates.add(date)
            } while (cursor.moveToNext())
        }

        cursor?.close()
        db.close()
        return selectedDates
    }

    @SuppressLint("Range")
    fun getSelectedWorkTypes(): List<String> {
        val selectedWorkTypes = mutableListOf<String>()
        val selectQuery = "SELECT $COLUMN_WORK_TYPE FROM $TABLE_CALENDAR"
        val db = this.readableDatabase
        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: Exception) {
            Log.e("DBHelper", "Error while trying to get selected work types from database", e)
            return selectedWorkTypes
        }

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val workType = cursor.getString(cursor.getColumnIndex(COLUMN_WORK_TYPE))
                selectedWorkTypes.add(workType)
            } while (cursor.moveToNext())
        }

        cursor?.close()
        db.close()
        return selectedWorkTypes
    }

    fun insertVegetable(name: String): Long {
        val values = ContentValues()
        values.put(COLUMN_NAME, name)
        val db = this.writableDatabase
        val id = db.insert(TABLE_VEGETABLES, null, values)
        db.close()
        // Обновление LiveData после добавления овоща
        updateVegetablesLiveData()
        return id
    }

    fun insertAction(vegetableId: Long, actionName: String, date: String) {
        val values = ContentValues()
        values.put(COLUMN_VEGETABLE_ID, vegetableId)
        values.put(COLUMN_ACTION_NAME, actionName)
        values.put(COLUMN_DATE, date)
        val db = this.writableDatabase
        db.insert(TABLE_ACTIONS, null, values)
        db.close()
    }


    fun deleteVegetable(vegetable: Vegetable) {
        val db = this.writableDatabase
        db.delete(TABLE_VEGETABLES, "$COLUMN_ID = ?", arrayOf(vegetable.id.toString()))
        db.close()
        // Обновление LiveData после удаления овоща
        updateVegetablesLiveData()
    }

    fun isVegetableFavorite(vegetableId: Long): Boolean {
        val db = this.readableDatabase
        val query = "SELECT $COLUMN_IS_FAVORITE FROM $TABLE_VEGETABLES WHERE $COLUMN_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(vegetableId.toString()))
        var isFavorite = false
        cursor.use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(COLUMN_IS_FAVORITE)
                if (columnIndex != -1) {
                    isFavorite = cursor.getInt(columnIndex) == 1
                }
            }
        }
        cursor?.close()
        return isFavorite
    }

    fun updateVegetableIsFavorite(vegetableId: Long, isFavorite: Boolean) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_IS_FAVORITE, if (isFavorite) 1 else 0)
        }
        db.update(TABLE_VEGETABLES, values, "$COLUMN_ID = ?", arrayOf(vegetableId.toString()))
        db.close()
        Log.d("DBHelper", "Vegetable $vegetableId favorite status updated to $isFavorite")
        // Обновление LiveData после обновления овоща
        updateVegetablesLiveData()
    }

    fun deleteSelectedDate(date: String) {
        val db = this.writableDatabase
        db.delete(TABLE_CALENDAR, "$COLUMN_DATE = ?", arrayOf(date))
        db.close()
    }

    // Метод для обновления LiveData
    private fun updateVegetablesLiveData() {
        vegetablesLiveData.postValue(getAllVegetables())
    }

    @SuppressLint("Range")
    fun getAllVegetables(): List<Vegetable> {
        val vegetables = mutableListOf<Vegetable>()
        val selectQuery = "SELECT  * FROM $TABLE_VEGETABLES ORDER BY $COLUMN_IS_FAVORITE DESC"
        val db = this.readableDatabase
        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: Exception) {
            Log.e("DBHelper", "Error while trying to get vegetables from database", e)
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id: Long
        var name: String

        if (cursor != null && cursor.moveToFirst()) {
            do {
                id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID))
                name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
                val vegetable = Vegetable(id, name, isFavorite = false)
                vegetables.add(vegetable)
                Log.d("DBHelper", "Vegetable: $id, $name")
            } while (cursor.moveToNext())
        }

        cursor?.close()
        db.close()
        return vegetables
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "plants.db"
        private const val TABLE_VEGETABLES = "vegetables"
        private const val TABLE_ACTIONS = "actions"
        private const val TABLE_CALENDAR = "calendar"
        private const val COLUMN_ID = "_id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_VEGETABLE_ID = "vegetable_id"
        private const val COLUMN_ACTION_NAME = "action_name"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_IS_FAVORITE = "is_favorite"
        private const val COLUMN_WORK_TYPE = "work_type"

        @Volatile
        private var INSTANCE: DBHelper? = null

        fun getInstance(context: Context): DBHelper {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = DBHelper(context.applicationContext)
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
