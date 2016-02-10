package net.project104.civyshkbirds;

import java.io.Serializable;

class Cheep implements Serializable{
    final int ID;
    final String latinName, author, contact;
    public Cheep(int id, String latinName, String author, String contact){
        this.ID = id;
        this.latinName = latinName;
        this.author = author;
        this.contact = contact;
    }
}

