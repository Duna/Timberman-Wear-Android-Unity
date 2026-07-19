package com.dunatv.timberman.util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences

class Prefs {
    private val prefs: Preferences by lazy { Gdx.app.getPreferences("TimbermanWear") }

    fun getPlayerName(): String = prefs.getString("playerName", "")

    fun setPlayerName(name: String) {
        prefs.putString("playerName", name)
        prefs.flush()
    }

    fun hasPlayerName(): Boolean = getPlayerName().isNotEmpty()

    fun getHighScore(): Int = prefs.getInteger("highscore", 0)

    fun updateHighScore(score: Int): Boolean {
        if (score > getHighScore()) {
            prefs.putInteger("highscore", score)
            prefs.flush()
            return true
        }
        return false
    }

    fun isTapShown(): Boolean = prefs.getBoolean("tapshown", false)

    fun setTapShown() {
        prefs.putBoolean("tapshown", true)
        prefs.flush()
    }
}
