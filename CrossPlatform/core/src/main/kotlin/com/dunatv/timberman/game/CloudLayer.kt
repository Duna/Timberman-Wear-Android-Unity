package com.dunatv.timberman.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.dunatv.timberman.util.Constants
import kotlin.random.Random

class CloudLayer {
    private data class Cloud(var x: Float, var y: Float, val texture: Texture)

    private lateinit var cloud1Texture: Texture
    private lateinit var cloud2Texture: Texture
    private val clouds = mutableListOf<Cloud>()
    private var cloudWidth = 0f

    fun load() {
        cloud1Texture = Texture(Gdx.files.internal("textures/cloud1.png"))
        cloud2Texture = Texture(Gdx.files.internal("textures/cloud2.png"))
        cloudWidth = cloud1Texture.width / 100f

        clouds.clear()
        for (i in 0 until Constants.CLOUD_COUNT) {
            val tex = if (Random.nextBoolean()) cloud1Texture else cloud2Texture
            val x = i * cloudWidth
            val y = 2f + Random.nextFloat() * 1.5f
            clouds.add(Cloud(x, y, tex))
        }
    }

    fun update(delta: Float) {
        val tailX = clouds.maxOf { it.x }
        for (cloud in clouds) {
            cloud.x -= Constants.CLOUD_SPEED * delta
            if (cloud.x < -cloudWidth) {
                cloud.x = tailX + cloudWidth
            }
        }
    }

    fun render(batch: SpriteBatch) {
        for (cloud in clouds) {
            val w = cloud.texture.width / 100f
            val h = cloud.texture.height / 100f
            batch.draw(cloud.texture, cloud.x, cloud.y, w, h)
        }
    }

    fun dispose() {
        if (::cloud1Texture.isInitialized) cloud1Texture.dispose()
        if (::cloud2Texture.isInitialized) cloud2Texture.dispose()
    }
}
