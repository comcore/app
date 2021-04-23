package com.gmail.comcorecrew.comcore.fragments;

import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.navigation.NavArgs;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
import java.io.Serializable;
import java.lang.IllegalArgumentException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.HashMap;

public class GroupFragmentArgs implements NavArgs {
  private final HashMap arguments = new HashMap();

  private GroupFragmentArgs() {
  }

  private GroupFragmentArgs(HashMap argumentsMap) {
    this.arguments.putAll(argumentsMap);
  }

  @NonNull
  @SuppressWarnings("unchecked")
  public static GroupFragmentArgs fromBundle(@NonNull Bundle bundle) {
    GroupFragmentArgs __result = new GroupFragmentArgs();
    bundle.setClassLoader(GroupFragmentArgs.class.getClassLoader());
    if (bundle.containsKey("Group_ID")) {
      GroupID GroupID;
      if (Parcelable.class.isAssignableFrom(GroupID.class) || Serializable.class.isAssignableFrom(GroupID.class)) {
        GroupID = (GroupID) bundle.get("Group_ID");
      } else {
        throw new UnsupportedOperationException(GroupID.class.getName() + " must implement Parcelable or Serializable or must be an Enum.");
      }
      if (GroupID == null) {
        throw new IllegalArgumentException("Argument \"Group_ID\" is marked as non-null but was passed a null value.");
      }
      __result.arguments.put("Group_ID", GroupID);
    } else {
      throw new IllegalArgumentException("Required argument \"Group_ID\" is missing and does not have an android:defaultValue");
    }
    return __result;
  }

  @SuppressWarnings("unchecked")
  @NonNull
  public GroupID getGroupID() {
    return (GroupID) arguments.get("Group_ID");
  }

  @SuppressWarnings("unchecked")
  @NonNull
  public Bundle toBundle() {
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
  public boolean equals(Object object) {
    if (this == object) {
        return true;
    }
    if (object == null || getClass() != object.getClass()) {
        return false;
    }
    GroupFragmentArgs that = (GroupFragmentArgs) object;
    if (arguments.containsKey("Group_ID") != that.arguments.containsKey("Group_ID")) {
      return false;
    }
    if (getGroupID() != null ? !getGroupID().equals(that.getGroupID()) : that.getGroupID() != null) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + (getGroupID() != null ? getGroupID().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "GroupFragmentArgs{"
        + "GroupID=" + getGroupID()
        + "}";
  }

  public static class Builder {
    private final HashMap arguments = new HashMap();

    public Builder(GroupFragmentArgs original) {
      this.arguments.putAll(original.arguments);
    }

    public Builder(@NonNull GroupID GroupID) {
      if (GroupID == null) {
        throw new IllegalArgumentException("Argument \"Group_ID\" is marked as non-null but was passed a null value.");
      }
      this.arguments.put("Group_ID", GroupID);
    }

    @NonNull
    public GroupFragmentArgs build() {
      GroupFragmentArgs result = new GroupFragmentArgs(arguments);
      return result;
    }

    @NonNull
    public Builder setGroupID(@NonNull GroupID GroupID) {
      if (GroupID == null) {
        throw new IllegalArgumentException("Argument \"Group_ID\" is marked as non-null but was passed a null value.");
      }
      this.arguments.put("Group_ID", GroupID);
      return this;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public GroupID getGroupID() {
      return (GroupID) arguments.get("Group_ID");
    }
  }
}
