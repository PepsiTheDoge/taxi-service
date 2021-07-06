package taxi.mate.dao.impl;

import mate.dao.CarDao;
import mate.lib.annotations.Dao;
import mate.lib.exception.DataProcessingException;
import mate.model.Car;
import mate.model.Driver;
import mate.model.Manufacturer;
import mate.util.ConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Dao
public class CarDaoImpl implements CarDao {
    private static final int ZERO_PLACEHOLDER = 0;
    private static final int PARAMETER_SHIFT = 2;

    @Override
    public Car create(Car car) {
        String insertQuery = "INSERT INTO cars (model, manufacturer_id)"
                + "VALUES (?, ?)";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(
                             insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, car.getModel());
            preparedStatement.setLong(2, car.getManufacturer().getId());
            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                car.setId(resultSet.getObject(1, Long.class));
            }
        } catch (SQLException e) {
            throw new DataProcessingException("Can't create car " + car, e);
        }
        insertAllDrivers(car);
        return car;
    }

    @Override
    public Optional<Car> get(Long id) {
        String selectQuery = "SELECT c.id as id, "
                + "model, "
                + "manufacturer_id, "
                + "m.name as manufacturer_name, "
                + "m.country as manufacturer_country "
                + "FROM cars c"
                + " JOIN manufacturers m on c.manufacturer_id = m.id"
                + " where c.id = ? AND c.deleted = false";
        Car car = null;
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(selectQuery)) {
            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                car = parseCarFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            throw new DataProcessingException("Can't get car by id: " + id, e);
        }
        if (car != null) {
            car.setDrivers(getAllDriversByCarId(car.getId()));
        }
        return Optional.ofNullable(car);
    }

    @Override
    public List<Car> getAll() {
        String selectQuery = "SELECT c.id as id, "
                + "model, "
                + "manufacturer_id, "
                + "m.name as manufacturer_name, "
                + "m.country as manufacturer_country "
                + "FROM cars c"
                + " JOIN manufacturers m on c.manufacturer_id = m.id"
                + " where c.deleted = false";
        List<Car> cars = new ArrayList<>();
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(selectQuery)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                cars.add(parseCarFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            throw new DataProcessingException("Can't get all cars", e);
        }
        cars.forEach(car -> car.setDrivers(getAllDriversByCarId(car.getId())));
        return cars;
    }

    @Override
    public Car update(Car car) {
        String selectQuery = "UPDATE cars SET model = ?, manufacturer_id = ? WHERE id = ?"
                + " and deleted = false";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(selectQuery)) {
            preparedStatement.setString(1, car.getModel());
            preparedStatement.setLong(2, car.getManufacturer().getId());
            preparedStatement.setLong(3, car.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataProcessingException("Can't update car " + car, e);
        }
        deleteAllDriversExceptList(car);
        insertAllDrivers(car);
        return car;
    }

    @Override
    public boolean delete(Long id) {
        String selectQuery = "UPDATE cars SET deleted = true WHERE id = ?"
                + " and deleted = false";
        try (Connection connection = ConnectionUtil.getConnection();
                 PreparedStatement preparedStatement =
                         connection.prepareStatement(selectQuery)) {
            preparedStatement.setLong(1, id);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataProcessingException("Can't delete car by id " + id, e);
        }
    }

    @Override
    public List<Car> getAllByDriver(Long driverId) {
        String selectQuery = "SELECT c.id as id, "
                + "model, "
                + "manufacturer_id, "
                + "m.name as manufacturer_name, "
                + "m.country as manufacturer_country "
                + "FROM cars c"
                + " JOIN manufacturers m on c.manufacturer_id = m.id"
                + " JOIN cars_drivers cd on c.id = cd.car_id"
                + " JOIN drivers d on cd.driver_id = d.id"
                + " where c.deleted = false and driver_id = ?"
                + " and d.deleted = false";
        List<Car> cars = new ArrayList<>();
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(selectQuery)) {
            preparedStatement.setLong(1, driverId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                cars.add(parseCarFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            throw new DataProcessingException("Can't get all cars", e);
        }
        cars.forEach(car -> car.setDrivers(getAllDriversByCarId(car.getId())));
        return cars;
    }

    private void insertAllDrivers(Car car) {
        Long carId = car.getId();
        List<Driver> drivers = car.getDrivers();
        if (drivers.size() == 0) {
            return;
        }
        String insertQuery = "INSERT INTO cars_drivers (car_id, driver_id) VALUES "
                + drivers.stream().map(driver -> "(?, ?)").collect(Collectors.joining(", "))
                + " ON DUPLICATE KEY UPDATE car_id = car_id";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(insertQuery)) {
            for (int i = 0; i < drivers.size(); i++) {
                Driver driver = drivers.get(i);
                preparedStatement.setLong((i * PARAMETER_SHIFT) + 1, carId);
                preparedStatement.setLong((i * PARAMETER_SHIFT) + 2, driver.getId());
            }
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataProcessingException("Can't insert drivers " + drivers, e);
        }
    }


    private void deleteAllDriversExceptList(Car car) {
        Long carId = car.getId();
        List<Driver> exceptions = car.getDrivers();
        int size = exceptions.size();
        String insertQuery = "DELETE FROM cars_drivers WHERE car_id = ? "
                + "AND NOT driver_id IN ("
                + ZERO_PLACEHOLDER + ", ?".repeat(size)
                + ");";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(insertQuery)) {
            preparedStatement.setLong(1, carId);
            for (int i = 0; i < size; i++) {
                Driver driver = exceptions.get(i);
                preparedStatement.setLong((i) + PARAMETER_SHIFT, driver.getId());
            }
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataProcessingException("Can't delete drivers " + exceptions, e);
        }
    }

    private List<Driver> getAllDriversByCarId(Long carId) {
        String selectQuery = "SELECT id, name, license_number, login, password "
                + "FROM cars_drivers cd "
                + "JOIN drivers d on cd.driver_id = d.id "
                + "where car_id = ? AND deleted = false";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(selectQuery)) {
            preparedStatement.setLong(1, carId);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Driver> drivers = new ArrayList<>();
            while (resultSet.next()) {
                drivers.add(parseDriverFromResultSet(resultSet));
            }
            return drivers;
        } catch (SQLException e) {
            throw new DataProcessingException("Can't get all drivers by car id" + carId, e);
        }
    }

    private Driver parseDriverFromResultSet(ResultSet resultSet) throws SQLException {
        long driverId = resultSet.getLong("id");
        String name = resultSet.getNString("name");
        String licenseNumber = resultSet.getNString("license_number");
        Driver driver = new Driver(name, licenseNumber);
        driver.setId(driverId);
        return driver;
    }

    private Car parseCarFromResultSet(ResultSet resultSet) throws SQLException {
        long manufacturerId = resultSet.getObject("manufacturer_id", Long.class);
        String manufacturerName = resultSet.getNString("manufacturer_name");
        String manufacturerCountry = resultSet.getNString("manufacturer_country");
        Manufacturer manufacturer = new Manufacturer(manufacturerName, manufacturerCountry);
        manufacturer.setId(manufacturerId);
        long carId = resultSet.getLong("id");
        String model = resultSet.getNString("model");
        Car car = new Car(model, manufacturer);
        car.setId(carId);
        return car;
    }
}
