using System.Collections;
using System.Collections.Generic;
using TMPro;
using UnityEngine;
using UnityEngine.UI;

public class ScrollListItemm : MonoBehaviour {
    [SerializeField]
    private TextMeshProUGUI myName;
    [SerializeField]
    private TextMeshProUGUI myScore;

    public void setText(string text, string score) {
        myName.SetText(text);
        myScore.SetText(score);
        if (text.Equals(PlayerPrefs.GetString("playerName"))) {
            Debug.Log("Text highlight exuals ");
            myName.color = new Color(131f / 255.0f, 10f / 255.0f, 32f / 255.0f, 1);
        }
        else {
            myName.color = new Color(250f / 255.0f, 241f / 255.0f, 213f / 255.0f, 1);
            Debug.Log("Text highlight not ");
        }
    }
}