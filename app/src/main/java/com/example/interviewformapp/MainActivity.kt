package com.example.interviewformapp

import android.os.Bundle
import android.util.Log
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



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
            var correctAnswersCount = 0;
            var totalScore = 0

            correctAnswer.forEach {
                (questionGroupId, correctAnswerId) ->
                val selectedId = findViewById<RadioGroup>(questionGroupId).checkedRadioButtonId

                if (selectedId == correctAnswerId){
                    correctAnswersCount++
                    totalScore += 2
                }
            }

            // Дополнительные балы
            val hasExperience = findViewById<CheckBox>(R.id.cbExperience).isChecked
            val hasTeamSkills = findViewById<CheckBox>(R.id.cbTeamwork).isChecked
            val readyForTrips = findViewById<CheckBox>(R.id.cbBussinesTrips).isChecked

            if(hasExperience) totalScore += 2
            if(hasTeamSkills) totalScore += 2
            if(readyForTrips) totalScore += 2

            val passedInterview = totalScore >= 10


            val candidate = Candidate(
                fullName = etFullName.text.toString(),
                age = etAge.text.toString().toInt(),
                desiredSalary = sbSalary.progress,
                correctAnswers = correctAnswersCount,
                totalScore = totalScore,
                hasExperience = hasExperience,
                hasTeamSkills = hasTeamSkills,
                readyForTrips = readyForTrips,
                passedInterview = passedInterview
            )


            sendDataToServer(candidate, passedInterview, totalScore)
        }

    }

    private fun sendDataToServer(candidate: Candidate, passed: Boolean, totalScore: Int){
        val progressBar = ProgressBar(this).apply{
            visibility = View.VISIBLE
        }

        (findViewById<View>(android.R.id.content) as ViewGroup).addView(progressBar)

        RetrofitClient.instance.sendCandidateData(candidate).enqueue(object : Callback<ApiResponse> {

            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>){
                progressBar.visibility = View.GONE
                (findViewById<View>(android.R.id.content) as ViewGroup).removeView(progressBar)

                if(response.isSuccessful){
                    val apiResponse = response.body()
                    val resultMessage = if (passed){
                        "Поздравляем! Вы набрали $totalScore баллов и прошли тест.\n\n" + "${apiResponse?.nextStep ?: companyContacts}"
                    } else {
                        "К сожалению, вы набрали только $totalScore баллов из 10 необходимых. " +
                                "Попробуйте ещё раз через 6 месяцев."
                    }
                    findViewById<TextView>(R.id.tvResult).text = resultMessage


                } else {
                    val errorMessage = "Ошибка сервера: ${response.code()}"
                    findViewById<TextView>(R.id.tvResult).text = errorMessage
                    Log.e("API_ERROR", errorMessage)
                }
            findViewById<TextView>(R.id.tvResult).visibility = View.VISIBLE
            findViewById<TextView>(R.id.tvResult).alpha = 0f
            findViewById<TextView>(R.id.tvResult).animate().alpha(1f).setDuration(500).start()
        }


            override fun onFailure(call: Call<ApiResponse>, t: Throwable){
                progressBar.visibility = View.GONE
                (findViewById<View>(android.R.id.content) as ViewGroup).removeView(progressBar)

                val errorMessage = "Ошибка соединения: ${t.localizedMessage}"
                findViewById<TextView>(R.id.tvResult).text = errorMessage
                findViewById<TextView>(R.id.tvResult).visibility = View.VISIBLE
                findViewById<TextView>(R.id.tvResult).alpha = 0f
                findViewById<TextView>(R.id.tvResult).animate().alpha(1f).setDuration(500).start()

                Log.e("API_FAILURE", errorMessage)
            }
            })
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