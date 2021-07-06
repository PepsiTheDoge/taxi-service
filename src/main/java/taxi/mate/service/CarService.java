package taxi.mate.service;

import mate.model.Car;
import mate.model.Driver;

import java.util.List;

public interface CarService extends GenericService<Car> {
    void addDriverToCar(Driver driver, Car car);

    void removeDriverFromCar(Driver driver, Car car);

    List<Car> getAllByDriver(Long driverId);
}
