package com.gmail.comcorecrew.comcore.fragments;

import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.navigation.NavDirections;
import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.server.id.ModuleID;
import java.io.Serializable;
import java.lang.IllegalArgumentException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.HashMap;

public class PollingFragmentDirections {
  private PollingFragmentDirections() {
  }

  @NonNull
  public static ActionPollingFragmentToPollItemFragment actionPollingFragmentToPollItemFragment(
      @NonNull ModuleID parentPolling) {
    return new ActionPollingFragmentToPollItemFragment(parentPolling);
  }

  public static class ActionPollingFragmentToPollItemFragment implements NavDirections {
    private final HashMap arguments = new HashMap();

    private ActionPollingFragmentToPollItemFragment(@NonNull ModuleID parentPolling) {
      if (parentPolling == null) {
        throw new IllegalArgumentException("Argument \"parent_polling\" is marked as non-null but was passed a null value.");
      }
      this.arguments.put("parent_polling", parentPolling);
    }

    @NonNull
    public ActionPollingFragmentToPollItemFragment setParentPolling(
        @NonNull ModuleID parentPolling) {
      if (parentPolling == null) {
        throw new IllegalArgumentException("Argument \"parent_polling\" is marked as non-null but was passed a null value.");
      }
      this.arguments.put("parent_polling", parentPolling);
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    @NonNull
    public Bundle getArguments() {
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
    public int getActionId() {
      return R.id.action_pollingFragment_to_pollItemFragment;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public ModuleID getParentPolling() {
      return (ModuleID) arguments.get("parent_polling");
    }

    @Override
    public boolean equals(Object object) {
      if (this == object) {
          return true;
      }
      if (object == null || getClass() != object.getClass()) {
          return false;
      }
      ActionPollingFragmentToPollItemFragment that = (ActionPollingFragmentToPollItemFragment) object;
      if (arguments.containsKey("parent_polling") != that.arguments.containsKey("parent_polling")) {
        return false;
      }
      if (getParentPolling() != null ? !getParentPolling().equals(that.getParentPolling()) : that.getParentPolling() != null) {
        return false;
      }
      if (getActionId() != that.getActionId()) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + (getParentPolling() != null ? getParentPolling().hashCode() : 0);
      result = 31 * result + getActionId();
      return result;
    }

    @Override
    public String toString() {
      return "ActionPollingFragmentToPollItemFragment(actionId=" + getActionId() + "){"
          + "parentPolling=" + getParentPolling()
          + "}";
    }
  }
}
