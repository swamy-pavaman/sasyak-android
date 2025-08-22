package com.kapilagro.sasyak.data.db
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kapilagro.sasyak.data.db.converters.DateConverters
import com.kapilagro.sasyak.data.db.converters.ForecastConverters
import com.kapilagro.sasyak.data.db.dao.TaskDao
import com.kapilagro.sasyak.data.db.dao.NotificationDao
import com.kapilagro.sasyak.data.db.dao.PreviewDao
import com.kapilagro.sasyak.data.db.dao.UserDao
import com.kapilagro.sasyak.data.db.dao.WeatherDao
import com.kapilagro.sasyak.data.db.dao.WorkerDao
import com.kapilagro.sasyak.data.db.entities.TaskEntity
import com.kapilagro.sasyak.data.db.entities.NotificationEntity
import com.kapilagro.sasyak.data.db.entities.PreviewEntity
import com.kapilagro.sasyak.data.db.entities.UserEntity
import com.kapilagro.sasyak.data.db.entities.WeatherEntity
import com.kapilagro.sasyak.data.db.entities.WorkJobEntity

@Database(
    entities = [
        TaskEntity::class,
        NotificationEntity::class,
        UserEntity::class,
        WeatherEntity::class,
        PreviewEntity::class,
        WorkJobEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(DateConverters::class, ForecastConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun notificationDao(): NotificationDao
    abstract fun userDao(): UserDao
    abstract fun weatherDao(): WeatherDao
    abstract fun previewDao(): PreviewDao
    abstract fun workerDao(): WorkerDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sasyak_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}