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
        stage = Stage(FitViewport(Constants.UI_VIEWPORT_SIZE, Constants.UI_VIEWPORT_SIZE))
        Gdx.input.inputProcessor = stage

        backTexture = Texture(Gdx.files.internal("textures/back.png"))

        val rootTable = Table()
        rootTable.setFillParent(true)
        rootTable.top().pad(20f)

        val titleLabel = Label("High Scores", skin)
        titleLabel.setFontScale(Constants.TITLE_FONT_SCALE)
        rootTable.add(titleLabel).padBottom(Constants.SCORE_LIST_PAD_BOTTOM)
        rootTable.row()

        val listTable = Table()
        val playerName = game.prefs.getPlayerName()

        val cachedScores = game.firebaseService.getScoresSync()
        Gdx.app.log("TimbermanGame", "HighScoreScreen: cachedScores=${cachedScores.size}")
        if (cachedScores.isNotEmpty()) {
            populateScores(listTable, cachedScores, playerName)
        } else {
            listTable.add(Label("Loading...", skin)).center()
            listTable.row()
        }

        Gdx.app.log("TimbermanGame", "HighScoreScreen: calling getScores...")
        game.firebaseService.getScores { scores ->
            Gdx.app.log("TimbermanGame", "HighScoreScreen: getScores callback, got ${scores.size} scores")
            Gdx.app.postRunnable {
                listTable.clear()
                val allScores = if (scores.isNotEmpty()) scores else cachedScores
                if (allScores.isEmpty()) {
                    Gdx.app.log("TimbermanGame", "HighScoreScreen: no scores to display")
                    listTable.add(Label("No scores yet", skin)).center()
                    return@postRunnable
                }
                Gdx.app.log("TimbermanGame", "HighScoreScreen: displaying ${allScores.size} scores")
                populateScores(listTable, allScores, playerName)
            }
        }

        val scrollPane = ScrollPane(listTable, skin)
        scrollPane.setScrollingDisabled(true, false)
        rootTable.add(scrollPane).expand().fill().padBottom(Constants.SCORE_LIST_PAD_BOTTOM)
        rootTable.row()

        val backH = Constants.BACK_BUTTON_HEIGHT
        val backW = backH * Constants.BACK_BUTTON_ASPECT_RATIO
        val backDrawable = TextureRegionDrawable(TextureRegion(backTexture))
        backDrawable.minWidth = backW
        backDrawable.minHeight = backH
        val backButton = ImageButton(backDrawable)
        backButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.setScreen(StartScreen(game))
            }
        })
        rootTable.add(backButton).size(backW, backH)

        stage.addActor(rootTable)
    }

    private fun populateScores(table: Table, scores: List<UserScore>, playerName: String) {
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

            table.add(nameLabel).expandX().left().padRight(Constants.SCORE_LIST_PAD_RIGHT)
            table.add(scoreLabel).right()
            table.row().padTop(Constants.SCORE_LIST_PAD_TOP)
        }
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
