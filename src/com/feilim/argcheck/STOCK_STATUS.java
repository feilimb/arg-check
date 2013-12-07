package com.feilim.argcheck;

public enum STOCK_STATUS 
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
