package akiniyalocts.imgurapiexample.services;


import okhttp3.*;
import java.io.*;

import akiniyalocts.imgurapiexample.imgurmodel.ImageResponse;

/**
 * Created by mcotse on 2016-02-27.
 */


public class ClarifindRest {

    public static final MediaType MEDIA_TYPE_MARKDOWN
            = MediaType.parse("text/x-markdown; charset=utf-8");

    OkHttpClient client = new OkHttpClient();

    public String post(String url) {
        try {
            System.out.println(url);
            RequestBody body = RequestBody.create(MEDIA_TYPE_MARKDOWN, url);
            Request request = new Request.Builder()
                    .url("http://35.14.149.201:4000/android")
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            return response.body().string();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
