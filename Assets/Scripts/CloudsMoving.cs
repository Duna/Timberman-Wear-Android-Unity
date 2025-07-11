using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class CloudsMoving : MonoBehaviour {
    public GameObject cloudOne;
    public GameObject cloudTwo;

    GameObject objCloudOne;
    GameObject objCloudTwo;
    private List<GameObject> cloudList;
    float width = 0f;
    float shiftSpeed = 0.4f;
    Vector3 tail;

    void Start() {
        instantiateTreeTrunks();
    }

    private void instantiateTreeTrunks() {
        Vector3 v = new Vector3(0, 0, 0);
        objCloudOne = Instantiate(cloudOne, v, Quaternion.identity);

        if (width.Equals(0f)) {
            width = objCloudOne.GetComponentInChildren<SpriteRenderer>().bounds.size.x;
            Debug.Log("Width cloud is: " + width);
        }

        cloudList = new List<GameObject>();
        cloudList.Add(objCloudOne);
        for (int i = 1; i < 3; i++) {
            addChunk(i * width);
        }
        tail = cloudList[cloudList.Count - 1].transform.position;
    }

    private void addChunk(float x) {
        Vector3 pos = new Vector3(x, 0, 0);
        int left = UnityEngine.Random.Range(0, 100);
        GameObject go;
        if (left % 2 == 0) {
            go = Instantiate(cloudOne, pos, Quaternion.identity);
        }
        else {
            go = Instantiate(cloudTwo, pos, Quaternion.identity);
        }

        cloudList.Add(go);
    }

    void Update() {
        Shift();
    }

    void Shift() {
        for (int i = 0; i < cloudList.Count; i++) {
            GameObject go = cloudList[i];
            go.transform.position += Vector3.left * shiftSpeed * Time.deltaTime;
            CheckOutOfScreen(go);
        }
    }

    private void CheckOutOfScreen(GameObject go) {
        if (go.transform.position.x < -width) {
            go.transform.position = tail;
        }
    }
}
