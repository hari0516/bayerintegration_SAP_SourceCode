//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.04.30 at 07:51:15 PM CEST 
//


package com.bayer.integration.rest.taskcategory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BayerTaskCategoryAPIType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BayerTaskCategoryAPIType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TaskCategoryName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="TaskCategoryID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Active" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="ParentCategoryID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CategoryID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BayerTaskCategoryAPIType", propOrder = {
    "taskCategoryName",
    "taskCategoryID",
    "active",
    "parentCategoryID",
    "categoryID"
})
public class BayerTaskCategoryAPIType {

    @XmlElement(name = "TaskCategoryName", required = true)
    protected String taskCategoryName;
    @XmlElement(name = "TaskCategoryID", required = true)
    protected String taskCategoryID;
    @XmlElement(name = "Active")
    protected Boolean active;
    @XmlElement(name = "ParentCategoryID")
    protected String parentCategoryID;
    @XmlElement(name = "CategoryID")
    protected String categoryID;

    /**
     * Gets the value of the taskCategoryName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaskCategoryName() {
        return taskCategoryName;
    }

    /**
     * Sets the value of the taskCategoryName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaskCategoryName(String value) {
        this.taskCategoryName = value;
    }

    /**
     * Gets the value of the taskCategoryID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaskCategoryID() {
        return taskCategoryID;
    }

    /**
     * Sets the value of the taskCategoryID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaskCategoryID(String value) {
        this.taskCategoryID = value;
    }

    /**
     * Gets the value of the active property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isActive() {
        return active;
    }

    /**
     * Sets the value of the active property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setActive(Boolean value) {
        this.active = value;
    }

    /**
     * Gets the value of the parentCategoryID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParentCategoryID() {
        return parentCategoryID;
    }

    /**
     * Sets the value of the parentCategoryID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParentCategoryID(String value) {
        this.parentCategoryID = value;
    }

    /**
     * Gets the value of the categoryID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCategoryID() {
        return categoryID;
    }

    /**
     * Sets the value of the categoryID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCategoryID(String value) {
        this.categoryID = value;
    }

}