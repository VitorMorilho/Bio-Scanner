package com.bioscanner.data.repository;

import com.bioscanner.data.local.db.SpeciesDao;
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
public final class SpeciesRepositoryImpl_Factory implements Factory<SpeciesRepositoryImpl> {
  private final Provider<SpeciesDao> daoProvider;

  private SpeciesRepositoryImpl_Factory(Provider<SpeciesDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public SpeciesRepositoryImpl get() {
    return newInstance(daoProvider.get());
  }

  public static SpeciesRepositoryImpl_Factory create(Provider<SpeciesDao> daoProvider) {
    return new SpeciesRepositoryImpl_Factory(daoProvider);
  }

  public static SpeciesRepositoryImpl newInstance(SpeciesDao dao) {
    return new SpeciesRepositoryImpl(dao);
  }
}
