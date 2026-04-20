package com.myexpense

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var recordListLayout: LinearLayout
    private lateinit var tvTotal: TextView
    private lateinit var tvMonth: TextView
    private var jsonArray = JSONArray()
    private var currentMonthStr = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editName = findViewById<EditText>(R.id.editName)
        val editAmount = findViewById<EditText>(R.id.editAmount)
        val btnSave = findViewById<Button>(R.id.btnSave)
        tvTotal = findViewById(R.id.tvTotal)
        tvMonth = findViewById(R.id.tvMonth)
        recordListLayout = findViewById(R.id.recordListLayout)
        val circleLayout = findViewById<LinearLayout>(R.id.circleLayout)

        val monthBg = GradientDrawable()
        monthBg.shape = GradientDrawable.RECTANGLE
        monthBg.cornerRadius = 50f
        monthBg.setColor(Color.parseColor("#E5E7EB"))
        tvMonth.background = monthBg

        val circleBg = GradientDrawable()
        circleBg.shape = GradientDrawable.OVAL
        circleBg.setColor(Color.parseColor("#F3F4F6"))
        circleLayout.background = circleBg

        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        currentMonthStr = sdf.format(Date())
        tvMonth.text = currentMonthStr

        val sharedPreferences = getSharedPreferences("MyExpenseData", Context.MODE_PRIVATE)
        val historyStr = sharedPreferences.getString("HISTORY_JSON", "[]")
        jsonArray = JSONArray(historyStr)

        refreshUI()

        btnSave.setOnClickListener {
            val nameStr = editName.text.toString()
            val amountStr = editAmount.text.toString()

            if (nameStr.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(this, "填完整哦！", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newRecord = JSONObject()
            newRecord.put("name", nameStr)
            newRecord.put("amount", amountStr.toDouble())
            newRecord.put("month", currentMonthStr)
            
            jsonArray.put(newRecord)
            sharedPreferences.edit().putString("HISTORY_JSON", jsonArray.toString()).apply()

            refreshUI()

            editName.text.clear()
            editAmount.text.clear()
            Toast.makeText(this, "记账成功！", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshUI() {
        recordListLayout.removeAllViews()
        
        val monthTotals = HashMap<String, Double>()
        var currentMonthTotal = 0.0

        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            val amount = item.getDouble("amount")
            val month = if (item.has("month")) item.getString("month") else currentMonthStr
            
            val oldTotal = monthTotals[month] ?: 0.0
            monthTotals[month] = oldTotal + amount

            if (month == currentMonthStr) {
                currentMonthTotal += amount
            }
        }

        tvTotal.text = String.format(Locale.getDefault(), "%.2f", currentMonthTotal)

        val sortedMonths = monthTotals.keys.sortedDescending()
        
        for (month in sortedMonths) {
            val monthAmount = monthTotals[month] ?: 0.0
            
            val recordLayout = LinearLayout(this)
            recordLayout.orientation = LinearLayout.HORIZONTAL
            recordLayout.setPadding(40, 40, 40, 40)
            
            val itemBg = GradientDrawable()
            itemBg.shape = GradientDrawable.RECTANGLE
            itemBg.cornerRadius = 20f
            itemBg.setColor(Color.parseColor("#F9FAFB"))
            recordLayout.background = itemBg

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 20)
            recordLayout.layoutParams = params

            val tvMonthItem = TextView(this)
            tvMonthItem.text = month
            tvMonthItem.textSize = 16f
            tvMonthItem.setTextColor(Color.parseColor("#4B5563"))
            tvMonthItem.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

            val tvAmountItem = TextView(this)
            tvAmountItem.text = "¥ ${String.format(Locale.getDefault(), "%.2f", monthAmount)}"
            tvAmountItem.textSize = 18f
            tvAmountItem.typeface = Typeface.DEFAULT_BOLD
            tvAmountItem.setTextColor(Color.parseColor("#111827"))
            tvAmountItem.gravity = Gravity.END

            recordLayout.addView(tvMonthItem)
            recordLayout.addView(tvAmountItem)
            
            recordListLayout.addView(recordLayout)
        }
    }
}
