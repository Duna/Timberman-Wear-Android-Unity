package com.dunatv.timberman.android

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.dunatv.timberman.firebase.FirebaseService
import com.dunatv.timberman.firebase.UserScore

class AndroidFirebaseService : FirebaseService {
    private val dbRef by lazy { FirebaseDatabase.getInstance().reference.child("top") }
    private var cachedScores: List<UserScore> = emptyList()

    override fun initialize() {
        listenForScores { scores ->
            cachedScores = scores
        }
    }

    override fun submitScore(name: String, score: Int, previousHighScore: Int) {
        val timestamp = System.currentTimeMillis() / 1000
        val scoreData = mapOf(
            "Name" to name,
            "Score" to score.toLong(),
            "Timestamp" to timestamp
        )
        dbRef.child("$score").setValue(scoreData)
        if (previousHighScore > 0 && previousHighScore != score) {
            dbRef.child("$previousHighScore").removeValue()
        }
    }

    override fun getScores(callback: (List<UserScore>) -> Unit) {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(parseScores(snapshot))
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }

    override fun listenForScores(callback: (List<UserScore>) -> Unit) {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val scores = parseScores(snapshot)
                cachedScores = scores
                callback(scores)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun getScoresSync(): List<UserScore> = cachedScores

    private fun parseScores(snapshot: DataSnapshot): List<UserScore> {
        val scores = mutableListOf<UserScore>()
        for (child in snapshot.children) {
            try {
                val name = child.child("Name").getValue(String::class.java) ?: ""
                val score = child.child("Score").getValue(Long::class.java) ?: 0L
                val timestamp = child.child("Timestamp").getValue(Long::class.java) ?: 0L
                scores.add(UserScore(name, score, timestamp))
            } catch (_: Exception) {}
        }
        return scores
    }
}
