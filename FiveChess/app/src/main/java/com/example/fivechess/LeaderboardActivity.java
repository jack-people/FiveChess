package com.example.fivechess;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;

public class LeaderboardActivity extends AppCompatActivity {
    private Button btn_back;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    long exittime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();
        btn_back = findViewById(R.id.btn_back);
        recordGameStartTime();
        recordGameEndTime();

        String duration = getGameDurationFromDatabase();
        TextView tvGameDuration = findViewById(R.id.tv_game_duration);
        tvGameDuration.setText(String.format("对局时间：%s", duration));
    }

    private String getGameDurationFromDatabase() {
        String duration = "";

        // 执行数据库查询
        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME, new String[]{DatabaseHelper.game_duration},
                null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            // 获取对局时间
            @SuppressLint("Range") long gameDurationInMilliseconds = cursor.getLong(
                    cursor.getColumnIndex(DatabaseHelper.game_duration));
            // 格式化对局时间
            duration = formatGameDuration(gameDurationInMilliseconds);
            cursor.close();
        }
        return duration;
    }

    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - exittime) > 2000) {
            Toast.makeText(this, "再次返回退出排行榜退出！", Toast.LENGTH_SHORT).show();
            exittime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date currentTime = new Date();
        return dateFormat.format(currentTime);
    }

    private void executeDatabaseOperation(ContentValues values) {
        db.insert(DatabaseHelper.TABLE_NAME, null, values);
    }

    private void recordGameStartTime() {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.start_time, getCurrentTime());
        executeDatabaseOperation(values);
    }

    private void recordGameEndTime() {
        ContentValues values = new ContentValues();
        LocalDateTime endTime = null;
        Instant instant = null;
        Date date = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            endTime = LocalDateTime.now();
            instant = endTime.atZone(ZoneId.systemDefault()).toInstant();
            date = Date.from(instant);
        }

        // 转换为字符串
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        assert date != null;
        String endTimeString = dateFormat.format(date);

        values.put(DatabaseHelper.end_time, endTimeString);
        executeDatabaseOperation(values);

        // 计算对局花费的时间，通过开始时间和结束时间的差值
        SharedPreferences sharedPrefs = getSharedPreferences("GameData", Context.MODE_PRIVATE);
        String startTime = sharedPrefs.getString("startTime", "");
        long timeDifference = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            timeDifference = calculateTimeDifference(startTime, endTime);
        }

        // 将对局时间差保存到数据库中
        values.clear();
        values.put(DatabaseHelper.game_duration, timeDifference);
        executeDatabaseOperation(values);
        Log.d("GameDuration", "对局花费的时间：" + timeDifference + " 毫秒");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private long calculateTimeDifference(String startTime, LocalDateTime endTime) {
        try {
            LocalDateTime startDateTime = LocalDateTime.parse(startTime);

            long timeDifference = ChronoUnit.MILLIS.between(startDateTime, endTime);
            return timeDifference;
        } catch (DateTimeParseException e) {
            e.printStackTrace();
        }

        return 0;
    }

    // 将对局时间差格式化为合适的时间格式，比如 小时:分钟:秒
    private String formatGameDuration(long duration) {
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
}