package com.clarifai.androidstarter;

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

import com.clarifai.api.ClarifaiClient;
import com.clarifai.api.RecognitionRequest;
import com.clarifai.api.RecognitionResult;
import com.clarifai.api.Tag;
import com.clarifai.api.exception.ClarifaiException;

import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.*;
import java.io.*;


//import retrofit2.Call;
//import retrofit2.converter.gson.GsonConverterFactory;
//import retrofit2.Retrofit;
//import retrofit2.http.GET;
//import retrofit2.http.Path;

import static android.provider.MediaStore.Images.Media;

/**
 * A simple Activity that performs recognition using the Clarifai API.
 */
public class RecognitionActivity extends Activity {
  private static final String TAG = RecognitionActivity.class.getSimpleName();

  private static final int CODE_PICK = 1;
  private static final int REQUEST_IMAGE_CAPTURE = 2;
  public static final int MEDIA_TYPE_IMAGE = 1;
  public static final int MEDIA_TYPE_VIDEO = 2;
  private String photoPath;
  private final ClarifaiClient client = new ClarifaiClient(Credentials.CLIENT_ID, Credentials.CLIENT_SECRET);
  private Button selectButton;
  private Button cameraButton;
  private ImageView imageView;
  private TextView textView;
  private Uri fileUri;
    public static final MediaType MEDIA_TYPE_MARKDOWN
            = MediaType.parse("text/x-markdown; charset=utf-8");

    private final OkHttpClient okclient = new OkHttpClient();


    @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_recognition);
    imageView = (ImageView) findViewById(R.id.image_view);
    textView = (TextView) findViewById(R.id.text_view);
    selectButton = (Button) findViewById(R.id.select_button);
    cameraButton = (Button) findViewById(R.id.camera_button);
    selectButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // Send an intent to launch the media picker.
        final Intent intent = new Intent(Intent.ACTION_PICK, Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, CODE_PICK);
      }
    });

    cameraButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
          File photoFile = null;
          try {
            photoFile = createImageFile();
          } catch (IOException ex) {
            // do nothing
          }
          // Continue only if the File was successfully created
          if (photoFile != null) {
            fileUri = Uri.fromFile(photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                    fileUri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
          }
        }
      }
    });
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

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);
    if (requestCode == CODE_PICK && resultCode == RESULT_OK) {
      // The user picked an image. Send it to Clarifai for recognition.
      Log.d(TAG, "User picked image: " + intent.getData());
      Bitmap bitmap = loadBitmapFromUri(intent.getData());
      if (bitmap != null) {
        imageView.setImageBitmap(bitmap);
        textView.setText("Recognizing...");
        selectButton.setEnabled(false);

        // Run recognition on a background thread since it makes a network call.
        new AsyncTask<Bitmap, Void, RecognitionResult>() {
          @Override
          protected RecognitionResult doInBackground(Bitmap... bitmaps) {
            return recognizeBitmap(bitmaps[0]);
          }

          @Override
          protected void onPostExecute(RecognitionResult result) {
            updateUIForResult(result);
          }
        }.execute(bitmap);
      } else {
        textView.setText("Unable to load selected image.");
      }
    }
    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
      // user captured an image
      Log.d(TAG, "User captured image: " + intent.getData());
      Bitmap bitmap = loadBitmapFromUri(fileUri);
      if (bitmap != null) {
        imageView.setImageBitmap(bitmap);
        textView.setText("Recognizing...");
        selectButton.setEnabled(false);

        // Run recognition on a background thread since it makes a network call.
        new AsyncTask<Bitmap, Void, RecognitionResult>() {
          @Override
          protected RecognitionResult doInBackground(Bitmap... bitmaps) {
            return recognizeBitmap(bitmaps[0]);
          }

          @Override
          protected void onPostExecute(RecognitionResult result) {
            updateUIForResult(result);
          }
        }.execute(bitmap);

      } else {
        textView.setText("Unable to load image.");
      }
    }
  }

  /**
   * Loads a Bitmap from a content URI returned by the media picker.
   */
  private Bitmap loadBitmapFromUri(Uri uri) {
    try {
      // The image may be large. Load an image that is sized for display. This follows best
      // practices from http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
      BitmapFactory.Options opts = new BitmapFactory.Options();
      opts.inJustDecodeBounds = true;
      BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, opts);
      int sampleSize = 1;
      while (opts.outWidth / (2 * sampleSize) >= imageView.getWidth() &&
              opts.outHeight / (2 * sampleSize) >= imageView.getHeight()) {
        sampleSize *= 2;
      }

      opts = new BitmapFactory.Options();
      opts.inSampleSize = sampleSize;
      return BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, opts);
    } catch (IOException e) {
      Log.e(TAG, "Error loading image: " + uri, e);
    }
    return null;
  }

  /**
   * Sends the given bitmap to Clarifai for recognition and returns the result.
   */
  private RecognitionResult recognizeBitmap(Bitmap bitmap) {
    try {
      // Scale down the image. This step is optional. However, sending large images over the
      // network is slow and does not significantly improve recognition performance.
      Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 320,
              320 * bitmap.getHeight() / bitmap.getWidth(), true);

      // Compress the image as a JPEG.
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      scaled.compress(Bitmap.CompressFormat.JPEG, 90, out);
      byte[] jpeg = out.toByteArray();

      // Send the JPEG to Clarifai and return the result.
      return client.recognize(new RecognitionRequest(jpeg)).get(0);
    } catch (ClarifaiException e) {
      Log.e(TAG, "Clarifai error", e);
      return null;
    }
  }

  /**
   * Updates the UI by displaying tags for the given result.
   */
  private void updateUIForResult(RecognitionResult result) {
    if (result != null) {
      if (result.getStatusCode() == RecognitionResult.StatusCode.OK) {
        // Display the list of tags in the UI.
        StringBuilder b = new StringBuilder();
        for (Tag tag : result.getTags()) {
          b.append(b.length() > 0 ? ", " : "").append(tag.getName());
        }
        textView.setText("Tags:\n" + b);
      } else {
        Log.e(TAG, "Clarifai: " + result.getStatusMessage());
        textView.setText("Sorry, there was an error recognizing your image.");
      }
    } else {
      textView.setText("Sorry, there was an error recognizing your image.");
    }
    selectButton.setEnabled(true);
  }
}