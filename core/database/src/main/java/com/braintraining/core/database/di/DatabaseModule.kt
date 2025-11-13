package com.braintraining.core.database.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.braintraining.core.database.BraintrainingDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Provides
    @Singleton
    fun providesNiaDatabase(
        @ApplicationContext context: Context,
    ): BraintrainingDatabase = Room.databaseBuilder(
        context,
        BraintrainingDatabase::class.java,
        "braintraining-database",
    )
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Executors.newSingleThreadExecutor().execute {
                    try {
                        val inputStream = context.assets.open("data-test.sql")
                        val sqlStatements = inputStream.bufferedReader()
                            .readText()
                            .replace(Regex("--.*"), "")
                            .split(";")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }

                        db.beginTransaction()
                        try {
                            sqlStatements.forEach { statement ->
                                try {
                                    db.execSQL(statement)
                                    Log.d("DB", "Executed: $statement")
                                } catch (e: Exception) {
                                    Log.e("DB", "Failed: $statement", e)
                                }
                            }
                            db.setTransactionSuccessful()
                        } finally {
                            db.endTransaction()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            }
        })
        .build()
}