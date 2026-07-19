package com.dunatv.timberman.firebase

interface FirebaseService {
    fun initialize()
    fun submitScore(name: String, score: Int, previousHighScore: Int)
    fun getScores(callback: (List<UserScore>) -> Unit)
    fun listenForScores(callback: (List<UserScore>) -> Unit)
    fun getScoresSync(): List<UserScore>
}
