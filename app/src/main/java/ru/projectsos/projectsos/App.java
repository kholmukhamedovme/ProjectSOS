package ru.projectsos.projectsos;

import android.app.Application;

import ru.projectsos.projectsos.di.component.AppComponent;
import ru.projectsos.projectsos.di.component.DaggerAppComponent;
import ru.projectsos.projectsos.di.component.MainComponent;
import ru.projectsos.projectsos.di.module.AppModule;
import ru.projectsos.projectsos.di.module.MainModule;

public final class App extends Application {

    private static AppComponent sAppComponent;
    private static MainComponent sMainComponent;

    public static AppComponent getAppComponent() {
        return sAppComponent;
    }

    public static MainComponent getMainComponent() {
        return sMainComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sAppComponent = DaggerAppComponent
                .builder()
                .appModule(new AppModule(this.getApplicationContext()))
                .build();

        sMainComponent = sAppComponent
                .mainComponentBuilder()
                .mainModule(new MainModule())
                .build();
    }

}
