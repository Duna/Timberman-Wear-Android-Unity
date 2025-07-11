using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class TapTapScript : MonoBehaviour {

    public static TapTapScript Instance;

    public Image image;
    // Start is called before the first frame update
    void Start() {
        if (PlayerPrefs.GetInt("tapshown", 0) == 0) {
            PlayerPrefs.SetInt("tapshown", 1);
            image.gameObject.SetActive(true);
            StartAnimation();
        }
        else {
            image.gameObject.SetActive(false);
        }
    }

    void Awake() {
        if (Instance != null) {
            Destroy(gameObject);
        }
        else {
            Instance = this;
            DontDestroyOnLoad(gameObject);
        }
    }

    private void StartAnimation() {
        StartCoroutine(FadeImage(true));
    }

    IEnumerator FadeImage(bool fadeAway) {
        // fade from opaque to transparent
        if (fadeAway) {
            // loop over 1 second backwards
            for (float i = 1; i >= 0; i -= Time.deltaTime) {
                // set color with i as alpha
                image.color = new Color(1, 1, 1, i);
                yield return null;
            }
            StartCoroutine(FadeImage(false));
        }
        // fade from transparent to opaque
        else {
            // loop over 1 second
            for (float i = 0; i <= 1; i += Time.deltaTime) {
                // set color with i as alpha
                image.color = new Color(1, 1, 1, i);
                yield return null;
            }
            StartCoroutine(FadeImage(true));
        }
    }

    public void SetInvisible() {
        if (image.gameObject.activeSelf == true) {
            image.gameObject.SetActive(false);
            StopCoroutine(FadeImage(true));
            StopCoroutine(FadeImage(false));
        }
    }
}
