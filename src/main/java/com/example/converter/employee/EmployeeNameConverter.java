package com.example.converter.employee;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("employeeNameConverter")
public class EmployeeNameConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        String normalized = value.trim().replaceAll("\\s+", " ");
        return convertToSnakeCase(normalized);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null) {
            return "";
        }
        
        String dbValue = value.toString();
        return convertToProperCase(dbValue);
    }

    private String convertToProperCase(String snakeCase) {
        if (snakeCase == null || snakeCase.trim().isEmpty()) {
            return "";
        }
        
        String normalized = snakeCase.trim().replaceAll("_+", "_");
        String[] parts = normalized.split("_");
        StringBuilder result = new StringBuilder();
        
        for (String part : parts) {
            if (!part.isEmpty()) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(capitalizeFirstLetter(part.toLowerCase()));
            }
        }
        
        return result.toString();
    }

    private String convertToSnakeCase(String properCase) {
        if (properCase == null || properCase.trim().isEmpty()) {
            return "";
        }
        
        String normalized = properCase.trim().replaceAll("\\s+", " ");
        String[] words = normalized.split(" ");
        
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < words.length; i++) {
            if (!words[i].isEmpty()) {
                if (i > 0) {
                    result.append("_");
                }
                result.append(words[i].toLowerCase());
            }
        }
        
        return result.toString();
    }

    private String capitalizeFirstLetter(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }
}