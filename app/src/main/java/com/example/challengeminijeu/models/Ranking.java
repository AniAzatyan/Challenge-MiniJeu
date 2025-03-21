package com.example.challengeminijeu.models;

import com.google.firebase.firestore.DocumentId;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Ranking {

    @DocumentId
    private String id;
    private String userName;
    private int score;
    private int nbHand;
    private int nbFingers;

}
