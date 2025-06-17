package com.example.interviewformapp

data class Candidate(
    val fullName: String,
    val age: Int,
    val desiredSalary: Int,
    val correctAnswers: Int,

    val totalScore: Int,
    val hasExperience: Boolean,
    val hasTeamSkills: Boolean,
    val readyForTrips: Boolean,
    val passedInterview: Boolean
)

data class ApiResponse(
    val status: String,
    val message: String,
    val passed: Boolean,
    val score: Int,
    val nextStep: String
)
