package com.example.synchess;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


import org.eclipse.paho.client.mqttv3.*;

import at.synchess.utils.timers.*;


public class ChessClient{
    private String host;
    private static final int port = 14031;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private MqttClient mqttClient;
    private String myID;

    public ChessClient(String host)  throws IOException,MqttException {
        this.host = host;
        myID = "ChessBoard-Software-000000";
        connect(host);
    }

    public void connect(String host) throws IOException, MqttException{
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("Connected to " + host + ":" + port);

        mqttClient = new MqttClient("tcp://" + host + ":3110", myID,null);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        mqttClient.connect(options);


    }

    public void subscribeToGame(int gameid, MainActivity ma) throws MqttException{

        mqttClient.subscribe("" + gameid, (topic, mqttMessage) -> {
           ma.onMqttMessage(mqttMessage.toString());
        });

    }

    public void post(int gameid, String message) throws MqttException {
        MqttMessage mqttMessage = new MqttMessage(message.getBytes());
        mqttMessage.setQos(2);
        mqttClient.publish(gameid + "", mqttMessage);
    }

    public String requestString(String message) throws IOException {
        if (socket == null || socket.isClosed()) {
            System.out.println("Client is not connected. Please connect first.");
            return null;
        }
        out.println(message);
        out.flush();


        String response = in.readLine();
        return response;
    }

    public List<String> requestList(String message) throws IOException {
        if (socket == null || socket.isClosed()) {
            System.out.println("Client is not connected. Please connect first.");
            return null;
        }

        out.println(message);
        out.flush();


        String amount = in.readLine();
        List ret = new ArrayList();
        for (int i = 0; i < Integer.parseInt(amount); i++) {
            ret.add(in.readLine());
        }
        return ret;


    }

    public int joinGame(int gameID) throws IOException{
        String s = requestString("JOIN " + gameID);
        System.out.println(s);
        return Integer.parseInt(s);
    }

    public void postMove(int gameID, String move, int secsRemaining) throws MqttException {
        MqttMessage mm = new MqttMessage((move + " " + secsRemaining).getBytes());
        mm.setQos(2); // QoS level 2
        mqttClient.publish(gameID + "", mm);
    }

    public int createGame(String timerType, int minutes, int seconds) throws IOException {
        String s = requestString("START " + timerType.toUpperCase() + " " + minutes + " " + seconds);
        return Integer.parseInt(s);
    }

    public Timer getTimer(int gameID) throws IOException {
        Timer t;
        String[] data = requestString("GETTIMER " + gameID).split(" ");
        switch (data[0]){
            case "Fixed":
                return new FixedTimer(Integer.parseInt(data[1]));
            case "Fischer":
                return new FischerTimer(Integer.parseInt(data[1]),Integer.parseInt(data[2]));
            case "Bronstein":
                return new BronsteinTimer(Integer.parseInt(data[1]),Integer.parseInt(data[2]));
            case "Sanddial":
                return new SanddialTimer(Integer.parseInt(data[1]));

        }
        return null;
    }

    public List<String> getGameList(boolean onlyOpen) throws IOException {
        return requestList("GAMELIST");
    }

    public void close() throws IOException, MqttException {
        socket.close();
        mqttClient.close();
    }

}
