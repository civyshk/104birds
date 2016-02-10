package net.project104.civyshkbirds;

import android.content.res.Resources;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ComboName implements Serializable{
    String question;
    List<Picture> pictures;
    int correctAnswer, selectedAnswer;
    ComboName(){
        pictures = new ArrayList<Picture>();
        selectedAnswer = -1;
    }
    public void setQuestion(String name){
        question = name;
    }
    public String getQuestion(Resources res){
        return String.format(res.getString(R.string.which_is_x), question);
    }
    public Picture getAnswer(int idx){
        return pictures.get(idx);
    }
    public void addPicture(Picture pic){
        pictures.add(pic);
    }
    public void setCorrectAnswer(int idx){
        correctAnswer = idx;
    }
    public void setSelectedAnswer(int idx){
        selectedAnswer = idx;
    }
}
