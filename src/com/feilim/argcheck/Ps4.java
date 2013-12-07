package com.feilim.argcheck;

public enum Ps4 
{
	KZ_BUNDLE("KZ Bundle", 1447947),
	KN_BUNDLE("KN Bundle", 1473825),
	PS4_SOLUS("PS4 Solus", 1222540),
	FIFA_BUNDLE("Fifa Bundle", 1578641),
	AC_BUNDLE("AC Bundle", 1451483),
	KZ_MEGABUNDLE("KZ Megabundle", 1450312);
	
	private int _code;
	private String _name;
	
	private Ps4(String name, int code) 
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
}
