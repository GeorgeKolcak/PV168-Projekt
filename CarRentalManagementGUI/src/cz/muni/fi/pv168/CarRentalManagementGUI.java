package cz.muni.fi.pv168;

/**
 *
 * @author Jooji
 */
public class CarRentalManagementGUI
{
    public static void main(String[] args)
    {
        new NewCarForm().setVisible(true);
        new NewCustomerForm().setVisible(true);
        new NewRentForm().setVisible(true);
        new DatabaseConnectionForm().setVisible(true);
        
        MainForm mainForm = new MainForm();
        
        mainForm.setVisible(true);
    }
}
