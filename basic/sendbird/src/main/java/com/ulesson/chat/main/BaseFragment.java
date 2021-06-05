package com.ulesson.chat.main;

import androidx.fragment.app.Fragment;

import com.sendbird.android.SendBird;
import com.sendbird.syncmanager.SendBirdSyncManager;

public class BaseFragment extends Fragment {

    @Override
    public void onResume() {
        super.onResume();

        registerConnectionHandler();
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
    }

}
