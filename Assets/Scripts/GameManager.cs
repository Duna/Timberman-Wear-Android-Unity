using System;
using UnityEngine;
using UnityEngine.UI;

public class GameManager : MonoBehaviour {

    public delegate void GameDelegate();
    public static event GameDelegate OnGameStarted;
    public static event GameDelegate OnGameOverConfirmed;
    bool playerHit;

    public static GameManager Instance;

    public InputField textName;
    public GameObject welcomePage;
    public GameObject startPage;
    public GameObject gameOverPage;
    public GameObject countdownPage;
    public GameObject highScore;
    public Text scoreText;
    private bool isMoved;
    private PageState lastState = PageState.None;

    enum PageState {
        None,
        Start,
        Countdown,
        GameOver,
        HighScore
    }

    int score = 0;
    bool gameOver = true;


    void Awake() {
        if (Instance != null) {
            Destroy(gameObject);
        }
        else {
            Instance = this;
            DontDestroyOnLoad(gameObject);
        }
        if (System.String.IsNullOrEmpty(PlayerPrefs.GetString("playerName"))) {
            SetPageState(PageState.None);
        }
        else {
            SetPageState(PageState.Start);
        }

    }

    public bool playerHasMoved() {
        return isMoved;
    }

    public void setPlayerMoved(bool isMoving) {
        isMoved = isMoving;
    }

    public bool GameOver { get { return gameOver; } }

    public void setPlayerCanHit(bool canHit) {
        Debug.Log("Player allowed to hit: " + canHit);
        playerHit = canHit;
    }

    public bool playerCanHit() {
        // Debug.Log("Player can get hit manager: " + playerHit);
        return playerHit;
    }

    public void OnWelcomeButton() {
        if (System.String.IsNullOrEmpty(textName.text)) {

        }
        else {
            PlayerPrefs.SetString("playerName", textName.text);
            SetPageState(PageState.Start);
        }
    }

    void OnEnable() {
        playerHit = true;
        /* TapController.OnPlayerDied += OnPlayerDied;
         TapController.OnPlayerScored += OnPlayerScored;
         CountdownText.OnCountdownFinished += OnCountdownFinished;*/
    }

    void OnDisable() {
        /* TapController.OnPlayerDied -= OnPlayerDied;
         TapController.OnPlayerScored -= OnPlayerScored;
         CountdownText.OnCountdownFinished -= OnCountdownFinished;*/
    }

    void OnCountdownFinished() {
        Debug.Log("Game started");
        SetPageState(PageState.None);
        OnGameStarted();
        score = 0;
        gameOver = false;
    }

    void OnPlayerScored() {
        score++;
        scoreText.text = score.ToString();
    }

    public void OnPlayerDied() {
        gameOver = true;
        int savedScore = PlayerPrefs.GetInt("HighScore");
        if (score > savedScore) {
            PlayerPrefs.SetInt("HighScore", score);
        }
        SetPageState(PageState.GameOver);
    }

    void SetPageState(PageState state) {
        Debug.Log("Page State: " + state);
        switch (state) {
            case PageState.None:
                highScore.SetActive(false);
                welcomePage.SetActive(true);
                startPage.SetActive(false);
                gameOverPage.SetActive(false);
                countdownPage.SetActive(false);
                lastState = state;
                break;
            case PageState.Start:
                highScore.SetActive(false);
                welcomePage.SetActive(false);
                startPage.SetActive(true);
                gameOverPage.SetActive(false);
                countdownPage.SetActive(false);
                lastState = state;
                break;
            case PageState.Countdown:
                highScore.SetActive(false);
                welcomePage.SetActive(false);
                startPage.SetActive(false);
                gameOverPage.SetActive(false);
                countdownPage.SetActive(true);
                lastState = state;
                break;
            case PageState.GameOver:
                highScore.SetActive(false);
                welcomePage.SetActive(false);
                startPage.SetActive(true);
                gameOverPage.SetActive(true);
                countdownPage.SetActive(false);
                lastState = state;
                break;
            case PageState.HighScore:
                highScore.SetActive(true);
                welcomePage.SetActive(false);
                startPage.SetActive(false);
                gameOverPage.SetActive(false);
                countdownPage.SetActive(false);
                break;
        }
    }

    public void SetGameOverScreen() {
        SetPageState(PageState.GameOver);
    }

    public void ConfirmGameOver() {
        SetPageState(PageState.Start);
        scoreText.text = "0";
        OnGameOverConfirmed();
    }

    public void StartGame() {
        SetPageState(PageState.Countdown);
    }

    public void HighScoretGame() {
        SetPageState(PageState.HighScore);
    }

    public void GoBack() {
        SetPageState(lastState);
    }

    public void OnExit() {
        Application.Quit();
    }
}
