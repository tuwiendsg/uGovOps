/**
 Copyright 2013 Technische Universitat Wien (TUW), Distributed SystemsGroup E184.
 
 This work was partially supported by the European Commission in terms of the CELAR FP7 project (FP7-ICT-2011-8 #317790).
 
 This repository contains only the governance specific part of rSYBL. For full rSYBL you can access https://github.com/tuwiendsg/rSYBL
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

/**
 *  Author : Georgiana Copil - e.copil@dsg.tuwien.ac.at
 */

package at.ac.tuwien.dsg.csdg.elasticityInformation.elasticityRequirements;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "condition",
    "toEnforce"
})
public  class Strategy implements Serializable{

    @XmlElement(name = "Condition", required = true)
    protected Condition condition;
    @XmlElement(name = "ToEnforce", required = true)
    protected ToEnforce toEnforce;
    @XmlAttribute(name = "Id")
    protected String id;
    @XmlAttribute(name="Governance")
    private String governanceScope;
    @XmlAttribute(name="UncertaintyConsideration")
    private String uncertaintyConsideration;

    /**
     * Gets the value of the condition property.
     * 
     * @return
     *     possible object is
     *     {@link SYBLSpecification.Strategy.Condition }
     *     
     */
    public Condition getCondition() {
        return condition;
    }

    /**
     * Sets the value of the condition property.
     * 
     * @param value
     *     allowed object is
     *     {@link SYBLSpecification.Strategy.Condition }
     *     
     */
    public void setCondition(Condition value) {
        this.condition = value;
    }

    /**
     * Gets the value of the toEnforce property.
     * 
     * @return
     *     possible object is
     *     {@link SYBLSpecification.Strategy.ToEnforce }
     *     
     */
    public ToEnforce getToEnforce() {
        return toEnforce;
    }

    /**
     * Sets the value of the toEnforce property.
     * 
     * @param value
     *     allowed object is
     *     {@link SYBLSpecification.Strategy.ToEnforce }
     *     
     */
    public void setToEnforce(ToEnforce value) {
        this.toEnforce = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    public String toString(){
    	return  condition.toString()+toEnforce.toString();
    }

    /**
     * @return the uncertaintyConsideration
     */
    public String getUncertaintyConsideration() {
        return uncertaintyConsideration;
    }

    /**
     * @param uncertaintyConsideration the uncertaintyConsideration to set
     */
    public void setUncertaintyConsideration(String uncertaintyConsideration) {
        this.uncertaintyConsideration = uncertaintyConsideration;
    }

    /**
     * @return the governanceScope
     */
    public String getGovernanceScope() {
        return governanceScope;
    }

    /**
     * @param governanceScope the governanceScope to set
     */
    public void setGovernanceScope(String governanceScope) {
        this.governanceScope = governanceScope;
    }
  

}