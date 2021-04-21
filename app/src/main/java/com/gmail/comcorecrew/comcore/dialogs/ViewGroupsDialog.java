package com.gmail.comcorecrew.comcore.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.fragments.MainFragment;

public class ViewGroupsDialog extends DialogFragment {
    private final MainFragment fragment;

    public ViewGroupsDialog(MainFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        /** ViewGroupsDialog uses the same layout as ViewMembersDialog **/
        View rootView = inflater.inflate(R.layout.dialog_with_title, container, false);

        // Create the RecyclerView
        RecyclerView rvGroups = (RecyclerView) rootView.findViewById(R.id.dialog_with_title_recycler);
        rvGroups.setLayoutManager(new LinearLayoutManager(getActivity()));
        CustomAdapter adapter = new CustomAdapter();
        rvGroups.setAdapter(adapter);
        rvGroups.setItemAnimator(new DefaultItemAnimator());

        return rootView;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView labelViewGroups = view.findViewById(R.id.label_dialog_with_title);
        labelViewGroups.setText(R.string.pin_group_desc);

        /*
          If the "back" button is clicked, close the dialog box.
          ViewGroupsDialog uses the same layout and buttons as ViewMembersDialog.
         */
        view.findViewById(R.id.dialog_with_title_back_button).setOnClickListener(clickedView -> {
            this.dismiss();
        });

    }


    /** The CustomAdapter internal class sets up the RecyclerView, which displays
     * the list of members in the GUI
     *
     */
    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final TextView textView;
            private Group currentGroup;

            public ViewHolder(View view) {
                super(view);
                view.setOnClickListener(this);

                textView = (TextView) view.findViewById(R.id.module_row_text);
            }

            public TextView getTextView() {
                return textView;
            }

            public void setCurrentGroup(Group currentGroup) {
                this.currentGroup = currentGroup;
            }

            @Override
            public void onClick(View view) {
                AppData.getGroup(currentGroup.getIndex()).setPinned(!currentGroup.isPinned());
                fragment.refresh();
                dismiss();
            }
        }

        /**
         * Initialize the dataset of the Adapter.
         */
        public CustomAdapter() {

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.module_row_item, viewGroup, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CustomAdapter.ViewHolder viewHolder, final int position) {

            TextView nameText = viewHolder.itemView.findViewById(R.id.module_row_text);
            nameText.setText(AppData.getFromPos(position).getDisplayName());
            viewHolder.setCurrentGroup(AppData.getFromPos(position));
        }

        @Override
        public int getItemCount() {
            return AppData.getGroupSize();
        }
    }

}