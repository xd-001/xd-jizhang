package com.myexpense

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var listContainer: LinearLayout
    private lateinit var tvBalance: TextView
    private lateinit var tvIncome: TextView
    private lateinit var tvExpense: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvBudgetProgress: TextView
    private lateinit var tvBudgetLeft: TextView
    
    private var jsonArray = JSONArray()
    private val monthlyBudget = 2000.00

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listContainer = findViewById(R.id.listContainer)
        tvBalance = findViewById(R.id.tvBalance)
        tvIncome = findViewById(R.id.tvIncome)
        tvExpense = findViewById(R.id.tvExpense)
        progressBar = findViewById(R.id.progressBar)
        tvBudgetProgress = findViewById(R.id.tvBudgetProgress)
        tvBudgetLeft = findViewById(R.id.tvBudgetLeft)
        
        val cardBalance = findViewById<LinearLayout>(R.id.cardBalance)
        val cardBudget = findViewById<LinearLayout>(R.id.cardBudget)
        val btnFabAdd = findViewById<Button>(R.id.btnFabAdd)

        val balanceBg = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Color.parseColor("#3B82F6"), Color.parseColor("#2563EB")))
        balanceBg.cornerRadius = 48f
        cardBalance.background = balanceBg

        val budgetBg = GradientDrawable()
        budgetBg.setColor(Color.WHITE)
        budgetBg.cornerRadius = 48f
        cardBudget.background = budgetBg

        val listBg = GradientDrawable()
        listBg.setColor(Color.WHITE)
        listBg.cornerRadius = 48f
        listContainer.background = listBg

        val btnBg = GradientDrawable()
        btnBg.setColor(Color.parseColor("#3B82F6"))
        btnBg.cornerRadius = 100f
        btnFabAdd.background = btnBg

        val sharedPreferences = getSharedPreferences("MyExpenseData", Context.MODE_PRIVATE)
        val historyStr = sharedPreferences.getString("HISTORY_JSON", "[]")
        
        // 【防崩黑科技 1】加 try-catch 捕捉异常的旧数据
        try {
            jsonArray = JSONArray(historyStr)
        } catch (e: Exception) {
            jsonArray = JSONArray()
        }

        if (jsonArray.length() == 0) {
            val dummy = JSONObject()
            dummy.put("name", "猫砂")
            dummy.put("amount", -49.50)
            dummy.put("time", "今天 18:44")
            dummy.put("date", SimpleDateFormat("MM-dd E", Locale.getDefault()).format(Date()))
            dummy.put("account", "支付宝")
            jsonArray.put(dummy)
            sharedPreferences.edit().putString("HISTORY_JSON", jsonArray.toString()).apply()
        }

        refreshUI()

        btnFabAdd.setOnClickListener {
            showAddRecordDialog()
        }
    }

    private fun showAddRecordDialog() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 0)

        val editName = EditText(this)
        editName.hint = "花在哪了？(如：打车)"
        layout.addView(editName)

        val editAmount = EditText(this)
        editAmount.hint = "金额 (支出加负号，如：-15)"
        editAmount.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
        layout.addView(editAmount)

        AlertDialog.Builder(this)
            .setTitle("记一笔明细")
            .setView(layout)
            .setPositiveButton("保存") { _, _ ->
                val name = editName.text.toString()
                val amountStr = editAmount.text.toString()
                if (name.isNotEmpty() && amountStr.isNotEmpty()) {
                    val record = JSONObject()
                    record.put("name", name)
                    record.put("amount", amountStr.toDouble())
                    record.put("time", SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
                    record.put("date", SimpleDateFormat("MM-dd E", Locale.getDefault()).format(Date()))
                    record.put("account", "微信/支付宝")
                    
                    jsonArray.put(record)
                    getSharedPreferences("MyExpenseData", Context.MODE_PRIVATE)
                        .edit().putString("HISTORY_JSON", jsonArray.toString()).apply()
                    refreshUI()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun refreshUI() {
        listContainer.removeAllViews()
        var totalIncome = 0.0
        var totalExpense = 0.0

        val groupedRecords = HashMap<String, MutableList<JSONObject>>()
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.optJSONObject(i) ?: continue
            
            // 【防崩黑科技 2】使用 optDouble 和 optString 替代 get，旧数据没有就用默认值
            val amount = item.optDouble("amount", 0.0)
            if (amount > 0) totalIncome += amount else totalExpense += amount
            
            val date = item.optString("date", "历史记录")
            if (!groupedRecords.containsKey(date)) {
                groupedRecords[date] = mutableListOf()
            }
            groupedRecords[date]!!.add(item)
        }

        tvBalance.text = String.format(Locale.getDefault(), "本月结余 %.2f", totalIncome + totalExpense)
        tvIncome.text = String.format(Locale.getDefault(), "本月收入 %.2f", totalIncome)
        tvExpense.text = String.format(Locale.getDefault(), "本月支出 %.2f", Math.abs(totalExpense))

        val absExpense = Math.abs(totalExpense)
        val progress = ((absExpense / monthlyBudget) * 100).toInt()
        progressBar.progress = Math.min(progress, 100)
        tvBudgetProgress.text = String.format(Locale.getDefault(), "%.2f / %.2f", absExpense, monthlyBudget)
        tvBudgetLeft.text = String.format(Locale.getDefault(), "剩余 %.2f", monthlyBudget - absExpense)

        for ((date, records) in groupedRecords) {
            var dayIncome = 0.0
            var dayExpense = 0.0
            for (r in records) {
                val a = r.optDouble("amount", 0.0)
                if (a > 0) dayIncome += a else dayExpense += a
            }

            val headerLayout = LinearLayout(this)
            headerLayout.orientation = LinearLayout.HORIZONTAL
            headerLayout.gravity = Gravity.CENTER_VERTICAL
            headerLayout.setPadding(0, 20, 0, 20)

            val tvDate = TextView(this)
            tvDate.text = "📅 $date"
            tvDate.textSize = 14f
            tvDate.setTextColor(Color.parseColor("#6B7280"))
            tvDate.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            headerLayout.addView(tvDate)

            val tvDaySummary = TextView(this)
            tvDaySummary.text = String.format(Locale.getDefault(), "收 %.2f  支 %.2f", dayIncome, Math.abs(dayExpense))
            tvDaySummary.textSize = 12f
            tvDaySummary.setTextColor(Color.parseColor("#9CA3AF"))
            headerLayout.addView(tvDaySummary)
            
            listContainer.addView(headerLayout)

            for (record in records) {
                val itemLayout = LinearLayout(this)
                itemLayout.orientation = LinearLayout.HORIZONTAL
                itemLayout.gravity = Gravity.CENTER_VERTICAL
                itemLayout.setPadding(0, 30, 0, 30)

                val iconTv = TextView(this)
                iconTv.text = "🏷️"
                iconTv.textSize = 18f
                iconTv.gravity = Gravity.CENTER
                val iconBg = GradientDrawable()
                iconBg.shape = GradientDrawable.OVAL
                iconBg.setColor(Color.parseColor("#FEE2E2"))
                iconTv.background = iconBg
                iconTv.layoutParams = LinearLayout.LayoutParams(90, 90)
                itemLayout.addView(iconTv)

                val middleLayout = LinearLayout(this)
                middleLayout.orientation = LinearLayout.VERTICAL
                val midParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                midParams.setMargins(30, 0, 0, 0)
                middleLayout.layoutParams = midParams

                val tvName = TextView(this)
                tvName.text = record.optString("name", "未知") // 防崩
                tvName.textSize = 16f
                tvName.setTextColor(Color.parseColor("#111827"))
                val tvTime = TextView(this)
                tvTime.text = record.optString("time", "未知时间") // 防崩
                tvTime.textSize = 12f
                tvTime.setTextColor(Color.parseColor("#9CA3AF"))
                
                middleLayout.addView(tvName)
                middleLayout.addView(tvTime)
                itemLayout.addView(middleLayout)

                val rightLayout = LinearLayout(this)
                rightLayout.orientation = LinearLayout.VERTICAL
                rightLayout.gravity = Gravity.END

                val tvAmt = TextView(this)
                val amt = record.optDouble("amount", 0.0)
                tvAmt.text = String.format(Locale.getDefault(), "%.2f", amt)
                tvAmt.textSize = 18f
                tvAmt.setTypeface(null, Typeface.BOLD)
                tvAmt.setTextColor(if (amt > 0) Color.parseColor("#10B981") else Color.parseColor("#EF4444"))
                
                val tvAcc = TextView(this)
                tvAcc.text = record.optString("account", "默认账户") // 防崩
                tvAcc.textSize = 10f
                tvAcc.setTextColor(Color.parseColor("#9CA3AF"))

                rightLayout.addView(tvAmt)
                rightLayout.addView(tvAcc)
                itemLayout.addView(rightLayout)

                listContainer.addView(itemLayout)
            }
        }

        val btnMore = TextView(this)
        btnMore.text = "查看更多明细 ▼"
        btnMore.textSize = 12f
        btnMore.setTextColor(Color.parseColor("#6B7280"))
        btnMore.gravity = Gravity.CENTER
        btnMore.setPadding(0, 40, 0, 20)
        listContainer.addView(btnMore)
    }
}
