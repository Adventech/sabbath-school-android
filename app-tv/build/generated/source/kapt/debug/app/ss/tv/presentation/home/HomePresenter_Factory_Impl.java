package app.ss.tv.presentation.home;

import com.slack.circuit.runtime.Navigator;
import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class HomePresenter_Factory_Impl implements HomePresenter.Factory {
  private final HomePresenter_Factory delegateFactory;

  HomePresenter_Factory_Impl(HomePresenter_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public HomePresenter create(Navigator navigator) {
    return delegateFactory.get(navigator);
  }

  public static Provider<HomePresenter.Factory> create(HomePresenter_Factory delegateFactory) {
    return InstanceFactory.create(new HomePresenter_Factory_Impl(delegateFactory));
  }
}
