package ca.cmpt276.charcoal.practicalparent;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

import ca.cmpt276.charcoal.practicalparent.model.Child;
import ca.cmpt276.charcoal.practicalparent.model.ChildManager;
import ca.cmpt276.charcoal.practicalparent.model.TasksManager;

import static android.os.Environment.getExternalStoragePublicDirectory;

/**
 *  Sets up Edit Child Activity, Allows for Editing children, and saving data
 */
public class EditChildActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "SavedData";
    private static final String CHILDREN_PREFS = "My children";
    public static final String EXTRA_CHILD_INDEX = "ca.cmpt276.charcoal.practicalparent - childIndex";
    private final int STORAGE_REQUEST_CODE = 0;
    private final String[] STORAGE_PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int RESULT_LOAD_IMAGE = 2;
    private String currentPhotoPath = null;
    public ImageView childPhoto;
    private int childIndex;
    private EditText nameBox;
    private final ChildManager childManager = ChildManager.getInstance();
    private final TasksManager tasksManager = TasksManager.getInstance();

    public static Intent makeLaunchIntent(Context context, int childIndex) {
        Intent intent = new Intent(context, EditChildActivity.class);
        intent.putExtra(EXTRA_CHILD_INDEX, childIndex);
        return intent;
    }

    private void extractIntentData() {
        Intent intent = getIntent();
        childIndex = intent.getIntExtra(EXTRA_CHILD_INDEX, -1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_child);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        setupSaveButton();

        extractIntentData();
        preFillNameBox();
        preLoadChildImage();
        setupImportImageButton();
        setupCameraImageButton();
    }

    private void preFillNameBox() {
        nameBox = findViewById(R.id.edit_child_name);
        if (childIndex >= 0) {
            Child currentChild = childManager.getChild(childIndex);
            nameBox.setText(currentChild.getName());
        }
    }

    private void preLoadChildImage() {
        childPhoto = findViewById(R.id.image_child);
        if (childIndex >= 0) {
            Child currentChild = childManager.getChild(childIndex);
            childPhoto.setImageBitmap(currentChild.getChildImage(this));
        } else {
            childPhoto.setImageResource(R.drawable.editchild_default_image);
        }
    }

    private void setupImportImageButton() {
        Button importImageButton = findViewById(R.id.button_image_import);
        importImageButton.setOnClickListener(v -> dispatchChooseImportIntent());
    }

    // Reference:
    //   https://www.youtube.com/watch?v=8nDKwtTcOUg
    private void dispatchChooseImportIntent() {
        Intent choosePictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(choosePictureIntent, RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            galleryAddPic();
            File img = new File(currentPhotoPath);
            if (img.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                childPhoto.setImageBitmap(bitmap);
                copyPictureToPrivate(bitmap);
            }
        } else if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            childPhoto.setImageURI(selectedImage);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                copyPictureToPrivate(bitmap);
            } catch (IOException e) {
                Toast.makeText(this, R.string.error_could_not_save_file, Toast.LENGTH_SHORT).show();
            }
        }
    }

    void copyPictureToPrivate(Bitmap image) {
        try {
            File privateFile = createPrivateImageFile();
            OutputStream out = new FileOutputStream(privateFile);
            image.compress(Bitmap.CompressFormat.WEBP, 10, out);
            currentPhotoPath = privateFile.getAbsolutePath();
        } catch (IOException e) {
            Toast.makeText(this, R.string.error_could_not_save_file, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupCameraImageButton() {
        Button editImageCameraButton = findViewById(R.id.button_get_camera_image);
        editImageCameraButton.setOnClickListener(v -> checkFileAccessPermission());
    }

    private void setupSaveButton() {
        Button saveBtn = findViewById(R.id.button_save_child);
        saveBtn.setOnClickListener(v -> saveChild());
    }

    private void saveChild() {
        String childName = nameBox.getText().toString();
        if (nameIsValid(childName)) {
            if (childIndex >= 0) {
                Child currentChild = childManager.getChild(childIndex);
                currentChild.setName(childName);
                if (currentPhotoPath != null) {
                    currentChild.updateChildImage(currentPhotoPath);
                }
            } else {
                childManager.add(new Child(childName, currentPhotoPath));
            }
            saveChildren();
            finish();
        }
    }

    private boolean nameIsValid(String childName) {
        if (childName.length() == 0) {
            nameBox.setError(getString(R.string.edit_child_error_name));
            return false;
        } else {
            for(int i = 0; i < childManager.getChildren().size(); i++){
                if(childName.equals(childManager.getChild(i).getName()) && i != childIndex){
                    nameBox.setError("Children names must be unique");
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_child, menu);
        if (childIndex == -1) {
            menu.findItem(R.id.action_delete)
                    .setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            childManager.remove(childIndex);
            saveChildren();
            reassignTaskForDeletedChild(childIndex);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void reassignTaskForDeletedChild(int deletedChildIndex) {
        tasksManager.reassignTaskForDeletedChild(deletedChildIndex);
        EditTaskActivity.saveTasksInSharedPrefs(this);
    }

    private void saveChildren() {
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        List<Child> children = childManager.getChildren();
        Gson gson = new Gson();
        String json = gson.toJson(children);
        editor.putString(CHILDREN_PREFS, json);
        editor.apply();
    }

    // Reference - Gson serialization code found here:
    //   https://stackoverflow.com/questions/28107647/how-to-save-listobject-to-sharedpreferences/28107838
    public static List<Child> getSavedChildren(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        List<Child> children;
        String serializedChildren = prefs.getString(CHILDREN_PREFS, null);
        if (serializedChildren != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Child>>(){}.getType();
            children = gson.fromJson(serializedChildren, type);
            return children;
        } else {
            return null;
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create the File where the photo should go
        File photoFile = null;
        try {
            photoFile = createPublicImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
            Toast.makeText(this, R.string.error_image_save_problem, Toast.LENGTH_SHORT).show();
        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this, getString(R.string.file_provider), photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void checkFileAccessPermission() {
        if (ContextCompat.checkSelfPermission(EditChildActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            dispatchTakePictureIntent();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected. In this UI,
            // include a "cancel" or "no thanks" button that allows the user to
            // continue using your app without granting the permission.
            View myLayout = findViewById(R.id.coordinator_layout);
            Snackbar.make(myLayout, R.string.msg_storage_permission_explanation, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.action_save, v -> ActivityCompat.requestPermissions(EditChildActivity.this,
                            STORAGE_PERMISSIONS, STORAGE_REQUEST_CODE))
                    .show();
        } else {
            ActivityCompat.requestPermissions(EditChildActivity.this,STORAGE_PERMISSIONS, STORAGE_REQUEST_CODE);
        }
    }

    private File createPublicImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",   /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private File createPrivateImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",   /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == STORAGE_REQUEST_CODE) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(EditChildActivity.this, R.string.error_cannot_take_picture, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
