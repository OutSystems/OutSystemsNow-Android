package com.outsystems.android.helpers;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Intent;
import android.net.Uri;

import com.outsystems.android.ApplicationsActivity;
import com.outsystems.android.BaseActivity;
import com.outsystems.android.HubAppActivity;
import com.outsystems.android.LoginActivity;
import com.outsystems.android.WebApplicationActivity;
import com.outsystems.android.model.Application;
import com.outsystems.android.model.DeepLink;
import com.outsystems.android.model.HubApplicationModel;

public class DeepLinkController {
	
	private static DeepLinkController _instance;
	
	private DeepLink deepLinkSettings;
	
	private BaseActivity lastActivity;
	
	public DeepLinkController(){
		this.setDeepLinkSettings(new DeepLink());
		this.lastActivity = null;
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
		 
		 this.lastActivity = null;		 
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
	
	private boolean hasApplicationUrl(){
		Map<String,String> parameters = this.deepLinkSettings.getParameters();
		if(parameters == null)
			return false;
		
		Object url = parameters.get(DeepLink.KEY_URL_PARAMETER);
		
		return url != null && url.toString().length() > 0;		
	}
	
	
	public void resolveOperation(BaseActivity activity, Object[] params){
		
		switch(this.getDeepLinkSettings().getOperation()){
		
			case dlLoginOperation :
				// HubAppActivity
				if(activity instanceof HubAppActivity){
					if(lastActivity != null){
						this.invalidate();
						return;
					}
										
					// Go to login page if it has credentials or username
					if(this.hasCredentials() || this.hasUsername()){
						
			        	String host = this.getDeepLinkSettings().getEnvironment();
			        	HubManagerHelper.getInstance().setApplicationHosted(host);
						
						HubAppActivity hubActivity = (HubAppActivity)activity;
						hubActivity.getInfrastructure = true;
						
						lastActivity = hubActivity;
					}
				}
				else{
					// LoginActivity
					if(activity instanceof LoginActivity){

						if(!(lastActivity instanceof HubAppActivity))
						{
							if(!ApplicationSettingsController.getInstance().hasValidSettings() ) {
								this.invalidate();
								return;
							}
						}
						
						LoginActivity loginActivity = (LoginActivity)activity;		
						
						// Get HubApplicationModel
						if(params == null || !(params[0] instanceof HubApplicationModel)){
							loginActivity.doLogin = false;
							return;
						}
												
						HubApplicationModel hub = (HubApplicationModel)params[0];
			        
						String user = this.getParameterValue(DeepLink.KEY_USERNAME_PARAMETER);
						String pass = this.getParameterValue(DeepLink.KEY_PASSWORD_PARAMETER);
						
						hub.setUserName(user);
						hub.setPassword(pass);

						// If it has credentials perform login
						if(this.hasCredentials()){										
							loginActivity.doLogin = true;
						}
						else{
							// Stay at login screen
							loginActivity.doLogin = false;
						}					
						

						lastActivity = loginActivity;
					}
				}				
				
				break;
				
			case dlOpenUrlOperation:
				// HubAppActivity	
				if(activity instanceof HubAppActivity){
					if(lastActivity != null){
						this.invalidate();
						return;
					}
					
		        	String host = this.getDeepLinkSettings().getEnvironment();
		        	HubManagerHelper.getInstance().setApplicationHosted(host);
					
					HubAppActivity hubActivity = (HubAppActivity)activity;
					hubActivity.getInfrastructure = true;
					
					lastActivity = hubActivity;
				}
				else{
					// LoginActivity
					if(activity instanceof LoginActivity){
						
						if(!(lastActivity instanceof HubAppActivity)){
							if(!ApplicationSettingsController.getInstance().hasValidSettings() ) {
								this.invalidate();
								return;
							}
						}
						
						LoginActivity loginActivity = (LoginActivity)activity;		
						
						// Get HubApplicationModel
						if(params == null || !(params[0] instanceof HubApplicationModel)){
							loginActivity.doLogin = false;
							return;
						}
												
						HubApplicationModel hub = (HubApplicationModel)params[0];
			        
						// Login with the given credentials 
						if(this.hasCredentials()){
							String user = this.getParameterValue(DeepLink.KEY_USERNAME_PARAMETER);
				            String pass = this.getParameterValue(DeepLink.KEY_PASSWORD_PARAMETER);
				            				        		
					        hub.setUserName(user);
					        hub.setPassword(pass);    
					        
					        loginActivity.doLogin = true;
						}
						else{
							// Stay at login screen
							loginActivity.doLogin = false;
							
							// Login with the stored credentials if exists
							if(hub.getUserName() != null && hub.getUserName().length() > 0 && 
							   hub.getPassword() != null && hub.getPassword().length() > 0){
								loginActivity.doLogin = true;								
							}
						}		              

						lastActivity = loginActivity;
					}
					else{
						if(activity instanceof ApplicationsActivity){

							if(!(lastActivity instanceof LoginActivity)){
								if(!ApplicationSettingsController.getInstance().hasValidSettings() ) {
									this.invalidate();
									return;
								}
							}
							
							ApplicationsActivity appActivity = (ApplicationsActivity)activity;				
							lastActivity = appActivity;
							
							if(this.hasApplicationUrl()){
								String url = this.getParameterValue(DeepLink.KEY_URL_PARAMETER);
																
								// Ensure that the url format its correct
								String applicationName = url.replace("\\", "/");
								
								// Get the application's name
								if(applicationName.contains("/")){
									
									while(applicationName.startsWith("/")){
										applicationName = applicationName.substring(1);
									}
									
									url = applicationName;
									
									int slashPosition = applicationName.indexOf("/");
									
									if(slashPosition > 0 ){
										applicationName = applicationName.substring(0,slashPosition);
									}
								}
																
								Application application = new Application(applicationName, -1, applicationName);									
								application.setPath(url);
								
								Intent intent = new Intent(appActivity.getApplicationContext(), WebApplicationActivity.class);							
					            intent.putExtra(WebApplicationActivity.KEY_APPLICATION, application);

					            appActivity.startActivity(intent);
								
							}
						}
					
					}
				}
				
				break;
				
			default:
				// Do nothing
				break;		
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


	/**
	 * Method to extract from url string the data to build Uri
	 *
	 * @param urlToOpen
	 * @return URI with data from link sent by push notification
	 */
	public static Uri convertUrlToUri(String urlToOpen, String scheme) {
		Uri.Builder builder = new Uri.Builder();
		builder.scheme(scheme);
		builder.appendPath("openurl");

		Pattern p = Pattern.compile("^\\/?([^:\\/\\s]+)((\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+)(.*)?(#[\\w\\-]+)?$");
		Matcher m = p.matcher(urlToOpen);
		if (m.find()) {
			if (m.groupCount() > 0) {
				if (m.group(1) != null) {
					builder.authority(m.group(1));
				}
				if (m.group(2) != null) {
					String applicationName = m.group(2);

					if (applicationName.substring(0, 1).equals("/")) {
						applicationName = applicationName.substring(1, applicationName.length());
					}
					builder.appendQueryParameter("url", applicationName + m.group(4) + m.group(5));
				}
			}
		}
		return builder.build();
	}
}
