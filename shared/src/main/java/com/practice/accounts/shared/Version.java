package com.practice.accounts.shared;

import java.util.StringJoiner;

public class Version {
  private final int value;

  public static Version createFirstVersion() {
    return new Version(0);
  }

  private Version(int value) {
    this.value = value;
  }

  public Version next() {
    return new Version(value + 1);
  }

  public boolean isNextAfter(Version that) {
    return (value - that.value) == 1;
  }

  int value() {
    return value;
  }

  public boolean isNotEqualTo(Object that) {
    return !this.equals(that);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Version version)) return false;

    return value == version.value;
  }

  @Override
  public int hashCode() {
    return value;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Version.class.getSimpleName() + "[", "]")
        .add("value=" + value)
        .toString();
  }
}
