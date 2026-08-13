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
    private val dragonAttack = DragonAttack()
    private val stoneAttack = StoneAttack()

    private var score = 0
    private var gameOver = false
    private val layout = GlyphLayout()
    private var nextDragonScore = Constants.DRAGON_SCORE_INTERVAL
    private var dragonInterval = Constants.DRAGON_SCORE_INTERVAL

    private var tapHintVisible = false
    private var tapHintAlpha = 1f
    private var tapHintFadeIn = true
    private lateinit var tapTexture: Texture

    override fun show() {
        Gdx.input.inputProcessor = null

        player.load()
        treeManager.load()
        timerBar.load()
        cloudLayer.load()
        backgroundLayer.load()
        dragonAttack.load()
        stoneAttack.load()

        tapTexture = Texture(Gdx.files.internal("textures/tap.png"))

        player.reset()
        timerBar.reset()
        dragonAttack.reset()
        stoneAttack.reset()
        treeManager.initialize(game.firebaseService.getScoresSync())
        player.groundY = treeManager.getGroundY()
        score = 0
        nextDragonScore = Constants.DRAGON_SCORE_INTERVAL
        dragonInterval = Constants.DRAGON_SCORE_INTERVAL
        gameOver = false
        tapHintVisible = !game.prefs.isTapShown()
    }

    override fun render(delta: Float) {
        if (gameOver) return

        clearScreen()

        if (!dragonAttack.isActive) {
            timerBar.update(delta)
            if (timerBar.isDead) {
                Gdx.app.log("TimbermanGame", "DEATH by timer, score=$score")
                onGameOver()
                return
            }
        }

        cloudLayer.update(delta)
        backgroundLayer.update(delta)
        player.update(delta)

        val burnFinished = dragonAttack.update(delta)
        if (burnFinished) {
            val blueX = dragonAttack.getRandomBlueFlameX()
            treeManager.clearAllBranches()
            dragonAttack.reset()
            if (blueX != null) {
                stoneAttack.trigger(blueX)
            }
        }

        stoneAttack.update(delta)
        if (stoneAttack.isActive && stoneAttack.checkHit(player.getX(), player.groundY, player.getWidth(), player.getHeight())) {
            Gdx.app.log("TimbermanGame", "DEATH by falling stone, score=$score")
            player.die()
            onGameOver(stoneKill = true)
            return
        }

        if (!dragonAttack.isActive) {
            val chopped = player.handleInput()
            if (chopped) {
                score++
                timerBar.addTick()
                Gdx.app.log("TimbermanGame", "after chop: timerTicks=${timerBar.getFillPercent() * Constants.TIMER_MAX_TICKS}, timerFill=${timerBar.getFillPercent()}")
                val died = treeManager.chop(player.isLeft, score)
                if (died) {
                    Gdx.app.log("TimbermanGame", "DEATH by branch collision, score=$score, playerIsLeft=${player.isLeft}")
                    player.die()
                    onGameOver()
                    return
                }
                treeManager.enablePlayerHit(player)

                if (tapHintVisible) {
                    tapHintVisible = false
                    game.prefs.setTapShown()
                }

                if (score >= nextDragonScore) {
                    dragonInterval += Constants.DRAGON_SCORE_INTERVAL
                    nextDragonScore = score + dragonInterval
                    val branchPositions = treeManager.getBranchPositions()
                    if (branchPositions.isNotEmpty()) {
                        dragonAttack.trigger(branchPositions)
                    }
                }
            }
        }

        camera.update()
        batch.projectionMatrix = camera.combined
        batch.begin()

        backgroundLayer.renderBack(batch, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT)
        stoneAttack.render(batch)
        backgroundLayer.renderFront(batch, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT)
        cloudLayer.renderBack(batch)
        dragonAttack.renderFlames(batch)
        treeManager.render(batch, dragonAttack.branchAlpha)
        player.render(batch)
        dragonAttack.renderDragon(batch)
        cloudLayer.renderFront(batch)

        renderHUD()

        if (tapHintVisible) {
            renderTapHint(delta)
        }

        batch.end()
    }

    private fun renderHUD() {
        val scoreText = score.toString()
        layout.setText(game.tinyFont, scoreText)
        game.tinyFont.draw(batch, scoreText, -layout.width / 2f, Constants.WORLD_HEIGHT / 2f - Constants.HUD_SCORE_OFFSET_Y)

        val bestScore = game.prefs.getHighScore()
        val bestText = "Best: $bestScore"
        layout.setText(game.tinyFont, bestText)
        game.tinyFont.draw(batch, bestText, -layout.width / 2f, Constants.WORLD_HEIGHT / 2f - Constants.HUD_BEST_OFFSET_Y)

        timerBar.render(batch, Constants.TIMER_BAR_X, -Constants.WORLD_HEIGHT / 2f + Constants.TIMER_BAR_BOTTOM_OFFSET, Constants.TIMER_BAR_WIDTH, Constants.TIMER_BAR_HEIGHT)
    }

    private fun renderTapHint(delta: Float) {
        if (tapHintFadeIn) {
            tapHintAlpha += delta * Constants.TAP_HINT_FADE_SPEED
            if (tapHintAlpha >= 1f) {
                tapHintAlpha = 1f
                tapHintFadeIn = false
            }
        } else {
            tapHintAlpha -= delta * Constants.TAP_HINT_FADE_SPEED
            if (tapHintAlpha <= 0f) {
                tapHintAlpha = 0f
                tapHintFadeIn = true
            }
        }

        val oldColor = batch.color.cpy()
        batch.setColor(1f, 1f, 1f, tapHintAlpha)
        val tapW = tapTexture.width / 100f
        val tapH = tapTexture.height / 100f
        batch.draw(tapTexture, -tapW / 2f, Constants.TAP_HINT_Y, tapW, tapH)
        batch.color = oldColor
    }

    private fun onGameOver(stoneKill: Boolean = false) {
        gameOver = true
        Gdx.app.log("TimbermanGame", ">>> onGameOver CALLED, score=$score, stacktrace:")
        Thread.currentThread().stackTrace.take(8).forEach {
            Gdx.app.log("TimbermanGame", "  at ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})")
        }
        val playerName = game.prefs.getPlayerName()
        val prevHigh = game.prefs.getHighScore()
        val isNewBest = game.prefs.updateHighScore(score)
        val bestScore = game.prefs.getHighScore()
        Gdx.app.log("TimbermanGame", "onGameOver: score=$score, playerName=$playerName, prevHigh=$prevHigh, isNewBest=$isNewBest, bestScore=$bestScore")
        if (bestScore > 0) {
            Gdx.app.log("TimbermanGame", "Submitting bestScore=$bestScore to Firebase...")
            game.firebaseService.submitScore(playerName, bestScore, prevHigh)
        }
        game.setScreen(GameOverScreen(game, score, stoneKill))
    }

    override fun dispose() {
        player.dispose()
        treeManager.dispose()
        timerBar.dispose()
        cloudLayer.dispose()
        backgroundLayer.dispose()
        dragonAttack.dispose()
        stoneAttack.dispose()
        if (::tapTexture.isInitialized) tapTexture.dispose()
    }
}
