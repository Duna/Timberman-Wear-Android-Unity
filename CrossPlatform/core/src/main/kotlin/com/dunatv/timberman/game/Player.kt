package com.dunatv.timberman.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Array as GdxArray
import com.dunatv.timberman.util.Constants

class Player {
    enum class State { IDLE, HIT, DEAD }

    var state = State.IDLE
        private set
    var isLeft = false
        private set
    private var stateTime = 0f
    private var hitTimer = 0f
    private var canHit = true
    private var hasMoved = false

    private lateinit var playerTexture: Texture
    private lateinit var ripTexture: Texture
    private lateinit var idleAnim: Animation<TextureRegion>
    private lateinit var hitAnim: Animation<TextureRegion>
    private lateinit var deathAnim: Animation<TextureRegion>

    private var width = 0f
    private var height = 0f
    var groundY = Constants.TREE_ROOT_Y

    fun load() {
        playerTexture = Texture(Gdx.files.internal("sprites/player.png"))
        ripTexture = Texture(Gdx.files.internal("sprites/rip_sprites.png"))

        // Unity rects use bottom-left origin (x, yFromBottom, w, h) in a 64px tall sheet
        // libGDX TextureRegion uses top-left: y = texHeight - unityY - h
        // player1: unity(0,26,51,38) -> libgdx(0, 64-26-38, 51, 38) = (0,0,51,38)
        val idle1 = TextureRegion(playerTexture, 0, 0, 51, 38)
        val idle2 = TextureRegion(playerTexture, 58, 0, 51, 38)
        val hit1 = TextureRegion(playerTexture, 116, 0, 51, 38)
        val hit2 = TextureRegion(playerTexture, 174, 0, 58, 38)

        val rip1 = TextureRegion(ripTexture, 0, 0, 51, 38)
        val rip2 = TextureRegion(ripTexture, 51, 0, 51, 38)

        // Use first frame's actual pixel dimensions for aspect ratio
        val frameW = 51f
        val frameH = 38f
        width = frameW / 100f * Constants.PLAYER_SCALE
        height = frameH / 100f * Constants.PLAYER_SCALE

        idleAnim = Animation(Constants.IDLE_FRAME_DURATION, GdxArray(arrayOf(idle1, idle2)), Animation.PlayMode.LOOP)
        hitAnim = Animation(Constants.HIT_FRAME_DURATION, GdxArray(arrayOf(hit1, hit2)), Animation.PlayMode.LOOP)
        deathAnim = Animation(Constants.DEATH_FRAME_DURATION, GdxArray(arrayOf(rip1, rip2)), Animation.PlayMode.LOOP)
    }

    fun reset() {
        isLeft = false
        state = State.IDLE
        stateTime = 0f
        hitTimer = 0f
        canHit = true
        hasMoved = false
    }

    fun handleInput(): Boolean {
        if (state == State.DEAD) return false

        var touched = false
        var tappedLeft = false

        if (Gdx.input.justTouched()) {
            val screenX = Gdx.input.getX().toFloat()
            val screenW = Gdx.graphics.width.toFloat()
            tappedLeft = screenX < screenW / 2f
            touched = true
        }

        if (!touched) return false

        moveTo(tappedLeft)
        state = State.HIT
        stateTime = 0f
        hitTimer = Constants.HIT_ANIM_DURATION

        canHit = true
        hasMoved = true
        return true
    }

    fun enableHit() {
        canHit = true
    }

    fun consumeMove(): Boolean {
        if (hasMoved) {
            hasMoved = false
            return true
        }
        return false
    }

    fun update(delta: Float) {
        stateTime += delta
        if (state == State.HIT) {
            hitTimer -= delta
            if (hitTimer <= 0) {
                state = State.IDLE
            }
        }
    }

    fun die() {
        state = State.DEAD
        stateTime = 0f
    }

    fun getX(): Float = if (isLeft) Constants.PLAYER_LEFT_X else Constants.PLAYER_RIGHT_X
    fun getY(): Float = Constants.PLAYER_Y

    fun render(batch: SpriteBatch) {
        val frame = when (state) {
            State.IDLE -> idleAnim.getKeyFrame(stateTime)
            State.HIT -> hitAnim.getKeyFrame(stateTime)
            State.DEAD -> deathAnim.getKeyFrame(stateTime)
        }

        val drawX = getX() - width / 2f
        val drawY = groundY

        if (isLeft) {
            batch.draw(frame, drawX, drawY, width, height)
        } else {
            batch.draw(frame, drawX + width, drawY, -width, height)
        }
    }

    fun dispose() {
        if (::playerTexture.isInitialized) playerTexture.dispose()
        if (::ripTexture.isInitialized) ripTexture.dispose()
    }

    private fun moveTo(left: Boolean) {
        isLeft = left
    }
}
