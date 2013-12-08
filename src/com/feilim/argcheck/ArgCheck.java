package com.feilim.argcheck;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
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
	private int _lock;
	private static final String STOCK_URL_TEMPLATE = "http://www.argos.ie/webapp/wcs/stores/servlet/ISALTMStockAvailability?storeId=10152&langId=111&partNumber_1=PRODUCT_ID&checkStock=true&backTo=product&storeSelection=STORE_ID&viewTaskName=ISALTMAjaxResponseView";
	private static final String IN_STOCK_KEY = "inStock";
	private static final String OUT_OF_STOCK_KEY = "outOfStock";
	public static Timer MAIN_TIMER;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		ArgCheck ac = new ArgCheck();
		ac.initialise();
		//ac.runIndefinitely();
		ReservationRobot robot = new ReservationRobot("1024353", Store.CORK_MAHON);
		MAIN_TIMER.schedule(robot, 100);
		
		try 
		{
			Thread.sleep(60000);
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
	
	public ArgCheck() 
	{
		MAIN_TIMER = new Timer();
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
	
	private void runIndefinitely()
	{
		_lock = 0;
		while (true)
		{
			long t = System.currentTimeMillis();
			checkAllStores();
			while (_lock != 0)
			{
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {}
			}
			long t1 = System.currentTimeMillis();
			System.out.println("\n\nTime to check all: " + (t1-t) + " ms.");
		}
	}
	
	private void checkAllStores()
	{
		_lock = Store.values().length;
		for (final Store s : Store.values()) 
		{
			TimerTask task = new MyTimerTask(s);
			MAIN_TIMER.schedule(task, 0);
		}
	}

	private class MyTimerTask extends TimerTask
	{
		final Store _store;
		
		public MyTimerTask(Store s) 
		{
			_store = s;
		}
		
		@Override
		public void run() 
		{
			checkAllPS4s(_store);
			decrementLock();
		}
	}
	
	private synchronized void decrementLock()
	{
		_lock--;
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
		sb.append(ps4.getName()).append(" (").append(ps4.getCode()).append(") In Stock! Store: ").append(s.getName());
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
	
	private HttpResponse getStockCheckResponse(Store store, Ps4 ps4)
	{
		String url = STOCK_URL_TEMPLATE.replaceFirst("PRODUCT_ID", Integer.toString(ps4.getCode()));
		url = url.replaceFirst("STORE_ID", Integer.toString(store.getCode()));
		
		HttpResponse response = null;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(url);
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return response;
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
		
		String s = parsedStatus.toString();
		int inStockIndex = s.indexOf(IN_STOCK_KEY);
		
		if (inStockIndex != -1)
		{
			return new StockWrapper(StockStatus.IN_STOCK, parseQuantity(s.substring(inStockIndex)));
		}
		else if (s.contains(OUT_OF_STOCK_KEY))
		{
			return new StockWrapper(StockStatus.OUT_OF_STOCK, 0);
		}
		else
		{
			return new StockWrapper(StockStatus.UNKNOWN_STATUS, 0);
		}
	}

	private int parseQuantity(String status) 
	{
		Pattern p = Pattern.compile("(\\d+) left to collect");
		Matcher m = p.matcher(status);
		if (m.find())
		{
			String d = m.group(1);
			return Integer.parseInt(d);
		}
		
		return 0;
	}
}