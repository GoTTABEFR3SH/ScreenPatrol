package com.cl.screenpatrol

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update

@Database(entities = [usageStats::class], version = 1)
abstract class AppDataBase : RoomDatabase(){
    abstract fun usageDao(): UsageDao
}




@Entity
data class usageStats(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "test") val testing:String?
)

@Dao
interface UsageDao{
    @Insert
   suspend fun insert(usageStats: usageStats)
    @Update
    fun update(usageStats: usageStats)
    @Delete
    fun delete(usageStats: usageStats)
    @Query("SELECT test FROM usageStats WHERE id = :id ")
    suspend fun getUsageStat(id:String): String

}



