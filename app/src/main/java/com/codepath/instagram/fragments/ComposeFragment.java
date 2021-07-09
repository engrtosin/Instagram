package com.codepath.instagram.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.codepath.instagram.BitmapScaler;
import com.codepath.instagram.FeedFragment;
import com.codepath.instagram.R;
import com.codepath.instagram.activities.ComposePostActivity;
import com.codepath.instagram.databinding.ActivityComposePostBinding;
import com.codepath.instagram.databinding.FragmentCommentsBinding;
import com.codepath.instagram.databinding.FragmentComposeBinding;
import com.codepath.instagram.models.Post;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ComposeFragment extends FeedFragment {

    public static final String TAG = "ComposeFragment";
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
    private static final int TAKEN_PHOTO_WIDTH = 100;
    private static final int PICK_PHOTO_CODE = 100;
    protected File photoFile;
    public String photoFileName = "photo.jpg";
    Uri fileProvider;

    FragmentComposeBinding binding;

    public ComposeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // fancy_fragment.xml -> FancyFragmentBinding
        binding = FragmentComposeBinding.inflate(getLayoutInflater(), container, false);

        // layout of fragment is stored in a special property called root
        View view = binding.getRoot();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        photoFile = getPhotoFileUri(photoFileName);
        fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider", photoFile);

        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"wants to submit");
                String description = binding.etDescription.getText().toString();
                if (description.isEmpty()) {
                    Toast.makeText(getContext(), "Description cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (photoFile == null || binding.ivPostPhoto.getDrawable() == null) {
                    Toast.makeText(getContext(), "Image cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
                ParseUser currentUser = ParseUser.getCurrentUser();
                try {
                    savePost(description,currentUser,photoFile);
                } catch (ParseException e) {
                    Log.e(TAG,"could not save new post " + e.getMessage(),e);
                }
                binding.pbLoading.setVisibility(View.INVISIBLE);
                listener.goToFragment(new PostsFragment(), null);
            }
        });
//        Button btnTakePicture = (Button) view.findViewById(R.id.btnTakePicture);
        binding.btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"onCLicked");
                launchCamera();
            }
        });
        binding.ivAttachFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"Attach file for new post");
                onPickPhoto(v);
            }
        });
    }

    public void onPickPhoto(View view) {
        Log.i(TAG,"on pick photo");
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Bring up gallery to select a photo
            Log.i(TAG,"Intent to media gallery");
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }

    protected void launchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            // check version of Android on device
            if(Build.VERSION.SDK_INT > 27){
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(getActivity().getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == getActivity().RESULT_OK) {
                // RESIZE BITMAP, see section below
                try {
                    resizeBitmap();
                } catch (IOException e) {
                    Log.e(TAG,"Resizing taken photo failed: + " + e, e);
                }
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // Load the taken image into a preview
                binding.ivPostPhoto.setImageBitmap(takenImage);
            } else { // Result was a failure
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
        else if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            Uri photoUri = data.getData();

            // Load the image located at photoUri into selectedImage
            Bitmap selectedImage = loadFromUri(photoUri);

            // Load the selected image into a preview
            binding.ivPostPhoto.setImageBitmap(selectedImage);
        }
    }

    protected void resizeBitmap() throws IOException {
        // See code above
        Uri takenPhotoUri = Uri.fromFile(getPhotoFileUri(photoFileName));
// by this point we have the camera photo on disk
        Bitmap rawTakenImage = BitmapFactory.decodeFile(takenPhotoUri.getPath());
// See BitmapScaler.java: https://gist.github.com/nesquena/3885707fd3773c09f1bb
        Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(rawTakenImage, TAKEN_PHOTO_WIDTH);

        // Configure byte output stream
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
// Compress the image further
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
// Create a new file for the resized bitmap (`getPhotoFileUri` defined above)
        File resizedFile = getPhotoFileUri(photoFileName + "_resized");
        resizedFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(resizedFile);
// Write the bytes of the bitmap to file
        fos.write(bytes.toByteArray());
        fos.close();
    }

    private void savePost(String description, ParseUser currentUser, File photoFile) throws ParseException {
        binding.pbLoading.setVisibility(View.VISIBLE);
        Post post = new Post();
        post.setDescription(description);
        post.setImage(new ParseFile(photoFile));
        post.setUser(currentUser);
        post.setComments(new JSONArray());
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG,"Error while saving post: " + e, e);
                    Toast.makeText(getContext(), "Error while saving post!", Toast.LENGTH_SHORT);
                    return;
                }
                Log.i(TAG,"Post by " + currentUser.getUsername() + " is saved.");
                Toast.makeText(getContext(), "Successful!", Toast.LENGTH_SHORT);
                binding.etDescription.setText("");
                binding.ivPostPhoto.setImageResource(0);
            }
        });
        binding.pbLoading.setVisibility(View.GONE);
    }

    @Override
    public void setListener(FeedFragmentInterface listener) {
        this.listener = listener;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}