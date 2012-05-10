package cz.muni.fi.pv168;

import java.sql.SQLException;
import java.util.*;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class CustomerManagerTest {

    private CustomerManager manager;
    private DataSource dataSource;

    private static DataSource prepareDataSource() throws SQLException {
        BasicDataSource ds = new BasicDataSource();
        ds.setUrl("jdbc:derby:memory:CarRentalDB;create=true");
        return ds;
    }

    @Before
    public void setUp() throws SQLException {
        dataSource = prepareDataSource();
        DBUtils.createTables(dataSource);
        manager = new CustomerManagerImplementation();
        manager.setDataSource(dataSource);
    }

    @After
    public void tearDown() throws SQLException {
        DBUtils.dropTables(dataSource);
    }

    public static Customer newCustomer(String firstName, String lastName, String address, String phoneNumber, String driversLicense) {
        Customer customer = new Customer();
        customer.setID(null);
        customer.setActive(false);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setAddress(address);
        customer.setPhoneNumber(phoneNumber);
        customer.setDriversLicense(driversLicense);
        return customer;
    }

    /**
     * Test of addCustomer method, of class CustomerManagerImplementation.
     */
    @Test
    public void testAddCustomer() {
        Customer customer = newCustomer("Vitalii", "Chepeliuk", "Komarov", "5-20-86", "AK 373979");
        manager.addCustomer(customer);
        Long ID = customer.getID();
        assertNotNull(ID);
        Customer result = manager.findCustomerByID(ID);
        assertEquals(customer, result);
        assertNotSame(customer, result);
        assertCustomerDeepEquals(customer, result);

        try {
            manager.addCustomer(null);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        customer = newCustomer("Juraj", "Kolchak", "Brno", "5-34-13", "AK 474854");
        customer.setID(new Long(50L));
        try {
            manager.addCustomer(customer);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        customer.setID(null);
        customer.setDriversLicense(null);
        try {
            manager.addCustomer(customer);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }

    /**
     * Test of removeCustomer method, of class CustomerManagerImplementation.
     */
    @Test
    public void testRemoveCustomer() {
        assertTrue(manager.getAllCustomers().isEmpty());

        Customer customer1 = newCustomer("Vitalii", "Chepeliuk", "Komarov", "5-20-86", "AK 373979");
        Customer customer2 = newCustomer("Juraj", "Kolchak", "Komarov", "5-34-86", "AK 372548");
        Customer customer3 = newCustomer("Martin", "Jirman", "Lazhot", "5-25-87", "AK 251245");
        manager.addCustomer(customer1);
        manager.addCustomer(customer2);
        manager.addCustomer(customer3);
        Long ID1 = customer1.getID();
        Long ID2 = customer2.getID();
        Long ID3 = customer3.getID();
        customer1 = manager.findCustomerByID(new Long(ID1));
        customer2 = manager.findCustomerByID(new Long(ID2));
        customer3 = manager.findCustomerByID(new Long(ID3));
        List<Customer> expResult = Arrays.asList(customer1, customer2);
        manager.removeCustomer(customer3);
        List<Customer> result = manager.getAllCustomers();
        assertCustomerDeepEquals(expResult, result);

        //update Customer
        manager.removeCustomer(customer2);
        customer2 = newCustomer("Vasylyna", "Chepelyuk", "Louny", "7-89-53", "AK 235689");
        manager.addCustomer(customer2);
        ID2 = customer2.getID();
        customer2 = manager.findCustomerByID(new Long(ID2));

        expResult = Arrays.asList(customer2);

        customer1 = newCustomer("Andrii", "Chepeliuk", "Komarov", "5-20-86", "AK 373979");
        customer1.setID(new Long(ID1));
        manager.updateCustomerInfo(customer1);
        manager.removeCustomer(customer1);
        result = manager.getAllCustomers();
        assertCustomerDeepEquals(expResult, result);
    }

    /**
     * Test of findCustomerByID method, of class CustomerManagerImplementation.
     */
    @Test
    public void testFindCustomerByID() {
        Customer expResult = newCustomer("Vitalii", "Chepeliuk", "Komarov", "5-20-86", "AK 373979");
        manager.addCustomer(expResult);
        Long ID = expResult.getID();
        Customer result = manager.findCustomerByID(ID);
        assertEquals(expResult, result);
        try {
            manager.findCustomerByID(null);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        result = manager.findCustomerByID(new Long(ID + 1));
        assertNull(result);
    }

    /**
     * Test of getAllCustomers method, of class CustomerManagerImplementation.
     */
    @Test
    public void testGetAllCustomers() {
        assertTrue(manager.getAllCustomers().isEmpty());

        Customer customer1 = newCustomer("Vitalii", "Chepeliuk", "Komarov", "5-20-86", "AK 373979");
        Customer customer2 = newCustomer("Juraj", "Kolchak", "Komarov", "5-34-86", "AK 372548");
        Customer customer3 = newCustomer("Martin", "Jirman", "Lazhot", "5-25-87", "AK 251245");
        Customer customer4 = newCustomer("Martin", "Pulec", "Brno", "5-11-24", "AK 897589");
        Customer customer5 = newCustomer("Lukas", "Rucka", "Brno", "5-21-06", "AK 256354");
        manager.addCustomer(customer1);
        manager.addCustomer(customer2);
        manager.addCustomer(customer3);
        manager.addCustomer(customer4);
        manager.addCustomer(customer5);
        List<Customer> expResult = Arrays.asList(customer1, customer2, customer3, customer4, customer5);
        List<Customer> result = manager.getAllCustomers();
        assertEquals(5, expResult.size());
        assertCustomerDeepEquals(expResult, result);

        // add new Customer
        Customer customer = newCustomer("Petr", "Adamek", "Brno", "4-35-47", "AK 125798");
        manager.addCustomer(customer);
        result = manager.getAllCustomers();
        expResult = Arrays.asList(customer, customer1, customer2, customer3, customer4, customer5);
        assertCustomerDeepEquals(expResult, result);

        //remove Customer
        Long ID = customer.getID();
        customer.setID(new Long(ID));
        customer.setAddress("new address");
        expResult = Arrays.asList(customer, customer1, customer2, customer3, customer4, customer5);
        // update Customer
        manager.updateCustomerInfo(customer);
        result = manager.getAllCustomers();
        assertCustomerDeepEquals(expResult, result);
    }

    /**
     * Test of updateCustomerInfo method, of class
     * CustomerManagerImplementation.
     */
    @Test
    public void testUpdateCustomerInfo() {
        Customer customer = newCustomer("Petr", "Adamek", "Brno", "4-35-47", "AK 125798");
        manager.addCustomer(customer);
        Long ID = customer.getID();
        customer.setAddress("new address");
        manager.updateCustomerInfo(customer);
        Customer result = manager.findCustomerByID(ID);
        assertCustomerDeepEquals(customer, result);

        customer = newCustomer("Hello", "World", "Google", "4-25-41", "AK 785428");
        customer.setID(new Long(ID + 1));

        try {
            manager.updateCustomerInfo(customer);
            fail();
        } catch (TransactionException ex) {
        }
    }

    /**
     * Test of getActiveCustomers method, of class
     * CustomerManagerImplementation.
     */
    @Test
    public void testGetActiveCustomers() {
        Customer customer1 = newCustomer("Vitalii", "Chepeliuk", "Komarov", "5-20-86", "AK 373979");
        Customer customer2 = newCustomer("Jutaj", "Kolchak", "Komarov", "5-34-86", "AK 372548");
        Customer customer3 = newCustomer("Martin", "Jirman", "Lazhot", "5-25-87", "AK 251245");
        Customer customer4 = newCustomer("Martin", "Pulec", "Brno", "5-11-24", "AK 897589");
        Customer customer5 = newCustomer("Lukas", "Rucka", "Brno", "5-21-06", "AK 256354");
        customer1.setActive(true);
        customer2.setActive(true);
        customer3.setActive(true);
        customer4.setActive(true);
        customer5.setActive(true);
        manager.addCustomer(customer1);
        manager.addCustomer(customer2);
        manager.addCustomer(customer3);
        manager.addCustomer(customer4);
        manager.addCustomer(customer5);

        List expResult = Arrays.asList(customer1, customer2, customer3, customer4, customer5);
        List result = manager.getActiveCustomers();
        assertEquals(expResult, result);
        assertCustomerDeepEquals(expResult, result);

        expResult = Arrays.asList(customer1, customer2, customer4, customer5);
        Long ID = customer3.getID();
        customer3.setActive(false);
        manager.updateCustomerInfo(customer3);
        result = manager.getActiveCustomers();
        assertCustomerDeepEquals(expResult, result);

        customer3.setActive(true);
        customer3.setID(new Long(ID));
        expResult = Arrays.asList(customer1, customer2, customer3, customer4, customer5);
        manager.updateCustomerInfo(customer3);
        result = manager.getActiveCustomers();
        assertCustomerDeepEquals(expResult, result);
    }

    public static void assertCustomerDeepEquals(Customer expected, Customer actual) {
        assertEquals(expected.getID(), actual.getID());
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getAddress(), actual.getAddress());
        assertEquals(expected.getPhoneNumber(), actual.getPhoneNumber());
        assertEquals(expected.getActive(), actual.getActive());
    }

    public static void assertCustomerDeepEquals(List<Customer> expected, List<Customer> actual) {
        assertEquals(expected.size(), actual.size());
        List<Customer> expectedSortedList = new ArrayList<>(expected);
        List<Customer> actualSortedList = new ArrayList<>(actual);
        Collections.sort(expectedSortedList, CustomerByIDComparator);
        Collections.sort(actualSortedList, CustomerByIDComparator);

        for (int i = 0; i < expectedSortedList.size(); i++) {
            assertCustomerDeepEquals(expectedSortedList.get(i), actualSortedList.get(i));
        }
    }
    private static Comparator<Customer> CustomerByIDComparator = new Comparator<Customer>() {

        @Override
        public int compare(Customer customer1, Customer customer2) {
            return Long.valueOf(customer1.getID()).compareTo(Long.valueOf(customer2.getID()));
        }
    };
}
