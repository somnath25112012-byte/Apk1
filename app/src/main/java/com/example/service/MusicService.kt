package com.example.service

import com.example.model.MusicTrack

interface MusicService {
    val availableTracks: List<MusicTrack>
    fun getTrackDescription(track: MusicTrack): String
}

class DefaultMusicService : MusicService {
    override val availableTracks: List<MusicTrack> = MusicTrack.values().toList()

    override fun getTrackDescription(track: MusicTrack): String {
        return "${track.titleBn} (${track.moodBn})"
    }
}
