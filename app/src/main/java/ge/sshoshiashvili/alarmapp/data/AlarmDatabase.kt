package ge.sshoshiashvili.alarmapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ge.sshoshiashvili.alarmapp.data.dao.AlarmItemDao
import ge.sshoshiashvili.alarmapp.data.entity.AlarmItem

@Database(entities = [AlarmItem::class], version = 1)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmItemDao(): AlarmItemDao

    companion object {
        private const val dbName = "alarm_db"

        @Volatile
        private var INSTANCE: AlarmDatabase? = null

        fun getInstance(context: Context): AlarmDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AlarmDatabase::class.java,
                    dbName
                ).build()
                INSTANCE = instance
                instance
            }
        }

    }
}