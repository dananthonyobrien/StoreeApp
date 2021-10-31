package org.wit.placemark.activities;

import org.wit.placemark.models.Quote;
import com.google.android.gms.common.api.Api;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

class RetrofitClient {

    private static RetrofitClient instance = null;
    private Api myApi;

    private RetrofitClient() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Api.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        myApi = retrofit.create(Api.class);
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public Api getMyApi() {
        return myApi;
    }

    public interface Api {

        String BASE_URL = "https://shakespeare-quotes-generator.herokuapp.com/api/v1/quotes/single";
        @GET("quote")
        Call getQuote();
    }
}

