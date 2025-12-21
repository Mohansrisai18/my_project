


package com.example.cognisync.del;

import com.example.cognisync.model.AudioResponse;
import com.example.cognisync.model.LoginRequest;
import com.example.cognisync.model.Patient;
import com.example.cognisync.model.ScoreRequest;
import com.example.cognisync.model.ScoreResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ===============================
    // AUTH / PROFILE
    // ===============================
    @POST("user/signup/")
    Call<Void> registerPatient(@Body Patient patient);

    @POST("user/login/")
    Call<Void> loginPatient(@Body LoginRequest request);

    @GET("user/profile/{email}/")
    Call<Patient> getPatientProfile(@Path("email") String email);

    // ===============================
    // 🔐 FORGOT PASSWORD (OTP FLOW ONLY)
    // ===============================
    @POST("user/send-otp/")
    Call<Void> sendOtp(@Body Map<String, String> body);

    @POST("user/verify-otp/")
    Call<Void> verifyOtp(@Body Map<String, String> body);

    @POST("user/reset-password/")
    Call<Void> resetPassword(@Body Map<String, String> body);



    // ===============================
    // INITIAL QUESTIONNAIRES
    // ===============================
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

    // ===============================
    // PRE QUESTIONNAIRES
    // ===============================
    @POST("user/save-maas-pre/")
    Call<Void> saveMaasPre(@Body ScoreRequest body);

    @POST("user/save-panas-pre/")
    Call<Void> savePanasPre(@Body ScoreRequest body);

    @POST("user/save-dass-pre/")
    Call<Void> saveDassPre(@Body ScoreRequest body);

    @POST("user/save-cfq-pre/")
    Call<Void> saveCfqPre(@Body ScoreRequest body);

    @POST("user/save-phlms-pre/")
    Call<Void> savePhlmsPre(@Body ScoreRequest body);

    // ===============================
    // POST / TASK ENDPOINTS
    // ===============================
    @POST("user/save-srt-post/")
    Call<Void> saveSrtPost(@Body ScoreRequest body);

    @POST("user/save-nback-post/")
    Call<Void> saveNbackPost(@Body ScoreRequest body);

    @POST("user/save-stroop-post/")
    Call<Void> saveStroopPost(@Body ScoreRequest body);

    @POST("user/save-task-switch-post/")
    Call<Void> saveTaskSwitchPost(@Body ScoreRequest body);

    @POST("user/save-sart-post/")
    Call<Void> saveSartPost(@Body ScoreRequest body);

    // ===============================
    // GENERIC SCORE SAVE (OPTIONAL)
    // ===============================
    @POST("user/save-score/")
    Call<Void> saveScore(@Body ScoreRequest body);

    // ===============================
    // FETCH SCORE HISTORY
    // ===============================
    @GET("user/scores/{email}/")
    Call<List<ScoreResponse>> getScoreHistory(
            @Path("email") String email,
            @Query("domain") String domain
    );

    // ===============================
    // AUDIO CONTENT
    // ===============================
    @GET("user/audios/")
    Call<List<AudioResponse>> getAudios(@Query("module") String module);
}
