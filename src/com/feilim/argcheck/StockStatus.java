package com.feilim.argcheck;

public enum StockStatus 
{
	IN_STOCK("in stock"),
	OUT_OF_STOCK("out of stock"),
	UNKNOWN_STATUS("unknown");
	
	private String _status;
	
	private StockStatus(String status) 
	{
		_status = status;
	}
	
	public String getStatus() 
	{
		return _status;
	}
}
