package com.example.validator.employee;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import com.example.dao.EmployeeDAO;

@FacesValidator("employeeValidator")
public class EmployeeCodeValidator implements Validator {

	@Override
	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		if (value == null || value.toString().trim().isEmpty()) {
			return;
		}

		String employeeCode = value.toString().trim();

		EmployeeDAO employeeDAO = new EmployeeDAO();
		if (employeeDAO.isEmployeeIdExist(employeeCode)) {
			FacesMessage msg = new FacesMessage("Duplicate Employee Code",
					"Employee Code '" + employeeCode + "' already exist");
			throw new ValidatorException(msg);
		}
	}
}
