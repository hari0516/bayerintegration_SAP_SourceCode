//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.09.18 at 03:23:22 PM GMT-04:00 
//


package com.bayer.integration.rest.poh2;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.bayer.integration.rest.poh2 package. 
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

    private final static QName _BayerPOPRHeadersV2APIRequest_QNAME = new QName("http://ecosys.net/api/BayerPOPRHeadersV2API", "BayerPOPRHeadersV2APIRequest");
    private final static QName _BayerPOPRHeadersV2APIResult_QNAME = new QName("http://ecosys.net/api/BayerPOPRHeadersV2API", "BayerPOPRHeadersV2APIResult");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.bayer.integration.rest.poh2
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link BayerPOPRHeadersV2APIResultType }
     * 
     */
    public BayerPOPRHeadersV2APIResultType createBayerPOPRHeadersV2APIResultType() {
        return new BayerPOPRHeadersV2APIResultType();
    }

    /**
     * Create an instance of {@link BayerPOPRHeadersV2APIRequestType }
     * 
     */
    public BayerPOPRHeadersV2APIRequestType createBayerPOPRHeadersV2APIRequestType() {
        return new BayerPOPRHeadersV2APIRequestType();
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
     * Create an instance of {@link BayerPOPRHeadersV2APIType }
     * 
     */
    public BayerPOPRHeadersV2APIType createBayerPOPRHeadersV2APIType() {
        return new BayerPOPRHeadersV2APIType();
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
     * Create an instance of {@link JAXBElement }{@code <}{@link BayerPOPRHeadersV2APIRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ecosys.net/api/BayerPOPRHeadersV2API", name = "BayerPOPRHeadersV2APIRequest")
    public JAXBElement<BayerPOPRHeadersV2APIRequestType> createBayerPOPRHeadersV2APIRequest(BayerPOPRHeadersV2APIRequestType value) {
        return new JAXBElement<BayerPOPRHeadersV2APIRequestType>(_BayerPOPRHeadersV2APIRequest_QNAME, BayerPOPRHeadersV2APIRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BayerPOPRHeadersV2APIResultType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ecosys.net/api/BayerPOPRHeadersV2API", name = "BayerPOPRHeadersV2APIResult")
    public JAXBElement<BayerPOPRHeadersV2APIResultType> createBayerPOPRHeadersV2APIResult(BayerPOPRHeadersV2APIResultType value) {
        return new JAXBElement<BayerPOPRHeadersV2APIResultType>(_BayerPOPRHeadersV2APIResult_QNAME, BayerPOPRHeadersV2APIResultType.class, null, value);
    }

}
