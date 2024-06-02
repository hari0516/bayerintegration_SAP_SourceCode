//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2021.07.26 at 04:38:36 PM EDT 
//


package com.bayer.integration.rest.wbsread;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BayerWBSReadAPIResultType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BayerWBSReadAPIResultType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Meta" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/>
 *         &lt;element name="BayerWBSReadAPI" type="{http://ecosys.net/api/BayerWBSReadAPI}BayerWBSReadAPIType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ObjectResult" type="{http://ecosys.net/api/BayerWBSReadAPI}ObjectResultType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Performance" type="{http://ecosys.net/api/BayerWBSReadAPI}PerformanceType" minOccurs="0"/>
 *         &lt;element name="Error" type="{http://ecosys.net/api/BayerWBSReadAPI}ErrorType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="successFlag" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="schemaLocation" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BayerWBSReadAPIResultType", propOrder = {
    "meta",
    "bayerWBSReadAPI",
    "objectResult",
    "performance",
    "error"
})
public class BayerWBSReadAPIResultType {

    @XmlElement(name = "Meta")
    protected Object meta;
    @XmlElement(name = "BayerWBSReadAPI")
    protected List<BayerWBSReadAPIType> bayerWBSReadAPI;
    @XmlElement(name = "ObjectResult")
    protected List<WPObjectResultType> objectResult;
    @XmlElement(name = "Performance")
    protected PerformanceType performance;
    @XmlElement(name = "Error")
    protected ErrorType error;
    @XmlAttribute(name = "successFlag")
    protected Boolean successFlag;
    @XmlAttribute(name = "schemaLocation")
    protected String schemaLocation;

    /**
     * Gets the value of the meta property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getMeta() {
        return meta;
    }

    /**
     * Sets the value of the meta property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setMeta(Object value) {
        this.meta = value;
    }

    /**
     * Gets the value of the bayerWBSReadAPI property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bayerWBSReadAPI property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBayerWBSReadAPI().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BayerWBSReadAPIType }
     * 
     * 
     */
    public List<BayerWBSReadAPIType> getBayerWBSReadAPI() {
        if (bayerWBSReadAPI == null) {
            bayerWBSReadAPI = new ArrayList<BayerWBSReadAPIType>();
        }
        return this.bayerWBSReadAPI;
    }

    /**
     * Gets the value of the objectResult property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the objectResult property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getObjectResult().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WPObjectResultType }
     * 
     * 
     */
    public List<WPObjectResultType> getObjectResult() {
        if (objectResult == null) {
            objectResult = new ArrayList<WPObjectResultType>();
        }
        return this.objectResult;
    }

    /**
     * Gets the value of the performance property.
     * 
     * @return
     *     possible object is
     *     {@link PerformanceType }
     *     
     */
    public PerformanceType getPerformance() {
        return performance;
    }

    /**
     * Sets the value of the performance property.
     * 
     * @param value
     *     allowed object is
     *     {@link PerformanceType }
     *     
     */
    public void setPerformance(PerformanceType value) {
        this.performance = value;
    }

    /**
     * Gets the value of the error property.
     * 
     * @return
     *     possible object is
     *     {@link ErrorType }
     *     
     */
    public ErrorType getError() {
        return error;
    }

    /**
     * Sets the value of the error property.
     * 
     * @param value
     *     allowed object is
     *     {@link ErrorType }
     *     
     */
    public void setError(ErrorType value) {
        this.error = value;
    }

    /**
     * Gets the value of the successFlag property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSuccessFlag() {
        return successFlag;
    }

    /**
     * Sets the value of the successFlag property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSuccessFlag(Boolean value) {
        this.successFlag = value;
    }

    /**
     * Gets the value of the schemaLocation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSchemaLocation() {
        return schemaLocation;
    }

    /**
     * Sets the value of the schemaLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSchemaLocation(String value) {
        this.schemaLocation = value;
    }

}