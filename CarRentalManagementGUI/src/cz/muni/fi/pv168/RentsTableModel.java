package cz.muni.fi.pv168;

import java.sql.Date;
import java.util.*;
import javax.swing.table.AbstractTableModel;

public class RentsTableModel extends AbstractTableModel {

    List<Rent> rents = new ArrayList<>();
    private ResourceBundle localization;
    
    public RentsTableModel(ResourceBundle localization)
    {
        this.localization = localization;
    }

    public void updateRents(List<Rent> newInventories) {
        if (null == newInventories) {
            return;
        }
        int firstRow = 0;
        int lastRow = rents.size() - 1;
        rents.clear();
        fireTableRowsDeleted(firstRow, lastRow < 0 ? 0 : lastRow);
        rents.addAll(newInventories);
        Collections.sort(rents, rentByIDComparator);
        lastRow = rents.size() - 1;
        fireTableRowsInserted(firstRow, lastRow < 0 ? 0 : lastRow);
    }

    @Override
    public int getRowCount() {
        return rents.size();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
            case 1:
            case 2:
                return Long.class;
            case 3:
            case 4:
                return Date.class;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "ID";
            case 1:
                return (localization.getString("car") + " ID");
            case 2:
                return (localization.getString("customer") + " ID");
            case 3:
                return localization.getString("rent_date");
            case 4:
                return localization.getString("due_date");
            default:
                throw new IllegalArgumentException("Column");

        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= rents.size()) {
            throw new IllegalArgumentException("Row Index Out Of Bounds.");
        }
        Rent rent = rents.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return rent.getID();
            case 1:
                return rent.getCarID();
            case 2:
                return rent.getCustomerID();
            case 3:
                return rent.getRentDate();
            case 4:
                return rent.getDueDate();
            default:
                throw new IllegalArgumentException("Column Index");
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex >= rents.size()) {
            throw new IllegalArgumentException("Row Index Out Of Bounds.");
        }
        Rent rent = rents.get(rowIndex);
        switch (columnIndex) {
            case 1:
                rent.setCarID((Long) aValue);
                break;
            case 2:
                rent.setCustomerID((Long) aValue);
                break;
            case 3:
                rent.setRentDate((Date) aValue);
                break;
            case 4:
                rent.setDueDate((Date) aValue);
                break;
            default:
                throw new IllegalArgumentException("Column Index");
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
    private static Comparator<Rent> rentByIDComparator = new Comparator<Rent>() {

        @Override
        public int compare(Rent rent1, Rent rent2) {
            return Long.valueOf(rent1.getID()).compareTo(Long.valueOf(rent2.getID()));
        }
    };
}
