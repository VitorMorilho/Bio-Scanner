package com.bioscanner.domain.usecase;

import com.bioscanner.domain.classifier.ImageClassifier;
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
public final class ClassifyAndLookupSpeciesUseCase_Factory implements Factory<ClassifyAndLookupSpeciesUseCase> {
  private final Provider<ImageClassifier> classifierProvider;

  private final Provider<SpeciesRepository> repositoryProvider;

  private ClassifyAndLookupSpeciesUseCase_Factory(Provider<ImageClassifier> classifierProvider,
      Provider<SpeciesRepository> repositoryProvider) {
    this.classifierProvider = classifierProvider;
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public ClassifyAndLookupSpeciesUseCase get() {
    return newInstance(classifierProvider.get(), repositoryProvider.get());
  }

  public static ClassifyAndLookupSpeciesUseCase_Factory create(
      Provider<ImageClassifier> classifierProvider,
      Provider<SpeciesRepository> repositoryProvider) {
    return new ClassifyAndLookupSpeciesUseCase_Factory(classifierProvider, repositoryProvider);
  }

  public static ClassifyAndLookupSpeciesUseCase newInstance(ImageClassifier classifier,
      SpeciesRepository repository) {
    return new ClassifyAndLookupSpeciesUseCase(classifier, repository);
  }
}
