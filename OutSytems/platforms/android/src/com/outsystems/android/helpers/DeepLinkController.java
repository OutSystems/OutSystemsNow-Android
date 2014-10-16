package com.outsystems.android.helpers;

import java.util.List;
import java.util.Map;

import android.net.Uri;

import com.outsystems.android.BaseActivity;
import com.outsystems.android.HubAppActivity;
import com.outsystems.android.LoginActivity;
import com.outsystems.android.model.DeepLink;

public class DeepLinkController {
	
	private static DeepLinkController _instance;
	
	private DeepLink deepLinkSettings;
	
	
	public DeepLinkController(){
		this.setDeepLinkSettings(new DeepLink());
	}
	
    public static DeepLinkController getInstance() {
        if (_instance == null) {
            _instance = new DeepLinkController();
        }
        return _instance;
    }
	

	public DeepLink getDeepLinkSettings() {
		return deepLinkSettings;
	}

	public void setDeepLinkSettings(DeepLink deepLinkSettings) {
		this.deepLinkSettings = deepLinkSettings;
	}
	
	public boolean hasValidSettings(){
		return this.getDeepLinkSettings() != null && this.getDeepLinkSettings().isValid();
	}
	

	
	public void createSettingsFromUrl(Uri uri){
		 if(uri == null){
			 this.getDeepLinkSettings().invalidateSettings();
			 return;
		 }
		 
		 String host = uri.getHost();
		 		 
		 List<String> pathSegments = uri.getPathSegments();
		 
		 if(pathSegments.size() == 0){
			 this.getDeepLinkSettings().invalidateSettings();
			 return;
		 }
		 
		 String path = pathSegments.get(0); 
		 
		 String query = uri.getQuery();

		 this.getDeepLinkSettings().createSettings(host, path, query);
		 
	}
	
	// Future work: Use Enumerators
	private boolean isLoginOperation(){
		String operation = this.deepLinkSettings.getOperation();
		return operation != null && operation.contains(DeepLink.KEY_LOGIN_OPERATION);
	}
	
	private boolean isOpenUrl(){
		String operation = this.deepLinkSettings.getOperation();
		return operation != null && operation.contains(DeepLink.KEY_OPEN_URL_OPERATION);
	}
	
	
	private boolean hasCredentials(){
		Map<String,String> parameters = this.deepLinkSettings.getParameters();
		if(parameters == null)
			return false;
		
		Object user = parameters.get(DeepLink.KEY_USERNAME_PARAMETER);
		Object pass = parameters.get(DeepLink.KEY_PASSWORD_PARAMETER);
		
		return user != null && pass != null && user.toString().length() > 0 && pass.toString().length() > 0;
	}
	
	private boolean hasUsername(){
		Map<String,String> parameters = this.deepLinkSettings.getParameters();
		if(parameters == null)
			return false;
		
		Object user = parameters.get(DeepLink.KEY_USERNAME_PARAMETER);
		
		return user != null && user.toString().length() > 0;
	}	
	
	
	public void resolveOperation(BaseActivity activity){
		if(this.isLoginOperation()){
		
			
			if(activity instanceof HubAppActivity){
				// HubAppActivity
				
				// Go to login page if it has credentials or username
				if(this.hasCredentials() || this.hasUsername()){
					
		        	String host = this.getDeepLinkSettings().getEnvironment();
		        	HubManagerHelper.getInstance().setApplicationHosted(host);
					
					HubAppActivity hubActivity = (HubAppActivity)activity;
					hubActivity.getInfrastructure = true;
				}
			}
			else{
				if(activity instanceof LoginActivity){
					LoginActivity loginActivity = (LoginActivity)activity;		
					// If it has credentials perform login
					if(this.hasCredentials()){										
						loginActivity.doLogin = true;
					}
					else{
						// Stay at login screen
						loginActivity.doLogin = false;
					}
					
				}
			}
			
		}
		else{
			if(this.isOpenUrl()){
				// TODO
			}
		}
			
	}
	
	public String getParameterValue(String key){
		String value = null;

		try{
		
			value = this.getDeepLinkSettings().getParameters().get(key);
		
		}catch(Exception e){
			value = null;
		}
		
		return value;
	}
	
	public void invalidate(){
		this.deepLinkSettings.invalidateSettings();
	}
}
