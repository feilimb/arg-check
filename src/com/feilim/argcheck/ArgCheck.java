package com.feilim.argcheck;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class ArgCheck {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet("http://checkargos.com/StockCheckPage.php?productId=1222540");
			HttpResponse response = client.execute(request);

			// Get the response
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));

			StringBuffer textView = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				textView.append(line);
			}
			
			System.out.println("===========");
			System.out.println(textView.toString());
			System.out.println("===========");
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}