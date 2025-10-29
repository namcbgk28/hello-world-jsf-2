package com.example.bean;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

@Named("layoutBean")
@SessionScoped
public class NavigationBean implements Serializable{
private static final long serialVersionUID = 1L;
	
    private String currentPage = "/page/employee/list-employee.xhtml";

    public String getCurrentPage() {
        return currentPage;
    }

    public void loadPage(String pagePath) {
        this.currentPage = pagePath;
    }
}