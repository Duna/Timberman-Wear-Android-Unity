using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class TreeRootScript : MonoBehaviour
{
    // Start is called before the first frame update
    void Start()
    {
        
    }

    // Update is called once per frame
    void Update()
    {
        
    }

    void OnTriggerEnter2D(Collider2D obj) {
        if (obj.gameObject.CompareTag("leaf")) {
           //Debug.Log("Collider2D leaf");
           // obj.gameObject.SetActive(false);
            Destroy(obj.gameObject);
        }
        if (obj.gameObject.CompareTag("trunk")) {
          //  Debug.Log("Collider2D trunk1");
          // obj.gameObject.SetActive(false);
            Destroy(obj.gameObject);
        }
    }
}