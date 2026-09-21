package com.bioscanner.presentation.capture;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class CaptureViewModel_Factory implements Factory<CaptureViewModel> {
  @Override
  public CaptureViewModel get() {
    return newInstance();
  }

  public static CaptureViewModel_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static CaptureViewModel newInstance() {
    return new CaptureViewModel();
  }

  private static final class InstanceHolder {
    static final CaptureViewModel_Factory INSTANCE = new CaptureViewModel_Factory();
  }
}
