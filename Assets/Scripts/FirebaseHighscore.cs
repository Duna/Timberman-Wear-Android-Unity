using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using Firebase;
using Firebase.Database;
using System.Threading.Tasks;
using System;

public class FirebaseHighscore : MonoBehaviour {
    DatabaseReference dbref;
    private static Dictionary<long, UserScore> listScore;

    [Serializable]
    public class UserScore {
        public string Name;
        public long Timestamp;
        public long Score;

        public UserScore(string name, long score, long timestamp) {
            Name = name;
            Score = score;
            Timestamp = timestamp;
        }
    }

    public static FirebaseHighscore Instance;

    // Explicit static constructor to tell C# compiler
    // not to mark type as beforefieldinit
    void Awake() {
        if (Instance != null) {
            Destroy(gameObject);
        }
        else {
            Instance = this;
            DontDestroyOnLoad(gameObject);
        }
        LoadData();
    }

    private void LoadData() {
        listScore = new Dictionary<long, UserScore>();

        dbref = FirebaseDatabase.DefaultInstance.RootReference.Child("top");
        dbref.ValueChanged += HandleValueChanged;
    }

    public void setScore(long score, long previousScore) {
        UpdateScore(PlayerPrefs.GetString("playerName"), score, previousScore);
    }

    public Dictionary<long, UserScore> getScores() {
        return listScore;
    }

    void HandleValueChanged(object sender, ValueChangedEventArgs args) {
        if (args.DatabaseError != null) {
            Debug.LogError(args.DatabaseError.Message);
            return;
        }
        //Debug.Log(args.Snapshot.Value.ToString());
        listScore.Clear();
        listScore = null;
        listScore = new Dictionary<long, UserScore>();
        foreach (DataSnapshot userS in args.Snapshot.Children) {
            string user = (string)userS.Value;
            Debug.Log("" + user);
            UserScore us = JsonUtility.FromJson<UserScore>(user);
            listScore.Add(us.Score, us);
        }
    }

    private void UpdateScore(string username, long score, long previousScore) {
        long timestamp = DateTime.UtcNow.Ticks / TimeSpan.TicksPerSecond;
        var scoreOb = new UserScore(username, score, timestamp);
        DatabaseReference userDbRef = dbref.Child("" + score);

        string json = JsonUtility.ToJson(scoreOb);
        userDbRef.SetValueAsync(json);

        //remove previous score
        DatabaseReference lastRef = dbref.Child("" + previousScore);
        lastRef.SetValueAsync(null); //to get notified by value changed
        lastRef.RemoveValueAsync();
        Debug.LogWarning("Task score end");
    }
}
