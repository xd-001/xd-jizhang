package com.myexpense

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var rootLayout: LinearLayout
    private lateinit var recordListLayout: LinearLayout
    private lateinit var tvTotal: TextView
    private lateinit var tvMonth: TextView
    private lateinit var imgHeader: ImageView
    private var jsonArray = JSONArray()
    private var currentMonthStr = ""
    private var selectedMonthStr = "" // 当前选中的用来查看的月份

    // 处理选择图片的黑科技
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                // 向系统申请永久保留这张图片的读取权限
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                getSharedPreferences("MyExpenseData", Context.MODE_PRIVATE).edit().putString("HEADER_URI", it.toString()).apply()
                imgHeader.setImageURI(it)
            } catch (e: Exception) {
                Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rootLayout = findViewById(R.id.rootLayout)
        val editName = findViewById<EditText>(R.id.editName)
        val editAmount = findViewById<EditText>(R.id.editAmount)
        val btnSave = findViewById<Button>(R.id.btnSave)
        tvTotal = findViewById(R.id.tvTotal)
        tvMonth = findViewById(R.id.tvMonth)
        recordListLayout = findViewById(R.id.recordListLayout)
        val circleLayout = findViewById<LinearLayout>(R.id.circleLayout)
        imgHeader = findViewById(R.id.imgHeader)
        val btnTheme = findViewById<TextView>(R.id.btnTheme)
        val btnImage = findViewById<TextView>(R.id.btnImage)
        val layoutTemplates = findViewById<LinearLayout>(R.id.layoutTemplates)

        // --- 1. 画出好看的圆角 ---
        val monthBg = GradientDrawable()
        monthBg.shape = GradientDrawable.RECTANGLE
        monthBg.cornerRadius = 50f
        monthBg.setColor(Color.parseColor("#B3E5E7EB"))
        tvMonth.background = monthBg

        val circleBg = GradientDrawable()
        circleBg.shape = GradientDrawable.OVAL
        circleBg.setColor(Color.parseColor("#80F3F4F6"))
        circleLayout.background = circleBg

        // --- 2. 初始化时间和数据 ---
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        currentMonthStr = sdf.format(Date())
        selectedMonthStr = currentMonthStr // 默认选中当前月
        tvMonth.text = "$selectedMonthStr ▼"

        val sharedPreferences = getSharedPreferences("MyExpenseData", Context.MODE_PRIVATE)
        val historyStr = sharedPreferences.getString("HISTORY_JSON", "[]")
        jsonArray = JSONArray(historyStr)

        // --- 3. 加载自定义封面和主题 ---
        val savedUri = sharedPreferences.getString("HEADER_URI", null)
        if (savedUri != null) {
            try {
                imgHeader.setImageURI(Uri.parse(savedUri))
            } catch (e: Exception) { }
        }
        val savedTheme = sharedPreferences.getString("THEME_COLOR", "#FFFFFF")
        rootLayout.setBackgroundColor(Color.parseColor(savedTheme))

        // --- 4. 生成快捷模板按钮 ---
        val templates = arrayOf("🍱 吃饭", "🚕 交通", "🥤 零食", "🛒 购物", "🏠 房租", "🎮 娱乐", "🏥 医疗")
        for (t in templates) {
            val tagBtn = TextView(this)
            tagBtn.text = t
            tagBtn.textSize = 14f
            tagBtn.setTextColor(Color.parseColor("#333333"))
            
            val tagBg = GradientDrawable()
            tagBg.shape = GradientDrawable.RECTANGLE
            tagBg.cornerRadius = 30f
            tagBg.setColor(Color.parseColor("#E5E7EB"))
            tagBtn.background = tagBg
            tagBtn.setPadding(35, 15, 35, 15)

            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 0, 20, 0)
            tagBtn.layoutParams = params

            tagBtn.setOnClickListener { editName.setText(t) } // 点击填入输入框
            layoutTemplates.addView(tagBtn)
        }

        refreshUI()

        // --- 5. 各种点击事件 ---
        // 记一笔
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
            newRecord.put("month", currentMonthStr) // 记账永远记在真实的当前月
            
            jsonArray.put(newRecord)
            sharedPreferences.edit().putString("HISTORY_JSON", jsonArray.toString()).apply()

            refreshUI()
            editName.text.clear()
            editAmount.text.clear()
            Toast.makeText(this, "记账成功！", Toast.LENGTH_SHORT).show()
        }

        // 选择封面图片
        btnImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // 选择主题颜色
        btnTheme.setOnClickListener {
            val themeNames = arrayOf("极简白", "蜜桃粉", "薄荷绿", "海天蓝", "柠檬黄")
            val themeColors = arrayOf("#FFFFFF", "#FFF0F5", "#F0FFF0", "#F0F8FF", "#FFFFE0")
            
            AlertDialog.Builder(this)
                .setTitle("选择主题颜色")
                .setItems(themeNames) { _, which ->
                    val colorHex = themeColors[which]
                    rootLayout.setBackgroundColor(Color.parseColor(colorHex))
                    sharedPreferences.edit().putString("THEME_COLOR", colorHex).apply()
                }
                .show()
        }

        // 选择月份
        tvMonth.setOnClickListener {
            // 生成最近12个月的列表
            val monthList = Array(12) { i ->
                val cal = Calendar.getInstance()
                cal.add(Calendar.MONTH, -i)
                sdf.format(cal.time)
            }
            
            AlertDialog.Builder(this)
                .setTitle("切换月份查看")
                .setItems(monthList) { _, which ->
                    selectedMonthStr = monthList[which]
                    tvMonth.text = "$selectedMonthStr ▼"
                    refreshUI() // 刷新大圆圈和列表
                }
                .show()
        }
    }

    private fun refreshUI() {
        recordListLayout.removeAllViews()
        val monthTotals = HashMap<String, Double>()
        var displayMonthTotal = 0.0

        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            val amount = item.getDouble("amount")
            val month = if (item.has("month")) item.getString("month") else currentMonthStr
            
            val oldTotal = monthTotals[month] ?: 0.0
            monthTotals[month] = oldTotal + amount

            // 只有属于当前【选中的月份】的金额，才算进大圆圈里
            if (month == selectedMonthStr) {
                displayMonthTotal += amount
            }
        }

        // 更新大圆圈金额
        tvTotal.text = String.format(Locale.getDefault(), "%.2f", displayMonthTotal)

        val sortedMonths = monthTotals.keys.sortedDescending()
        for (month in sortedMonths) {
            val monthAmount = monthTotals[month] ?: 0.0
            
            val recordLayout = LinearLayout(this)
            recordLayout.orientation = LinearLayout.HORIZONTAL
            recordLayout.setPadding(40, 40, 40, 40)
            
            val itemBg = GradientDrawable()
            itemBg.shape = GradientDrawable.RECTANGLE
            itemBg.cornerRadius = 20f
            itemBg.setColor(Color.parseColor("#66FFFFFF")) // 半透明白底，适配各种主题色
            recordLayout.background = itemBg

            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
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
