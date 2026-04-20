<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="24dp"
    android:background="#FFFFFF">

    <!-- 顶部的当前月份 (我们在代码里给它加上灰底圆角) -->
    <TextView
        android:id="@+id/tvMonth"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="202X-XX"
        android:textSize="14sp"
        android:textColor="#333333"
        android:paddingStart="16dp"
        android:paddingEnd="16dp"
        android:paddingTop="6dp"
        android:paddingBottom="6dp"
        android:layout_gravity="center_horizontal"
        android:layout_marginBottom="30dp"/>

    <!-- 大圆圈区域：显示总支出 (我们在代码里给它画一个灰色的正圆) -->
    <LinearLayout
        android:id="@+id/circleLayout"
        android:layout_width="160dp"
        android:layout_height="160dp"
        android:gravity="center"
        android:orientation="vertical"
        android:layout_gravity="center_horizontal"
        android:layout_marginBottom="30dp">
        
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="总支出"
            android:textSize="14sp"
            android:textColor="#666666"/>
            
        <TextView
            android:id="@+id/tvTotal"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="0.00"
            android:textSize="32sp"
            android:textStyle="bold"
            android:textColor="#111827"
            android:layout_marginTop="8dp"/>
    </LinearLayout>

    <!-- 输入区域 (必须保留才能记账哦，我把它美化并紧凑排列了) -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_marginBottom="10dp">
        
        <EditText
            android:id="@+id/editName"
            android:layout_width="0dp"
            android:layout_weight="1"
            android:layout_height="50dp"
            android:hint="花费 (例: 吃饭)"
            android:background="#F3F4F6"
            android:padding="10dp"
            android:layout_marginEnd="10dp"/>

        <EditText
            android:id="@+id/editAmount"
            android:layout_width="0dp"
            android:layout_weight="1"
            android:layout_height="50dp"
            android:hint="金额"
            android:inputType="numberDecimal"
            android:background="#F3F4F6"
            android:padding="10dp"/>
    </LinearLayout>

    <!-- 酷黑风格的保存按钮 -->
    <Button
        android:id="@+id/btnSave"
        android:layout_width="match_parent"
        android:layout_height="50dp"
        android:text="记一笔"
        android:textSize="16sp"
        android:backgroundTint="#333333"
        android:textColor="#FFFFFF"
        android:layout_marginBottom="20dp"/>

    <!-- 底部逐月统计标题 -->
    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="📊 逐月支出统计"
        android:textSize="16sp"
        android:textStyle="bold"
        android:textColor="#333333"
        android:layout_marginBottom="10dp"/>

    <!-- 滑动列表 -->
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1">
        <LinearLayout
            android:id="@+id/recordListLayout"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"/>
    </ScrollView>
</LinearLayout>
