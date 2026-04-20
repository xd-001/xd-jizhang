package com.myexpense

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.graphics.Color

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 我们先用纯代码写一个简单的界面
        val textView = TextView(this)
        textView.text = "🎉 成功啦！\n\n这是一个纯正的安卓APP，完全在GitHub云端编译生成！\n\n接下来，我们将在这里写出记账功能。"
        textView.textSize = 22f
        textView.setTextColor(Color.BLACK)
        textView.setPadding(60, 100, 60, 60)
        
        setContentView(textView)
    }
}
