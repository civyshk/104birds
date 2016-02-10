package net.project104.civyshkbirds;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ComboPicture implements Serializable{
    Picture picture;
    List<String> answers;
    int correctAnswer, selectedAnswer;
    ComboPicture(){
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
    public void setPicture(Picture pic){
        picture = pic;
    }
    public Picture getPicture(){
        return picture;
    }
    String getAnswer(int idx){
        return answers.get(idx);
    }
}