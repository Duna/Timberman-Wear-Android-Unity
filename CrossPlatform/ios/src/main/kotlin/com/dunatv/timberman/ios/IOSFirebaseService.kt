package com.dunatv.timberman.ios

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Net
import com.badlogic.gdx.net.HttpParametersUtils
import com.dunatv.timberman.firebase.FirebaseService
import com.dunatv.timberman.firebase.UserScore

class IOSFirebaseService : FirebaseService {
    private val dbUrl = "https://smart-timberman-default-rtdb.europe-west1.firebasedatabase.app/top"
    private var cachedScores: List<UserScore> = emptyList()

    override fun initialize() {
        fetchScores { scores ->
            cachedScores = scores
        }
    }

    override fun submitScore(name: String, score: Int, previousHighScore: Int) {
        val getRequest = Net.HttpRequest(Net.HttpMethods.GET).apply {
            url = "$dbUrl/$name.json"
        }
        Gdx.net.sendHttpRequest(getRequest, object : Net.HttpResponseListener {
            override fun handleHttpResponse(httpResponse: Net.HttpResponse) {
                val responseStr = httpResponse.resultAsString
                val existingScore = extractJsonNumber(responseStr, "Score")
                if (score > existingScore) {
                    val json = """{"Name":"$name","Score":$score,"Timestamp":${System.currentTimeMillis() / 1000}}"""
                    val putRequest = Net.HttpRequest(Net.HttpMethods.PUT).apply {
                        url = "$dbUrl/$name.json"
                        content = json
                        setHeader("Content-Type", "application/json")
                    }
                    Gdx.net.sendHttpRequest(putRequest, object : Net.HttpResponseListener {
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
