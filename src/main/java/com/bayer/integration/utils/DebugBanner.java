package com.bayer.integration.utils;import java.util.List;

import com.bayer.integration.rest.wbsread.BayerWBSReadAPIType;

public abstract class DebugBanner {

	static String bottom = new String("--------------------------------------------------------------------------");
	static String top = new String("__________________________________________________________________________");

	public static void outputBanner() {
		//System.out.println();
		System.out.println(top);
		System.out.println();
		System.out.println("RUNNING IN DEBUG MODE\nSet DEBUGMODE = false in app.properties");
		System.out.println();
		System.out.println(bottom);
		System.out.println();
		//System.out.println();
	}

	public static void outputBanner(String callingClassName) {
		//System.out.println();		
		System.out.println(top);
		System.out.println(callingClassName);		
		System.out.println("\nRUNNING IN DEBUG MODE\nSet DEBUGMODE = false in app.properties");
		System.out.println();
		System.out.println(bottom);
		//System.out.println();
		//System.out.println();
	}	
}