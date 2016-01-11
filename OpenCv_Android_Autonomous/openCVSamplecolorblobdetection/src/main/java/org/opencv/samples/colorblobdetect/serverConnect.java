package org.opencv.samples.colorblobdetect;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.Menu;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class serverConnect extends Activity {

     private Socket client;
    private PrintWriter printwriter;
     private EditText ip, port, message;
     private String Message;
    private CheckBox serverConnection;
    private int connection=0;
    private  Button setting;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.serverconnect);

        serverConnection = (CheckBox) findViewById(R.id.check);
        ip = (EditText) findViewById(R.id.textIp); // reference to the text field
        port = (EditText) findViewById(R.id.textPort); // reference to the text field
        setting=(Button) findViewById(R.id.setting);
        ip.setText("192.168.43.68");
        port.setText("23");

        setting.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(serverConnect.this, ColorBlobDetectionActivity.class);
                intent.putExtra("ipName", "setting");
                intent.putExtra("portName", "");
                startActivity(intent);
            }
        });
        serverConnection.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                // TODO Auto-generated method stub
                if (serverConnection.isChecked()) {
                    try {
                        Toast.makeText(getBaseContext(), "Baglanti saglaniyor.", Toast.LENGTH_LONG).show();
                        SendMessage sendMessageTask = new SendMessage();
                        sendMessageTask.execute();
                        connection = 1;
                    } catch (Exception e) {
                        Toast.makeText(getBaseContext(), "Hata olustu : " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

                if (connection == 1) {
                    Toast.makeText(getBaseContext(), "Server ile baglanti kuruldu", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(serverConnect.this, ColorBlobDetectionActivity.class);
                    intent.putExtra("ipName", ip.getText().toString());
                    intent.putExtra("portName", port.getText().toString());
                    startActivity(intent);
                }
            }
        });
    }
     private class SendMessage extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                String text = port.getText().toString();
                client = new Socket(ip.getText().toString(), Integer.parseInt(text)); // connect to the server
                printwriter = new PrintWriter(client.getOutputStream(), true);
                printwriter.write("Android telefon ile baglanti kuruldu."); // write the message to output stream

                printwriter.flush();
                printwriter.close();
                client.close(); // closing the connection

            } catch (UnknownHostException e) {
                e.printStackTrace();
                Toast.makeText(getBaseContext(), "Hata olustu : " + e.getMessage(), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getBaseContext(), "Hata olustu : " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            return null;
        }
    }
 }