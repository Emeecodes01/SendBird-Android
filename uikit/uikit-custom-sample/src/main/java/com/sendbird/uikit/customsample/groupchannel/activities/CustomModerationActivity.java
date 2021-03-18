package com.sendbird.uikit.customsample.groupchannel.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import com.sendbird.uikit.activities.ModerationActivity;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.groupchannel.fragments.CustomModerationFragment;
import com.sendbird.uikit.fragments.ModerationFragment;

public class CustomModerationActivity extends ModerationActivity {
    @Override
    protected ModerationFragment createModerationFragment(@NonNull String channelUrl) {
        return new ModerationFragment.Builder(channelUrl, R.style.SendBird_Custom)
                .setCustomModerationFragment(new CustomModerationFragment())
                .setUseHeader(true)
                .setUseHeaderLeftButton(true)
                .setHeaderTitle(getString(R.string.sb_text_channel_settings_moderations))
                .setHeaderLeftButtonIcon(R.drawable.icon_arrow_left, AppCompatResources.getColorStateList(this, R.color.ondark_01))
                .setHeaderLeftButtonListener(null)
                .setOnMenuItemClickListener(null)
                .build();
    }
}
