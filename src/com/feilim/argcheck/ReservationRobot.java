package com.feilim.argcheck;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.TimerTask;

public class ReservationRobot extends TimerTask
{
	private static final String URL_01_TEMPLATE = "http://www.argos.ie/webapp/wcs/stores/servlet/Search?storeId=10152&catalogId=14551&langId=111&searchTerms=PRODUCT_ID&authToken=%252d1002%252c8RvTgzRQUIkiaCQg1wd0wg264Io%253d";
	private String _productId;
	private Store _store;
	private Robot _robot;
	
	public ReservationRobot(String productId, Store store) 
	{
		_productId = productId;
		_store = store;
		initialseRobot();
	}
	
	private void initialseRobot()
	{
		try 
		{
			_robot = new Robot();
		} 
		catch (AWTException e) 
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() 
	{
		try 
		{
			String url01 = URL_01_TEMPLATE.replaceFirst("PRODUCT_ID", _productId);
			openBrowser(url01, 7000);
			performClick(600, 450);
			_robot.delay(7000);
			performClick(1050, 460);
			_robot.delay(7000);
			performClick(400, 350);
			enterStoreName();
			performClick(1050, 400);
			_robot.delay(7000);
			performClick(480, 570);
			enterEmail();
		} 
		catch (AWTException e)
		{
			e.printStackTrace();
		}
	}
	
	private void enterEmail() 
	{
		_robot.keyPress(KeyEvent.VK_F);
		_robot.keyPress(KeyEvent.VK_E);
		_robot.keyPress(KeyEvent.VK_I);
		_robot.keyPress(KeyEvent.VK_L);
		_robot.keyPress(KeyEvent.VK_I);
		_robot.keyPress(KeyEvent.VK_M);
		_robot.keyPress(KeyEvent.VK_B);
		_robot.keyPress(KeyEvent.VK_AT);
		_robot.keyPress(KeyEvent.VK_G);
		_robot.keyPress(KeyEvent.VK_M);
		_robot.keyPress(KeyEvent.VK_A);
		_robot.keyPress(KeyEvent.VK_I);
		_robot.keyPress(KeyEvent.VK_L);
		_robot.keyPress(KeyEvent.VK_PERIOD);
		_robot.keyPress(KeyEvent.VK_C);
		_robot.keyPress(KeyEvent.VK_O);
		_robot.keyPress(KeyEvent.VK_M);


	}

	private void enterStoreName() throws AWTException
	{
		_robot.keyPress(KeyEvent.VK_C);
		_robot.keyPress(KeyEvent.VK_O);
		_robot.keyPress(KeyEvent.VK_R);
		_robot.keyPress(KeyEvent.VK_K);
		_robot.keyPress(KeyEvent.VK_ENTER);
	}

	private void openBrowser(String url, long pauseTime)
	{
		try 
		{
			Desktop.getDesktop().browse(java.net.URI.create(url));
			Thread.sleep(pauseTime);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
	
	private void performClick(final int x, final int y) throws AWTException
	{
		_robot.mouseMove( x, y );
		_robot.mousePress(InputEvent.BUTTON1_MASK);
		_robot.mouseRelease(InputEvent.BUTTON1_MASK);
	}
}
