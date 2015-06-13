package org.pimatic.model;

/**
 * Created by Oliver Schneider <oliverschneider89+sweetpi@gmail.com>
 */
public interface DeviceVisitor<T> {

    T visitDevice(Device d);
    T visitSwitchDevice(SwitchDevice d);
    T visitButtonsDevice(ButtonsDevice buttonsDevice);
    T visitThermostatDevice(ThermostatDevice thermostatDevice);
}
