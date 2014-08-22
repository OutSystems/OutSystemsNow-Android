/*
 * OutSystems Project
 * 
 * Copyright (C) 2014 OutSystems.
 * 
 * This software is proprietary.
 */
package com.outsystems.android.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.Header;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.MySSLSocketFactory;
import com.loopj.android.http.RequestParams;
import com.outsystems.android.core.parsing.GenericResponseParsingTask;
import com.outsystems.android.helpers.HubManagerHelper;
import com.outsystems.android.model.Application;
import com.outsystems.android.model.Infrastructure;
import com.outsystems.android.model.Login;

/**
 * Class description.
 * 
 * @author <a href="mailto:vmfo@xpand-it.com">vmfo</a>
 * @version $Revision: 666 $
 * 
 */
public class WebServicesClient {

	public static final String URL_WEB_APPLICATION = "https://%1$s/%2$s";
	public static final String BASE_URL = "https://%1$s/OutSystemsNowService/";
	public static String DEMO_HOST_NAME = "your.demo.server";	

	private static volatile WebServicesClient instance = null;
	private AsyncHttpClient client = null;

	private List<String> trustedHosts;

	// private constructor
	private WebServicesClient() {
		client = new AsyncHttpClient();

		trustedHosts = new ArrayList<String>();
		trustedHosts.add("outsystems.com");
		trustedHosts.add("outsystems.net");
		trustedHosts.add("outsystemscloud.com");
	}
	
	public static String PrettyErrorMessage(int statusCode) {
		switch(statusCode) {
		case -1001:
			return "The request timed out.";
		case -1003:
			return "Could not contact the specified server. Please verify the server name and your internet connection and try again.";
		case -1206:
			return "An SSL error has occurred and a secure connection to the server cannot be made.";
		case 404:
			return "The required OutSystems Now service was not detected. If the location entered above is accurate, please check the instructions on preparing your installation at labs.outsystems.net/Native.";		
		default:
			return "There was an error trying to connect to the provided environment, please try again.";		
		}
	}

	public static WebServicesClient getInstance() {
		instance = new WebServicesClient();
		return instance;
	}

	public static String getAbsoluteUrl(String hubApp, String relativeUrl) {
		return String.format(BASE_URL, hubApp) + relativeUrl
				+ getApplicationServer();
	}

	public static String getAbsoluteUrlForImage(String hubApp, int idImage) {
		return String.format(BASE_URL, hubApp) + "applicationImage"
				+ getApplicationServer() + "?id=" + idImage;
	}

	public static String getApplicationServer() {
		boolean jsfApplicationServer = HubManagerHelper.getInstance()
				.isJSFApplicationServer();
		if (jsfApplicationServer) {
			return ".jsf";
		} else {
			return ".aspx";
		}
	}

	// post for content parameters
	private void post(String hubApp, String urlPath,
			HashMap<String, String> parameters,
			AsyncHttpResponseHandler asyncHttpResponseHandler) {

		RequestParams params = null;
		if (parameters != null) {
			params = new RequestParams(parameters);
		}

		// TODO remove comments to force the check the validity of SSL
		// certificates, except for list of trusted servers
		// if (trustedHosts != null && hubApp != null) {
		// for (String trustedHost : trustedHosts) {
		// if (hubApp.contains(trustedHost)) {
		client.setSSLSocketFactory(getSSLMySSLSocketFactory());
		// break;
		// }
		// }
		// }

		client.post(getAbsoluteUrl(hubApp, urlPath), params,
				asyncHttpResponseHandler);
	}

	private void get(String hubApp, String urlPath,
			HashMap<String, String> parameters,
			AsyncHttpResponseHandler asyncHttpResponseHandler) {
		RequestParams params = null;
		if (parameters != null) {
			params = new RequestParams(parameters);
		}

		// TODO remove comments to force the check the validity of SSL
		// certificates, except for list of trusted servers
		// if (trustedHosts != null && hubApp != null) {
		// for (String trustedHost : trustedHosts) {
		// if (hubApp.contains(trustedHost)) {
		client.setSSLSocketFactory(getSSLMySSLSocketFactory());
		// break;
		// }
		// }
		// }
		client.get(getAbsoluteUrl(hubApp, urlPath), params,
				asyncHttpResponseHandler);
	}

	public void getInfrastructure(final String urlHubApp,
			final WSRequestHandler handler) {
				
		get(urlHubApp, "infrastructure", null, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(final int statusCode, Header[] headers,
					final byte[] content) {
				if (statusCode != 200) {
					handler.requestFinish(null, true, statusCode);
				} else {

					new GenericResponseParsingTask() {
						@Override
						public Object parsingMethod() {

							String contentString = "";
							try {
								contentString = new String(content, "UTF-8");
							} catch (UnsupportedEncodingException e) {
								EventLogger.logError(getClass(), e);
							}

							try {
								Gson gson = new Gson();

								Infrastructure infrastructure = gson.fromJson(
										contentString, Infrastructure.class);

								return infrastructure;
							} catch (JsonSyntaxException e) {
								EventLogger.logError(getClass(), e);
							}
							return null;
						}

						@Override
						public void parsingFinishMethod(Object result) {
							handler.requestFinish(result, false, statusCode);
						}
					}.execute();

				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				EventLogger.logMessage(getClass(), error.toString() + " "
						+ statusCode);				
				
				if (statusCode == 404
						&& !HubManagerHelper.getInstance()
								.isJSFApplicationServer()) {
					HubManagerHelper.getInstance()
							.setJSFApplicationServer(true);
					getInfrastructure(urlHubApp, handler);
				} else {					
					if(error.getMessage().indexOf("UnknownHostException") != -1) {
						statusCode = -1003; // NSURLErrorCannotFindHost
					} else if (error.getMessage().indexOf("SSL handshake timed out") != -1) {
						statusCode = -1206; // NSURLErrorClientCertificateRequired
					} else if (error.getMessage().indexOf("SocketTimeoutException") != -1) {
						statusCode = -1001; // NSURLErrorTimedOut 
					}
					
					handler.requestFinish(null, true, statusCode);
				}
			}
		});
	}

	public void loginPlattform(final String username, final String password,
			final String device, final WSRequestHandler handler) {
		if (username == null || password == null) {
			handler.requestFinish(null, true, -1);
			return;
		}

		HashMap<String, String> param = new HashMap<String, String>();
		param.put("username", username);
		param.put("password", password);
		param.put("device", device);
		param.put("devicetype", "android");

		post(HubManagerHelper.getInstance().getApplicationHosted(), "login",
				param, new AsyncHttpResponseHandler() {

					@Override
					public void onFailure(int statusCode, Header[] headers,
							byte[] responseBody, Throwable arg3) {
						if (statusCode == 404
								&& !HubManagerHelper.getInstance()
										.isJSFApplicationServer()) {
							HubManagerHelper.getInstance()
									.setJSFApplicationServer(true);
							loginPlattform(username, password, device, handler);
						} else {
							if(arg3.getMessage().indexOf("UnknownHostException") != -1) {
								statusCode = -1003; // NSURLErrorCannotFindHost
							} else if (arg3.getMessage().indexOf("SSL handshake timed out") != -1) {
								statusCode = -1206; // NSURLErrorClientCertificateRequired
							} else if (arg3.getMessage().indexOf("SocketTimeoutException") != -1) {
								statusCode = -1001; // NSURLErrorTimedOut 
							}
							
							handler.requestFinish(null, true, statusCode);
						}
					}

					@Override
					public void onSuccess(final int statusCode,
							Header[] headers, final byte[] content) {
						if (statusCode != 200) {
							handler.requestFinish(null, true, statusCode);
						} else {
							new GenericResponseParsingTask() {
								@Override
								public Object parsingMethod() {
									String contentString = "";
									try {
										contentString = new String(content,
												"UTF-8");
									} catch (UnsupportedEncodingException e) {
										EventLogger.logError(getClass(), e);
									}
									try {
										Gson gson = new Gson();
										Login login = gson.fromJson(
												contentString, Login.class);

										return login;
									} catch (JsonSyntaxException e) {
										EventLogger.logError(getClass(), e);
									}
									return null;
								}

								@Override
								public void parsingFinishMethod(Object result) {
									if (statusCode == 404
											&& !HubManagerHelper.getInstance()
													.isJSFApplicationServer()) {
										HubManagerHelper.getInstance()
												.setJSFApplicationServer(true);
										loginPlattform(username, password,
												device, handler);
									} else {
										handler.requestFinish(result, false,
												statusCode);
									}

								}
							}.execute();
						}
					}
				});
	}

	public void registerToken(final String device,
			final WSRequestHandler handler) {
		if (device == null) {
			handler.requestFinish(null, true, -1);
			return;
		}

		HashMap<String, String> param = new HashMap<String, String>();
		param.put("device", device);
		param.put("devicetype", "android");

		get(HubManagerHelper.getInstance().getApplicationHosted(),
				"registertoken", param, new AsyncHttpResponseHandler() {

					@Override
					public void onFailure(int statusCode, Header[] headers,
							byte[] responseBody, Throwable arg3) {
						handler.requestFinish(null, true, statusCode);
					}

					@Override
					public void onSuccess(final int statusCode,
							Header[] headers, final byte[] content) {
						if (statusCode != 200) {
							handler.requestFinish(null, true, statusCode);
						} else {
							handler.requestFinish(null, false, statusCode);
						}
					}
				});
	}

	public void getApplications(final String urlHubApp,
			final WSRequestHandler handler) {
		get(urlHubApp, "applications", null, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(final int statusCode, Header[] headers,
					final byte[] content) {
				if (statusCode != 200) {
					handler.requestFinish(null, true, statusCode);
				} else {

					new GenericResponseParsingTask() {
						@Override
						public Object parsingMethod() {

							String contentString = "";
							try {
								contentString = new String(content, "UTF-8");
							} catch (UnsupportedEncodingException e) {
								EventLogger.logError(getClass(), e);
							}

							try {
								Gson gson = new Gson();
								Type collectionType = new TypeToken<List<Application>>() {
								}.getType();
								List<Application> applications = gson.fromJson(
										contentString, collectionType);

								return applications;
							} catch (JsonSyntaxException e) {
								EventLogger.logError(getClass(), e);
							}
							return null;
						}

						@Override
						public void parsingFinishMethod(Object result) {
							handler.requestFinish(result, false, statusCode);
						}
					}.execute();

				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				EventLogger.logMessage(getClass(), error.toString() + " "
						+ statusCode);
				if (statusCode == 404
						&& !HubManagerHelper.getInstance()
								.isJSFApplicationServer()) {
					HubManagerHelper.getInstance()
							.setJSFApplicationServer(true);
					getApplications(urlHubApp, handler);
				} else {
					handler.requestFinish(null, true, statusCode);
				}
			}
		});
	}

	private MySSLSocketFactory getSSLMySSLSocketFactory() {
		KeyStore trustStore = null;
		try {
			trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);
		} catch (NoSuchAlgorithmException e) {
			EventLogger.logError(getClass(), e);
		} catch (CertificateException e) {
			EventLogger.logError(getClass(), e);
		} catch (IOException e) {
			EventLogger.logError(getClass(), e);
		} catch (KeyStoreException e1) {
			EventLogger.logError(getClass(), e1);
		}
		MySSLSocketFactory sf = null;
		try {
			sf = new MySSLSocketFactory(trustStore);
		} catch (KeyManagementException e) {
			EventLogger.logError(getClass(), e);
		} catch (UnrecoverableKeyException e) {
			EventLogger.logError(getClass(), e);
		} catch (NoSuchAlgorithmException e) {
			EventLogger.logError(getClass(), e);
		} catch (KeyStoreException e) {
			EventLogger.logError(getClass(), e);
		}
		sf.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		return sf;
	}

	/**
	 * Gets the trusted hosts.
	 * 
	 * @return the trusted hosts
	 */
	public List<String> getTrustedHosts() {
		return trustedHosts;
	}

	/**
	 * Sets the trusted hosts.
	 * 
	 * @param trustedHosts
	 *            the new trusted hosts
	 */
	public void setTrustedHosts(List<String> trustedHosts) {
		this.trustedHosts = trustedHosts;
	}
}
