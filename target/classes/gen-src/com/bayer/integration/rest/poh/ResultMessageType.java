//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.11.28 at 05:07:12 PM CET 
//


package com.bayer.integration.rest.poh;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ResultMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ResultMessageType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="KeyName" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
 *         &lt;element name="Message" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
 *         &lt;element name="Exception" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
 *         &lt;element name="StackTrace" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="level" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="reasonCode" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResultMessageType", propOrder = {
    "keyName",
    "message",
    "exception",
    "stackTrace"
})
public class ResultMessageType {

    @XmlElement(name = "KeyName", required = true)
    protected Object keyName;
    @XmlElement(name = "Message", required = true)
    protected Object message;
    @XmlElement(name = "Exception", required = true)
    protected Object exception;
    @XmlElement(name = "StackTrace", required = true)
    protected Object stackTrace;
    @XmlAttribute(name = "level")
    protected String level;
    @XmlAttribute(name = "reasonCode")
    protected String reasonCode;

    /**
     * Gets the value of the keyName property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getKeyName() {
        return keyName;
    }

    /**
     * Sets the value of the keyName property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setKeyName(Object value) {
        this.keyName = value;
    }

    /**
     * Gets the value of the message property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getMessage() {
        return message;
    }

    /**
     * Sets the value of the message property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setMessage(Object value) {
        this.message = value;
    }

    /**
     * Gets the value of the exception property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getException() {
        return exception;
    }

    /**
     * Sets the value of the exception property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setException(Object value) {
        this.exception = value;
    }

    /**
     * Gets the value of the stackTrace property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getStackTrace() {
        return stackTrace;
    }

    /**
     * Sets the value of the stackTrace property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setStackTrace(Object value) {
        this.stackTrace = value;
    }

    /**
     * Gets the value of the level property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLevel() {
        return level;
    }

    /**
     * Sets the value of the level property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLevel(String value) {
        this.level = value;
    }

    /**
     * Gets the value of the reasonCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReasonCode() {
        return reasonCode;
    }

    /**
     * Sets the value of the reasonCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReasonCode(String value) {
        this.reasonCode = value;
    }

}