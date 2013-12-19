package com.jargha.argcheck;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
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

	private Map<String, StockStatus> _stockStatusMap;
	
	private static final String STOCK_URL_TEMPLATE = 
			"http://www.argos.ie/webapp/wcs/stores/servlet/ISALTMStockAvailability?storeId=10152&langId=111&partNumber_1=PRODUCT_ID&checkStock=true&backTo=product&storeSelection=STORE_ID&viewTaskName=ISALTMAjaxResponseView";
	
	private static final String URL_01_TEMPLATE = 
			"http://www.argos.ie/webapp/wcs/stores/servlet/Search?storeId=10152&catalogId=14551&langId=111&searchTerms=PRODUCT_ID&authToken=%252d1002%252c8RvTgzRQUIkiaCQg1wd0wg264Io%253d";

	private static final String IN_STOCK_KEY = "inStock";
	
	private static final String OUT_OF_STOCK_KEY = "outOfStock";
	
	private boolean _autoLaunchBrowser;
	
	private boolean _playInStockSound;

	private Logger _logger = Logger.getLogger("ArgCheck");
	
    private FileHandler _fh;
    
    private Collection<Store> _storesToCheck;
    
    private Collection<Ps4> _ps4sToCheck;
    
    private String _logfilePath;
    
    private int _repeatCheckDelay;
    
    public ArgCheck() 
    {
    	initialise();
	}
    
	private void initialise() 
	{
		Properties prop = new Properties();
		try 
		{
			// load our properties file
			prop.load(new FileInputStream("config.properties"));

			// get the property values for pushover notifications if any
			//_pushoverAppToken = prop.getProperty("PUSHOVER_APP_TOKEN");
			_pushoverAppToken = "afDf8adppu37JVGb1CY7Nwy4oKNCSf";
			_pushoverUserToken = prop.getProperty("PUSHOVER_USER_TOKEN");
			
			String repeatCheckDelayStr = prop.getProperty("REPEAT_CHECK_DELAY", ""+6);
			try 
			{
				_repeatCheckDelay = Integer.parseInt(repeatCheckDelayStr);
			} 
			catch (NumberFormatException e) 
			{
				System.err.println("Invalid value specified repeat check delay! See REPEAT_CHECK_DELAY in config.properties!");
				System.exit(1);
			}
			
			String storeIds = prop.getProperty("STORES");
			String bundleIds = prop.getProperty("BUNDLES");
			String autoLaunchBrowserStr = prop.getProperty("AUTO_LAUNCH_BROWSER", "FALSE");
			String playInStockSoundStr = prop.getProperty("PLAY_IN_STOCK_SOUND", "TRUE");
			
			_logfilePath = prop.getProperty("LOG_FILE_PATH", "C:\\Temp");
			File f = new File(_logfilePath);
			if (!f.exists() || !f.isDirectory())
			{
				System.err.println("Invalid path specified for log file location! See LOG_FILE_PATH in config.properties!");
				System.exit(1);
			}
			
			_storesToCheck = parseStores(storeIds);
			_ps4sToCheck = parsePS4s(bundleIds);
			_autoLaunchBrowser = parseAutoLaunch(autoLaunchBrowserStr);
			_playInStockSound = parsePlayInStockSound(playInStockSoundStr);
			
			if (_storesToCheck.isEmpty())
			{
				System.err.println("No valid stores were found in config.properties!");
				System.exit(1);
			}
			
			if (_ps4sToCheck.isEmpty())
			{
				System.err.println("No valid PS4 bundles were found in config.properties!");
				System.exit(1);
			}
		} 
		catch (IOException ex) 
		{
			ex.printStackTrace();
		}
		
		_stockStatusMap = new HashMap<String, StockStatus>();
		
        try 
        {
        	SimpleDateFormat sim=new SimpleDateFormat("ddMMyyyy_HHmm");
        	String s = sim.format(new Date());
			_fh = new FileHandler(_logfilePath + File.separator + s + ".log");  
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
	
	private boolean parsePlayInStockSound(String playInStockSoundStr)
	{
		if (playInStockSoundStr == null)
		{
			return false;
		}
		return Boolean.valueOf(playInStockSoundStr);
	}

	private boolean parseAutoLaunch(String autoLaunchBrowserStr) 
	{
		if (autoLaunchBrowserStr == null)
		{
			return false;
		}
		return Boolean.valueOf(autoLaunchBrowserStr);
	}

	private Collection<Ps4> parsePS4s(String bundleIds) 
	{
		Set<Ps4> ps4s = new LinkedHashSet<Ps4>();
		if (bundleIds == null)
		{
			System.err.println("No BUNDLES were specified in config.properties!");
			System.exit(1);
		}
		
		String[] ids = bundleIds.split(",");
		for (String id : ids) 
		{
			int code = Integer.parseInt(id);
			Ps4 p = Ps4.getFromCode(code);
			if (p != null)
			{
				ps4s.add(p);
			}
		}
		
		return ps4s;
	}

	private Collection<Store> parseStores(String storeIds) 
	{
		Set<Store> stores = new LinkedHashSet<Store>();
		if (storeIds == null)
		{
			System.err.println("No STORES were specified in config.properties!");
			System.exit(1);
		}
		
		String[] ids = storeIds.split(",");
		for (String id : ids) 
		{
			int code = Integer.parseInt(id);
			Store s = Store.getFromCode(code);
			if (s != null)
			{
				stores.add(s);
			}
		}
		
		return stores;
	}

	void runIndefinitely()
	{
		_logger.info("AC :: Starting Stock Checks...");
		checkAllPS4s();
	}
	
	private void checkAllPS4s()
	{
		Map<Ps4, Timer> timersMap = new HashMap<Ps4, Timer>();
		for (final Ps4 p : _ps4sToCheck)
		{
			timersMap.put(p, new Timer());
		}
		
		for (final Ps4 p : _ps4sToCheck) 
		{
			StockCheckTask task = new StockCheckTask(p);
			Timer t = timersMap.get(p);
			t.schedule(task, 0, _repeatCheckDelay*1000);
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
		}
	}
	
	private void checkAllStores(Ps4 p)
	{
		for (Store s : _storesToCheck)
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
						if (_playInStockSound)
						{
							playSound(sw._status);
						}
						sendPushoverNotification(s, ps4, sw);
						if (_autoLaunchBrowser)
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
							sendPushoverNotification(s, ps4, sw);
						}
						updateStatusMap(key, sw._status);
					}
					break;
				}
			}
		}
	}

	private void playSound(StockStatus status) 
	{
		if (status == StockStatus.IN_STOCK)
		{
			playSound("tada.wav");
		}
	}
	
	private void playSound(String filename){

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
	
	private void sendPushoverNotification(Store s, Ps4 ps4, StockWrapper sw) 
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