package com.gmail.comcorecrew.comcore.fragments;

import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;
import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
import java.io.Serializable;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.HashMap;

public class GroupFragmentDirections {
  private GroupFragmentDirections() {
  }

  @NonNull
  public static NavDirections actionGroupFragmentToChatFragment5() {
    return new ActionOnlyNavDirections(R.id.action_groupFragment_to_chatFragment5);
  }

  @NonNull
  public static NavDirections actionGroupFragmentToTaskListFragment() {
    return new ActionOnlyNavDirections(R.id.action_groupFragment_to_taskListFragment);
  }

  @NonNull
  public static NavDirections actionGroupFragmentToSettingsFragment() {
    return new ActionOnlyNavDirections(R.id.action_groupFragment_to_settingsFragment);
  }

  @NonNull
  public static NavDirections actionGroupFragmentToCustomFragment() {
    return new ActionOnlyNavDirections(R.id.action_groupFragment_to_customFragment);
  }

  @NonNull
  public static ActionGroupFragmentToCreateGroupFragment actionGroupFragmentToCreateGroupFragment(
      @Nullable GroupID parentGroupID) {
    return new ActionGroupFragmentToCreateGroupFragment(parentGroupID);
  }

  @NonNull
  public static NavDirections actionGroupFragmentToCalendarFragment() {
    return new ActionOnlyNavDirections(R.id.action_groupFragment_to_calendarFragment);
  }

  @NonNull
  public static NavDirections actionGroupFragmentToPollingFragment() {
    return new ActionOnlyNavDirections(R.id.action_groupFragment_to_pollingFragment);
  }

  public static class ActionGroupFragmentToCreateGroupFragment implements NavDirections {
    private final HashMap arguments = new HashMap();

    private ActionGroupFragmentToCreateGroupFragment(@Nullable GroupID parentGroupID) {
      this.arguments.put("parentGroupID", parentGroupID);
    }

    @NonNull
    public ActionGroupFragmentToCreateGroupFragment setParentGroupID(
        @Nullable GroupID parentGroupID) {
      this.arguments.put("parentGroupID", parentGroupID);
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    @NonNull
    public Bundle getArguments() {
      Bundle __result = new Bundle();
      if (arguments.containsKey("parentGroupID")) {
        GroupID parentGroupID = (GroupID) arguments.get("parentGroupID");
        if (Parcelable.class.isAssignableFrom(GroupID.class) || parentGroupID == null) {
          __result.putParcelable("parentGroupID", Parcelable.class.cast(parentGroupID));
        } else if (Serializable.class.isAssignableFrom(GroupID.class)) {
          __result.putSerializable("parentGroupID", Serializable.class.cast(parentGroupID));
        } else {
          throw new UnsupportedOperationException(GroupID.class.getName() + " must implement Parcelable or Serializable or must be an Enum.");
        }
      }
      return __result;
    }

    @Override
    public int getActionId() {
      return R.id.action_groupFragment_to_createGroupFragment;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public GroupID getParentGroupID() {
      return (GroupID) arguments.get("parentGroupID");
    }

    @Override
    public boolean equals(Object object) {
      if (this == object) {
          return true;
      }
      if (object == null || getClass() != object.getClass()) {
          return false;
      }
      ActionGroupFragmentToCreateGroupFragment that = (ActionGroupFragmentToCreateGroupFragment) object;
      if (arguments.containsKey("parentGroupID") != that.arguments.containsKey("parentGroupID")) {
        return false;
      }
      if (getParentGroupID() != null ? !getParentGroupID().equals(that.getParentGroupID()) : that.getParentGroupID() != null) {
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
      result = 31 * result + (getParentGroupID() != null ? getParentGroupID().hashCode() : 0);
      result = 31 * result + getActionId();
      return result;
    }

    @Override
    public String toString() {
      return "ActionGroupFragmentToCreateGroupFragment(actionId=" + getActionId() + "){"
          + "parentGroupID=" + getParentGroupID()
          + "}";
    }
  }
}
