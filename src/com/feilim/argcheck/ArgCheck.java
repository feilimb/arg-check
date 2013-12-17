package com.feilim.argcheck;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class ArgCheck 
{
	private String _pushoverAppToken;
	private String _pushoverUserToken;
	//private int _lock;
	private Map<String, StockStatus> _stockStatusMap;
	private static final String STOCK_URL_TEMPLATE = "http://www.argos.ie/webapp/wcs/stores/servlet/ISALTMStockAvailability?storeId=10152&langId=111&partNumber_1=PRODUCT_ID&checkStock=true&backTo=product&storeSelection=STORE_ID&viewTaskName=ISALTMAjaxResponseView";
	private static final String IN_STOCK_KEY = "inStock";
	private static final String OUT_OF_STOCK_KEY = "outOfStock";
	public static Timer MAIN_TIMER;
	
	private static final boolean AUTO_LAUNCH_BROWSER = true;
	private static final String URL_01_TEMPLATE = "http://www.argos.ie/webapp/wcs/stores/servlet/Search?storeId=10152&catalogId=14551&langId=111&searchTerms=PRODUCT_ID&authToken=%252d1002%252c8RvTgzRQUIkiaCQg1wd0wg264Io%253d";

	private Logger _logger = Logger.getLogger("ArgCheck");  
    private FileHandler _fh;  
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		ArgCheck ac = new ArgCheck();
		ac.initialise();
		ac.runIndefinitely();
//		ac.playSound(StockStatus.IN_STOCK);
//		try {
//			Thread.sleep(5000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		//ReservationRobot rr = new ReservationRobot(""+1024353, Store.Cork_Queens, "feilimb");
		//Timer t = new Timer();
		//t.schedule(rr, 100);
	}
	
	private void initialise() 
	{
		MAIN_TIMER = new Timer();
		
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
		
		_stockStatusMap = new HashMap<String, StockStatus>();
		
        try 
        {
        	SimpleDateFormat sim=new SimpleDateFormat("ddMMyyyy_HHmm");
        	Date d = new Date();
        	String s = sim.format(d);
			_fh = new FileHandler("log/" + s + ".log");  
			_logger.addHandler(_fh);
			SimpleFormatter formatter = new SimpleFormatter();  
			_fh.setFormatter(formatter);
		}
        catch (SecurityException e)
        {
			e.printStackTrace();
		} 
        catch (IOException e) 
        {
			e.printStackTrace();
		}  
	}
	
	private void runIndefinitely()
	{
		//_lock = 0;
		_logger.info("AC :: Starting Stock Check...");
		checkAllPS4s();
	}
	
	private void checkAllPS4s()
	{
		//_lock = Ps4.values().length;
		for (final Ps4 p : Ps4.values()) 
		{
			TimerTask task = new StockCheckTask(p);
			Timer t = new Timer();
			t.schedule(task, 0, 8000);
		}
	}

	private class StockCheckTask extends TimerTask
	{
		final Ps4 _ps4;
		
		public StockCheckTask(Ps4 p) 
		{
			_ps4 = p;
		}
		
		@Override
		public void run() 
		{
			checkAllStores(_ps4);
			//decrementLock();
		}
	}
	
//	private synchronized void decrementLock()
//	{
//		_lock--;
//	}
	
	private void checkAllStores(Ps4 p)
	{
		for (Store s : Store.values())
		{
			checkPS4Status(s, p);		
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
				
				String key = getUniqueStockStatusKey(s, ps4);
				StockStatus cachedStatus = getFromStatusMap(key);
				switch (sw._status)
				{
				case IN_STOCK:
					if (cachedStatus == null || cachedStatus != StockStatus.IN_STOCK)
					{
						updateStatusMap(key, sw._status);
						playSound(sw._status);
						sendNotification(s, ps4, sw);
						if (AUTO_LAUNCH_BROWSER && 
								(s==Store.Cork_Mahon||s==Store.Cork_Queens||s==Store.Cork_Retail))
						{
							ReservationRobot.openBrowser(Integer.toString(ps4.getCode()), 0);
						}
					}
					break;
				case OUT_OF_STOCK:
					if (cachedStatus == null || cachedStatus != StockStatus.OUT_OF_STOCK)
					{
						if (cachedStatus == StockStatus.IN_STOCK)
						{
							sendNotification(s, ps4, sw);
						}
						updateStatusMap(key, sw._status);
					}
					break;
				}
			}
		}
	}

	private void playSound(StockStatus _status) {
		if (_status == StockStatus.IN_STOCK)
		{
			playSound("tada.wav");
		}
	}
	
	public void playSound(String filename){

        String strFilename = filename;
        File soundFile = null;
        AudioInputStream audioStream = null;
        try {
            soundFile = new File(strFilename);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        try {
            audioStream = AudioSystem.getAudioInputStream(soundFile);
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }

        AudioFormat audioFormat = audioStream.getFormat();

        SourceDataLine sourceLine = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        try {
            sourceLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceLine.open(audioFormat);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        sourceLine.start();

        int nBytesRead = 0;
        byte[] abData = new byte[1024];
        while (nBytesRead != -1) {
            try {
                nBytesRead = audioStream.read(abData, 0, abData.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (nBytesRead >= 0) {
                @SuppressWarnings("unused")
                int nBytesWritten = sourceLine.write(abData, 0, nBytesRead);
            }
        }

        sourceLine.drain();
        sourceLine.close();
    }

	private synchronized void updateStatusMap(String key, StockStatus status)
	{
		_stockStatusMap.put(key, status);
	}

	private synchronized StockStatus getFromStatusMap(String key)
	{
		return _stockStatusMap.get(key);
	}
	
	private String getUniqueStockStatusKey(Store s, Ps4 p)
	{
		return s.getCode() + "_" + p.getCode();
	}
	
	private void sendNotification(Store s, Ps4 ps4, StockWrapper sw) 
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
		sb.append(ps4.getName()).append(" (").append(ps4.getCode()).append(") Status: "); 
		sb.append(sw._status.getStatus()).append(". Store: ").append(s.getName()).append("(");
		sb.append(s.getCode()).append(")");
		sb.append(", Quantity: ").append(sw._quantity);
		
		// record this in stock change to log file on disk
		_logger.info(sb.toString());
		
		String url = URL_01_TEMPLATE.replaceFirst("PRODUCT_ID", ""+ps4.getCode());
		sb.append("\n").append(url);
		
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