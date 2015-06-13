package org.pimatic.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oliver Schneider <oliverschneider89+sweetpi@gmail.com
 */
public abstract class UpdateEventEmitter<L extends UpdateEventEmitter.UpdateListener> {

    private List<L> listeners = new ArrayList<>();

    protected void didChange() {
        for (L l : listeners ) {
            l.onChange();
        }
    }

    protected void didChange(UpdateListenerNotifier<L> notifier) {
        for (L l : listeners ) {
            notifier.notifyListener(l);
        }
    }

    public void onChange(L l) {
        listeners.add(l);
    }

    public void removeListener(UpdateListener listener) {
        listeners.remove(listener);
    }

    public interface UpdateListener {
        void onChange();
    }

    public interface UpdateListenerNotifier<L> {
        void notifyListener(L l);
    }

}
