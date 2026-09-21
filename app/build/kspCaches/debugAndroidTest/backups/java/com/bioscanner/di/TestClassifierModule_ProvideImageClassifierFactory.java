package com.bioscanner.di;

import com.bioscanner.domain.classifier.ImageClassifier;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class TestClassifierModule_ProvideImageClassifierFactory implements Factory<ImageClassifier> {
  @Override
  public ImageClassifier get() {
    return provideImageClassifier();
  }

  public static TestClassifierModule_ProvideImageClassifierFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ImageClassifier provideImageClassifier() {
    return Preconditions.checkNotNullFromProvides(TestClassifierModule.INSTANCE.provideImageClassifier());
  }

  private static final class InstanceHolder {
    static final TestClassifierModule_ProvideImageClassifierFactory INSTANCE = new TestClassifierModule_ProvideImageClassifierFactory();
  }
}
