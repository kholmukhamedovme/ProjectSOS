package ru.projectsos.projectsos.di.component;

import javax.inject.Singleton;

import dagger.Component;
import ru.projectsos.projectsos.di.module.AppModule;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {

    MainComponent.Builder mainComponentBuilder();

}
