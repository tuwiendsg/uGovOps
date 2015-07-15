/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.rsybl.operationsmanagementplatform.entities.interfaces;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Georgiana
 */
public interface IInteraction {
public interface InteractionType extends Serializable{
        String NOTIFICATION= "Notification";
        String EMERGENCY = "Emergency";
        String WARNING = "Warning";
        String REQUEST = "Request";
    }
    public Date getInitiationDate();

    public void setInitiationDate(Date initiationDate);

    public IRole getInitiator();

    public void setInitiator(IRole initiator);

    public IRole getReceiver();

    public void setReceiver(IRole receiver);

    public IMessage getMessage();

    public void setMessage(IMessage message);

    public Long getId();

    public void setId(Long id);

    public String getDialogUuid();

    public void setDialogUuid(String dialogId);

    public String getUuid();

    public void setUuid(String uuid);

    public String getType();

    public void setType(String type);
}
