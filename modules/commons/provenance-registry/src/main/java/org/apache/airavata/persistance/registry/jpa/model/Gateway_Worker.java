package org.apache.airavata.persistance.registry.jpa.model;

import javax.persistence.Entity;
import javax.persistence.IdClass;

@Entity
@IdClass(Gateway_Worker_PK.class)
public class Gateway_Worker {


}

class Gateway_Worker_PK {
    private int gateway_ID;
    private int user_ID;

    Gateway_Worker_PK() {
    }

    public int getGateway_ID() {
        return gateway_ID;
    }

    public int getUser_ID() {
        return user_ID;
    }

    public void setGateway_ID(int gateway_ID) {
        this.gateway_ID = gateway_ID;
    }

    public void setUser_ID(int user_ID) {
        this.user_ID = user_ID;
    }
}
