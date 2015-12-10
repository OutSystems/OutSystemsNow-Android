package com.outsystems.android.model;

import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.cordova.LOG;

public class DeepLink {
	
	public static final String KEY_LOGIN_OPERATION = "login";
	public static final String KEY_OPEN_URL_OPERATION = "openurl";
	
	public static final String KEY_USERNAME_PARAMETER = "username";
	public static final String KEY_PASSWORD_PARAMETER = "password";
	public static final String KEY_URL_PARAMETER = "url";
	
	
	private static final String TAG = "DeepLink";

	private String environment;
	private DLOperationType operation;
	private boolean isValid;
	


	private Map<String,String> parameters;

	
	public DeepLink(){
		this.setValid(false);
		this.setParameters(new HashMap<String,String>());
	}
	
	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public DLOperationType getOperation() {
		return operation;
	}

	public void setOperation(DLOperationType operation) {
		this.operation = operation;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}
	
	public boolean isValid() {
		return isValid;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	
	public void createSettings(String environment, String operation, String parameters){

		if(this.getParameters() == null)
			this.setParameters(new HashMap<String,String>());
		else
			this.getParameters().clear();
		
		if(environment != null && operation != null && environment.length() > 0 && operation.length() > 0 ){
			this.setValid(true);
		}
		else{
			this.setValid(false);
			this.setEnvironment(null);
			this.setOperation(null);
					
			return;
		}
		
		
		this.setEnvironment(environment);
		this.setOperation(DLOperationType.getOperationType(operation.toLowerCase()));
		
		LOG.v(TAG, "Deep Link - Environment: "+environment);
		LOG.v(TAG, "Deep Link - Operation: "+operation);
			
		
		if(parameters != null){

			// Get username parameter
			int paramIndex = parameters.indexOf(KEY_USERNAME_PARAMETER+"=");
			if(paramIndex >= 0){
				String userStr = parameters.substring(paramIndex);
				int endOfParam = userStr.indexOf("&");
				String paramString = userStr.substring(0,endOfParam);
				this.addParameter(paramString);
			}

			// Get password parameter
			paramIndex = parameters.indexOf(KEY_PASSWORD_PARAMETER+"=");
			if(paramIndex >= 0){
				String pwdStr = parameters.substring(paramIndex);
				int endOfParam = pwdStr.indexOf("&");
				String paramString = pwdStr.substring(0,endOfParam);
				this.addParameter(paramString);
			}

			// Get url parameter
			paramIndex = parameters.indexOf(KEY_URL_PARAMETER+"=");
			if(paramIndex >= 0){
				String urlStr = parameters.substring(paramIndex);
				this.addParameter(urlStr);
			}

		}
	}
	
	private void addParameter(String parameter){
		
		if(this.getParameters() == null)
			this.setParameters(new HashMap<String,String>());

        int separator = parameter.indexOf("=");

		
		if (separator < 0)
			return;

		String key = parameter.substring(0,separator).toLowerCase();
		String value = parameter.substring(separator+1);
		
		this.getParameters().put(key, value);
	}
	
	public void invalidateSettings(){
		this.setValid(false);
	}	
	
}
