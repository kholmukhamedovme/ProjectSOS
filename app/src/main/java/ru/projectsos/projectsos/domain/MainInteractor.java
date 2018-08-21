package ru.projectsos.projectsos.domain;

import io.reactivex.Completable;
import io.reactivex.Observable;
import ru.projectsos.projectsos.models.domain.BluetoothState;

import static dagger.internal.Preconditions.checkNotNull;

public final class MainInteractor {

    private final MainRepository mRepository;

    public MainInteractor(MainRepository repository) {
        mRepository = checkNotNull(repository, "MainRepository is required");
    }

    public Observable<BluetoothState> traceBluetoothState() {
        return mRepository.traceBluetoothState();
    }

    public Completable authenticateDevice(String macAddress) {
        return mRepository.authenticateDevice(macAddress);
    }

}
