//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2021.09.01 at 03:09:38 PM EDT 
//


package com.bayer.integration.rest.project;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for BayerProjectAPIType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BayerProjectAPIType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CostObjectName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CostObjectTypeName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CostObjectStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CostObjectHierarchyLevel" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="SapProjectId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SAPSystemID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="InternalID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ProjectStatusID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ProjectTypeID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ParentCostObjectID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SAPLastRunDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="RootCostObjectID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MigrationFlagID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SAPActualImportEndDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="POHistoryTrackedID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="HierarchyPathID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="OwnerOrganizationID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="RootCostObjectCurrencyCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="NewAssignedProjectFlagID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ParentProjectCopyStatusID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ParentProjectCopyTypeID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ProjectCopyTypeID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ProjectCopyStatusID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ProjectCPStatusCO" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ParentProjectCPStatusCO" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BayerProjectAPIType", propOrder = {
    "id",
    "costObjectName",
    "costObjectTypeName",
    "costObjectStatus",
    "costObjectHierarchyLevel",
    "sapProjectId",
    "sapSystemID",
    "internalID",
    "projectStatusID",
    "projectTypeID",
    "parentCostObjectID",
    "sapLastRunDate",
    "rootCostObjectID",
    "migrationFlagID",
    "sapActualImportEndDate",
    "poHistoryTrackedID",
    "hierarchyPathID",
    "ownerOrganizationID",
    "rootCostObjectCurrencyCode",
    "newAssignedProjectFlagID",
    "parentProjectCopyStatusID",
    "parentProjectCopyTypeID",
    "projectCopyTypeID",
    "projectCopyStatusID",
    "projectCPStatusCO",
    "parentProjectCPStatusCO"
})
public class BayerProjectAPIType {

    @XmlElement(name = "ID", required = true)
    protected String id;
    @XmlElement(name = "CostObjectName", required = true)
    protected String costObjectName;
    @XmlElement(name = "CostObjectTypeName", required = true)
    protected String costObjectTypeName;
    @XmlElement(name = "CostObjectStatus")
    protected String costObjectStatus;
    @XmlElement(name = "CostObjectHierarchyLevel")
    protected int costObjectHierarchyLevel;
    @XmlElement(name = "SapProjectId")
    protected String sapProjectId;
    @XmlElement(name = "SAPSystemID")
    protected String sapSystemID;
    @XmlElement(name = "InternalID")
    protected String internalID;
    @XmlElement(name = "ProjectStatusID")
    protected String projectStatusID;
    @XmlElement(name = "ProjectTypeID")
    protected String projectTypeID;
    @XmlElement(name = "ParentCostObjectID")
    protected String parentCostObjectID;
    @XmlElement(name = "SAPLastRunDate")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar sapLastRunDate;
    @XmlElement(name = "RootCostObjectID")
    protected String rootCostObjectID;
    @XmlElement(name = "MigrationFlagID")
    protected String migrationFlagID;
    @XmlElement(name = "SAPActualImportEndDate")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar sapActualImportEndDate;
    @XmlElement(name = "POHistoryTrackedID")
    protected String poHistoryTrackedID;
    @XmlElement(name = "HierarchyPathID")
    protected String hierarchyPathID;
    @XmlElement(name = "OwnerOrganizationID")
    protected String ownerOrganizationID;
    @XmlElement(name = "RootCostObjectCurrencyCode", required = true)
    protected String rootCostObjectCurrencyCode;
    @XmlElement(name = "NewAssignedProjectFlagID")
    protected String newAssignedProjectFlagID;
    @XmlElement(name = "ParentProjectCopyStatusID")
    protected String parentProjectCopyStatusID;
    @XmlElement(name = "ParentProjectCopyTypeID")
    protected String parentProjectCopyTypeID;
    @XmlElement(name = "ProjectCopyTypeID")
    protected String projectCopyTypeID;
    @XmlElement(name = "ProjectCopyStatusID")
    protected String projectCopyStatusID;
    @XmlElement(name = "ProjectCPStatusCO")
    protected String projectCPStatusCO;
    @XmlElement(name = "ParentProjectCPStatusCO")
    protected String parentProjectCPStatusCO;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getID() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setID(String value) {
        this.id = value;
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
     * Gets the value of the costObjectHierarchyLevel property.
     * 
     */
    public int getCostObjectHierarchyLevel() {
        return costObjectHierarchyLevel;
    }

    /**
     * Sets the value of the costObjectHierarchyLevel property.
     * 
     */
    public void setCostObjectHierarchyLevel(int value) {
        this.costObjectHierarchyLevel = value;
    }

    /**
     * Gets the value of the sapProjectId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSapProjectId() {
        return sapProjectId;
    }

    /**
     * Sets the value of the sapProjectId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSapProjectId(String value) {
        this.sapProjectId = value;
    }

    /**
     * Gets the value of the sapSystemID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSAPSystemID() {
        return sapSystemID;
    }

    /**
     * Sets the value of the sapSystemID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSAPSystemID(String value) {
        this.sapSystemID = value;
    }

    /**
     * Gets the value of the internalID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInternalID() {
        return internalID;
    }

    /**
     * Sets the value of the internalID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInternalID(String value) {
        this.internalID = value;
    }

    /**
     * Gets the value of the projectStatusID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProjectStatusID() {
        return projectStatusID;
    }

    /**
     * Sets the value of the projectStatusID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProjectStatusID(String value) {
        this.projectStatusID = value;
    }

    /**
     * Gets the value of the projectTypeID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProjectTypeID() {
        return projectTypeID;
    }

    /**
     * Sets the value of the projectTypeID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProjectTypeID(String value) {
        this.projectTypeID = value;
    }

    /**
     * Gets the value of the parentCostObjectID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParentCostObjectID() {
        return parentCostObjectID;
    }

    /**
     * Sets the value of the parentCostObjectID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParentCostObjectID(String value) {
        this.parentCostObjectID = value;
    }

    /**
     * Gets the value of the sapLastRunDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getSAPLastRunDate() {
        return sapLastRunDate;
    }

    /**
     * Sets the value of the sapLastRunDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setSAPLastRunDate(XMLGregorianCalendar value) {
        this.sapLastRunDate = value;
    }

    /**
     * Gets the value of the rootCostObjectID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRootCostObjectID() {
        return rootCostObjectID;
    }

    /**
     * Sets the value of the rootCostObjectID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRootCostObjectID(String value) {
        this.rootCostObjectID = value;
    }

    /**
     * Gets the value of the migrationFlagID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMigrationFlagID() {
        return migrationFlagID;
    }

    /**
     * Sets the value of the migrationFlagID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMigrationFlagID(String value) {
        this.migrationFlagID = value;
    }

    /**
     * Gets the value of the sapActualImportEndDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getSAPActualImportEndDate() {
        return sapActualImportEndDate;
    }

    /**
     * Sets the value of the sapActualImportEndDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setSAPActualImportEndDate(XMLGregorianCalendar value) {
        this.sapActualImportEndDate = value;
    }

    /**
     * Gets the value of the poHistoryTrackedID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPOHistoryTrackedID() {
        return poHistoryTrackedID;
    }

    /**
     * Sets the value of the poHistoryTrackedID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPOHistoryTrackedID(String value) {
        this.poHistoryTrackedID = value;
    }

    /**
     * Gets the value of the hierarchyPathID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHierarchyPathID() {
        return hierarchyPathID;
    }

    /**
     * Sets the value of the hierarchyPathID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHierarchyPathID(String value) {
        this.hierarchyPathID = value;
    }

    /**
     * Gets the value of the ownerOrganizationID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOwnerOrganizationID() {
        return ownerOrganizationID;
    }

    /**
     * Sets the value of the ownerOrganizationID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOwnerOrganizationID(String value) {
        this.ownerOrganizationID = value;
    }

    /**
     * Gets the value of the rootCostObjectCurrencyCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRootCostObjectCurrencyCode() {
        return rootCostObjectCurrencyCode;
    }

    /**
     * Sets the value of the rootCostObjectCurrencyCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRootCostObjectCurrencyCode(String value) {
        this.rootCostObjectCurrencyCode = value;
    }

    /**
     * Gets the value of the newAssignedProjectFlagID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNewAssignedProjectFlagID() {
        return newAssignedProjectFlagID;
    }

    /**
     * Sets the value of the newAssignedProjectFlagID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNewAssignedProjectFlagID(String value) {
        this.newAssignedProjectFlagID = value;
    }

    /**
     * Gets the value of the parentProjectCopyStatusID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParentProjectCopyStatusID() {
        return parentProjectCopyStatusID;
    }

    /**
     * Sets the value of the parentProjectCopyStatusID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParentProjectCopyStatusID(String value) {
        this.parentProjectCopyStatusID = value;
    }

    /**
     * Gets the value of the parentProjectCopyTypeID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParentProjectCopyTypeID() {
        return parentProjectCopyTypeID;
    }

    /**
     * Sets the value of the parentProjectCopyTypeID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParentProjectCopyTypeID(String value) {
        this.parentProjectCopyTypeID = value;
    }

    /**
     * Gets the value of the projectCopyTypeID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProjectCopyTypeID() {
        return projectCopyTypeID;
    }

    /**
     * Sets the value of the projectCopyTypeID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProjectCopyTypeID(String value) {
        this.projectCopyTypeID = value;
    }

    /**
     * Gets the value of the projectCopyStatusID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProjectCopyStatusID() {
        return projectCopyStatusID;
    }

    /**
     * Sets the value of the projectCopyStatusID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProjectCopyStatusID(String value) {
        this.projectCopyStatusID = value;
    }

    /**
     * Gets the value of the projectCPStatusCO property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProjectCPStatusCO() {
        return projectCPStatusCO;
    }

    /**
     * Sets the value of the projectCPStatusCO property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProjectCPStatusCO(String value) {
        this.projectCPStatusCO = value;
    }

    /**
     * Gets the value of the parentProjectCPStatusCO property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParentProjectCPStatusCO() {
        return parentProjectCPStatusCO;
    }

    /**
     * Sets the value of the parentProjectCPStatusCO property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParentProjectCPStatusCO(String value) {
        this.parentProjectCPStatusCO = value;
    }

}