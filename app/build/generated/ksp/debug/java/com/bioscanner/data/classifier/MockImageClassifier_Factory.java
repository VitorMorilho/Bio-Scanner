package com.bioscanner.data.classifier;

import com.bioscanner.domain.repository.SpeciesRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
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
public final class MockImageClassifier_Factory implements Factory<MockImageClassifier> {
  private final Provider<SpeciesRepository> repositoryProvider;

  private MockImageClassifier_Factory(Provider<SpeciesRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public MockImageClassifier get() {
    return newInstance(repositoryProvider.get());
  }

  public static MockImageClassifier_Factory create(Provider<SpeciesRepository> repositoryProvider) {
    return new MockImageClassifier_Factory(repositoryProvider);
  }

  public static MockImageClassifier newInstance(SpeciesRepository repository) {
    return new MockImageClassifier(repository);
  }
}
