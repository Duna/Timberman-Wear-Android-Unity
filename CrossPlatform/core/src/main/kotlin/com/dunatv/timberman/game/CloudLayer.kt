package com.dunatv.timberman.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.dunatv.timberman.util.Constants
import kotlin.random.Random

class CloudLayer {
    private data class Cloud(
        var x: Float,
        val y: Float,
        val w: Float,
        val h: Float,
        val speed: Float,
        val texture: Texture
    )

    private lateinit var cloud1Texture: Texture
    private lateinit var cloud2Texture: Texture
    private val backClouds = mutableListOf<Cloud>()
    private val frontClouds = mutableListOf<Cloud>()

    fun load() {
        cloud1Texture = Texture(Gdx.files.internal("textures/cloud1.png"))
        cloud2Texture = Texture(Gdx.files.internal("textures/cloud2.png"))

        backClouds.clear()
        frontClouds.clear()

        val halfW = Constants.WORLD_WIDTH / 2f

        for (i in 0 until Constants.BACK_CLOUD_COUNT) {
            val tex = if (Random.nextBoolean()) cloud1Texture else cloud2Texture
            val scale = 0.8f + Random.nextFloat() * 0.4f
            val w = tex.width / 100f * scale
            val h = tex.height / 100f * scale
            val x = -halfW + Random.nextFloat() * Constants.WORLD_WIDTH
            val y = Random.nextFloat() * (Constants.WORLD_HEIGHT / 2f)
            backClouds.add(Cloud(x, y, w, h, Constants.BACK_CLOUD_SPEED, tex))
        }

        for (i in 0 until Constants.FRONT_CLOUD_COUNT) {
            val tex = if (Random.nextBoolean()) cloud1Texture else cloud2Texture
            val scale = 1.0f + Random.nextFloat() * 0.5f
            val w = tex.width / 100f * scale
            val h = tex.height / 100f * scale
            val x = -halfW + Random.nextFloat() * Constants.WORLD_WIDTH
            val y = Random.nextFloat() * (Constants.WORLD_HEIGHT / 2f)
            frontClouds.add(Cloud(x, y, w, h, Constants.FRONT_CLOUD_SPEED, tex))
        }
    }

    fun update(delta: Float) {
        val halfW = Constants.WORLD_WIDTH / 2f
        for (cloud in backClouds) {
            cloud.x -= cloud.speed * delta
            if (cloud.x + cloud.w < -halfW) {
                cloud.x = halfW
            }
        }
        for (cloud in frontClouds) {
            cloud.x -= cloud.speed * delta
            if (cloud.x + cloud.w < -halfW) {
                cloud.x = halfW
            }
        }
    }

    fun renderBack(batch: SpriteBatch) {
        for (cloud in backClouds) {
            batch.draw(cloud.texture, cloud.x, cloud.y, cloud.w, cloud.h)
        }
    }

    fun renderFront(batch: SpriteBatch) {
        for (cloud in frontClouds) {
            batch.draw(cloud.texture, cloud.x, cloud.y, cloud.w, cloud.h)
        }
    }

    fun render(batch: SpriteBatch) {
        renderBack(batch)
    }

    fun dispose() {
        if (::cloud1Texture.isInitialized) cloud1Texture.dispose()
        if (::cloud2Texture.isInitialized) cloud2Texture.dispose()
    }
}
