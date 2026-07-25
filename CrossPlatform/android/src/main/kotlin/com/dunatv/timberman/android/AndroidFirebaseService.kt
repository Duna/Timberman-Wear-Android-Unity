package com.dunatv.timberman.android

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.dunatv.timberman.firebase.FirebaseService
import com.dunatv.timberman.firebase.UserScore

private const val TAG = "TimbermanFirebase"

class AndroidFirebaseService : FirebaseService {
    private val dbRef by lazy { FirebaseDatabase.getInstance().reference.child("top") }
    private var cachedScores: List<UserScore> = emptyList()

    override fun initialize() {
        Log.d(TAG, "initialize() called, dbRef=$dbRef")
        Log.d(TAG, "Firebase URL: ${FirebaseDatabase.getInstance().reference}")
        listenForScores { scores ->
            Log.d(TAG, "listenForScores callback: ${scores.size} scores cached")
            cachedScores = scores
        }
    }

    override fun submitScore(name: String, score: Int, previousHighScore: Int) {
        Log.d(TAG, "submitScore(name=$name, score=$score, prevHigh=$previousHighScore)")
        val timestamp = System.currentTimeMillis() / 1000
        val scoreData = mapOf(
            "Name" to name,
            "Score" to score.toLong(),
            "Timestamp" to timestamp
        )
        dbRef.child("$score").setValue(scoreData)
            .addOnSuccessListener { Log.d(TAG, "submitScore SUCCESS for score=$score") }
            .addOnFailureListener { Log.e(TAG, "submitScore FAILED for score=$score", it) }
        if (previousHighScore > 0 && previousHighScore != score) {
            dbRef.child("$previousHighScore").removeValue()
        }
    }

    override fun getScores(callback: (List<UserScore>) -> Unit) {
        Log.d(TAG, "getScores() called")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "getScores onDataChange: childrenCount=${snapshot.childrenCount}, exists=${snapshot.exists()}")
                val scores = parseScores(snapshot)
                Log.d(TAG, "getScores parsed ${scores.size} scores")
                callback(scores)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "getScores onCancelled: ${error.message}, code=${error.code}")
                callback(emptyList())
            }
        })
    }

    override fun listenForScores(callback: (List<UserScore>) -> Unit) {
        Log.d(TAG, "listenForScores() called")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "listenForScores onDataChange: childrenCount=${snapshot.childrenCount}")
                val scores = parseScores(snapshot)
                cachedScores = scores
                callback(scores)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "listenForScores onCancelled: ${error.message}, code=${error.code}")
            }
        })
    }

    override fun getScoresSync(): List<UserScore> = cachedScores

    private fun parseScores(snapshot: DataSnapshot): List<UserScore> {
        val scores = mutableListOf<UserScore>()
        for (child in snapshot.children) {
            try {
                Log.d(TAG, "parseScores child key=${child.key}, value=${child.value}")
                val name = child.child("Name").getValue(String::class.java) ?: ""
                val score = child.child("Score").getValue(Long::class.java) ?: 0L
                val timestamp = child.child("Timestamp").getValue(Long::class.java) ?: 0L
                scores.add(UserScore(name, score, timestamp))
            } catch (e: Exception) {
                Log.e(TAG, "parseScores error for child ${child.key}", e)
            }
        }
        return scores
    }
}
