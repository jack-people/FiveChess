package com.example.fivechess;

import static com.example.fivechess.R.*;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SecActivity extends AppCompatActivity {
    Button game, exit,ranking;

    long exittime;
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK){
            if((System.currentTimeMillis()-exittime)>2000){
                Toast.makeText(SecActivity.this,"再次返回程序退出！",
                        Toast.LENGTH_SHORT).show();
                exittime = System.currentTimeMillis();
            }else{
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_sec);
        game = findViewById(id.game);
        exit = findViewById(id.eit);
        ranking = findViewById(id.ranking);

        //Ranking List
        ranking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(SecActivity.this,
                        LeaderboardActivity.class);
                startActivity(intent);
            }
        });

        game.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(SecActivity.this,
                        FightActivity.class);
                startActivity(intent);
            }
        });

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder ab = new AlertDialog.Builder(SecActivity.this);
                ab.setTitle("提示");
                ab.setIcon(mipmap.wz);
                ab.setMessage("您是否确定退出？");
                ab.setPositiveButton("取消", null);
                ab.setNeutralButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SecActivity.this.finish();
                    }
                });
                ab.create();
                ab.show();
            }
        });
    }
}

