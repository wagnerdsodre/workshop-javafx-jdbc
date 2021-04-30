package gui;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import model.entities.Department;
import model.entities.Seller;
import model.exceptions.ValidationExceptions;
import model.services.DepartmentService;
import model.services.SellerService;

public class SellerFormController implements Initializable {

	private Seller entity;

	private SellerService service;

	private DepartmentService departmentService;

	// objects events dependence
	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();

	@FXML
	private TextField txtId;

	@FXML
	private TextField txtName;

	@FXML
	private TextField txtEmail;

	@FXML
	private DatePicker dpBirthDate;

	@FXML
	private TextField txtBaseSalary;
	@FXML
	private ComboBox<Department> comboBoxDepartment;

	@FXML
	private Label labelErrorName;

	@FXML
	private Label labelErrorEmail;

	@FXML
	private Label labelErrorBirthDate;

	@FXML
	private Label labelErrorBaseSalary;

	@FXML
	private Button btSave;

	@FXML
	private Button btCancel;

	private ObservableList<Department> obsList;

	public void setSeller(Seller entity) {
		this.entity = entity;
	}

	// Services Seller and DepartmentService
	public void setServices(SellerService service, DepartmentService departmentService) {
		this.service = service;
		this.departmentService = departmentService;
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
		// error form
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
		Seller obj = new Seller();// Objeto com dados do formulário

		// exceção
		ValidationExceptions exception = new ValidationExceptions("Validation Error");

		// Atribuindo o txtId capturado, ao id do objeto
		obj.setId(Utils.tryParseToint(txtId.getText()));
		// Data Department
		// teste se os atibutos estão nulos ou em branco
		if (txtName.getText() == null || txtName.getText().trim().equals("")) {
			exception.addErros("name", "Este Campo não pode ser vazio ");/// erro
		}
		obj.setName(txtName.getText());

		// verify exception numbers exceptions
		if (txtEmail.getText() == null || txtEmail.getText().trim().equals("")) {
			exception.addErros("email", "Este Campo não pode ser vazio ");/// erro
		}
		obj.setEmail(txtEmail.getText());

		if (dpBirthDate.getValue() == null) {
			exception.addErros("birthDate", "Este Campo não pode ser vazio ");
		} else {
			// Pegando a variavel do DatePicker
			Instant instant = Instant.from(dpBirthDate.getValue().atStartOfDay(ZoneId.systemDefault()));
			// armazenando no objeto
			obj.setBirthDate(Date.from(instant));
		}

		// Salary
		if (txtBaseSalary.getText() == null || txtBaseSalary.getText().trim().equals("")) {
			exception.addErros("BaseSalary", "Este Campo não pode ser vazio ");/// erro
		}
		obj.setBaseSalary(Utils.tryParseToDouble(txtBaseSalary.getText()));
		obj.setDepartment(comboBoxDepartment.getValue());

		if (exception.getErros().size() > 0) {
			// put in method setErrorMessagens
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

	// INITIALIZENODES: set properties txtId and txtName
	private void initializeNodes() {
		Constraints.setTextFieldInteger(txtId);
		Constraints.setTextFieldMaxLength(txtName, 30);
		Constraints.setTextFieldDouble(txtBaseSalary);
		Constraints.setTextFieldMaxLength(txtEmail, 60);
		Utils.formatDatePicker(dpBirthDate, "dd/MM/yyyy");
		initializeComboBoxDepartment();
	}

	public void updateFormData() {

		if (entity == null) {
			throw new IllegalStateException("Entity was null");
		}

		txtId.setText(String.valueOf(entity.getId()));
		txtName.setText(String.valueOf(entity.getName()));
		txtEmail.setText(String.valueOf(entity.getEmail()));
		Locale.setDefault(Locale.US);
		txtBaseSalary.setText(String.format("%.2f", entity.getBaseSalary()));// converter format Double
		if (entity.getBirthDate() != null)
			dpBirthDate.setValue(LocalDate.ofInstant(entity.getBirthDate().toInstant(), ZoneId.systemDefault()));
		if (entity.getDepartment() == null) {
			comboBoxDepartment.getSelectionModel().selectFirst();
		} else {
			comboBoxDepartment.setValue(entity.getDepartment());
		}

	}

	private void setErrorMessages(Map<String, String> errors) {
		// colections names fields
		       		
		Set<String> fields = errors.keySet();
		labelErrorName.setText(fields.contains("name") ? errors.get("name") : "");
		labelErrorEmail.setText(fields.contains("email") ? errors.get("email") : "");
		labelErrorBirthDate.setText(fields.contains("birthDate") ? errors.get("birthDate") : "");
		labelErrorBaseSalary.setText(fields.contains("BaseSalary") ? errors.get("BaseSalary") : "");

	}

	public void loadAssociateObjects() {
		if (departmentService == null) {
			throw new IllegalStateException("DepartmentService was null");
		}

		// load departments on list
		List<Department> list = departmentService.findAll();
		// load list in obsList
		obsList = FXCollections.observableArrayList(list);
		// set list comboBoxDepartment
		comboBoxDepartment.setItems(obsList);
	}

	private void initializeComboBoxDepartment() {
		Callback<ListView<Department>, ListCell<Department>> factory = lv -> new ListCell<Department>() {
			@Override
			protected void updateItem(Department item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty ? "" : item.getName());
			}
		};
		comboBoxDepartment.setCellFactory(factory);
		comboBoxDepartment.setButtonCell(factory.call(null));
	}

}
