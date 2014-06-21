package org.pimatic.model;

/**
 * Created by h3llfire on 21.06.14.
 */
public interface DeviceVisitor<T> {

    T visitDevice(Device d);
    T visitSwitchDevice(SwitchDevice d);

}
