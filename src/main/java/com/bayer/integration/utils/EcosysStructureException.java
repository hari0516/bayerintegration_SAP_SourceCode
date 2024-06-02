package com.bayer.integration.utils;

public class EcosysStructureException extends Exception {
	
	public EcosysStructureException(String errorMessage, Throwable err) {
		
		err.printStackTrace();
		System.out.println(errorMessage);
	}
}
