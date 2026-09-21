package com.bioscanner.presentation.result;

import android.content.Context;
import androidx.lifecycle.SavedStateHandle;
import com.bioscanner.domain.usecase.ClassifyAndLookupSpeciesUseCase;
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
public final class ResultViewModel_Factory implements Factory<ResultViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<ClassifyAndLookupSpeciesUseCase> classifyAndLookupSpeciesProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private ResultViewModel_Factory(Provider<Context> contextProvider,
      Provider<ClassifyAndLookupSpeciesUseCase> classifyAndLookupSpeciesProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.contextProvider = contextProvider;
    this.classifyAndLookupSpeciesProvider = classifyAndLookupSpeciesProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public ResultViewModel get() {
    return newInstance(contextProvider.get(), classifyAndLookupSpeciesProvider.get(), savedStateHandleProvider.get());
  }

  public static ResultViewModel_Factory create(Provider<Context> contextProvider,
      Provider<ClassifyAndLookupSpeciesUseCase> classifyAndLookupSpeciesProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new ResultViewModel_Factory(contextProvider, classifyAndLookupSpeciesProvider, savedStateHandleProvider);
  }

  public static ResultViewModel newInstance(Context context,
      ClassifyAndLookupSpeciesUseCase classifyAndLookupSpecies, SavedStateHandle savedStateHandle) {
    return new ResultViewModel(context, classifyAndLookupSpecies, savedStateHandle);
  }
}
