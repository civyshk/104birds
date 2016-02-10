package net.project104.civyshkbirds;

import java.io.Serializable;

class Picture implements Serializable{
    private final int ID;
    final String family, latinName, author, contact, licence, wikilink;
    public Picture(int id, String family, String latinName, String author, String contact, String licence, String wikilink){
        this.ID = id;
        this.family = family;
        this.latinName = latinName;
        this.author = author;
        this.contact = contact;
        this.licence = licence;
        this.wikilink = wikilink;
    }
    public int getID(){ return ID; }
}

