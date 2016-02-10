package net.project104.civyshkbirds;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Bird implements Serializable {
    final String latinName, family;
    List<Picture> pictures;
    List<Cheep> cheeps;
    //I randomly add final qualifier to fields

    static final Random rand;

    static {
        rand = new Random();
    }

    public Bird(String name, String family){
        this.latinName = name;
        this.family = family;
        pictures = new ArrayList<Picture>();
        cheeps = new ArrayList<Cheep>();

    }

    public void addPicture(Picture pic){
        pictures.add(pic);
    }

    public void addCheep(Cheep cheep){
        cheeps.add(cheep);
    }

    public Picture getRandomPicture(){
        return pictures.get(rand.nextInt(pictures.size()));
    }

    public Cheep getRandomCheep(){
        return cheeps.get(rand.nextInt(cheeps.size()));
    }

    public int getThumbID(){
        if(pictures.isEmpty()){
            return -1;
        }else{
            return pictures.get(rand.nextInt(pictures.size())).getID();
        }
    }
}

