package cz.muni.fi.pv168;

import java.io.FileOutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.sql.DataSource;

public class RentlManagerImplementation implements RentlManager {

    @Override
    public Customer findCustomerWithCar(Car car) throws IllegalArgumentException, TransactionException {
        if (null == car) {
            throw new IllegalArgumentException("Can't find Car with NULL pointer");
        }
        if (null == car.getID()) {
            throw new IllegalArgumentException("Can't find Car with NULL ID");
        }
        if (car.getAvailable()) {
            throw new IllegalArgumentException("Car is NOT RENTED");
        }
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("SELECT customer FROM INVENTORIES WHERE car=?");
            statement.setLong(1, car.getID());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Customer result = customerManager.findCustomerByID(resultSet.getLong("customer"));
                if (resultSet.next()) {
                    throw new IllegalArgumentException("Multiple Customers with same Car");
                }
                return result;
            } else {
                return null;
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error when getting customer from DB", ex);
            throw new TransactionException("Error when getting customer from DB", ex);
        } finally {
            DBUtils.closeQuietly(connection);
        }
    }

    @Override
    public List<Car> getAllCustomerCars(Customer customer) throws IllegalArgumentException, TransactionException {
        if (null == customer) {
            throw new IllegalArgumentException("CUSTOMER POINTS TO NULL");
        }
        if (null == customer.getID()) {
            throw new IllegalArgumentException("CUSTOMER ID IN NULL");
        }
        if (!customer.getActive()) {
            throw new IllegalArgumentException("CUSTOMER IS NOT ACTIVE");
        }
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("SELECT car FROM INVENTORIES WHERE customer=?");
            statement.setLong(1, customer.getID());
            ResultSet resultSet = statement.executeQuery();
            List<Car> customerCars = new ArrayList<>();
            while (resultSet.next()) {
                customerCars.add(carManager.findCarByID(resultSet.getLong("car")));
            }
            return customerCars;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error when getting Cars from Inventory DB", ex);
            throw new TransactionException("Error when getting Cars from Inventory DB", ex);
        } finally {
            DBUtils.closeQuietly(connection);
        }
    }

    @Override
    public void rentCarToCustomer(Car car, Customer customer, Date rentDate, Date dueDate) throws IllegalArgumentException, TransactionException {
        if ((null == car) || (null == customer)) {
            throw new IllegalArgumentException("Can't insert null entry to DB");
        }
        if (null == car.getID()) {
            throw new IllegalArgumentException("Car ID is NULL");
        }
        if (null == customer.getID()) {
            throw new IllegalArgumentException("Customer ID is NULL");
        }
        if (!car.getAvailable()) {
            throw new IllegalArgumentException("Car is already rented");
        }
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("INSERT INTO INVENTORIES (car, customer, rent_date, due_date) VALUES (?,?,?,?)");
            statement.setLong(1, car.getID());
            statement.setLong(2, customer.getID());
            statement.setDate(3, rentDate);
            statement.setDate(4, dueDate);
            if (1 != statement.executeUpdate()) {
                throw new TransactionException("Cant INSERT Inventory to InventoryDB");
            }
            car.setStatus(Boolean.FALSE);
            customer.setActive(Boolean.TRUE);
            carManager.updateCarInfo(car);
            customerManager.updateCustomerInfo(customer);
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error when renting car to customer in InventoryDB", ex);
            throw new TransactionException("Error when renting car to customer in InventoryDB", ex);
        } finally {
            DBUtils.closeQuietly(connection);
        }
    }

    @Override
    public void getCarFromCustomer(Car car, Customer customer) throws IllegalArgumentException, TransactionException {
        if ((null == car) || (null == customer)) {
            throw new IllegalArgumentException("Can't use NULL entry");
        }
        if ((null == car.getID()) || (null == customer.getID())) {
            throw new IllegalArgumentException("Customer or Car ID is NULL not exist in DB");
        }
        if (!customer.getActive()) {
            throw new IllegalArgumentException("Customer is not active");
        }
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("DELETE FROM INVENTORIES WHERE customer=? AND car=?");
            statement.setLong(1, customer.getID());
            statement.setLong(2, car.getID());

            if (0 == statement.executeUpdate()) {
                throw new TransactionException("Rent not found");
            }
            if (getAllCustomerCars(customer).isEmpty()) {
                customer.setActive(Boolean.FALSE);
            }
            car.setStatus(true);
            customerManager.updateCustomerInfo(customer);
            carManager.updateCarInfo(car);
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error when DELETE from DB", ex);
            throw new TransactionException("Error when DELETE from DB", ex);
        } finally {
            DBUtils.closeQuietly(connection);
        }

    }

    public void tryCreateTables() {
        if (null == dataSource) {
            throw new IllegalStateException("DataSource is not set");
        }
        try {
            DBUtils.tryCreateTables(dataSource);
        } catch (SQLException ex) {
            throw new IllegalStateException("Error when trying to create tables");
        }
    }

    @Override
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        carManager.setDataSource(dataSource);
        customerManager.setDataSource(dataSource);
    }
    public static final Logger logger = Logger.getLogger(CarManagerImplementation.class.getName());
    private CustomerManager customerManager = new CustomerManagerImplementation();
    private CarManager carManager = new CarManagerImplementation();
    private DataSource dataSource;

    @Override
    public void setLogger(FileOutputStream fs) {
        logger.addHandler(new StreamHandler(fs, new SimpleFormatter()));
    }
}
