package com.example.synchess;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;

public class EnterCodeActivity extends AppCompatActivity {

    private EditText numberInput;
    private Button submitButton;
    private TextView resultTextView;
    private ChessViewModel cvm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entercode);

        numberInput = findViewById(R.id.numberInput);
        submitButton = findViewById(R.id.submitButton);
        resultTextView = findViewById(R.id.resultTextView);

        cvm = ((ChessApplication) getApplication()).getSharedViewModel();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the number from the EditText
                String input = numberInput.getText().toString();
                if (!input.isEmpty()) {
                    int number = Integer.parseInt(input);
                    try {
                        joinGame(number);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    resultTextView.setText("Please enter a number.");
                }
            }
        });
    }

    private void joinGame(int id) throws IOException {
    Thread thread = new Thread(() -> {
        try {
            if ( cvm.getClient().joinGame(id) == 1)
                runOnUiThread(() -> enterGame(id));
            else
                runOnUiThread(() ->
                resultTextView.setText("Failed to join game " + id));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    });
    thread.start();
    }

    private void enterGame(int id) {
        Intent intent = new Intent(this, MainActivity.class);
        cvm.setGameID(id);

        startActivity(intent);
    }


}