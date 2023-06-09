package org.apache.airavata.apis.db.entity.application.output;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ApplicationOutputValueEntity {

    @Id
    @Column(name = "OUTPUT_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String outputId;

    @OneToOne(mappedBy = "applicationOutputValue")
    private ApplicationOutputEntity applicationOutput;

    public String getOutputId() {
        return outputId;
    }

    public void setOutputId(String outputId) {
        this.outputId = outputId;
    }

    public ApplicationOutputEntity getApplicationOutput() {
        return applicationOutput;
    }

    public void setApplicationOutput(ApplicationOutputEntity applicationOutput) {
        this.applicationOutput = applicationOutput;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((outputId == null) ? 0 : outputId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ApplicationOutputValueEntity other = (ApplicationOutputValueEntity) obj;
        if (outputId == null) {
            if (other.outputId != null)
                return false;
        } else if (!outputId.equals(other.outputId))
            return false;
        return true;
    }

}
