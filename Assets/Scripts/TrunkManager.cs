using System;
using System.Collections;
using System.Collections.Generic;
using TMPro;
using UnityEngine;
using UnityEngine.UI;

public class TrunkManager : MonoBehaviour, IPlayerHit<Boolean> {
    public GameObject treeTrunk;
    public GameObject treeRoot;
    public GameObject treeCenter;
    public GameObject scoreTop;
    public GameObject player;
    private List<GameObject> trunkObjects;
    private List<GameObject> centerTrunkObjects;
    private List<GameObject> scoreObjects;

    private Dictionary<long, FirebaseHighscore.UserScore> scores;
    private Vector2 scale;
    private int index;
    float sizeYtrunk = 0f;
    float sizeYroot = 0f;
    float topY;
    GameObject objPlayer;
    GameObject objRoot;

    private float maxY;
    // Start is called before the first frame update
    void Start() {
        SpriteRenderer spriteRenderer = GetComponent<SpriteRenderer>();

        float cameraHeight = Camera.main.orthographicSize * 2;
        Vector2 cameraSize = new Vector2(Camera.main.aspect * cameraHeight, cameraHeight);
        Vector2 spriteSize = spriteRenderer.sprite.bounds.size;
        scale = transform.localScale;
        if (cameraSize.x >= cameraSize.y) { // Landscape (or equal)
            scale *= cameraSize.x / spriteSize.x;
        }
        else { // Portrait
            scale *= cameraSize.y / spriteSize.y;
        }

        transform.position = Vector2.zero; // Optional
        transform.localScale = scale;
    }

    void OnEnable() {
        index = 0;
        Debug.Log("Trunk Manager OnEnable");
        scores = FirebaseHighscore.Instance.getScores();
        instantiateTreeTrunks();
    }

    private void instantiateTreeTrunks() {
        Vector3 v = new Vector3(0, -3f, 0);
        objRoot = Instantiate(treeRoot, v, Quaternion.identity);

        if (sizeYroot.Equals(0f)) {
            sizeYroot = objRoot.GetComponent<BoxCollider2D>().size.y * 4f;
        }
        scoreObjects = new List<GameObject>();
        trunkObjects = new List<GameObject>();
        centerTrunkObjects = new List<GameObject>();
        float y = -3f + sizeYroot;
        for (int i = 0; i < 10; i++) {
            maxY = y;
            addChunk(i == 0, y);
            addScore(i, y);
            y += sizeYtrunk;
        }
        GameObject top = trunkObjects[trunkObjects.Count - 1];
        topY = top.transform.position.y;
        Vector3 poss = new Vector3(0, 0, 0);
        objPlayer = Instantiate(player, poss, Quaternion.identity);
        PlayerScript script = objPlayer.GetComponent<PlayerScript>();
        script.setListener(this);
        script.setScale(scale);
    }

    private void addScore(int position, float y) {
        Vector3 pos = new Vector3(3.95f, y, 0);
        GameObject go = Instantiate(scoreTop, pos, Quaternion.identity);
        scoreObjects.Add(go);
        go.gameObject.SetActive(false);
    }

    private void addChunk(bool isFirst, float yPos) {
        Vector3 pos = new Vector3(0, yPos, 0);
        GameObject go = Instantiate(treeTrunk, pos, Quaternion.identity);
        centerTrunkObjects.Add(Instantiate(treeCenter, pos, Quaternion.identity));
        string myTag = "branchHit" + yPos.ToString();
        go.name = myTag;
        Debug.Log("Name: " + myTag);

        if (sizeYtrunk.Equals(0f)) {
            sizeYtrunk = go.GetComponent<BoxCollider2D>().size.y * 4f;
        }
        int left = UnityEngine.Random.Range(0, 100);
        if (left % 2 == 0 || isFirst) {
            go.transform.localRotation = Quaternion.Euler(0, 0, 0);
        }
        else {
            go.transform.localRotation = Quaternion.Euler(0, 180, 0);
        }
        trunkObjects.Add(go);
    }

    // Update is called once per frame
    void Update() {
        for (int i = 0; i < 10; i++) {
            TextMeshProUGUI tx = scoreObjects[i].GetComponentInChildren<TextMeshProUGUI>();
            Vector3 pos = trunkObjects[i].transform.position;
            pos.x = 3.95f;

            tx.transform.position = pos;
            scoreObjects[i].transform.position = pos;
        }
    }

    private readonly object syncLock = new object();

    private void moveDown() {
        lock (syncLock) {
            Debug.Log("Current Index is: " + index);
            GameObject bottom = trunkObjects[index];
            Vector3 v = new Vector3(0, topY + sizeYtrunk, 0);
            bottom.transform.position = v;
            int left = UnityEngine.Random.Range(0, 100);
            if (left % 2 == 0) {
                bottom.transform.localRotation = Quaternion.Euler(0, 0, 0);
            }
            else {
                bottom.transform.localRotation = Quaternion.Euler(0, 180, 0);
            }
         
            scoreObjects[index].name = "" + (PlayerPrefs.GetInt("score", 0) + 10);
            if (scores.ContainsKey(PlayerPrefs.GetInt("score", 0) + 10)) {
                GameObject scoreObject = scoreObjects[index].gameObject;
                TextMeshProUGUI tm = scoreObject.GetComponentInChildren<TextMeshProUGUI>();

                string text = scores[PlayerPrefs.GetInt("score", 0) + 10].Name;
                if (text.Equals(PlayerPrefs.GetString("playerName"))) {
                    tm.color = new Color32(205, 89, 75, 255);
                }
                else {
                    tm.color = new Color32(255, 255, 255, 255);
                }

                Vector3 v3 = new Vector3(6f, topY + sizeYtrunk, 0);

                tm.SetText("");
                scoreObject.SetActive(true);
                scoreObject.transform.position = v3;
                tm.transform.position = v3;
                tm.SetText(text);
            }
            else {
                scoreObjects[index].gameObject.SetActive(false);
            }
            //scoreObjects[index].gameObject.SetActive(true);
            index++;
            index %= trunkObjects.Count;
        }
    }

    public void Hit(bool hitUp) {
        //if (!PlayerScript.isGameStarted) return;
        // if (hitUp) {
        //     moveUp();
        // }
        // else {
        moveDown();
        // }
    }

    private void OnDisable() {
        disposeAll();
    }

    private void disposeAll() {
        if (trunkObjects != null) {
            foreach (GameObject go in trunkObjects) {
                if (go != null) {
                    go.gameObject.SetActive(false);
                    Destroy(go.gameObject);
                }
            }

            foreach (GameObject go in scoreObjects) {
                if (go != null) {
                    go.gameObject.SetActive(false);
                    Destroy(go.gameObject);
                }
            }

            foreach (GameObject go in centerTrunkObjects) {
                if (go != null) {
                    go.gameObject.SetActive(false);
                    Destroy(go.gameObject);
                }
            }
            trunkObjects.Clear();
            trunkObjects = null;
            scoreObjects.Clear();
            scoreObjects = null;
            centerTrunkObjects.Clear();
            centerTrunkObjects = null;
            Destroy(objPlayer.gameObject);
            Destroy(objRoot.gameObject);
        }
    }
}

