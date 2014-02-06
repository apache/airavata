package org.apache.airavata.registry.cpi;

import javax.xml.crypto.Data;
import java.util.List;

/**
 * This is the interface for Registry CPI
 */
public interface Registry {

    /**
     * This method is to add an object in to the registry
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param newObjectToAdd Object which contains the fields that need to be saved in to registry. This object is a
     *                       thrift model object. In experiment case this object can be BasicMetadata, ConfigurationData
     *                       etc
     */
    public void add(TopLevelDataType dataType, Object newObjectToAdd);

    /**
     * This method is to add an object in to the registry
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param newObjectToAdd Object which contains the fields that need to be saved in to registry. This object is a
     *                       thrift model object. In experiment case this object can be BasicMetadata, ConfigurationData
     *                       etc
     * @param dependentIdentifier Object which contains the identifier if the object that is going to add is not a top
     *                            level object in the data model. If it is a top level object, programmer can pass it as
     *                            null
     */
    public void add(DependentDataType dataType, Object newObjectToAdd, Object dependentIdentifier);

    /**
     * This method is to update the whole object in registry
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param newObjectToUpdate Object which contains the fields that need to be updated in to registry. This object is a
     *                       thrift model object. In experiment case this object can be BasicMetadata, ConfigurationData
     *                       etc. CPI programmer can only fill necessary fields that need to be updated. He does not
     *                       have to fill the whole object. He needs to only fill the mandatory fields and whatever the
     *                       other fields that need to be updated.
     */
    public void update(TopLevelDataType dataType, Object newObjectToUpdate);

    /**
     * This method is to update the whole object in registry
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param newObjectToUpdate Object which contains the fields that need to be updated in to registry. This object is a
     *                       thrift model object. In experiment case this object can be BasicMetadata, ConfigurationData
     *                       etc. CPI programmer can only fill necessary fields that need to be updated. He does not
     *                       have to fill the whole object. He needs to only fill the mandatory fields and whatever the
     *                       other fields that need to be updated.
     * @param dependentIdentifier Object which contains the identifier if the object that is going to add is not a top
     *                            level object in the data model. If it is a top level object, programmer can pass it as
     *                            null
     */
    public void update(DependentDataType dataType, Object newObjectToUpdate, Object dependentIdentifier);

    /**
     * This method is to update a specific field of the data model
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param identifier Identifier which will uniquely identify the data model. For example, in Experiment_Basic_Type,
     *                   identifier will be generated experimentID
     * @param field Field which need to be updated in the registry. In Experiment_Basic_Type, if you want to update the
     *              description, field will be "description"
     * @param value Value by which the given field need to be updated. If the field is "description", that field will be
     *              updated by given value
     */
    public void update(DataType dataType, Object identifier, Object field, Object value);

    /**
     * This method is to retrieve list of objects according to a given criteria
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param filteredBy FilterBy is the field that filtering should be done. For example, if we want to retrieve all
     *                   the experiments for a given user, filterBy will be "userName"
     * @param value value for the filtering field. In the experiment case, value for "userName" can be "admin"
     * @return List of objects according to the given criteria
     */
    public List<Object> get(DataType dataType, Object filteredBy, Object value);

    /**
     * This method is to retrieve a specific value for a given field.
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param identifier Identifier which will uniquely identify the data model. For example, in Experiment_Basic_Type,
     *                   identifier will be generated experimentID
     * @param field field that filtering should be done. For example, if we want to execution user for a given
     *              experiment, field will be "userName"
     * @return return the value for the specific field where data model is identified by the unique identifier that has
     *         given
     */
    public Object getValue (DataType dataType, Object identifier, Object field);

    /**
     * This method is to remove a item from the registry
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param identifier Identifier which will uniquely identify the data model. For example, in Experiment_Basic_Type,
     *                   identifier will be generated experimentID
     */
    public void remove (DataType dataType, Object identifier);

    /**
     * This method will check whether a given data type which can be identified with the identifier exists or not
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param identifier Identifier which will uniquely identify the data model. For example, in Experiment_Basic_Type,
     *                   identifier will be generated experimentID
     * @return whether the given data type exists or not
     */
    public boolean isExist(DataType dataType, Object identifier);


}
