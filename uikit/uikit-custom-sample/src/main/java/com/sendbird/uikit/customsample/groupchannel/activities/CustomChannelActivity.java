package com.sendbird.uikit.customsample.groupchannel.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sendbird.android.MessageListParams;
import com.sendbird.android.SendBird;
import com.sendbird.uikit.activities.ChannelActivity;
import com.sendbird.uikit.consts.KeyboardDisplayType;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.groupchannel.activities.adapters.CustomMessageListAdapter;
import com.sendbird.uikit.customsample.groupchannel.fragments.CustomChannelFragment;
import com.sendbird.uikit.customsample.models.CustomMessageType;
import com.sendbird.uikit.customsample.utils.PreferenceUtils;
import com.sendbird.uikit.fragments.ChannelFragment;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.DialogListItem;
import com.sendbird.uikit.utils.ContextUtils;
import com.sendbird.uikit.utils.DialogUtils;
import com.sendbird.uikit.utils.FileUtils;
import com.sendbird.uikit.utils.IntentUtils;
import com.sendbird.uikit.utils.PermissionUtils;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class CustomChannelActivity extends ChannelActivity {
    private final CustomChannelFragment customChannelFragment = new CustomChannelFragment();

    private final String[] REQUIRED_PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

    private Uri mediaUri;
    private File mediaFile;

    private static final int STORAGE_PERMISSIONS_REQUEST_CODE = 1001;
    private static final int PERMISSION_SETTINGS_REQUEST_ID = 2000;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 2001;
    private static final int PICK_IMAGE_ACTIVITY_REQUEST_CODE = 2002;

    @Override
    protected ChannelFragment createChannelFragment(@NonNull String channelUrl) {
        final boolean useMessageGroupUI = false;
        return new ChannelFragment.Builder(channelUrl, R.style.CustomMessageListStyle)
                .setCustomChannelFragment(customChannelFragment)
                .setUseHeader(true)
                .setUseUserProfile(true)
                .setHeaderTitle("Tamilore")
                .setUseHeaderLeftButton(true)
                .setUseHeaderRightButton(true)
                .setUseTypingIndicator(true)
                .setHeaderLeftButtonIcon(R.drawable.ic_back_arrow, null)
                .setHeaderRightButtonIcon(R.drawable.ic_clock, null)
                .setInputLeftButtonIcon(R.drawable.ic_attach, null)
                .setInputRightButtonIcon(R.drawable.icon_send, AppCompatResources.getColorStateList(this, R.color.ondark_01))
                .setInputHint(getString(R.string.write_a_reply))
                .setHeaderLeftButtonListener(null)
                .setHeaderRightButtonListener(v -> showCustomChannelSettingsActivity(channelUrl))
                .setMessageListAdapter(new CustomMessageListAdapter(true))
                .setItemClickListener(null)
                .setItemLongClickListener(null)
                .setInputLeftButtonListener(v -> showMessageTypeDialog())
                .setMessageListParams(null)
                .setUseMessageGroupUI(true)
                .build();
    }

    private void showCustomChannelSettingsActivity(String channelUrl) {
        Intent intent = CustomChannelSettingsActivity.newIntentFromCustomActivity(CustomChannelActivity.this, CustomChannelSettingsActivity.class, channelUrl);
        startActivity(intent);
    }

    private void showMessageTypeDialog() {

        final boolean hasPermission = PermissionUtils.hasPermissions(this, REQUIRED_PERMISSIONS);
        if (hasPermission) {
            showMediaSelectDialog();
            return;
        }

        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, STORAGE_PERMISSIONS_REQUEST_CODE);
    }

    private void showMediaSelectDialog() {
        DialogListItem delete = new DialogListItem(R.string.text_remove_photo, 0, true);
        DialogListItem camera = new DialogListItem(com.sendbird.uikit.R.string.sb_text_channel_settings_change_channel_image_camera);
        DialogListItem gallery = new DialogListItem(com.sendbird.uikit.R.string.sb_text_channel_settings_change_channel_image_gallery);
        DialogListItem[] items;
        if (mediaFile == null) {
            items = new DialogListItem[]{camera, gallery};
        } else {
            items = new DialogListItem[]{delete, camera, gallery};
        }

        DialogUtils.buildItemsBottom(items, (view, position, key) -> {
            try {
                SendBird.setAutoBackgroundDetection(false);
                if (key == com.sendbird.uikit.R.string.sb_text_channel_settings_change_channel_image_camera) {
                    takeCamera();
                } else if (key == com.sendbird.uikit.R.string.sb_text_channel_settings_change_channel_image_gallery) {
                    pickImage();
                } else {
                    removeFile();
                }
            } catch (Exception e) {
                Logger.e(e);
            }
        }).showSingle(getSupportFragmentManager());
    }

    private void takeCamera() {
        this.mediaUri = FileUtils.createPictureImageUri(this);
        Intent intent = IntentUtils.getCameraIntent(this, mediaUri);
        if (IntentUtils.hasIntent(this, intent)) {
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    private void pickImage() {
        Intent intent = IntentUtils.getGalleryIntent();
        startActivityForResult(intent, PICK_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    private void removeFile() {
        this.mediaFile = null;
        this.mediaUri = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SendBird.setAutoBackgroundDetection(true);

        if (resultCode != RESULT_OK) return;

        if (requestCode == PERMISSION_SETTINGS_REQUEST_ID) {
            final boolean hasPermission = PermissionUtils.hasPermissions(this, REQUIRED_PERMISSIONS);
            if (hasPermission) {
                showMediaSelectDialog();
            }
            return;
        }

        switch (requestCode) {
            case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
                break;
            case PICK_IMAGE_ACTIVITY_REQUEST_CODE:
                if (data != null) {
                    this.mediaUri = data.getData();
                }
                break;
        }

        if (this.mediaUri != null) {
            mediaFile = FileUtils.uriToFile(getApplicationContext(), mediaUri);
//            updateChannelCover();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSIONS_REQUEST_CODE && grantResults.length == REQUIRED_PERMISSIONS.length) {
            boolean isAllGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }

            if (isAllGranted) {
                showMediaSelectDialog();
            } else {
                String[] notGranted = PermissionUtils.getNotGrantedPermissions(this, permissions);
                List<String> deniedList = PermissionUtils.getShowRequestPermissionRationale(this, permissions);
                if (deniedList.size() == 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getString(com.sendbird.uikit.R.string.sb_text_dialog_permission_title));
                    builder.setMessage(getPermissionGuildeMessage(this, notGranted[0]));
                    builder.setPositiveButton(com.sendbird.uikit.R.string.sb_text_go_to_settings, (dialogInterface, i) -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        startActivityForResult(intent, PERMISSION_SETTINGS_REQUEST_ID);
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, com.sendbird.uikit.R.color.secondary_300));
                }
            }
        }
    }

    private static String getPermissionGuildeMessage(@NonNull Context context, @NonNull String permission) {
        int textResId;
        if (Manifest.permission.CAMERA.equals(permission)) {
            textResId = com.sendbird.uikit.R.string.sb_text_need_to_allow_permission_camera;
        } else {
            textResId = com.sendbird.uikit.R.string.sb_text_need_to_allow_permission_storage;
        }
        return String.format(Locale.US, context.getString(textResId), ContextUtils.getApplicationName(context));
    }

}
