package com.mock.musictpn.app.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.mock.musictpn.R
import com.mock.musictpn.mediaplayer.MusicPlayer
import com.mock.musictpn.model.track.TrackList
import com.mock.musictpn.app.receiver.MusicReceiver
import com.mock.musictpn.ui.activity.MainActivity
import com.mock.musictpn.utils.Const.CHANNEL_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : Service() {

    private var iBinder: IBinder = MusicBinder()

    @Inject
    lateinit var musicController: MusicPlayer

    @Inject
    lateinit var scope: CoroutineScope

    var isPlaying = false


    override fun onBind(intent: Intent): IBinder {
        return iBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("ThangDN6 - MusicService", "onStartCommand: ${intent?.action}")
        when (intent?.action) {
            MusicPlayer.ACTION_START -> {
                val bundle = intent.extras
                val list = bundle?.getSerializable("list")
                if (list != null) {
                    musicController.listTrack = list as TrackList
                    setupService()

                }
            }
            MusicPlayer.ACTION_STOP -> {
                musicController.stop()
            }
            MusicPlayer.ACTION_PLAY, MusicPlayer.ACTION_PAUSE -> {
                if (musicController.isStopped()) {
                    val bundle = intent.extras
                    val list = bundle?.getSerializable("list")
                    if (list != null) {
                        musicController.listTrack = list as TrackList
                        setupService()

                    }
                } else {
                    musicController.togglePlayButton()
                }
            }
            MusicPlayer.ACTION_NEXT -> {
                musicController.next()
            }
            MusicPlayer.ACTION_PREV -> {
                musicController.prev()
            }
            MusicPlayer.ACTION_SHUFFLE -> {
                musicController.toggleShuffle()
            }
            MusicPlayer.ACTION_REPEAT -> {
                musicController.toggleRepeat()
            }
            else -> Log.e("ThangDN6", "onStartCommand: Unknown or null action: ${intent?.action}")
        }


        return START_STICKY
    }

    private fun setupService() {

        val img = BitmapFactory.decodeResource(resources, R.drawable.logo)
        createNotification(
            musicController.listTrack.tracks[musicController.getCurrentIndex()].name,
            img,
            musicController.listTrack.tracks[musicController.getCurrentIndex()].artistName
        )
        musicController.playTrack(musicController.listTrack.pivot)
            Log.e("ThangDN6 - MusicService", "setupService: Load internet")
            Glide.with(this@MusicService)
                .asBitmap()
                .load(musicController.getCurrentTrack().getImageUrl())
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        createNotification(
                            musicController.listTrack.tracks[musicController.getCurrentIndex()].name,
                            resource,
                            musicController.listTrack.tracks[musicController.getCurrentIndex()].artistName
                        )
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        Log.d("ThangDN6 - MusicService", "onLoadCleared: cleared")
                    }

                })


    }


    fun createNotification(track: String, image: Bitmap, artistName: String) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val mediaSession = MediaSessionCompat(this, "tag")

        val mediaIntent = Intent(this, MusicReceiver::class.java)

        val nextPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            mediaIntent.apply { action = MusicPlayer.ACTION_NEXT },
            0
        )
        val prevPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            mediaIntent.apply { action = MusicPlayer.ACTION_PREV },
            0
        )
        val playPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            mediaIntent.apply {
                action =
                    if (musicController.isPlaying()) MusicPlayer.ACTION_PAUSE else MusicPlayer.ACTION_PLAY
            },
            0
        )
        val stopPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            mediaIntent.apply { action = MusicPlayer.ACTION_STOP },
            0
        )

        val btnIcon =
            if (musicController.isPlaying()) R.drawable.ic_pause else R.drawable.ic_play_noti
        val notificationIcon =
            if (musicController.isPlaying()) R.drawable.ic_play_noti else R.drawable.ic_pause

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(notificationIcon)
            .setContentTitle(track)
            .setContentText(artistName)
            .setLargeIcon(image)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.ic_prev, "Previous", prevPendingIntent)
            .addAction(btnIcon, "Play", playPendingIntent)
            .addAction(R.drawable.ic_next, "Next", nextPendingIntent)
            .addAction(R.drawable.ic_close, "Stop", stopPendingIntent)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(1)
                    .setMediaSession(mediaSession.sessionToken)
            )
            .build()

        startForeground(11, notification)

    }


    override fun onDestroy() {
        Log.d("ThangDN6 - MusicService", "onDestroy: ")
        super.onDestroy()
    }


    inner class MusicBinder : Binder() {
        fun getService(): MusicService {
            return this@MusicService
        }
    }
}