package com.dunatv.timberman.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.dunatv.timberman.util.Constants
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class DragonAttack {
    enum class Phase { IDLE, FLYING_IN, BREATHING, BURNING, FLYING_OUT, DONE }

    private lateinit var dragonTexture: Texture
    private lateinit var flameTextures: Array<Texture>
    private lateinit var flyFrames: Array<TextureRegion>
    private lateinit var breathFrames: Array<TextureRegion>
    private lateinit var allFlameFrames: Array<Array<TextureRegion>>

    var phase = Phase.IDLE
        private set
    private var timer = 0f
    private var wingTimer = 0f
    private var flameTimer = 0f
    private var dragonX = 0f
    private var dragonY = 0f
    private var baseY = 0f
    private var flyTime = 0f
    private var startX = 0f
    private var breathX = 0f
    private var flyProgress = 0f
    private var rotationAngle = 0f
    private var bobBlend = 0f
    private var arrivalSpeed = 0f

    private data class BranchFlame(
        val x: Float, val y: Float,
        var alpha: Float, var scale: Float,
        val frameOffset: Int,
        val timeOffset: Float,
        val flameType: Int
    )
    private val branchFlames = mutableListOf<BranchFlame>()

    val isActive get() = phase != Phase.IDLE && phase != Phase.DONE
    var branchAlpha = 1f
        private set

    fun load() {
        dragonTexture = Texture(Gdx.files.internal("textures/dragon.png"))
        flameTextures = arrayOf(
            Texture(Gdx.files.internal("textures/flame.png")),
            Texture(Gdx.files.internal("textures/flame2.png")),
            Texture(Gdx.files.internal("textures/flame3.png"))
        )

        val frameW = dragonTexture.width / Constants.DRAGON_FRAME_COUNT
        val frameH = dragonTexture.height
        flyFrames = arrayOf(
            TextureRegion(dragonTexture, 0, 0, frameW, frameH),
            TextureRegion(dragonTexture, frameW, 0, frameW, frameH)
        )
        breathFrames = arrayOf(
            TextureRegion(dragonTexture, frameW * 2, 0, frameW, frameH),
            TextureRegion(dragonTexture, frameW * 3, 0, frameW, frameH)
        )

        allFlameFrames = Array(flameTextures.size) { t ->
            val tex = flameTextures[t]
            val flameFrameW = tex.width / Constants.FLAME_FRAME_COUNT
            val flameFrameH = tex.height
            Array(Constants.FLAME_FRAME_COUNT) { i ->
                TextureRegion(tex, i * flameFrameW, 0, flameFrameW, flameFrameH)
            }
        }
    }

    fun trigger(branchPositions: List<Pair<Float, Float>>) {
        phase = Phase.FLYING_IN
        timer = 0f
        wingTimer = 0f
        flameTimer = 0f
        startX = Constants.WORLD_WIDTH / 2f + 2f
        dragonX = startX
        baseY = Constants.WORLD_HEIGHT / 2f - 3f
        dragonY = baseY
        flyTime = 0f
        breathX = startX * 0.3f
        flyProgress = 0f
        branchFlames.clear()
        for ((bx, by) in branchPositions) {
            branchFlames.add(BranchFlame(bx, by, 0f, 0.5f + Random.nextFloat() * 0.5f, Random.nextInt(Constants.FLAME_FRAME_COUNT), Random.nextFloat() * Constants.FLAME_FRAME_DURATION * Constants.FLAME_FRAME_COUNT, Random.nextInt(flameTextures.size)))
        }
    }

    fun update(delta: Float): Boolean {
        if (phase == Phase.IDLE || phase == Phase.DONE) return false
        timer += delta
        wingTimer += delta
        flameTimer += delta

        flyTime += delta

        when (phase) {
            Phase.FLYING_IN -> {
                val totalDist = startX - breathX
                val flyDuration = totalDist / Constants.DRAGON_FLY_SPEED
                flyProgress = (flyProgress + delta / flyDuration).coerceAtMost(1f)
                val eased = easeInOut(flyProgress)
                dragonX = startX + (breathX - startX) * eased
                dragonY = baseY + sin(flyTime * Constants.DRAGON_BOB_SPEED).toFloat() * Constants.DRAGON_BOB_AMOUNT
                rotationAngle = 0f
                branchAlpha = 1f
                if (flyProgress >= 1f) {
                    dragonX = breathX
                    phase = Phase.BREATHING
                    timer = 0f
                    flyProgress = 0f
                    bobBlend = 0f
                    arrivalSpeed = Constants.DRAGON_FLY_SPEED * 0.5f
                }
            }
            Phase.BREATHING -> {
                bobBlend = (bobBlend + delta * 2f).coerceAtMost(1f)
                val flyBob = sin(flyTime * Constants.DRAGON_BOB_SPEED).toFloat() * Constants.DRAGON_BOB_AMOUNT
                val breathBob = sin(flyTime * Constants.DRAGON_BREATH_BOB_SPEED).toFloat() * Constants.DRAGON_BREATH_BOB_AMOUNT
                dragonY = baseY + flyBob + (breathBob - flyBob) * bobBlend
                rotationAngle = cos(flyTime * Constants.DRAGON_BREATH_BOB_SPEED).toFloat() * Constants.DRAGON_BREATH_ROTATION * bobBlend

                arrivalSpeed = (arrivalSpeed - delta * Constants.DRAGON_FLY_SPEED).coerceAtLeast(0f)
                dragonX -= arrivalSpeed * delta

                for (flame in branchFlames) {
                    flame.alpha = (flame.alpha + delta * 3f).coerceAtMost(1f)
                }
                val fadeProgress = easeIn((timer / Constants.DRAGON_BREATH_DURATION).coerceAtMost(1f))
                branchAlpha = 1f - 0.7f * fadeProgress
                if (timer >= Constants.DRAGON_BREATH_DURATION) {
                    phase = Phase.BURNING
                    timer = 0f
                }
            }
            Phase.BURNING -> {
                dragonY = baseY + sin(flyTime * Constants.DRAGON_BREATH_BOB_SPEED).toFloat() * Constants.DRAGON_BREATH_BOB_AMOUNT
                rotationAngle = cos(flyTime * Constants.DRAGON_BREATH_BOB_SPEED).toFloat() * Constants.DRAGON_BREATH_ROTATION
                val burnProgress = easeIn((timer / Constants.DRAGON_BURN_DURATION).coerceAtMost(1f))
                branchAlpha = 0.3f * (1f - burnProgress)
                for (flame in branchFlames) {
                    flame.scale = flame.scale + delta * 0.3f
                    flame.alpha = (1f - timer / Constants.DRAGON_BURN_DURATION).coerceAtLeast(0f)
                }
                dragonX -= Constants.DRAGON_FLY_SPEED * delta * 0.3f
                if (timer >= Constants.DRAGON_BURN_DURATION) {
                    phase = Phase.FLYING_OUT
                    timer = 0f
                    bobBlend = 0f
                }
            }
            Phase.FLYING_OUT -> {
                val exitX = -Constants.WORLD_WIDTH / 2f - 3f
                val flyDuration = (startX - breathX) / Constants.DRAGON_FLY_SPEED
                flyProgress = (flyProgress + delta / flyDuration).coerceAtMost(1f)
                val eased = easeIn(flyProgress)
                dragonX -= eased * Constants.DRAGON_FLY_SPEED * delta * 2f

                bobBlend = (bobBlend + delta * 2f).coerceAtMost(1f)
                val breathBob = sin(flyTime * Constants.DRAGON_BREATH_BOB_SPEED).toFloat() * Constants.DRAGON_BREATH_BOB_AMOUNT
                val flyBob = sin(flyTime * Constants.DRAGON_BOB_SPEED).toFloat() * Constants.DRAGON_BOB_AMOUNT
                dragonY = baseY + breathBob + (flyBob - breathBob) * bobBlend

                val breathRot = cos(flyTime * Constants.DRAGON_BREATH_BOB_SPEED).toFloat() * Constants.DRAGON_BREATH_ROTATION
                rotationAngle = breathRot * (1f - bobBlend)

                if (dragonX < exitX) {
                    phase = Phase.DONE
                    return true
                }
            }
            else -> {}
        }

        return false
    }

    fun renderFlames(batch: SpriteBatch) {
        if (phase == Phase.IDLE || phase == Phase.DONE) return

        val oldColor = batch.color.cpy()
        for (flame in branchFlames) {
            if (flame.alpha > 0f) {
                val flameTex = flameTextures[flame.flameType]
                val flameFrameW = flameTex.width / Constants.FLAME_FRAME_COUNT
                val fw = flameFrameW / 100f * Constants.TRUNK_SCALE * flame.scale
                val fh = flameTex.height / 100f * Constants.TRUNK_SCALE * flame.scale
                val frameIdx = ((flameTimer + flame.timeOffset) / Constants.FLAME_FRAME_DURATION).toInt() % Constants.FLAME_FRAME_COUNT
                batch.setColor(1f, 1f, 1f, flame.alpha)
                batch.draw(allFlameFrames[flame.flameType][frameIdx], flame.x - fw / 2f, flame.y, fw, fh)
            }
        }

        batch.color = oldColor
    }

    fun renderDragon(batch: SpriteBatch) {
        if (phase == Phase.IDLE || phase == Phase.DONE) return

        val frameW = dragonTexture.width / Constants.DRAGON_FRAME_COUNT
        val dragonW = frameW / 100f * Constants.DRAGON_SCALE
        val dragonH = dragonTexture.height / 100f * Constants.DRAGON_SCALE

        val wingIdx = (wingTimer / Constants.DRAGON_WING_SPEED).toInt() % 2
        val useBreathFrames = phase == Phase.BREATHING || phase == Phase.BURNING ||
            (phase == Phase.FLYING_OUT && timer < 0.3f)
        val frames = if (useBreathFrames) breathFrames else flyFrames
        val region = frames[wingIdx]
        val drawX = dragonX - dragonW / 2f
        val originX = dragonW * 0.15f
        val originY = dragonH * 0.5f
        batch.draw(region, drawX, dragonY, originX, originY, dragonW, dragonH, 1f, 1f, rotationAngle)
    }

    fun getRandomBlueFlameX(): Float? {
        val blueFlames = branchFlames.filter { it.flameType == 1 }
        if (blueFlames.isEmpty()) return null
        return blueFlames[Random.nextInt(blueFlames.size)].x
    }

    fun reset() {
        phase = Phase.IDLE
        timer = 0f
        wingTimer = 0f
        flameTimer = 0f
        branchAlpha = 1f
        branchFlames.clear()
    }

    private fun easeInOut(t: Float): Float {
        return if (t < 0.5f) 2f * t * t else 1f - (-2f * t + 2f).let { it * it } / 2f
    }

    private fun easeIn(t: Float): Float = t * t

    fun dispose() {
        if (::dragonTexture.isInitialized) dragonTexture.dispose()
        if (::flameTextures.isInitialized) flameTextures.forEach { it.dispose() }
    }
}
