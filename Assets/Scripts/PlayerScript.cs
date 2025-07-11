using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class PlayerScript : MonoBehaviour {
    private TrunkManager tm;
    public Animator anim;
    private const float ANIM_HIT_TIME = 0.1f;

    private float targetTime = ANIM_HIT_TIME;
    private bool hitHey = false;
    private bool up = false;
    //private bool isLeft = false;
    private bool isLastLeft = false;
    //private int isTouchLeft = 0;

    void Start() {

    }

    private void OnEnable() {
        gameObject.transform.localRotation = Quaternion.Euler(0, 180, 0);
        Vector3 pos1 = new Vector3(1.60f, -2.4f, 0);
        gameObject.transform.position = pos1;
        anim.SetInteger("player_state", 0);

        PlayerPrefs.SetInt("score", 0);
        GameManager.Instance.setPlayerCanHit(true);
    }

    public void setListener(TrunkManager tmm) {
        tm = tmm;
    }

    // Update is called once per frame
    void Update() {
        // if (!isGameStarted) return;
#if UNITY_EDITOR
        if (Input.GetKeyDown("left")) {
            //isTouchLeft = 1;
            handleKey(true);
        }
        if (Input.GetKeyDown("right")) {
            //isTouchLeft = 2;
            handleKey(false);
        }
#endif
#if UNITY_ANDROID
        if (Input.touchCount > 0) {
            Touch touch = Input.GetTouch(0);
            if (touch.phase == TouchPhase.Began) {
                float width = Screen.width / 2;
                if (touch.position.x < width) {
                    handleKey(true);
                }
                else {
                    handleKey(false);
                }
            }
        }
#endif
        //anim.SetBool("hit_key", false);
        if (hitHey) {
            targetTime -= Time.deltaTime;
            //Debug.Log("Time is: " + targetTime);
            if (targetTime <= 0.0f) {
                hitHey = false;
                anim.SetInteger("player_state", 0);
                //tm.Hit(up);
            }
        }
    }
    void doAnimation(bool isLeft) {
        anim.SetInteger("player_state", 1);
        hitHey = true;
        targetTime = ANIM_HIT_TIME;
        movePlayer(isLeft);
    }

    private void handleKey(bool isLeft) {
        doAnimation(isLeft);

        TapTapScript.Instance.SetInvisible();

        if (GameManager.Instance.playerCanHit()) {
            GameManager.Instance.setPlayerCanHit(false);
            tm.Hit(false);
            increaseScore();
            GameManager.Instance.setPlayerMoved(true);
        }
        else {
            //Handheld.Vibrate();
        }
    }

    private void movePlayer(bool isLeft) {
        if (isLastLeft != isLeft) {
            isLastLeft = isLeft;
            if (isLeft) {
                Vector3 pos = new Vector3(-1.60f, -2.4f, 0);
                gameObject.transform.position = pos;
                transform.localRotation = Quaternion.Euler(0, 0, 0);
            }
            else {
                transform.localRotation = Quaternion.Euler(0, 180, 0);
                Vector3 pos = new Vector3(1.60f, -2.4f, 0);
                gameObject.transform.position = pos;
            }
        }
    }

    private void increaseScore() {
        int score = PlayerPrefs.GetInt("score", 0);
        PlayerPrefs.SetInt("score", score + 1);
    }

    internal void setScale(Vector2 scale) {
        //aAwake(scale);
    }

    void OnTriggerEnter2D(Collider2D obj) {
        if (obj.gameObject.CompareTag("diePlayer")) {
            Debug.Log("Game Over Duna");
            anim.SetInteger("player_state", 2);
            GameManager.Instance.SetGameOverScreen();
        }
    }
}
