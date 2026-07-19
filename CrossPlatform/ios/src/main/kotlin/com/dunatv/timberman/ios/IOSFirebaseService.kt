package com.dunatv.timberman.ios

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Net
import com.badlogic.gdx.net.HttpParametersUtils
import com.dunatv.timberman.firebase.FirebaseService
import com.dunatv.timberman.firebase.UserScore

class IOSFirebaseService : FirebaseService {
    private val dbUrl = "https://fullscreen2022-c3dc9-default-rtdb.firebaseio.com/top"
    private var cachedScores: List<UserScore> = emptyList()

    override fun initialize() {
        fetchScores { scores ->
            cachedScores = scores
        }
    }

    override fun submitScore(name: String, score: Int, previousHighScore: Int) {
        val json = """{"Name":"$name","Score":$score,"Timestamp":${System.currentTimeMillis() / 1000}}"""
        val request = Net.HttpRequest(Net.HttpMethods.PUT).apply {
            url = "$dbUrl/$score.json"
            content = json
            setHeader("Content-Type", "application/json")
        }
        Gdx.net.sendHttpRequest(request, object : Net.HttpResponseListener {
            override fun handleHttpResponse(httpResponse: Net.HttpResponse) {
                if (previousHighScore > 0 && previousHighScore != score) {
                    val deleteReq = Net.HttpRequest(Net.HttpMethods.DELETE).apply {
                        url = "$dbUrl/$previousHighScore.json"
                    }
                    Gdx.net.sendHttpRequest(deleteReq, object : Net.HttpResponseListener {
                        override fun handleHttpResponse(httpResponse: Net.HttpResponse) {}
                        override fun failed(t: Throwable?) {}
                        override fun cancelled() {}
                    })
                }
            }
            override fun failed(t: Throwable?) {}
            override fun cancelled() {}
        })
    }

    override fun getScores(callback: (List<UserScore>) -> Unit) {
        fetchScores(callback)
    }

    override fun listenForScores(callback: (List<UserScore>) -> Unit) {
        fetchScores { scores ->
            cachedScores = scores
            callback(scores)
        }
    }

    override fun getScoresSync(): List<UserScore> = cachedScores

    private fun fetchScores(callback: (List<UserScore>) -> Unit) {
        val request = Net.HttpRequest(Net.HttpMethods.GET).apply {
            url = "$dbUrl.json"
        }
        Gdx.net.sendHttpRequest(request, object : Net.HttpResponseListener {
            override fun handleHttpResponse(httpResponse: Net.HttpResponse) {
                val responseStr = httpResponse.resultAsString
                val scores = parseJsonScores(responseStr)
                cachedScores = scores
                Gdx.app.postRunnable { callback(scores) }
            }
            override fun failed(t: Throwable?) {
                Gdx.app.postRunnable { callback(emptyList()) }
            }
            override fun cancelled() {
                Gdx.app.postRunnable { callback(emptyList()) }
            }
        })
    }

    private fun parseJsonScores(json: String): List<UserScore> {
        val scores = mutableListOf<UserScore>()
        if (json.isNullOrEmpty() || json == "null") return scores

        try {
            val content = json.trim().removeSurrounding("{", "}")
            val entries = splitJsonEntries(content)
            for (entry in entries) {
                val colonIdx = entry.indexOf(":")
                if (colonIdx < 0) continue
                val value = entry.substring(colonIdx + 1).trim()
                val name = extractJsonString(value, "Name") ?: ""
                val score = extractJsonNumber(value, "Score")
                val timestamp = extractJsonNumber(value, "Timestamp")
                if (name.isNotEmpty()) {
                    scores.add(UserScore(name, score, timestamp))
                }
            }
        } catch (_: Exception) {}
        return scores
    }

    private fun splitJsonEntries(content: String): List<String> {
        val entries = mutableListOf<String>()
        var depth = 0
        var start = 0
        for (i in content.indices) {
            when (content[i]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) {
                        entries.add(content.substring(start, i + 1))
                        start = i + 1
                    }
                }
            }
            if (depth == 0 && content[i] == ',') start = i + 1
        }
        return entries
    }

    private fun extractJsonString(json: String, key: String): String? {
        val pattern = "\"$key\"\\s*:\\s*\"([^\"]*)\""
        val regex = Regex(pattern)
        return regex.find(json)?.groupValues?.get(1)
    }

    private fun extractJsonNumber(json: String, key: String): Long {
        val pattern = "\"$key\"\\s*:\\s*(\\d+)"
        val regex = Regex(pattern)
        return regex.find(json)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
    }
}
