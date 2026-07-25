package com.dunatv.timberman.util

object Constants {
    // World
    const val WORLD_WIDTH = 10f
    const val WORLD_HEIGHT = 10f

    // Player
    const val PLAYER_LEFT_X = -1.4f
    const val PLAYER_RIGHT_X = 1.4f
    const val PLAYER_Y = -2.4f
    const val PLAYER_SCALE = 3.5f

    // Tree
    const val TREE_ROOT_Y = -3.2f
    const val TRUNK_COUNT = 10
    const val TRUNK_SCALE = 4f
    const val BRANCH_OFFSET_X = -0.3695f

    // Timer
    const val TIMER_DRAIN_INTERVAL = 2.0f
    const val TIMER_MAX_TICKS = 10

    // Animation
    const val HIT_ANIM_DURATION = 0.1f
    const val IDLE_FRAME_DURATION = 0.5f
    const val HIT_FRAME_DURATION = 1f / 12f
    const val DEATH_FRAME_DURATION = 1f / 3f
    const val TAP_HINT_FADE_SPEED = 2f

    // Background & Clouds
    const val BG_SCROLL_SPEED = 0.2f
    const val BACK_CLOUD_COUNT = 3
    const val BACK_CLOUD_SPEED = 0.3f
    const val FRONT_CLOUD_COUNT = 2
    const val FRONT_CLOUD_SPEED = 0.8f

    // Countdown
    const val COUNTDOWN_SECONDS = 3

    // UI Stage
    const val UI_VIEWPORT_SIZE = 450f
    const val LOGO_SIZE = 200f
    const val LOGO_PAD_BOTTOM = 15f
    const val BUTTON_SIZE = 60f
    const val BUTTON_ASPECT_RATIO = 37f / 23f
    const val BUTTON_PAD_RIGHT = 10f

    // HUD
    const val SCORE_MARKER_X = 3.95f
    const val HUD_SCORE_OFFSET_Y = 0.2f
    const val HUD_BEST_OFFSET_Y = 0.7f
    const val TIMER_BAR_X = -2f
    const val TIMER_BAR_WIDTH = 4f
    const val TIMER_BAR_HEIGHT = 0.2f
    const val TIMER_BAR_BOTTOM_OFFSET = 0.5f

    // Tap hint
    const val TAP_HINT_Y = -1f

    // Welcome screen
    const val NAME_FIELD_WIDTH = 250f
    const val NAME_FIELD_HEIGHT = 50f
    const val NAME_FIELD_PAD_BOTTOM = 20f

    // High score screen
    const val BACK_BUTTON_HEIGHT = 60f
    const val BACK_BUTTON_ASPECT_RATIO = 37f / 23f
    const val SCORE_LIST_PAD_BOTTOM = 15f
    const val SCORE_LIST_PAD_RIGHT = 10f
    const val SCORE_LIST_PAD_TOP = 5f

    // Game over screen
    const val GAMEOVER_SCORE_PAD_BOTTOM = 8f
    const val GAMEOVER_BEST_PAD_BOTTOM = 12f

    // Fonts
    const val FONT_SIZE = 48
    const val FONT_SCALE = 0.04f
    const val SMALL_FONT_SIZE = 32
    const val SMALL_FONT_SCALE = 0.03f
    const val TINY_FONT_SIZE = 24
    const val TINY_FONT_SCALE = 0.02f
    const val LARGE_FONT_SIZE = 96
    const val LARGE_FONT_SCALE = 0.08f
    const val UI_FONT_SIZE = 24
    const val TITLE_FONT_SCALE = 1.3f

    // Colors (RGB floats)
    const val CLEAR_R = 0.2f
    const val CLEAR_G = 0.6f
    const val CLEAR_B = 0.3f

    const val DARK_GRAY = 0.15f

    const val HIGHLIGHT_R = 205f / 255f
    const val HIGHLIGHT_G = 89f / 255f
    const val HIGHLIGHT_B = 75f / 255f

    const val LEADERBOARD_NAME_R = 250f / 255f
    const val LEADERBOARD_NAME_G = 241f / 255f
    const val LEADERBOARD_NAME_B = 213f / 255f

    const val LEADERBOARD_SELF_R = 131f / 255f
    const val LEADERBOARD_SELF_G = 10f / 255f
    const val LEADERBOARD_SELF_B = 32f / 255f
}
