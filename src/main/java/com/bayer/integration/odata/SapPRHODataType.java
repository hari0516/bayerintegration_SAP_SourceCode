package com.bayer.integration.odata;

//
//This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
//See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
//Any modifications to this file will be lost upon recompilation of the source schema. 
//Generated on: 2018.11.13 at 10:26:17 PM EST 
//

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

/**
* <p>Java class for BayerWBSAPIType complex type.
* 
* <p>The following schema fragment specifies the expected content contained within this class.
* 
* <pre>
* &lt;complexType name="BayerWBSAPIType">
*   &lt;complexContent>
*     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
*       &lt;sequence>
*         &lt;element name="CostObjectID" type="{http://www.w3.org/2001/XMLSchema}string"/>
*         &lt;element name="CostObjectName" type="{http://www.w3.org/2001/XMLSchema}string"/>
*         &lt;element name="CostObjectHierarchyLevel" type="{http://www.w3.org/2001/XMLSchema}int"/>
*         &lt;element name="ParentCostObjectID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
*         &lt;element name="CostObjectStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
*         &lt;element name="AccountAssignmentFlag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
*         &lt;element name="ObjectClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
*         &lt;element name="LocationID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
*         &lt;element name="ResponsibleCostCenter" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
*         &lt;element name="PersonResponsible" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
*         &lt;element name="ProjectTypeID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
*         &lt;element name="CostObjectTypeInternalID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
*         &lt;element name="HierarchyPathID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
*       &lt;/sequence>
*     &lt;/restriction>
*   &lt;/complexContent>
* &lt;/complexType>
* </pre>
* 
* 
*/
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PurchOrd_HdrSet", propOrder = {
 "PurchReq",
 "PurchReqDesc",
 "PoComCost",
 "PoActCost",
 "PoDocCurr",
 "VendorId",
 "VendorName",
 "ProjectDefinition",
 "SystemId"
})

public class SapPRHODataType {
	
	@XmlElement(name = "PurchReq", required = true)
	 protected String purchReq;
	 @XmlElement(name = "PurcReqDesc")
	 protected String purchReqDesc;
	 @XmlElement(name = "PrComCost")
	 protected Double prComCost;
	 @XmlElement(name = "prActCost")
	 protected Double prActCost;
	 @XmlElement(name = "PrDocCurr")
	 protected String prDocCurr;
	 @XmlElement(name = "VendorId")
	 protected String vendorId;
	 @XmlElement(name = "VendorName")
	 protected String vendorName;
	 @XmlElement(name = "ProjectDefinition")
	 protected String projectDefinition;
	 @XmlElement(name = "SystemId", required = true)
	 protected String systemId;
	
	public String getPurchReq() {
		return purchReq;
	}
	public void setPurchReq(String purchReq) {
		this.purchReq = purchReq;
	}
	public String getPurchReqDesc() {
		return purchReqDesc;
	}
	public void setPurchReqDesc(String purchReqDesc) {
		this.purchReqDesc = purchReqDesc;
	}
	public Double getPrComCost() {
		return prComCost;
	}
	public void setPrComCost(Double prComCost) {
		this.prComCost = prComCost;
	}
	public Double getPrActCost() {
		return prActCost;
	}
	public void setPrActCost(Double prActCost) {
		this.prActCost = prActCost;
	}
	public String getPrDocCurr() {
		return prDocCurr;
	}
	public void setPrDocCurr(String prDocCurr) {
		this.prDocCurr = prDocCurr;
	}
	public String getVendorId() {
		return vendorId;
	}
	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}
	public String getVendorName() {
		return vendorName;
	}
	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}
	public String getProjectDefinition() {
		return projectDefinition;
	}
	public void setProjectDefinition(String projectDefinition) {
		this.projectDefinition = projectDefinition;
	}
	public String getSystemId() {
		return systemId;
	}
	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	 
}

