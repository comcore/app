package com.gmail.comcorecrew.comcore.fragments;

import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.navigation.NavArgs;
import com.gmail.comcorecrew.comcore.server.id.ModuleID;
import java.io.Serializable;
import java.lang.IllegalArgumentException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.HashMap;

public class PollItemFragmentArgs implements NavArgs {
  private final HashMap arguments = new HashMap();

  private PollItemFragmentArgs() {
  }

  private PollItemFragmentArgs(HashMap argumentsMap) {
    this.arguments.putAll(argumentsMap);
  }

  @NonNull
  @SuppressWarnings("unchecked")
  public static PollItemFragmentArgs fromBundle(@NonNull Bundle bundle) {
    PollItemFragmentArgs __result = new PollItemFragmentArgs();
    bundle.setClassLoader(PollItemFragmentArgs.class.getClassLoader());
    if (bundle.containsKey("parent_polling")) {
      ModuleID parentPolling;
      if (Parcelable.class.isAssignableFrom(ModuleID.class) || Serializable.class.isAssignableFrom(ModuleID.class)) {
        parentPolling = (ModuleID) bundle.get("parent_polling");
      } else {
        throw new UnsupportedOperationException(ModuleID.class.getName() + " must implement Parcelable or Serializable or must be an Enum.");
      }
      if (parentPolling == null) {
        throw new IllegalArgumentException("Argument \"parent_polling\" is marked as non-null but was passed a null value.");
      }
      __result.arguments.put("parent_polling", parentPolling);
    } else {
      throw new IllegalArgumentException("Required argument \"parent_polling\" is missing and does not have an android:defaultValue");
    }
    return __result;
  }

  @SuppressWarnings("unchecked")
  @NonNull
  public ModuleID getParentPolling() {
    return (ModuleID) arguments.get("parent_polling");
  }

  @SuppressWarnings("unchecked")
  @NonNull
  public Bundle toBundle() {
    Bundle __result = new Bundle();
    if (arguments.containsKey("parent_polling")) {
      ModuleID parentPolling = (ModuleID) arguments.get("parent_polling");
      if (Parcelable.class.isAssignableFrom(ModuleID.class) || parentPolling == null) {
        __result.putParcelable("parent_polling", Parcelable.class.cast(parentPolling));
      } else if (Serializable.class.isAssignableFrom(ModuleID.class)) {
        __result.putSerializable("parent_polling", Serializable.class.cast(parentPolling));
      } else {
        throw new UnsupportedOperationException(ModuleID.class.getName() + " must implement Parcelable or Serializable or must be an Enum.");
      }
    }
    return __result;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
        return true;
    }
    if (object == null || getClass() != object.getClass()) {
        return false;
    }
    PollItemFragmentArgs that = (PollItemFragmentArgs) object;
    if (arguments.containsKey("parent_polling") != that.arguments.containsKey("parent_polling")) {
      return false;
    }
    if (getParentPolling() != null ? !getParentPolling().equals(that.getParentPolling()) : that.getParentPolling() != null) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + (getParentPolling() != null ? getParentPolling().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "PollItemFragmentArgs{"
        + "parentPolling=" + getParentPolling()
        + "}";
  }

  public static class Builder {
    private final HashMap arguments = new HashMap();

    public Builder(PollItemFragmentArgs original) {
      this.arguments.putAll(original.arguments);
    }

    public Builder(@NonNull ModuleID parentPolling) {
      if (parentPolling == null) {
        throw new IllegalArgumentException("Argument \"parent_polling\" is marked as non-null but was passed a null value.");
      }
      this.arguments.put("parent_polling", parentPolling);
    }

    @NonNull
    public PollItemFragmentArgs build() {
      PollItemFragmentArgs result = new PollItemFragmentArgs(arguments);
      return result;
    }

    @NonNull
    public Builder setParentPolling(@NonNull ModuleID parentPolling) {
      if (parentPolling == null) {
        throw new IllegalArgumentException("Argument \"parent_polling\" is marked as non-null but was passed a null value.");
      }
      this.arguments.put("parent_polling", parentPolling);
      return this;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public ModuleID getParentPolling() {
      return (ModuleID) arguments.get("parent_polling");
    }
  }
}
