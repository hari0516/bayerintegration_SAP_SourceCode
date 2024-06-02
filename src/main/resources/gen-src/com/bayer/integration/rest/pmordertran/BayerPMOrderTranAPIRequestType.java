//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.10.19 at 12:33:59 AM GMT+01:00 
//


package com.bayer.integration.rest.pmordertran;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BayerPMOrderTranAPIRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BayerPMOrderTranAPIRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="BayerPMOrderTranAPI" type="{http://ecosys.net/api/BayerPMOrderTranAPI}BayerPMOrderTranAPIType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="schemaLocation" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BayerPMOrderTranAPIRequestType", propOrder = {
    "bayerPMOrderTranAPI"
})
public class BayerPMOrderTranAPIRequestType {

    @XmlElement(name = "BayerPMOrderTranAPI")
    protected List<BayerPMOrderTranAPIType> bayerPMOrderTranAPI;
    @XmlAttribute(name = "schemaLocation")
    protected String schemaLocation;

    /**
     * Gets the value of the bayerPMOrderTranAPI property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bayerPMOrderTranAPI property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBayerPMOrderTranAPI().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BayerPMOrderTranAPIType }
     * 
     * 
     */
    public List<BayerPMOrderTranAPIType> getBayerPMOrderTranAPI() {
        if (bayerPMOrderTranAPI == null) {
            bayerPMOrderTranAPI = new ArrayList<BayerPMOrderTranAPIType>();
        }
        return this.bayerPMOrderTranAPI;
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
