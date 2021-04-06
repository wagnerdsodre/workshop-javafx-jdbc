package gui;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.entities.Seller;
import model.exceptions.ValidationExceptions;
import model.services.SellerService;

public class SellerFormController implements Initializable {

	private Seller entity;

	private SellerService service;

	// objects events dependence
	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();

	@FXML
	private TextField txtId;

	@FXML
	private TextField txtName;

	@FXML
	private Label labelErrorName;

	@FXML
	private Button btSave;

	@FXML
	private Button btCancel;

	public void setSeller(Seller entity) {
		this.entity = entity;
	}

	public void setSellerService(SellerService service) {
		this.service = service;
	}

	// subscrible events
	public void subscribleDataChangeListeners(DataChangeListener listener) {
		dataChangeListeners.add(listener);

	}

	@FXML
	public void onBtSaveAction(ActionEvent event) {
		if (entity == null) {
			throw new IllegalStateException("Entity was null");
		}

		if (service == null) {
			throw new IllegalStateException("Service was null");
		}

		try {
			entity = getFormData();
			service.saveOrUpdate(entity);
			// method dependence event
			notifyDataChangeListeners();
			Utils.currentStage(event).close();

		} 
		//error form
		catch (ValidationExceptions e) {
			setErrorMessages(e.getErros());
		}
		
		
		catch (DbException e) {
			Alerts.showAlert("Error saving object", null, e.getMessage(), AlertType.ERROR);
		}

	}

	private void notifyDataChangeListeners() {
		for (DataChangeListener listener : dataChangeListeners) {
			listener.onDataChange();
		}

	}

	private Seller getFormData() {
		Seller obj = new Seller();
		obj.setId(Utils.tryParseToint(txtId.getText()));
		
		// exception instance
		ValidationExceptions exception = new ValidationExceptions("Validation Error");
		//verify exception field
		if (txtName.getText() == null || txtName.getText().trim().equals("")) {
			exception.addErros("name", "field can't be empaty ");
		}
		obj.setName(txtName.getText());
        //verify exception numbers exceptions
		if (exception.getErros().size() > 0) {
			//put in method setErrorMessagens
			throw exception;
		}

		return obj;
	}

	@FXML
	public void obBtCancelAction(ActionEvent event) {
		Utils.currentStage(event).close();
	}

	@Override
	public void initialize(URL url, ResourceBundle rg) {
		initializeNodes();

	}

	// set properties txtId and txtName
	private void initializeNodes() {
		Constraints.setTextFieldInteger(txtId);
		Constraints.setTextFieldMaxLength(txtName, 30);
	}

	public void updateFormData() {

		if (entity == null) {
			throw new IllegalStateException("Entity was null");
		}

		txtId.setText(String.valueOf(entity.getId()));
		txtName.setText(String.valueOf(entity.getName()));

	}
	
	private void setErrorMessages (Map<String, String> errors) {
		//colections names fields
		Set<String> fields = errors.keySet();
		
		if(fields.contains("name")) {
		labelErrorName.setText(errors.get("name"));
		}
		
	}
	

	
	

}
