package com.gmail.comcorecrew.comcore.fragments;

import androidx.annotation.NonNull;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;
import com.gmail.comcorecrew.comcore.R;

public class LoginFragmentDirections {
  private LoginFragmentDirections() {
  }

  @NonNull
  public static NavDirections actionLoginFragmentToMainFragment() {
    return new ActionOnlyNavDirections(R.id.action_loginFragment_to_mainFragment);
  }

  @NonNull
  public static NavDirections actionLoginFragmentToCreateUserFragment() {
    return new ActionOnlyNavDirections(R.id.action_loginFragment_to_createUserFragment);
  }
}
