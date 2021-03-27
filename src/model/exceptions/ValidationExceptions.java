package model.exceptions;

import java.util.HashMap;
import java.util.Map;

public class ValidationExceptions extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	
	private Map<String, String> errors = new HashMap<>();

	public ValidationExceptions(String msg) {
		super(msg);
	}
	
	public Map<String, String> getErros(){
		return errors;
	}
	
	public void addErros(String fieldName, String errorMessange) {
		errors.put(fieldName, errorMessange);
	}

}
