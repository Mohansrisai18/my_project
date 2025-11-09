package com.example.cognisync.del;

import com.example.cognisync.model.Patient;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("user/signup/")
    Call<Void> registerPatient(@Body Patient patient);

    @POST("user/login/")
    Call<Void> loginPatient(@Body Patient patient);
}
