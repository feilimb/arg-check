package com.feilim.argcheck;

import com.feilim.argcheck.StockStatus;

public class StockWrapper 
{
	StockStatus _status;
	int _quantity;
	
	public StockWrapper(StockStatus status, int quantity) 
	{
		_status = status;
		_quantity = quantity;
	}
}
