package org.apache.airavata.k8s.api.server.model.experiment;

import javax.persistence.*;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Entity
@Table(name = "EXPERIMENT_INPUT_OBJECT")
public class ExperimentInputData {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;
    private String type;
    private ExperimentOutputData.DataType value;
    private String arguments;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ExperimentOutputData.DataType getValue() {
        return value;
    }

    public void setValue(ExperimentOutputData.DataType value) {
        this.value = value;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public static enum DataType {
        STRING(0),
        INTEGER(1),
        FLOAT(2),
        URI(3),
        URI_COLLECTION(4),
        STDOUT(5),
        STDERR(6);

        private final int value;

        private DataType(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }
}
