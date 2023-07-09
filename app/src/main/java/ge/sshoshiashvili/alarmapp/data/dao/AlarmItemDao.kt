package ge.sshoshiashvili.alarmapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import ge.sshoshiashvili.alarmapp.data.entity.AlarmItem

@Dao
interface AlarmItemDao {

    @Query("SELECT * FROM AlarmItem")
    fun getAllAlarmItems(): LiveData<List<AlarmItem>>

    @Query("SELECT * FROM AlarmItem WHERE id = :id")
    fun getAlarmItemById(id: Long): AlarmItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addAlarmItem(item: AlarmItem): Long

    @Update
    fun updateAlarmItem(item: AlarmItem)

    @Delete
    fun deleteAlarmItem(item: AlarmItem)

}