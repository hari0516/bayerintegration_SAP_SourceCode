//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2021.02.09 at 01:15:32 PM EST 
//


package com.bayer.integration.rest.polhis;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.bayer.integration.rest.polhis package. 
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
public class PhsObjectFactory {

    private final static QName _BayerCommitmentLIHistoryAPIRequest_QNAME = new QName("http://ecosys.net/api/BayerCommitmentLIHistoryAPI", "BayerCommitmentLIHistoryAPIRequest");
    private final static QName _BayerCommitmentLIHistoryAPIResult_QNAME = new QName("http://ecosys.net/api/BayerCommitmentLIHistoryAPI", "BayerCommitmentLIHistoryAPIResult");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.bayer.integration.rest.polhis
     * 
     */
    public PhsObjectFactory() {
    }

    /**
     * Create an instance of {@link BayerCommitmentLIHistoryAPIRequestType }
     * 
     */
    public BayerCommitmentLIHistoryAPIRequestType createBayerCommitmentLIHistoryAPIRequestType() {
        return new BayerCommitmentLIHistoryAPIRequestType();
    }

    /**
     * Create an instance of {@link BayerCommitmentLIHistoryAPIResultType }
     * 
     */
    public BayerCommitmentLIHistoryAPIResultType createBayerCommitmentLIHistoryAPIResultType() {
        return new BayerCommitmentLIHistoryAPIResultType();
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
     * Create an instance of {@link BayerCommitmentLIHistoryAPIType }
     * 
     */
    public BayerCommitmentLIHistoryAPIType createBayerCommitmentLIHistoryAPIType() {
        return new BayerCommitmentLIHistoryAPIType();
    }

    /**
     * Create an instance of {@link ResultMessageType }
     * 
     */
    public ResultMessageType createResultMessageType() {
        return new ResultMessageType();
    }

    /**
     * Create an instance of {@link PhsObjectResultType }
     * 
     */
    public PhsObjectResultType createObjectResultType() {
        return new PhsObjectResultType();
    }

    /**
     * Create an instance of {@link DocumentLinkType }
     * 
     */
    public DocumentLinkType createDocumentLinkType() {
        return new DocumentLinkType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BayerCommitmentLIHistoryAPIRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ecosys.net/api/BayerCommitmentLIHistoryAPI", name = "BayerCommitmentLIHistoryAPIRequest")
    public JAXBElement<BayerCommitmentLIHistoryAPIRequestType> createBayerCommitmentLIHistoryAPIRequest(BayerCommitmentLIHistoryAPIRequestType value) {
        return new JAXBElement<BayerCommitmentLIHistoryAPIRequestType>(_BayerCommitmentLIHistoryAPIRequest_QNAME, BayerCommitmentLIHistoryAPIRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BayerCommitmentLIHistoryAPIResultType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ecosys.net/api/BayerCommitmentLIHistoryAPI", name = "BayerCommitmentLIHistoryAPIResult")
    public JAXBElement<BayerCommitmentLIHistoryAPIResultType> createBayerCommitmentLIHistoryAPIResult(BayerCommitmentLIHistoryAPIResultType value) {
        return new JAXBElement<BayerCommitmentLIHistoryAPIResultType>(_BayerCommitmentLIHistoryAPIResult_QNAME, BayerCommitmentLIHistoryAPIResultType.class, null, value);
    }

}
