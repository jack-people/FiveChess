package com.example.fivechess;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    EditText username,password;   //Create an account, password
    Button login,quit;

    //Click the return button twice within 2 seconds to exit
    long exittime;  //Declare integer
    public boolean onKeyDown(int keyCode, KeyEvent event){
        //  Determine event triggering
        if (keyCode == KeyEvent.KEYCODE_BACK){
            if((System.currentTimeMillis()-exittime)>2000){
                Toast.makeText(MainActivity.this,"再次返回程序退出！",
                Toast.LENGTH_SHORT).show();
                exittime = System.currentTimeMillis(); // Set First Click Time
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
        setContentView(R.layout.activity_main);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        quit = findViewById(R.id.quit);
        //  object non empty judgment
        if (username == null || password == null) {
            return;
        }
        // Add listening events to the login button to achieve login
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (username.getText().toString().equals("admin")
                    &&password.getText().toString().equals("123456")){
                    Toast.makeText(MainActivity.this,"登陆成功!",
                    Toast.LENGTH_SHORT).show();
                    // Implement page redirection
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this,
                            SecActivity.class);
                    // Store the current username for displaying information on the tool interface
                    intent.putExtra("username",
                            username.getText().toString());
                    startActivity(intent);
                } else if (username.getText().toString().equals("")
                        ||password.getText().toString().equals("")){
                    Toast.makeText(MainActivity.this,"用户名/密码不能为空!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this,"登录失败，密码或用户名错误!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Add listener to the quit button to implement exit functionality -- with confirmation dialog quit.
        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1.Create a pop-up object to display on the current page
                AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
                //2. Edit bullet box style
                //2.1 Creating a Title
                ab.setTitle("提示");
                // 2.3 Set icon
                ab.setIcon(R.mipmap.wq);
                // 2.4 Set Content
                ab.setMessage("您是否确定退出？");
                // 2.5 set button
                ab.setPositiveButton("取消",null);
                ab.setNeutralButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Implement program exit and end the current
                        MainActivity.this.finish();
                    }
                });
                // 3.Create Popup
                ab.create();
                // 4.Show Popup
                ab.show();
            }
        });
    }
}


