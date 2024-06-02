//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2021.07.26 at 04:38:36 PM EDT 
//


package com.bayer.integration.rest.wbsread;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.bayer.integration.rest.wbsread package. 
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
public class WPObjectFactory {

    private final static QName _BayerWBSReadAPIResult_QNAME = new QName("http://ecosys.net/api/BayerWBSReadAPI", "BayerWBSReadAPIResult");
    private final static QName _BayerWBSReadAPIRequest_QNAME = new QName("http://ecosys.net/api/BayerWBSReadAPI", "BayerWBSReadAPIRequest");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.bayer.integration.rest.wbsread
     * 
     */
    public WPObjectFactory() {
    }

    /**
     * Create an instance of {@link BayerWBSReadAPIRequestType }
     * 
     */
    public BayerWBSReadAPIRequestType createBayerWBSReadAPIRequestType() {
        return new BayerWBSReadAPIRequestType();
    }

    /**
     * Create an instance of {@link BayerWBSReadAPIResultType }
     * 
     */
    public BayerWBSReadAPIResultType createBayerWBSReadAPIResultType() {
        return new BayerWBSReadAPIResultType();
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
     * Create an instance of {@link ResultMessageType }
     * 
     */
    public ResultMessageType createResultMessageType() {
        return new ResultMessageType();
    }

    /**
     * Create an instance of {@link BayerWBSReadAPIType }
     * 
     */
    public BayerWBSReadAPIType createBayerWBSReadAPIType() {
        return new BayerWBSReadAPIType();
    }

    /**
     * Create an instance of {@link WPObjectResultType }
     * 
     */
    public WPObjectResultType createObjectResultType() {
        return new WPObjectResultType();
    }

    /**
     * Create an instance of {@link DocumentLinkType }
     * 
     */
    public DocumentLinkType createDocumentLinkType() {
        return new DocumentLinkType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BayerWBSReadAPIResultType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ecosys.net/api/BayerWBSReadAPI", name = "BayerWBSReadAPIResult")
    public JAXBElement<BayerWBSReadAPIResultType> createBayerWBSReadAPIResult(BayerWBSReadAPIResultType value) {
        return new JAXBElement<BayerWBSReadAPIResultType>(_BayerWBSReadAPIResult_QNAME, BayerWBSReadAPIResultType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BayerWBSReadAPIRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ecosys.net/api/BayerWBSReadAPI", name = "BayerWBSReadAPIRequest")
    public JAXBElement<BayerWBSReadAPIRequestType> createBayerWBSReadAPIRequest(BayerWBSReadAPIRequestType value) {
        return new JAXBElement<BayerWBSReadAPIRequestType>(_BayerWBSReadAPIRequest_QNAME, BayerWBSReadAPIRequestType.class, null, value);
    }

}
