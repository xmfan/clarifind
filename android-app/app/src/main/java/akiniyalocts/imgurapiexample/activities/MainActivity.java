package akiniyalocts.imgurapiexample.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.ImageView;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import akiniyalocts.imgurapiexample.R;
import akiniyalocts.imgurapiexample.helpers.DocumentHelper;
import akiniyalocts.imgurapiexample.helpers.IntentHelper;
import akiniyalocts.imgurapiexample.imgurmodel.ImageResponse;
import akiniyalocts.imgurapiexample.imgurmodel.Upload;
import akiniyalocts.imgurapiexample.services.UploadService;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    private String photoPath;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    public final static String TAG = MainActivity.class.getSimpleName();
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private Button cameraButton;

    private Uri fileUri;

    /*
      These annotations are for ButterKnife by Jake Wharton
      https://github.com/JakeWharton/butterknife
     */
    @Bind(R.id.imageview)
    ImageView uploadImage;
    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private Upload upload; // Upload object containing image and meta data
    private File chosenFile; //chosen file from intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        cameraButton = (Button) findViewById(R.id.camera_button);
        setSupportActionBar(toolbar);
//        cameraButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//                    File photoFile = null;
//                    try {
//                        photoFile = createImageFile();
//                    } catch (IOException ex) {
//                        // do nothing
//                    }
//                    // Continue only if the File was successfully created
//                    if (photoFile != null) {
//                        fileUri = Uri.fromFile(photoFile);
//                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
//                                fileUri);
//                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//                    }
//                }
//            }
//        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri returnUri;

        if (requestCode != IntentHelper.FILE_PICK) {
            return;
        }

        if (resultCode != RESULT_OK) {
            return;
        }

        returnUri = data.getData();
        String filePath = DocumentHelper.getPath(this, returnUri);
        //Safety check to prevent null pointer exception
        if (filePath == null || filePath.isEmpty()) return;
        chosenFile = new File(filePath);

                /*
                    Picasso is a wonderful image loading tool from square inc.
                    https://github.com/square/picasso
                 */
        Picasso.with(getBaseContext())
                .load(chosenFile)
                .placeholder(R.drawable.ic_photo_library_black)
                .fit()
                .into(uploadImage);

    }


    @OnClick(R.id.imageview)
    public void onChooseImage() {
//        uploadDesc.clearFocus();
//        uploadTitle.clearFocus();
        IntentHelper.chooseFileIntent(this);
    }
//
//    private void clearInput() {
//        uploadTitle.setText("");
//        uploadDesc.clearFocus();
//        uploadDesc.setText("");
//        uploadTitle.clearFocus();
//        uploadImage.setImageResource(R.drawable.ic_photo_library_black);
//    }

    @OnClick(R.id.fab)
    public void uploadImage() {
    /*
      Create the @Upload object
     */
        if (chosenFile == null) return;
        createUpload(chosenFile);

    /*
      Start upload
     */
        new UploadService(this).Execute(upload, new UiCallback());
    }

    private void createUpload(File image) {
        upload = new Upload();

        upload.image = image;
//        upload.title = uploadTitle.getText().toString();
//        upload.description = uploadDesc.getText().toString();
    }

    private class UiCallback implements Callback<ImageResponse> {

        @Override
        public void success(ImageResponse imageResponse, Response response){}

        @Override
        public void failure(RetrofitError error) {
            //Assume we have no connection, since error is null
            if (error == null) {
                Snackbar.make(findViewById(R.id.rootView), "No internet connection", Snackbar.LENGTH_SHORT).show();
            }
        }
    }
    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        photoPath = "file:" + image.getAbsolutePath();
        return image;
    }

}
