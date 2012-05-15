package cz.muni.fi.pv168;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.apache.commons.dbcp.BasicDataSource;

public class MainForm extends javax.swing.JFrame {

    private DataSource prepareDataSource() {
        /*BasicDataSource ds = new BasicDataSource();
        Properties properties = new Properties();
        FileInputStream in = null;
        try {
            in = new FileInputStream("database.properties");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, "File Not Found", ex);
        }
        try {
            properties.load(in);
        } catch (IOException ex) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, "Can Not Read Properties File", ex);
        }
        String drivers = properties.getProperty("jdbc.drivers");
        if (null == drivers) {
            System.setProperty("jdbs.drivers", drivers);
        }
        String url = properties.getProperty("jdbc.url");
        String username = properties.getProperty("jdbc.username");
        String password = properties.getProperty("jdbc.password");
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        return ds;*/
        
        ResourceBundle databaseProperties = ResourceBundle.getBundle("cz.muni.fi.pv168.database");
        
        String url = databaseProperties.getString("jdbc.url");
        String username = databaseProperties.getString("jdbc.username");
        String password = databaseProperties.getString("jdbc.password");
        
        BasicDataSource ds = new BasicDataSource();
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        
        return ds;
    }

    private class CustomerSwingWorker extends SwingWorker<List<Customer>, Void> {

        @Override
        protected List<Customer> doInBackground() throws Exception {
            switch (customersAction) {
                case ADD_CUSTOMER:
                    NewCustomerForm customerForm = new NewCustomerForm();
                    customerForm.setVisible(true);
                    // Get Data From Form!!!
                    //String addFirstName = TextField.getText();
                    //String addLastName = TextField.getText();
                    //String addAddress = TextField.getText();
                    //String addPhoneNumber = TextField.getText();
                    //String addDriversLicense = TextField.getText();
                    Customer addCustomer = new Customer();
                    addCustomer.setFirstName("Put From Form");
                    addCustomer.setLastName("Put From Form");
                    addCustomer.setAddress("Put From Form");
                    addCustomer.setPhoneNumber("Put From Form");
                    addCustomer.setDriversLicense("Put From Form");
                    addCustomer.setActive(Boolean.FALSE);

                    try {
                        customerManager.addCustomer(addCustomer);
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("CAN_NOT_ADD_CUSTOMER"),
                                localization.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                    return customerManager.getAllCustomers();

                case EDIT_CUSTOMER:
                    // Exact same!!!
                    //String editFirstName = TextField.getText();
                    //String editLastName = TextField.getText();
                    //String editAddress = TextField.getText();
                    //String editPhoneNumber = TextField.getText();
                    //String editDriversLicense = TextField.getText();
                    Customer editCustomer = new Customer();
                    editCustomer.setID(Long.parseLong(""/*
                             * Label.getText()
                             */));
                    editCustomer.setFirstName("Put From Form");
                    editCustomer.setLastName("Put From Form");
                    editCustomer.setAddress("Put From Form");
                    editCustomer.setPhoneNumber("Put From Form");
                    editCustomer.setDriversLicense("Put From Form");
                    editCustomer.setActive(Boolean.FALSE/*
                             * Label.getText().equals("true")? true: false
                             */);

                    try {
                        customerManager.updateCustomerInfo(editCustomer);
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("CAN_NOT_EDIT_CUSTOMER"),
                                localization.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }

                    return customerManager.getAllCustomers();
                case REMOVE_CUSTOMER:
                    if (-1 != customerTable.getSelectedRow()) {
                        Customer removeCustomer = new Customer();
                        removeCustomer.setID((Long) customerTable.getValueAt(
                                customerTable.getSelectedRow(), 0));
                        removeCustomer.setFirstName((String) customerTable.getValueAt(
                                customerTable.getSelectedRow(), 1));
                        removeCustomer.setLastName((String) customerTable.getValueAt(
                                customerTable.getSelectedRow(), 2));
                        removeCustomer.setAddress((String) customerTable.getValueAt(
                                customerTable.getSelectedRow(), 3));
                        removeCustomer.setPhoneNumber((String) customerTable.getValueAt(
                                customerTable.getSelectedRow(), 4));
                        removeCustomer.setDriversLicense((String) customerTable.getValueAt(
                                customerTable.getSelectedRow(), 5));
                        removeCustomer.setActive((Boolean) customerTable.getValueAt(
                                customerTable.getSelectedRow(), 6));

                        customerManager.removeCustomer(removeCustomer);
                        return customerManager.getAllCustomers();
                    } else {
                        return null;
                    }
                case FIND_CUSTOMER_BY_ID:
                    String stringID = JOptionPane.showInputDialog(null, localization.getString("ENTER_CUSTOMER_ID")
                            + ":", localization.getString("FIND"), JOptionPane.QUESTION_MESSAGE);
                    Long ID = -1L;
                    if (null == stringID) {
                        return null;
                    }

                    try {
                        ID = Long.parseLong(stringID);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("WRONG_NUMBER_INPUT"));
                        return null;
                    }
                    Customer foundCustomer = null;
                    try {
                        foundCustomer = customerManager.findCustomerByID(ID);
                        if (null == foundCustomer) {
                            JOptionPane.showMessageDialog(rootPane, localization.getString("WRONG_CUSTOMER_ID"),
                                    localization.getString("NOT_FOUND"), JOptionPane.INFORMATION_MESSAGE);
                            return null;
                        }
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("WRONG_CUSTOMER_ID"),
                                localization.getString("NOT_FOUND"), JOptionPane.INFORMATION_MESSAGE);
                        return null;
                    }

                    List<Customer> foundList = new ArrayList<>();
                    foundList.add(foundCustomer);
                    return foundList;
                case GET_ALL_CUSTOMERS:
                    List<Customer> allCustomers = null;
                    try {
                        allCustomers = customerManager.getAllCustomers();
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("ERROR_DB"),
                                localization.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    } catch (RuntimeException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("ERROR_DB"),
                                localization.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                    return allCustomers;
                default:
                    throw new IllegalStateException("Default reached in doInBackground() in CustomerSwingWorker");
            }
        }

        @Override
        protected void done() {
            // Set Buttons or Form Enabled Method!!!
            try {
                if (null == get()) {
                    customersAction = null;
                    return;
                }
            } catch (ExecutionException ex) {
                customersAction = null;
                JOptionPane.showMessageDialog(rootPane, ex.getCause().getMessage(),
                        ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            } catch (InterruptedException ex) {
                customersAction = null;
                throw new IllegalStateException(localization.getString("INTERRUPTED"));
            }
            switch (customersAction) {
                case ADD_CUSTOMER:
                    CustomersTableModel addModel = (CustomersTableModel) customerTable.getModel();
                    try {
                        addModel.updateCustomers(get());
                    } catch (ExecutionException ex) {
                        JOptionPane.showMessageDialog(rootPane, ex.getCause().getMessage(),
                                ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(localization.getString("INTERRUPTED"));
                    }
                    break;
                case EDIT_CUSTOMER:
                    CustomersTableModel editModel = (CustomersTableModel) customerTable.getModel();
                    try {
                        editModel.updateCustomers(get());
                    } catch (ExecutionException ex) {
                        JOptionPane.showMessageDialog(rootPane, ex.getCause().getMessage(),
                                ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(localization.getString("INTERRUPTED"));
                    }
                    break;

                case REMOVE_CUSTOMER:
                    CustomersTableModel removeModel = (CustomersTableModel) customerTable.getModel();
                    try {
                        removeModel.updateCustomers(get());
                    } catch (ExecutionException ex) {
                        JOptionPane.showMessageDialog(rootPane, ex.getCause().getMessage(),
                                ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(localization.getString("INTERRUPTED"));
                    }
                    break;

                case FIND_CUSTOMER_BY_ID:
                    CustomersTableModel foundModel = (CustomersTableModel) customerTable.getModel();
                    try {
                        foundModel.updateCustomers(get());
                    } catch (ExecutionException ex) {
                        JOptionPane.showMessageDialog(rootPane, ex.getCause().getMessage(),
                                ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(localization.getString("INTERRUPTED"));
                    }
                    break;
                case GET_ALL_CUSTOMERS:
                    CustomersTableModel allModel = (CustomersTableModel) customerTable.getModel();
                    try {
                        allModel.updateCustomers(get());
                    } catch (ExecutionException ex) {
                        JOptionPane.showMessageDialog(rootPane, ex.getCause().getMessage(),
                                ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(localization.getString("INTERRUPTED"));
                    }
                    break;
                default:
                    throw new IllegalStateException("Default reached in done() in CustomerSwingWorker");
            }
            customersAction = null;
        }
    }

    private class CarSwingWorker extends SwingWorker<List<Car>, Void> {

        @Override
        protected List<Car> doInBackground() throws Exception {
            switch (carsAction) {
                case ADD_CAR:
                    // Get Data From Form!!!
                    //String addModel = TextField.getText();
                    //String addColor = TextField.getText();
                    //String addLicensePlate = TextField.getText();
                    Car addCar = new Car();
                    addCar.setModel("Put From Form");
                    addCar.setColor("Put From Form");
                    addCar.setLicensePlate("Put From Form");
                    addCar.setStatus(Boolean.TRUE);
                    try {
                        carManager.addCar(addCar);

                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("CAN_NOT_ADD_CAR"),
                                localization.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                case EDIT_CAR:
                    Car editCar = null;
                    Long ID = Long.parseLong(""/*
                             * Label.getText
                             */);
                    // Get Data From Form!!!
                    //String editModel = TextField.getText();
                    //String editColor = TextField.getText();
                    //String editLicensePlate = TextField.getText();
                    editCar.setID(ID);
                    editCar.setModel("Put From Form");
                    editCar.setColor("Put From Form");
                    editCar.setLicensePlate("Put From Form");
                    editCar.setRentalPayment(0.0/*
                             * Put From Form
                             */);
                    try {
                        carManager.updateCarInfo(editCar);
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("CAN_NOT_UPDATE_CAR"),
                                localization.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                    return carManager.getAllCars();

                case REMOVE_CAR:
                    if (-1 != carTable.getSelectedRow()) {
                        Car removeCar = new Car();
                        Long removeID = ((Long) carTable.getValueAt(carTable.getSelectedRow(), 0));
                        removeCar.setID(removeID);
                        removeCar.setModel((String) carTable.getValueAt(
                                carTable.getSelectedRow(), 1));
                        removeCar.setColor((String) carTable.getValueAt(
                                carTable.getSelectedRow(), 2));
                        removeCar.setLicensePlate((String) carTable.getValueAt(
                                carTable.getSelectedRow(), 3));
                        removeCar.setRentalPayment((Double) carTable.getValueAt(
                                carTable.getSelectedRow(), 4));
                        try {
                            carManager.removeCar(removeCar);
                        } catch (IllegalArgumentException ex) {
                            JOptionPane.showMessageDialog(rootPane, localization.getString("CAN_NOT_REMOVE_CAR"),
                                    localization.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                        }
                        return carManager.getAllCars();
                    } else {
                        return null;
                    }
                case FIND_CAR_BY_ID:
                    String stringID = JOptionPane.showInputDialog(null, localization.getString("ENTER_CAR_ID"), localization.getString("FIND"),
                            JOptionPane.QUESTION_MESSAGE);
                    Long foundID = -1L;
                    if (null == stringID) {
                        return null;
                    }
                    try {
                        ID = Long.parseLong(stringID);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("WRONG_NUMBER_INPUT"),
                                localization.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                    Car foundCar = null;
                    try {
                        foundCar = carManager.findCarByID(ID);
                        if (null == foundCar) {
                            JOptionPane.showMessageDialog(rootPane, localization.getString("WRONG_CAR_ID"),
                                    localization.getString("NOT_FOUND"), JOptionPane.INFORMATION_MESSAGE);
                            return null;
                        }
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("WRONG_CAR_ID"),
                                localization.getString("NOT_FOUND"), JOptionPane.INFORMATION_MESSAGE);
                        return null;
                    }
                    List<Car> foundList = new ArrayList<>();
                    foundList.add(foundCar);
                    return foundList;
                case GET_ALL_AVAILABLE_CARS:
                    try {
                        return carManager.getAvailableCars();
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("ERROR_DB"),
                                localization.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                case GET_ALL_CARS:
                    try {
                        return carManager.getAllCars();
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("ERROR_DB"),
                                localization.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    } catch (RuntimeException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("ERROR_DB"),
                                localization.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                default:
                    throw new IllegalStateException("Default reached in doInBackground() in CarSwingWorker");
            }
        }

        @Override
        protected void done() {
            try {
                if (null == get()) {
                    carsAction = null;
                    return;
                }
            } catch (ExecutionException ex) {
                carsAction = null;
                JOptionPane.showMessageDialog(rootPane,
                        ex.getCause().getMessage(), ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                return;
            } catch (InterruptedException ex) {
                carsAction = null;
                throw new IllegalStateException(localization.getString("INTERRUPTED"));
            }
            switch (carsAction) {
                case ADD_CAR:
                    CarsTableModel addModel = (CarsTableModel) carTable.getModel();
                    try {
                        addModel.updateCars(get());
                    } catch (ExecutionException ex) {
                        JOptionPane.showMessageDialog(rootPane,
                                ex.getCause().getMessage(), ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(localization.getString("INTERRUPTED"));
                    }
                    break;
                case EDIT_CAR:
                    CarsTableModel editModel = (CarsTableModel) carTable.getModel();
                    try {
                        editModel.updateCars(get());
                    } catch (ExecutionException ex) {
                        JOptionPane.showMessageDialog(rootPane,
                                ex.getCause().getMessage(), ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(localization.getString("INTERRUPTED"));
                    }
                    break;
                case REMOVE_CAR:
                    CarsTableModel removeModel = (CarsTableModel) carTable.getModel();
                    try {
                        removeModel.updateCars(get());
                    } catch (ExecutionException ex) {
                        JOptionPane.showMessageDialog(rootPane,
                                ex.getCause().getMessage(), ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(localization.getString("INTERRUPTED"));
                    }
                    break;
                case GET_ALL_AVAILABLE_CARS:
                    CarsTableModel allAvailableCars = (CarsTableModel) carTable.getModel();
                    try {
                        allAvailableCars.updateCars(get());
                    } catch (ExecutionException ex) {
                        JOptionPane.showMessageDialog(rootPane,
                                ex.getCause().getMessage(), ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(localization.getString("INTERRUPTED"));
                    }
                    break;
                case GET_ALL_CARS:
                    CarsTableModel allCars = (CarsTableModel) carTable.getModel();
                    try {
                        allCars.updateCars(get());
                    } catch (ExecutionException ex) {
                        JOptionPane.showMessageDialog(rootPane,
                                ex.getCause().getMessage(), ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(localization.getString("INTERRUPTED"));
                    }
                    break;
                default:
                    throw new IllegalStateException("Default reached in done() in CarSwingWorker");
            }
            carsAction = null;
        }
    }

    private class RentalSwingWorker extends SwingWorker<List<Rent>, Void> {

        @Override
        protected List<Rent> doInBackground() throws Exception {
            switch (rentsAction) {
                case ADD_RENT:
                    Long addRentCustomerID = Long.parseLong("Put From Text");
                    Car addRentCar = null;
                    try {
                        // 
                        addRentCar = carManager.findCarByID(1L/*
                                 * Text
                                 */);
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("CAN_NOT_RENT_CAR"),
                                localization.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }

                    Customer addRentCustomer = null;
                    try {
                        addRentCustomer = customerManager.findCustomerByID(addRentCustomerID);
                        rentManager.rentCarToCustomer(addRentCar, addRentCustomer, null/*
                                 * Date From Calendar
                                 */, null/*
                                 * Date From Calendar
                                 */);
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("CAN_NOT_ADD_RENT"),
                                localization.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                    return rentManager.getAllRents();
                case REMOVE_RENT:
                    if (-1 != rentTable.getSelectedRow()) {
                        Long removeRentCustomerID = (Long) (rentTable.getValueAt(
                                rentTable.getSelectedRow(), 0));
                        Long removeRentCarID = (Long) rentTable.getValueAt(
                                rentTable.getSelectedRow(), 1);
                        Customer removeRentCustomer = null;
                        try {
                            removeRentCustomer = customerManager.findCustomerByID(removeRentCustomerID);
                        } catch (IllegalArgumentException ex) {
                            JOptionPane.showMessageDialog(rootPane, localization.getString("CAN_NOT_FIND_CUSTOMER"),
                                    localization.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                            return null;
                        }
                        Car removeRentCar = null;
                        try {
                            removeRentCar = carManager.findCarByID(removeRentCarID);
                        } catch (IllegalArgumentException ex) {
                            JOptionPane.showMessageDialog(rootPane, localization.getString("CAN_NOT_FIND_CAR"),
                                    localization.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                            return null;
                        }
                        try {
                            rentManager.getCarFromCustomer(removeRentCar, removeRentCustomer);
                        } catch (IllegalArgumentException ex) {
                            JOptionPane.showMessageDialog(rootPane, localization.getString("ERROR_REMOVE_RENT"),
                                    localization.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                            return null;
                        }
                        return rentManager.getAllRents();
                    } else {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("CHOOSE_CAR"),
                                localization.getString("ERROR"), JOptionPane.INFORMATION_MESSAGE);
                        return null;
                    }
                case GET_CUSTOMER_CARS:
                    String stringID = JOptionPane.showInputDialog(null, localization.getString("ENTER_CUSTOMER_ID"), localization.getString("FIND"),
                            JOptionPane.QUESTION_MESSAGE);
                    if (stringID == null) {
                        return null;
                    }
                    Long customerID;
                    try {
                        customerID = Long.parseLong(stringID);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("WRONG_NUMBER_INPUT"),
                                localization.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                    Customer customer = customerManager.findCustomerByID(customerID);
                    if (customer == null) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("CUSTOMER_ID_NOT_EXIST"),
                                localization.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }

                    List<Rent> allCustomerCars = new ArrayList<>();
                    for (Car car : rentManager.getAllCustomerCars(customer)) {
                        Rent rent = rentManager.findRentWithCar(car);
                        allCustomerCars.add(rent);
                    }
                    return allCustomerCars;

                case GET_CUSTOMER_WITH_CAR:
                    String string = JOptionPane.showInputDialog(null, localization.getString("ENTER_CUSTOMER_ID"),
                            localization.getString("FIND"), JOptionPane.QUESTION_MESSAGE);
                    if (null == string) {
                        return null;
                    }
                    Long ID;
                    try {
                        ID = Long.parseLong(string);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("WRONG_NUMBER_INPUT"),
                                localization.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                    Car car = carManager.findCarByID(ID);
                    if (null == car) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("CAR_ID_NOT_EXIST"),
                                localization.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                    if (car.getAvailable() == true || rentManager.findCustomerWithCar(car) == null) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("CAR_NOT_RENTED"),
                                localization.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }

                    List<Rent> rentFoundCustomer = new ArrayList<>();
                    rentFoundCustomer.add(rentManager.findRentWithCar(car));

                    return rentFoundCustomer;

                case GET_ALL_RENTS:
                    try {
                        return rentManager.getAllRents();
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("ERROR_DB"),
                                localization.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    } catch (RuntimeException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("ERROR_DB"),
                                localization.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                default:
                    throw new IllegalStateException("Default reached in doInBackground() RentalSwingWorker");
            }
        }

        @Override
        protected void done() {
            try {
                if (null == get()) {
                    rentsAction = null;
                    return;
                }
            } catch (ExecutionException ex) {
                JOptionPane.showMessageDialog(rootPane,
                        ex.getCause().getMessage(), ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                rentsAction = null;
                return;
            } catch (InterruptedException ex) {
                rentsAction = null;
                throw new IllegalStateException(localization.getString("INTERRUPTED"));
            }
            switch (rentsAction) {
                case ADD_RENT:
                    RentsTableModel addModel = (RentsTableModel) rentTable.getModel();
                    try {
                        addModel.updateRents(get());
                    } catch (ExecutionException ex) {
                        JOptionPane.showMessageDialog(rootPane,
                                ex.getCause().getMessage(), ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(localization.getString("INTERRUPTED"));
                    }
                    // update 
                    customersAction = CustomersActions.GET_ALL_CUSTOMERS;
                    CustomerSwingWorker customerSwingWorker = new CustomerSwingWorker();
                    customerSwingWorker.execute();
                    break;
                case REMOVE_RENT:
                    RentsTableModel removeModel = (RentsTableModel) rentTable.getModel();
                    try {
                        removeModel.updateRents(get());
                    } catch (ExecutionException ex) {
                        JOptionPane.showMessageDialog(rootPane,
                                ex.getCause().getMessage(), ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(localization.getString("INTERRUPTED"));
                    }
                    break;
                case GET_CUSTOMER_WITH_CAR:
                    RentsTableModel customerWithCarModel = (RentsTableModel) rentTable.getModel();
                    try {
                        customerWithCarModel.updateRents(get());
                    } catch (ExecutionException ex) {
                        JOptionPane.showMessageDialog(rootPane,
                                ex.getCause().getMessage(), ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(localization.getString("INTERRUPTED"));
                    }
                    break;
                case GET_ALL_RENTS:
                    RentsTableModel allModel = (RentsTableModel) rentTable.getModel();
                    try {
                        allModel.updateRents(get());
                    } catch (ExecutionException ex) {
                        JOptionPane.showMessageDialog(rootPane,
                                ex.getCause().getMessage(), ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(localization.getString("interrupted"));
                    }
                    break;
                default:
                    throw new IllegalStateException("default reached in done() RentalSwingWorker");
            }
            rentsAction = null;
        }
    }

    public MainForm() {
        initComponents();
        carManager.setDataSource(dataSource);
        customerManager.setDataSource(dataSource);
        rentManager.setDataSource(dataSource);
        FileOutputStream fs = null;
        try {
            fs = new FileOutputStream("main.log", true);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        carManager.setLogger(fs);
        customerManager.setLogger(fs);
        rentManager.setLogger(fs);

        jTabbedPane1.setTitleAt(0, localization.getString("cars"));
        jTabbedPane1.setTitleAt(1, localization.getString("customers"));
        jTabbedPane1.setTitleAt(2, localization.getString("rent"));
        
        carTable.setModel(new CarsTableModel(localization));
        customerTable.setModel(new CustomersTableModel(localization));
        rentTable.setModel(new RentsTableModel(localization));
        
        jMenuItem3.setAction(new ExitAction(localization.getString("exit")));
        jMenuItem7.setAction(new AddRentAction(localization.getString("rent")));
        jMenuItem8.setAction(new AddCarAction(localization.getString("car")));
        jMenuItem18.setAction(new AddCustomerAction(localization.getString("customer")));
        
        jButton2.setAction(new AddCarAction(localization.getString("new_car")));
        jButton3.setAction(new AddCustomerAction(localization.getString("new_customer")));
        jButton4.setAction(new AddRentAction(localization.getString("new_rent")));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuItem6 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jDesktopPane1 = new javax.swing.JDesktopPane();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        carTable = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        customerTable = new javax.swing.JTable();
        jScrollPane4 = new javax.swing.JScrollPane();
        rentTable = new javax.swing.JTable();
        jProgressBar1 = new javax.swing.JProgressBar();
        mainToolBar = new javax.swing.JToolBar();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jTextField2 = new javax.swing.JTextField();
        jButton9 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jToolBar1 = new javax.swing.JToolBar();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem11 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenuItem18 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenu5 = new javax.swing.JMenu();
        jMenuItem14 = new javax.swing.JMenuItem();
        jMenuItem13 = new javax.swing.JMenuItem();
        jMenuItem12 = new javax.swing.JMenuItem();
        jMenuItem16 = new javax.swing.JMenuItem();
        jMenuItem17 = new javax.swing.JMenuItem();
        jMenuItem15 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenuItem9 = new javax.swing.JMenuItem();
        jMenuItem10 = new javax.swing.JMenuItem();

        jMenuItem6.setText("jMenuItem6");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Car Rental Manager");

        jDesktopPane1.setPreferredSize(new java.awt.Dimension(1000, 660));

        jTabbedPane1.setName("tabPanel");
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(985, 600));

        carTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Model", "Colour", "License Plate", "Price", "Available"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Long.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Double.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, true, true, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        carTable.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        carTable.setRequestFocusEnabled(false);
        carTable.setSelectionBackground(new java.awt.Color(153, 102, 255));
        jScrollPane2.setViewportView(carTable);

        jTabbedPane1.addTab("Cars", jScrollPane2);

        customerTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "First Name", "Surname", "Address", "Telephone", "Driver's License", "Active"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Long.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, true, true, true, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        customerTable.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        customerTable.setRequestFocusEnabled(false);
        customerTable.setSelectionBackground(new java.awt.Color(153, 102, 255));
        jScrollPane3.setViewportView(customerTable);

        jTabbedPane1.addTab("Customers", jScrollPane3);

        rentTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID", "Car ID", "Customer ID", "Lease Date", "Due Date"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Long.class, java.lang.Long.class, java.lang.Long.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        rentTable.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        rentTable.setRequestFocusEnabled(false);
        rentTable.setSelectionBackground(new java.awt.Color(153, 102, 255));
        jScrollPane4.setViewportView(rentTable);

        jTabbedPane1.addTab("Car Rents", jScrollPane4);

        jTabbedPane1.setBounds(0, 25, 985, 600);
        jDesktopPane1.add(jTabbedPane1, javax.swing.JLayeredPane.DEFAULT_LAYER);

        jProgressBar1.setPreferredSize(new java.awt.Dimension(200, 15));
        jProgressBar1.setRequestFocusEnabled(false);
        jProgressBar1.setBounds(50, 635, 200, 15);
        jDesktopPane1.add(jProgressBar1, javax.swing.JLayeredPane.DEFAULT_LAYER);

        mainToolBar.setFloatable(false);
        mainToolBar.setRollover(true);
        mainToolBar.setMaximumSize(new java.awt.Dimension(100000, 100000));
        mainToolBar.setPreferredSize(new java.awt.Dimension(985, 25));

        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setLabel("New Car");
        jButton2.setName("");
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        mainToolBar.add(jButton2);

        jButton3.setText("New Customer");
        jButton3.setFocusable(false);
        jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton3.setName("");
        jButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        mainToolBar.add(jButton3);

        jButton4.setText("New Lease");
        jButton4.setFocusable(false);
        jButton4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton4.setName("");
        jButton4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        mainToolBar.add(jButton4);

        jSeparator3.setMinimumSize(new java.awt.Dimension(200, 0));
        jSeparator3.setPreferredSize(new java.awt.Dimension(200, 0));
        jSeparator3.setSeparatorSize(new java.awt.Dimension(200, 0));
        mainToolBar.add(jSeparator3);

        jButton5.setText("Copy");
        jButton5.setFocusable(false);
        jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton5.setName("");
        jButton5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        mainToolBar.add(jButton5);

        jButton6.setText("Cut");
        jButton6.setFocusable(false);
        jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton6.setName("");
        jButton6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        mainToolBar.add(jButton6);

        jButton7.setText("Paste");
        jButton7.setFocusable(false);
        jButton7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton7.setName("");
        jButton7.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        mainToolBar.add(jButton7);

        jSeparator2.setMinimumSize(new java.awt.Dimension(200, 0));
        jSeparator2.setPreferredSize(new java.awt.Dimension(200, 0));
        jSeparator2.setSeparatorSize(new java.awt.Dimension(200, 0));
        mainToolBar.add(jSeparator2);

        jTextField2.setText("Search");
        jTextField2.setSelectionColor(new java.awt.Color(153, 102, 255));
        mainToolBar.add(jTextField2);

        jButton9.setText("Search");
        jButton9.setFocusable(false);
        jButton9.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton9.setName("");
        jButton9.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });
        mainToolBar.add(jButton9);

        jButton8.setText("Sort");
        jButton8.setFocusable(false);
        jButton8.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton8.setName("");
        jButton8.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        mainToolBar.add(jButton8);

        mainToolBar.setBounds(0, 0, 985, 25);
        jDesktopPane1.add(mainToolBar, javax.swing.JLayeredPane.PALETTE_LAYER);

        jButton1.setText("Commit");
        jButton1.setMaximumSize(new java.awt.Dimension(80, 23));
        jButton1.setMinimumSize(new java.awt.Dimension(80, 23));
        jButton1.setPreferredSize(new java.awt.Dimension(80, 23));
        jButton1.setBounds(800, 630, 80, 23);
        jDesktopPane1.add(jButton1, javax.swing.JLayeredPane.DEFAULT_LAYER);

        jToolBar1.setRollover(true);
        jToolBar1.setMaximumSize(new java.awt.Dimension(10000, 10000));

        jMenu1.setText("File");

        jMenuItem1.setText("Connect to Database");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem2.setText("Disconnect from Database");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem3.setText("Exit");
        jMenuItem3.setName("exitMenuItem");
        jMenu1.add(jMenuItem3);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Data");

        jMenuItem11.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem11.setText("Commit");
        jMenu2.add(jMenuItem11);

        jMenu3.setText("New");

        jMenuItem8.setText("Car");
        jMenu3.add(jMenuItem8);

        jMenuItem18.setText("Customer");
        jMenu3.add(jMenuItem18);

        jMenuItem7.setText("Lease");
        jMenu3.add(jMenuItem7);

        jMenu2.add(jMenu3);

        jMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem4.setText("Sort");
        jMenu2.add(jMenuItem4);

        jMenuItem5.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem5.setText("Find");
        jMenu2.add(jMenuItem5);

        jMenuBar1.add(jMenu2);

        jMenu5.setText("Language");

        jMenuItem14.setText("Česky");
        jMenu5.add(jMenuItem14);

        jMenuItem13.setText("Deutch");
        jMenu5.add(jMenuItem13);

        jMenuItem12.setText("English");
        jMenu5.add(jMenuItem12);

        jMenuItem16.setText("日本語");
        jMenu5.add(jMenuItem16);

        jMenuItem17.setText("русский");
        jMenu5.add(jMenuItem17);

        jMenuItem15.setText("Slovensky");
        jMenu5.add(jMenuItem15);

        jMenuBar1.add(jMenu5);

        jMenu4.setText("Help");

        jMenuItem9.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem9.setText("Help");
        jMenu4.add(jMenuItem9);

        jMenuItem10.setText("Credits");
        jMenu4.add(jMenuItem10);

        jMenuBar1.add(jMenu4);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jDesktopPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jDesktopPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 39, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private class ExitAction extends AbstractAction
    {
        public ExitAction(String string) {
            super(string);
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (!((CarsTableModel)carTable.getModel()).getUpdatedCars().isEmpty() ||
                ((CarsTableModel)carTable.getModel()).hasNewCars())
            {
                if (JOptionPane.showConfirmDialog(jMenu1,
                        localization.getString("unsaved_cars_message"),
                        localization.getString("uncommited_changes"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                    return;
            }
            
            if (!((CustomersTableModel)customerTable.getModel()).getUpdatedCustomers().isEmpty() ||
                ((CustomersTableModel)customerTable.getModel()).hasNewCustomers())
            {
                if (JOptionPane.showConfirmDialog(jMenu1,
                        localization.getString("unsaved_customers_message"),
                        localization.getString("uncommited_changes"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                    return;
            }
            
            System.exit(0);
        }
    }
    
    private class AddCarAction extends AbstractAction
    {
        public AddCarAction(String string)
        {
            super(string);
        }
        
        @Override
        public void actionPerformed(ActionEvent e)
        {
            NewCarForm addCarForm = new NewCarForm();
            addCarForm.setVisible(true);
            addCarForm.toFront();
        }
    }
    
    private class AddCustomerAction extends AbstractAction
    {
        public AddCustomerAction(String string)
        {
            super(string);
        }
        
        @Override
        public void actionPerformed(ActionEvent e)
        {
            NewCustomerForm addCustomerForm = new NewCustomerForm();
            addCustomerForm.setVisible(true);
            addCustomerForm.toFront();
        }
    }
    
    private class AddRentAction extends AbstractAction
    {
        public AddRentAction(String string)
        {
            super(string);
        }
        
        @Override
        public void actionPerformed(ActionEvent e)
        {
            NewRentForm addRentForm = new NewRentForm();
            addRentForm.setVisible(true);
            addRentForm.toFront();
        }
    }
    
    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        dataSource = prepareDataSource();
        
        customersAction = CustomersActions.GET_ALL_CUSTOMERS;
        carsAction = CarsActions.GET_ALL_CARS;
        rentsAction = RentsActions.GET_ALL_RENTS;
        CustomerSwingWorker customerSwingWorker = new CustomerSwingWorker();
        customerSwingWorker.execute();
        CarSwingWorker carSwingWorker = new CarSwingWorker();
        carSwingWorker.execute();
        RentalSwingWorker rentalSwingWorker = new RentalSwingWorker();
        rentalSwingWorker.execute();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        dataSource = null;
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        switch (jTabbedPane1.getSelectedIndex())
        {
            case 0:
            {
                String query = jTextField2.getText();
                
                break;
            }
            case 1:
            {
                //search customers
                break;
            }
            case 2:
            {
                //search rents
                break;
            }
        }
    }//GEN-LAST:event_jButton9ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new MainForm().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable carTable;
    private javax.swing.JTable customerTable;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JDesktopPane jDesktopPane1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem11;
    private javax.swing.JMenuItem jMenuItem12;
    private javax.swing.JMenuItem jMenuItem13;
    private javax.swing.JMenuItem jMenuItem14;
    private javax.swing.JMenuItem jMenuItem15;
    private javax.swing.JMenuItem jMenuItem16;
    private javax.swing.JMenuItem jMenuItem17;
    private javax.swing.JMenuItem jMenuItem18;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar mainToolBar;
    private javax.swing.JTable rentTable;
    // End of variables declaration//GEN-END:variables

    private static java.sql.Date newDate() {
        java.util.Date today = new java.util.Date();
        return (new java.sql.Date(today.getTime()));
    }

    private enum CustomersActions {

        ADD_CUSTOMER, EDIT_CUSTOMER, REMOVE_CUSTOMER, FIND_CUSTOMER_BY_ID, GET_ALL_CUSTOMERS;
    };

    private enum CarsActions {

        ADD_CAR, EDIT_CAR, REMOVE_CAR, FIND_CAR_BY_ID, GET_ALL_CARS, GET_ALL_AVAILABLE_CARS;
    };

    private enum RentsActions {

        ADD_RENT, REMOVE_RENT, EDIT_RENT, GET_CUSTOMER_CARS, GET_CUSTOMER_WITH_CAR, GET_ALL_RENTS;
    };
    
    private ResourceBundle localization = ResourceBundle.getBundle("cz.muni.fi.pv168.localization");
    private DataSource dataSource = null;
    private CarManager carManager = new CarManagerImplementation();
    private CustomerManager customerManager = new CustomerManagerImplementation();
    private RentManager rentManager = new RentManagerImplementation();
    private CustomersActions customersAction;
    private CarsActions carsAction;
    private RentsActions rentsAction;
}
