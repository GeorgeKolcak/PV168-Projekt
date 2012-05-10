package cz.muni.fi.pv168;

import java.util.*;
import javax.swing.table.AbstractTableModel;

public class CustomersTableModel extends AbstractTableModel {

    List<Customer> customers = new ArrayList<>();
    private ResourceBundle resourceBundle = MainForm.RESOURCE_BUNDLE;

    public void updateCustomers(List<Customer> newCustomers) {
        if (null == newCustomers) {
            return;
        }
        int firstRow = 0;
        int lastRow = customers.size() - 1;
        customers.clear();
        fireTableRowsDeleted(firstRow, lastRow < 0 ? 0 : lastRow);
        customers.addAll(newCustomers);
        Collections.sort(customers, CustomerByIDComparator);
        lastRow = customers.size() - 1;
        fireTableRowsInserted(firstRow, lastRow < 0 ? 0 : lastRow);
    }

    @Override
    public int getRowCount() {
        return customers.size();
    }

    @Override
    public int getColumnCount() {
        return 7;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Long.class;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return String.class;
            case 6:
                return Boolean.class;
            default:
                throw new IllegalArgumentException("Column Index");
        }
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "ID";
            case 1:
                return resourceBundle.getString("FIRST_NAME");
            case 2:
                return resourceBundle.getString("LAST_NAME");
            case 3:
                return resourceBundle.getString("ADDRESS");
            case 4:
                return resourceBundle.getString("PHONE_NUMBER");
            case 5:
                return resourceBundle.getString("DRIVERS_LICENSE");
            case 6:
                return resourceBundle.getString("ACTIVE");
            default:
                throw new IllegalArgumentException("Column");
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= customers.size()) {
            throw new IllegalArgumentException("Row Index Out Of Bounds.");
        }
        Customer customer = customers.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return customer.getID();
            case 1:
                return customer.getFirstName();
            case 2:
                return customer.getLastName();
            case 3:
                return customer.getAddress();
            case 4:
                return customer.getPhoneNumber();
            case 5:
                return customer.getDriversLicense();
            case 6:
                return customer.getActive();
            default:
                throw new IllegalArgumentException("Column Index");
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex > customers.size()) {
            throw new IllegalArgumentException("Row Index Out of Bounds");
        }
        Customer customer = customers.get(rowIndex);
        switch (columnIndex) {
            case 1:
                customer.setFirstName((String) aValue);
                break;
            case 2:
                customer.setLastName((String) aValue);
                break;
            case 3:
                customer.setAddress((String) aValue);
                break;
            case 4:
                customer.setPhoneNumber((String) aValue);
                break;
            case 5:
                customer.setDriversLicense((String) aValue);
                break;
            default:
                throw new IllegalArgumentException("Column Index");
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }
    private static Comparator<Customer> CustomerByIDComparator = new Comparator<Customer>() {

        @Override
        public int compare(Customer customer1, Customer customer2) {
            return Long.valueOf(customer1.getID()).compareTo(Long.valueOf(customer2.getID()));
        }
    };
}
