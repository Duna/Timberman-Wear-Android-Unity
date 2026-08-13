package com.dunatv.timberman.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.dunatv.timberman.util.Constants
import kotlin.random.Random

class StoneAttack {
    private lateinit var stoneTexture: Texture
    private lateinit var stoneRegion: TextureRegion

    private var stoneX = 0f
    private var stoneY = 0f
    private var velocity = 0f
    private var rotation = 0f
    private var rotationSpeed = 0f
    private var active = false

    val isActive get() = active

    fun load() {
        stoneTexture = Texture(Gdx.files.internal("textures/stone.png"))
        stoneRegion = TextureRegion(stoneTexture)
    }

    fun trigger(x: Float) {
        stoneX = x
        stoneY = Constants.WORLD_HEIGHT / 2f
        velocity = 0f
        rotation = 0f
        rotationSpeed = Random.nextFloat() * 360f - 180f
        active = true
    }

    fun update(delta: Float) {
        if (!active) return
        val gravity = 15f
        velocity += gravity * delta
        stoneY -= velocity * delta
        rotation += rotationSpeed * delta
        if (stoneY < -Constants.WORLD_HEIGHT / 2f - 1f) {
            active = false
        }
    }

    fun checkHit(playerX: Float, playerY: Float, playerW: Float, playerH: Float): Boolean {
        if (!active) return false
        val stoneSize = stoneTexture.width / 100f * Constants.TRUNK_SCALE
        val stoneLeft = stoneX - stoneSize / 2f
        val stoneRight = stoneX + stoneSize / 2f
        val stoneBottom = stoneY
        val stoneTop = stoneY + stoneSize
        val playerLeft = playerX - playerW / 2f
        val playerRight = playerX + playerW / 2f
        val playerBottom = playerY
        val playerTop = playerY + playerH
        return stoneRight > playerLeft && stoneLeft < playerRight &&
            stoneTop > playerBottom && stoneBottom < playerTop
    }

    fun render(batch: SpriteBatch) {
        if (!active) return
        val stoneSize = stoneTexture.width / 100f * Constants.TRUNK_SCALE
        batch.draw(
            stoneRegion,
            stoneX - stoneSize / 2f, stoneY,
            stoneSize / 2f, stoneSize / 2f,
            stoneSize, stoneSize,
            1f, 1f, rotation
        )
    }

    fun reset() {
        active = false
    }

    fun dispose() {
        if (::stoneTexture.isInitialized) stoneTexture.dispose()
    }
}
