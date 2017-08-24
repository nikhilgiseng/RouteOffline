package com.leptonsoftware.route;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
/**
 * Created by Hp on 17-Aug-17.
 */

public class splash extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        Thread timerThread = new Thread(){
            public void run(){
                try{
                    sleep(3000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }finally{
                    Intent intentLogin = new Intent(splash.this,TabbedMain.class);
                    startActivity(intentLogin);
                }
            }
        };
        timerThread.start();
    }
}
