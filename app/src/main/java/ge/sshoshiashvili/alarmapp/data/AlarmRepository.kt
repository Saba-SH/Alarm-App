package ge.sshoshiashvili.alarmapp.data

import androidx.lifecycle.LiveData
import ge.sshoshiashvili.alarmapp.data.entity.AlarmItem

interface AlarmRepository {

    fun getAllAlarmItems(): LiveData<List<AlarmItem>>

    suspend fun getAlarmItemById(id: Long): AlarmItem?

    suspend fun addAlarmItem(item: AlarmItem): Long

    suspend fun updateAlarmItem(item: AlarmItem)

    suspend fun deleteAlarmItem(item: AlarmItem)

}