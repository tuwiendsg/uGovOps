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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Georgiana
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GovernanceScope", propOrder = {
    "query","consideringUncertainty",
   "id"
})
public class GovernanceScope {
    @XmlAttribute(name="Query")
    private String query="";
   @XmlAttribute(name = "Id")
    private String id;
   @XmlAttribute(name="ConsideringUncertainty")
   private String consideringUncertainty;

    /**
     * @return the query
     */
    public String getQuery() {
        return query;
    }

    /**
     * @param query the query to set
     */
    public void setQuery(String query) {
        this.query = query;
    }

   

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the consideringUncertainty
     */
    public String getConsideringUncertainty() {
        return consideringUncertainty;
    }

    /**
     * @param consideringUncertainty the consideringUncertainty to set
     */
    public void setConsideringUncertainty(String consideringUncertainty) {
        this.consideringUncertainty = consideringUncertainty;
    }


}
