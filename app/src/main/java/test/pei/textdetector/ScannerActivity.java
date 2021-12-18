package test.pei.textdetector;

import static android.Manifest.permission.CAMERA;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import test.pei.textdetector.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ScannerActivity extends AppCompatActivity {

    private ImageView captureIV;
    private TextView resultTV;
    private Button snapBtn, detectBtn;
    private Bitmap imageBitmap;
    static final int REQUEST_IMAGE_CAPTURE = 1;


    // variables for our image view, image bitmap,
    // buttons, recycler view, adapter and array list.
    private ImageView img;
    private Button snap, searchResultsBtn;
    private Bitmap imageBitmap1;
    private RecyclerView resultRV;
    private SearchResultsRVAdapter searchResultsRVAdapter;
    private ArrayList<DataModal> dataModalArrayList;
    private String title, link, displayed_link, snippet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        captureIV = findViewById(R.id.idIVCaptureImage);
        resultTV = findViewById(R.id.idTVDetectedText);
        snapBtn = findViewById(R.id.idBtnSnap);
        detectBtn = findViewById(R.id.idBtnDetect);

        // Google lens variables
        img = captureIV;
        snap = snapBtn;
        searchResultsBtn = detectBtn;
        resultRV = findViewById(R.id.idRVSearchResults);

        detectBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                detectText();
            }
        });

        snapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissions()) {
                    captureImage();
                } else {
                    requestPermission();
                }
            }
        });

        // initializing our array list
        dataModalArrayList = new ArrayList<>();
        // initializing our adapter class.
        searchResultsRVAdapter = new SearchResultsRVAdapter(dataModalArrayList, ScannerActivity.this);

        // layout manager for our recycler view.
        LinearLayoutManager manager = new LinearLayoutManager(ScannerActivity.this, LinearLayoutManager.HORIZONTAL, false);

        // on below line we are setting layout manager
        // and adapter to our recycler view.
        resultRV.setLayoutManager(manager);
        resultRV.setAdapter(searchResultsRVAdapter);

        // adding on click listener for our snap button.
        snap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        searchResultsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getResults();
            }
        });

    }

    private boolean checkPermissions() {
        int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(),CAMERA);
        return cameraPermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        int PERMISSION_CODE = 200;
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, PERMISSION_CODE);
    }

    private void captureImage() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicture.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            boolean cameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (cameraPermission) {
                Toast.makeText(this, "Permissions Granted..", Toast.LENGTH_SHORT).show();
                captureImage();
            } else {
                Toast.makeText(this, "Permission denied..", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            captureIV.setImageBitmap(imageBitmap);
        }

        // inside on activity result method we are
        // setting our image to our image view from bitmap.
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            // on below line we are setting our
            // bitmap to our image view.
            img.setImageBitmap(imageBitmap);
        }
    }

    private void detectText(){
        InputImage image = InputImage.fromBitmap(imageBitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> result = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(@NonNull Text text) {
                StringBuilder result = new StringBuilder();
                for(Text.TextBlock block : text.getTextBlocks()) {
                    String blockText = block.getText();
                    Point[] blockCornerPoint = block.getCornerPoints();
                    Rect blockFrame = block.getBoundingBox();
                    for (Text.Line line : block.getLines()) {
                        String lineText = line.getText();
                        Point[] lineCornerPoint = line.getCornerPoints();
                        Rect lineRect = line.getBoundingBox();
                        for (Text.Element element : line.getElements()) {
                            String elementText = element.getText();
                            result.append(elementText);
                        }
                        resultTV.setText(blockText);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ScannerActivity.this, "Fail to detect text from image.." + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getResults() {
        // inside the label image method we are calling a firebase vision image
        // and passing our image bitmap to it.
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);

        // on below line we are creating a labeler for our image bitmap and
        // creating a variable for our firebase vision image labeler.
        FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler();

        // calling a method to process an image and adding on success listener method to it.
        labeler.processImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
            @Override
            public void onSuccess(List<FirebaseVisionImageLabel> firebaseVisionImageLabels) {
                String searchQuery = firebaseVisionImageLabels.get(0).getText();
                searchData(searchQuery);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // displaying error message.
                Toast.makeText(ScannerActivity.this, "Fail to detect image..", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchData(String searchQuery) {
        String apiKey = "ff81d03085573a6e1813711e3e76250f84ba2f4a7a9827c7b0e0c6ba83e5b7d3";
        String url = "https://serpapi.com/search.json?q=" + searchQuery.trim() + "&location=Delhi,India&hl=en&gl=us&google_domain=google.com&api_key=" + apiKey;

        // creating a new variable for our request queue
        RequestQueue queue = Volley.newRequestQueue(ScannerActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    // on below line we are extracting data from our json.
                    JSONArray organicResultsArray = response.getJSONArray("organic_results");
                    for (int i = 0; i < organicResultsArray.length(); i++) {
                        JSONObject organicObj = organicResultsArray.getJSONObject(i);
                        if (organicObj.has("title")) {
                            title = organicObj.getString("title");
                        }
                        if (organicObj.has("link")) {
                            link = organicObj.getString("link");
                        }
                        if (organicObj.has("displayed_link")) {
                            displayed_link = organicObj.getString("displayed_link");
                        }
                        if (organicObj.has("snippet")) {
                            snippet = organicObj.getString("snippet");
                        }
                        // on below line we are adding data to our array list.
                        dataModalArrayList.add(new DataModal(title, link, displayed_link, snippet));
                    }
                    // notifying our adapter class
                    // on data change in array list.
                    searchResultsRVAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // displaying error message.
                Toast.makeText(ScannerActivity.this, "No Result found for the search query..", Toast.LENGTH_SHORT).show();
            }
        });
        // adding json object request to our queue.
        queue.add(jsonObjectRequest);
    }

    // method to capture image.
    private void dispatchTakePictureIntent() {
        // inside this method we are calling an implicit intent to capture an image.
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // calling a start activity for result when image is captured.
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


}