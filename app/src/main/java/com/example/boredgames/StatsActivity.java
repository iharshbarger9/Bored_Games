package com.example.boredgames;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

public class StatsActivity extends AppCompatActivity {
    private static final String TAG = "Stats Activity";
    public static final String DOCUMENT_PATH = "stats/global stats";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        //update stats button
        Button update_button = (Button) findViewById(R.id.update_button);
        update_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateStats();
            }
        });

        //updateStats();
        //testingFirestore();
    }

    public void updateStats() {
        FirebaseFirestore stats_db = FirebaseFirestore.getInstance();
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
    }
}