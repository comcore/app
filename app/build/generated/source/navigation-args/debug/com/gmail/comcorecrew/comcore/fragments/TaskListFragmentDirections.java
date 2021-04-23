package com.gmail.comcorecrew.comcore.fragments;

import androidx.annotation.NonNull;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;
import com.gmail.comcorecrew.comcore.R;

public class TaskListFragmentDirections {
  private TaskListFragmentDirections() {
  }

  @NonNull
  public static NavDirections actionTaskListFragmentToSettingsFragment() {
    return new ActionOnlyNavDirections(R.id.action_taskListFragment_to_settingsFragment);
  }
}
