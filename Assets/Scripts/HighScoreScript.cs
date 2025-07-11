using System;
using System.Collections;
using System.Collections.Generic;
using TMPro;
using UnityEngine;
using UnityEngine.UI;

public class HighScoreScript : MonoBehaviour {
    public static int value;
    private TextMeshProUGUI textScore;
    private FirebaseHighscore fire;

    void Start() {
        textScore = GetComponent<TextMeshProUGUI>();
        fire = FirebaseHighscore.Instance;
    }

    // Update is called once per frame
    void Update() {
        //PlayerPrefs.DeleteAll();
        int bestScore = PlayerPrefs.GetInt("highscore", 0);
        int score = PlayerPrefs.GetInt("score", 0);
        if (bestScore < score) {
            PlayerPrefs.SetInt("highscore", score);
            setFirebaseScore(score, bestScore);
            bestScore = score;
        }
        textScore.SetText("Best: " + bestScore);
    }

    private void setFirebaseScore(int score, int previousScore) {
        fire.setScore(score, previousScore);
    }
}
