package com.sendbird.main;

import androidx.fragment.app.Fragment;

import com.sendbird.android.SendBird;
import com.sendbird.syncmanager.SendBirdSyncManager;
import com.sendbird.utils.ConnectionEvent;
import com.sendbird.utils.PreferenceUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class BaseFragment extends Fragment {

    @Override
    public void onResume() {
        super.onResume();

        registerConnectionHandler();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    protected String getConnectionHandlerId() {
        return "CONNECTION_HANDLER_GROUP_CHANNEL_LIST";
    }

    private void registerConnectionHandler() {
        SendBird.addConnectionHandler(getConnectionHandlerId(), new SendBird.ConnectionHandler() {
            @Override
            public void onReconnectStarted() {
                SendBirdSyncManager.getInstance().pauseSync();
            }

            @Override
            public void onReconnectSucceeded() {
                SendBirdSyncManager.getInstance().resumeSync();
            }

            @Override
            public void onReconnectFailed() {
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        SendBird.removeConnectionHandler(getConnectionHandlerId());
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConnectionEvent event) {
        if (!event.isConnected() && PreferenceUtils.getConnected()) {
        }
    }
}
