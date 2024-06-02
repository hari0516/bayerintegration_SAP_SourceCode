//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2021.02.09 at 01:15:15 PM EST 
//


package com.bayer.integration.rest.polprd;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for BayerCommitmentLIPRDAPIType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BayerCommitmentLIPRDAPIType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="InternalID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CostObjectHierarchyPathID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CostObjectExternalKey" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CommitmentID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SAPPurchasingDocumentNumberID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SAPPurchasingDocumentLineItemNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CostObjectID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CostObjectName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="SAPWBSElement" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CostAccountID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CostAccountName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CostTransactionCurrency" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="CostCostObjectCurrency" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="CurrencyTransactionCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CurrencyCostObjectCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="VersionID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ConversionRateCostObjectCurrency" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="TransactionDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="DeletionFlagID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ExternalKey" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SAPPurchaseRequisitionNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SAPPurchaseRequisitionLineItemNumbe" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="FinalConfirmationID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SAPPRProcessingState" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SAPPRProcessingStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SAPPurcharsingOrderSeqNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ActualCostTransactionCurrency" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="Obligo" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="SAPExchangeRate" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="Receiver" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Requestor" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="LineItemText" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="POQuantity" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="PRQuantity" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="UnitofMeasureID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PODistributionPCT" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="TransactionExchangeRateSource" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CostExternal" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="AlternateCostExternal" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="SAPPRExchangeRate" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="CommitmentIgnoreFlagID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CommitmentTypeID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BayerCommitmentLIPRDAPIType", propOrder = {
    "internalID",
    "costObjectHierarchyPathID",
    "costObjectExternalKey",
    "commitmentID",
    "sapPurchasingDocumentNumberID",
    "sapPurchasingDocumentLineItemNumber",
    "costObjectID",
    "costObjectName",
    "sapwbsElement",
    "costAccountID",
    "costAccountName",
    "costTransactionCurrency",
    "costCostObjectCurrency",
    "currencyTransactionCode",
    "currencyCostObjectCode",
    "versionID",
    "conversionRateCostObjectCurrency",
    "transactionDate",
    "deletionFlagID",
    "externalKey",
    "sapPurchaseRequisitionNumber",
    "sapPurchaseRequisitionLineItemNumbe",
    "finalConfirmationID",
    "sapprProcessingState",
    "sapprProcessingStatus",
    "sapPurcharsingOrderSeqNumber",
    "actualCostTransactionCurrency",
    "obligo",
    "sapExchangeRate",
    "receiver",
    "requestor",
    "lineItemText",
    "poQuantity",
    "prQuantity",
    "unitofMeasureID",
    "poDistributionPCT",
    "transactionExchangeRateSource",
    "costExternal",
    "alternateCostExternal",
    "sapprExchangeRate",
    "commitmentIgnoreFlagID",
    "commitmentTypeID"
})
public class BayerCommitmentLIPRDAPIType {

    @XmlElement(name = "InternalID")
    protected String internalID;
    @XmlElement(name = "CostObjectHierarchyPathID", required = true)
    protected String costObjectHierarchyPathID;
    @XmlElement(name = "CostObjectExternalKey", required = true)
    protected String costObjectExternalKey;
    @XmlElement(name = "CommitmentID")
    protected String commitmentID;
    @XmlElement(name = "SAPPurchasingDocumentNumberID")
    protected String sapPurchasingDocumentNumberID;
    @XmlElement(name = "SAPPurchasingDocumentLineItemNumber")
    protected String sapPurchasingDocumentLineItemNumber;
    @XmlElement(name = "CostObjectID", required = true)
    protected String costObjectID;
    @XmlElement(name = "CostObjectName", required = true)
    protected String costObjectName;
    @XmlElement(name = "SAPWBSElement")
    protected String sapwbsElement;
    @XmlElement(name = "CostAccountID", required = true)
    protected String costAccountID;
    @XmlElement(name = "CostAccountName", required = true)
    protected String costAccountName;
    @XmlElement(name = "CostTransactionCurrency")
    protected Double costTransactionCurrency;
    @XmlElement(name = "CostCostObjectCurrency")
    protected Double costCostObjectCurrency;
    @XmlElement(name = "CurrencyTransactionCode", required = true)
    protected String currencyTransactionCode;
    @XmlElement(name = "CurrencyCostObjectCode")
    protected String currencyCostObjectCode;
    @XmlElement(name = "VersionID")
    protected String versionID;
    @XmlElement(name = "ConversionRateCostObjectCurrency")
    protected Double conversionRateCostObjectCurrency;
    @XmlElement(name = "TransactionDate")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar transactionDate;
    @XmlElement(name = "DeletionFlagID")
    protected String deletionFlagID;
    @XmlElement(name = "ExternalKey")
    protected String externalKey;
    @XmlElement(name = "SAPPurchaseRequisitionNumber")
    protected String sapPurchaseRequisitionNumber;
    @XmlElement(name = "SAPPurchaseRequisitionLineItemNumbe")
    protected String sapPurchaseRequisitionLineItemNumbe;
    @XmlElement(name = "FinalConfirmationID")
    protected String finalConfirmationID;
    @XmlElement(name = "SAPPRProcessingState")
    protected String sapprProcessingState;
    @XmlElement(name = "SAPPRProcessingStatus")
    protected String sapprProcessingStatus;
    @XmlElement(name = "SAPPurcharsingOrderSeqNumber")
    protected String sapPurcharsingOrderSeqNumber;
    @XmlElement(name = "ActualCostTransactionCurrency")
    protected Double actualCostTransactionCurrency;
    @XmlElement(name = "Obligo")
    protected Double obligo;
    @XmlElement(name = "SAPExchangeRate")
    protected Double sapExchangeRate;
    @XmlElement(name = "Receiver")
    protected String receiver;
    @XmlElement(name = "Requestor")
    protected String requestor;
    @XmlElement(name = "LineItemText")
    protected String lineItemText;
    @XmlElement(name = "POQuantity")
    protected Double poQuantity;
    @XmlElement(name = "PRQuantity")
    protected Double prQuantity;
    @XmlElement(name = "UnitofMeasureID")
    protected String unitofMeasureID;
    @XmlElement(name = "PODistributionPCT")
    protected Double poDistributionPCT;
    @XmlElement(name = "TransactionExchangeRateSource")
    protected String transactionExchangeRateSource;
    @XmlElement(name = "CostExternal")
    protected Double costExternal;
    @XmlElement(name = "AlternateCostExternal")
    protected Double alternateCostExternal;
    @XmlElement(name = "SAPPRExchangeRate")
    protected Double sapprExchangeRate;
    @XmlElement(name = "CommitmentIgnoreFlagID")
    protected String commitmentIgnoreFlagID;
    @XmlElement(name = "CommitmentTypeID")
    protected String commitmentTypeID;

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
     * Gets the value of the costObjectHierarchyPathID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCostObjectHierarchyPathID() {
        return costObjectHierarchyPathID;
    }

    /**
     * Sets the value of the costObjectHierarchyPathID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCostObjectHierarchyPathID(String value) {
        this.costObjectHierarchyPathID = value;
    }

    /**
     * Gets the value of the costObjectExternalKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCostObjectExternalKey() {
        return costObjectExternalKey;
    }

    /**
     * Sets the value of the costObjectExternalKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCostObjectExternalKey(String value) {
        this.costObjectExternalKey = value;
    }

    /**
     * Gets the value of the commitmentID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCommitmentID() {
        return commitmentID;
    }

    /**
     * Sets the value of the commitmentID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCommitmentID(String value) {
        this.commitmentID = value;
    }

    /**
     * Gets the value of the sapPurchasingDocumentNumberID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSAPPurchasingDocumentNumberID() {
        return sapPurchasingDocumentNumberID;
    }

    /**
     * Sets the value of the sapPurchasingDocumentNumberID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSAPPurchasingDocumentNumberID(String value) {
        this.sapPurchasingDocumentNumberID = value;
    }

    /**
     * Gets the value of the sapPurchasingDocumentLineItemNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSAPPurchasingDocumentLineItemNumber() {
        return sapPurchasingDocumentLineItemNumber;
    }

    /**
     * Sets the value of the sapPurchasingDocumentLineItemNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSAPPurchasingDocumentLineItemNumber(String value) {
        this.sapPurchasingDocumentLineItemNumber = value;
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

    /**
     * Gets the value of the costAccountID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCostAccountID() {
        return costAccountID;
    }

    /**
     * Sets the value of the costAccountID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCostAccountID(String value) {
        this.costAccountID = value;
    }

    /**
     * Gets the value of the costAccountName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCostAccountName() {
        return costAccountName;
    }

    /**
     * Sets the value of the costAccountName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCostAccountName(String value) {
        this.costAccountName = value;
    }

    /**
     * Gets the value of the costTransactionCurrency property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getCostTransactionCurrency() {
        return costTransactionCurrency;
    }

    /**
     * Sets the value of the costTransactionCurrency property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setCostTransactionCurrency(Double value) {
        this.costTransactionCurrency = value;
    }

    /**
     * Gets the value of the costCostObjectCurrency property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getCostCostObjectCurrency() {
        return costCostObjectCurrency;
    }

    /**
     * Sets the value of the costCostObjectCurrency property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setCostCostObjectCurrency(Double value) {
        this.costCostObjectCurrency = value;
    }

    /**
     * Gets the value of the currencyTransactionCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCurrencyTransactionCode() {
        return currencyTransactionCode;
    }

    /**
     * Sets the value of the currencyTransactionCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCurrencyTransactionCode(String value) {
        this.currencyTransactionCode = value;
    }

    /**
     * Gets the value of the currencyCostObjectCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCurrencyCostObjectCode() {
        return currencyCostObjectCode;
    }

    /**
     * Sets the value of the currencyCostObjectCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCurrencyCostObjectCode(String value) {
        this.currencyCostObjectCode = value;
    }

    /**
     * Gets the value of the versionID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersionID() {
        return versionID;
    }

    /**
     * Sets the value of the versionID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersionID(String value) {
        this.versionID = value;
    }

    /**
     * Gets the value of the conversionRateCostObjectCurrency property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getConversionRateCostObjectCurrency() {
        return conversionRateCostObjectCurrency;
    }

    /**
     * Sets the value of the conversionRateCostObjectCurrency property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setConversionRateCostObjectCurrency(Double value) {
        this.conversionRateCostObjectCurrency = value;
    }

    /**
     * Gets the value of the transactionDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTransactionDate() {
        return transactionDate;
    }

    /**
     * Sets the value of the transactionDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTransactionDate(XMLGregorianCalendar value) {
        this.transactionDate = value;
    }

    /**
     * Gets the value of the deletionFlagID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeletionFlagID() {
        return deletionFlagID;
    }

    /**
     * Sets the value of the deletionFlagID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeletionFlagID(String value) {
        this.deletionFlagID = value;
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
     * Gets the value of the sapPurchaseRequisitionNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSAPPurchaseRequisitionNumber() {
        return sapPurchaseRequisitionNumber;
    }

    /**
     * Sets the value of the sapPurchaseRequisitionNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSAPPurchaseRequisitionNumber(String value) {
        this.sapPurchaseRequisitionNumber = value;
    }

    /**
     * Gets the value of the sapPurchaseRequisitionLineItemNumbe property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSAPPurchaseRequisitionLineItemNumbe() {
        return sapPurchaseRequisitionLineItemNumbe;
    }

    /**
     * Sets the value of the sapPurchaseRequisitionLineItemNumbe property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSAPPurchaseRequisitionLineItemNumbe(String value) {
        this.sapPurchaseRequisitionLineItemNumbe = value;
    }

    /**
     * Gets the value of the finalConfirmationID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFinalConfirmationID() {
        return finalConfirmationID;
    }

    /**
     * Sets the value of the finalConfirmationID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFinalConfirmationID(String value) {
        this.finalConfirmationID = value;
    }

    /**
     * Gets the value of the sapprProcessingState property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSAPPRProcessingState() {
        return sapprProcessingState;
    }

    /**
     * Sets the value of the sapprProcessingState property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSAPPRProcessingState(String value) {
        this.sapprProcessingState = value;
    }

    /**
     * Gets the value of the sapprProcessingStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSAPPRProcessingStatus() {
        return sapprProcessingStatus;
    }

    /**
     * Sets the value of the sapprProcessingStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSAPPRProcessingStatus(String value) {
        this.sapprProcessingStatus = value;
    }

    /**
     * Gets the value of the sapPurcharsingOrderSeqNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSAPPurcharsingOrderSeqNumber() {
        return sapPurcharsingOrderSeqNumber;
    }

    /**
     * Sets the value of the sapPurcharsingOrderSeqNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSAPPurcharsingOrderSeqNumber(String value) {
        this.sapPurcharsingOrderSeqNumber = value;
    }

    /**
     * Gets the value of the actualCostTransactionCurrency property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getActualCostTransactionCurrency() {
        return actualCostTransactionCurrency;
    }

    /**
     * Sets the value of the actualCostTransactionCurrency property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setActualCostTransactionCurrency(Double value) {
        this.actualCostTransactionCurrency = value;
    }

    /**
     * Gets the value of the obligo property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getObligo() {
        return obligo;
    }

    /**
     * Sets the value of the obligo property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setObligo(Double value) {
        this.obligo = value;
    }

    /**
     * Gets the value of the sapExchangeRate property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getSAPExchangeRate() {
        return sapExchangeRate;
    }

    /**
     * Sets the value of the sapExchangeRate property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setSAPExchangeRate(Double value) {
        this.sapExchangeRate = value;
    }

    /**
     * Gets the value of the receiver property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReceiver() {
        return receiver;
    }

    /**
     * Sets the value of the receiver property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReceiver(String value) {
        this.receiver = value;
    }

    /**
     * Gets the value of the requestor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequestor() {
        return requestor;
    }

    /**
     * Sets the value of the requestor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequestor(String value) {
        this.requestor = value;
    }

    /**
     * Gets the value of the lineItemText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLineItemText() {
        return lineItemText;
    }

    /**
     * Sets the value of the lineItemText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLineItemText(String value) {
        this.lineItemText = value;
    }

    /**
     * Gets the value of the poQuantity property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getPOQuantity() {
        return poQuantity;
    }

    /**
     * Sets the value of the poQuantity property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setPOQuantity(Double value) {
        this.poQuantity = value;
    }

    /**
     * Gets the value of the prQuantity property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getPRQuantity() {
        return prQuantity;
    }

    /**
     * Sets the value of the prQuantity property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setPRQuantity(Double value) {
        this.prQuantity = value;
    }

    /**
     * Gets the value of the unitofMeasureID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnitofMeasureID() {
        return unitofMeasureID;
    }

    /**
     * Sets the value of the unitofMeasureID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnitofMeasureID(String value) {
        this.unitofMeasureID = value;
    }

    /**
     * Gets the value of the poDistributionPCT property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getPODistributionPCT() {
        return poDistributionPCT;
    }

    /**
     * Sets the value of the poDistributionPCT property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setPODistributionPCT(Double value) {
        this.poDistributionPCT = value;
    }

    /**
     * Gets the value of the transactionExchangeRateSource property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTransactionExchangeRateSource() {
        return transactionExchangeRateSource;
    }

    /**
     * Sets the value of the transactionExchangeRateSource property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransactionExchangeRateSource(String value) {
        this.transactionExchangeRateSource = value;
    }

    /**
     * Gets the value of the costExternal property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getCostExternal() {
        return costExternal;
    }

    /**
     * Sets the value of the costExternal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setCostExternal(Double value) {
        this.costExternal = value;
    }

    /**
     * Gets the value of the alternateCostExternal property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getAlternateCostExternal() {
        return alternateCostExternal;
    }

    /**
     * Sets the value of the alternateCostExternal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setAlternateCostExternal(Double value) {
        this.alternateCostExternal = value;
    }

    /**
     * Gets the value of the sapprExchangeRate property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getSAPPRExchangeRate() {
        return sapprExchangeRate;
    }

    /**
     * Sets the value of the sapprExchangeRate property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setSAPPRExchangeRate(Double value) {
        this.sapprExchangeRate = value;
    }

    /**
     * Gets the value of the commitmentIgnoreFlagID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCommitmentIgnoreFlagID() {
        return commitmentIgnoreFlagID;
    }

    /**
     * Sets the value of the commitmentIgnoreFlagID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCommitmentIgnoreFlagID(String value) {
        this.commitmentIgnoreFlagID = value;
    }

    /**
     * Gets the value of the commitmentTypeID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCommitmentTypeID() {
        return commitmentTypeID;
    }

    /**
     * Sets the value of the commitmentTypeID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCommitmentTypeID(String value) {
        this.commitmentTypeID = value;
    }

}