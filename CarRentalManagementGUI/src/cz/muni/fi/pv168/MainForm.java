package cz.muni.fi.pv168;

import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.apache.commons.dbcp.BasicDataSource;

public class MainForm extends javax.swing.JFrame implements ClipboardOwner {

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
        
        try
        {
            DBUtils.tryCreateTables(ds);
        }
        catch (SQLException ex)
        {
            JOptionPane.showMessageDialog(jMenu1, localization.getString("db_connection_failure"),
                localization.getString("error"), JOptionPane.ERROR_MESSAGE);
        }
        
        carManager.setDataSource(ds);
        customerManager.setDataSource(ds);
        rentManager.setDataSource(ds);
        
        return ds;
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) { }

    private class CustomerSwingWorker extends SwingWorker<List<Customer>, Void> {

        private CustomersActions customersAction;
        private Customer customer;
        private String searchQuery;
        
        public CustomerSwingWorker(CustomersActions action)
        {
            customersAction = action;
        }
        
        public CustomerSwingWorker(CustomersActions action, Customer customer)
        {
            this(action);
            this.customer = customer;
        }
        
        public CustomerSwingWorker(String query)
        {
            this(CustomersActions.SEARCH_CUSTOMERS);
            searchQuery = query;
        }
        
        @Override
        protected List<Customer> doInBackground() throws Exception {
            switch (customersAction) {
                case ADD_CUSTOMER:
                    try {
                        customerManager.addCustomer(customer);
                        return customerManager.getAllCustomers();
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("cannot_add_customer"),
                                localization.getString("error"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                case EDIT_CUSTOMER:
                    try {
                        customerManager.updateCustomerInfo(customer);
                        return customerManager.getAllCustomers();
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(rootPane, (localization.getString("cannot_edit_customer") + " " + customer.getID()),
                                localization.getString("error"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                case REMOVE_CUSTOMER:
                    try {
                        customerManager.removeCustomer(customer);
                        return customerManager.getAllCustomers();
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(rootPane, (localization.getString("cannot_remove_customer") + " " + customer.getID()),
                                localization.getString("error"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                case GET_ALL_CUSTOMERS:
                    try {
                        return customerManager.getAllCustomers();
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("db_error"),
                                localization.getString("error"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    } catch (RuntimeException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("db_error"),
                                localization.getString("error"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                case SEARCH_CUSTOMERS:
                    try {
                        List<Customer> customers = customerManager.getAllCustomers();
                        List<Customer> customerMatches = new ArrayList<>();
                        
                        for (Customer c : customers)
                        {
                            if (c.getID().toString().contains(searchQuery) || c.getAddress().contains(searchQuery) ||
                                    c.getDriversLicense().contains(searchQuery) || c.getFirstName().contains(searchQuery) ||
                                    c.getLastName().contains(searchQuery) || c.getPhoneNumber().contains(searchQuery))
                                customerMatches.add(c);
                        }
                        
                        return customerMatches;
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("db_error"),
                                localization.getString("error"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    } catch (RuntimeException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("db_error"),
                                localization.getString("error"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
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
                throw new IllegalStateException(localization.getString("interrupted"));
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
                        throw new IllegalStateException(localization.getString("interrupted"));
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
                        throw new IllegalStateException(localization.getString("interrupted"));
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
                        throw new IllegalStateException(localization.getString("interrupted"));
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
                        throw new IllegalStateException(localization.getString("interrupted"));
                    }
                    break;
                case SEARCH_CUSTOMERS:
                    CustomersTableModel matchesModel = (CustomersTableModel) customerTable.getModel();
                    try {
                        matchesModel.updateCustomers(get());
                    } catch (ExecutionException ex) {
                        JOptionPane.showMessageDialog(rootPane, ex.getCause().getMessage(),
                                ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(localization.getString("interrupted"));
                    }
                    break;
                default:
                    throw new IllegalStateException("Default reached in done() in CustomerSwingWorker");
            }
            customersAction = null;
        }
    }

    private class CarSwingWorker extends SwingWorker<List<Car>, Void> {

        private CarsActions carsAction;
        private Car car;
        private String searchQuery;
        
        public CarSwingWorker(CarsActions action)
        {
            carsAction = action;
        }
        
        public CarSwingWorker(CarsActions action, Car car)
        {
            this(action);
            this.car = car;
        }
        
        public CarSwingWorker(String query)
        {
            this(CarsActions.SEARCH_CARS);
            searchQuery = query;
        }
        
        @Override
        protected List<Car> doInBackground() throws Exception {
            switch (carsAction) {
                case ADD_CAR:
                {
                    try {
                        carManager.addCar(car);

                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(rootPane,
                                (localization.getString("cannot_add_car") + localization.getString("into_db")),
                                localization.getString("error"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                    return carManager.getAllCars();
                }
                case EDIT_CAR:
                    try {
                        carManager.updateCarInfo(car);
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(rootPane,
                                (localization.getString("cannot_update_car") + " " + car.getID()),
                                localization.getString("error"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                    return carManager.getAllCars();

                case REMOVE_CAR:
                    try {
                        carManager.removeCar(car);
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(rootPane,
                                (localization.getString("cannot_remove_car") + " " + car.getID()),
                                localization.getString("error"), JOptionPane.ERROR_MESSAGE);
                    }
                    return carManager.getAllCars();
                case GET_ALL_AVAILABLE_CARS:
                    try {
                        return carManager.getAvailableCars();
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("db_error"),
                                localization.getString("error"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                case GET_ALL_CARS:
                    try {
                        return carManager.getAllCars();
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("db_error"),
                                localization.getString("error"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    } catch (RuntimeException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("db_error"),
                                localization.getString("error"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                case SEARCH_CARS:
                    try {
                        List<Car> cars = carManager.getAllCars();
                        List<Car> carMatches = new ArrayList<>();
                
                        for (Car c : cars)
                        {
                            if (c.getID().toString().contains(searchQuery) || c.getColor().contains(searchQuery) ||
                                    c.getLicensePlate().contains(searchQuery) || c.getModel().contains(searchQuery) ||
                                    c.getRentalPayment().toString().contains(searchQuery))
                                carMatches.add(c);
                        }
                        
                        return carMatches;
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("db_error"),
                                localization.getString("error"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    } catch (RuntimeException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("db_error"),
                                localization.getString("error"), JOptionPane.ERROR_MESSAGE);
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
                throw new IllegalStateException(localization.getString("interrupted"), ex);
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
                        throw new IllegalStateException(localization.getString("interrupted"), ex);
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
                        throw new IllegalStateException(localization.getString("interrupted"), ex);
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
                        throw new IllegalStateException(localization.getString("interrupted"), ex);
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
                        throw new IllegalStateException(localization.getString("interrupted"), ex);
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
                        throw new IllegalStateException(localization.getString("interrupted"), ex);
                    }
                    break;
                case SEARCH_CARS:
                    CarsTableModel matchesCars = (CarsTableModel) carTable.getModel();
                    try {
                        matchesCars.updateCars(get());
                    } catch (ExecutionException ex) {
                        JOptionPane.showMessageDialog(rootPane,
                                ex.getCause().getMessage(), ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(localization.getString("interrupted"), ex);
                    }
                    break;
                default:
                    throw new IllegalStateException("Default reached in done() in CarSwingWorker");
            }
            carsAction = null;
        }
    }

    private class RentalSwingWorker extends SwingWorker<List<Rent>, Void> {

        private RentsActions rentsAction;
        private Rent rent;
        private String searchQuery;
        
        public RentalSwingWorker(RentsActions action)
        {
            rentsAction = action;
        }
        
        public RentalSwingWorker(RentsActions action, Rent rent)
        {
            this(action);
            this.rent = rent;
        }
        
        public RentalSwingWorker(String query)
        {
            this(RentsActions.SEARCH_RENTS);
            searchQuery = query;
        }
        
        @Override
        protected List<Rent> doInBackground() throws Exception {
            switch (rentsAction) {
                case ADD_RENT:
                    try {
                        Car car = carManager.findCarByID(rent.getCarID());
                        Customer customer = customerManager.findCustomerByID(rent.getCustomerID());
                        
                        if ((car == null) || (customer == null))
                            return null;
                        
                        rentManager.rentCarToCustomer(car, customer, rent.getRentDate(), rent.getDueDate());
                        
                        return rentManager.getAllRents();
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("cannot_add_rent"),
                                localization.getString("error"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                case REMOVE_RENT:
                    try {
                        Car car = carManager.findCarByID(rent.getCarID());
                        Customer customer = customerManager.findCustomerByID(rent.getCustomerID());
                        
                        rentManager.getCarFromCustomer(car, customer);
                        return rentManager.getAllRents();
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(rootPane, (localization.getString("cannot_remove_rent") + " " + rent.getID()),
                                localization.getString("error"), JOptionPane.ERROR_MESSAGE);
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
                                localization.getString("error"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                    Customer customer = customerManager.findCustomerByID(customerID);
                    if (customer == null) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("CUSTOMER_ID_NOT_EXIST"),
                                localization.getString("error"), JOptionPane.ERROR_MESSAGE);
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
                                localization.getString("error"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                    Car car = carManager.findCarByID(ID);
                    if (null == car) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("CAR_ID_NOT_EXIST"),
                                localization.getString("error"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                    if (car.getAvailable() == true || rentManager.findCustomerWithCar(car) == null) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("CAR_NOT_RENTED"),
                                localization.getString("error"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }

                    List<Rent> rentFoundCustomer = new ArrayList<>();
                    rentFoundCustomer.add(rentManager.findRentWithCar(car));

                    return rentFoundCustomer;

                case GET_ALL_RENTS:
                    try {
                        return rentManager.getAllRents();
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("db_error"),
                                localization.getString("error"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    } catch (RuntimeException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("db_error"),
                                localization.getString("error"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                case SEARCH_RENTS:
                    try {
                        List<Rent> rents = rentManager.getAllRents();
                        List<Rent> rentMatches = new ArrayList<>();
                        
                        for (Rent r : rents)
                        {
                            if (r.getID().toString().contains(searchQuery) ||
                                    r.getCarID().toString().contains(searchQuery) ||
                                    r.getCustomerID().toString().contains(searchQuery) ||
                                    r.getDueDate().toString().contains(searchQuery) ||
                                    r.getRentDate().toString().contains(searchQuery))
                                rentMatches.add(r);
                        }
                        
                        return rentMatches;
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("db_error"),
                                localization.getString("error"), JOptionPane.ERROR_MESSAGE);
                        return null;
                    } catch (RuntimeException ex) {
                        JOptionPane.showMessageDialog(rootPane, localization.getString("db_error"),
                                localization.getString("error"), JOptionPane.ERROR_MESSAGE);
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
                throw new IllegalStateException(localization.getString("interrupted"), ex);
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
                        throw new IllegalStateException(localization.getString("interrupted"), ex);
                    }
                    new CarSwingWorker(CarsActions.GET_ALL_CARS).execute();
                    new CustomerSwingWorker(CustomersActions.GET_ALL_CUSTOMERS).execute();
                    break;
                case REMOVE_RENT:
                    RentsTableModel removeModel = (RentsTableModel) rentTable.getModel();
                    try {
                        removeModel.updateRents(get());
                    } catch (ExecutionException ex) {
                        JOptionPane.showMessageDialog(rootPane,
                                ex.getCause().getMessage(), ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(localization.getString("interrupted"), ex);
                    }
                    new CarSwingWorker(CarsActions.GET_ALL_CARS).execute();
                    new CustomerSwingWorker(CustomersActions.GET_ALL_CUSTOMERS).execute();
                    break;
                case GET_CUSTOMER_WITH_CAR:
                    RentsTableModel customerWithCarModel = (RentsTableModel) rentTable.getModel();
                    try {
                        customerWithCarModel.updateRents(get());
                    } catch (ExecutionException ex) {
                        JOptionPane.showMessageDialog(rootPane,
                                ex.getCause().getMessage(), ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(localization.getString("interrupted"), ex);
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
                        throw new IllegalStateException(localization.getString("interrupted"), ex);
                    }
                    break;
                case SEARCH_RENTS:
                    RentsTableModel matchesModel = (RentsTableModel) rentTable.getModel();
                    try {
                        matchesModel.updateRents(get());
                    } catch (ExecutionException ex) {
                        JOptionPane.showMessageDialog(rootPane,
                                ex.getCause().getMessage(), ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(localization.getString("interrupted"), ex);
                    }
                    break;
                default:
                    throw new IllegalStateException("default reached in done() RentalSwingWorker");
            }
            rentsAction = null;
        }
    }
    
    public MainForm()
    {
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
        jTabbedPane1.setTitleAt(2, localization.getString("rents"));
        
        carTable.setModel(new CarsTableModel(localization));
        customerTable.setModel(new CustomersTableModel(localization));
        rentTable.setModel(new RentsTableModel(localization));
        
        jMenu1.setText(localization.getString("file"));
        jMenu2.setText(localization.getString("data"));
        jMenu3.setText(localization.getString("new"));
        jMenu4.setText(localization.getString("help"));
        
        jMenuItem1.setText(localization.getString("db_connect"));
        jMenuItem2.setText(localization.getString("db_disconnect"));
        jMenuItem9.setText(localization.getString("help"));
        jMenuItem10.setText(localization.getString("credits"));
        
        jMenuItem3.setAction(new ExitAction(localization.getString("exit")));
        jMenuItem7.setAction(new AddRentAction(localization.getString("rent")));
        jMenuItem8.setAction(new AddCarAction(localization.getString("car")));
        jMenuItem11.setAction(new CommitAction(localization.getString("commit")));
        jMenuItem18.setAction(new AddCustomerAction(localization.getString("customer")));
        
        jButton1.setAction(new CommitAction(localization.getString("commit")));
        jButton2.setAction(new AddCarAction(localization.getString("new_car")));
        jButton3.setAction(new AddCustomerAction(localization.getString("new_customer")));
        jButton4.setAction(new AddRentAction(localization.getString("new_rent")));
        
        jMenuItem4.setAction(new SortAction(localization.getString("sort")));
        jMenuItem5.setText(localization.getString("find"));
        
        jButton8.setAction(new SortAction(localization.getString("sort")));
        jButton9.setText(localization.getString("search"));
        
        jMenu6.setText(localization.getString("remove"));
        jMenuItem19.setText(localization.getString("car"));
        jMenuItem20.setText(localization.getString("customer"));
        jMenuItem21.setText(localization.getString("rent"));
        
        jMenuItem23.setAction(new CopyAction(localization.getString("copy")));
        jMenuItem24.setAction(new CutAction(localization.getString("cut")));
        jMenuItem25.setAction(new PasteAction(localization.getString("paste")));
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
        jSeparator5 = new javax.swing.JSeparator();
        jMenuItem22 = new javax.swing.JMenuItem();
        jDesktopPane1 = new javax.swing.JDesktopPane();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        carTable = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        customerTable = new javax.swing.JTable();
        jScrollPane4 = new javax.swing.JScrollPane();
        rentTable = new javax.swing.JTable();
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
        jMenu6 = new javax.swing.JMenu();
        jMenuItem19 = new javax.swing.JMenuItem();
        jMenuItem20 = new javax.swing.JMenuItem();
        jMenuItem21 = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        jMenuItem23 = new javax.swing.JMenuItem();
        jMenuItem24 = new javax.swing.JMenuItem();
        jMenuItem25 = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenuItem9 = new javax.swing.JMenuItem();
        jMenuItem10 = new javax.swing.JMenuItem();

        jMenuItem6.setText("jMenuItem6");

        jMenuItem22.setText("jMenuItem22");

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

        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cz/muni/fi/pv168/copy_icon.png"))); // NOI18N
        jButton5.setFocusable(false);
        jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton5.setName("");
        jButton5.setPreferredSize(new java.awt.Dimension(25, 25));
        jButton5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        mainToolBar.add(jButton5);

        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cz/muni/fi/pv168/cut_icon.png"))); // NOI18N
        jButton6.setFocusable(false);
        jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton6.setName("");
        jButton6.setPreferredSize(new java.awt.Dimension(25, 25));
        jButton6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        mainToolBar.add(jButton6);

        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cz/muni/fi/pv168/paste_icon.png"))); // NOI18N
        jButton7.setFocusable(false);
        jButton7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton7.setName("");
        jButton7.setPreferredSize(new java.awt.Dimension(25, 25));
        jButton7.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        mainToolBar.add(jButton7);

        jSeparator2.setMinimumSize(new java.awt.Dimension(200, 0));
        jSeparator2.setPreferredSize(new java.awt.Dimension(200, 0));
        jSeparator2.setSeparatorSize(new java.awt.Dimension(200, 0));
        mainToolBar.add(jSeparator2);

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

        jMenu6.setText("jMenu6");

        jMenuItem19.setText("jMenuItem19");
        jMenuItem19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem19ActionPerformed(evt);
            }
        });
        jMenu6.add(jMenuItem19);

        jMenuItem20.setText("jMenuItem20");
        jMenuItem20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem20ActionPerformed(evt);
            }
        });
        jMenu6.add(jMenuItem20);

        jMenuItem21.setText("jMenuItem21");
        jMenuItem21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem21ActionPerformed(evt);
            }
        });
        jMenu6.add(jMenuItem21);

        jMenu2.add(jMenu6);
        jMenu2.add(jSeparator6);

        jMenuItem23.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem23.setText("jMenuItem23");
        jMenu2.add(jMenuItem23);

        jMenuItem24.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem24.setText("jMenuItem24");
        jMenu2.add(jMenuItem24);

        jMenuItem25.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem25.setText("jMenuItem25");
        jMenu2.add(jMenuItem25);
        jMenu2.add(jSeparator4);

        jMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem4.setText("Sort");
        jMenu2.add(jMenuItem4);

        jMenuItem5.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem5.setText("Find");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem5);

        jMenuBar1.add(jMenu2);

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

    private boolean discardChanges()
    {
        if (!((CarsTableModel)carTable.getModel()).getUpdatedCars().isEmpty() ||
                ((CarsTableModel)carTable.getModel()).hasNewCars())
        {
            if (JOptionPane.showConfirmDialog(jMenu1,
                    localization.getString("unsaved_cars_message"),
                    localization.getString("uncommited_changes"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                return false;
        }

        if (!((CustomersTableModel)customerTable.getModel()).getUpdatedCustomers().isEmpty() ||
            ((CustomersTableModel)customerTable.getModel()).hasNewCustomers())
        {
            if (JOptionPane.showConfirmDialog(jMenu1,
                    localization.getString("unsaved_customers_message"),
                    localization.getString("uncommited_changes"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                return false;
        }
        
        if (((RentsTableModel)rentTable.getModel()).hasNewRents())
        {
            if (JOptionPane.showConfirmDialog(jMenu1,
                    localization.getString("unsaved_rents_message"),
                    localization.getString("uncommited_changes"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                return false;
        }
        
        return true;
    }
    
    private class ExitAction extends AbstractAction
    {
        public ExitAction(String string) {
            super(string);
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (discardChanges())
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
            NewCarForm addCarForm = new NewCarForm((CarsTableModel)carTable.getModel(), localization);
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
            NewCustomerForm addCustomerForm = new NewCustomerForm((CustomersTableModel)customerTable.getModel(), localization);
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
            NewRentForm addRentForm = new NewRentForm((RentsTableModel)rentTable.getModel(), localization);
            addRentForm.setVisible(true);
            addRentForm.toFront();
        }
    }
    
    private void Copy()
    {
        Clipboard clipboard = getToolkit().getSystemClipboard();
        String data = null;
        
        switch (jTabbedPane1.getSelectedIndex())
        {
            case 0:
            {   
                int column = carTable.getSelectedColumn();
                int row = carTable.getSelectedRow();
                
                if ((column < 0) || (row < 0))
                    return;
                
                CarsTableModel ctm = (CarsTableModel)carTable.getModel();
                
                data = ctm.getValueAt(row, column).toString();
                
                break;
            }
            case 1:
            {
                int column = customerTable.getSelectedColumn();
                int row = customerTable.getSelectedRow();
                
                if ((column < 0) || (row < 0))
                    return;
                
                CustomersTableModel ctm = (CustomersTableModel)customerTable.getModel();
                
                data = ctm.getValueAt(row, column).toString();
                
                break;
            }
            case 2:
            {
                int column = rentTable.getSelectedColumn();
                int row = rentTable.getSelectedRow();
                
                if ((column < 0) || (row < 0))
                    return;
                
                RentsTableModel rtm = (RentsTableModel)rentTable.getModel();
                
                data = rtm.getValueAt(row, column).toString();
                
                break;
            }
            default: break;
        }
        
        clipboard.setContents(new StringSelection(data), this);
    }
    
    private class CopyAction extends AbstractAction
    {
        public CopyAction(String string)
        {
            super(string);
        }
        
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Copy();
        }
    }
    
    private void Cut()
    {
        Clipboard clipboard = getToolkit().getSystemClipboard();
        String data = null;
        
        switch (jTabbedPane1.getSelectedIndex())
        {
            case 0:
            {   
                int column = carTable.getSelectedColumn();
                int row = carTable.getSelectedRow();
                
                CarsTableModel ctm = (CarsTableModel)carTable.getModel();
                
                if ((column < 0) || (row < 0) || !ctm.isCellEditable(row, column))
                    return;
                
                data = ctm.getValueAt(row, column).toString();
                ctm.setValueAt("", row, column);
                
                break;
            }
            case 1:
            {
                int column = customerTable.getSelectedColumn();
                int row = customerTable.getSelectedRow();
                
                CustomersTableModel ctm = (CustomersTableModel)customerTable.getModel();
                
                if ((column < 0) || (row < 0) || !ctm.isCellEditable(row, column))
                    return;
                
                data = ctm.getValueAt(row, column).toString();
                ctm.setValueAt("", row, column);
                
                break;
            }
            case 2:
            {
                int column = rentTable.getSelectedColumn();
                int row = rentTable.getSelectedRow();
                
                RentsTableModel rtm = (RentsTableModel)rentTable.getModel();
                
                if ((column < 0) || (row < 0) || !rtm.isCellEditable(row, column))
                    return;
                
                data = rtm.getValueAt(row, column).toString();
                rtm.setValueAt("", row, column);
                
                break;
            }
            default: break;
        }
        
        clipboard.setContents(new StringSelection(data), this);
    }
    
    private class CutAction extends AbstractAction
    {
        public CutAction(String string)
        {
            super(string);
        }
        
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Cut();
        }
    }
    
    private void Paste()
    {
        Clipboard clipboard = getToolkit().getSystemClipboard();
        String data = null;
        
        try
        {
            data = (String)clipboard.getData(DataFlavor.stringFlavor);
        }
        catch (IOException | UnsupportedFlavorException e)
        {
            return;
        }
        
        if (data == null)
            return;
        
        switch (jTabbedPane1.getSelectedIndex())
        {
            case 0:
            {   
                int column = carTable.getSelectedColumn();
                int row = carTable.getSelectedRow();
                
                CarsTableModel ctm = (CarsTableModel)carTable.getModel();
                
                if ((column < 0) || (row < 0) || !ctm.isCellEditable(row, column))
                    return;
                
                ctm.setValueAt(data, row, column);
                
                break;
            }
            case 1:
            {
                int column = customerTable.getSelectedColumn();
                int row = customerTable.getSelectedRow();
                
                CustomersTableModel ctm = (CustomersTableModel)customerTable.getModel();
                
                if ((column < 0) || (row < 0) || !ctm.isCellEditable(row, column))
                    return;
                
                ctm.setValueAt(data, row, column);
                
                break;
            }
            case 2:
            {
                int column = rentTable.getSelectedColumn();
                int row = rentTable.getSelectedRow();
                
                RentsTableModel rtm = (RentsTableModel)rentTable.getModel();
                
                if ((column < 0) || (row < 0) || !rtm.isCellEditable(row, column))
                    return;
                
                rtm.setValueAt(data, row, column);
                
                break;
            }
            default: break;
        }
    }
    
    private class PasteAction extends AbstractAction
    {
        public PasteAction(String string)
        {
            super(string);
        }
        
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Paste();
        }
    }
    
    private class SortAction extends AbstractAction
    {
        public SortAction(String string)
        {
            super(string);
        }
        
        @Override
        public void actionPerformed(ActionEvent e)
        {
            
        }
    }
    
    private class CommitAction extends AbstractAction
    {
        public CommitAction(String string)
        {
            super(string);
        }
        
        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (dataSource == null)
            {
                JOptionPane.showMessageDialog(jMenu1, localization.getString("no_db_loaded_message"),
                        localization.getString("db_missing"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            switch (jTabbedPane1.getSelectedIndex())
            {
                case 0:
                {
                    CarsTableModel ctm = (CarsTableModel)carTable.getModel();
                    
                    Set<Car> toBeRemovedCars = new HashSet<>();
                    Set<Car> toBeUpdatedCars = new HashSet<>();
                    
                    if (ctm.hasNewCars())
                        for (Car c : ctm.getCars())
                            if ((c.getID() == null) && (isValid(c)))
                                new CarSwingWorker(CarsActions.ADD_CAR, c).execute();
                    
                    for (Car c : ctm.getUpdatedCars())
                    {
                        if (!isValid(c))
                            toBeRemovedCars.add(c);
                        else
                            toBeUpdatedCars.add(c);
                    }
                    
                    for (Car c : toBeUpdatedCars)
                    {
                        new CarSwingWorker(CarsActions.EDIT_CAR, c).execute();
                        ctm.carResolved(c);
                    }
                    
                    if (!toBeRemovedCars.isEmpty())
                    {
                        if (JOptionPane.showConfirmDialog(jMenu1, localization.getString("cars_being_deleted"),
                            localization.getString("car_info_missing"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                        {
                            for (Car c : toBeRemovedCars)
                            {
                                new CarSwingWorker(CarsActions.REMOVE_CAR, c).execute();
                                ctm.carResolved(c);
                            }
                        }
                    }
                    
                    break;
                }
                case 1:
                {
                    CustomersTableModel ctm = (CustomersTableModel)customerTable.getModel();
                    
                    Set<Customer> toBeRemovedCustomers = new HashSet<>();
                    Set<Customer> toBeUpdatedCustomers = new HashSet<>();
                    
                    if (ctm.hasNewCustomers())
                        for (Customer c : ctm.getCustomers())
                            if ((c.getID() == null) && (isValid(c)))
                                new CustomerSwingWorker(CustomersActions.ADD_CUSTOMER, c).execute();
                    
                    for (Customer c : ctm.getUpdatedCustomers())
                    {
                        if (!isValid(c))
                            toBeRemovedCustomers.add(c);
                        else
                            toBeUpdatedCustomers.add(c);
                    }
                    
                    for (Customer c : toBeUpdatedCustomers)
                    {
                        new CustomerSwingWorker(CustomersActions.EDIT_CUSTOMER, c).execute();
                        ctm.customerResolved(c);
                    }
                    
                    if (!toBeRemovedCustomers.isEmpty())
                    {
                        if (JOptionPane.showConfirmDialog(jMenu1, localization.getString("customers_being_deleted"),
                            localization.getString("customer_info_missing"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                        {
                            for (Customer c : toBeRemovedCustomers)
                            {
                                new CustomerSwingWorker(CustomersActions.REMOVE_CUSTOMER, c).execute();
                                ctm.customerResolved(c);
                            }
                        }
                    }
                    
                    break;
                }
                case 2:
                {
                    RentsTableModel rtm = (RentsTableModel)rentTable.getModel();
                    
                    if (rtm.hasNewRents())
                        for (Rent r : rtm.getRents())
                            if ((r.getID() == null) && (isValid(r)))
                                new RentalSwingWorker(RentsActions.ADD_RENT, r).execute();
                    
                    break;
                }
            }
        }
    }
    
    private boolean isValid(Car car)
    {
        return ((car.getModel() != null) && !car.getModel().isEmpty() && (car.getColor() != null) &&
                !car.getColor().isEmpty() && (car.getLicensePlate() != null) && (car.getLicensePlate().length() > 6) &&
                (car.getLicensePlate().length() < 9) && (car.getRentalPayment() != null) && (car.getRentalPayment() >= 0));
    }
    
    private boolean isValid(Customer customer)
    {
        return ((customer.getAddress() != null) && !customer.getAddress().isEmpty() &&
                (customer.getDriversLicense() != null) && !customer.getDriversLicense().isEmpty() &&
                (customer.getFirstName() != null) && !customer.getFirstName().isEmpty() &&
                (customer.getLastName() != null) && !customer.getLastName().isEmpty() &&
                (customer.getPhoneNumber() != null) && !customer.getPhoneNumber().isEmpty());
    }
    
    private boolean isValid(Rent rent)
    {
        return ((rent.getCarID() != null) && (rent.getCustomerID() != null) && (rent.getDueDate() != null) &&
                (rent.getRentDate() != null));
    }
    
    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        dataSource = prepareDataSource();
        
        new CustomerSwingWorker(CustomersActions.GET_ALL_CUSTOMERS).execute();
        new CarSwingWorker(CarsActions.GET_ALL_CARS).execute();
        new RentalSwingWorker(RentsActions.GET_ALL_RENTS).execute();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        if (discardChanges())
        {
            ((CarsTableModel)carTable.getModel()).updateCars(Collections.EMPTY_LIST);
            ((CustomersTableModel)customerTable.getModel()).updateCustomers(Collections.EMPTY_LIST);
            ((RentsTableModel)rentTable.getModel()).updateRents(Collections.EMPTY_LIST);
            
            dataSource = null;
        }
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void Search(String query)
    {
        switch (jTabbedPane1.getSelectedIndex())
        {
            case 0:
            {   
                if ((query == null) || query.isEmpty())
                    new CarSwingWorker(CarsActions.GET_ALL_CARS).execute();
                else
                    new CarSwingWorker(query).execute();
                
                break;
            }
            case 1:
            {
                if ((query == null) || query.isEmpty())
                    new CustomerSwingWorker(CustomersActions.GET_ALL_CUSTOMERS).execute();
                else
                    new CustomerSwingWorker(query).execute();
                
                break;
            }
            case 2:
            {
                if ((query == null) || (query.isEmpty()))
                    new RentalSwingWorker(RentsActions.GET_ALL_RENTS).execute();
                else
                    new RentalSwingWorker(query).execute();
                
                break;
            }
            default: break;
        }
    }
    
    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        String query = jTextField2.getText();
        
        Search(query);
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        Copy();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        Cut();
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        Paste();
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        String query = JOptionPane.showInputDialog(localization.getString("find"));
        
        Search(query);
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenuItem19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem19ActionPerformed
        String stringID = JOptionPane.showInputDialog("ID");
        
        try {
            long id = Long.parseLong(stringID);
            for (Car c : ((CarsTableModel)carTable.getModel()).getCars())
                if (c.getID() == id)
                {
                    new CarSwingWorker(CarsActions.REMOVE_CAR, c).execute();
                    return;
                }
        }
        catch (NumberFormatException ex)
        {
            JOptionPane.showConfirmDialog(jMenu1,
                    ("ID " + localization.getString("must_be_number")),
                    localization.getString("invalid_input"), JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jMenuItem19ActionPerformed

    private void jMenuItem20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem20ActionPerformed
        String stringID = JOptionPane.showInputDialog("ID");
        
        try {
            long id = Long.parseLong(stringID);
            for (Customer c : ((CustomersTableModel)customerTable.getModel()).getCustomers())
                if (c.getID() == id)
                {
                    new CustomerSwingWorker(CustomersActions.REMOVE_CUSTOMER, c).execute();
                    return;
                }
        }
        catch (NumberFormatException ex)
        {
            JOptionPane.showConfirmDialog(jMenu1,
                    ("ID " + localization.getString("must_be_number")),
                    localization.getString("invalid_input"), JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jMenuItem20ActionPerformed

    private void jMenuItem21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem21ActionPerformed
        String stringID = JOptionPane.showInputDialog("ID");
        
        try {
            long id = Long.parseLong(stringID);
            for (Rent r : ((RentsTableModel)rentTable.getModel()).getRents())
                if (r.getID() == id)
                {
                    new RentalSwingWorker(RentsActions.REMOVE_RENT, r).execute();
                    return;
                }
        }
        catch (NumberFormatException ex)
        {
            JOptionPane.showConfirmDialog(jMenu1,
                    ("ID " + localization.getString("must_be_number")),
                    localization.getString("invalid_input"), JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jMenuItem21ActionPerformed

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
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem11;
    private javax.swing.JMenuItem jMenuItem18;
    private javax.swing.JMenuItem jMenuItem19;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem20;
    private javax.swing.JMenuItem jMenuItem21;
    private javax.swing.JMenuItem jMenuItem22;
    private javax.swing.JMenuItem jMenuItem23;
    private javax.swing.JMenuItem jMenuItem24;
    private javax.swing.JMenuItem jMenuItem25;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
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

        ADD_CUSTOMER, EDIT_CUSTOMER, REMOVE_CUSTOMER, GET_ALL_CUSTOMERS, SEARCH_CUSTOMERS
    };

    private enum CarsActions {

        ADD_CAR, EDIT_CAR, REMOVE_CAR, GET_ALL_CARS, GET_ALL_AVAILABLE_CARS, SEARCH_CARS
    };

    private enum RentsActions {

        ADD_RENT, REMOVE_RENT, EDIT_RENT, GET_CUSTOMER_CARS, GET_CUSTOMER_WITH_CAR, GET_ALL_RENTS, SEARCH_RENTS
    };
    
    private ResourceBundle localization = ResourceBundle.getBundle("cz.muni.fi.pv168.localization", new Locale("ru", "RU"));
    private DataSource dataSource = null;
    private CarManager carManager = new CarManagerImplementation();
    private CustomerManager customerManager = new CustomerManagerImplementation();
    private RentManager rentManager = new RentManagerImplementation();
}
