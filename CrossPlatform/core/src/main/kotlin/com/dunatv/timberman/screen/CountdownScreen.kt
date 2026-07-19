package com.dunatv.timberman.screen

import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.dunatv.timberman.TimbermanGame
import com.dunatv.timberman.util.Constants

class CountdownScreen(game: TimbermanGame) : BaseScreen(game) {
    private var timeRemaining = Constants.COUNTDOWN_SECONDS.toFloat()
    private val layout = GlyphLayout()

    override fun render(delta: Float) {
        clearScreen()
        timeRemaining -= delta

        if (timeRemaining <= 0) {
            game.setScreen(GameScreen(game))
            return
        }

        val countText = kotlin.math.ceil(timeRemaining.toDouble()).toInt().toString()

        camera.update()
        batch.projectionMatrix = camera.combined
        batch.begin()
        game.font.setUseIntegerPositions(false)
        layout.setText(game.font, countText)
        game.font.draw(batch, countText,
            -layout.width / 2f,
            layout.height / 2f)
        batch.end()
    }
}
