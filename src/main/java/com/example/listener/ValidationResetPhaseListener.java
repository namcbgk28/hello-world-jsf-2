package com.example.listener;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.faces.component.UIInput;
import javax.faces.component.UIComponent;
import java.util.Iterator;

public class ValidationResetPhaseListener implements PhaseListener {
    
    private static final long serialVersionUID = 1L;

    @Override
    public void afterPhase(PhaseEvent event) {
        FacesContext facesContext = event.getFacesContext();
        
        if (facesContext.isValidationFailed()) {
            UIComponent viewRoot = facesContext.getViewRoot();
            if (viewRoot != null) {
                resetInvalidInputs(viewRoot);
            }
        }
    }

    @Override
    public void beforePhase(PhaseEvent event) {
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.PROCESS_VALIDATIONS;
    }
    
    private void resetInvalidInputs(UIComponent component) {
    	System.out.println("Running in resetInvalidInputs");
        if (component instanceof UIInput) {
            UIInput input = (UIInput) component;
            
            if (!input.isValid()) {
                Object lastValidValue = input.getValue();
                
                input.resetValue();
                
                input.setValue(lastValidValue);
                input.setValid(true);
                input.setSubmittedValue(null);
            }
        }
        
        Iterator<UIComponent> kids = component.getFacetsAndChildren();
        while (kids.hasNext()) {
            resetInvalidInputs(kids.next());
        }
    }
}