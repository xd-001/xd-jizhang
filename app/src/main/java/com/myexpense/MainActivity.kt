package com.myexpense

import android.content.Context
import android.content.SharedPreferences
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
import java.util.Calendar
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
    private lateinit var tvDailyLeft: TextView
    private lateinit var prefs: SharedPreferences
    
    private var jsonArray = JSONArray()
    private var monthlyBudget = 2000.00
    private var currentMonthStr = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("MyExpenseData", Context.MODE_PRIVATE)
        currentMonthStr = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        
        // 读取用户自定义的预算，默认 2000
        monthlyBudget = prefs.getFloat("MONTHLY_BUDGET", 2000f).toDouble()

        listContainer = findViewById(R.id.listContainer)
        tvBalance = findViewById(R.id.tvBalance)
        tvIncome = findViewById(R.id.tvIncome)
        tvExpense = findViewById(R.id.tvExpense)
        progressBar = findViewById(R.id.progressBar)
        tvBudgetProgress = findViewById(R.id.tvBudgetProgress)
        tvBudgetLeft = findViewById(R.id.tvBudgetLeft)
        tvDailyLeft = findViewById(R.id.tvDailyLeft)
        
        val cardBalance = findViewById<LinearLayout>(R.id.cardBalance)
        val cardBudget = findViewById<LinearLayout>(R.id.cardBudget)
        val btnFabAdd = findViewById<Button>(R.id.btnFabAdd)

        // 绘制 UI 圆角
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

        // 读取数据
        try {
            jsonArray = JSONArray(prefs.getString("HISTORY_JSON", "[]"))
        } catch (e: Exception) {
            jsonArray = JSONArray()
        }

        refreshUI()

        // --- 交互 1：修改预算 ---
        cardBudget.setOnClickListener {
            showEditBudgetDialog()
        }

        // --- 交互 2：记一笔 ---
        btnFabAdd.setOnClickListener {
            showAddRecordDialog()
        }

        // --- 交互 3：顶部菜单 ---
        findViewById<TextView>(R.id.btnMenu).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("⚠️ 警告")
                .setMessage("确定要清空所有账单数据吗？此操作不可恢复！")
                .setPositiveButton("清空") { _, _ ->
                    jsonArray = JSONArray()
                    prefs.edit().putString("HISTORY_JSON", "[]").apply()
                    refreshUI()
                    Toast.makeText(this, "数据已清空", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("取消", null)
                .show()
        }

        findViewById<TextView>(R.id.btnSync).setOnClickListener {
            Toast.makeText(this, "☁️ 数据已保存在本地安全沙盒", Toast.LENGTH_SHORT).show()
        }
    }

    // 智能记账弹窗 (带收入支出切换)
    private fun showAddRecordDialog() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 0)

        // 收支切换 RadioGroup
        val radioGroup = RadioGroup(this)
        radioGroup.orientation = LinearLayout.HORIZONTAL
        radioGroup.gravity = Gravity.CENTER_HORIZONTAL
        
        val rbExpense = RadioButton(this)
        rbExpense.text = "支出 (默认)"
        rbExpense.isChecked = true // 默认选中支出
        
        val rbIncome = RadioButton(this)
        rbIncome.text = "收入"
        
        radioGroup.addView(rbExpense)
        radioGroup.addView(rbIncome)
        layout.addView(radioGroup)

        val editName = EditText(this)
        editName.hint = "花在哪了？(如：午饭)"
        layout.addView(editName)

        val editAmount = EditText(this)
        editAmount.hint = "输入金额 (仅数字)"
        editAmount.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        layout.addView(editAmount)

        AlertDialog.Builder(this)
            .setTitle("记一笔")
            .setView(layout)
            .setPositiveButton("保存") { _, _ ->
                val name = editName.text.toString()
                val amountStr = editAmount.text.toString()
                if (name.isNotEmpty() && amountStr.isNotEmpty()) {
                    var finalAmount = amountStr.toDouble()
                    // 自动判断正负号！
                    if (rbExpense.isChecked) {
                        finalAmount = -Math.abs(finalAmount)
                    } else {
                        finalAmount = Math.abs(finalAmount)
                    }

                    val record = JSONObject()
                    record.put("id", System.currentTimeMillis()) // 唯一ID，用于精准删除
                    record.put("name", name)
                    record.put("amount", finalAmount)
                    record.put("time", SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
                    record.put("date", SimpleDateFormat("MM-dd E", Locale.getDefault()).format(Date()))
                    record.put("month", currentMonthStr) // 标记月份
                    record.put("account", "默认账户")
                    
                    jsonArray.put(record)
                    prefs.edit().putString("HISTORY_JSON", jsonArray.toString()).apply()
                    refreshUI()
                } else {
                    Toast.makeText(this, "名称和金额不能为空", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    // 修改预算弹窗
    private fun showEditBudgetDialog() {
        val editBudget = EditText(this)
        editBudget.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        editBudget.setText(monthlyBudget.toString())

        AlertDialog.Builder(this)
            .setTitle("设置本月总预算")
            .setView(editBudget)
            .setPositiveButton("确定") { _, _ ->
                val newBudgetStr = editBudget.text.toString()
                if (newBudgetStr.isNotEmpty()) {
                    monthlyBudget = newBudgetStr.toDouble()
                    prefs.edit().putFloat("MONTHLY_BUDGET", monthlyBudget.toFloat()).apply()
                    refreshUI()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    // 核心渲染逻辑
    private fun refreshUI() {
        listContainer.removeAllViews()
        var monthIncome = 0.0
        var monthExpense = 0.0

        val groupedRecords = HashMap<String, MutableList<JSONObject>>()
        
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.optJSONObject(i) ?: continue
            val amount = item.optDouble("amount", 0.0)
            val itemMonth = item.optString("month", "")
            
            // 【核心联动】结余和预算只计算本月的钱！
            if (itemMonth == currentMonthStr || itemMonth.isEmpty()) {
                if (amount > 0) monthIncome += amount else monthExpense += amount
            }
            
            val date = item.optString("date", "未知日期")
            if (!groupedRecords.containsKey(date)) {
                groupedRecords[date] = mutableListOf()
            }
            groupedRecords[date]!!.add(item)
        }

        // 刷新结余
        tvBalance.text = String.format(Locale.getDefault(), "本月结余 %.2f", monthIncome + monthExpense)
        tvIncome.text = String.format(Locale.getDefault(), "收入 %.2f", monthIncome)
        tvExpense.text = String.format(Locale.getDefault(), "支出 %.2f", Math.abs(monthExpense))

        // 刷新预算和日均
        val absExpense = Math.abs(monthExpense)
        val progress = if (monthlyBudget > 0) ((absExpense / monthlyBudget) * 100).toInt() else 0
        progressBar.progress = Math.min(progress, 100)
        
        tvBudgetProgress.text = String.format(Locale.getDefault(), "%.2f / %.2f", absExpense, monthlyBudget)
        val budgetLeft = monthlyBudget - absExpense
        tvBudgetLeft.text = String.format(Locale.getDefault(), "剩余 %.2f", budgetLeft)
        
        val cal = Calendar.getInstance()
        val daysLeft = cal.getActualMaximum(Calendar.DAY_OF_MONTH) - cal.get(Calendar.DAY_OF_MONTH) + 1
        tvDailyLeft.text = String.format(Locale.getDefault(), "剩余日均 %.2f", if (daysLeft > 0) budgetLeft / daysLeft else budgetLeft)

        // 渲染列表 (并按日期倒序排)
        val sortedDates = groupedRecords.keys.sortedDescending()
        for (date in sortedDates) {
            val records = groupedRecords[date]!!
            var dayIncome = 0.0
            var dayExpense = 0.0
            for (r in records) {
                val a = r.optDouble("amount", 0.0)
                if (a > 0) dayIncome += a else dayExpense += a
            }

            // 标题栏
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

            // 账单明细
            for (record in records.reversed()) { // 倒序显示，最新添加的在上面
                val itemLayout = LinearLayout(this)
                itemLayout.orientation = LinearLayout.HORIZONTAL
                itemLayout.gravity = Gravity.CENTER_VERTICAL
                itemLayout.setPadding(0, 30, 0, 30)

                // 交互 4：长按删除明细
                itemLayout.setOnLongClickListener {
                    AlertDialog.Builder(this)
                        .setTitle("确认删除")
                        .setMessage("要删除这笔【${record.optString("name")}】记录吗？")
                        .setPositiveButton("删除") { _, _ ->
                            val idToDelete = record.optLong("id", -1)
                            for (j in jsonArray.length() - 1 downTo 0) {
                                if (jsonArray.optJSONObject(j)?.optLong("id", -1) == idToDelete) {
                                    jsonArray.remove(j)
                                    break
                                }
                            }
                            prefs.edit().putString("HISTORY_JSON", jsonArray.toString()).apply()
                            refreshUI() // 删除后自动重新计算一切！
                        }
                        .setNegativeButton("取消", null)
                        .show()
                    true
                }

                val iconTv = TextView(this)
                val amt = record.optDouble("amount", 0.0)
                iconTv.text = if (amt > 0) "💰" else "🛒"
                iconTv.textSize = 18f
                iconTv.gravity = Gravity.CENTER
                val iconBg = GradientDrawable()
                iconBg.shape = GradientDrawable.OVAL
                iconBg.setColor(if (amt > 0) Color.parseColor("#D1FAE5") else Color.parseColor("#FEE2E2"))
                iconTv.background = iconBg
                iconTv.layoutParams = LinearLayout.LayoutParams(90, 90)
                itemLayout.addView(iconTv)

                val middleLayout = LinearLayout(this)
                middleLayout.orientation = LinearLayout.VERTICAL
                val midParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                midParams.setMargins(30, 0, 0, 0)
                middleLayout.layoutParams = midParams

                val tvName = TextView(this)
                tvName.text = record.optString("name", "未知")
                tvName.textSize = 16f
                tvName.setTextColor(Color.parseColor("#111827"))
                val tvTime = TextView(this)
                tvTime.text = record.optString("time", "未知时间")
                tvTime.textSize = 12f
                tvTime.setTextColor(Color.parseColor("#9CA3AF"))
                
                middleLayout.addView(tvName)
                middleLayout.addView(tvTime)
                itemLayout.addView(middleLayout)

                val rightLayout = LinearLayout(this)
                rightLayout.orientation = LinearLayout.VERTICAL
                rightLayout.gravity = Gravity.END

                val tvAmt = TextView(this)
                tvAmt.text = String.format(Locale.getDefault(), "%.2f", amt)
                tvAmt.textSize = 18f
                tvAmt.setTypeface(null, Typeface.BOLD)
                tvAmt.setTextColor(if (amt > 0) Color.parseColor("#10B981") else Color.parseColor("#EF4444"))
                
                val tvAcc = TextView(this)
                tvAcc.text = record.optString("account", "默认账户")
                tvAcc.textSize = 10f
                tvAcc.setTextColor(Color.parseColor("#9CA3AF"))

                rightLayout.addView(tvAmt)
                rightLayout.addView(tvAcc)
                itemLayout.addView(rightLayout)

                listContainer.addView(itemLayout)
            }
        }
        
        val tvFooter = TextView(this)
        tvFooter.text = "长按任意账单可进行删除"
        tvFooter.textSize = 12f
        tvFooter.setTextColor(Color.parseColor("#9CA3AF"))
        tvFooter.gravity = Gravity.CENTER
        tvFooter.setPadding(0, 30, 0, 30)
        listContainer.addView(tvFooter)
    }
}
