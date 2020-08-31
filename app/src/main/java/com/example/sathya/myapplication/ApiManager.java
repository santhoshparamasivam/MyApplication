package com.example.sathya.myapplication;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiManager {
    @Multipart
    @POST("user/updateUserProfile")
    Call<ResponseBody> updateImage(@Part MultipartBody.Part image);

}