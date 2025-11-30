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

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class ApiClient {
    // If you run server on your PC and test on a real device on the same Wi-Fi:
    // replace 10.60.229.221 with your PC's IPv4 address (ipconfig shows it).
//    private static final String BASE_URL = "http://10.66.101.221:8000/";
    private static final String BASE_URL = "http://10.191.129.221:8000/";
//    private static final String BASE_URL = "http://127.0.0.1:8000/";

    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
