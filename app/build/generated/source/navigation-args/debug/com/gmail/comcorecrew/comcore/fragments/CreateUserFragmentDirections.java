package com.gmail.comcorecrew.comcore.fragments;

import androidx.annotation.NonNull;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;
import com.gmail.comcorecrew.comcore.R;

public class CreateUserFragmentDirections {
  private CreateUserFragmentDirections() {
  }

  @NonNull
  public static NavDirections actionCreateUserFragmentToMainFragment() {
    return new ActionOnlyNavDirections(R.id.action_createUserFragment_to_mainFragment);
  }
}
