package com.example.cognisync.del;

import com.example.cognisync.model.LoginRequest;
import com.example.cognisync.model.Patient;
import com.example.cognisync.model.ScoreRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    // INITIAL endpoints
    @POST("user/save-maas-initial/")
    Call<Void> saveMaasInitial(@Body ScoreRequest body);

    @POST("user/save-panas-initial/")
    Call<Void> savePanasInitial(@Body ScoreRequest body);

    @POST("user/save-dass-initial/")
    Call<Void> saveDassInitial(@Body ScoreRequest body);

    @POST("user/save-erq-initial/")
    Call<Void> saveErqInitial(@Body ScoreRequest body);

    @POST("user/save-phlms-initial/")
    Call<Void> savePhlmsInitial(@Body ScoreRequest body);

    // existing auth endpoints
    @POST("user/signup/")
    Call<Void> registerPatient(@Body Patient patient);

    @POST("user/login/")
    Call<Void> loginPatient(@Body LoginRequest loginRequest);

    @GET("user/profile/{email}/")
    Call<Patient> getPatientProfile(@Path("email") String email);
}
