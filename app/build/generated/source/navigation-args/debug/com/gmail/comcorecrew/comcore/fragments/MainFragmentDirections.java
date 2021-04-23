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
import java.lang.IllegalArgumentException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.HashMap;

public class MainFragmentDirections {
  private MainFragmentDirections() {
  }

  @NonNull
  public static NavDirections actionMainFragmentToSettingsFragment() {
    return new ActionOnlyNavDirections(R.id.action_mainFragment_to_settingsFragment);
  }

  @NonNull
  public static ActionMainFragmentToCreateGroupFragment actionMainFragmentToCreateGroupFragment(
      @Nullable GroupID parentGroupID) {
    return new ActionMainFragmentToCreateGroupFragment(parentGroupID);
  }

  @NonNull
  public static ActionMainFragmentToGroupFragment actionMainFragmentToGroupFragment(
      @NonNull GroupID GroupID) {
    return new ActionMainFragmentToGroupFragment(GroupID);
  }

  @NonNull
  public static NavDirections actionMainFragmentToSharedCalendarFragment23() {
    return new ActionOnlyNavDirections(R.id.action_mainFragment_to_sharedCalendarFragment23);
  }

  public static class ActionMainFragmentToCreateGroupFragment implements NavDirections {
    private final HashMap arguments = new HashMap();

    private ActionMainFragmentToCreateGroupFragment(@Nullable GroupID parentGroupID) {
      this.arguments.put("parentGroupID", parentGroupID);
    }

    @NonNull
    public ActionMainFragmentToCreateGroupFragment setParentGroupID(
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
      return R.id.action_mainFragment_to_createGroupFragment;
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
      ActionMainFragmentToCreateGroupFragment that = (ActionMainFragmentToCreateGroupFragment) object;
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
      return "ActionMainFragmentToCreateGroupFragment(actionId=" + getActionId() + "){"
          + "parentGroupID=" + getParentGroupID()
          + "}";
    }
  }

  public static class ActionMainFragmentToGroupFragment implements NavDirections {
    private final HashMap arguments = new HashMap();

    private ActionMainFragmentToGroupFragment(@NonNull GroupID GroupID) {
      if (GroupID == null) {
        throw new IllegalArgumentException("Argument \"Group_ID\" is marked as non-null but was passed a null value.");
      }
      this.arguments.put("Group_ID", GroupID);
    }

    @NonNull
    public ActionMainFragmentToGroupFragment setGroupID(@NonNull GroupID GroupID) {
      if (GroupID == null) {
        throw new IllegalArgumentException("Argument \"Group_ID\" is marked as non-null but was passed a null value.");
      }
      this.arguments.put("Group_ID", GroupID);
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    @NonNull
    public Bundle getArguments() {
      Bundle __result = new Bundle();
      if (arguments.containsKey("Group_ID")) {
        GroupID GroupID = (GroupID) arguments.get("Group_ID");
        if (Parcelable.class.isAssignableFrom(GroupID.class) || GroupID == null) {
          __result.putParcelable("Group_ID", Parcelable.class.cast(GroupID));
        } else if (Serializable.class.isAssignableFrom(GroupID.class)) {
          __result.putSerializable("Group_ID", Serializable.class.cast(GroupID));
        } else {
          throw new UnsupportedOperationException(GroupID.class.getName() + " must implement Parcelable or Serializable or must be an Enum.");
        }
      }
      return __result;
    }

    @Override
    public int getActionId() {
      return R.id.action_mainFragment_to_groupFragment;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public GroupID getGroupID() {
      return (GroupID) arguments.get("Group_ID");
    }

    @Override
    public boolean equals(Object object) {
      if (this == object) {
          return true;
      }
      if (object == null || getClass() != object.getClass()) {
          return false;
      }
      ActionMainFragmentToGroupFragment that = (ActionMainFragmentToGroupFragment) object;
      if (arguments.containsKey("Group_ID") != that.arguments.containsKey("Group_ID")) {
        return false;
      }
      if (getGroupID() != null ? !getGroupID().equals(that.getGroupID()) : that.getGroupID() != null) {
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
      result = 31 * result + (getGroupID() != null ? getGroupID().hashCode() : 0);
      result = 31 * result + getActionId();
      return result;
    }

    @Override
    public String toString() {
      return "ActionMainFragmentToGroupFragment(actionId=" + getActionId() + "){"
          + "GroupID=" + getGroupID()
          + "}";
    }
  }
}
