package com.dunatv.timberman

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle
import com.dunatv.timberman.util.Constants
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.dunatv.timberman.firebase.FirebaseService
import com.dunatv.timberman.screen.StartScreen
import com.dunatv.timberman.screen.WelcomeScreen
import com.dunatv.timberman.util.Prefs

class TimbermanGame(val firebaseService: FirebaseService) : Game() {
    lateinit var batch: SpriteBatch
    lateinit var font: BitmapFont
    lateinit var smallFont: BitmapFont
    lateinit var tinyFont: BitmapFont
    lateinit var largeFont: BitmapFont
    lateinit var prefs: Prefs

    override fun create() {
        batch = SpriteBatch()
        prefs = Prefs()
        firebaseService.initialize()

        val generator = FreeTypeFontGenerator(Gdx.files.internal("fonts/font.ttf"))

        val param = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = Constants.FONT_SIZE
            color = Color.WHITE
        }
        font = generator.generateFont(param)
        font.setUseIntegerPositions(false)
        font.data.setScale(Constants.FONT_SCALE)

        val smallParam = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = Constants.SMALL_FONT_SIZE
            color = Color.WHITE
        }
        smallFont = generator.generateFont(smallParam)
        smallFont.setUseIntegerPositions(false)
        smallFont.data.setScale(Constants.SMALL_FONT_SCALE)

        val tinyParam = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = Constants.TINY_FONT_SIZE
            color = Color.WHITE
        }
        tinyFont = generator.generateFont(tinyParam)
        tinyFont.setUseIntegerPositions(false)
        tinyFont.data.setScale(Constants.TINY_FONT_SCALE)

        val largeParam = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = Constants.LARGE_FONT_SIZE
            color = Color.WHITE
        }
        largeFont = generator.generateFont(largeParam)
        largeFont.setUseIntegerPositions(false)
        largeFont.data.setScale(Constants.LARGE_FONT_SCALE)

        generator.dispose()

        if (prefs.hasPlayerName()) {
            setScreen(StartScreen(this))
        } else {
            setScreen(WelcomeScreen(this))
        }
    }

    fun createSkin(): Skin {
        val skin = Skin()

        val generator = FreeTypeFontGenerator(Gdx.files.internal("fonts/font.ttf"))
        val param = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = Constants.UI_FONT_SIZE
            color = Color.WHITE
        }
        val uiFont = generator.generateFont(param)
        generator.dispose()

        skin.add("default-font", uiFont)

        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fill()
        val whiteTexture = Texture(pixmap)
        pixmap.dispose()
        skin.add("white", whiteTexture)

        val whiteDrawable = TextureRegionDrawable(TextureRegion(whiteTexture))

        val buttonUp = whiteDrawable.tint(Color(0.3f, 0.5f, 0.2f, 1f))
        val buttonDown = whiteDrawable.tint(Color(0.2f, 0.4f, 0.15f, 1f))

        val textButtonStyle = TextButtonStyle().apply {
            font = uiFont
            fontColor = Color.WHITE
            up = buttonUp
            down = buttonDown
        }
        skin.add("default", textButtonStyle)

        val labelStyle = LabelStyle().apply {
            font = uiFont
            fontColor = Color.WHITE
        }
        skin.add("default", labelStyle)

        val cursor = whiteDrawable.tint(Color.WHITE)
        val selection = whiteDrawable.tint(Color(0.3f, 0.3f, 0.7f, 0.5f))
        val textFieldBg = whiteDrawable.tint(Color(0.15f, 0.15f, 0.15f, 0.8f))

        val textFieldStyle = TextFieldStyle().apply {
            font = uiFont
            fontColor = Color.WHITE
            this.cursor = cursor
            this.selection = selection
            background = textFieldBg
            messageFontColor = Color.GRAY
        }
        skin.add("default", textFieldStyle)

        val scrollPaneStyle = ScrollPaneStyle()
        skin.add("default", scrollPaneStyle)

        return skin
    }

    override fun dispose() {
        batch.dispose()
        font.dispose()
        smallFont.dispose()
        tinyFont.dispose()
        largeFont.dispose()
    }
}
