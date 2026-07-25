package com.dunatv.timberman.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.dunatv.timberman.firebase.UserScore
import com.dunatv.timberman.util.Constants
import kotlin.random.Random

class TreeManager {
    enum class BranchSide { LEFT, RIGHT, NONE }

    data class TrunkSegment(
        var y: Float,
        var branch: BranchSide
    )

    private val segments = mutableListOf<TrunkSegment>()
    private var index = 0
    private var trunkHeight = 0f
    private var rootHeight = 0f

    private lateinit var trunkTexture: Texture
    private lateinit var leafTexture: Texture
    private lateinit var rootTexture: Texture

    private var trunkRegion: TextureRegion? = null
    private var leafRegion: TextureRegion? = null
    private var leafFlippedRegion: TextureRegion? = null
    private var rootRegion: TextureRegion? = null

    private val scale = Constants.TRUNK_SCALE

    private var scores: List<UserScore> = emptyList()
    private var currentScore = 0

    fun load() {
        trunkTexture = Texture(Gdx.files.internal("textures/tree_trunk.png"))
        leafTexture = Texture(Gdx.files.internal("textures/leaf.png"))
        rootTexture = Texture(Gdx.files.internal("textures/tree_root.png"))

        trunkRegion = TextureRegion(trunkTexture)
        leafRegion = TextureRegion(leafTexture)
        leafFlippedRegion = TextureRegion(leafTexture)
        leafFlippedRegion!!.flip(true, false)
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
            val branch = if (i == 0) BranchSide.NONE else randomBranch()
            segments.add(TrunkSegment(y, branch))
            y += trunkHeight
        }
    }

    fun chop(playerIsLeft: Boolean, score: Int): Boolean {
        currentScore = score
        moveDown()

        val nextBottom = segments[index]
        val died = when (nextBottom.branch) {
            BranchSide.LEFT -> playerIsLeft
            BranchSide.RIGHT -> !playerIsLeft
            BranchSide.NONE -> false
        }

        Gdx.app.log("TimbermanTree", "chop: index=$index, fallingBranch=${nextBottom.branch}, playerIsLeft=$playerIsLeft, died=$died")
        logTree(playerIsLeft)
        if (died) {
            Gdx.app.log("TimbermanTree", "DEATH: player on ${if (playerIsLeft) "LEFT" else "RIGHT"}, falling branch on ${nextBottom.branch}")
        }

        return died
    }

    fun checkNextCollision(playerIsLeft: Boolean): Boolean {
        val nextIndex = (index + 1) % segments.size
        val next = segments[nextIndex]
        return when (next.branch) {
            BranchSide.LEFT -> playerIsLeft
            BranchSide.RIGHT -> !playerIsLeft
            BranchSide.NONE -> false
        }
    }

    private fun moveDown() {
        val topY = segments.maxOf { it.y }
        val bottom = segments[index]
        bottom.y = topY + trunkHeight
        bottom.branch = randomBranch()

        index = (index + 1) % segments.size

        for (segment in segments) {
            segment.y -= trunkHeight
        }
    }

    private fun randomBranch(): BranchSide {
        return when (Random.nextInt(3)) {
            0 -> BranchSide.LEFT
            1 -> BranchSide.RIGHT
            else -> BranchSide.NONE
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

        val branchW = leafTexture.width / 100f * scale
        val branchH = leafTexture.height / 100f * scale

        for (segment in segments) {
            batch.draw(trunkRegion, -trunkW / 2f, segment.y, trunkW, trunkH)

            if (segment.branch != BranchSide.NONE) {
                val branchY = segment.y + (trunkH - branchH) / 2f

                if (segment.branch == BranchSide.LEFT) {
                    batch.draw(leafRegion, -trunkW / 2f - branchW + branchW * 0.1f, branchY, branchW, branchH)
                } else {
                    batch.draw(leafFlippedRegion, trunkW / 2f - branchW * 0.1f, branchY, branchW, branchH)
                }
            }
        }
    }

    private fun logTree(playerIsLeft: Boolean) {
        val sb = StringBuilder("\n--- TREE STATE (player on ${if (playerIsLeft) "LEFT" else "RIGHT"}) ---\n")
        val sortedByY = segments.sortedByDescending { it.y }
        for ((i, seg) in sortedByY.withIndex()) {
            val isBottom = seg == segments[index]
            val branchStr = when (seg.branch) {
                BranchSide.LEFT -> "[B]---[T]    "
                BranchSide.RIGHT -> "    [T]---[B]"
                BranchSide.NONE -> "    [T]      "
            }
            val marker = if (isBottom) " <-- PLAYER" else ""
            sb.append("  $branchStr  y=${String.format("%.2f", seg.y)}$marker\n")
        }
        sb.append("--- END TREE ---")
        Gdx.app.log("TimbermanTree", sb.toString())
    }

    fun getGroundY(): Float = Constants.TREE_ROOT_Y

    fun enablePlayerHit(player: Player) {
        player.enableHit()
    }

    fun dispose() {
        if (::trunkTexture.isInitialized) trunkTexture.dispose()
        if (::leafTexture.isInitialized) leafTexture.dispose()
        if (::rootTexture.isInitialized) rootTexture.dispose()
    }
}
