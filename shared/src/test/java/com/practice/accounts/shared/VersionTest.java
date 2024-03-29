package com.practice.accounts.shared;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class VersionTest {

  @Test
  public void shoutReturnNextVersion() {
    // GIVEN
    var version = Version.createFirstVersion();

    // WHEN
    var newVersion = version.next();

    // THEN
    assertThat(newVersion.value()).isEqualTo(1);
  }

  @Test
  public void shoutReturnTrueForIsNextIf1VersionAhead() {
    // GIVEN
    var version = Version.createFirstVersion();
    var newVersion = version.next();

    // WHEN
    var result = newVersion.isNextAfter(version);

    // THEN
    assertThat(result).isEqualTo(true);
  }

  @Test
  public void shoutReturnFalseForIsNextIfMoreThen1VersionAhead() {
    // GIVEN
    var version = Version.createFirstVersion();
    var newVersion = version.next();
    newVersion = newVersion.next();
    // WHEN
    var result = newVersion.isNextAfter(version);

    // THEN
    assertThat(result).isEqualTo(false);
  }

  @Test
  public void shoutReturnFalseForIsNextIfVersionBellow() {
    // GIVEN
    var version = Version.createFirstVersion();
    var newVersion = version.next();

    // WHEN
    var result = version.isNextAfter(newVersion);

    // THEN
    assertThat(result).isEqualTo(false);
  }

  @Test
  public void shoutReturnFalseForIsNextIfSameVersions() {
    // GIVEN
    var version = Version.createFirstVersion();
    var newVersion = Version.createFirstVersion();

    // WHEN
    var result = newVersion.isNextAfter(version);

    // THEN
    assertThat(result).isEqualTo(false);
  }

  @Test
  public void shoutReturnFalseIsNotEqualWhenEqual() {
    // GIVEN
    var version = Version.createFirstVersion();
    var newVersion = Version.createFirstVersion();

    // WHEN
    var result = newVersion.isNotEqualTo(version);

    // THEN
    assertThat(result).isEqualTo(false);
  }

  @Test
  public void shoutReturnTrueIsNotEqualWhenNotEqual() {
    // GIVEN
    var version = Version.createFirstVersion();
    var newVersion = Version.createFirstVersion();
    newVersion = newVersion.next();

    // WHEN
    var result = newVersion.isNotEqualTo(version);

    // THEN
    assertThat(result).isEqualTo(true);
  }
}
