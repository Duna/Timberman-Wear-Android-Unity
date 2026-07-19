package com.dunatv.timberman.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.FitViewport
import com.dunatv.timberman.TimbermanGame

class WelcomeScreen(game: TimbermanGame) : BaseScreen(game) {
    private lateinit var stage: Stage
    private lateinit var skin: Skin
    private lateinit var logoTexture: Texture

    override fun show() {
        skin = game.createSkin()
        stage = Stage(FitViewport(450f, 450f))
        Gdx.input.inputProcessor = stage

        logoTexture = Texture(Gdx.files.internal("textures/timber_logo.png"))

        val table = Table()
        table.setFillParent(true)
        table.center()

        val nameField = TextField("", skin)
        nameField.messageText = "Enter your name"
        nameField.setAlignment(1)
        table.add(nameField).width(250f).height(50f).padBottom(20f)
        table.row()

        val confirmButton = TextButton("OK", skin)
        confirmButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                val name = nameField.text.trim()
                if (name.isNotEmpty()) {
                    game.prefs.setPlayerName(name)
                    game.setScreen(StartScreen(game))
                }
            }
        })
        table.add(confirmButton).width(150f).height(50f)

        stage.addActor(table)
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
        if (::logoTexture.isInitialized) logoTexture.dispose()
    }
}
