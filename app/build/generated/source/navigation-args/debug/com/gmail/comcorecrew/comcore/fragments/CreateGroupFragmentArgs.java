package com.gmail.comcorecrew.comcore.fragments;

import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavArgs;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
import java.io.Serializable;
import java.lang.IllegalArgumentException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.HashMap;

public class CreateGroupFragmentArgs implements NavArgs {
  private final HashMap arguments = new HashMap();

  private CreateGroupFragmentArgs() {
  }

  private CreateGroupFragmentArgs(HashMap argumentsMap) {
    this.arguments.putAll(argumentsMap);
  }

  @NonNull
  @SuppressWarnings("unchecked")
  public static CreateGroupFragmentArgs fromBundle(@NonNull Bundle bundle) {
    CreateGroupFragmentArgs __result = new CreateGroupFragmentArgs();
    bundle.setClassLoader(CreateGroupFragmentArgs.class.getClassLoader());
    if (bundle.containsKey("parentGroupID")) {
      GroupID parentGroupID;
      if (Parcelable.class.isAssignableFrom(GroupID.class) || Serializable.class.isAssignableFrom(GroupID.class)) {
        parentGroupID = (GroupID) bundle.get("parentGroupID");
      } else {
        throw new UnsupportedOperationException(GroupID.class.getName() + " must implement Parcelable or Serializable or must be an Enum.");
      }
      __result.arguments.put("parentGroupID", parentGroupID);
    } else {
      throw new IllegalArgumentException("Required argument \"parentGroupID\" is missing and does not have an android:defaultValue");
    }
    return __result;
  }

  @SuppressWarnings("unchecked")
  @Nullable
  public GroupID getParentGroupID() {
    return (GroupID) arguments.get("parentGroupID");
  }

  @SuppressWarnings("unchecked")
  @NonNull
  public Bundle toBundle() {
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
  public boolean equals(Object object) {
    if (this == object) {
        return true;
    }
    if (object == null || getClass() != object.getClass()) {
        return false;
    }
    CreateGroupFragmentArgs that = (CreateGroupFragmentArgs) object;
    if (arguments.containsKey("parentGroupID") != that.arguments.containsKey("parentGroupID")) {
      return false;
    }
    if (getParentGroupID() != null ? !getParentGroupID().equals(that.getParentGroupID()) : that.getParentGroupID() != null) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + (getParentGroupID() != null ? getParentGroupID().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "CreateGroupFragmentArgs{"
        + "parentGroupID=" + getParentGroupID()
        + "}";
  }

  public static class Builder {
    private final HashMap arguments = new HashMap();

    public Builder(CreateGroupFragmentArgs original) {
      this.arguments.putAll(original.arguments);
    }

    public Builder(@Nullable GroupID parentGroupID) {
      this.arguments.put("parentGroupID", parentGroupID);
    }

    @NonNull
    public CreateGroupFragmentArgs build() {
      CreateGroupFragmentArgs result = new CreateGroupFragmentArgs(arguments);
      return result;
    }

    @NonNull
    public Builder setParentGroupID(@Nullable GroupID parentGroupID) {
      this.arguments.put("parentGroupID", parentGroupID);
      return this;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public GroupID getParentGroupID() {
      return (GroupID) arguments.get("parentGroupID");
    }
  }
}
