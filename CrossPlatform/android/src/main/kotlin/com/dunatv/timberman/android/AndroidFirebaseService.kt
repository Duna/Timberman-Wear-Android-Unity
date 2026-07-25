package com.dunatv.timberman.android

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.dunatv.timberman.firebase.FirebaseService
import com.dunatv.timberman.firebase.UserScore

private const val TAG = "TimbermanFirebase"

class AndroidFirebaseService : FirebaseService {
    private val dbRef by lazy { FirebaseDatabase.getInstance().reference.child("top") }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private var cachedScores: List<UserScore> = emptyList()
    private var authenticated = false

    override fun initialize() {
        Log.d(TAG, "initialize() called, signing in anonymously...")
        auth.signInAnonymously()
            .addOnSuccessListener {
                Log.d(TAG, "Anonymous auth SUCCESS, uid=${auth.currentUser?.uid}")
                authenticated = true
                cleanupDuplicates()
                listenForScores { scores ->
                    Log.d(TAG, "listenForScores callback: ${scores.size} scores cached")
                    cachedScores = scores
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Anonymous auth FAILED", e)
                listenForScores { scores ->
                    Log.d(TAG, "listenForScores callback (no auth): ${scores.size} scores cached")
                    cachedScores = scores
                }
            }
    }

    private fun cleanupDuplicates() {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bestByName = mutableMapOf<String, Pair<String, Long>>()
                val keysToDelete = mutableListOf<String>()

                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    val name = child.child("Name").getValue(String::class.java) ?: continue
                    val score = child.child("Score").getValue(Long::class.java) ?: 0L

                    val existing = bestByName[name]
                    if (existing == null || score > existing.second) {
                        if (existing != null) keysToDelete.add(existing.first)
                        bestByName[name] = Pair(key, score)
                    } else {
                        keysToDelete.add(key)
                    }
                }

                for (key in keysToDelete) {
                    Log.d(TAG, "cleanup: removing duplicate entry key=$key")
                    dbRef.child(key).removeValue()
                }
                if (keysToDelete.isEmpty()) {
                    Log.d(TAG, "cleanup: no duplicates found")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "cleanup failed: ${error.message}")
            }
        })
    }

    override fun submitScore(name: String, score: Int, previousHighScore: Int) {
        Log.d(TAG, "submitScore(name=$name, score=$score, prevHigh=$previousHighScore, auth=$authenticated)")
        val playerRef = dbRef.child(name)
        playerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val existingScore = snapshot.child("Score").getValue(Long::class.java) ?: 0L
                Log.d(TAG, "submitScore existing score for $name: $existingScore")
                if (score > existingScore) {
                    val timestamp = System.currentTimeMillis() / 1000
                    val scoreData = mapOf(
                        "Name" to name,
                        "Score" to score.toLong(),
                        "Timestamp" to timestamp
                    )
                    playerRef.setValue(scoreData)
                        .addOnSuccessListener { Log.d(TAG, "submitScore SUCCESS: $name=$score (was $existingScore)") }
                        .addOnFailureListener { Log.e(TAG, "submitScore FAILED for $name", it) }
                } else {
                    Log.d(TAG, "submitScore SKIPPED: $name already has $existingScore >= $score")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "submitScore read FAILED: ${error.message}")
            }
        })
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
