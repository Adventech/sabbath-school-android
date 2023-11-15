package app.ss.tv.presentation.home;

import com.slack.circuit.runtime.Navigator;
import dagger.internal.DaggerGenerated;
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
    "KotlinInternalInJava"
})
public final class HomePresenter_Factory {
  public HomePresenter_Factory() {
  }

  public HomePresenter get(Navigator navigator) {
    return newInstance(navigator);
  }

  public static HomePresenter_Factory create() {
    return new HomePresenter_Factory();
  }

  public static HomePresenter newInstance(Navigator navigator) {
    return new HomePresenter(navigator);
  }
}
