package com.dunatv.timberman.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport
import com.dunatv.timberman.TimbermanGame
import com.dunatv.timberman.util.Constants

abstract class BaseScreen(protected val game: TimbermanGame) : Screen {
    protected val camera = OrthographicCamera()
    protected val viewport = FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera)
    protected val batch: SpriteBatch get() = game.batch

    init {
        camera.position.set(0f, 0f, 0f)
        camera.update()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }

    protected fun clearScreen() {
        Gdx.gl.glClearColor(0.2f, 0.6f, 0.3f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    }

    override fun show() {}
    override fun hide() {}
    override fun pause() {}
    override fun resume() {}
    override fun dispose() {}
}
