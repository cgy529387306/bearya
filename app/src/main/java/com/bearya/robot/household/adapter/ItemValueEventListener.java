package com.bearya.robot.household.adapter;

import android.view.View;
import android.widget.TextView;

import com.bearya.robot.household.R;
import com.bearya.robot.household.networkInteraction.BYValueEventListener;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncError;
import com.wilddog.client.ValueEventListener;

/**
 * Created by Qiujz on 2017/12/8.
 */

public class ItemValueEventListener implements BYValueEventListener {
    private View itemView;
    private TextView itemName;

    public ItemValueEventListener(View view, TextView name) {
        itemView = view;
        itemName = name;
    }

    @Override
    public void onStateChange(boolean isOnline) {
        if (isOnline) {
            itemView.setSelected(true);//在线
            if (itemName != null) {
                itemName.setText(R.string.machine_state_inline);
            }
        } else {
            itemView.setSelected(false);//离线
            if (itemName != null) {
                itemName.setText(R.string.machine_state_outline);
            }
        }
    }

    @Override
    public void onDataChange(String data) {

    }
}
