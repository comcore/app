package com.gmail.comcorecrew.comcore.classes.modules;

import android.annotation.SuppressLint;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.abstracts.CustomChat;
import com.gmail.comcorecrew.comcore.caching.CustomItem;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.id.CustomModuleID;


/**
 * Simple dummy class to show that custom modules work.
 *
 * If the state is 0, the color is blue.
 * If the state is 1, the color is red.
 */
public class DummyButton extends CustomChat {

    public DummyButton(String name, CustomModuleID id, Group group) {
        super(name, id, group);
    }

    public DummyButton(String name, Group group) {
        super(name, group, "dummy");
    }

    @Override
    public void viewInit(@NonNull View view, Fragment current) {
        view.findViewById(R.id.dummy_back_button).setOnClickListener(clickedView -> {
            NavHostFragment.findNavController(current)
                    .popBackStack();
        });

        refreshView(view);
    }

    @Override
    public int getLayout() {
        return R.layout.fragment_custom_button;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void refreshView(View view) {

        if (getState() == 1) {
            view.findViewById(R.id.dummy_button).setBackgroundColor(R.color.blue);
        } else {
            view.findViewById(R.id.dummy_button).setBackgroundColor(R.color.red);
        }
    }

    public int getState() {
        CustomItem lastItem = getLastItem();
        if ((lastItem == null) || (lastItem.getData().equals("1"))) {
            return 1;
        }
        return 0;
    }

    public int press() {
        if (getState() == 0) {
            sendMessage("1");
            return 1;
        }
        sendMessage("0");
        return 0;
    }

    @Override
    public void afterCreate() {
        sendMessage("0");
    }
}
