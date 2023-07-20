package com.example.fivechess;

import java.util.Deque;
import java.util.LinkedList;
import android.os.Handler;
import android.os.Message;

public class GameLogic {
    public static class Game {
        public static final int SCALE_MEDIUM = 15;
        Player me;
        Player challenger;

        private int mMode = 0;

        // 默认黑子先出
        private int mActive = 1;
        int mGameWidth = 0;
        int mGameHeight = 0;
        int[][] mGameMap = null;
        Deque<Coordinate> mActions ;

        public static final int BLACK = 1;
        public static final int WHITE = 2;
        private Handler mNotify;

        public Game(Handler h, Player me, Player challenger){
            this(h, me, challenger, SCALE_MEDIUM, SCALE_MEDIUM);
        }

        public Game(Handler h, Player me, Player challenger, int width, int height){
            mNotify = h;
            this.me = me;
            this.challenger = challenger;
            mGameWidth = width;
            mGameHeight = height;
            mGameMap = new int[mGameWidth][mGameHeight];
            mActions = new LinkedList<Coordinate>();
        }

        /**
         * retract a false move in a chess game
         */
        public boolean rollback(){
            Coordinate c = mActions.pollLast();
            if (c != null){
                mGameMap[c.x][c.y] = 0;
                changeActive();
                return true;
            }
            return false;
        }

        public int getWidth(){
            return mGameWidth;
        }
        public int getHeight(){
            return mGameHeight;
        }

        /**
         * do
         */
        public boolean addChess(int x, int y){
            if (mMode == GameConstants.MODE_FIGHT){
                if(mGameMap[x][y] == 0){
                    int type ;
                    if (mActive == BLACK){
                        mGameMap[x][y] = BLACK;
                        type = com.example.fivechess.Game.BLACK;
                    } else {
                        mGameMap[x][y] = WHITE;
                        type = com.example.fivechess.Game.WHITE;
                    }
                    if(!isGameEnd(x, y, type)){
                        changeActive();
                        sendAddChess(x, y);
                        mActions.add(new Coordinate(x, y));
                    }
                    return true;
                }

            } else if(mMode == GameConstants.MODE_SINGLE){
                if(mActive == me.type && mGameMap[x][y] == 0){
                    mGameMap[x][y] = me.type;
                    mActive = challenger.type;
                    if(!isGameEnd(x, y, me.type)){
                        sendAddChess(x, y);
                        mActions.add(new Coordinate(x, y));
                    }
                    return true;
                }
            }
            return false;
        }

        public void addChess(int x, int y, Player player){
            if(mGameMap[x][y] == 0){
                mGameMap[x][y] = player.type;
                mActions.add(new Coordinate(x, y));
                boolean isEnd = isGameEnd(x, y, player.type);
                mActive = me.type;
                if(!isEnd){
                    mNotify.sendEmptyMessage(GameConstants.ACTIVE_CHANGE);
                }
            }
        }

        public void addChess(Coordinate c, Player player){
            addChess(c.x, c.y, player);
        }

        public static int getFighter(int type){
            if (type == BLACK){
                return WHITE;
            } else {
                return BLACK;
            }
        }

        /**
         * Return to the current placement party
         */
        public int getActive(){
            return mActive;
        }

        /**
         *Obtain the chessboard
         */
        public int[][] getChessMap(){
            return mGameMap;
        }

        /**
         * Obtain Chessboard History
         * @return mActions
         */
        public Deque<Coordinate> getActions(){
            return mActions;
        }

        /**
         * Reset Game
         */
        public void reset(){
            mGameMap = new int[mGameWidth][mGameHeight];
            mActive = BLACK;
            mActions.clear();
        }

        /**
         * No need to update the placement party, whoever loses will take the lead
         */
        public void resetNet(){
            mGameMap = new int[mGameWidth][mGameHeight];
            mActions.clear();
        }

        private void changeActive(){
            if(mActive == BLACK){
                mActive = WHITE;
            } else {
                mActive = BLACK;
            }
        }

        private void sendAddChess(int x, int y){
            Message msg = new Message();
            msg.what = GameConstants.ADD_CHESS;
            msg.arg1 = x;
            msg.arg2 = y;
            mNotify.sendMessage(msg);
        }

        // Determine if the five sons are in a row
        private boolean isGameEnd(int x, int y, int type){
            int leftX = x-4 > 0? x-4 : 0;
            int rightX = x+4 < mGameWidth-1 ? x+4: mGameWidth-1;
            int topY = y-4 > 0? y-4 : 0;
            int bottomY = y + 4< mGameHeight-1 ? y+4: mGameHeight-1;

            int horizontal = 1;
            // Horizontal Left
            for (int i = x - 1; i >= leftX ; --i){
                if (mGameMap[i][y] != type){
                    break;
                }
                ++horizontal;
            }
            // Horizontal right
            for (int i = x + 1; i <= rightX ; ++i){
                if (mGameMap[i][y] != type){
                    break;
                }
                ++horizontal;
            }
            if (horizontal>=5) {
                sendGameResult(type);
                return true;
            }

            int vertical = 1;
            // Vertical Up
            for (int j = y - 1; j >= topY ; --j){
                if (mGameMap[x][j] != type){
                    break;
                }
                ++vertical;
            }
            // Vertical Down
            for (int j = y + 1; j <= bottomY ; ++j){
                if (mGameMap[x][j] != type){
                    break;
                }
                ++vertical;
            }
            if (vertical >= 5) {
                sendGameResult(type);
                return true;
            }

            int leftOblique = 1;
            // Left diagonal upward
            for (int i = x + 1,j = y - 1; i <= rightX && j >= topY ; ++i, --j){
                if (mGameMap[i][j] != type){
                    break;
                }
                ++leftOblique;
            }
            // Left diagonal downward
            for (int i = x - 1,j = y + 1; i >= leftX && j <= bottomY ; --i, ++j){
                if (mGameMap[i][j] != type){
                    break;
                }
                ++leftOblique;
            }
            if (leftOblique >= 5) {
                sendGameResult(type);
                return true;
            }

            int rightOblique = 1;
            // Right diagonal upward
            for (int i = x - 1,j = y - 1; i >= leftX && j >= topY ; --i, --j){
                if (mGameMap[i][j] != type){
                    break;
                }
                ++rightOblique;
            }
            // Right diagonal downward
            for (int i = x + 1,j = y + 1; i <= rightX && j <= bottomY ; ++i, ++j){
                if (mGameMap[i][j] != type){
                    break;
                }
                ++rightOblique;
            }
            if (rightOblique >= 5) {
                sendGameResult(type);
                return true;
            }
            return false;
        }
        private void sendGameResult(int player){
            Message msg = Message.obtain();
            msg.what = GameConstants.GAME_OVER;
            msg.arg1 = player;
            mNotify.sendMessage(msg);
        }
    }

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
            if (type == com.example.fivechess.Game.WHITE){
                this.mName = "White";
            } else if (type == com.example.fivechess.Game.BLACK){
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

    public class ComputerAI {

        public static final int HOR = 1;
        public static final int VER = 2;
        public static final int HOR_VER = 3;
        public static final int VER_HOR = 4;

        private int mWidth = 0;
        private int mHeight = 0;

        // Black chess priority value array
        int[][][] black = null;
        // white chess priority value array
        int[][][] white = null;

        int[][] plaValue = {{2,6,173,212,250,250,250},{0,5,7,200,230,231,231},
                {0,0,0,0,230,230,230,0}};
        int[][] cpuValue = {{0,3,166,186,229,229,229},{0,0,5,167,220,220,220},
                {0,0,0,0,220,220,220,0}};

        public ComputerAI(int width, int height) {
            mWidth = width;
            mHeight = height;
            black = new int[width][height][5];
            white = new int[width][height][5];
        }

        /**
         * Update chessboard weights
         */
        public void updateValue(com.example.fivechess.Game game){
            int[][] map = game.getChessMap();

        }
        public void updateValue(int[][] map)
        {

            int[] computerValue = {0,0,0,0};
            int[] playerValue = {0,0,0,0};
            for(int i = 0; i < mWidth; i ++)
            {
                for(int j = 0; j < mHeight; j ++)
                {
                    if(map[i][j] == 0)
                    {
                        int counter = 0;
                        //Give different weights to different situations
                        //Vertical
                        for(int k = j + 1; k < mHeight; k ++)
                        {
                            if(map[i][k] == com.example.fivechess.Game.BLACK)
                                computerValue[0] ++;
                            if(map[i][k] == 0)
                                break;
                            if(map[i][k] == com.example.fivechess.Game.WHITE)
                            {
                                counter ++;
                                break;
                            }
                            if(k == mHeight-1)
                                counter ++;
                        }

                        for(int k = j - 1; k >= 0; k --)
                        {
                            if(map[i][k] == com.example.fivechess.Game.BLACK)
                                computerValue[0] ++;
                            if(map[i][k] == 0)
                                break;
                            if(map[i][k] == com.example.fivechess.Game.WHITE)
                            {
                                counter ++;
                                break;
                            }
                            if(k == 0)
                                counter ++;
                        }
                        if(j == 0 || j == mHeight-1)
                            counter ++;
                        white[i][j][0] = cpuValue[counter][computerValue[0]];
                        computerValue[0] = 0;
                        counter = 0;
                        // backslash
                        for(int k = i + 1, l = j + 1; l < mHeight; k ++, l ++)
                        {
                            if(k >= mHeight)
                            {
                                break;
                            }
                            if(map[k][l] == com.example.fivechess.Game.BLACK)
                                computerValue[1] ++;
                            if(map[k][l] == 0)
                                break;
                            if(map[k][l] == com.example.fivechess.Game.WHITE)
                            {
                                counter ++;
                                break;
                            }
                            if(k == mWidth-1 || l == mHeight-1)
                                counter ++;

                        }

                        for(int k = i - 1, l = j - 1; l >= 0; k --, l --)
                        {
                            if(k < 0)
                            {
                                break;
                            }
                            if(map[k][l] == com.example.fivechess.Game.BLACK)
                                computerValue[1] ++;
                            if(map[k][l] == 0)
                                break;
                            if(map[k][l] == com.example.fivechess.Game.WHITE)
                            {
                                counter ++;
                                break;
                            }
                            if(k == 0 || l == 0)
                                counter ++;
                        }
                        if(i == 0 || i == mWidth-1 || j == 0 || j == mHeight-1)
                            counter ++;
                        white[i][j][1] = cpuValue[counter][computerValue[1]];
                        computerValue[1] = 0;
                        counter = 0;

                        // transverse
                        for(int k = i + 1; k < mWidth; k ++)
                        {
                            if(map[k][j] == com.example.fivechess.Game.BLACK)
                                computerValue[2] ++;
                            if(map[k][j] == 0)
                                break;
                            if(map[k][j] == com.example.fivechess.Game.WHITE)
                            {
                                counter ++;
                                break;
                            }
                            if(k == mWidth-1)
                                counter ++;
                        }

                        for(int k = i - 1; k >= 0; k --)
                        {
                            if(map[k][j] == com.example.fivechess.Game.BLACK)
                                computerValue[2] ++;
                            if(map[k][j] == 0)
                                break;
                            if(map[k][j] == com.example.fivechess.Game.WHITE)
                            {
                                counter ++;
                                break;
                            }
                            if(k == 0)
                                counter ++;
                        }
                        if(i == 0 || i == mWidth-1)
                            counter ++;
                        white[i][j][2] = cpuValue[counter][computerValue[2]];
                        computerValue[2] = 0;
                        counter = 0;

                        // forward slash
                        for(int k = i - 1, l = j + 1; l < mWidth; k --, l ++)
                        {
                            if(k < 0)
                            {
                                break;
                            }
                            if(map[k][l] == com.example.fivechess.Game.BLACK)
                                computerValue[3] ++;
                            if(map[k][l] == 0)
                                break;
                            if(map[k][l] == com.example.fivechess.Game.WHITE)
                            {
                                counter ++;
                                break;
                            }
                            if(k ==0 || l == mHeight-1)
                                counter ++;
                        }

                        for(int k = i + 1, l = j - 1; l >= 0; k ++, l --)
                        {
                            if(k >= mWidth)
                            {
                                break;
                            }
                            if(map[k][l] == com.example.fivechess.Game.BLACK)
                                computerValue[3] ++;
                            if(map[k][l] == 0)
                                break;
                            if(map[k][l] == com.example.fivechess.Game.WHITE)
                            {
                                counter ++;
                                break;
                            }
                            if(k == mWidth-1 || l == 0)
                                counter ++;
                        }

                        if(i == 0 || i == mWidth-1 || j == 0 || j == mHeight-1)
                            counter ++;
                        white[i][j][3] = cpuValue[counter][computerValue[3]];
                        computerValue[3] = 0;
                        counter = 0;
// Simultaneously determine the weights in both directions and give them an appropriate weight
                        for(int k = 0; k < 4; k ++)
                        {
                            if(white[i][j][k] == 173)
                                counter ++;
                        }
                        if(counter >= 2 && white[i][j][4] < 175)
                            white[i][j][4] = 175;
                        counter = 0;

                        for(int k = 0; k < 4; k ++)
                        {
                            for(int l = 0; l < 4; l ++)
                            {
                                if(white[i][j][k] == 173 && white[i][j][l] == 200
                                        && white[i][j][4] < 201)
                                    white[i][j][4] = 201;
                            }
                        }

                        if(j >= 1)
                        {
                            if(map[i][j-1] == 0)
                            {
                                if(white[i][j-1][0] >= 173)
                                {
                                    for(int k = 0; k < 4; k ++)
                                    {
                                        if(white[i][j][k] >= 173)
                                        {
                                            if(white[i][j][4] < 201)
                                            {
                                                white[i][j][4] = 201;
                                            }
                                        }
                                    }
                                }
// If the weights in both directions are active three, reduce the weights
                                if(white[i][j-1][0] == 173)
                                {
                                    for(int k = 0; k < 4; k ++)
                                    {
                                        if(white[i][j][k] == 173)
                                        {
                                            if(white[i][j][4] == 201)
                                            {
                                                white[i][j][4] = 175;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if(j >= 1 && i >= 1)
                        {
                            if(map[i-1][j-1] == 0)
                            {
                                if(white[i-1][j-1][1] >= 173)
                                {
                                    for(int k = 0; k < 4; k ++)
                                    {
                                        if(white[i][j][k] >= 173)
                                        {
                                            if(white[i][j][4] < 201)
                                            {
                                                white[i][j][4] = 201;
                                            }
                                        }
                                    }
                                }
                                if(white[i-1][j-1][1] == 173)
                                {
                                    for(int k = 0; k < 4; k ++)
                                    {
                                        if(white[i][j][k] == 173)
                                        {
                                            if(white[i][j][4] == 201)
                                            {
                                                white[i][j][4] = 175;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if(i >= 1)
                        {
                            if(map[i-1][j] == 0)
                            {
                                if(white[i-1][j][2] >= 173)
                                {
                                    for(int k = 0; k < 4; k ++)
                                    {
                                        if(white[i][j][k] >= 173)
                                        {
                                            if(white[i][j][4] < 201)
                                            {
                                                white[i][j][4] = 201;
                                            }
                                        }
                                    }
                                }
                                if(white[i-1][j][2] == 173)
                                {
                                    for(int k = 0; k < 4; k ++)
                                    {
                                        if(white[i][j][k] == 173)
                                        {
                                            if(white[i][j][4] == 201)
                                            {
                                                white[i][j][4] = 175;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if(i > 0 && j < mHeight-1)
                        {
                            if(map[i-1][j+1] == 0)
                            {
                                if(white[i-1][j+1][3] >= 173)
                                {
                                    for(int k = 0; k < 4; k ++)
                                    {
                                        if(white[i][j][k] >= 173)
                                        {
                                            if(white[i][j][4] < 201)
                                            {
                                                white[i][j][4] = 201;
                                            }
                                        }
                                    }
                                }
                                if(white[i-1][j+1][3] == 173)
                                {
                                    for(int k = 0; k < 4; k ++)
                                    {
                                        if(white[i][j][k] == 173)
                                        {
                                            if(white[i][j][4] == 201)
                                            {
                                                white[i][j][4] = 175;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if(j < mHeight-1)
                        {
                            if(map[i][j+1] == 0)
                            {
                                if(white[i][j+1][0] >= 173)
                                {
                                    for(int k = 0; k < 4; k ++)
                                    {
                                        if(white[i][j][k] >= 173)
                                        {
                                            if(white[i][j][4] < 201)
                                            {
                                                white[i][j][4] = 201;
                                            }
                                        }
                                    }
                                }
                                if(white[i][j+1][0] == 173)
                                {
                                    for(int k = 0; k < 4; k ++)
                                    {
                                        if(white[i][j][k] == 173)
                                        {
                                            if(white[i][j][4] == 201)
                                            {
                                                white[i][j][4] = 175;
                                            }
                                        }
                                    }
                                }

                            }
                        }

                        if(i < mWidth-1 && j < mHeight-1)
                        {
                            if(map[i+1][j+1] == 0)
                            {
                                if(white[i+1][j+1][1] >= 173)
                                {
                                    for(int k = 0; k < 4; k ++)
                                    {
                                        if(white[i][j][k] >= 173)
                                        {
                                            if(white[i][j][4] < 201)
                                            {
                                                white[i][j][4] = 201;
                                            }
                                        }
                                    }
                                }
                                if(white[i+1][j+1][1] == 173)
                                {
                                    for(int k = 0; k < 4; k ++)
                                    {
                                        if(white[i][j][k] == 173)
                                        {
                                            if(white[i][j][4] == 201)
                                            {
                                                white[i][j][4] = 175;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if(i < mWidth-1)
                        {
                            if(map[i+1][j] == 0)
                            {
                                if(white[i+1][j][2] >= 173)
                                {
                                    for(int k = 0; k < 4; k ++)
                                    {
                                        if(white[i][j][k] >= 173)
                                        {
                                            if(white[i][j][4] < 201)
                                            {
                                                white[i][j][4] = 201;
                                            }
                                        }
                                    }
                                }
                                if(white[i+1][j][2] == 173)
                                {
                                    for(int k = 0; k < 4; k ++)
                                    {
                                        if(white[i][j][k] == 173)
                                        {
                                            if(white[i][j][4] == 201)
                                            {
                                                white[i][j][4] = 175;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if(i < mWidth-1 && j > 0)
                        {
                            if(map[i+1][j-1] == 0)
                            {
                                if(white[i+1][j-1][3] >= 173)
                                {
                                    for(int k = 0; k < 4; k ++)
                                    {
                                        if(white[i][j][k] >= 173)
                                        {
                                            if(white[i][j][4] < 201)
                                            {
                                                white[i][j][4] = 201;
                                            }
                                        }
                                    }
                                }
                                if(white[i+1][j-1][3] == 173)
                                {
                                    for(int k = 0; k < 4; k ++)
                                    {
                                        if(white[i][j][k] == 173)
                                        {
                                            if(white[i][j][4] == 201)
                                            {
                                                white[i][j][4] = 175;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            for(int i = 0; i < mWidth; i ++)
            {
                for(int j = 0; j < mHeight; j ++)
                {
                    if(map[i][j] == 0)
                    {
                        int counter = 0;
                        for(int k = j + 1; k < mHeight; k ++)
                        {
                            if(map[i][k] == com.example.fivechess.Game.WHITE)
                                playerValue[0] ++;
                            if(map[i][k] == 0)
                                break;
                            if(map[i][k] == com.example.fivechess.Game.BLACK)
                            {
                                counter ++;
                                break;
                            }
                            if(k == mHeight-1)
                                counter ++;
                        }

                        for(int k = j - 1; k >= 0; k --)
                        {
                            if(map[i][k] == com.example.fivechess.Game.WHITE)
                                playerValue[0] ++;
                            if(map[i][k] == 0)
                                break;
                            if(map[i][k] == com.example.fivechess.Game.BLACK)
                            {
                                counter ++;
                                break;
                            }
                            if(k == 0)
                                counter ++;
                        }
                        if(j == 0 || j == mHeight-1)
                            counter ++;
                        black[i][j][0] = plaValue[counter][playerValue[0]];
                        playerValue[0] = 0;
                        counter = 0;

                        for(int k = i + 1, l = j + 1; l < mHeight; k ++, l ++)
                        {
                            if(k >= mWidth)
                            {
                                break;
                            }
                            if(map[k][l] == com.example.fivechess.Game.WHITE)
                                playerValue[1] ++;
                            if(map[k][l] == 0)
                                break;
                            if(map[k][l] == com.example.fivechess.Game.BLACK)
                            {
                                counter ++;
                                break;
                            }
                            if(k == mWidth-1 || l == mHeight-1)
                                counter ++;

                        }

                        for(int k = i - 1, l = j - 1; l >= 0; k --, l --)
                        {
                            if(k < 0)
                            {
                                break;
                            }
                            if(map[k][l] == com.example.fivechess.Game.WHITE)
                                playerValue[1] ++;
                            if(map[k][l] == 0)
                                break;
                            if(map[k][l] == com.example.fivechess.Game.BLACK)
                            {
                                counter ++;
                                break;
                            }
                            if(k == 0 || l == 0)
                                counter ++;
                        }
                        if(i == 0 || i == mWidth-1 || j == 0 || j == mHeight-1)
                            counter ++;
                        black[i][j][1] = plaValue[counter][playerValue[1]];
                        playerValue[1] = 0;
                        counter = 0;

                        for(int k = i + 1; k < mWidth; k ++)
                        {
                            if(map[k][j] == com.example.fivechess.Game.WHITE)
                                playerValue[2] ++;
                            if(map[k][j] == 0)
                                break;
                            if(map[k][j] == com.example.fivechess.Game.BLACK)
                            {
                                counter ++;
                                break;
                            }
                            if(k == mWidth-1)
                                counter ++;
                        }

                        for(int k = i - 1; k >= 0; k --)
                        {
                            if(map[k][j] == com.example.fivechess.Game.WHITE)
                                playerValue[2] ++;
                            if(map[k][j] == 0)
                                break;
                            if(map[k][j] == com.example.fivechess.Game.BLACK)
                            {
                                counter ++;
                                break;
                            }
                            if(k == 0)
                                counter ++;
                        }
                        if(i == 0 || i == mWidth-1)
                            counter ++;
                        black[i][j][2] = plaValue[counter][playerValue[2]];
                        playerValue[2] = 0;
                        counter = 0;

                        for(int k = i - 1, l = j + 1; l < mHeight; k --, l ++)
                        {
                            if(k < 0)
                            {
                                break;
                            }
                            if(map[k][l] == com.example.fivechess.Game.WHITE)
                                playerValue[3] ++;
                            if(map[k][l] == 0)
                                break;
                            if(map[k][l] == com.example.fivechess.Game.BLACK)
                            {
                                counter ++;
                                break;
                            }
                            if(k == 0 || l == mHeight-1)
                                counter ++;
                        }

                        for(int k = i + 1, l = j - 1; l >= 0; k ++, l --)
                        {

                            if(k >= mWidth)
                            {
                                break;
                            }
                            if(map[k][l] == com.example.fivechess.Game.WHITE)
                                playerValue[3] ++;
                            if(map[k][l] == 0)
                                break;
                            if(map[k][l] == com.example.fivechess.Game.BLACK)
                            {
                                counter ++;
                                break;
                            }
                            if(k == mWidth-1 || l ==0)
                                counter ++;
                        }

                        if(i == 0 || i == mWidth-1 || j == 0 || j == mHeight-1)
                            counter ++;
                        black[i][j][3] = plaValue[counter][playerValue[3]];
                        playerValue[3] = 0;
                        counter = 0;

                        for(int k = 0; k < 4; k ++)
                        {
                            if(black[i][j][k] == 166)
                                counter ++;
                        }
                        if(counter >= 2 && black[i][j][0] < 174)
                        {
                            black[i][j][0] = 174;
                        }
                        counter = 0;

                        for(int k = 0; k < 4; k ++)
                        {
                            for(int l = 0; l < 4; l ++)
                            {
                                if(black[i][j][k] == 166 && black[i][j][l] == 167
                                        && black[i][j][0] < 176)
                                    black[i][j][0] = 176;
                            }
                        }

                        for(int k = 0; k < 4; k ++)
                        {
                            for(int l = 0; l < 4; l ++)
                            {
                                if(black[i][j][k] == 166 && black[i][j][l] == 186
                                        && black[i][j][0] < 177)
                                    black[i][j][0] = 177;
                            }
                        }

                        for(int k = 0; k < 4; k ++)
                        {
                            if(black[i][j][k] == 167)
                                counter ++;
                        }
                        if(counter >= 2 && black[i][j][0] < 178)
                            black[i][j][0] = 178;
                        counter = 0;

                        for(int k = 0; k < 4; k ++)
                        {
                            for(int l = 0; l < 4; l ++)
                            {
                                if(black[i][j][k] == 167 && black[i][j][l] == 186
                                        && black[i][j][0] < 179)
                                    black[i][j][0] = 179;
                            }
                        }

                        for(int k = 0; k < 4; k ++)
                        {
                            if(black[i][j][k] == 186)
                                counter ++;
                        }
                        if(counter >= 2 && black[i][j][0] < 180)
                            black[i][j][0] = 180;
                        counter = 0;

                        if(j >= 1)
                        {
                            if(map[i][j-1] == 0)
                            {
                                if(black[i][j-1][0] >= 166)
                                {
                                    for(int k = 0; k < 4; k ++)
                                    {
                                        if(black[i][j][k] >= 166 && black[i][j][k] < 176)
                                        {
                                            if(black[i][j][0] < 176)
                                            {
                                                black[i][j][0] = 176;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if(j >= 1 && i >= 1)
                        {
                            if(map[i-1][j-1] == 0)
                            {
                                if(black[i-1][j-1][1] >= 166)
                                {
                                    for(int k = 0; k < 4; k ++)
                                    {
                                        if(black[i][j][k] >= 166)
                                        {
                                            if(black[i][j][0] < 176)
                                            {
                                                black[i][j][0] = 176;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if(i >= 1)
                        {
                            if(map[i-1][j] == 0)
                            {
                                if(black[i-1][j][2] >= 166)
                                {
                                    for(int k = 0; k < 4; k ++)
                                    {
                                        if(black[i][j][k] >= 166)
                                        {
                                            if(black[i][j][0] < 176)
                                            {
                                                black[i][j][0] = 176;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if(i > 0 && j < mHeight-1)
                        {
                            if(map[i-1][j+1] == 0)
                            {
                                if(black[i-1][j+1][3] >= 166)
                                {
                                    for(int k = 0; k < 4; k ++)
                                    {
                                        if(black[i][j][k] >= 166)
                                        {
                                            if(black[i][j][0] < 176)
                                            {
                                                black[i][j][0] = 176;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if(j < mHeight-1)
                        {
                            if(map[i][j+1] == 0)
                            {
                                if(black[i][j+1][0] >= 166)
                                {
                                    for(int k = 0; k < 4; k ++)
                                    {
                                        if(black[i][j][k] >= 166)
                                        {
                                            if(black[i][j][0] < 176)
                                            {
                                                black[i][j][0] = 176;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if(i < mWidth-1 && j < mHeight-1)
                        {
                            if(map[i+1][j+1] == 0)
                            {
                                if(black[i+1][j+1][1] >= 166)
                                {
                                    for(int k = 0; k < 4; k ++)
                                    {
                                        if(black[i][j][k] >= 166)
                                        {
                                            if(black[i][j][0] < 176)
                                            {
                                                black[i][j][0] = 176;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if(i < mWidth-1)
                        {
                            if(map[i+1][j] == 0)
                            {
                                if(black[i+1][j][2] >= 166)
                                {
                                    for(int k = 0; k < 4; k ++)
                                    {
                                        if(black[i][j][k] >= 166)
                                        {
                                            if(black[i][j][0] < 176)
                                            {
                                                black[i][j][0] = 176;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if(i < mWidth-1 && j > 0)
                        {
                            if(map[i+1][j-1] == 0)
                            {
                                if(black[i+1][j-1][3] >= 166)
                                {
                                    for(int k = 0; k < 4; k ++)
                                    {
                                        if(black[i][j][k] >= 166)
                                        {
                                            if(black[i][j][0] < 176)
                                            {
                                                black[i][j][0] = 176;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }

        public Coordinate getPosition(int[][] map)
        {
            int maxpSum = 0;
            int maxcSum = 0;
            int maxpValue = -10;
            int maxcValue = -10;
            int blackRow = 0;
            int blackCollum = 0;
            int whiteRow = 0;
            int whiteCollum = 0;
            for(int i = 0; i < mWidth; i ++)
            {
                for(int j = 0; j < mHeight; j ++)
                {
                    if(map[i][j] == 0)
                    {
                        for(int k = 0; k < 4; k ++)
                        {
                            if(black[i][j][k] > maxpValue)
                            {
                                blackRow = i;
                                blackCollum = j;
                                maxpValue = black[i][j][k];
                                maxpSum = black[i][j][0] + black[i][j][1]
                                        + black[i][j][2] + black[i][j][3] ;
                            }

                            // if the value if equal, check the sum of the value
                            if(black[i][j][k] == maxpValue)
                            {
                                if(maxpSum < (black[i][j][0] + black[i][j][1]
                                        + black[i][j][2] + black[i][j][3]))
                                {
                                    blackRow = i;
                                    blackCollum = j;
                                    maxpSum = black[i][j][0] + black[i][j][1]
                                            + black[i][j][2] + black[i][j][3];
                                }
                            }

                            if(white[i][j][k] > maxcValue)
                            {
                                whiteRow = i;
                                whiteCollum = j;
                                maxcValue = white[i][j][k];
                                maxcSum = black[i][j][0] + black[i][j][1]
                                        + black[i][j][2] + black[i][j][3];

                            }

                            if(white[i][j][k] == maxcValue)
                            {
                                if(maxcSum < (black[i][j][0] + black[i][j][1]
                                        + black[i][j][2] + black[i][j][3]))
                                {
                                    whiteRow = i;
                                    whiteCollum = j;
                                    maxcSum = black[i][j][0] + black[i][j][1]
                                            + black[i][j][2] + black[i][j][3];
                                }
                            }
                        }
                    }
                }
            }
            Coordinate c = new Coordinate();
            if(maxcValue > maxpValue){
                c.x = whiteRow;
                c.y = whiteCollum;
            }
            else
            {
                c.x = blackRow;
                c.y = blackCollum;
            }
            return c;
        }

    }

}
