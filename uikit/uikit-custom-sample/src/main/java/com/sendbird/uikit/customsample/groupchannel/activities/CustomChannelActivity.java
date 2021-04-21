package com.sendbird.uikit.customsample.groupchannel.activities;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;

import com.sendbird.android.MessageListParams;
import com.sendbird.uikit.activities.ChannelActivity;
import com.sendbird.uikit.consts.KeyboardDisplayType;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.groupchannel.activities.adapters.CustomMessageListAdapter;
import com.sendbird.uikit.customsample.groupchannel.fragments.CustomChannelFragment;
import com.sendbird.uikit.customsample.models.CustomMessageType;
import com.sendbird.uikit.fragments.ChannelFragment;

public class CustomChannelActivity extends ChannelActivity {
    private final CustomChannelFragment customChannelFragment = new CustomChannelFragment();

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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick message type")
                .setMultiChoiceItems(new String[]{com.sendbird.uikit.customsample.consts.StringSet.highlight},
                        new boolean[]{customChannelFragment.getCustomMessageType().equals(CustomMessageType.HIGHLIGHT)},
                        (dialog, which, isChecked) -> {
                            final CustomMessageType type = isChecked ? CustomMessageType.HIGHLIGHT : CustomMessageType.NONE;
                            customChannelFragment.setCustomMessageType(type);
                        })
                .create()
                .show();
    }
}
