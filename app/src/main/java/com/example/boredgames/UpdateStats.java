package com.example.boredgames;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UpdateStats {
    public UpdateStats() {
        db = FirebaseFirestore.getInstance();
    }
    public static final String TAG = "UpdateStats Class";
    public static final String COLLECTION_PATH = "stats";
    FirebaseFirestore db;

    public void updateWin(String dispName, long wins) {
        DocumentReference docRef = db.collection(COLLECTION_PATH).document(dispName);
        docRef
                .update("win", wins)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "wins updated!");
                    }
                });
    }

    public void incrementWin(String dispName) {
        DocumentReference docRef = db.collection(COLLECTION_PATH).document(dispName);
        docRef
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot docSnap) {
                        long wins = (long) docSnap.get("win");
                        wins++;
                        updateWin(dispName, wins);
                    }
                });
    }

    public void incrementDraw(String dispName) {
        DocumentReference docRef = db.collection(COLLECTION_PATH).document(dispName);
        docRef
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot docSnap) {
                        long draws = (long) docSnap.get("draw");
                        draws++;
                        updateDraw(dispName, draws);
                    }
                });
    }
    public void updateDraw(String dispName, long draws) {
        DocumentReference docRef = db.collection(COLLECTION_PATH).document(dispName);
        docRef
                .update("draw", draws)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "draws updated!");
                    }
                });
    }

    public void incrementLoss(String dispName) {
        DocumentReference docRef = db.collection(COLLECTION_PATH).document(dispName);
        docRef
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot docSnap) {
                        long losses = (long) docSnap.get("draw");
                        losses++;
                        updateLoss(dispName, losses);
                    }
                });
    }
    public void updateLoss(String dispName, long losses) {
        DocumentReference docRef = db.collection(COLLECTION_PATH).document(dispName);
        docRef
                .update("loss", losses)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "losses updated!");
                    }
                });
    }

    public void checkName(String dispName) {
        DocumentReference docRef = db.collection(COLLECTION_PATH).document(dispName);
        docRef
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot docSnap) {
                        if (docSnap.exists()) {
                            // Display name is taken, do nothing
                        } else {
                            createUser(dispName);
                        }
                    }
                });
    }

    public void createUser(String dispName) {
        DocumentReference docRef = db.collection(COLLECTION_PATH).document(dispName);
        Map<String, Number> user = new HashMap<>();
        user.put("win", 0);
        user.put("draw", 0);
        user.put("loss", 0);
        docRef
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "new user created!");
                    }
                });
    }
};
