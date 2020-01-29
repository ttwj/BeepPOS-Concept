package com.beep.beepposconcept;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Jacek on 3/12/16.
 */

public class UserSessionManager {
    private Context mContext;
    private static UserSessionManager sessionUserManager;
    private SharedPreferences sessionSharedPreferences;
    private SharedPreferences currentUser;
    static boolean loggedIn;
    static String CURRENT_SESSION_USER_NAME;
    static String CURRENT_SESSION_USER_AUTHENTICATION_KEY;
    static String CURRENT_SESSION_USER_MERCHANT_ID;
    public Retrofit retrofit;

    public MerchantAPIService apiService;
    public static String BASE_URL = "http://192.168.1.224:3000/";

    private UserSessionManager(Context context){
        mContext = context;
        currentUser = mContext.getSharedPreferences("CurrentUser", 0);
        sessionSharedPreferences = mContext.getSharedPreferences("UserSessionManager", 0);
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(MerchantAPIService.class);
    }

    public static UserSessionManager createOrGetUserSessionManager(Context mContext){
        if(sessionUserManager == null){
            sessionUserManager = new UserSessionManager(mContext);
        }
        return sessionUserManager;
    }

    public void setContext(Context mContext){
        this.mContext = mContext;
    }

    private boolean checkCredentials(String userName, String passWord, final String merchantID){
        //TODO: Add server capabilities for merchantID handling.
        //Should return if username and password actually exist in database
        Map<String, String> data = new HashMap<>();
        data.put("username", userName);
        data.put("password", passWord);

        Call<LoginResponse> call = apiService.login(data);
        final String userNAME = userName;
        call.enqueue(new Callback<LoginResponse>() {

            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                Log.d("UserSesssion", "yo response");
                Log.d("UserSession", response.toString());
                if(response.body().getSuccess()) {
                    CURRENT_SESSION_USER_AUTHENTICATION_KEY = response.body().getToken();
                    SharedPreferences.Editor editor = currentUser.edit();
                    editor.putString("CurrentUser", userNAME);
                    editor.putString("CurrentAuthenticationKey", CURRENT_SESSION_USER_AUTHENTICATION_KEY);
                    editor.putString("CurrentMerchantID", merchantID);
                    editor.apply();
                    Toast.makeText(mContext, "Logged In!", Toast.LENGTH_LONG).show();
                    CURRENT_SESSION_USER_NAME = userNAME;
                    CURRENT_SESSION_USER_MERCHANT_ID = merchantID;
                    loggedIn = true;
                    Intent intent = new Intent(mContext, PrimaryMainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                } else {
                    Toast.makeText(mContext, response.body().getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.d("Failed", "FAILLLL " + t.toString());
                Toast.makeText(mContext, "Log In Failed!", Toast.LENGTH_LONG).show();
            }

        });
        return true;
    }

    private void addCredentials(final String userName, String passWord){
        //Will return true if successfully verifies that credentials are added
        Map<String, String> data = new HashMap<>();
        data.put("username", userName);
        data.put("password", passWord);

        Call<RegisterResponse> call = apiService.register(data);

        call.enqueue(new Callback<RegisterResponse>() {

            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                Log.d("UserSesssion", "yo response");
                Log.d("UserSession", response.toString());
                if(!checkIfUserAlreadyExists(userName))registerPhoneUser(userName);
                Toast.makeText(mContext, "User Registered", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Log.d("Failed", "FAILLLL " + t.toString());
                Toast.makeText(mContext, "User Registration Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }



    public boolean checkIfLoggedIn(){
        String userName = "";
        String authKey = "";
        String merchantID = "";
        userName = currentUser.getString("CurrentUser", "");
        authKey = currentUser.getString("CurrentAuthenticationKey", "");
        merchantID = currentUser.getString("CurrentMerchantID", "");

        if(!userName.isEmpty() && !authKey.isEmpty() && !merchantID.isEmpty()) {
            CURRENT_SESSION_USER_NAME = userName;
            CURRENT_SESSION_USER_AUTHENTICATION_KEY = authKey;
            CURRENT_SESSION_USER_MERCHANT_ID = merchantID;
            return true;
        }
        return false;
    }

    public void clearSessionSharedPreferences(){
        SharedPreferences.Editor editor = sessionSharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    private boolean checkIfUserAlreadyExists(String userName){
        return (sessionSharedPreferences.contains("UserName" + userName));
    }

    private void registerPhoneUser(String userName){
        CURRENT_SESSION_USER_NAME = userName;
        SharedPreferences.Editor editor = sessionSharedPreferences.edit();
        editor.putString("UserName" + userName, CURRENT_SESSION_USER_NAME);
        editor.apply();
    }

    public void registerUser(String userName, String passWord) {
        addCredentials(userName, passWord);
    }

    public void logIn(String userName, String passWord, String merchantID){
        checkCredentials(userName, passWord, merchantID);
    }

    public boolean logOut(){
        SharedPreferences.Editor editor = currentUser.edit();
        editor.remove("CurrentUser");
        editor.remove("CurrentAuthenticationKey");
        editor.remove("CurrentUserMerchantID");
        editor.apply();
        CURRENT_SESSION_USER_AUTHENTICATION_KEY = "";
        CURRENT_SESSION_USER_NAME = "";
        CURRENT_SESSION_USER_MERCHANT_ID = "";
        loggedIn = false;
        return true;
    }


    //TODO: Remove this when server is online/running smoothly
    public void bypass(String userName, String passWord, String merchantID){
        CURRENT_SESSION_USER_AUTHENTICATION_KEY = "asdfghklkjhgfdsa";
        SharedPreferences.Editor editor = currentUser.edit();
        editor.putString("CurrentUser", userName);
        editor.putString("CurrentAuthenticationKey", CURRENT_SESSION_USER_AUTHENTICATION_KEY);
        editor.putString("CurrentMerchantID", merchantID);
        editor.apply();
        Toast.makeText(mContext, "Logged In!", Toast.LENGTH_LONG).show();
        CURRENT_SESSION_USER_MERCHANT_ID = merchantID;
        CURRENT_SESSION_USER_NAME = userName;
        loggedIn = true;
        Intent intent = new Intent(mContext, PrimaryMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

}


interface MerchantAPIService {
    @FormUrlEncoded
    @POST("merchant_api/register/")
    Call<RegisterResponse> register(@FieldMap Map<String, String> data);

    @FormUrlEncoded
    @POST("merchant_api/login/")
    Call<LoginResponse> login(@FieldMap Map<String, String> data);
}
