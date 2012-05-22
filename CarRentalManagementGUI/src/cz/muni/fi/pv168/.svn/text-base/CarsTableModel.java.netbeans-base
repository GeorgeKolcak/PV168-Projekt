package cz.muni.fi.pv168;

import java.util.*;
import javax.swing.table.AbstractTableModel;

public class CarsTableModel extends AbstractTableModel {

    List<Car> cars = new ArrayList<>();
    private ResourceBundle resourceBundle = MainForm.RESOURCE_BUNDLE;

    public void updateCars(List<Car> newCars) {
        if (null == newCars) {
            return;
        }

        int firstRow = 0;
        int lastRow = cars.size() - 1;
        cars.clear();
        fireTableRowsDeleted(firstRow, lastRow < 0 ? 0 : lastRow);
        cars.addAll(newCars);
        Collections.sort(cars, carByIDComparator);
        lastRow = cars.size() - 1;
        fireTableRowsInserted(firstRow, lastRow < 0 ? 0 : lastRow);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Long.class;
            case 1:
            case 2:
            case 3:
                return String.class;
            case 4:
                return Double.class;
            case 5:
                return Boolean.class;
            default:
                throw new IllegalArgumentException("Column index");
        }
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "ID";
            case 1:
                return resourceBundle.getString("MODEL");
            case 2:
                return resourceBundle.getString("COLOUR");
            case 3:
                return resourceBundle.getString("LICENSE_PLATE");
            case 4:
                return resourceBundle.getString("PAYMENT");
            case 5:
                return resourceBundle.getString("AVAILABLE");
            default:
                throw new IllegalArgumentException("Column Name");
        }
    }

    @Override
    public int getRowCount() {
        return cars.size();
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex > cars.size()) {
            throw new IllegalArgumentException("Row Index Out of Bounds");
        }
        Car car = cars.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return car.getID();
            case 1:
                return car.getModel();
            case 2:
                return car.getColor();
            case 3:
                return car.getLicensePlate();
            case 4:
                return car.getRentalPayment();
            case 5:
                return car.getAvailable();
            default:
                throw new IllegalArgumentException("Column Index");
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex > cars.size()) {
            throw new IllegalArgumentException("Row Index Out of Bounds");
        }
        Car car = cars.get(rowIndex);
        switch (columnIndex) {
            case 1:
                car.setModel((String) aValue);
                break;
            case 2:
                car.setColor((String) aValue);
                break;
            case 3:
                car.setLicensePlate((String) aValue);
            case 4:
                car.setRentalPayment((Double) aValue);
                break;
            default:
                throw new IllegalArgumentException("Column Index");
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }
    private static Comparator<Car> carByIDComparator = new Comparator<Car>() {

        @Override
        public int compare(Car car1, Car car2) {
            return Long.valueOf(car1.getID()).compareTo(Long.valueOf(car2.getID()));
        }
    };
}
