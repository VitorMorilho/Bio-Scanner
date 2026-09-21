package com.bioscanner.data.classifier;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class TfLiteImageClassifier_Factory implements Factory<TfLiteImageClassifier> {
  private final Provider<Context> contextProvider;

  private TfLiteImageClassifier_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public TfLiteImageClassifier get() {
    return newInstance(contextProvider.get());
  }

  public static TfLiteImageClassifier_Factory create(Provider<Context> contextProvider) {
    return new TfLiteImageClassifier_Factory(contextProvider);
  }

  public static TfLiteImageClassifier newInstance(Context context) {
    return new TfLiteImageClassifier(context);
  }
}
