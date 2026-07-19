package com.dunatv.timberman.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.FitViewport
import com.dunatv.timberman.TimbermanGame
import com.dunatv.timberman.firebase.UserScore
import com.dunatv.timberman.util.Constants

class HighScoreScreen(game: TimbermanGame) : BaseScreen(game) {
    private lateinit var stage: Stage
    private lateinit var skin: Skin
    private lateinit var backTexture: Texture

    override fun show() {
        skin = game.createSkin()
        stage = Stage(FitViewport(450f, 450f))
        Gdx.input.inputProcessor = stage

        backTexture = Texture(Gdx.files.internal("textures/back.png"))

        val rootTable = Table()
        rootTable.setFillParent(true)
        rootTable.top().pad(20f)

        val titleLabel = Label("High Scores", skin)
        titleLabel.setFontScale(1.3f)
        rootTable.add(titleLabel).padBottom(15f)
        rootTable.row()

        val listTable = Table()
        val playerName = game.prefs.getPlayerName()

        game.firebaseService.getScores { scores ->
            Gdx.app.postRunnable {
                val sorted = scores.sortedByDescending { it.score }
                for ((idx, userScore) in sorted.withIndex()) {
                    val nameLabel = Label("${idx + 1}. ${userScore.name}", skin)
                    val scoreLabel = Label("${userScore.score}", skin)

                    if (userScore.name == playerName) {
                        val selfColor = Color(
                            Constants.LEADERBOARD_SELF_R,
                            Constants.LEADERBOARD_SELF_G,
                            Constants.LEADERBOARD_SELF_B,
                            1f
                        )
                        nameLabel.color = selfColor
                        scoreLabel.color = selfColor
                    } else {
                        val otherColor = Color(
                            Constants.LEADERBOARD_NAME_R,
                            Constants.LEADERBOARD_NAME_G,
                            Constants.LEADERBOARD_NAME_B,
                            1f
                        )
                        nameLabel.color = otherColor
                        scoreLabel.color = otherColor
                    }

                    listTable.add(nameLabel).expandX().left().padRight(10f)
                    listTable.add(scoreLabel).right()
                    listTable.row().padTop(5f)
                }
            }
        }

        val scrollPane = ScrollPane(listTable, skin)
        scrollPane.setScrollingDisabled(true, false)
        rootTable.add(scrollPane).expand().fill().padBottom(15f)
        rootTable.row()

        val backButton = ImageButton(TextureRegionDrawable(TextureRegion(backTexture)))
        backButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.setScreen(StartScreen(game))
            }
        })
        rootTable.add(backButton).size(60f, 60f)

        stage.addActor(rootTable)
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
        if (::backTexture.isInitialized) backTexture.dispose()
    }
}
