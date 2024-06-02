//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2021.02.09 at 01:15:43 PM EST 
//


package com.bayer.integration.rest.polhistg;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.bayer.integration.rest.polhistg package. 
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

    private final static QName _BayerCommitmentLIHistoryStgAPIResult_QNAME = new QName("http://ecosys.net/api/BayerCommitmentLIHistoryStgAPI", "BayerCommitmentLIHistoryStgAPIResult");
    private final static QName _BayerCommitmentLIHistoryStgAPIRequest_QNAME = new QName("http://ecosys.net/api/BayerCommitmentLIHistoryStgAPI", "BayerCommitmentLIHistoryStgAPIRequest");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.bayer.integration.rest.polhistg
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link BayerCommitmentLIHistoryStgAPIRequestType }
     * 
     */
    public BayerCommitmentLIHistoryStgAPIRequestType createBayerCommitmentLIHistoryStgAPIRequestType() {
        return new BayerCommitmentLIHistoryStgAPIRequestType();
    }

    /**
     * Create an instance of {@link BayerCommitmentLIHistoryStgAPIResultType }
     * 
     */
    public BayerCommitmentLIHistoryStgAPIResultType createBayerCommitmentLIHistoryStgAPIResultType() {
        return new BayerCommitmentLIHistoryStgAPIResultType();
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
     * Create an instance of {@link BayerCommitmentLIHistoryStgAPIType }
     * 
     */
    public BayerCommitmentLIHistoryStgAPIType createBayerCommitmentLIHistoryStgAPIType() {
        return new BayerCommitmentLIHistoryStgAPIType();
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
     * Create an instance of {@link JAXBElement }{@code <}{@link BayerCommitmentLIHistoryStgAPIResultType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ecosys.net/api/BayerCommitmentLIHistoryStgAPI", name = "BayerCommitmentLIHistoryStgAPIResult")
    public JAXBElement<BayerCommitmentLIHistoryStgAPIResultType> createBayerCommitmentLIHistoryStgAPIResult(BayerCommitmentLIHistoryStgAPIResultType value) {
        return new JAXBElement<BayerCommitmentLIHistoryStgAPIResultType>(_BayerCommitmentLIHistoryStgAPIResult_QNAME, BayerCommitmentLIHistoryStgAPIResultType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BayerCommitmentLIHistoryStgAPIRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ecosys.net/api/BayerCommitmentLIHistoryStgAPI", name = "BayerCommitmentLIHistoryStgAPIRequest")
    public JAXBElement<BayerCommitmentLIHistoryStgAPIRequestType> createBayerCommitmentLIHistoryStgAPIRequest(BayerCommitmentLIHistoryStgAPIRequestType value) {
        return new JAXBElement<BayerCommitmentLIHistoryStgAPIRequestType>(_BayerCommitmentLIHistoryStgAPIRequest_QNAME, BayerCommitmentLIHistoryStgAPIRequestType.class, null, value);
    }

}
