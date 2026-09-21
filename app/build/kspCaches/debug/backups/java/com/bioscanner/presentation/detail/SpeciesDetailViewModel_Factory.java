package com.bioscanner.presentation.detail;

import androidx.lifecycle.SavedStateHandle;
import com.bioscanner.domain.usecase.GetSpeciesByIdUseCase;
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
public final class SpeciesDetailViewModel_Factory implements Factory<SpeciesDetailViewModel> {
  private final Provider<GetSpeciesByIdUseCase> getSpeciesByIdProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private SpeciesDetailViewModel_Factory(Provider<GetSpeciesByIdUseCase> getSpeciesByIdProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.getSpeciesByIdProvider = getSpeciesByIdProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public SpeciesDetailViewModel get() {
    return newInstance(getSpeciesByIdProvider.get(), savedStateHandleProvider.get());
  }

  public static SpeciesDetailViewModel_Factory create(
      Provider<GetSpeciesByIdUseCase> getSpeciesByIdProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new SpeciesDetailViewModel_Factory(getSpeciesByIdProvider, savedStateHandleProvider);
  }

  public static SpeciesDetailViewModel newInstance(GetSpeciesByIdUseCase getSpeciesById,
      SavedStateHandle savedStateHandle) {
    return new SpeciesDetailViewModel(getSpeciesById, savedStateHandle);
  }
}
