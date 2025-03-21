package com.example.challengeminijeu.models;

import com.google.firebase.firestore.DocumentId;

import lombok.Builder;
import lombok.Data;

@Data
public class Ranking {

    @DocumentId
    private String id;
    private String userName;
    private int score;
    private int nbHand;
    private int nbFingers;

    public Ranking(String userName, int score, int nbHand, int nbFingers) {
        this.userName = userName;
        this.score = score;
        this.nbHand = nbHand;
        this.nbFingers = nbFingers;
    }
}
