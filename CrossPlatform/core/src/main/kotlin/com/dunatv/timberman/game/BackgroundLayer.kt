package com.dunatv.timberman.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.dunatv.timberman.util.Constants

class BackgroundLayer {
    private lateinit var bgTexture: Texture
    private lateinit var bgBgTexture: Texture
    private var scrollOffset = 0f

    fun load() {
        bgTexture = Texture(Gdx.files.internal("textures/tree_bg.png"))
        bgBgTexture = Texture(Gdx.files.internal("textures/tree_bg_bg.png"))
    }

    fun update(delta: Float) {
        scrollOffset += Constants.BG_SCROLL_SPEED * delta
        val bgBgW = bgBgTexture.width / 100f
        if (scrollOffset > bgBgW) scrollOffset -= bgBgW
    }

    fun render(batch: SpriteBatch, worldWidth: Float, worldHeight: Float) {
        val halfW = worldWidth / 2f
        val halfH = worldHeight / 2f

        // Distant forest (tree_bg_bg) — scrolls slowly for parallax
        val bgBgW = bgBgTexture.width / 100f
        val bgBgH = bgBgTexture.height / 100f
        val bgBgScale = worldHeight / bgBgH
        val scaledBgBgW = bgBgW * bgBgScale

        var x = -halfW - scrollOffset
        while (x < halfW + scaledBgBgW) {
            batch.draw(bgBgTexture, x, -halfH, scaledBgBgW, worldHeight)
            x += scaledBgBgW
        }

        // Main scenery frame (tree_bg) — static, covers full viewport
        val bgW = bgTexture.width / 100f
        val bgH = bgTexture.height / 100f
        val scaleX = worldWidth / bgW
        val scaleY = worldHeight / bgH
        val scale = maxOf(scaleX, scaleY)
        batch.draw(bgTexture, -halfW, -halfH, bgW * scale, bgH * scale)
    }

    fun dispose() {
        if (::bgTexture.isInitialized) bgTexture.dispose()
        if (::bgBgTexture.isInitialized) bgBgTexture.dispose()
    }
}
