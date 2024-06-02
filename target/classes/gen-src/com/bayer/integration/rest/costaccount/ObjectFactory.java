//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.04.15 at 09:24:55 PM CEST 
//


package com.bayer.integration.rest.costaccount;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.bayer.integration.rest.costaccount package. 
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
public class ObjectFactory {

    private final static QName _BayerCostAccountsAPIResult_QNAME = new QName("http://ecosys.net/api/BayerCostAccountsAPI", "BayerCostAccountsAPIResult");
    private final static QName _BayerCostAccountsAPIRequest_QNAME = new QName("http://ecosys.net/api/BayerCostAccountsAPI", "BayerCostAccountsAPIRequest");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.bayer.integration.rest.costaccount
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link BayerCostAccountsAPIResultType }
     * 
     */
    public BayerCostAccountsAPIResultType createBayerCostAccountsAPIResultType() {
        return new BayerCostAccountsAPIResultType();
    }

    /**
     * Create an instance of {@link BayerCostAccountsAPIRequestType }
     * 
     */
    public BayerCostAccountsAPIRequestType createBayerCostAccountsAPIRequestType() {
        return new BayerCostAccountsAPIRequestType();
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
     * Create an instance of {@link ErrorType }
     * 
     */
    public ErrorType createErrorType() {
        return new ErrorType();
    }

    /**
     * Create an instance of {@link BayerCostAccountsAPIType }
     * 
     */
    public BayerCostAccountsAPIType createBayerCostAccountsAPIType() {
        return new BayerCostAccountsAPIType();
    }

    /**
     * Create an instance of {@link ResultMessageType }
     * 
     */
    public ResultMessageType createResultMessageType() {
        return new ResultMessageType();
    }

    /**
     * Create an instance of {@link ObjectResultType }
     * 
     */
    public ObjectResultType createObjectResultType() {
        return new ObjectResultType();
    }

    /**
     * Create an instance of {@link DocumentLinkType }
     * 
     */
    public DocumentLinkType createDocumentLinkType() {
        return new DocumentLinkType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BayerCostAccountsAPIResultType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ecosys.net/api/BayerCostAccountsAPI", name = "BayerCostAccountsAPIResult")
    public JAXBElement<BayerCostAccountsAPIResultType> createBayerCostAccountsAPIResult(BayerCostAccountsAPIResultType value) {
        return new JAXBElement<BayerCostAccountsAPIResultType>(_BayerCostAccountsAPIResult_QNAME, BayerCostAccountsAPIResultType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BayerCostAccountsAPIRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ecosys.net/api/BayerCostAccountsAPI", name = "BayerCostAccountsAPIRequest")
    public JAXBElement<BayerCostAccountsAPIRequestType> createBayerCostAccountsAPIRequest(BayerCostAccountsAPIRequestType value) {
        return new JAXBElement<BayerCostAccountsAPIRequestType>(_BayerCostAccountsAPIRequest_QNAME, BayerCostAccountsAPIRequestType.class, null, value);
    }

}
