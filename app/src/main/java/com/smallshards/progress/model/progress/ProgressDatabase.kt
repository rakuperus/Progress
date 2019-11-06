package com.smallshards.progress.model.progress

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.smallshards.progress.extension.addDays
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*


@Dao
interface ProgressDao {

    @Query("SELECT * FROM progress ORDER BY timeMillis ")
    fun getAllProgress(): LiveData<List<Progress>>

    @Insert
    suspend fun insert(item: Progress)

    @Query("DELETE FROM progress")
    suspend fun deleteAll()

}

@Database(entities = [Progress::class], version = 6)
abstract class ProgressDatabase : RoomDatabase() {
    abstract fun progressDao(): ProgressDao

    companion object {
        @Volatile
        private var Instance: ProgressDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): ProgressDatabase {

            return Instance ?: synchronized(this) {

                val inst = Room.databaseBuilder(
                    context.applicationContext,
                    ProgressDatabase::class.java,
                    "Progress_Database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(
                        ProgressDatabaseCallback(
                            scope
                        )
                    )
                    .build()

                Instance = inst

                inst
            }

        }

        private class ProgressDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                Instance?.let { database ->
                    scope.launch {
                        populateDatabase(database.progressDao())
                    }
                }
            }
        }

        suspend fun populateDatabase(dao: ProgressDao) {
            dao.deleteAll()

            dao.insert(Progress(Date().addDays(-9).time, 75))
            dao.insert(Progress(Date().addDays(-7).time, 38))
            dao.insert(Progress(Date().addDays(-5).time, 42))
            dao.insert(Progress(Date().addDays(-4).time, 51))
            dao.insert(Progress(Date().addDays(-4).time, -10))
            dao.insert(Progress(Date().addDays(-4).time, 87))
            dao.insert(Progress(Date().addDays(-4).time, 12))
            dao.insert(Progress(Date().addDays(-2).time, 103))
            dao.insert(Progress(Date().addDays(-1).time, 66))
            dao.insert(Progress(Date().addDays(-1).time, 48))
        }
    }
}

