package com.example.synchess;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.squareup.picasso.Picasso;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import at.synchess.exceptions.ChessException;
import at.synchess.utils.ChessBoard;
import at.synchess.utils.ChessNotation;
import at.synchess.utils.Move;
import at.synchess.utils.Pieces;
import at.synchess.utils.timers.Timer;

public class MainActivity extends AppCompatActivity {

    private ImageView selectedPiece;
    private List<String> logs;
    ChessViewModel cvm;
    private ChessBoard chessUtils;
    private int gameId;
    private boolean oppsTurn = false;
    private GridLayout chessboard;


    private int startX = 0;
    private int startY = 0;
    private int endX = 0;
    private int endY = 0;

    private static int[] pieceImages = new int[]{R.drawable.white_pawn, R.drawable.black_pawn, R.drawable.white_knight, R.drawable.black_knight, R.drawable.white_rook, R.drawable.black_rook, R.drawable.white_bishop, R.drawable.black_bishop, R.drawable.white_queen, R.drawable.black_queen, R.drawable.white_king, R.drawable.black_king};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        chessboard = findViewById(R.id.chessboard);
        ViewTreeObserver observer = chessboard.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int width = chessboard.getWidth();
                chessboard.getLayoutParams().height = width;
                chessboard.requestLayout();
                chessboard.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }

        });
        setupDragAndDrop();
        loadPieces();

        cvm = ((ChessApplication) getApplication()).getSharedViewModel();

        gameId = cvm.getGameID();

        logs = new ArrayList<>();
        ListView lv = findViewById(R.id.logListView);
        ListAdapter la = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, logs);
        lv.setAdapter(la);

        connectToServer(gameId);

        chessUtils = new ChessBoard(false);

    }

    @SuppressLint("ClickableViewAccessibility")
    private void loadPiece( int row, int col, int drawable) {
        Log.d("loadPiece", "Loading piece at " + row + ", " + col);
        ImageView iv = new ImageView(this);
        Picasso.get().load(drawable).resize(120,120).into(iv);
        movePiece( iv, row, col);


        iv.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                selectedPiece = (ImageView) v;
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                v.startDragAndDrop(null, shadowBuilder, v, 0);
                return true;
            }
            return false;
        });


    }

    private void loadPieces(){
        loadPiece(7, 0, R.drawable.white_rook);
        loadPiece(7, 7, R.drawable.white_rook);
        loadPiece(7, 1, R.drawable.white_knight);
        loadPiece(7, 6, R.drawable.white_knight);
        loadPiece(7, 2, R.drawable.white_bishop);
        loadPiece(7, 5, R.drawable.white_bishop);
        loadPiece(7, 4, R.drawable.white_queen);
        loadPiece(7, 3, R.drawable.white_king);
        loadPiece(6, 0, R.drawable.white_pawn);
        loadPiece(6, 1, R.drawable.white_pawn);
        loadPiece(6, 2, R.drawable.white_pawn);
        loadPiece(6, 3, R.drawable.white_pawn);
        loadPiece(6, 4, R.drawable.white_pawn);
        loadPiece(6, 5, R.drawable.white_pawn);
        loadPiece(6, 6, R.drawable.white_pawn);
        loadPiece(6, 7, R.drawable.white_pawn);

        loadPiece(0, 0, R.drawable.black_rook);
        loadPiece(0, 7, R.drawable.black_rook);
        loadPiece(0, 1, R.drawable.black_knight);
        loadPiece(0, 6, R.drawable.black_knight);
        loadPiece(0, 2, R.drawable.black_bishop);
        loadPiece(0, 5, R.drawable.black_bishop);
        loadPiece(0, 4, R.drawable.black_queen);
        loadPiece(0, 3, R.drawable.black_king);
        loadPiece(1, 0, R.drawable.black_pawn);
        loadPiece(1, 1, R.drawable.black_pawn);
        loadPiece(1, 2, R.drawable.black_pawn);
        loadPiece(1, 3, R.drawable.black_pawn);
        loadPiece(1, 4, R.drawable.black_pawn);
        loadPiece(1, 5, R.drawable.black_pawn);
        loadPiece(1, 6, R.drawable.black_pawn);
        loadPiece(1, 7, R.drawable.black_pawn);
    }

    private void setupDragAndDrop() {
        chessboard.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                if (oppsTurn) return false;

                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        float x = event.getX();
                        float y = event.getY();

                        startX = (int) (event.getY() / (chessboard.getHeight() / 8.0));
                        startY = (int) (event.getX() / (chessboard.getWidth() / 8.0));
                        return true;
                    case DragEvent.ACTION_DROP:

                        endX = (int) (event.getY() / (chessboard.getHeight() / 8.0));
                        endY = (int) (event.getX() / (chessboard.getWidth() / 8.0));
                        chessUtils.setTempTile(startX, startY, Pieces.NONE);
                        chessUtils.setTempTile(endX, endY, chessUtils.board[startX][startY]);

                        try {
                            if (chessUtils.checkMove(chessUtils.detectMove())) {
                                movePiece(selectedPiece, endX, endY);
                                chessUtils.pushBoard(false);
                                oppsTurn = true;

                            } else {

                                movePiece(selectedPiece, startX, startY);
                                chessUtils.pushBoard(true);
                                privLog("Illegal move");
                            }
                        } catch (ChessException e) {
                            privLog(e.toString());
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void movePiece(ImageView piece, int targetRow, int targetCol) {
        RelativeLayout view = (RelativeLayout) chessboard.getChildAt(targetRow * chessboard.getColumnCount() + targetCol);
        RelativeLayout previousView = (RelativeLayout) piece.getParent();
        //Log.d("movePiece", "Moving piece from" + previousView + " to " + view );

        if(previousView != null)
            previousView.removeView(piece);

        if (view.getChildAt(0) != null)
            view.removeView(view.getChildAt(0));

        view.addView(piece);

    }
    private void movePiece(int startRow, int startCol, int targetRow, int targetCol) {
        RelativeLayout target = (RelativeLayout) chessboard.getChildAt(targetRow * chessboard.getColumnCount() + targetCol);
        ImageView piece = (ImageView) ((RelativeLayout) chessboard.getChildAt(startRow * chessboard.getColumnCount() + startCol)).getChildAt(0);
        ((RelativeLayout) piece.getParent()).removeView(piece);
        target.addView(piece);
    }

    private void displayMove(Move m){
        ImageView piece = (ImageView) ((RelativeLayout) chessboard.getChildAt(m.getStartX() * chessboard.getColumnCount() + m.getStartY())).getChildAt(0);
        ImageView takenPiece = (ImageView) ((RelativeLayout) chessboard.getChildAt(m.getTargX() * chessboard.getColumnCount() + m.getTargY())).getChildAt(0);

        switch (m.getMoveType()){
            case STANDARD:
                if (takenPiece != null)
                    ((RelativeLayout) takenPiece.getParent()).removeView(takenPiece);
                movePiece(piece,m.getTargX(), m.getTargY());
            break;
            case ENPASSANTE:
                if (takenPiece != null)
                    ((RelativeLayout) takenPiece.getParent()).removeView(takenPiece);
                ((RelativeLayout) piece.getParent()).removeView(takenPiece);
                loadPiece( m.getTargX(), m.getTargY(), pieceImages[m.getPiece()]);
            break;
            case CASTLE:
                switch (m.getCastleType()) {
                    case 0:
                        movePiece(4, 0, 6, 0);
                        movePiece(7, 0, 5, 0);
                        break;
                    case 1:
                        movePiece(0, 0, 2, 0);
                        movePiece(3, 0, 1, 0);
                        break;
                    case 2:
                        movePiece(0, 7, 2, 7);
                        movePiece(3, 7, 1, 7);
                        break;
                    case 3:
                        movePiece(3, 7, 6, 7);
                        movePiece(7, 7, 5, 7);
                        break;
                }
                break;
        }

    }



    private void privLog(String s){
        Log.d("privLog", s + "");
        logs.add(s);
    }

    private void pubLog(String s) throws MqttException {
    cvm.getClient().post(gameId, "M:" + s);
    }

    private void connectToServer(int gameId) {

        Thread thread = new Thread(() -> {
            try {
                Log.d("connectToServer", cvm.getClient() + " " + this);
                cvm.getClient().subscribeToGame(3, this);

            } catch (MqttException e) {
                throw new RuntimeException(e);
            }
        });

        thread.start();
    }



    public void onMqttMessage(String s){

            if(!s.startsWith("M:"))
            {
                Log.d("onMqttMessage", s);
                String[] data = s.split(" ");
                Move m = ChessNotation.parseAnnotation(data[0]);
                Log.d("onMqttMessage", m.toString());
                chessUtils.applyMove(m);
                oppsTurn = false;
                runOnUiThread(() -> {
                    // Update UI here
                    displayMove(m);
                });

            }
            else {
                logs.add(s.substring(2));
            }

    }
}