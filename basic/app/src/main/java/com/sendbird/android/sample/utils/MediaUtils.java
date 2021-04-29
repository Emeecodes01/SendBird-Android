package com.sendbird.android.sample.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.sendbird.android.SendBird;
import com.sendbird.android.sample.BuildConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MediaUtils extends Activity {

    private boolean mRequestingCamera = false;
    private Uri mTempPhotoUri = null;

    public static String useCamera = "camera";

    Intent returnIntent = new Intent();

    private static final int INTENT_REQUEST_CHOOSE_MEDIA = 0xf0;
    private static final int INTENT_REQUEST_CAMERA = 0xf1;
    private static final int INTENT_REQUEST_CAMERA_WITH_FILE_PROVIDER = 0xf2;

    private static final int MEDIA_REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private static final int CAMERA_REQUEST_PERMISSIONS_REQUEST_CODE = 2;

    public static final int MEDIA_REQUEST_CODE = 3;

    private static final String[] MEDIA_MANDATORY_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final String[] CAMERA_MANDATORY_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        Intent intent = getIntent();
        boolean isCamera = intent.getBooleanExtra(useCamera, true);

        if (isCamera) {
            checkPermissions(CAMERA_MANDATORY_PERMISSIONS, CAMERA_REQUEST_PERMISSIONS_REQUEST_CODE);
        } else {
            checkPermissions(MEDIA_MANDATORY_PERMISSIONS, MEDIA_REQUEST_PERMISSIONS_REQUEST_CODE);
        }

        super.onCreate(savedInstanceState);
    }

    private void requestCamera() {
        mRequestingCamera = true;

        try {
//            File imagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File imagePath = this.getExternalFilesDir(null);
            File tempFile = File.createTempFile("SendBird_" + System.currentTimeMillis(), ".jpg", imagePath);

            if (Build.VERSION.SDK_INT >= 24) {
                mTempPhotoUri = FileProvider.getUriForFile(this, "com.ulesson.debug.theprovider", tempFile);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    grantUriPermission(packageName, mTempPhotoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                intent.putExtra(MediaStore.EXTRA_OUTPUT, mTempPhotoUri);
                intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(intent, INTENT_REQUEST_CAMERA_WITH_FILE_PROVIDER);

            } else {
                mTempPhotoUri = Uri.fromFile(tempFile);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mTempPhotoUri);
                startActivityForResult(intent, INTENT_REQUEST_CAMERA);

            }
            SendBird.setAutoBackgroundDetection(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (requestCode == INTENT_REQUEST_CHOOSE_MEDIA && resultCode == Activity.RESULT_OK) {
                // If user has successfully chosen the image, show a dialog to confirm upload.
                if (data == null) {
                    return;
                }

                returnURI(data.getData(), resultCode);

            } else if (requestCode == INTENT_REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {
                if (!mRequestingCamera) {
                    return;
                }

                returnURI(mTempPhotoUri, resultCode);

            } else if (requestCode == INTENT_REQUEST_CAMERA_WITH_FILE_PROVIDER && resultCode == Activity.RESULT_OK) {
                if (!mRequestingCamera) {
                    return;
                }

                returnURI(mTempPhotoUri, resultCode);

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Set this as true to restore background connection management.
            SendBird.setAutoBackgroundDetection(true);
        }

    }

    private void checkPermissions(String[] permissions, int requestCode) {
        ArrayList<String> deniedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permission);
            }
        }

        if (deniedPermissions.size() > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                SendBird.setAutoBackgroundDetection(false);
                requestPermissions(deniedPermissions.toArray(new String[0]), requestCode);
            } else {
                permissionDenied();
            }
        } else {
            if (requestCode == MEDIA_REQUEST_PERMISSIONS_REQUEST_CODE) {
                requestMedia();
            } else if (requestCode == CAMERA_REQUEST_PERMISSIONS_REQUEST_CODE) {
                requestCamera();
            }
        }
    }

    private void returnURI(Uri uri, int resultCode) {
        returnIntent.setData(uri);
        setResult(resultCode, returnIntent);
        finish();
    }

    private void permissionDenied() {
        Toast.makeText(this, "Permission denied.", Toast.LENGTH_LONG).show();
    }

    private void requestMedia() {
        Intent intent = new Intent();
        // Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Image"), INTENT_REQUEST_CHOOSE_MEDIA);

        // Set this as false to maintain connection
        // even when an external Activity is started.
        SendBird.setAutoBackgroundDetection(false);
    }

}
