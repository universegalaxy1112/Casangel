package com.itikarus.miska.network;



import com.itikarus.miska.oauth.BasicOAuth;
import com.itikarus.miska.oauth.OAuthInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;


/**
 * APIClient handles all the Network API Requests using Retrofit Library
 **/

public class APIClient {
    
    public static Retrofit retrofit;
    private static APIRequests apiRequests;
    public static final String BASE_URL = "https://www.casangel.com.co";
    private static final String WOOCOMMERCE_CONSUMER_KEY = "ck_dc5ed563cbc0a3e23db82aa8c11747f147488eb6";//
    private static final String WOOCOMMERCE_CONSUMER_SECRET = "cs_d41ef295ebaf09c8c4875f43dadebf05753aea4c";//
    
    
    // Singleton Instance of APIRequests
    public static APIRequests getInstance() {
        if (apiRequests == null) {
            
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            OAuthInterceptor oauth1Woocommerce = new OAuthInterceptor.Builder()
                    .consumerKey(WOOCOMMERCE_CONSUMER_KEY)
                    .consumerSecret(WOOCOMMERCE_CONSUMER_SECRET)
                    .build();
            
            BasicOAuth basicOAuthWoocommerce = new BasicOAuth.Builder()
                    .consumerKey(WOOCOMMERCE_CONSUMER_KEY)
                    .consumerSecret(WOOCOMMERCE_CONSUMER_SECRET)
                    .build();
            
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(BASE_URL.startsWith("http://")?  oauth1Woocommerce : basicOAuthWoocommerce)
                    .build();
           /* OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(BASE_URL.startsWith("http://")?  oauth1Woocommerce : basicOAuthWoocommerce)
                    .addInterceptor(interceptor)
                    .build();*/
            
            
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            
            
            apiRequests = retrofit.create(APIRequests.class);
            
            return apiRequests;
            
        }
        else {
            return apiRequests;
        }
    }
    
}
