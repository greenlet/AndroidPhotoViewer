package com.example.photoviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ImageAdapter extends BaseAdapter {
    private final static String TAG = "ImageAdapter";

    private Context context;
    private List<String> imagePaths;

    public ImageAdapter(Context context) {
        this.context = context;
        this.imagePaths = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return imagePaths.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
//        Log.d(TAG, "getView: " + i);
        String imagePath = imagePaths.get(i);
        File imageFile = new File(imagePath);
        if(imageFile.exists()){
//            Log.d(TAG, "" + imageFile.toURI());
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.image_preview, null);
            }
            ImageView imagePreview = view.findViewById(R.id.imagePreview);
            TextView textTitle = view.findViewById(R.id.textTitle);
//            TextView textSubtitle = view.findViewById(R.id.textSubTitle);

//            // Nonresponsive UI
//            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
//            bitmap = ThumbnailUtils.extractThumbnail(bitmap, 200, 200);
//            imagePreview.setImageBitmap(bitmap);

            Picasso.with(context)
                    .load(imageFile)
                    .resize(200, 200)
                    .centerInside()
                    .placeholder(R.drawable.ic_file_image)
                    .into(imagePreview);
            double size = ((double)imageFile.length())/(1024*1024);
            textTitle.setText(String.format("%s (%.1f MB)", imageFile.getName(), size));
        } else {
            view = null;
        }
//        Log.d(TAG, "view: " + view);
        return view;
    }

    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths;
    }
}
