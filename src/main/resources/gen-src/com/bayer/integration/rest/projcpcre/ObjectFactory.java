//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2021.02.19 at 05:34:33 PM EST 
//


package com.bayer.integration.rest.projcpcre;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.bayer.integration.rest.projcpcre package. 
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

    private final static QName _BayerProjectCopyCreateAPIResult_QNAME = new QName("http://ecosys.net/api/BayerProjectCopyCreateAPI", "BayerProjectCopyCreateAPIResult");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.bayer.integration.rest.projcpcre
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link BayerProjectCopyCreateAPIResultType }
     * 
     */
    public BayerProjectCopyCreateAPIResultType createBayerProjectCopyCreateAPIResultType() {
        return new BayerProjectCopyCreateAPIResultType();
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
     * Create an instance of {@link DocumentLinkType }
     * 
     */
    public DocumentLinkType createDocumentLinkType() {
        return new DocumentLinkType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BayerProjectCopyCreateAPIResultType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ecosys.net/api/BayerProjectCopyCreateAPI", name = "BayerProjectCopyCreateAPIResult")
    public JAXBElement<BayerProjectCopyCreateAPIResultType> createBayerProjectCopyCreateAPIResult(BayerProjectCopyCreateAPIResultType value) {
        return new JAXBElement<BayerProjectCopyCreateAPIResultType>(_BayerProjectCopyCreateAPIResult_QNAME, BayerProjectCopyCreateAPIResultType.class, null, value);
    }

}