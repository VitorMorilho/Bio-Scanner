package com.bioscanner.di;

import com.bioscanner.data.local.db.BioScannerDatabase;
import com.bioscanner.data.local.db.SpeciesDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideSpeciesDaoFactory implements Factory<SpeciesDao> {
  private final Provider<BioScannerDatabase> databaseProvider;

  private DatabaseModule_ProvideSpeciesDaoFactory(Provider<BioScannerDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public SpeciesDao get() {
    return provideSpeciesDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideSpeciesDaoFactory create(
      Provider<BioScannerDatabase> databaseProvider) {
    return new DatabaseModule_ProvideSpeciesDaoFactory(databaseProvider);
  }

  public static SpeciesDao provideSpeciesDao(BioScannerDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideSpeciesDao(database));
  }
}
