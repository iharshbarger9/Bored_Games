package com.example.boredgames;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

public class StatsActivity extends AppCompatActivity {
    private static final String TAG = "Stats Activity";
    public static final String DOCUMENT_PATH = "stats/global stats";
    public static final String COLLECTION_PATH = "stats";

    FirebaseFirestore stats_db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        //constructing leaderboard
        TextView first = findViewById(R.id.first_spot);
        TextView second = findViewById(R.id.second_spot);
        TextView third = findViewById(R.id.third_spot);
        TextView fourth = findViewById(R.id.fourth_spot);
        TextView fifth = findViewById(R.id.fifth_spot);

        stats_db.collection(COLLECTION_PATH)
                .orderBy("win", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int i = 1;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                switch(i) {
                                    case 1:
                                        first.setText(String.format("%s : %s-%s-%s", document.getId(), document.get("win"), document.get("draw"), document.get("loss")));
                                        break;
                                    case 2:
                                        second.setText(String.format("%s : %s-%s-%s", document.getId(), document.get("win"), document.get("draw"), document.get("loss")));
                                        break;
                                    case 3:
                                        third.setText(String.format("%s : %s-%s-%s", document.getId(), document.get("win"), document.get("draw"), document.get("loss")));
                                        break;
                                    case 4:
                                        fourth.setText(String.format("%s : %s-%s-%s", document.getId(), document.get("win"), document.get("draw"), document.get("loss")));
                                        break;
                                    case 5:
                                        fifth.setText(String.format("%s : %s-%s-%s", document.getId(), document.get("win"), document.get("draw"), document.get("loss")));
                                        break;
                                }
                                i++;
                            }
                        } else {
                            Log.d(TAG, "database query was unsuccessful");
                        }
                    }
                });

        //find player button
        EditText search_et = findViewById(R.id.hs_search_tb);
        Button search_bt = findViewById(R.id.hs_search_bt);
        TextView search_results = findViewById(R.id.search_results);
        search_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_et.getText();
                stats_db.collection(COLLECTION_PATH).document(search_et.getText().toString()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot document) {
                        if (document.exists()) {
                            search_results.setText(document.getId() + " : " + document.get("win") + "-" + document.get("draw") + "-" + document.get("loss"));
                        } else {
                            search_results.setText("Player not found");
                        }
                    }
                });
            }
        });

        //updateStats();
        //testingFirestore();
    }

  /*  public void updateStats() {
        //FirebaseFirestore stats_db = FirebaseFirestore.getInstance();
        stats_db.document(DOCUMENT_PATH).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    String winloss = documentSnapshot.getString("lood");
                    Log.d(TAG, "");

                    TextView testing = (TextView) findViewById(R.id.text_testUpdate);
                    testing.setText(winloss);
                }
            }
        });
    }

    public void testingFirestore() {
        FirebaseFirestore stats_db = FirebaseFirestore.getInstance();
        DocumentReference statsRef = stats_db.document("stats/global stats");
        statsRef
                .update("lood", "beebee wins 0 losses")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "score updated");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "failed");
            }
        });
    } */
}