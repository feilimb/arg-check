package com.feilim.argcheck;

public enum Store 
{
//	 Arklow("Arklow", 4101), 
//	 Ashbourne("Ashbourne Retail Park", 943), 
//	 Athlone("Athlone", 262),
//	 Blanchardstown("Blanchardstown West End", 669), 
//	 Carlow("Carlow", 4130),
//	 Castlebar("Castlebar", 807), 
//	 Cavan("Cavan", 814),
//	 Clonmel("Clonmel", 4214), 
	 Cork_Mahon("Cork Mahon", 4113), 
	 Cork_Queens("Cork Queens Old Castle", 45), 
	 Cork_Retail("Cork Retail Park", 801), 
//	 Drogheda("Drogheda", 875), 
//	 Dun_Laoghaire("Dun Laoghaire", 200), 
//	 Dundalk("Dundalk Retail Park", 931), 
//	 Dundrum("Dundrum", 817), 
//	 Galway("Galway", 547), 
//	 Ilac("Ilac Centre (Dublin)", 394), 
//	 Jervis("Jervis Street (Dublin)", 397), 
//	 Kilkenny("Kilkenny", 201), 
	 Killarney("Killarney", 899); 
//	 Letterkenny("Letterkenny", 793), 
//	 Liffey("Liffey Valley", 687), 
//	 Limerick_Childers("Limerick Childers Road", 915), 
//	 Limerick_Cruises("Limerick Cruises Street", 393), 
//	 Limerick_Crescent("Limerick The Crescent", 583), 
//	 Longford("Longford", 880), 
//	 Monaghan("Monaghan", 945),
//	 Naas("Naas", 4218), 
//	 Navan("Navan", 832), 
//	 Nutgrove("Nutgrove", 392), 
//	 Omni("Omni Park (Dublin)", 4150), 
//	 Portlaoise("Portlaoise", 4125), 
//	 Sligo("Sligo", 4146), 
//	 Stephens_Green("St. Stephens Green (Dublin)", 584), 
//	 Swords("Swords", 581), 
//	 Tallaght("Tallaght", 395), 
//	 Tralee("Tralee", 11), 
//	 Tullamore("Tullamore", 879), 
//	 Waterford("Waterford", 396), 
//	 Wexford("Wexford", 826);	
	private int _code;
	private String _name;
	
	private Store(String name, int code)
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
