package com.mock.musictpn.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mock.musictpn.datasource.local.AppDatabase
import com.mock.musictpn.datasource.local.dao.TrackDao
import com.mock.musictpn.datasource.network.ApiContract.BASE_URL
import com.mock.musictpn.datasource.network.IMusicService
import com.mock.musictpn.mediaplayer.MusicPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideHandler() = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }

    @Singleton
    @Provides
    fun provideIOScope(handler: CoroutineExceptionHandler) = CoroutineScope(
        Dispatchers.IO +
                SupervisorJob() +
                CoroutineName("IO_Scope") +
                handler
    )

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").setLenient().create()
    }

    @Provides
    @Singleton
    fun provideHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder()
            .connectTimeout(300, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS).addInterceptor(logging).build()
    }

    @Provides
    @Singleton
    fun provideApiService(gson: Gson, client: OkHttpClient): IMusicService {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(BASE_URL)
            .client(client)
            .build()
            .create(IMusicService::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(appContext, AppDatabase::class.java, "track.db").build()
    }

    @Provides
    fun provideLogDao(database: AppDatabase): TrackDao {
        return database.trackDao()
    }
    @Provides
    @Singleton
    fun providePlayer(): MusicPlayer {
        return MusicPlayer()
    }
}