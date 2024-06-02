package com.bayer.integration.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import javax.ws.rs.HttpMethod;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;
//import javax.ws.rs.client.Client;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
//import javax.ws.rs.client.ClientBuilder;
import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientResponse;
//import javax.ws.rs.client.WebTarget;


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

//import com.bayer.integration.rest.project.BayerProjectAPITypes;
import com.bayer.integration.rest.project.BayerProjectAPIType;

//import com.bayer.integration.rest.wbs.BayerWBSAPITypes;
import com.bayer.integration.rest.wbs.BayerWBSAPIType;
import com.bayer.integration.rest.wbs.BayerWBSAPIRequestType;

import com.bayer.integration.rest.pmorder.BayerPMOrderAPIType;
import com.bayer.integration.rest.pmorder.BayerPMOrderAPIRequestType;

import com.bayer.integration.persistence.StagingDatabaseManager;
import com.ecosys.exception.SystemException;
import com.ecosys.service.EpcRestMgr;

import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

import com.bayer.integration.odata.SapWBSODataType;
import com.bayer.integration.odata.SapPMOODataType;
import com.bayer.integration.odata.SapACTODataType;

import com.bayer.integration.odata.SapPOHODataType;
import com.bayer.integration.odata.SapPOLODataType;

import com.bayer.integration.odata.SapPRHODataType;
import com.bayer.integration.odata.SapPRLODataType;

public class ImportManagerHelper extends ImportManagerBase {

	public ImportManagerHelper() {
	}
	
	/*
    public List<BayerProjectAPIType> getBayerProjectAPITypes(Client client, String apiUrl) throws JAXBException{

        WebResource resource = client.resource(apiUrl); 
        String strInput = "";
        strInput = resource.get(String.class);
        InputStream inputStream = new ByteArrayInputStream(strInput.getBytes());
        
        //unmarshal to desired objects
        JAXBContext jaxbContext = JAXBContext.newInstance(BayerProjectAPITypes.class);
        Unmarshaller jaxUnmarshaller = jaxbContext.createUnmarshaller();
        
        BayerProjectAPITypes projAPITypes = (BayerProjectAPITypes) jaxUnmarshaller.unmarshal(inputStream);
        return projAPITypes.getBayerProjectAPITypes();
    }

    public List<BayerWBSAPIType> getBayerWBSAPITypes(Client client, String apiUrl) throws JAXBException{
        WebResource resource = client.resource(apiUrl); 
        String strInput = resource.get(String.class);
        InputStream inputStream = new ByteArrayInputStream(strInput.getBytes());
       
        //unmarshal to desired objects
        JAXBContext jaxbContext = JAXBContext.newInstance(BayerWBSAPITypes.class);
        Unmarshaller jaxUnmarshaller = jaxbContext.createUnmarshaller();        
        BayerWBSAPITypes wbsAPITypes = (BayerWBSAPITypes) jaxUnmarshaller.unmarshal(inputStream);
 
        return wbsAPITypes.getBayerWBSAPITypes();
    }
    
  */  
    public List<BayerWBSAPIType> getBayerWBSAPITypes(List<SapWBSODataType> sapWBSTypes, BayerWBSAPIRequestType wbsAPIRequestType){
              
        return wbsAPIRequestType.getBayerWBSAPI();
    }
    
    public List<BayerPMOrderAPIType> getBayerPMOrderAPITypes(List<SapPMOODataType> sapTypes, BayerPMOrderAPIRequestType apiRequestType){
        
        return apiRequestType.getBayerPMOrderAPI();
    }
    
    public List<SapWBSODataType> getSapWBSTypesSample(){
        List<SapWBSODataType> sapWBSTypes = new ArrayList<SapWBSODataType>();
        for (int i = 0; i <6; i++)
        {	
        	SapWBSODataType sapWBSType = new SapWBSODataType();
        	if (i==0)
        	{	
        		sapWBSType.setWbsElement("A00NC-003247");
        		sapWBSType.setProjectDefinition("A00NC-003247");
        		sapWBSType.setDescription("Test EcoSys Inte.");
        		sapWBSType.setResponsibleNo("00000104");
        		sapWBSType.setProfitCtr("TBR0000278");
        		sapWBSType.setProjType("A0");
        		sapWBSType.setCurrency("EUR");
        		sapWBSType.setObjectClass("IV");
        		sapWBSType.setDeletionFlag("");
        		sapWBSType.setRespsblCctr("EE20626790");
        		sapWBSType.setStatus("TestStatus");
        		sapWBSType.setHierarchyLevel(0);
        	}
        	
        	if (i==1)
        	{	
        		sapWBSType.setWbsElement("A00NC-003247-C");
        		sapWBSType.setParentWbs("A00NC-003247");
        		sapWBSType.setProjectDefinition("A00NC-003247");
        		sapWBSType.setDescription("Capital Invest.Test");
        		sapWBSType.setResponsibleNo("00000104");
        		sapWBSType.setProfitCtr("TBR0000278");
        		sapWBSType.setProjType("A0");
        		sapWBSType.setCurrency("EUR");
        		sapWBSType.setObjectClass("IV");
        		sapWBSType.setDeletionFlag("X");
        		sapWBSType.setRespsblCctr("EE20626790");
        		sapWBSType.setStatus("TestStatus");
        		sapWBSType.setHierarchyLevel(1);
        	}
        	if (i==2)
        	{	
        		sapWBSType.setWbsElement("A00NC-003247-C1");
        		sapWBSType.setParentWbs("A00NC-003247-C");
        		sapWBSType.setProjectDefinition("A00NC-003247");
        		sapWBSType.setDescription("Building Test");
        		sapWBSType.setResponsibleNo("00000104");
        		sapWBSType.setProfitCtr("TBR0000278");
        		sapWBSType.setProjType("A1");
        		sapWBSType.setCurrency("EUR");
        		sapWBSType.setObjectClass("IV");
        		sapWBSType.setDeletionFlag("");
        		sapWBSType.setRespsblCctr("EE20626790");
        		sapWBSType.setLocation("A117");
        		sapWBSType.setStatus("TestStatus2");
        		sapWBSType.setHierarchyLevel(2);
        	}
        	
        	if (i==3)
        	{	
        		sapWBSType.setWbsElement("A00NC-003247-C2");
        		sapWBSType.setParentWbs("A00NC-003247-C");
        		sapWBSType.setProjectDefinition("A00NC-003247");
        		sapWBSType.setDescription("Machinery");
        		sapWBSType.setResponsibleNo("00000104");
        		sapWBSType.setProfitCtr("TBR0000278");
        		sapWBSType.setProjType("AA");
        		sapWBSType.setCurrency("EUR");
        		sapWBSType.setObjectClass("IV");
        		sapWBSType.setDeletionFlag("");
        		sapWBSType.setRespsblCctr("EE20626790");
        		sapWBSType.setLocation("Frankfurt, Deutschland");
        		sapWBSType.setHierarchyLevel(2);
        	}
        	
        	if (i==4)
        	{	
        		sapWBSType.setWbsElement("A00NC-003247-F");
        		sapWBSType.setParentWbs("A00NC-003247");
        		sapWBSType.setProjectDefinition("A00NC-003247");
        		sapWBSType.setDescription("Expense");
        		sapWBSType.setResponsibleNo("00000104");
        		sapWBSType.setProfitCtr("TBR0000278");
        		sapWBSType.setProjType("A0");
        		sapWBSType.setCurrency("EUR");
        		sapWBSType.setObjectClass("OC");
        		sapWBSType.setDeletionFlag("");
        		sapWBSType.setRespsblCctr("EE20626790");
        		sapWBSType.setLocation("");
        		sapWBSType.setHierarchyLevel(1);
        	}
        	
        	
        	if (i==5)
        	{	
        		sapWBSType.setWbsElement("A00NC-003247-F1");
        		sapWBSType.setParentWbs("A00NC-003247-F");
        		sapWBSType.setProjectDefinition("A00NC-003247");
        		sapWBSType.setDescription("Project Manageme");
        		sapWBSType.setResponsibleNo("00000104");
        		sapWBSType.setProfitCtr("TBR0000278");
        		sapWBSType.setProjType("C6");
        		sapWBSType.setCurrency("EUR");
        		sapWBSType.setObjectClass("OC");
        		sapWBSType.setDeletionFlag("");
        		sapWBSType.setRespsblCctr("EE20626790");
        		sapWBSType.setLocation("A117");
        		sapWBSType.setHierarchyLevel(2);
        	}
        	
        	sapWBSTypes.add(sapWBSType);
        }
        return sapWBSTypes;
    }
    
    public List<SapWBSODataType> getSapWBSTypesSampleSub(){
        List<SapWBSODataType> sapWBSTypes = new ArrayList<SapWBSODataType>();
        for (int i = 0; i <6; i++)
        {	
        	SapWBSODataType sapWBSType = new SapWBSODataType();
        	if (i==0)
        	{	
        		sapWBSType.setWbsElement("A00NC-003247");
        		sapWBSType.setWbsLevel(0);
        		sapWBSType.setProjectDefinition("A00NC-003247");
        		sapWBSType.setDescription("Test EcoSys Inte.");
        		sapWBSType.setResponsibleNo("00000104");
        		sapWBSType.setProfitCtr("TBR0000278");
        		sapWBSType.setProjType("A0");
        		sapWBSType.setCurrency("EUR");
        		sapWBSType.setObjectClass("IV");
        		sapWBSType.setDeletionFlag("");
        		sapWBSType.setRespsblCctr("EE20626790");
        	}
        	
        	if (i==1)
        	{	
        		sapWBSType.setWbsElement("A00NC-003247-C");
        		sapWBSType.setParentWbs("A00NC-003247");
        		sapWBSType.setWbsLevel(1);
        		sapWBSType.setProjectDefinition("A00NC-003247");
        		sapWBSType.setDescription("Capital Invest.");
        		sapWBSType.setResponsibleNo("00000104");
        		sapWBSType.setProfitCtr("TBR0000278");
        		sapWBSType.setProjType("A0");
        		sapWBSType.setCurrency("EUR");
        		sapWBSType.setObjectClass("IV");
        		sapWBSType.setDeletionFlag("");
        		sapWBSType.setRespsblCctr("EE20626790");
        	}
        	if (i==2)
        	{	
        		sapWBSType.setWbsElement("A00NC-003247-C1");
        		sapWBSType.setParentWbs("A00NC-003247-C");
        		sapWBSType.setWbsLevel(2);
        		sapWBSType.setProjectDefinition("A00NC-003247");
        		sapWBSType.setDescription("Building");
        		sapWBSType.setResponsibleNo("00000104");
        		sapWBSType.setProfitCtr("TBR0000278");
        		sapWBSType.setProjType("A1");
        		sapWBSType.setCurrency("EUR");
        		sapWBSType.setObjectClass("IV");
        		sapWBSType.setDeletionFlag("");
        		sapWBSType.setRespsblCctr("EE20626790");
        		sapWBSType.setLocation("A117");
        	}
        	
        	if (i==3)
        	{	
        		sapWBSType.setWbsElement("A00NC-003247-C2");
        		sapWBSType.setParentWbs("A00NC-003247-C");
        		sapWBSType.setWbsLevel(2);
        		sapWBSType.setProjectDefinition("A00NC-003247");
        		sapWBSType.setDescription("Machinery");
        		sapWBSType.setResponsibleNo("00000104");
        		sapWBSType.setProfitCtr("TBR0000278");
        		sapWBSType.setProjType("AA");
        		sapWBSType.setCurrency("EUR");
        		sapWBSType.setObjectClass("IV");
        		sapWBSType.setDeletionFlag("");
        		sapWBSType.setRespsblCctr("EE20626790");
        		sapWBSType.setLocation("A117");
        	}
        	
        	if (i==4)
        	{	
        		sapWBSType.setWbsElement("A00NC-003247-E");
        		sapWBSType.setParentWbs("A00NC-003247");
        		sapWBSType.setWbsLevel(1);
        		sapWBSType.setProjectDefinition("A00NC-003247");
        		sapWBSType.setDescription("Expense");
        		sapWBSType.setResponsibleNo("00000104");
        		sapWBSType.setProfitCtr("TBR0000278");
        		sapWBSType.setProjType("A0");
        		sapWBSType.setCurrency("EUR");
        		sapWBSType.setObjectClass("OC");
        		sapWBSType.setDeletionFlag("");
        		sapWBSType.setRespsblCctr("EE20626790");
        		sapWBSType.setLocation("");
        	}
        	
        	
        	if (i==5)
        	{	
        		sapWBSType.setWbsElement("A00NC-003247-F1");
        		sapWBSType.setParentWbs("A00NC-003247-F");
        		sapWBSType.setWbsLevel(2);
        		sapWBSType.setProjectDefinition("A00NC-003247");
        		sapWBSType.setDescription("Project Manageme");
        		sapWBSType.setResponsibleNo("00000104");
        		sapWBSType.setProfitCtr("TBR0000278");
        		sapWBSType.setProjType("C6");
        		sapWBSType.setCurrency("EUR");
        		sapWBSType.setObjectClass("OC");
        		sapWBSType.setDeletionFlag("");
        		sapWBSType.setRespsblCctr("EE20626790");
        		sapWBSType.setLocation("A117");
        	}
        	
        	sapWBSTypes.add(sapWBSType);
        }
        return sapWBSTypes;
    }
    public List<SapPMOODataType> getSapPMOTypesSample(){
        List<SapPMOODataType> sapTypes = new ArrayList<SapPMOODataType>();
        for (int i = 0; i <1; i++)
        {	
        	SapPMOODataType sapType = new SapPMOODataType();
        	if (i==0)
        	{	
        		sapType.setWbsElement("A00NC-003247-C1");
        		sapType.setProjectDefinition("A00NC-003247");
        		sapType.setPmOrderid("3627500018001");
        		sapType.setPmDescription("Project Order Ecosys - Update again");
        		sapType.setPmEstCost(12000.00);
        		sapType.setPmActCost(19000.00);
        		sapType.setPmDocCurr("EUR");
        		sapType.setPmStatus("REL PCNF ESTC CSER MANC PRC SETC");
        	}
        	
        	sapTypes.add(sapType);
        }
        return sapTypes;
    }
    
    public List<SapPOHODataType> getSapPOHTypesSample(){
        List<SapPOHODataType> sapTypes = new ArrayList<SapPOHODataType>();
        for (int i = 0; i <2; i++)
        {	
        	SapPOHODataType sapType = new SapPOHODataType();
        	if (i==0)
        	{	
        		sapType.setPurchOrd("21101995644");
        		sapType.setPurchOrdDesc("Project materiall(Text)");
        		sapType.setPoComCost(1050.00);
        		sapType.setPoActCost(0.00);
        		sapType.setPoDocCurr("EUR");
        		sapType.setVendorId("2064857222");
        		sapType.setVendorName("Air Liquide GmbH for Test");
        		sapType.setRequestor("test1");
        		sapType.setReceiver("Test2");
        		sapType.setLastInvDt(null);
        		sapType.setProjectDefinition("A00NC-003247");
        		sapType.setWbsElement("A00NC-003247-C1");
        		sapType.setSystemId("");
        	}
        	if (i==1)
        	{	
        		sapType.setPurchOrd("21101995655");
        		sapType.setPurchOrdDesc("FIBERTROMMEL 100 L, BLECHDECKEL");
        		sapType.setPoComCost(100.00);
        		sapType.setPoActCost(0.00);
        		sapType.setPoDocCurr("EUR");
        		sapType.setVendorId("122621111");
        		sapType.setVendorName("Air Liquide GmbH for test");
        		sapType.setRequestor("test1");
        		sapType.setReceiver("Test2");
        		sapType.setLastInvDt(null);
        		sapType.setProjectDefinition("A00NC-003247");
        		sapType.setWbsElement("A00NC-003247-C1");
        		sapType.setSystemId("");
        	}
        	sapTypes.add(sapType);
        }
        return sapTypes;
    }
    
    
    public List<SapPOLODataType> getSapPOLTypesSample() throws DatatypeConfigurationException{
        List<SapPOLODataType> sapTypes = new ArrayList<SapPOLODataType>();
    	DatatypeFactory dFactory = DatatypeFactory.newInstance();
        for (int i = 0; i <2; i++)
        {	
        	SapPOLODataType sapType = new SapPOLODataType();
        	if (i==0)
        	{	
        		sapType.setPurchOrd("21101995644");
        		sapType.setPurchOrdItem("00010");
        		sapType.setPurchOrdSeq("01");
        		sapType.setPurchOrdDesc("Project materiall(Text)");
        		sapType.setWbsElement("A00NC-003247-C2");
        		sapType.setPmOrder("");
        		sapType.setPurReq("91103427400");
        		sapType.setPurReqItem("00010");
        		sapType.setCostElement("7722001");
        		sapType.setCostElementDesc("Test 011111");
        		sapType.setPoComCost(1050.00);
        		sapType.setPoActCost(0.00);
        		sapType.setPoDocCurr("EUR");
        		sapType.setExchangeRate(0.00000);
        		sapType.setPoDate(dFactory.newXMLGregorianCalendar("2018-11-02"));
        		sapType.setFinalConf("N");
        		sapType.setDelFlag(null);
        		sapType.setProjectDefinition("A00NC-003247");
        		sapType.setSystemId("");
        	}
        	if (i==1)
        	{	
        		sapType.setPurchOrd("21101995655");
        		sapType.setPurchOrdItem("00010");
        		sapType.setPurchOrdSeq("01");
        		sapType.setPurchOrdDesc("FIBERTROMMEL 100 L, BLECHDECKEL");
        		sapType.setWbsElement("A00NC-003247-C1");
        		sapType.setPmOrder("3627500018001");
        		sapType.setPurReq("91103427300");
        		sapType.setPurReqItem("00010");
        		sapType.setCostElement("7722000");
        		sapType.setCostElementDesc("Test 7722000");
        		sapType.setPoComCost(100.00);
        		sapType.setPoActCost(0.00);
        		sapType.setPoDocCurr("EUR");
        		sapType.setExchangeRate(0.00000);
        		sapType.setPoDate(dFactory.newXMLGregorianCalendar("2018-11-01"));
        		sapType.setFinalConf("");
        		sapType.setDelFlag("Y");
        		sapType.setProjectDefinition("A00NC-003247");
        		sapType.setSystemId("");
        	}
        	sapTypes.add(sapType);
        }
        return sapTypes;
    }
    
    public List<SapPRHODataType> getSapPRHTypesSample(){
        List<SapPRHODataType> sapTypes = new ArrayList<SapPRHODataType>();
        for (int i = 0; i <2; i++)
        {	
        	SapPRHODataType sapType = new SapPRHODataType();
        	if (i==0)
        	{	
        		sapType.setPurchReq("91103427300");
        		sapType.setPurchReqDesc("Test1");
        		sapType.setPrComCost(10000.00);
        		sapType.setPrActCost(0.00);
        		sapType.setPrDocCurr("EUR");
        		sapType.setVendorId("2064857");
        		sapType.setVendorName("Air Liquide GmbH");
        		sapType.setProjectDefinition("A00NC-003247");
        		sapType.setSystemId("");
        	}
        	if (i==1)
        	{	
        		sapType.setPurchReq("91103427400");
        		sapType.setPurchReqDesc("Test2");
        		sapType.setPrComCost(1050.00);
        		sapType.setPrActCost(0.00);
        		sapType.setPrDocCurr("EUR");
        		sapType.setVendorId("");
        		sapType.setVendorName("");
        		sapType.setProjectDefinition("A00NC-003247");
        		sapType.setSystemId("");
        	}
        	sapTypes.add(sapType);
        }
        return sapTypes;
    }
    
    public List<SapPRLODataType> getSapPRLTypesSample() throws DatatypeConfigurationException{
        List<SapPRLODataType> sapTypes = new ArrayList<SapPRLODataType>();
    	DatatypeFactory dFactory = DatatypeFactory.newInstance();
    	
        for (int i = 0; i <2; i++)
        {	
        	SapPRLODataType sapType = new SapPRLODataType();
        	if (i==0)
        	{	
        		sapType.setPurchReq("91103427300");
        		sapType.setPurchReqItem("00010");
        		sapType.setPurchReqSeq("00");
        		sapType.setPurchReqDesc("Fibre drum");
        		sapType.setWbsElement("");
        		sapType.setPmOrder("3627500018001");
        		sapType.setPurOrd("21101995655");
        		sapType.setPurOrdItem("00010");
        		sapType.setCostElement("71111331");
        		sapType.setCostElementDesc("Test 011111");
        		sapType.setPrComCost(10000.00);
        		sapType.setPrActCost(1000.00);
        		sapType.setPrDocCurr("EUR");
        		sapType.setPrProjCurr(null);
        		sapType.setExchangeRate(0.00000);
        		sapType.setPrDate(dFactory.newXMLGregorianCalendar("2018-12-01"));
        		sapType.setDeleteInd(null);
        		sapType.setProjectDefinition("A00NC-003247");
        		sapType.setSystemId("");
        	}
        	if (i==1)
        	{	
        		sapType.setPurchReq("91103427400");
        		sapType.setPurchReqItem("00010");
        		sapType.setPurchReqSeq("00");
        		sapType.setPurchReqDesc("Project material (Text)");
        		sapType.setWbsElement("A00NC-003247-C2");
        		sapType.setPmOrder("");
        		sapType.setPurOrd("21101995655");
        		sapType.setPurOrdItem("00010");
        		sapType.setCostElement("71111112");
        		sapType.setCostElementDesc("Test 2");
        		sapType.setPrComCost(1050.00);
        		sapType.setPrActCost(0.00);
        		sapType.setPrDocCurr("EUR");
        		sapType.setPrProjCurr(null);
        		sapType.setExchangeRate(0.00000);
        		sapType.setPrDate(dFactory.newXMLGregorianCalendar("2018-11-02"));
        		sapType.setDeleteInd(null);
        		sapType.setProjectDefinition("A00NC-003247");
        		sapType.setSystemId("");
        	}
        	sapTypes.add(sapType);
        }
        return sapTypes;
    }
    
    public List<SapACTODataType> getSapACTTypesSample() throws DatatypeConfigurationException{
        List<SapACTODataType> sapTypes = new ArrayList<SapACTODataType>();
        for (int i = 0; i <3; i++)
        {	
        	DatatypeFactory dFactory = DatatypeFactory.newInstance();

        	//XMLGregorianCalendar gCal1 = XMLDateUtils.
        	SapACTODataType sapType = new SapACTODataType();
        	if (i==0)
        	{	
        		sapType.setControllingArea("H249");
        		sapType.setDocumentNo("500100420");
        		sapType.setPostingRow("002");
        		sapType.setPeriod("008");
        		sapType.setWbsElement("");
        		sapType.setPmOrderid("3627500018001");
        		sapType.setAmountTcurr(400.00);
        		sapType.setAmountOcurr(400.00);
        		sapType.setObjectNo("OR3627500018001");
        		sapType.setFiscalYear("2018");
        		sapType.setCostElement("011112");
        		sapType.setTransCurr("EUR");
        		sapType.setObjectCurr("EUR");
        		sapType.setPostingDate(dFactory.newXMLGregorianCalendar("2018-12-05"));
        		sapType.setExchangeRate(0.00000);
        		sapType.setProjectDefinition("A00NC-003247");
        		sapType.setAcHeaderTxt("test1");
        		sapType.setAcReferenceDoc("test1");
        		sapType.setPurchOrd("21101995655");
        		sapType.setPurchOrdItem("00010");
        		sapType.setPurchOrdSeq("003");
        		sapType.setVendorId("586510000");
        		sapType.setVendorName("Smith and Associates tsts"); 		
        		sapType.setSystemId("");
        		//sapType.
        	}
        	if (i==1)
        	{	
        		sapType.setControllingArea("H249");
        		sapType.setDocumentNo("500100421");
        		sapType.setPostingRow("002");
        		sapType.setPeriod("008");
        		sapType.setWbsElement("A00NC-003247-C2");
        		sapType.setPmOrderid("");
        		sapType.setAmountTcurr(400.00);
        		sapType.setAmountOcurr(400.00);
        		sapType.setObjectNo("");
        		sapType.setFiscalYear("2018");
        		sapType.setCostElement("7722000");
        		sapType.setTransCurr("EUR");
        		sapType.setObjectCurr("EUR");
        		//sapType.setExchangeRate(0.00000);
        		sapType.setPostingDate(dFactory.newXMLGregorianCalendar("2018-12-03"));
        		sapType.setProjectDefinition("A00NC-003247");
        		sapType.setAcHeaderTxt("test1");
        		sapType.setAcReferenceDoc("test1");
        		sapType.setPurchOrd("21101995644");
        		sapType.setPurchOrdItem("00010");
        		sapType.setPurchOrdSeq("003");
        		sapType.setVendorId("58651");
        		sapType.setVendorName("Smith and Associates"); 		
        		sapType.setSystemId("");
        	}
        	if (i==2)
        	{	
        		sapType.setControllingArea("H249");
        		sapType.setDocumentNo("50010042");
        		sapType.setPostingRow("002");
        		sapType.setPeriod("008");
        		sapType.setWbsElement("A00NC-003247-E1");
        		sapType.setPmOrderid("");
        		sapType.setAmountTcurr(500.00);
        		sapType.setAmountOcurr(500.00);
        		sapType.setObjectNo("");
        		sapType.setFiscalYear("2018");
        		sapType.setCostElement("7722001");
        		sapType.setTransCurr("EUR");
        		sapType.setObjectCurr("EUR");
        		//sapType.setExchangeRate(0.00000);
        		sapType.setPostingDate(dFactory.newXMLGregorianCalendar("2018-12-05"));
        		sapType.setProjectDefinition("A00NC-003247");
        		sapType.setAcHeaderTxt("test1");
        		sapType.setAcReferenceDoc("test1");
        		sapType.setPurchOrd("");
        		sapType.setPurchOrdItem("");
        		sapType.setPurchOrdSeq("");
        		sapType.setVendorId("586510011");
        		sapType.setVendorName("Smith and Associates test 2"); 		
        		sapType.setSystemId("");
        	}
        	sapTypes.add(sapType);
        }
        return sapTypes;
    }
}
