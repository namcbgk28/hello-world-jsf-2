package com.example.bean.employee;

import com.example.bean.NavigationBean;
import com.example.dao.EmployeeDAO;
import com.example.model.Employee;
import com.example.model.Mode;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Named("employeeBean")
@ViewScoped
public class EmployeeBean implements Serializable {

	private static final String PAGE_EMPLOYEE_BASE = "/page/employee";
	private static final String PAGE_LIST_EMPLOYEE = PAGE_EMPLOYEE_BASE + "/list-employee.xhtml";
	private static final String PAGE_ADD_EMPLOYEE = PAGE_EMPLOYEE_BASE + "/add-employee.xhtml";
	private static final String PAGE_UPDATE_EMPLOYEE = PAGE_EMPLOYEE_BASE + "/update-employee.xhtml";


	private static final long serialVersionUID = 1L;

	private List<Employee> employees;

	private EmployeeDAO employeeDAO;
	private String message;
	private String searchKeyword;

	private Employee newEmployee;
	private Employee selectedEmployee;
	private Employee deleteEmployee;

	private int currentPage = 1;
	private int pageSize = 5;
	private int totalRecords = 0;
	private int totalPages = 0;
	private Mode mode;

	@Inject
	private NavigationBean layoutBean;

	public EmployeeBean() {
	}

	@PostConstruct
	public void init() {
		mode = Mode.LIST;
		employeeDAO = new EmployeeDAO();
		newEmployee = new Employee();
		loadAllEmployees();
	}

	public void loadAllEmployees() {
		clearFilter();
		loadEmployees(currentPage, pageSize);
	}

	public void loadEmployees(int pageIndex, int pageSize) {
		try {
			String keyword = null;
			if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
				keyword = searchKeyword.trim();
			}

			totalRecords = employeeDAO.getTotalEmployeeCount(keyword);
			totalPages = (int) Math.ceil((double) totalRecords / pageSize);
			employees = employeeDAO.getAllEmployees(pageIndex, pageSize, keyword);
			currentPage = pageIndex;

			message = "Page " + currentPage + " of " + totalPages + " (Total: " + totalRecords + " employees)";
		} catch (Exception e) {
			message = "Error loading data: " + e.getMessage();
			e.printStackTrace();
			employees = new ArrayList<>();
		}
	}

	public List<Employee> getEmployees() {
		return employees;
	}

	public void clearFilter() {
		searchKeyword = "";
	}

	public void prepareNewEmployee() {
		mode = Mode.ADD;
		this.newEmployee = new Employee();
	}

	public void validateEmployeeName(FacesContext context, UIComponent component, Object value)
	        throws ValidatorException {
	    UIInput input = (UIInput) component;
	    String raw = input.getSubmittedValue() != null ? input.getSubmittedValue().toString() : null;

	    if (raw == null || raw.trim().isEmpty()) {
	        return; // để requiredMessage xử lý
	    }

	    String normalized = raw.trim().replaceAll("\\s+", " ");
	    boolean formatOk = normalized.matches("^[\\p{L} ]{2,50}$");
	    if (!formatOk) {
	        FacesMessage msg = new FacesMessage(
	                FacesMessage.SEVERITY_ERROR,
	                String.format("Employee Name must contain only letters and spaces (2-50 characters). You entered: '%s'", raw),
	                null);
	        System.out.println(">>> THROWING ValidatorException with message: " + msg.getSummary());
	        throw new ValidatorException(msg);
	    }

	    
	}


	public void validateDateOfBirth(FacesContext context, UIComponent component, Object value)
			throws ValidatorException {

		UIInput input = (UIInput) component;
		if (value == null) {
			String submittedValue = (String) input.getAttributes().get("submittedValueForMessage");
			if (submittedValue == null) {
				Object rawSubmitted = input.getSubmittedValue();
				submittedValue = rawSubmitted != null ? rawSubmitted.toString() : "";
			}
			String message = "Invalid date format. Please use dd/MM/yyyy.";
			if (submittedValue != null && !submittedValue.trim().isEmpty()) {
				message += " You entered: " + submittedValue;
			}
			resetInputDobAndThrow(input, message);
		}

		String enteredValue = value.toString();

		Date dateOfBirth = (Date)value;
		System.out.println("Running in validateDateOfBirth: " + dateOfBirth + "| " + input.getId());

		// Validate date rules
		LocalDate dob = dateOfBirth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate today = LocalDate.now();

		if (dob.isAfter(today)) {
			resetInputDobAndThrow(input, "Date of Birth cannot be in the future. You entered: " + enteredValue);
		}

		int calculatedAge = Period.between(dob, today).getYears();

		if (calculatedAge < 18) {
			resetInputDobAndThrow(input, "Employee must be at least 18 years old. You entered: " + enteredValue);
		}

		if (calculatedAge > 100) {
			resetInputDobAndThrow(input, "Invalid Date of Birth (age exceeds 100 years). You entered: " + enteredValue);
		}

		validateAgeConsistency(context, component, input, calculatedAge);
	}

	public void resetInputDobAndThrow(UIInput input, String errorMessage) {
		Date originalDate = (mode == Mode.ADD) ? newEmployee.getDateOfBirth() : selectedEmployee.getDateOfBirth();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

		if (originalDate != null) {
			input.setSubmittedValue(dateFormat.format(originalDate));
		} else {
			input.setSubmittedValue("");
		}

		input.setValid(false);
		throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, errorMessage, null));
	}

	private void validateAgeConsistency(FacesContext context, UIComponent component, UIInput input, int calculatedAge) {
		UIComponent ageComponent = component.findComponent("employeeAge");
		if (ageComponent == null) {
			return;
		}

		Object submittedAge = ((UIInput) ageComponent).getSubmittedValue();
		if (submittedAge == null || submittedAge.toString().trim().isEmpty()) {
			return;
		}

		try {
			int inputAge = Integer.parseInt(submittedAge.toString());
			if (inputAge != calculatedAge) {
				resetInputDobAndThrow(input,
						"Age does not match Date of Birth. Calculated age from date of birth is: " + calculatedAge);
			}
		} catch (NumberFormatException e) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Invalid age format. Please enter a valid number.", null);
			context.addMessage(ageComponent.getClientId(context), msg);
			((UIInput) ageComponent).setValid(false);
			throw new ValidatorException(new FacesMessage(""));
		}
	}

	public void saveEmployee() {
		FacesContext context = FacesContext.getCurrentInstance();
		try {
			boolean success = employeeDAO.addEmployee(newEmployee);
			newEmployee = new Employee();
			if (success) {
				loadEmployees(1, pageSize);
				context.addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Employee added successfully!"));
				gotoListPage();
			}
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
					"Failed to add employee: " + e.getMessage()));
			e.printStackTrace();
		}
	}

	public void updateEmployee() {
		// In ra tất cả message sau validation (debug)
	    FacesContext fc = FacesContext.getCurrentInstance();
	    for (FacesMessage msg : fc.getMessageList()) {
	        System.out.println("JSF Message in Context: " + msg.getSummary());
	    }
		FacesContext context = FacesContext.getCurrentInstance();
		try {
			boolean success = employeeDAO.updateEmployee(selectedEmployee);
			if (success) {
				loadEmployees(1, pageSize);
				context.addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Employee updated successfully!"));
				gotoListPage();
			} else {
				context.addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to update employee"));
			}
		} catch (Exception e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
		}
	}

	public void searchEmployees() {
		loadEmployees(1, pageSize);
	}

	public void prepareUpdateEmployee(Employee employee) {
		mode = Mode.EDIT;
		selectedEmployee = new Employee();
		selectedEmployee.setEmployeeCode(employee.getEmployeeCode());
		selectedEmployee.setEmployeeName(employee.getEmployeeName());
		selectedEmployee.setEmployeeAge(employee.getEmployeeAge());
		selectedEmployee.setDateOfBirth(employee.getDateOfBirth());
	}

	public void editEmployee(Employee emp) {
		prepareUpdateEmployee(emp);
		gotoUpdatePage();
	}

	public void prepareDeleteEmployee(Employee emp) {
		deleteEmployee = emp;
	}

	public void deleteEmployeeDialog() {
		deleteEmployee(deleteEmployee.getEmployeeCode());
	}

	public void deleteEmployee(String employeeCode) {
		FacesContext context = FacesContext.getCurrentInstance();
		try {
			boolean success = employeeDAO.deleteEmployee(employeeCode);
			if (success) {
				deleteEmployee = new Employee();
				loadEmployees(currentPage, pageSize);
				context.addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Employee deleted successfully!"));
			} else {
				context.addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to delete employee"));
			}
		} catch (Exception e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
		}
	}

	public List<Integer> getPaginationPages() {
		List<Integer> pages = new ArrayList<>();
		int maxVisiblePages = 5;
		int startPage = Math.max(1, currentPage - maxVisiblePages / 2);
		int endPage = Math.min(totalPages, startPage + maxVisiblePages - 1);

		if (endPage - startPage < maxVisiblePages - 1) {
			startPage = Math.max(1, endPage - maxVisiblePages + 1);
		}

		for (int i = startPage; i <= endPage; i++) {
			pages.add(i);
		}

		return pages;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Employee getNewEmployee() {
		return newEmployee;
	}

	public void setNewEmployee(Employee newEmployee) {
		this.newEmployee = newEmployee;
	}

	public Employee getSelectedEmployee() {
		return selectedEmployee;
	}

	public void setSelectedEmployee(Employee selectedEmployee) {
		this.selectedEmployee = selectedEmployee;
	}

	public String getSearchKeyword() {
		return searchKeyword;
	}

	public void setSearchKeyword(String searchKeyword) {
		this.searchKeyword = searchKeyword;
	}

	public void gotoListPage() {
		mode = Mode.LIST;
		layoutBean.loadPage(PAGE_LIST_EMPLOYEE);
	}

	public void gotoAddPage() {
		mode = Mode.ADD;
		newEmployee = new Employee();
		layoutBean.loadPage(PAGE_ADD_EMPLOYEE);
	}

	public void gotoUpdatePage() {
		mode = Mode.EDIT;
		layoutBean.loadPage(PAGE_UPDATE_EMPLOYEE);
	}

	public void goToPage(int page) {
		if (page >= 1 && page <= totalPages) {
			loadEmployees(page, pageSize);
		}
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
		currentPage = 1;
		loadEmployees(currentPage, pageSize);
	}

	public int getTotalRecords() {
		return totalRecords;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public Employee getDeleteEmployee() {
		return deleteEmployee;
	}

	public void setDeleteEmployee(Employee deleteEmployee) {
		this.deleteEmployee = deleteEmployee;
	}
}
