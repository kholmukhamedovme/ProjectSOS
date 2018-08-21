package ru.projectsos.projectsos.di.module;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.polidea.rxandroidble2.RxBleClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.projectsos.projectsos.di.component.MainComponent;

@Module(subcomponents = MainComponent.class)
public final class AppModule {

    private static final String PREFERENCES_FILE = "project_sos";

    private final Context mContext;

    public AppModule(Context context) {
        mContext = context;
    }

    @Singleton
    @Provides
    Context provideApplicationContext() {
        return mContext.getApplicationContext();
    }

    @Singleton
    @Provides
    SharedPreferences provideSharedPreferences(@NonNull Context context) {
        return context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Singleton
    @Provides
    RxBleClient provideRxBleClient(@NonNull Context context) {
        return RxBleClient.create(context);
    }

}
