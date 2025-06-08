package com.example.interviewformapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.interviewformapp.ui.theme.InterviewFormAppTheme

class MainActivity : ComponentActivity() {

    private val correctAnswer = mapOf(
        R.id.rgQuestion1 to R.id.rbQ1Option1,
        R.id.rgQuestion2 to R.id.rbQ2Option2,
        R.id.rgQuestion3 to R.id.rbQ3Option3,
        R.id.rgQuestion4 to R.id.rbQ4Option4,
        R.id.rgQuestion5 to R.id.rbQ5Option1,

    )

    private val companyContacts = """
        Контакты нашей компании:
        
        Email: hr@bw321.com
        Телефон: +7 (953) 326-68-20
        Адрес: г. Москва, ул. Программистов, д. 777
        
        Будем рады видеть вас в нашей команде!
    """.trimIndent()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etAge = findViewById<EditText>(R.id.etAge)
        val sbSalary = findViewById<SeekBar>(R.id.sbSalary)
        val tvSalaryValue = findViewById<TextView>(R.id.tvSalaryValue)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        //Обработчик изменения зарплаты
        sbSalary.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvSalaryValue.text = "$progress USD"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Проверка заполнения полей для активации кнопки (TextWatcher - валидатор)
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                btnSubmit.isEnabled = etFullName.text.isNotEmpty() && etAge.text.isNotEmpty()
            }
        }

        etFullName.addTextChangedListener(textWatcher)
        etAge.addTextChangedListener(textWatcher)

        // Обработчик нажатия кнопки

        btnSubmit.setOnClickListener{
            val validationResult = validateInput(
                etFullName.text.toString(),
                etAge.text.toString().toIntOrNull()?:0,
                sbSalary.progress
            )

            if(validationResult != null){

                tvResult.text = validationResult
                tvResult.visibility = View.VISIBLE
                return@setOnClickListener // TODO: Разобраться
            }

            //Подсчёт балов
            var totalScore = 0

            correctAnswer.forEach {
                (questionGroupId, correctAnswerId) ->
                val selectedId = findViewById<RadioGroup>(questionGroupId).checkedRadioButtonId

                if (selectedId == correctAnswerId){
                    totalScore += 2
                }
            }

            // Дополнительные балы
            if(findViewById<CheckBox>(R.id.cbExperience).isChecked){
                totalScore += 2
            }

            if(findViewById<CheckBox>(R.id.cbTeamwork).isChecked){
                totalScore += 2
            }

            if(findViewById<CheckBox>(R.id.cbBussinesTrips).isChecked){
                totalScore += 2
            }

            // Формирование результата

            val resultMessage = if (totalScore >= 10){
                "Поздравляем! Вы набрали $totalScore баллов и прошли тест.\n\n$companyContacts"
            }
            else{
                "К сожалению, вы набрали только $totalScore баллов из 10 необходимых. " +
                        "Попробуйте ещё раз через 6 месяцев."
            }
            tvResult.text = resultMessage
            tvResult.visibility = View.VISIBLE
            tvResult.alpha = 0f
            tvResult.animate().alpha(1f).setDuration(500).start()
        }

    }

    private fun validateInput(fullName: String, age: Int, salary: Int):String?{
        if(fullName.split(" ").size < 3){
            return "Пожалуйста, введите полное ФИО (Фамилия Имя Отчество)"
        }
        if (age < 21 || age > 40){
            return "К сожалению, мы рассматриваем кандидатов в возрасте от 21 до 40 лет"
        }
        if (salary < 800 || salary > 1600){
            return "На данный момент мы можем предложить зарплату только в диапазоне от 800 до 1600 USD"
        }
        return null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("result", findViewById<TextView>(R.id.tvResult).text.toString())
        outState.putInt("visibility", findViewById<TextView>(R.id.tvResult).visibility)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        findViewById<TextView>(R.id.tvResult).text = savedInstanceState.getString("result")
        findViewById<TextView>(R.id.tvResult).visibility = savedInstanceState.getInt("visibility")
    }
}