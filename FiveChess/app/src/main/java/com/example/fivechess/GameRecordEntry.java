package com.example.fivechess;

import android.provider.BaseColumns;

public class GameRecordEntry implements BaseColumns {
    public static final String TABLE_NAME = "mysql"; // 数据表格名称
    public static final String COLUMN_DURATION = "duration"; // 游戏持续时间列
    public static final String COLUMN_WINNER = "winner"; // 获胜者列

    // 列索引
    public static final int COLUMN_INDEX_ID = 0;
    public static final int COLUMN_INDEX_DURATION = 1;
    public static final int COLUMN_INDEX_WINNER = 2;
}
