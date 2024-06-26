//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.10.19 at 12:33:39 AM GMT+01:00 
//


package com.bayer.integration.rest.pmorderpre;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BayerPMOrderPreAPIResultType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BayerPMOrderPreAPIResultType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Performance" type="{http://ecosys.net/api/BayerPMOrderPreAPI}PerformanceType" minOccurs="0"/>
 *         &lt;element name="Error" type="{http://ecosys.net/api/BayerPMOrderPreAPI}ErrorType" minOccurs="0"/>
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
@XmlType(name = "BayerPMOrderPreAPIResultType", propOrder = {
    "performance",
    "error"
})
public class BayerPMOrderPreAPIResultType {

    @XmlElement(name = "Performance")
    protected PerformanceType performance;
    @XmlElement(name = "Error")
    protected ErrorType error;
    @XmlAttribute(name = "successFlag")
    protected Boolean successFlag;
    @XmlAttribute(name = "schemaLocation")
    protected String schemaLocation;

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
