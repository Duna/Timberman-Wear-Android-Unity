package com.dunatv.timberman.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.dunatv.timberman.TimbermanGame
import com.dunatv.timberman.game.*
import com.dunatv.timberman.util.Constants

class GameScreen(game: TimbermanGame) : BaseScreen(game) {
    private val player = Player()
    private val treeManager = TreeManager()
    private val timerBar = TimerBar()
    private val cloudLayer = CloudLayer()
    private val backgroundLayer = BackgroundLayer()

    private var score = 0
    private var gameOver = false
    private val layout = GlyphLayout()

    private var tapHintVisible = false
    private var tapHintAlpha = 1f
    private var tapHintFadeIn = true
    private lateinit var tapTexture: Texture

    override fun show() {
        player.load()
        treeManager.load()
        timerBar.load()
        cloudLayer.load()
        backgroundLayer.load()

        tapTexture = Texture(Gdx.files.internal("textures/tap.png"))

        player.reset()
        timerBar.reset()
        treeManager.initialize(game.firebaseService.getScoresSync())
        player.groundY = treeManager.getGroundY()
        score = 0
        gameOver = false
        tapHintVisible = !game.prefs.isTapShown()
    }

    override fun render(delta: Float) {
        if (gameOver) return

        clearScreen()

        timerBar.update(delta)
        if (timerBar.isDead) {
            onGameOver()
            return
        }

        cloudLayer.update(delta)
        backgroundLayer.update(delta)
        player.update(delta)

        val chopped = player.handleInput()
        if (chopped) {
            val died = treeManager.chop(player.isLeft, score)
            if (died) {
                player.die()
                onGameOver()
                return
            }
            score++
            timerBar.addTick()
            treeManager.enablePlayerHit(player)

            if (tapHintVisible) {
                tapHintVisible = false
                game.prefs.setTapShown()
            }
        }

        camera.update()
        batch.projectionMatrix = camera.combined
        batch.begin()

        backgroundLayer.render(batch, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT)
        cloudLayer.render(batch)
        treeManager.render(batch)
        player.render(batch)

        renderHUD()

        if (tapHintVisible) {
            renderTapHint(delta)
        }

        batch.end()
    }

    private fun renderHUD() {
        val scoreText = score.toString()
        layout.setText(game.font, scoreText)
        game.font.draw(batch, scoreText, -layout.width / 2f, Constants.WORLD_HEIGHT / 2f - 0.5f)

        val bestScore = game.prefs.getHighScore()
        val bestText = "Best: $bestScore"
        layout.setText(game.smallFont, bestText)
        game.smallFont.draw(batch, bestText, -layout.width / 2f, Constants.WORLD_HEIGHT / 2f - 1.2f)

        timerBar.render(batch, -2f, Constants.WORLD_HEIGHT / 2f - 1.8f, 4f, 0.3f)
    }

    private fun renderTapHint(delta: Float) {
        if (tapHintFadeIn) {
            tapHintAlpha += delta * 2f
            if (tapHintAlpha >= 1f) {
                tapHintAlpha = 1f
                tapHintFadeIn = false
            }
        } else {
            tapHintAlpha -= delta * 2f
            if (tapHintAlpha <= 0f) {
                tapHintAlpha = 0f
                tapHintFadeIn = true
            }
        }

        val oldColor = batch.color.cpy()
        batch.setColor(1f, 1f, 1f, tapHintAlpha)
        val tapW = tapTexture.width / 100f
        val tapH = tapTexture.height / 100f
        batch.draw(tapTexture, -tapW / 2f, -1f, tapW, tapH)
        batch.color = oldColor
    }

    private fun onGameOver() {
        gameOver = true
        val isNewBest = game.prefs.updateHighScore(score)
        if (isNewBest) {
            game.firebaseService.submitScore(
                game.prefs.getPlayerName(),
                score,
                game.prefs.getHighScore()
            )
        }
        game.setScreen(GameOverScreen(game, score))
    }

    override fun dispose() {
        player.dispose()
        treeManager.dispose()
        timerBar.dispose()
        cloudLayer.dispose()
        backgroundLayer.dispose()
        if (::tapTexture.isInitialized) tapTexture.dispose()
    }
}
