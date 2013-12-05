package com.feilim.argcheck;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class ArgCheck 
{
	private static final String PUSHOVER_APP_TOKEN = "";
	private static final String PUSHOVER_USER_TOKEN = "";
	
	private class StockWrapper
	{
		STOCK_STATUS _status;
		int _quantity;
		
		public StockWrapper(STOCK_STATUS status, int quantity) 
		{
			_status = status;
			_quantity = quantity;
		}
	}
	
	private enum STOCK_STATUS
	{
		IN_STOCK("in stock"),
		OUT_OF_STOCK("out of stock"),
		UNKNOWN_STATUS("unknown");
		
		private String _status;
		
		private STOCK_STATUS(String status) 
		{
			_status = status;
		}
		
		public String getStatus() 
		{
			return _status;
		}
	}
	
	private enum PS4
	{
		KZ_BUNDLE("KZ Bundle", 1447947),
		KN_BUNDLE("KN Bundle", 1473825),
		PS4_SOLUS("PS4 Solus", 1222540),
		FIFA_BUNDLE("Fifa Bundle", 1578641),
		AC_BUNDLE("AC Bundle", 1451483),
		KZ_MEGABUNDLE("KZ Megabundle", 1450312);
		
		private int _code;
		private String _name;
		
		private PS4(String name, int code) 
		{
			_name = name;
			_code = code;
		}

		public int getCode() 
		{
			return _code;
		}
		
		public String getName() {
			return _name;
		}
	};
	
	private enum STORE 
	{
		CORK_MAHON("Cork Mahon Point", 4113), 
		CORK_QUEENS_CASTLE("Cork Queens Castle", 45), 
		CORK_RETAIL("Cork Retail Park", 801);
		
		private int _code;
		private String _name;
		
		private STORE(String name, int code)
		{
			_name = name;
			_code = code;
		}
		
		public int getCode() 
		{
			return _code;
		}
		
		public String getName() 
		{
			return _name;
		}
		
	};
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArgCheck ac = new ArgCheck();
		ac.start01();
	}
	
	private void start01()
	{
		for (PS4 p : PS4.values()) 
		{
			checkPS4Status(p);		
		}
	}

	private void checkPS4Status(PS4 ps4)
	{
		for (STORE s : STORE.values()) 
		{
			HttpResponse response = getStockCheckResponse(s, ps4);
			if (response != null)
			{
				StockWrapper sw = parseResponse(response);
				if (sw != null)
				{
					System.out.println(">>> Store: " + s.getName() + ", Item: " + ps4.getName() + " ["+ps4.getCode()+"], Status: " + sw._status.getStatus());
					switch (sw._status)
					{
					case IN_STOCK:
						sendNotification(s, ps4, sw._quantity);
						break;
					}
				}
			}
		}
	}
	
	private void sendNotification(STORE s, PS4 ps4, int quantity) 
	{
		Pushover p = new Pushover(PUSHOVER_APP_TOKEN, PUSHOVER_USER_TOKEN);
		StringBuilder sb = new StringBuilder();
		sb.append(ps4.getCode()).append(" In Stock! Store: ").append(s.getName());
		sb.append(", Quantity: ").append(quantity);
		
		System.out.println("\nArgCheck.sendNotification() :: Sending Notification! Details:");
		System.out.println(sb.toString());
		try {
		    p.sendMessage(sb.toString());
		} catch (IOException e) {
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
			if (status.contains(STOCK_STATUS.IN_STOCK.getStatus()))
			{
				return new StockWrapper(STOCK_STATUS.IN_STOCK, parseQuantity(status));
			}
			else if (status.contains(STOCK_STATUS.OUT_OF_STOCK.getStatus()))
			{
				return new StockWrapper(STOCK_STATUS.OUT_OF_STOCK, 0);
			}
			else if (status.contains(STOCK_STATUS.UNKNOWN_STATUS.getStatus()))
			{
				return new StockWrapper(STOCK_STATUS.UNKNOWN_STATUS, 0);
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

	private HttpResponse getStockCheckResponse(STORE store, PS4 ps4)
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