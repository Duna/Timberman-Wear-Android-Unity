package com.dunatv.timberman.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.dunatv.timberman.util.Constants

class TimerBar {
    private var ticks = Constants.TIMER_MAX_TICKS
    private var elapsed = 0f
    var isDead = false
        private set

    private lateinit var progressTexture: Texture
    private lateinit var progressRegion: TextureRegion

    fun load() {
        progressTexture = Texture(Gdx.files.internal("sprites/progress.png"))
        progressRegion = TextureRegion(progressTexture)
    }

    fun reset() {
        ticks = Constants.TIMER_MAX_TICKS
        elapsed = 0f
        isDead = false
    }

    fun update(delta: Float) {
        if (isDead) return
        elapsed += delta
        while (elapsed >= Constants.TIMER_DRAIN_INTERVAL) {
            elapsed -= Constants.TIMER_DRAIN_INTERVAL
            ticks--
            if (ticks <= 0) {
                ticks = 0
                isDead = true
                return
            }
        }
    }

    fun addTick() {
        if (ticks < Constants.TIMER_MAX_TICKS) {
            ticks++
        }
    }

    fun getFillPercent(): Float = ticks / Constants.TIMER_MAX_TICKS.toFloat()

    fun render(batch: SpriteBatch, x: Float, y: Float, maxWidth: Float, height: Float) {
        val fillWidth = maxWidth * getFillPercent()
        if (fillWidth > 0) {
            val srcWidth = (progressTexture.width * getFillPercent()).toInt()
            val fillRegion = TextureRegion(progressTexture, 0, 0, srcWidth, progressTexture.height)
            batch.draw(fillRegion, x, y, fillWidth, height)
        }
    }

    fun dispose() {
        if (::progressTexture.isInitialized) progressTexture.dispose()
    }
}
