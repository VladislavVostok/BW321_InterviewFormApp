package com.example.interviewformapp

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


interface ApiService {
    @POST("candidates")
    fun sendCandidateData(@Body candidate: Candidate): Call<ApiResponse>
}