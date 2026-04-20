package com.myexpense

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var recordListLayout: LinearLayout
    private lateinit var tvTotal: TextView
    private var jsonArray = JSONArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editName = findViewById<EditText>(R.id.editName)
        val editAmount = findViewById<EditText>(R.id.editAmount)
        val btnSave = findViewById<Button>(R.id.btnSave)
        tvTotal = findViewById(R.id.tvTotal)
        recordListLayout = findViewById(R.id.recordListLayout)

        // 1. 打开 App 时，读取本地存的历史明细
        val sharedPreferences = getSharedPreferences("MyExpenseData", Context.MODE_PRIVATE)
        val historyStr = sharedPreferences.getString("HISTORY_JSON", "[]")
        jsonArray = JSONArray(historyStr)

        // 2. 把历史明细一条条画到屏幕上，并算出总金额
        refreshUI()

        // 3. 点击“记一笔”时的操作
        btnSave.setOnClickListener {
            val nameStr = editName.text.toString()
            val amountStr = editAmount.text.toString()

            if (nameStr.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(this, "名称和金额都得填哦！", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 把新记录做成一个 JSON 对象
            val newRecord = JSONObject()
            newRecord.put("name", nameStr)
            newRecord.put("amount", amountStr.toDouble())
            
            // 把新记录塞进大列表里
            jsonArray.put(newRecord)

            // 把大列表转换成字符串，永远保存在手机里
            sharedPreferences.edit().putString("HISTORY_JSON", jsonArray.toString()).apply()

            // 刷新屏幕显示
            refreshUI()

            // 清空输入框
            editName.text.clear()
            editAmount.text.clear()
            Toast.makeText(this, "记账成功！", Toast.LENGTH_SHORT).show()
        }
    }

    // 这是一个专门用来刷新界面的方法
    private fun refreshUI() {
        recordListLayout.removeAllViews() // 先清空屏幕上的旧列表
        var totalAmount = 0.0

        // 循环遍历每一笔明细
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            val name = item.getString("name")
            val amount = item.getDouble("amount")
            totalAmount += amount

            // 用代码动态创建一个 TextView 来显示这一笔明细
            val recordView = TextView(this)
            recordView.text = "🛒 $name : ¥$amount"
            recordView.textSize = 16f
            recordView.setTextColor(Color.parseColor("#333333"))
            recordView.setPadding(0, 15, 0, 15)
            
            // 把这笔明细塞进滑动列表里
            recordListLayout.addView(recordView)
        }

        // 更新总金额显示
        tvTotal.text = "总计花费: ¥$totalAmount"
    }
}
