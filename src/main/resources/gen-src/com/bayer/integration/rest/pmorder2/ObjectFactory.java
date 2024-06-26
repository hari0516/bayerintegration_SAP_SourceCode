//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2021.07.27 at 12:25:51 PM EDT 
//


package com.bayer.integration.rest.pmorder2;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.bayer.integration.rest.pmorder2 package. 
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

    private final static QName _BayerPMOrderV2APIResult_QNAME = new QName("http://ecosys.net/api/BayerPMOrderV2API", "BayerPMOrderV2APIResult");
    private final static QName _BayerPMOrderV2APIRequest_QNAME = new QName("http://ecosys.net/api/BayerPMOrderV2API", "BayerPMOrderV2APIRequest");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.bayer.integration.rest.pmorder2
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link BayerPMOrderV2APIResultType }
     * 
     */
    public BayerPMOrderV2APIResultType createBayerPMOrderV2APIResultType() {
        return new BayerPMOrderV2APIResultType();
    }

    /**
     * Create an instance of {@link BayerPMOrderV2APIRequestType }
     * 
     */
    public BayerPMOrderV2APIRequestType createBayerPMOrderV2APIRequestType() {
        return new BayerPMOrderV2APIRequestType();
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
     * Create an instance of {@link BayerPMOrderV2APIType }
     * 
     */
    public BayerPMOrderV2APIType createBayerPMOrderV2APIType() {
        return new BayerPMOrderV2APIType();
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
     * Create an instance of {@link JAXBElement }{@code <}{@link BayerPMOrderV2APIResultType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ecosys.net/api/BayerPMOrderV2API", name = "BayerPMOrderV2APIResult")
    public JAXBElement<BayerPMOrderV2APIResultType> createBayerPMOrderV2APIResult(BayerPMOrderV2APIResultType value) {
        return new JAXBElement<BayerPMOrderV2APIResultType>(_BayerPMOrderV2APIResult_QNAME, BayerPMOrderV2APIResultType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BayerPMOrderV2APIRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ecosys.net/api/BayerPMOrderV2API", name = "BayerPMOrderV2APIRequest")
    public JAXBElement<BayerPMOrderV2APIRequestType> createBayerPMOrderV2APIRequest(BayerPMOrderV2APIRequestType value) {
        return new JAXBElement<BayerPMOrderV2APIRequestType>(_BayerPMOrderV2APIRequest_QNAME, BayerPMOrderV2APIRequestType.class, null, value);
    }

}
