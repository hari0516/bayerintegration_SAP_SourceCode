//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.04.30 at 07:51:15 PM CEST 
//


package com.bayer.integration.rest.taskcategory;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BayerTaskCategoryAPIResultType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BayerTaskCategoryAPIResultType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Meta" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/>
 *         &lt;element name="BayerTaskCategoryAPI" type="{http://ecosys.net/api/BayerTaskCategoryAPI}BayerTaskCategoryAPIType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ObjectResult" type="{http://ecosys.net/api/BayerTaskCategoryAPI}ObjectResultType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Performance" type="{http://ecosys.net/api/BayerTaskCategoryAPI}PerformanceType" minOccurs="0"/>
 *         &lt;element name="Error" type="{http://ecosys.net/api/BayerTaskCategoryAPI}ErrorType" minOccurs="0"/>
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
@XmlType(name = "BayerTaskCategoryAPIResultType", propOrder = {
    "meta",
    "bayerTaskCategoryAPI",
    "objectResult",
    "performance",
    "error"
})
public class BayerTaskCategoryAPIResultType {

    @XmlElement(name = "Meta")
    protected Object meta;
    @XmlElement(name = "BayerTaskCategoryAPI")
    protected List<BayerTaskCategoryAPIType> bayerTaskCategoryAPI;
    @XmlElement(name = "ObjectResult")
    protected List<TACObjectResultType> objectResult;
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
     * Gets the value of the bayerTaskCategoryAPI property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bayerTaskCategoryAPI property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBayerTaskCategoryAPI().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BayerTaskCategoryAPIType }
     * 
     * 
     */
    public List<BayerTaskCategoryAPIType> getBayerTaskCategoryAPI() {
        if (bayerTaskCategoryAPI == null) {
            bayerTaskCategoryAPI = new ArrayList<BayerTaskCategoryAPIType>();
        }
        return this.bayerTaskCategoryAPI;
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
     * {@link TACObjectResultType }
     * 
     * 
     */
    public List<TACObjectResultType> getObjectResult() {
        if (objectResult == null) {
            objectResult = new ArrayList<TACObjectResultType>();
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
