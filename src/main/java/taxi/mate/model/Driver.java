package taxi.mate.model;

import java.util.Objects;

public class Driver {
    private Long id;
    private String name;
    private String licenseNumber;
    private String login;
    private String password;

    public Driver(String name, String licenseNumber) {
        this.name = name;
        this.licenseNumber = licenseNumber;
    }

    public Driver(String name, String licenseNumber, String login, String password) {
        this(name, licenseNumber);
        this.login = login;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Driver{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", licenseNumber='" + licenseNumber + '\''
                + ", login='" + login + '\''
                + '}';
    }

    @Override
    public boolean equals(Object driver) {
        if (this == driver) {
            return true;
        }
        if (driver == null || getClass() != driver.getClass()) {
            return false;
        }
        Driver otherDriver = (Driver) driver;
        return Objects.equals(id, otherDriver.id)
                && Objects.equals(name, otherDriver.name)
                && Objects.equals(licenseNumber, otherDriver.licenseNumber)
                && Objects.equals(login, otherDriver.login)
                && Objects.equals(password, otherDriver.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, licenseNumber, login, password);
    }
}
