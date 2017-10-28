package org.apache.airavata.k8s.api.server.model.application;


import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Entity
@Table(name = "APPLICATION_OUTPUT")
public class ApplicationOutput {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;
    private String value;
    private DataType type;

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
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
        private static Map<Integer, DataType> map = new HashMap<>();

        static {
            for (DataType dataType : DataType.values()) {
                map.put(dataType.value, dataType);
            }
        }

        private DataType(int value) {
            this.value = value;
        }

        public static DataType valueOf(int dataType) {
            return map.get(dataType);
        }

        public int getValue() {
            return value;
        }
    }

}
