package com.example.fivechess;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import com.example.fivechess.R;

/**
 * Responsible for displaying the game
 */

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "GameView";
    private static final boolean DEBUG = true;
    SurfaceHolder mHolder = null;
    // Chess brush
    private Paint chessPaint = new Paint();;
    // Chessboard brush
    private Paint boardPaint = new Paint();
    private int boardColor = 0;
    private float boardWidth = 0.0f;
    private float anchorWidth = 0.0f;
    // Clear Screen Brush
    Paint clear = new Paint();
    public int[][] mChessArray = null;
    Bitmap mBlack = null;
    Bitmap mBlackNew = null;
    Bitmap mWhite = null;
    Bitmap mWhiteNew = null;
    int mChessboardWidth = 0;
    int mChessboardHeight = 0;
    int mChessSize = 0;
    Context mContext = null;
    private Game mGame;
    //The coordinate position where the current focus is located
    private Coordinate focus;
    private boolean isDrawFocus;
    private Bitmap bFocus;
    //Use default parameters when calling constructors
    public GameView(Context context) {
        this(context, null);
    }
    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        boardColor = Color.BLACK;
        boardWidth = getResources().getDimensionPixelSize(R.dimen.boardWidth);
        anchorWidth = getResources().getDimensionPixelSize(R.dimen.anchorWidth);
        //Instantiation of constructor, storing focus coordinates
        focus = new Coordinate();
        init();
    }

    private void init(){
        mHolder = this.getHolder();
        mHolder.addCallback(this);
        // Set Transparency
        mHolder.setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);
        chessPaint.setAntiAlias(true);
        boardPaint.setStrokeWidth(boardWidth);
        boardPaint.setColor(boardColor);
        clear.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        setFocusable(true);
    }

    public void setGame(Game game){
        mGame = game;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        if(mGame != null){
            if (width % mGame.getWidth() == 0){
                float scale = ((float)mGame.getHeight()) / mGame.getWidth();
                int height = (int) (width*scale);
                setMeasuredDimension(width, height);
            } else {
                width = width / mGame.getWidth() * mGame.getWidth();
                float scale = ((float)mGame.getHeight()) / mGame.getWidth();
                int height = (int) (width*scale);
                setMeasuredDimension(width, height);
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (DEBUG) Log.d(TAG, "left="+left+"  top="+top+" right="+right+" bottom="+bottom);
        if (mGame != null) {
            mChessboardWidth = mGame.getWidth();
            mChessboardHeight = mGame.getHeight();
            mChessSize = (right - left) / mChessboardWidth;
            Log.d(TAG, "mChessSize=" + mChessSize + " mChessboardWidth="
                    + mChessboardWidth + " mChessboardHeight"
                    + mChessboardHeight);
        }
    }

    /**
     * Draw game interface
     */
    public void drawGame(){
        Canvas canvas = mHolder.lockCanvas();
        if (mHolder == null || canvas == null) {
            Log.d(TAG, "mholde="+mHolder+"  canvas="+canvas);
            return;
        }
        // clear
        canvas.drawPaint(clear);
        drawChessBoard(canvas);
        drawChess(canvas);
        drawFocus(canvas);
        mHolder.unlockCanvasAndPost(canvas);
    }

    /**
     * Add a chess piece
     */
    public void addChess(int x, int y){
        if (mGame == null){
            Log.d(TAG, "game can not be null");
            return;
        }
        mGame.addChess(x, y);
        drawGame();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                focus.x = (int) (x/mChessSize);
                focus.y = (int) (y/mChessSize);
                isDrawFocus = true;
                drawGame();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                isDrawFocus = false;
                int newx = (int) (x / mChessSize);
                int newy = (int) (y / mChessSize);
                if (canAdd(newx, newy, focus)) {
                    addChess(focus.x, focus.y);
                } else {
                    drawGame();
                }
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * Determine whether to cancel
     */
    private boolean canAdd(float x, float y, Coordinate focus){
        return x < focus.x+3 && x > focus.x -3
                && y < focus.y + 3 && y > focus.y - 3;
    }

    /**
     * Create Chess Pieces
     */
    private Bitmap createChess(int width, int height, int type){
        int tileSize = width/15;
        Bitmap bitmap = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable d = null;
        if (type == 0){
            d = getResources().getDrawable(R.mipmap.black);
        } else if (type == 1) {
            d = getResources().getDrawable(R.mipmap.white);
        } else if (type == 2){
            d = getResources().getDrawable(R.mipmap.black_new);
        } else if (type == 3){
            d = getResources().getDrawable(R.mipmap.white_new);
        } else if (type == 4){
            d = getResources().getDrawable(R.mipmap.focus);
        } else {
            throw new IllegalArgumentException("Invalid type value: " + type);
        }
        d.setBounds(0, 0, tileSize, tileSize);
        d.draw(canvas);
        return bitmap;
    }
    private void drawChessBoard(){
        Canvas canvas = mHolder.lockCanvas();
        if (mHolder == null || canvas == null) {
            return;
        }
        drawChessBoard(canvas);
        mHolder.unlockCanvasAndPost(canvas);
    }
    private void drawChessBoard(Canvas canvas){
        int startX = mChessSize/2;
        int startY = mChessSize/2;
        int endX = startX + (mChessSize * (mChessboardWidth - 1));
        int endY = startY + (mChessSize * (mChessboardHeight- 1));
        // draw Vertical straight line
        for (int i = 0; i < mChessboardWidth; ++i){
            canvas.drawLine(startX+(i*mChessSize), startY, startX+(i*mChessSize), endY, boardPaint);
        }
        // draw horizontal line
        for (int i = 0; i < mChessboardHeight; ++i){
            canvas.drawLine(startX, startY+(i*mChessSize), endX, startY+(i*mChessSize), boardPaint);
        }
        // // Draw anchor point (center point)
        int circleX = startX+mChessSize*(mChessboardWidth/2);
        int circleY = startY+mChessSize*(mChessboardHeight/2);;
        canvas.drawCircle(circleX, circleY, anchorWidth, boardPaint);
    }

    // Draw chess pieces
    private void drawChess(Canvas canvas){
        int[][] chessMap = mGame.getChessMap();
        for (int x = 0; x < chessMap.length; ++x){
            for (int y = 0; y < chessMap[0].length; ++y){
                int type = chessMap[x][y];
                if (type == Game.BLACK){
                    canvas.drawBitmap(mBlack, x*mChessSize, y*mChessSize, chessPaint);
                } else if (type == Game.WHITE){
                    canvas.drawBitmap(mWhite, x*mChessSize, y*mChessSize, chessPaint);
                }
            }
        }
        // Draw the latest chess piece
        if (mGame.getActions() != null && mGame.getActions().size() > 0){
            Coordinate last = mGame.getActions().getLast();
            int lastType = chessMap[last.x][last.y];
            if (lastType == Game.BLACK){
                canvas.drawBitmap(mBlackNew, last.x*mChessSize, last.y*mChessSize, chessPaint);
            } else if (lastType == Game.WHITE){
                canvas.drawBitmap(mWhiteNew, last.x*mChessSize, last.y*mChessSize, chessPaint);
            }
        }
    }

    /**
     * 画当前框
     */
    private void drawFocus(Canvas canvas){
        if (isDrawFocus){
            canvas.drawBitmap(bFocus, focus.x*mChessSize, focus.y*mChessSize, chessPaint);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mBlack != null){
            mBlack.recycle();
        }
        if (mWhite != null){
            mWhite.recycle();
        }
        mWhite = createChess(width, height, 1);
        mBlack = createChess(width, height, 0);
        mBlackNew = createChess(width, height, 2);
        mWhiteNew = createChess(width, height, 3);
        bFocus = createChess(width, height, 4);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Initialize chessboard
        drawChessBoard();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {

    }
}

