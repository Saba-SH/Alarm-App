package ge.sshoshiashvili.alarmapp.data

import androidx.lifecycle.LiveData
import ge.sshoshiashvili.alarmapp.data.dao.AlarmItemDao
import ge.sshoshiashvili.alarmapp.data.entity.AlarmItem

class AlarmRepositoryImpl(
    private val alarmItemDao: AlarmItemDao
) : AlarmRepository {

    private val allAlarmItems: LiveData<List<AlarmItem>> = alarmItemDao.getAllAlarmItems()

    override fun getAllAlarmItems(): LiveData<List<AlarmItem>> {
        return allAlarmItems
    }

    override suspend fun getAlarmItemById(id: Long): AlarmItem? {
        return alarmItemDao.getAlarmItemById(id)
    }

    override suspend fun addAlarmItem(item: AlarmItem): Long {
        return alarmItemDao.addAlarmItem(item)
    }

    override suspend fun updateAlarmItem(item: AlarmItem) {
        alarmItemDao.updateAlarmItem(item)
    }

    override suspend fun deleteAlarmItem(item: AlarmItem) {
        alarmItemDao.deleteAlarmItem(item)
    }
}