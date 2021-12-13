package com.example.musicupload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musicupload.Model.Constants;
import com.example.musicupload.Model.GetAlbum;
import com.example.musicupload.Model.Upload;
import com.example.musicupload.Model.UploadSong;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Uri fileFath;
    private static final int PICK_IMAGE_REQUEST = 234;
    TextView textViewImage;
    ProgressBar progressBar;
    Uri audioUri;
    StorageReference mStorageref;
    StorageTask mUploadTask;
    private DatabaseReference referenceSongs;
    String songsCategory;
    MediaMetadataRetriever mediaMetadataRetriever;
    byte[] art;
    String title1,artist1,album_art1 = "",durations1,mKey;
    TextView title,artist, album, duration,dataa;
    ImageView album_art;
    Button chooseImageAt,buttonUpload;
    StorageReference storageReference;

    private DatabaseReference mDatabase;
    private static List <String> categories = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewImage= findViewById(R.id.tv_songfile);
        progressBar = findViewById(R.id.progressbar);
        title = findViewById(R.id.tv_title);
        artist = findViewById(R.id.tv_artist);
        duration = findViewById(R.id.tv_duration);
        album = findViewById(R.id.tv_album);
        dataa = findViewById(R.id.tv_data);
        album_art = findViewById(R.id.imv_imgSong);



        mediaMetadataRetriever = new MediaMetadataRetriever();
        referenceSongs = FirebaseDatabase.getInstance("https://musicupload-7dde0-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("songs");
        mStorageref = FirebaseStorage.getInstance().getReference().child("songs");
        storageReference = FirebaseStorage.getInstance().getReference();

        Spinner spinner = findViewById(R.id.spinner);

        spinner.setOnItemSelectedListener(this);



       // List <String> categories = new ArrayList<>();

        mDatabase = FirebaseDatabase.getInstance("https://musicupload-7dde0-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("uploads");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categories.clear();
                for(DataSnapshot postsSnapshot:snapshot.getChildren()){
                    GetAlbum upload = postsSnapshot.getValue(GetAlbum.class);
                    categories.add(upload.getName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        categories.add("None");


        ArrayAdapter <String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);


    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        songsCategory = parent.getItemAtPosition(position).toString();
        Toast.makeText(this, "Selected: "+songsCategory,Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    public void openAudioFiles( View v){
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("audio/*");
        startActivityForResult(i,101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            fileFath = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),fileFath);

                album_art.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        if(requestCode == 101 && resultCode == RESULT_OK && data.getData() != null ){
            audioUri = data.getData();
            String fileNames = getFileName(audioUri);
            textViewImage.setText(fileNames);
            mediaMetadataRetriever.setDataSource(this, audioUri);

            art = mediaMetadataRetriever.getEmbeddedPicture();
            Bitmap bitmap = BitmapFactory.decodeByteArray(art, 0,art.length);
            album_art.setImageBitmap(bitmap);
            album.setText(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
            artist.setText(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            dataa.setText(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE));
            duration.setText(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            title.setText(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));


            artist1 = mediaMetadataRetriever.extractMetadata((MediaMetadataRetriever.METADATA_KEY_ARTIST));
            title1 = mediaMetadataRetriever.extractMetadata((MediaMetadataRetriever.METADATA_KEY_TITLE));
            durations1 = mediaMetadataRetriever.extractMetadata((MediaMetadataRetriever.METADATA_KEY_DURATION));

        }
    }
    @SuppressLint("Range")
    private String getFileName(Uri uri){
        String result = null;
        if(uri.getScheme().equals("content")){
            Cursor cursor = getContentResolver().query(uri,null,null,null,null);
            try {
                if(cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
            finally{
                cursor.close();
            }
        }
        if(result == null){
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if(cut != -1){
                result = result.substring(cut + 1);

            }
        }
        return result;
    }
    public void uploadFileTofirebass(View v){
        if(textViewImage.equals("No file Selected")){
            Toast.makeText(this,"please selected an image!", Toast.LENGTH_SHORT).show();

        }
        else{
            if(mUploadTask != null && mUploadTask.isInProgress()){
                Toast.makeText(this, "songs uploads in allready progress",Toast.LENGTH_SHORT).show();
            }
            else{
                uploadFile();
            }

        }
    }


    private void uploadFile() {
        List<UploadSong> Musiz = new ArrayList<>();
        mDatabase = FirebaseDatabase.getInstance("https://musicupload-7dde0-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("songs");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot postsSnapshot:snapshot.getChildren()){
                    UploadSong upload = postsSnapshot.getValue(UploadSong.class);
                    Musiz.add(upload);
                }
                mKey = String.valueOf(Musiz.size() + 1);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if(fileFath != null){

            final StorageReference sRef = storageReference.child(Constants.STORAGE_PATH_UPLOADS
                    + System.currentTimeMillis() + "." + getFileExtension(fileFath));

            sRef.putFile(fileFath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            String url =  uri.toString();
                            album_art1 = url;



                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();

                }
            });
        }

        if(audioUri != null){

            Toast.makeText(this,"Uploads please wait!",Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.VISIBLE);
            final StorageReference storageReference = mStorageref.child(System.currentTimeMillis()+"."+getfileextension(audioUri));
            mUploadTask = storageReference.putFile(audioUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            UploadSong uploadSong = new UploadSong(songsCategory,title1,artist1,album_art1,durations1,uri.toString(),mKey);
                            String uploadId = referenceSongs.push().getKey();
                            referenceSongs.child(uploadId).setValue(uploadSong);



                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {

                    double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    progressBar.setProgress((int) progress);

                }
            });
        }else{
            Toast.makeText(this,"No file Selected to upload",Toast.LENGTH_SHORT).show();
        }
    }
    private  String getfileextension(Uri audioUri){

        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(audioUri));
    }
    public void openAlbumUploadsActivity(View v){
        Intent in = new Intent(MainActivity.this, UploadAlbumActivity.class);
        startActivity(in);
    }

    public void showFileChoose(View v) {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),PICK_IMAGE_REQUEST);
    }
    private  String getFileExtension(Uri uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getMimeTypeFromExtension(cr.getType(uri));
    }

}