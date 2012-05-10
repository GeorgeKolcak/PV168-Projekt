package cz.muni.fi.pv168;

import java.sql.Date;
import java.util.List;

public interface CarRentalManager {

    public Customer findCustomerWithCar(Car car) throws IllegalArgumentException, TransactionException;

    public List<Car> getAllCustomerCars(Customer customer) throws IllegalArgumentException, TransactionException;

    public void rentCarToCustomer(Car car, Customer customer, Date rentDate, Date dueDate) throws IllegalArgumentException, TransactionException;

    public void getCarFromCustomer(Car car, Customer customer) throws IllegalArgumentException, TransactionException;
}
