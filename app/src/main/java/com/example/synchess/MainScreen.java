package com.example.synchess;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.Objects;

public class MainScreen extends AppCompatActivity {
    MenuScreen prev;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title);

        changeToMenu(MenuScreen.TITLE);
        Button b = findViewById(R.id.backButton);
        b.setOnClickListener(v -> {
            if (prev != null) changeToMenu(prev);
        });
        ChessViewModel cvm = ((ChessApplication) getApplication()).getSharedViewModel();


        }




    private void changeToMenu(MenuScreen menuScreen){
        switch (menuScreen){
            case TITLE:
                setButton(true, "Play", () -> changeToMenu(MenuScreen.PLAY));
                setButton(false, "", null);
                prev = null;
                break;
            case PLAY:
                setButton(true, "Host", this::openHostGame);
                setButton(false, "Join", () -> changeToMenu(MenuScreen.JOIN));
                prev = MenuScreen.TITLE;
                break;
            case JOIN:
                setButton(true, "Enter Code", this::openEnterCode);
                setButton(false, "", null);
                prev = MenuScreen.TITLE;
        }
    }


    private void openEnterCode() {
        Intent intent = new Intent(this, EnterCodeActivity.class);
        startActivity(intent);
    }

    private void openHostGame() {
        Intent intent = new Intent(this, HostGameActivity.class);
        startActivity(intent);
    }


    private void setButton(boolean top, String text, Runnable onClick){
        Button b = top ? findViewById(R.id.topButton) : findViewById(R.id.bottomButton);
        b.setText(text);
        b.setOnClickListener(v -> {
            if (onClick != null) onClick.run();
        });
        if (Objects.equals(text, ""))
            b.setVisibility(View.INVISIBLE);
        else b.setVisibility(View.VISIBLE);
    }

}

