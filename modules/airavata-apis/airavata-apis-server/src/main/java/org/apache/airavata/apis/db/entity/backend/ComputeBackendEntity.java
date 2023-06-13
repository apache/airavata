package org.apache.airavata.apis.db.entity.backend;

import org.apache.airavata.apis.db.entity.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ComputeBackendEntity extends BaseEntity {

}
