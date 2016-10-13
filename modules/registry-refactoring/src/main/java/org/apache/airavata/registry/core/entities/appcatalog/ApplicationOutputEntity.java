package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The persistent class for the application_output database table.
 */
@Entity
@Table(name = "application_output")
public class ApplicationOutput implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private ApplicationOutputPK id;

    @Column(name = "APP_ARGUMENT")
    private String appArgument;

    @Column(name = "DATA_MOVEMENT")
    private short dataMovement;

    @Column(name = "DATA_NAME_LOCATION")
    private String dataNameLocation;

    @Column(name = "DATA_TYPE")
    private String dataType;

    @Column(name = "IS_REQUIRED")
    private short isRequired;

    @Column(name = "OUTPUT_STREAMING")
    private short outputStreaming;

    @Column(name = "OUTPUT_VALUE")
    private String outputValue;

    @Column(name = "REQUIRED_TO_COMMANDLINE")
    private short requiredToCommandline;

    @Column(name = "SEARCH_QUERY")
    private String searchQuery;

    public ApplicationOutput() {
    }

    public ApplicationOutputPK getId() {
        return id;
    }

    public void setId(ApplicationOutputPK id) {
        this.id = id;
    }

    public String getAppArgument() {
        return appArgument;
    }

    public void setAppArgument(String appArgument) {
        this.appArgument = appArgument;
    }

    public short getDataMovement() {
        return dataMovement;
    }

    public void setDataMovement(short dataMovement) {
        this.dataMovement = dataMovement;
    }

    public String getDataNameLocation() {
        return dataNameLocation;
    }

    public void setDataNameLocation(String dataNameLocation) {
        this.dataNameLocation = dataNameLocation;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public short getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(short isRequired) {
        this.isRequired = isRequired;
    }

    public short getOutputStreaming() {
        return outputStreaming;
    }

    public void setOutputStreaming(short outputStreaming) {
        this.outputStreaming = outputStreaming;
    }

    public String getOutputValue() {
        return outputValue;
    }

    public void setOutputValue(String outputValue) {
        this.outputValue = outputValue;
    }

    public short getRequiredToCommandline() {
        return requiredToCommandline;
    }

    public void setRequiredToCommandline(short requiredToCommandline) {
        this.requiredToCommandline = requiredToCommandline;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }
}