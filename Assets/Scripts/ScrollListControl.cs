using System.Collections;
using System.Collections.Generic;
using System.Linq;
using UnityEngine;
using static FirebaseHighscore;

public class ScrollListControl : MonoBehaviour {
    [SerializeField]
    public GameObject textTemplate;

    private static Dictionary<long, UserScore> listScore;
    private static List<GameObject> listObjects;

    private void Awake() {
        listObjects = new List<GameObject>();
        var v = FirebaseHighscore.Instance;
    }

    private void OnEnable() {
        if (FirebaseHighscore.Instance != null) {
            listScore = FirebaseHighscore.Instance.getScores();
            if (listScore != null) {
                List<long> list = listScore.Keys.ToList();
                list.Sort();
                list.Reverse();

                foreach (long index in list) {
                    GameObject text = Instantiate(textTemplate) as GameObject;
                    listObjects.Add(text);
                    text.SetActive(true);
                    text.GetComponent<ScrollListItemm>().setText(listScore[index].Name, " " + index);
                    text.transform.SetParent(textTemplate.transform.parent, false);
                }
            }
        }
    }

    private void OnDisable() {
        foreach (GameObject go in listObjects) {
            if (go != null) {
                go.gameObject.SetActive(false);
                Destroy(go.gameObject);
            }
        }
        listObjects.Clear();
    }
}
