package com.feilim.argcheck;

import com.feilim.argcheck.STOCK_STATUS;

public class StockWrapper 
{
	STOCK_STATUS _status;
	int _quantity;
	
	public StockWrapper(STOCK_STATUS status, int quantity) 
	{
		_status = status;
		_quantity = quantity;
	}
}
