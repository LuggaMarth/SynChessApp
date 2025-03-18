package com.example.synchess;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;

public class ChessViewModel extends ViewModel {
    private ChessClient client;
    private int gameID;

    public ChessViewModel() {


            Thread thread = new Thread(() -> {
                    try {
                        client = new ChessClient("10.0.2.2");
                        Log.d("ChessViewModel", "Client created");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (MqttException e) {
                        throw new RuntimeException(e);
                    }

    });
    thread.start();
    }

    public void setGameID(int i){
        gameID = i;
    }
    public int getGameID(){
        return gameID;
    }
    public ChessClient getClient(){
        return client;
    }
}
