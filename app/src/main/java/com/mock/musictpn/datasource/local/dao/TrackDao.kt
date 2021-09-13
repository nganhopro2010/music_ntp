package com.mock.musictpn.datasource.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.mock.musictpn.model.track.Track

@Dao
interface TrackDao {
    @Insert
    suspend fun insertTrack(track: Track)

    @Delete
    suspend fun deleteTrack(track: Track)

    @Query("select * from favorite_track")
    suspend fun getAllFavoriteTrack():List<Track>
}