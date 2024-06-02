//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.05.03 at 04:34:26 AM CEST 
//


package com.bayer.integration.rest.trancategory;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.bayer.integration.rest.trancategory package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class TRCObjectFactory {

    private final static QName _BayerTransactionCategoryAPIResult_QNAME = new QName("http://ecosys.net/api/BayerTransactionCategoryAPI", "BayerTransactionCategoryAPIResult");
    private final static QName _BayerTransactionCategoryAPIRequest_QNAME = new QName("http://ecosys.net/api/BayerTransactionCategoryAPI", "BayerTransactionCategoryAPIRequest");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.bayer.integration.rest.trancategory
     * 
     */
    public TRCObjectFactory() {
    }

    /**
     * Create an instance of {@link BayerTransactionCategoryAPIResultType }
     * 
     */
    public BayerTransactionCategoryAPIResultType createBayerTransactionCategoryAPIResultType() {
        return new BayerTransactionCategoryAPIResultType();
    }

    /**
     * Create an instance of {@link BayerTransactionCategoryAPIRequestType }
     * 
     */
    public BayerTransactionCategoryAPIRequestType createBayerTransactionCategoryAPIRequestType() {
        return new BayerTransactionCategoryAPIRequestType();
    }

    /**
     * Create an instance of {@link PerformanceType }
     * 
     */
    public PerformanceType createPerformanceType() {
        return new PerformanceType();
    }

    /**
     * Create an instance of {@link DocumentValueType }
     * 
     */
    public DocumentValueType createDocumentValueType() {
        return new DocumentValueType();
    }

    /**
     * Create an instance of {@link BayerTransactionCategoryAPIType }
     * 
     */
    public BayerTransactionCategoryAPIType createBayerTransactionCategoryAPIType() {
        return new BayerTransactionCategoryAPIType();
    }

    /**
     * Create an instance of {@link ErrorType }
     * 
     */
    public ErrorType createErrorType() {
        return new ErrorType();
    }

    /**
     * Create an instance of {@link ResultMessageType }
     * 
     */
    public ResultMessageType createResultMessageType() {
        return new ResultMessageType();
    }

    /**
     * Create an instance of {@link TRCObjectResultType }
     * 
     */
    public TRCObjectResultType createObjectResultType() {
        return new TRCObjectResultType();
    }

    /**
     * Create an instance of {@link DocumentLinkType }
     * 
     */
    public DocumentLinkType createDocumentLinkType() {
        return new DocumentLinkType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BayerTransactionCategoryAPIResultType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ecosys.net/api/BayerTransactionCategoryAPI", name = "BayerTransactionCategoryAPIResult")
    public JAXBElement<BayerTransactionCategoryAPIResultType> createBayerTransactionCategoryAPIResult(BayerTransactionCategoryAPIResultType value) {
        return new JAXBElement<BayerTransactionCategoryAPIResultType>(_BayerTransactionCategoryAPIResult_QNAME, BayerTransactionCategoryAPIResultType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BayerTransactionCategoryAPIRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ecosys.net/api/BayerTransactionCategoryAPI", name = "BayerTransactionCategoryAPIRequest")
    public JAXBElement<BayerTransactionCategoryAPIRequestType> createBayerTransactionCategoryAPIRequest(BayerTransactionCategoryAPIRequestType value) {
        return new JAXBElement<BayerTransactionCategoryAPIRequestType>(_BayerTransactionCategoryAPIRequest_QNAME, BayerTransactionCategoryAPIRequestType.class, null, value);
    }

}