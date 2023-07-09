package ge.sshoshiashvili.alarmapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AlarmItem(
    var hour: Int,
    var minute: Int,
    var active: Boolean,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null

    override fun toString(): String {
        return super.toString() + " Time: " + hour.toString() + ":" + minute.toString() +
                ", Active: " + active.toString() + ", id: " + id.toString()
    }
}
