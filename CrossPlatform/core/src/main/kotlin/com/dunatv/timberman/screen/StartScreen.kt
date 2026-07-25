package com.dunatv.timberman.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.FitViewport
import com.dunatv.timberman.TimbermanGame
import com.dunatv.timberman.util.Constants

class StartScreen(game: TimbermanGame) : BaseScreen(game) {
    private lateinit var stage: Stage
    private lateinit var logoTexture: Texture
    private lateinit var playTexture: Texture
    private lateinit var exitTexture: Texture
    private lateinit var topTexture: Texture

    override fun show() {
        stage = Stage(FitViewport(Constants.UI_VIEWPORT_SIZE, Constants.UI_VIEWPORT_SIZE))
        Gdx.input.inputProcessor = stage

        logoTexture = Texture(Gdx.files.internal("textures/timber_logo.png"))
        playTexture = Texture(Gdx.files.internal("textures/play.png"))
        exitTexture = Texture(Gdx.files.internal("textures/exit.png"))
        topTexture = Texture(Gdx.files.internal("textures/top.png"))

        val table = Table()
        table.setFillParent(true)
        table.center()

        val logoDrawable = TextureRegionDrawable(TextureRegion(logoTexture))
        logoDrawable.minWidth = Constants.LOGO_SIZE
        logoDrawable.minHeight = Constants.LOGO_SIZE
        val logoImage = Image(logoDrawable)
        table.add(logoImage).size(Constants.LOGO_SIZE, Constants.LOGO_SIZE).padBottom(Constants.LOGO_PAD_BOTTOM).colspan(3)
        table.row()

        val exitSize = Constants.BUTTON_SIZE
        val playTopH = exitSize
        val playTopW = playTopH * Constants.BUTTON_ASPECT_RATIO

        val playDrawable = TextureRegionDrawable(TextureRegion(playTexture))
        playDrawable.minWidth = playTopW
        playDrawable.minHeight = playTopH
        val playButton = ImageButton(playDrawable)
        playButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.setScreen(CountdownScreen(game))
            }
        })

        val exitDrawable = TextureRegionDrawable(TextureRegion(exitTexture))
        exitDrawable.minWidth = exitSize
        exitDrawable.minHeight = exitSize
        val exitButton = ImageButton(exitDrawable)
        exitButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                Gdx.app.exit()
            }
        })

        val topDrawable = TextureRegionDrawable(TextureRegion(topTexture))
        topDrawable.minWidth = playTopW
        topDrawable.minHeight = playTopH
        val topButton = ImageButton(topDrawable)
        topButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.setScreen(HighScoreScreen(game))
            }
        })

        table.add(playButton).size(playTopW, playTopH).padRight(Constants.BUTTON_PAD_RIGHT)
        table.add(exitButton).size(exitSize, exitSize).padRight(Constants.BUTTON_PAD_RIGHT)
        table.add(topButton).size(playTopW, playTopH)

        stage.addActor(table)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(Constants.DARK_GRAY, Constants.DARK_GRAY, Constants.DARK_GRAY, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        if (::stage.isInitialized) stage.dispose()
        if (::logoTexture.isInitialized) logoTexture.dispose()
        if (::playTexture.isInitialized) playTexture.dispose()
        if (::exitTexture.isInitialized) exitTexture.dispose()
        if (::topTexture.isInitialized) topTexture.dispose()
    }
}
