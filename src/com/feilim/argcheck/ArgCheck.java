package com.feilim.argcheck;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class ArgCheck 
{
	private String _pushoverAppToken;
	private String _pushoverUserToken;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArgCheck ac = new ArgCheck();
		ac.initialise();
		ac.checkAllStores();
	}
	
	private void initialise() 
	{
		Properties prop = new Properties();
		try 
		{
			// load our properties file
			prop.load(new FileInputStream("config.properties"));

			// get the property values for pushover notifications if any
			_pushoverAppToken = prop.getProperty("PUSHOVER_APP_TOKEN");
			_pushoverUserToken = prop.getProperty("PUSHOVER_USER_TOKEN");
		} 
		catch (IOException ex) 
		{
			ex.printStackTrace();
		}
	}
	
	private void checkAllStores()
	{
		for (Store s : Store.values()) 
		{
			checkAllPS4s(s);
		}
	}

	private void checkAllPS4s(Store s)
	{
		for (Ps4 ps4 : Ps4.values())
		{
			checkPS4Status(s, ps4);		
		}
	}

	private void checkPS4Status(Store s, Ps4 ps4) 
	{
		HttpResponse response = getStockCheckResponse(s, ps4);
		if (response != null)
		{
			StockWrapper sw = parseResponse(response);
			if (sw != null)
			{
				System.out.println(">>> Store: " + s.getName() + ", Item: " 
						+ ps4.getName() + " ["+ps4.getCode()+"], Status: " + sw._status.getStatus());
				switch (sw._status)
				{
				case IN_STOCK:
					sendNotification(s, ps4, sw._quantity);
					break;
				}
			}
		}
	}

	private void sendNotification(Store s, Ps4 ps4, int quantity) 
	{
		if (_pushoverAppToken == null || _pushoverAppToken.isEmpty() || 
				_pushoverUserToken == null || _pushoverUserToken.isEmpty())
		{
			System.out.println("Cannot send Pushover notification as no " +
					"configuration tokens were set in 'config.properties'!");
			return;
		}
		
		Pushover p = new Pushover(_pushoverAppToken, _pushoverUserToken);
		StringBuilder sb = new StringBuilder();
		sb.append(ps4.getCode()).append(" In Stock! Store: ").append(s.getName());
		sb.append(", Quantity: ").append(quantity);
		
		System.out.println("\nArgCheck.sendNotification() :: Sending Notification! Details:");
		System.out.println(sb.toString());
		try 
		{
		    p.sendMessage(sb.toString());
		} 
		catch (IOException e) 
		{
		    e.printStackTrace();
		}		
	}

	private StockWrapper parseResponse(HttpResponse response)
	{
		StringBuffer parsedStatus = new StringBuffer();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) 
			{
				parsedStatus.append(line);
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String status = parsedStatus.toString().toLowerCase();
		if (!status.isEmpty())
		{
			if (status.contains(StockStatus.IN_STOCK.getStatus()))
			{
				return new StockWrapper(StockStatus.IN_STOCK, parseQuantity(status));
			}
			else if (status.contains(StockStatus.OUT_OF_STOCK.getStatus()))
			{
				return new StockWrapper(StockStatus.OUT_OF_STOCK, 0);
			}
			else if (status.contains(StockStatus.UNKNOWN_STATUS.getStatus()))
			{
				return new StockWrapper(StockStatus.UNKNOWN_STATUS, 0);
			}
		}
		
		return null;
	}
	
	private int parseQuantity(String status) 
	{
		Pattern p = Pattern.compile("\\d+");
		Matcher m = p.matcher(status);
		if (m.find())
		{
			String d = m.group();
			return Integer.parseInt(d);
		}
		
		return 0;
	}

	private HttpResponse getStockCheckResponse(Store store, Ps4 ps4)
	{
		HttpResponse response = null;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet("http://checkargos.com/StockCheckBackground.php?NI=false&productId="+ps4.getCode()+"&storeId="+store.getCode());
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return response;
	}
}