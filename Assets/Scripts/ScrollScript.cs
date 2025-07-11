using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class ScrollScript : MonoBehaviour {
    float scrollSpeed = -0.2f;
    Vector2 startPos;
    // Start is called before the first frame update
    void Start() {
        startPos = new Vector3(-5, 0, 0);
        transform.position = startPos;
    }

    // Update is called once per frame
    void Update() {
        float newPos = Mathf.Repeat(Time.time * scrollSpeed, 8);
        transform.position = startPos + Vector2.right * newPos;
        //Debug.Log("Posx is: " + transform.position.x);
        //if (transform.position.x < -4) {
        //    startPos = new Vector3(-4, 0, 0);
        //    transform.position = startPos;
        //}
    }
}
