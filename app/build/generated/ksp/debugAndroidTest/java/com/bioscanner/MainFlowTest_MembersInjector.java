package com.bioscanner;

import com.bioscanner.data.local.db.SpeciesDao;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;

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
public final class MainFlowTest_MembersInjector implements MembersInjector<MainFlowTest> {
  private final Provider<SpeciesDao> speciesDaoProvider;

  private MainFlowTest_MembersInjector(Provider<SpeciesDao> speciesDaoProvider) {
    this.speciesDaoProvider = speciesDaoProvider;
  }

  @Override
  public void injectMembers(MainFlowTest instance) {
    injectSpeciesDao(instance, speciesDaoProvider.get());
  }

  public static MembersInjector<MainFlowTest> create(Provider<SpeciesDao> speciesDaoProvider) {
    return new MainFlowTest_MembersInjector(speciesDaoProvider);
  }

  @InjectedFieldSignature("com.bioscanner.MainFlowTest.speciesDao")
  public static void injectSpeciesDao(MainFlowTest instance, SpeciesDao speciesDao) {
    instance.speciesDao = speciesDao;
  }
}
