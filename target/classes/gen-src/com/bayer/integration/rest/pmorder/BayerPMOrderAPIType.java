//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.12.23 at 07:26:13 AM EST 
//


package com.bayer.integration.rest.pmorder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for BayerPMOrderAPIType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BayerPMOrderAPIType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ParentCostObjectExternalKey" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CostObjectID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CostObjectName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PMEstimatedCost" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="PMActualCost" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="CostObjectStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CostObjectTypeName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ExternalKey" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PmStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="COParentChangedID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ParentCOChangeDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="PMOrderStgDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="SAPWBSElement" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BayerPMOrderAPIType", propOrder = {
    "parentCostObjectExternalKey",
    "costObjectID",
    "costObjectName",
    "pmEstimatedCost",
    "pmActualCost",
    "costObjectStatus",
    "costObjectTypeName",
    "externalKey",
    "pmStatus",
    "coParentChangedID",
    "parentCOChangeDate",
    "pmOrderStgDate",
    "sapwbsElement"
})
public class BayerPMOrderAPIType {

    @XmlElement(name = "ParentCostObjectExternalKey")
    protected String parentCostObjectExternalKey;
    @XmlElement(name = "CostObjectID", required = true)
    protected String costObjectID;
    @XmlElement(name = "CostObjectName", required = true)
    protected String costObjectName;
    @XmlElement(name = "PMEstimatedCost")
    protected Double pmEstimatedCost;
    @XmlElement(name = "PMActualCost")
    protected Double pmActualCost;
    @XmlElement(name = "CostObjectStatus")
    protected String costObjectStatus;
    @XmlElement(name = "CostObjectTypeName")
    protected String costObjectTypeName;
    @XmlElement(name = "ExternalKey")
    protected String externalKey;
    @XmlElement(name = "PmStatus")
    protected String pmStatus;
    @XmlElement(name = "COParentChangedID")
    protected String coParentChangedID;
    @XmlElement(name = "ParentCOChangeDate")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar parentCOChangeDate;
    @XmlElement(name = "PMOrderStgDate")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar pmOrderStgDate;
    @XmlElement(name = "SAPWBSElement")
    protected String sapwbsElement;

    /**
     * Gets the value of the parentCostObjectExternalKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParentCostObjectExternalKey() {
        return parentCostObjectExternalKey;
    }

    /**
     * Sets the value of the parentCostObjectExternalKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParentCostObjectExternalKey(String value) {
        this.parentCostObjectExternalKey = value;
    }

    /**
     * Gets the value of the costObjectID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCostObjectID() {
        return costObjectID;
    }

    /**
     * Sets the value of the costObjectID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCostObjectID(String value) {
        this.costObjectID = value;
    }

    /**
     * Gets the value of the costObjectName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCostObjectName() {
        return costObjectName;
    }

    /**
     * Sets the value of the costObjectName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCostObjectName(String value) {
        this.costObjectName = value;
    }

    /**
     * Gets the value of the pmEstimatedCost property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getPMEstimatedCost() {
        return pmEstimatedCost;
    }

    /**
     * Sets the value of the pmEstimatedCost property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setPMEstimatedCost(Double value) {
        this.pmEstimatedCost = value;
    }

    /**
     * Gets the value of the pmActualCost property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getPMActualCost() {
        return pmActualCost;
    }

    /**
     * Sets the value of the pmActualCost property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setPMActualCost(Double value) {
        this.pmActualCost = value;
    }

    /**
     * Gets the value of the costObjectStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCostObjectStatus() {
        return costObjectStatus;
    }

    /**
     * Sets the value of the costObjectStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCostObjectStatus(String value) {
        this.costObjectStatus = value;
    }

    /**
     * Gets the value of the costObjectTypeName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCostObjectTypeName() {
        return costObjectTypeName;
    }

    /**
     * Sets the value of the costObjectTypeName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCostObjectTypeName(String value) {
        this.costObjectTypeName = value;
    }

    /**
     * Gets the value of the externalKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExternalKey() {
        return externalKey;
    }

    /**
     * Sets the value of the externalKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExternalKey(String value) {
        this.externalKey = value;
    }

    /**
     * Gets the value of the pmStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPmStatus() {
        return pmStatus;
    }

    /**
     * Sets the value of the pmStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPmStatus(String value) {
        this.pmStatus = value;
    }

    /**
     * Gets the value of the coParentChangedID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCOParentChangedID() {
        return coParentChangedID;
    }

    /**
     * Sets the value of the coParentChangedID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCOParentChangedID(String value) {
        this.coParentChangedID = value;
    }

    /**
     * Gets the value of the parentCOChangeDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getParentCOChangeDate() {
        return parentCOChangeDate;
    }

    /**
     * Sets the value of the parentCOChangeDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setParentCOChangeDate(XMLGregorianCalendar value) {
        this.parentCOChangeDate = value;
    }

    /**
     * Gets the value of the pmOrderStgDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getPMOrderStgDate() {
        return pmOrderStgDate;
    }

    /**
     * Sets the value of the pmOrderStgDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setPMOrderStgDate(XMLGregorianCalendar value) {
        this.pmOrderStgDate = value;
    }

    /**
     * Gets the value of the sapwbsElement property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSAPWBSElement() {
        return sapwbsElement;
    }

    /**
     * Sets the value of the sapwbsElement property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSAPWBSElement(String value) {
        this.sapwbsElement = value;
    }

}
