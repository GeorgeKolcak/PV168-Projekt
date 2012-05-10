package cz.muni.fi.pv168;

import java.sql.SQLException;
import java.util.*;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class CarManagerTest {

    private CarManager manager;
    private DataSource dataSource;

    @Before
    public void setUp() throws SQLException {
        dataSource = prepareDataSource();
        DBUtils.createTables(dataSource);
        manager = new CarManagerImplementation();
        manager.setDataSource(dataSource);
    }

    @After
    public void tearDown() throws SQLException {
        DBUtils.dropTables(dataSource);
    }

    @Test
    public void addCar() {
        Car car = newCar("Black", "0B6 6835", "Škoda", 200.0);

        manager.addCar(car);

        Long id = car.getID();
        assertNotNull(id);
        assertTrue(car.getAvailable());

        Car result = manager.findCarByID(id);
        assertEquals(car, result);
        assertNotSame(car, result);
        assertCarDeepEquals(car, result);
    }

    @Test
    public void findCarByID() {
        assertNull(manager.findCarByID(1l));
        Car car = newCar("Black", "0B6 6835", "Škoda", 200.0);
        manager.addCar(car);
        Long id = car.getID();

        Car result = manager.findCarByID(id);
        assertEquals(car, result);
        assertCarDeepEquals(car, result);
    }

    @Test
    public void addCarWithWrongAttributes() {
        try {
            manager.addCar(null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        Car car = newCar("Black", "0B6 6835", "Škoda", 200.0);
        car.setID(1l);
        try {
            manager.addCar(car);
            fail();
        } catch (IllegalArgumentException e) {
        }

        car = newCar(null, "0B6 6835", "Škoda", 200.0);
        try {
            manager.addCar(car);
            fail();
        } catch (IllegalArgumentException e) {
        }

        car = newCar("Black", null, "Škoda", 200.0);
        try {
            manager.addCar(car);
            fail();
        } catch (IllegalArgumentException e) {
        }

        car = newCar("Black", "0B6 6835", null, 200.0);
        try {
            manager.addCar(car);
            fail();
        } catch (IllegalArgumentException e) {
        }

        car = newCar("Black", "0B6 6835", "Škoda", (-1.0));
        try {
            manager.addCar(car);
            fail();
        } catch (IllegalArgumentException e) {
        }

        car = newCar("Black", "0B6 6835", "Škoda", 0.0);
        try {
            manager.addCar(car);

            Car result = manager.findCarByID(car.getID());
            assertNotNull(result);
        } catch (IllegalArgumentException e) {
            fail();
        }
    }

    @Test
    public void removeCar() {
        Car car1 = newCar("Black", "0B6 6835", "Škoda", 200.0);
        Car car2 = newCar("Red", "7B4 0044", "BMW", 500.0);

        manager.addCar(car1);
        manager.addCar(car2);

        assertNotNull(manager.findCarByID(car1.getID()));
        assertNotNull(manager.findCarByID(car2.getID()));

        manager.removeCar(car1);

        assertNull(manager.findCarByID(car1.getID()));
        assertNotNull(manager.findCarByID(car2.getID()));
    }

    @Test
    public void removeCarWithWrongAttributes() {
        Car car = newCar("Black", "0B6 6835", "Škoda", 200.0);

        try {
            manager.removeCar(null);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        try {
            car.setID(null);
            manager.removeCar(car);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        try {
            car.setID(1l);
            manager.removeCar(car);
            fail();
        } catch (TransactionException ex) {
        }
    }

    @Test
    public void updateCarInfo() {
        Car car1 = newCar("Black", "0B6 6835", "Škoda", 200.0);
        Car car2 = newCar("Red", "7B4 0044", "BMW", 500.0);

        manager.addCar(car1);
        manager.addCar(car2);
        Long id = car1.getID();

        car1 = manager.findCarByID(id);
        car1.setColor("White");
        manager.updateCarInfo(car1);
        assertEquals("White", car1.getColor());
        assertEquals("0B6 6835", car1.getLicensePlate());
        assertEquals("Škoda", car1.getModel());
        assertEquals(Double.valueOf(200.0), Double.valueOf(car1.getRentalPayment()));
        assertTrue(car1.getAvailable());

        car1 = manager.findCarByID(id);
        car1.setLicensePlate("8B5 0983");
        manager.updateCarInfo(car1);
        assertEquals("White", car1.getColor());
        assertEquals("8B5 0983", car1.getLicensePlate());
        assertEquals("Škoda", car1.getModel());
        assertEquals(Double.valueOf(200.0), Double.valueOf(car1.getRentalPayment()));
        assertTrue(car1.getAvailable());

        car1 = manager.findCarByID(id);
        car1.setModel("Volkswagen");
        manager.updateCarInfo(car1);
        assertEquals("White", car1.getColor());
        assertEquals("8B5 0983", car1.getLicensePlate());
        assertEquals("Volkswagen", car1.getModel());
        assertEquals(Double.valueOf(200.0), Double.valueOf(car1.getRentalPayment()));
        assertTrue(car1.getAvailable());

        car1 = manager.findCarByID(id);
        car1.setRentalPayment(300.0);
        manager.updateCarInfo(car1);
        assertEquals("White", car1.getColor());
        assertEquals("8B5 0983", car1.getLicensePlate());
        assertEquals("Volkswagen", car1.getModel());
        assertEquals(Double.valueOf(300.0), Double.valueOf(car1.getRentalPayment()));
        assertTrue(car1.getAvailable());

        car1 = manager.findCarByID(id);
        car1.setRentalPayment(0.0);
        manager.updateCarInfo(car1);
        assertEquals("White", car1.getColor());
        assertEquals("8B5 0983", car1.getLicensePlate());
        assertEquals("Volkswagen", car1.getModel());
        assertEquals(Double.valueOf(0.0), Double.valueOf(car1.getRentalPayment()));
        assertTrue(car1.getAvailable());

        car1 = manager.findCarByID(id);
        car1.setStatus(false);
        manager.updateCarInfo(car1);
        assertEquals("White", car1.getColor());
        assertEquals("8B5 0983", car1.getLicensePlate());
        assertEquals("Volkswagen", car1.getModel());
        assertEquals(Double.valueOf(0.0), Double.valueOf(car1.getRentalPayment()));
        assertFalse(car1.getAvailable());

        assertCarDeepEquals(car2, manager.findCarByID(car2.getID()));
    }

    @Test
    public void updateCarInfoWithWrongAttributes() {
        Car car = newCar("Black", "0B6 6835", "Škoda", 200.0);

        manager.addCar(car);
        Long id = car.getID();

        try {
            manager.updateCarInfo(null);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        try {
            car = manager.findCarByID(id);
            car.setID(null);
            manager.updateCarInfo(car);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        try {
            car = manager.findCarByID(id);
            car.setID(id - 1);
            manager.updateCarInfo(car);
            fail();
        } catch (TransactionException ex) {
        }

        try {
            car = manager.findCarByID(id);
            car.setColor(null);
            manager.updateCarInfo(car);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        try {
            car = manager.findCarByID(id);
            car.setLicensePlate(null);
            manager.updateCarInfo(car);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        try {
            car = manager.findCarByID(id);
            car.setModel(null);
            manager.updateCarInfo(car);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        try {
            car = manager.findCarByID(id);
            car.setRentalPayment(-1.0);
            manager.updateCarInfo(car);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }

    @Test
    public void getAllCars() {
        assertTrue(manager.getAllCars().isEmpty());

        Car car1 = newCar("Black", "0B6 6835", "Škoda", 200.0);
        Car car2 = newCar("Red", "7B4 0044", "BMW", 500.0);

        manager.addCar(car1);
        manager.addCar(car2);

        List<Car> expected = Arrays.asList(car1, car2);
        List<Car> actual = manager.getAllCars();

        assertCarDeepEquals(expected, actual);
    }

    @Test
    public void findAllAvailableCars() {
        assertTrue(manager.getAvailableCars().isEmpty());

        Car car1 = newCar("Black", "0B6 6835", "Škoda", 200.0);
        Car car2 = newCar("Red", "7B4 0044", "BMW", 500.0);

        manager.addCar(car1);
        manager.addCar(car2);

        List<Car> expected = Arrays.asList(car1, car2);
        List<Car> actual = manager.getAvailableCars();

        assertCarDeepEquals(expected, actual);

        car1.setStatus(false);
        manager.updateCarInfo(car1);

        expected = Arrays.asList(car2);
        actual = manager.getAvailableCars();

        assertCarDeepEquals(expected, actual);

        car2.setStatus(false);
        manager.updateCarInfo(car2);

        assertTrue(manager.getAvailableCars().isEmpty());
    }

    public static Car newCar(String colour, String licensePlate, String model, double payment) {
        Car car = new Car();
        car.setColor(colour);
        car.setLicensePlate(licensePlate);
        car.setModel(model);
        car.setRentalPayment(payment);
        car.setStatus(true);

        return car;
    }

    public static void assertCarDeepEquals(Car expected, Car actual) {
        assertEquals(expected.getID(), actual.getID());
        assertEquals(expected.getColor(), actual.getColor());
        assertEquals(expected.getLicensePlate(), actual.getLicensePlate());
        assertEquals(expected.getModel(), actual.getModel());
        assertEquals(expected.getRentalPayment(), actual.getRentalPayment());
        assertEquals(expected.getAvailable(), actual.getAvailable());
    }

    public static void assertCarDeepEquals(List<Car> expected, List<Car> actual) {
        assertEquals(expected.size(), actual.size());
        List<Car> expectedSortedList = new ArrayList<>(expected);
        List<Car> actualSortedList = new ArrayList<>(actual);
        Collections.sort(expectedSortedList, carByIDComparator);
        Collections.sort(actualSortedList, carByIDComparator);

        for (int i = 0; i < actualSortedList.size(); i++) {
            assertCarDeepEquals(expectedSortedList.get(i), actualSortedList.get(i));
        }
    }
    private static Comparator<Car> carByIDComparator = new Comparator<Car>() {

        @Override
        public int compare(Car car1, Car car2) {
            return Long.valueOf(car1.getID()).compareTo(Long.valueOf(car2.getID()));
        }
    };

    private static DataSource prepareDataSource() throws SQLException {
        BasicDataSource ds = new BasicDataSource();
        ds.setUrl("jdbc:derby:memory:CarRentalDB;create=true");
        return ds;
    }
}
