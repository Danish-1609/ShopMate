package com.shopmate.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shopmate.data.dao.*
import com.shopmate.data.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Product::class,
        Sale::class,
        SaleTransaction::class,
        Category::class,
        User::class,
        RestockAlert::class
    ],
    version = 1,
    exportSchema = true
)
abstract class ShopDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun userDao(): UserDao
    abstract fun restockAlertDao(): RestockAlertDao

    companion object {
        @Volatile
        private var INSTANCE: ShopDatabase? = null

        fun getDatabase(context: Context): ShopDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ShopDatabase::class.java,
                    "shopmate_database"
                )
                    .addCallback(ShopDatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }

    private class ShopDatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDefaultCategories(database)
                }
            }
        }

        private suspend fun populateDefaultCategories(database: ShopDatabase) {
            // Default categories are now created via string resources - nothing to seed here
        }
    }
}
