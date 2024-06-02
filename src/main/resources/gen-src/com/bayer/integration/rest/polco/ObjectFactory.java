//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.07.19 at 04:12:03 PM GMT-04:00 
//


package com.bayer.integration.rest.polco;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.bayer.integration.rest.polco package. 
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

    private final static QName _BayerCommitmentLICOAPIResult_QNAME = new QName("http://ecosys.net/api/BayerCommitmentLICOAPI", "BayerCommitmentLICOAPIResult");
    private final static QName _BayerCommitmentLICOAPIRequest_QNAME = new QName("http://ecosys.net/api/BayerCommitmentLICOAPI", "BayerCommitmentLICOAPIRequest");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.bayer.integration.rest.polco
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link BayerCommitmentLICOAPIResultType }
     * 
     */
    public BayerCommitmentLICOAPIResultType createBayerCommitmentLICOAPIResultType() {
        return new BayerCommitmentLICOAPIResultType();
    }

    /**
     * Create an instance of {@link BayerCommitmentLICOAPIRequestType }
     * 
     */
    public BayerCommitmentLICOAPIRequestType createBayerCommitmentLICOAPIRequestType() {
        return new BayerCommitmentLICOAPIRequestType();
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
     * Create an instance of {@link BayerCommitmentLICOAPIType }
     * 
     */
    public BayerCommitmentLICOAPIType createBayerCommitmentLICOAPIType() {
        return new BayerCommitmentLICOAPIType();
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
     * Create an instance of {@link JAXBElement }{@code <}{@link BayerCommitmentLICOAPIResultType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ecosys.net/api/BayerCommitmentLICOAPI", name = "BayerCommitmentLICOAPIResult")
    public JAXBElement<BayerCommitmentLICOAPIResultType> createBayerCommitmentLICOAPIResult(BayerCommitmentLICOAPIResultType value) {
        return new JAXBElement<BayerCommitmentLICOAPIResultType>(_BayerCommitmentLICOAPIResult_QNAME, BayerCommitmentLICOAPIResultType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BayerCommitmentLICOAPIRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ecosys.net/api/BayerCommitmentLICOAPI", name = "BayerCommitmentLICOAPIRequest")
    public JAXBElement<BayerCommitmentLICOAPIRequestType> createBayerCommitmentLICOAPIRequest(BayerCommitmentLICOAPIRequestType value) {
        return new JAXBElement<BayerCommitmentLICOAPIRequestType>(_BayerCommitmentLICOAPIRequest_QNAME, BayerCommitmentLICOAPIRequestType.class, null, value);
    }

}
