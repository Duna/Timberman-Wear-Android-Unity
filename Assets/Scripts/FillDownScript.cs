using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class FillDownScript : MonoBehaviour {
    public int fillInt;
    private bool isFirstRun = false;
    Image fill;

    private void Awake() {
        fill = GetComponent<Image>();
    }

    void OnEnable() {
        if (isActiveAndEnabled && fill != null/* && isFirstRun*/) {
            fill.fillAmount = 1;
            fillInt = 10;
            InvokeRepeating("DecreaseFill", 0f, 0.4f);
        }
        isFirstRun = true;
    }

    private void OnDisable() {
        CancelInvoke("DecreaseFill");
    }

    private void Update() {
        if  (GameManager.Instance.playerHasMoved()) {
            GameManager.Instance.setPlayerMoved(false);
            increaseFill();
        }
    }

    void DecreaseFill() {
        fill.fillAmount = fillInt / 10f;
        if (fillInt > 0) {
            fillInt -= 1;
        }
        else {
            //game over
            CancelInvoke("DecreaseFill");
            GameManager.Instance.OnPlayerDied();
        }
    }

    void increaseFill() {
        if (fillInt < 10) {
            fillInt += 1;
            fill.fillAmount = fillInt / 10f;
        }
    }
}
