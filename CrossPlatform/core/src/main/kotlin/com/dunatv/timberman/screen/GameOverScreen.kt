package com.dunatv.timberman.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.FitViewport
import com.dunatv.timberman.TimbermanGame

class GameOverScreen(game: TimbermanGame, private val finalScore: Int) : BaseScreen(game) {
    private lateinit var stage: Stage
    private lateinit var skin: Skin
    private lateinit var gameOverTexture: Texture
    private lateinit var gameOverBgTexture: Texture

    override fun show() {
        skin = game.createSkin()
        stage = Stage(FitViewport(450f, 450f))
        Gdx.input.inputProcessor = stage

        gameOverTexture = Texture(Gdx.files.internal("textures/game_over.png"))
        gameOverBgTexture = Texture(Gdx.files.internal("sprites/game_over_bg.png"))

        // Extract regions from the sprite sheets
        val boardRegion = TextureRegion(gameOverBgTexture, 0, 0, 149, 135)
        val textRegion = TextureRegion(gameOverBgTexture, 153, 0, 106, 58)
        val chainRegion = TextureRegion(gameOverTexture, 264, 3, 12, 134)

        val screenW = 450f
        val screenH = 450f

        // Layout constants — board sized to fit round watch
        val boardW = 260f
        val boardH = boardW * (135f / 149f)
        val boardX = (screenW - boardW) / 2f
        val boardCenterY = screenH * 0.42f
        val boardY = boardCenterY - boardH / 2f

        val chainW = 14f
        val chainTopY = boardY + boardH
        val chainH = screenH - chainTopY

        // Left chain — from board top-left corner to top of screen
        val leftChain = Image(TextureRegionDrawable(chainRegion))
        leftChain.setBounds(boardX + 25f, chainTopY, chainW, chainH)
        stage.addActor(leftChain)

        // Right chain — from board top-right corner to top of screen
        val rightChain = Image(TextureRegionDrawable(chainRegion))
        rightChain.setBounds(boardX + boardW - 25f - chainW, chainTopY, chainW, chainH)
        stage.addActor(rightChain)

        // Board table with score, best, OK
        val boardTable = Table()
        boardTable.setBackground(TextureRegionDrawable(boardRegion))
        boardTable.setBounds(boardX, boardY, boardW, boardH)
        boardTable.center()
        boardTable.pad(20f)

        // "GAME OVER" text — centered on top of the board, overlapping its top edge
        val goTextW = 180f
        val goTextH = goTextW * (58f / 106f)
        val goTextDrawable = TextureRegionDrawable(textRegion)
        goTextDrawable.minWidth = goTextW
        goTextDrawable.minHeight = goTextH
        val goImage = Image(goTextDrawable)
        goImage.setBounds(
            (screenW - goTextW) / 2f,
            boardY + boardH - goTextH * 0.5f,
            goTextW, goTextH
        )

        val scoreLabel = Label("Score: $finalScore", skin)
        scoreLabel.setAlignment(Align.center)
        boardTable.add(scoreLabel).expandX().center().padBottom(8f)
        boardTable.row()

        val bestScore = game.prefs.getHighScore()
        val bestLabel = Label("Best: $bestScore", skin)
        bestLabel.setAlignment(Align.center)
        boardTable.add(bestLabel).expandX().center().padBottom(12f)
        boardTable.row()

        val okButton = TextButton("OK", skin)
        okButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.setScreen(StartScreen(game))
            }
        })
        boardTable.add(okButton).width(100f).height(35f).center()

        stage.addActor(boardTable)
        stage.addActor(goImage)
    }

    override fun render(delta: Float) {
        clearScreen()
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        if (::stage.isInitialized) stage.dispose()
        if (::skin.isInitialized) skin.dispose()
        if (::gameOverTexture.isInitialized) gameOverTexture.dispose()
        if (::gameOverBgTexture.isInitialized) gameOverBgTexture.dispose()
    }
}
