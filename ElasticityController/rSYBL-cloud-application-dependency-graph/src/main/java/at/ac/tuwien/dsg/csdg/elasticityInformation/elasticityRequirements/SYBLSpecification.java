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
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;



@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "constraints",
    "strategies",
    "monitoring",
    "priorities","governanceScopes","id"
})
public class SYBLSpecification  implements Serializable{

    @XmlElement(name = "Constraint", required = true)
    protected List<Constraint> constraints = new ArrayList<Constraint>();
    @XmlElement(name = "Strategy", required = true)
    protected List<Strategy> strategies = new ArrayList<Strategy>();
    @XmlElement(name = "Monitoring", required = true)
    protected List<Monitoring> monitoring = new ArrayList<Monitoring>();
    @XmlElement(name="GovernanceScopes")
    private List<GovernanceScope> governanceScopes = new ArrayList<GovernanceScope>();
    @XmlElement(name = "Priority")
    protected List<Priority> priorities=new ArrayList<>();
    @XmlElement(name = "Notification")
    private List<Notification> notifications = new ArrayList<Notification>();
    
    @XmlAttribute(name = "type")
    protected String type;
    @XmlAttribute(name="id")
	private String id;

    /**
     * Gets the value of the constraint property.
     * 
     * @return
     *     possible object is
     *     {@link SYBLSpecification.Constraint }
     *     
     */
    public List<Constraint> getConstraint() {
        return constraints;
    }

    /**
     * Sets the value of the constraint property.
     * 
     * @param value
     *     allowed object is
     *     {@link SYBLSpecification.Constraint }
     *     
     */
    public void addConstraint(Constraint value) {
        constraints.add(value);
    }

    /**
     * Gets the value of the strategy property.
     * 
     * @return
     *     possible object is
     *     {@link SYBLSpecification.Strategy }
     *     
     */
    public List<Strategy> getStrategy() {
        return strategies;
    }

    /**
     * Sets the value of the strategy property.
     * 
     * @param value
     *     allowed object is
     *     {@link SYBLSpecification.Strategy }
     *     
     */
    public void addStrategy(Strategy value) {
        strategies.add(value);
    }

    /**
     * Gets the value of the monitoring property.
     * 
     * @return
     *     possible object is
     *     {@link SYBLSpecification.Monitoring }
     *     
     */
    public List<Monitoring> getMonitoring() {
        return monitoring;
    }

    /**
     * Sets the value of the monitoring property.
     * 
     * @param value
     *     allowed object is
     *     {@link SYBLSpecification.Monitoring }
     *     
     */
    public void addMonitoring(Monitoring value) {
        monitoring.add(value);
    }

    /**
     * Gets the value of the priority property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the priority property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPriority().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SYBLSpecification.Priority }
     * 
     * 
     */
    public List<Priority> getPriority() {
        if (priorities == null) {
            priorities = new ArrayList<Priority>();
        }
        return this.priorities;
    }

    public void addPriority(Priority p){
    	priorities.add(p);
    }
    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

	public String getComponentId() {
		return id;
	}

	public void setComponentId(String componentId) {
		this.id = componentId;
	}
	public String toString(){
		return "Monitoring" + monitoring.toString()+" Strategies"+strategies.toString()+" Constraint"+constraints.toString();
	}

    /**
     * @return the notifications
     */
    public List<Notification> getNotifications() {
        return notifications;
    }

    /**
     * @param notifications the notifications to set
     */
    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }
    public void addNotification(Notification notification){
        notifications.add(notification);
    }

    /**
     * @return the governanceScopes
     */
    public List<GovernanceScope> getGovernanceScopes() {
        return governanceScopes;
    }

    /**
     * @param governanceScopes the governanceScopes to set
     */
    public void setGovernanceScopes(List<GovernanceScope> governanceScopes) {
        this.governanceScopes = governanceScopes;
    }
    public void addGovernanceScope(GovernanceScope governanceScope){
        governanceScopes.add(governanceScope);
    }

}
