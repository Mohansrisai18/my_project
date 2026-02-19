//package com.example.cognisync.del;
//
//import retrofit2.Retrofit;
//import retrofit2.converter.gson.GsonConverterFactory;
//
//public class ApiClient {
//
//    private static final String BASE_URL = "http://10.60.229.221:8000/";
//
//    private static Retrofit retrofit;
//
//    public static Retrofit getClient() {
//        if (retrofit == null) {
//            retrofit = new Retrofit.Builder()
//                    .baseUrl(BASE_URL)
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .build();
//        }
//        return retrofit;
//    }
//}
package com.example.cognisync.del;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class ApiClient {
    // If you run server on your PC and test on a real device on the same Wi-Fi:
    // replace 10.60.229.221 with your PC's IPv4 address (ipconfig shows it).
//    private static final String BASE_URL = "http://10.66.101.221:8000/";
//    private static final String BASE_URL = "http://10.191.129.221:8000/";
//    private static final String BASE_URL = "http://10.114.152.221:8000/";
//    private static final String BASE_URL = "http://10.123.184.221:8000/";
//    private static final String BASE_URL = "http://10.200.185.145:8000/";
//    private static final String BASE_URL = "http://127.0.0.1:8000/";
//    private static final String BASE_URL = "http://10.208.180.26:8000/";
//    private static final String BASE_URL = "http://10.115.82.187:8000/";
//    private static final String BASE_URL = "http://192.168.1.4:8000/";

//    private static final String BASE_URL = "http://10.190.101.221:8000/";
//private static final String BASE_URL = "http://10.202.20.221:8000/";
   /* private static final String BASE_URL = "http://10.57.186.221:8000/";*/

//    private static final String BASE_URL = "http://10.37.216.221:8000/";

    private static final String BASE_URL = "http://10.235.171.221:8000/";

//    private static final String BASE_URL = "http://180.235.121.253:8031/";









//    private static final String BASE_URL = "http://10.254.78.113:8000/";
//    private static final String BASE_URL = "http://10.151.211.172:8000/";

    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client) // 🔥 attach timeout-enabled client
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
