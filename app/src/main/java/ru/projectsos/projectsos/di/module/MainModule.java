package ru.projectsos.projectsos.di.module;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.polidea.rxandroidble2.RxBleClient;

import dagger.Module;
import dagger.Provides;
import ru.projectsos.projectsos.data.repository.MainRepositoryImpl;
import ru.projectsos.projectsos.di.scope.MainScope;
import ru.projectsos.projectsos.domain.MainInteractor;
import ru.projectsos.projectsos.domain.MainRepository;
import ru.projectsos.projectsos.presentation.presenter.MainPresenter;

@Module
public final class MainModule {

    @MainScope
    @Provides
    MainRepository provideMainRepository(@NonNull RxBleClient rxBleClient,
                                         @NonNull SharedPreferences sharedPreferences) {
        return new MainRepositoryImpl(rxBleClient, sharedPreferences);
    }

    @MainScope
    @Provides
    MainInteractor provideMainInteractor(@NonNull MainRepository repository) {
        return new MainInteractor(repository);
    }

    @MainScope
    @Provides
    MainPresenter provideMainPresenter(@NonNull MainInteractor interactor) {
        return new MainPresenter(interactor);
    }

}
