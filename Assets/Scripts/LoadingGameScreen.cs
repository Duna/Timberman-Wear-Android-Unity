using System.Collections;
using System.Collections.Generic;
using TMPro;
using UnityEngine;
using UnityEngine.UI;

public class LoadingGameScreen : MonoBehaviour {
    public TextMeshProUGUI loadingText;
    // Start is called before the first frame update
    void Start() {
        StartCoroutine(LoadYourAsyncScene());
    }

    // Update is called once per frame
    void Update() {

    }

    IEnumerator LoadYourAsyncScene() {
        AsyncOperation asyncLoad = UnityEngine.SceneManagement.SceneManager.LoadSceneAsync("Scenes/GameScene");

        // Wait until the asynchronous scene fully loads
        while (!asyncLoad.isDone) {
            if (loadingText != null) {
                loadingText.SetText("Loading... " + (asyncLoad.progress * 100) + "%");
            }
            yield return null;
        }
    }
}
