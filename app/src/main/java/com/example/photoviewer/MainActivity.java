package com.example.photoviewer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int READ_PERM_REQUEST_CODE = 1;
    private static final int IMAGES_REQUEST_CODE = 2;
    private GridView gridGallery;
    private ImageAdapter imageAdapter;
    boolean permissionGranted = false;
    private ContentObserver imagesObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gridGallery = (GridView) findViewById(R.id.gridGallery);
        imageAdapter = new ImageAdapter(this);
        gridGallery.setAdapter(imageAdapter);
        getPermission();
    }

    private void getPermission() {
        Log.d(TAG, "getPermission READ_EXTERNAL_STORAGE. SDK: " + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                onPermissionResult(null);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        READ_PERM_REQUEST_CODE);
            }
        } else {
            onPermissionResult(null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_PERM_REQUEST_CODE) {
            String genError = "Unknown error while requesting storage read permission";
            if (permissions.length == 0 || permissions.length > 1) {
                onPermissionResult(genError);
                return;
            }
            String perm = permissions[0];
            if (!perm.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                onPermissionResult(genError);
                return;
            }
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                onPermissionResult("Permission denied");
            } else {
                onPermissionResult(null);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (permissionGranted) {
            subscribeForImageChanges();
            updateImages();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unsubscribeForImageChanges();
    }

    private void subscribeForImageChanges() {
//        String imagesPath = Environment.getExternalStorageDirectory().toString() + "/DCIM";
//        Uri imagesUri = Uri.parse(imagesPath);
        Uri imagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        if (imagesUri != null) {
            Log.d(TAG, "Registering content observer for uri: " + imagesUri);
            imagesObserver = new ContentObserver(new Handler(getMainLooper())) {
                private static final String TAG = "ContentObserver";

                @Override
                public void onChange(boolean selfChange) {
                    onChange(selfChange, null);
                }

                @Override
                public void onChange(boolean selfChange, Uri uri) {
                    Log.d(TAG, String.format("onChange. selfChange: %s. uri: %s", selfChange, uri));
                    updateImages();
                }
            };
            getContentResolver().registerContentObserver(imagesUri, true, imagesObserver);
        }
    }

    private void unsubscribeForImageChanges() {
        if (imagesObserver != null) {
            Log.d(TAG, "Unregistering content observer");
            getContentResolver().unregisterContentObserver(imagesObserver);
        }
    }

    public void onPermissionResult(String error) {
        if (error != null) {
            String errStr = "Error: " + error;
            Toast toast = Toast.makeText(this, errStr, Toast.LENGTH_LONG);
            toast.show();
            Log.w(TAG, error);
        } else {
            permissionGranted = true;
            subscribeForImageChanges();
            updateImages();
        }
    }

    private void updateImages() {
        List<String> paths = getCameraImagePahts();
        imageAdapter.setImagePaths(paths);
        imageAdapter.notifyDataSetChanged();
    }

    private List<String> getCameraImagePahts() {
        Log.d(TAG, "getCameraImagePahts");
        final String[] columns = {MediaStore.Images.Media.DATA};
//        final String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
//        String imagesPath = Environment.getExternalStorageDirectory().toString()
//                + "/DCIM";
//        Log.d(TAG, "imagesPath: " + imagesPath);
//        String imagesBucketId = String.valueOf(imagesPath.toLowerCase().hashCode());
//        final String[] selectionArgs = {imagesBucketId};
        String selection = MediaStore.Images.Media.DATA + " LIKE? ";
        String[] selectionArgs = {Environment.getExternalStorageDirectory().toString() + "/DCIM/%"};
        String sortOrder = MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC";
        final Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                columns,
                selection,
                selectionArgs,
                sortOrder);
        ArrayList<String> result = new ArrayList<String>(cursor.getCount());

        if (cursor.moveToFirst()) {
            final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            do {
                final String data = cursor.getString(dataColumn);
//                Log.d(TAG, data);
                result.add(data);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

//    private List<String> getThumbnails() {
//        Log.d(TAG, "getThumbnails");
//        String[] projection = {MediaStore.Images.Thumbnails.DATA};
//        Cursor cursor = this.getContentResolver().query( MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
//                projection, // Which columns to return
//                null,       // Return all rows
//                null,
//                null);
//        int columnIndex = cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA);
//        ArrayList<String> paths = new ArrayList<>(cursor.getCount());
//        for(int i = 0; i < cursor.getCount(); i++){
//            cursor.moveToPosition(i);
//            String path = cursor.getString(columnIndex);
//            paths.add(path);
//            Log.d(TAG, path);
//        }
//        cursor.close();
//        return paths;
//    }
}
