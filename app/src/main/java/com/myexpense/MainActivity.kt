package com.myexpense

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. 告诉系统：用我们刚才画的 XML 文件作为界面
        setContentView(R.layout.activity_main)

        // 2. 从界面上找到对应的输入框、按钮和文字
        val editName = findViewById<EditText>(R.id.editName)
        val editAmount = findViewById<EditText>(R.id.editAmount)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val tvTotal = findViewById<TextView>(R.id.tvTotal)

        // 3. 准备手机的本地存储（类似一个叫 MyExpenseData 的账本文件）
        val sharedPreferences = getSharedPreferences("MyExpenseData", Context.MODE_PRIVATE)

        // 4. App 刚打开时，读取之前存的总金额并显示出来（如果没有，默认是 0.0）
        var currentTotal = sharedPreferences.getFloat("TOTAL_AMOUNT", 0.0f)
        tvTotal.text = "总计花费: ¥$currentTotal"

        // 5. 当“记一笔”按钮被点击时，执行花括号里的代码
        btnSave.setOnClickListener {
            val amountStr = editAmount.text.toString()
            val nameStr = editName.text.toString()

            // 如果金额没填，提示用户
            if (amountStr.isEmpty()) {
                Toast.makeText(this, "得填金额呀！", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 把填写的文字变成小数，加到总金额里
            val amount = amountStr.toFloat()
            currentTotal += amount

            // 把最新的总金额偷偷存到手机账本里（这样就算重启手机也不会丢数据）
            sharedPreferences.edit().putFloat("TOTAL_AMOUNT", currentTotal).apply()

            // 刷新界面上显示的金额
            tvTotal.text = "总计花费: ¥$currentTotal"

            // 清空输入框，方便记下一笔
            editName.text.clear()
            editAmount.text.clear()

            // 屏幕下方弹出一个黑色小提示
            Toast.makeText(this, "记账成功！", Toast.LENGTH_SHORT).show()
        }
    }
}
