package akiniyalocts.imgurapiexample.services;

import android.content.Context;

import java.io.*;

import java.lang.ref.WeakReference;
import java.io.IOException;

import akiniyalocts.imgurapiexample.Constants;
import akiniyalocts.imgurapiexample.helpers.NotificationHelper;
import akiniyalocts.imgurapiexample.imgurmodel.ImageResponse;
import akiniyalocts.imgurapiexample.imgurmodel.ImgurAPI;
import akiniyalocts.imgurapiexample.imgurmodel.Upload;
import akiniyalocts.imgurapiexample.utils.NetworkUtils;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okhttp3.Response;

/**
 * Created by AKiniyalocts on 1/12/15.
 * <p/>
 * Our upload service. This creates our restadapter, uploads our image, and notifies us of the response.
 */


public class UploadService {

    public final static String TAG = UploadService.class.getSimpleName();

    private WeakReference<Context> mContext;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public UploadService(Context context) {
        this.mContext = new WeakReference<>(context);
    }

    public void Execute(Upload upload, Callback<ImageResponse> callback) {
        final Callback<ImageResponse> cb = callback;

        if (!NetworkUtils.isConnected(mContext.get())) {
            //Callback will be called, so we prevent a unnecessary notification
            cb.failure(null);
            return;
        }

        final NotificationHelper notificationHelper = new NotificationHelper(mContext.get());
        notificationHelper.createUploadingNotification();

        RestAdapter restAdapter = buildRestAdapter();

        restAdapter.create(ImgurAPI.class).postImage(
                Constants.getClientAuth(),
                upload.title,
                upload.description,
                upload.albumId,
                null,
                new TypedFile("image/*", upload.image),
                new Callback<ImageResponse>() {
                    @Override
                    public void success(ImageResponse imageResponse, Response response) {
                        if (cb != null) cb.success(imageResponse, response);
                        if (response == null) {
                            /*
                             Notify image was NOT uploaded successfully
                            */
                            notificationHelper.createFailedUploadNotification();
                            return;
                        }
                        /*
                        Notify image was uploaded successfully
                        */
                        if (imageResponse.success) {
                            ClarifindRest test = new ClarifindRest();

                            test.post("nigga");

                            notificationHelper.createUploadedNotification(imageResponse);
                            //Posting the URL to the Node server
//                            OkHttpClient client = new OkHttpClient();
//                            String post(String url, String json) throws IOException {
//                                RequestBody body = RequestBody.create(JSON, json);
//                                Request request = new Request.Builder()
//                                        .url(http://35.14.149.201:4000/android)
//                                        .post(imageResponse)
//                                        .build();
//                                okhttp3.Response okresponse = client.newCall(request).execute();
//                                return okresponse.body().string();
//                            }
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (cb != null) cb.failure(error);
                        notificationHelper.createFailedUploadNotification();
                    }
                });
    }

    private RestAdapter buildRestAdapter() {
        RestAdapter imgurAdapter = new RestAdapter.Builder()
                .setEndpoint(ImgurAPI.server)
                .build();

        /*
        Set rest adapter logging if we're already logging
        */
        if (Constants.LOGGING)
            imgurAdapter.setLogLevel(RestAdapter.LogLevel.FULL);
        return imgurAdapter;
    }
}
