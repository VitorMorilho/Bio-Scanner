package com.bioscanner.domain.usecase;

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
public final class GetSpeciesByIdUseCase_Factory implements Factory<GetSpeciesByIdUseCase> {
  private final Provider<SpeciesRepository> repositoryProvider;

  private GetSpeciesByIdUseCase_Factory(Provider<SpeciesRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetSpeciesByIdUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetSpeciesByIdUseCase_Factory create(
      Provider<SpeciesRepository> repositoryProvider) {
    return new GetSpeciesByIdUseCase_Factory(repositoryProvider);
  }

  public static GetSpeciesByIdUseCase newInstance(SpeciesRepository repository) {
    return new GetSpeciesByIdUseCase(repository);
  }
}
