
package at.ac.tuwien.dsg.rSybl.cloudInteractionUnit.enforcementPlugins.flexiant.flexConnector;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * Input parameters for method listResources
 * 
 * <p>Java class for listResources complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="listResources">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="searchFilter" type="{http://extility.flexiant.net}searchFilter" minOccurs="0"/>
 *         &lt;element name="queryLimit" type="{http://extility.flexiant.net}queryLimit" minOccurs="0"/>
 *         &lt;element name="resourceType" type="{http://extility.flexiant.net}resourceType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "listResources", propOrder = {
    "searchFilter",
    "queryLimit",
    "resourceType"
})
public class ListResources {

    protected SearchFilter searchFilter;
    protected QueryLimit queryLimit;
    protected ResourceType resourceType;

    /**
     * Gets the value of the searchFilter property.
     * 
     * @return
     *     possible object is
     *     {@link SearchFilter }
     *     
     */
    public SearchFilter getSearchFilter() {
        return searchFilter;
    }

    /**
     * Sets the value of the searchFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link SearchFilter }
     *     
     */
    public void setSearchFilter(SearchFilter value) {
        this.searchFilter = value;
    }

    /**
     * Gets the value of the queryLimit property.
     * 
     * @return
     *     possible object is
     *     {@link QueryLimit }
     *     
     */
    public QueryLimit getQueryLimit() {
        return queryLimit;
    }

    /**
     * Sets the value of the queryLimit property.
     * 
     * @param value
     *     allowed object is
     *     {@link QueryLimit }
     *     
     */
    public void setQueryLimit(QueryLimit value) {
        this.queryLimit = value;
    }

    /**
     * Gets the value of the resourceType property.
     * 
     * @return
     *     possible object is
     *     {@link ResourceType }
     *     
     */
    public ResourceType getResourceType() {
        return resourceType;
    }

    /**
     * Sets the value of the resourceType property.
     * 
     * @param value
     *     allowed object is
     *     {@link ResourceType }
     *     
     */
    public void setResourceType(ResourceType value) {
        this.resourceType = value;
    }

}
