package com.bioscanner.di;

import android.content.Context;
import com.bioscanner.data.local.db.BioScannerDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.coroutines.CoroutineScope;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata({
    "dagger.hilt.android.qualifiers.ApplicationContext",
    "com.bioscanner.di.ApplicationScope"
})
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
public final class DatabaseModule_ProvideDatabaseFactory implements Factory<BioScannerDatabase> {
  private final Provider<Context> contextProvider;

  private final Provider<CoroutineScope> scopeProvider;

  private final Provider<BioScannerDatabase> databaseProvider;

  private DatabaseModule_ProvideDatabaseFactory(Provider<Context> contextProvider,
      Provider<CoroutineScope> scopeProvider, Provider<BioScannerDatabase> databaseProvider) {
    this.contextProvider = contextProvider;
    this.scopeProvider = scopeProvider;
    this.databaseProvider = databaseProvider;
  }

  @Override
  public BioScannerDatabase get() {
    return provideDatabase(contextProvider.get(), scopeProvider.get(), databaseProvider);
  }

  public static DatabaseModule_ProvideDatabaseFactory create(Provider<Context> contextProvider,
      Provider<CoroutineScope> scopeProvider, Provider<BioScannerDatabase> databaseProvider) {
    return new DatabaseModule_ProvideDatabaseFactory(contextProvider, scopeProvider, databaseProvider);
  }

  public static BioScannerDatabase provideDatabase(Context context, CoroutineScope scope,
      javax.inject.Provider<BioScannerDatabase> databaseProvider) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideDatabase(context, scope, databaseProvider));
  }
}
