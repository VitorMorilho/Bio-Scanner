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

@ScopeMetadata("javax.inject.Singleton")
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
public final class TestDatabaseModule_ProvideDatabaseFactory implements Factory<BioScannerDatabase> {
  private final Provider<Context> contextProvider;

  private TestDatabaseModule_ProvideDatabaseFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public BioScannerDatabase get() {
    return provideDatabase(contextProvider.get());
  }

  public static TestDatabaseModule_ProvideDatabaseFactory create(
      Provider<Context> contextProvider) {
    return new TestDatabaseModule_ProvideDatabaseFactory(contextProvider);
  }

  public static BioScannerDatabase provideDatabase(Context context) {
    return Preconditions.checkNotNullFromProvides(TestDatabaseModule.INSTANCE.provideDatabase(context));
  }
}
