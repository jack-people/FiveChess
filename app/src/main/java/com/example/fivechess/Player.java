package com.example.fivechess;

public class Player {
    //Player's Name
    String mName;
    int type;
    int mWin;
    int mLose;

    public Player(String name, int type){
        this.mName = name;
        this.type = type;
    }
    
    public Player(int type){
        if (type == Game.WHITE){
            this.mName = "White";
        } else if (type == Game.BLACK){
            this.mName = "Black";
        }
        this.type = type;
    }
    
    public int getType(){
        return this.type;
    }
    /**
     * win a game
     */
    public void win(){
        mWin += 1;
    }
    public String getWin(){
        return String.valueOf(mWin);
    }
    /**
     * Negative one game
     */
    public void lose(){
        mLose += 1;
    }
}
