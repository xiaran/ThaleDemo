package com.thale.summeress.thale.model;

/**
 * Created by summeress on 16/6/9.
 */
public class Path {
    int imageID;
    String details;

    public Path(int id, String details){
        this.imageID = id;
        this.details = details;
    }

    public int getImageID(){
        return this.imageID;
    }

    public String getDetails(){
        return this.details;
    }
}
