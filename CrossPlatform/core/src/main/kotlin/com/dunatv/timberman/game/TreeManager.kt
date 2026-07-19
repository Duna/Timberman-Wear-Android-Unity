package com.dunatv.timberman.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.dunatv.timberman.firebase.FirebaseService
import com.dunatv.timberman.firebase.UserScore
import com.dunatv.timberman.util.Constants
import kotlin.random.Random

class TreeManager {
    data class TrunkSegment(
        var y: Float,
        var branchOnLeft: Boolean
    )

    private val segments = mutableListOf<TrunkSegment>()
    private var index = 0
    private var trunkHeight = 0f
    private var rootHeight = 0f

    private lateinit var trunkTexture: Texture
    private lateinit var branchTexture: Texture
    private lateinit var rootTexture: Texture
    private lateinit var treeCenterTexture: Texture

    private var trunkRegion: TextureRegion? = null
    private var rootRegion: TextureRegion? = null

    private val scale = Constants.TRUNK_SCALE

    private var scores: List<UserScore> = emptyList()
    private var currentScore = 0

    fun load() {
        trunkTexture = Texture(Gdx.files.internal("textures/tree_trunk.png"))
        rootTexture = Texture(Gdx.files.internal("textures/tree_root.png"))

        trunkRegion = TextureRegion(trunkTexture)
        rootRegion = TextureRegion(rootTexture)

        trunkHeight = trunkTexture.height / 100f * scale
        rootHeight = rootTexture.height / 100f * scale
    }

    fun initialize(firebaseScores: List<UserScore>) {
        scores = firebaseScores
        currentScore = 0
        segments.clear()
        index = 0

        var y = getGroundY() + rootHeight
        for (i in 0 until Constants.TRUNK_COUNT) {
            val branchLeft = if (i == 0) true else Random.nextBoolean()
            segments.add(TrunkSegment(y, branchLeft))
            y += trunkHeight
        }
    }

    fun chop(playerIsLeft: Boolean, score: Int): Boolean {
        currentScore = score
        val bottom = segments[index]
        val died = if (playerIsLeft) bottom.branchOnLeft else !bottom.branchOnLeft

        moveDown()
        return died
    }

    fun checkNextCollision(playerIsLeft: Boolean): Boolean {
        val next = segments[index]
        return if (playerIsLeft) next.branchOnLeft else !next.branchOnLeft
    }

    private fun moveDown() {
        val topY = segments.maxOf { it.y }
        val bottom = segments[index]
        bottom.y = topY + trunkHeight
        bottom.branchOnLeft = Random.nextBoolean()

        index = (index + 1) % segments.size

        for (segment in segments) {
            segment.y -= trunkHeight
        }
    }

    fun render(batch: SpriteBatch) {
        val trunkW = trunkTexture.width / 100f * scale
        val trunkH = trunkHeight

        rootRegion?.let { root ->
            val rootW = rootTexture.width / 100f * scale
            val rootH = rootHeight
            batch.draw(root, -rootW / 2f, getGroundY(), rootW, rootH)
        }

        for (segment in segments) {
            batch.draw(trunkRegion, -trunkW / 2f, segment.y, trunkW, trunkH)

            val branchW = trunkW * 0.6f
            val branchH = trunkH * 0.5f
            val branchY = segment.y + trunkH * 0.25f

            if (segment.branchOnLeft) {
                batch.draw(trunkRegion, -trunkW / 2f - branchW * 0.7f, branchY, branchW, branchH)
            } else {
                batch.draw(trunkRegion, trunkW / 2f - branchW * 0.3f, branchY, branchW, branchH)
            }
        }
    }

    fun getGroundY(): Float = Constants.TREE_ROOT_Y

    fun enablePlayerHit(player: Player) {
        player.enableHit()
    }

    fun dispose() {
        if (::trunkTexture.isInitialized) trunkTexture.dispose()
        if (::rootTexture.isInitialized) rootTexture.dispose()
    }
}
