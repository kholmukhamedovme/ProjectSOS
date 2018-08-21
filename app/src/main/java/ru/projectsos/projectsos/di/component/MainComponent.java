package ru.projectsos.projectsos.di.component;

import dagger.Subcomponent;
import ru.projectsos.projectsos.di.module.MainModule;
import ru.projectsos.projectsos.di.scope.MainScope;
import ru.projectsos.projectsos.presentation.view.MainActivity;

@MainScope
@Subcomponent(modules = MainModule.class)
public interface MainComponent {

    void inject(MainActivity target);

    @Subcomponent.Builder
    interface Builder {

        Builder mainModule(MainModule module);

        MainComponent build();

    }

}
