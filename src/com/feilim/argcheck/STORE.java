package com.feilim.argcheck;

public enum STORE 
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
}
