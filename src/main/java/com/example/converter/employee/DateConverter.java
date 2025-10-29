package com.example.converter.employee;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

@FacesConverter("dateConverter")
public class DateConverter implements Converter {

	private static final String DISPLAY_FORMAT = "dd/MM/yyyy";

	private static final String[] PARSE_FORMATS = { "dd/MM/yyyy", "yyyy-MM-dd", "dd-MM-yyyy" };

	private static final String DATE_FORMAT = "dd/MM/yyyy";

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		System.out.println("Running in getAsObject, before: " + value);
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		String trimmedValue = value.trim();
		 Date originalDate = (Date) component.getAttributes().get("originalValue");
		component.getAttributes().put("submittedValueForMessage", trimmedValue);
		for (String format : PARSE_FORMATS) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat(format);
				sdf.setLenient(false);
				Date parsedDate = sdf.parse(value.trim());
				System.out.println("Running in getAsObject, after: " + sdf.format(parsedDate));
				component.getAttributes().remove("submittedValueForMessage");
				return parsedDate;
			} catch (ParseException e) {
			}
		}

//		return null;
		resetInputInConverter(context, component, originalDate, trimmedValue);
		return null;
//		throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
//				"Invalid date format. Please use dd/MM/yyyy. You entered: " + value, null));
	}

	private void resetInputInConverter(FacesContext context, UIComponent component, Date originalDate,
			String invalidValue) {
		UIInput input = (UIInput) component;

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		if (originalDate != null) {
			input.setSubmittedValue(dateFormat.format(originalDate));
			input.setValue(originalDate);
		} else {
			input.setSubmittedValue("");
			input.setValue(null);
		}
		
		input.setValid(false);
		
		throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
				"Invalid date format. Please use dd/MM/yyyy. You entered: " + invalidValue, null));
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		System.out.println("Running in getAsString, before: " + value);
		if (value == null) {
			return "";
		}

		if (value instanceof Date) {
			SimpleDateFormat sdf = new SimpleDateFormat(DISPLAY_FORMAT);
			System.out.println("Running in getAsString, after: " + sdf.format((Date) value));
			return sdf.format((Date) value);
		}

		return value.toString();
	}
}
