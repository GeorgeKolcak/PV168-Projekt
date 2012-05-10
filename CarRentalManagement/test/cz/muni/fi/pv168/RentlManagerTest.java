package cz.muni.fi.pv168;

import static cz.muni.fi.pv168.CarManagerTest.assertCarDeepEquals;
import static cz.muni.fi.pv168.CarManagerTest.newCar;
import static cz.muni.fi.pv168.CustomerManagerTest.assertCustomerDeepEquals;
import static cz.muni.fi.pv168.CustomerManagerTest.newCustomer;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Jooji
 */
public class RentlManagerTest {

    private CarManagerImplementation carManager;
    private CustomerManagerImplementation customerManager;
    private RentManagerImplementation manager;
    private DataSource dataSource;
    private Car car1;
    private Car car2;
    private Car car3;
    private Car carWithoutID;
    private Car carNotInDB;
    private Customer customer1;
    private Customer customer2;
    private Customer customer3;
    private Customer customerWithoutID;
    private Customer customerNotInDB;

    private void prepareTestData() {

        car1 = newCar("Black", "0B6 6835", "Škoda", 200.0);
        car2 = newCar("Red", "7B4 0044", "BMW", 500.0);
        car3 = newCar("White", "8B5 0983", "Volkwagen", 300.0);

        customer1 = newCustomer("Vitalii", "Chepeliuk", "Komarov", "5-20-86", "AK 373979");
        customer2 = newCustomer("Juraj", "Kolchak", "Komarov", "5-34-86", "AK 372548");
        customer3 = newCustomer("Martin", "Jirman", "Lazhot", "5-25-87", "AK 251245");

        carManager.addCar(car1);
        carManager.addCar(car2);
        carManager.addCar(car3);

        customerManager.addCustomer(customer1);
        customerManager.addCustomer(customer2);
        customerManager.addCustomer(customer3);

        carWithoutID = newCar("Green", "8B3 9763", "Audi", 400.0);
        carNotInDB = newCar("Blue", "3B6 8463", "Peugeot", 0.0);
        carNotInDB.setID(car3.getID() + 100);

        customerWithoutID = newCustomer("Martin", "Pulec", "Brno", "5-11-24", "AK 897589");
        customerNotInDB = newCustomer("Lukas", "Rucka", "Brno", "5-21-06", "AK 256354");
        customerNotInDB.setID(customer3.getID() + 100);
        customerNotInDB.setActive(true);
    }

    @Before
    public void setUp() throws SQLException {
        dataSource = prepareDataSource();
        DBUtils.createTables(dataSource);
        manager = new RentManagerImplementation();
        manager.setDataSource(dataSource);
        carManager = new CarManagerImplementation();
        carManager.setDataSource(dataSource);
        customerManager = new CustomerManagerImplementation();
        customerManager.setDataSource(dataSource);
        prepareTestData();
    }

    @After
    public void tearDown() throws SQLException {
        DBUtils.dropTables(dataSource);
    }

    private static DataSource prepareDataSource() throws SQLException {
        BasicDataSource ds = new BasicDataSource();
        ds.setUrl("jdbc:derby:memory:CarRentalDB;create=true");
        return ds;
    }

    @Test
    public void findCustomerWithCar() {
        assertTrue(car1.getAvailable());
        assertTrue(car2.getAvailable());
        assertTrue(car3.getAvailable());

        manager.rentCarToCustomer(car1, customer1, Date.valueOf("2012-03-21"), Date.valueOf("2012-03-31"));

        assertEquals(customer1, manager.findCustomerWithCar(car1));
        assertCustomerDeepEquals(customer1, manager.findCustomerWithCar(car1));
        assertTrue(car2.getAvailable());
        assertTrue(car3.getAvailable());

        try {
            manager.findCustomerWithCar(null);
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            manager.findCustomerWithCar(carWithoutID);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void getAllCustomerCars() {
        assertFalse(customer1.getActive());
        assertFalse(customer2.getActive());
        assertFalse(customer3.getActive());

        manager.rentCarToCustomer(car2, customer1, Date.valueOf("2012-03-21"), Date.valueOf("2012-03-31"));
        manager.rentCarToCustomer(car3, customer1, Date.valueOf("2012-03-25"), Date.valueOf("2012-04-02"));
        manager.rentCarToCustomer(car1, customer2, Date.valueOf("2012-03-15"), Date.valueOf("2012-03-27"));

        List<Car> carsRetnedtoCustomer1 = Arrays.asList(car2, car3);
        List<Car> carsRetnedtoCustomer2 = Arrays.asList(car1);

        assertCarDeepEquals(carsRetnedtoCustomer1, manager.getAllCustomerCars(customer1));
        assertCarDeepEquals(carsRetnedtoCustomer2, manager.getAllCustomerCars(customer2));
        assertFalse(customer3.getActive());

        try {
            manager.getAllCustomerCars(null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            manager.getAllCustomerCars(customerWithoutID);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void rentCarToCustomer() {
        assertTrue(car1.getAvailable());
        assertTrue(car2.getAvailable());
        assertTrue(car3.getAvailable());

        manager.rentCarToCustomer(car1, customer1, Date.valueOf("2012-03-21"), Date.valueOf("2012-03-31"));
        manager.rentCarToCustomer(car3, customer2, Date.valueOf("2012-03-15"), Date.valueOf("2012-03-27"));

        List<Car> carsRetnedtoCustomer1 = Arrays.asList(car1);
        List<Car> carsRetnedtoCustomer2 = Arrays.asList(car3);

        assertCarDeepEquals(carsRetnedtoCustomer1, manager.getAllCustomerCars(customer1));
        assertCarDeepEquals(carsRetnedtoCustomer2, manager.getAllCustomerCars(customer2));
        assertFalse(customer3.getActive());

        assertEquals(customer1, manager.findCustomerWithCar(car1));
        assertCustomerDeepEquals(customer1, manager.findCustomerWithCar(car1));
        assertTrue(car2.getAvailable());
        assertEquals(customer2, manager.findCustomerWithCar(car3));
        assertCustomerDeepEquals(customer2, manager.findCustomerWithCar(car3));

        try {
            manager.rentCarToCustomer(car1, customer3, Date.valueOf("2012-03-15"), Date.valueOf("2012-03-27"));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            manager.rentCarToCustomer(car1, customer1, Date.valueOf("2012-03-15"), Date.valueOf("2012-03-27"));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            manager.rentCarToCustomer(null, customer2, Date.valueOf("2012-03-15"), Date.valueOf("2012-03-27"));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            manager.rentCarToCustomer(carWithoutID, customer2, Date.valueOf("2012-03-15"), Date.valueOf("2012-03-27"));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            manager.rentCarToCustomer(carNotInDB, customer2, Date.valueOf("2012-03-15"), Date.valueOf("2012-03-27"));
            fail();
        } catch (TransactionException e) {
        }

        try {
            manager.rentCarToCustomer(car2, null, Date.valueOf("2012-03-15"), Date.valueOf("2012-03-27"));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            manager.rentCarToCustomer(car2, customerWithoutID, Date.valueOf("2012-03-15"), Date.valueOf("2012-03-27"));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            manager.rentCarToCustomer(car2, customerNotInDB, Date.valueOf("2012-03-15"), Date.valueOf("2012-03-27"));
            fail();
        } catch (TransactionException e) {
        }

        // Check that previous tests didn't affect data in database
        assertCarDeepEquals(carsRetnedtoCustomer1, manager.getAllCustomerCars(customer1));
        assertCarDeepEquals(carsRetnedtoCustomer2, manager.getAllCustomerCars(customer2));
        assertFalse(customer3.getActive());

        assertEquals(customer1, manager.findCustomerWithCar(car1));
        assertCustomerDeepEquals(customer1, manager.findCustomerWithCar(car1));
        assertTrue(car2.getAvailable());
        assertEquals(customer2, manager.findCustomerWithCar(car3));
        assertCustomerDeepEquals(customer2, manager.findCustomerWithCar(car3));
    }

    @Test
    public void getCarFromCustomer() {
        assertTrue(car1.getAvailable());
        assertTrue(car2.getAvailable());
        assertTrue(car3.getAvailable());

        manager.rentCarToCustomer(car1, customer1, Date.valueOf("2012-03-21"), Date.valueOf("2012-03-31"));
        manager.rentCarToCustomer(car2, customer1, Date.valueOf("2012-03-25"), Date.valueOf("2012-04-02"));
        manager.rentCarToCustomer(car3, customer2, Date.valueOf("2012-03-15"), Date.valueOf("2012-03-27"));

        assertEquals(customer1, manager.findCustomerWithCar(car1));
        assertCustomerDeepEquals(customer1, manager.findCustomerWithCar(car1));
        assertEquals(customer1, manager.findCustomerWithCar(car2));
        assertCustomerDeepEquals(customer1, manager.findCustomerWithCar(car2));
        assertEquals(customer2, manager.findCustomerWithCar(car3));
        assertCustomerDeepEquals(customer2, manager.findCustomerWithCar(car3));

        manager.getCarFromCustomer(car3, customer2);

        List<Car> carsRetnedtoCustomer1 = Arrays.asList(car1, car2);

        assertCarDeepEquals(carsRetnedtoCustomer1, manager.getAllCustomerCars(customer1));
        assertFalse(customer2.getActive());
        assertFalse(customer3.getActive());

        assertEquals(customer1, manager.findCustomerWithCar(car1));
        assertCustomerDeepEquals(customer1, manager.findCustomerWithCar(car1));
        assertEquals(customer1, manager.findCustomerWithCar(car2));
        assertCustomerDeepEquals(customer1, manager.findCustomerWithCar(car2));
        assertTrue(car3.getAvailable());

        try {
            manager.getCarFromCustomer(car3, customer1);
            fail();
        } catch (TransactionException e) {
        }

        try {
            manager.getCarFromCustomer(car3, customer2);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            manager.getCarFromCustomer(null, customer1);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            manager.getCarFromCustomer(carWithoutID, customer1);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            manager.getCarFromCustomer(carNotInDB, customer1);
            fail();
        } catch (TransactionException e) {
        }

        try {
            manager.getCarFromCustomer(car1, null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            manager.getCarFromCustomer(car1, customerWithoutID);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            manager.getCarFromCustomer(car1, customerNotInDB);
            fail();
        } catch (TransactionException e) {
        }

        // Check that previous tests didn't affect data in database
        assertCarDeepEquals(carsRetnedtoCustomer1, manager.getAllCustomerCars(customer1));
        assertFalse(customer2.getActive());
        assertFalse(customer3.getActive());

        assertEquals(customer1, manager.findCustomerWithCar(car1));
        assertCustomerDeepEquals(customer1, manager.findCustomerWithCar(car1));
        assertEquals(customer1, manager.findCustomerWithCar(car2));
        assertCustomerDeepEquals(customer1, manager.findCustomerWithCar(car2));
        assertTrue(car3.getAvailable());
    }

    private static Rent newRent(long carId, long customerId, Date rentDate, Date dueDate) {
        Rent rent = new Rent();

        rent.setCarID(carId);
        rent.setCustomerID(customerId);
        rent.setRentDate(rentDate);
        rent.setDueDate(dueDate);

        return rent;
    }
}
