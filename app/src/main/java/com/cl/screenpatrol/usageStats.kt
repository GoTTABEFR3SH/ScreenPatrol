package com.cl.screenpatrol

import android.content.Context
import android.os.FileObserver.DELETE
import androidx.room.*

@Database(entities = [usageStats::class, permissions::class], version = 1)
abstract class AppDataBase : RoomDatabase(){
    abstract fun usageDao(): UsageDao
    abstract fun permissionsDao(): permissionDao

    companion object{
        private var INSTANCE:AppDataBase? = null

        fun getInstance(context: Context): AppDataBase{
            if (INSTANCE == null){
                INSTANCE = Room.databaseBuilder(context,
                    AppDataBase::class.java,
                    "ScreenPatrolDb").build()
            }
            return INSTANCE!!
        }
        fun destroyInstance(){
            INSTANCE = null
        }
    }
}

@Entity
data class permissions(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "isGranted") val isGranted:Boolean?
)



@Entity
data class usageStats(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "isLimited") var limit:Boolean?,
    @ColumnInfo(name = "isLimitHit") val hit:Boolean?,
    @ColumnInfo(name = "time limit") val timeLimit:Long?,
    @ColumnInfo(name = "time used") val timeUsed:Long?



)

@Dao
interface UsageDao{
    @Insert
   suspend fun insert(usageStats: usageStats)
    @Update
    suspend fun update(usageStats: usageStats)
    @Delete
    suspend fun delete(usageStats: usageStats)
    @Query("DELETE FROM usageStats")
    suspend fun wipeData()
    @Query("SELECT * FROM usageStats WHERE id = :id ")
    suspend fun getUsageStat(id:String): usageStats
    @Query("SELECT * FROM usageStats")
    suspend fun getAll():List<usageStats>
    @Query("SELECT * FROM usageStats WHERE `time used` > `time limit`")
    suspend fun getAppsOverLimit():List<usageStats>
    @Query("SELECT * FROM usageStats WHERE isLimitHit == 0")
    suspend fun getAllAppsUnderLimit():List<usageStats>
    @Query("SELECT isLimitHit FROM usageStats WHERE id = :id")
    suspend fun isLimitHit(id: String):Boolean

}
@Dao
interface permissionDao{
    @Insert
    suspend fun insert(permissions: permissions)
    @Update
    suspend fun update(permissions: permissions)
    @Delete
    suspend fun delete(permissions: permissions)
    @Query("SELECT isGranted FROM permissions WHERE id = :id")
    suspend fun getPermission(id: String):Boolean
    @Query("SELECT * FROM permissions")
    suspend fun getAll():List<permissions>
}



