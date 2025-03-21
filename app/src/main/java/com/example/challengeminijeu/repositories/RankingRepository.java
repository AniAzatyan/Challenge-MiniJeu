package com.example.challengeminijeu.repositories;

import android.util.Log;

import com.example.challengeminijeu.models.Ranking;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.function.Consumer;

public class RankingRepository {

    private static final String TAG = "AvisRepository";
    private final CollectionReference rankingCollection;

    public RankingRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        rankingCollection = db.collection("ranking");
    }

    public void addRanking(Ranking ranking, Consumer<Boolean> callback) {
        rankingCollection.document(ranking.getId()).set(ranking, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Ranking added : " + ranking.getId());
                    callback.accept(true);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error when ranking added", e);
                    e.printStackTrace();
                    callback.accept(false);
                });
    }

}
