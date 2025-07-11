using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class CollisionDetection : MonoBehaviour
{
    string currentTag = "tag";

    private void OnCollisionEnter2D(Collision2D obj) {
        string tagu = obj.gameObject.name;
        Debug.Log("Collision name: " + tagu);
        if (tagu.Contains("branchHit")) {
            if (tagu.Equals(currentTag) == false) {
                currentTag = tagu;
                GameManager.Instance.setPlayerCanHit(true);
            }
        }
    }
}
