//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.10.20 at 02:15:08 PM EDT 
//


package com.bayer.integration.rest.wbs;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PerformanceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PerformanceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ElapsedSeconds" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="CPUSeconds" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="DatabaseSeconds" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PerformanceType", propOrder = {
    "elapsedSeconds",
    "cpuSeconds",
    "databaseSeconds"
})
public class PerformanceType {

    @XmlElement(name = "ElapsedSeconds")
    protected double elapsedSeconds;
    @XmlElement(name = "CPUSeconds")
    protected double cpuSeconds;
    @XmlElement(name = "DatabaseSeconds")
    protected double databaseSeconds;

    /**
     * Gets the value of the elapsedSeconds property.
     * 
     */
    public double getElapsedSeconds() {
        return elapsedSeconds;
    }

    /**
     * Sets the value of the elapsedSeconds property.
     * 
     */
    public void setElapsedSeconds(double value) {
        this.elapsedSeconds = value;
    }

    /**
     * Gets the value of the cpuSeconds property.
     * 
     */
    public double getCPUSeconds() {
        return cpuSeconds;
    }

    /**
     * Sets the value of the cpuSeconds property.
     * 
     */
    public void setCPUSeconds(double value) {
        this.cpuSeconds = value;
    }

    /**
     * Gets the value of the databaseSeconds property.
     * 
     */
    public double getDatabaseSeconds() {
        return databaseSeconds;
    }

    /**
     * Sets the value of the databaseSeconds property.
     * 
     */
    public void setDatabaseSeconds(double value) {
        this.databaseSeconds = value;
    }

}
