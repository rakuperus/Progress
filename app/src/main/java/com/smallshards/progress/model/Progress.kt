package com.smallshards.progress.model

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Dao
interface ProgressDao {

    @Query("SELECT * FROM progress ORDER BY date")
    fun getAllProgress(): LiveData<List<Progress>>

    @Insert
    suspend fun insert(item : Progress)

    @Query ("DELETE FROM progress")
    fun deletAll()

}

@Database(entities = arrayOf(Progress::class), version = 1)
abstract class ProgressDatabase : RoomDatabase() {
    abstract fun progressDao() : ProgressDao

    companion object {
        @Volatile
        private var Instance : ProgressDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope)  :ProgressDatabase {

            return Instance ?: synchronized(this) {

                val inst = Room.databaseBuilder(
                    context.applicationContext,
                    ProgressDatabase::class.java,
                    "Progress_Database"
                )
                    .addCallback(ProgressDatabaseCallback(scope))
                    .build()

                Instance = inst

                inst
            }

        }
    }

    private class ProgressDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            Instance?.let { 
                database -> scope.launch {
                    populateDatabase(database.progressDao())
                }
            }
        }

        suspend fun populateDatabase(dao: ProgressDao) {
            dao.deletAll()

            dao.insert(Progress(1, 200000000, 7))
            dao.insert(Progress(2, 200003421, 3))
            dao.insert(Progress(3, 347840000, 4))
        }
    }
}


class ProgressRepository (private val progressDao: ProgressDao){

    val allProgress = progressDao.getAllProgress()

    @WorkerThread
    suspend fun insert(item : Progress) {
        progressDao.insert(item)
    }

}

@Entity(tableName = "progress")
data class Progress(
    @PrimaryKey @ColumnInfo(name="id")
    val id : Long,

    @ColumnInfo(name = "date")
    val dateTime : Long,

    @ColumnInfo(name = "val")
    val progressValue : Long
)