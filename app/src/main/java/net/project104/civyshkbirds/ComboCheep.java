package net.project104.civyshkbirds;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ComboCheep implements Serializable {
    Cheep cheep;
    List<String> answers;
    int correctAnswer, selectedAnswer;
    ComboCheep(){
        answers = new ArrayList<String>();
        selectedAnswer = -1;
    }
    public void addAnswer(String name){
        answers.add(name);
    }
    public void setCorrectAnswer(int idx){
        correctAnswer = idx;
    }
    public void setSelectedAnswer(int idx){
        selectedAnswer = idx;
    }
    public void setCheep(Cheep cheep){
        this.cheep = cheep;
    }
    public Cheep getCheep(){
        return cheep;
    }
    String getAnswer(int idx){
        return answers.get(idx);
    }
}