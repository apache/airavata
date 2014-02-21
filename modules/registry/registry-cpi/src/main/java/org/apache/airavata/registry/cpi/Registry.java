package org.apache.airavata.registry.cpi;

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
     * @return return the identifier to identify the object
     */
    public Object add(ParentDataType dataType, Object newObjectToAdd) throws Exception ;

    /**
     * This method is to add an object in to the registry
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param newObjectToAdd Object which contains the fields that need to be saved in to registry. This object is a
     *                       thrift model object. In experiment case this object can be BasicMetadata, ConfigurationData
     *                       etc
     * @param dependentIdentifiers contains the identifier if the object that is going to add is not a top
     *                            level object in the data model. This object can be a simple string or a
     *                             org.apache.airavata.registry.cpi.CompositeIdentifier type if it is a child element
     *                             with multiple identifiers
     * @return return the identifier to identify the object
     */
    public Object add(ChildDataType dataType, Object newObjectToAdd, Object dependentIdentifiers) throws Exception;

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
    public void update(DataType dataType, Object newObjectToUpdate, Object identifier) throws Exception;


    /**
     * This method is to update a specific field of the data model
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param identifier Identifier which will uniquely identify the data model. For example, in Experiment_Basic_Type,
     *                   identifier will be generated experimentID
     * @param fieldName Field which need to be updated in the registry. In Experiment_Basic_Type, if you want to update the
     *              description, field will be "description". Field names are defined in
     *              org.apache.airavata.registry.cpi.utils.Constants
     * @param value Value by which the given field need to be updated. If the field is "description", that field will be
     *              updated by given value
     */
    public void update(DataType dataType, Object identifier, String fieldName, Object value) throws Exception;

    /**
     * This method is to retrieve object according to the identifier. In the experiment basic data type, if you give the
     * experiment id, this method will return the BasicMetadata object
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param identifier Identifier which will uniquely identify the data model. For example, in Experiment_Basic_Type,
     *                   identifier will be generated experimentID
     * @return object according to the given identifier.
     */
    public Object get(DataType dataType, Object identifier) throws Exception;

    /**
     * This method is to retrieve list of objects according to a given criteria
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param fieldName FieldName is the field that filtering should be done. For example, if we want to retrieve all
     *                   the experiments for a given user, filterBy will be "userName"
     * @param value value for the filtering field. In the experiment case, value for "userName" can be "admin"
     * @return List of objects according to the given criteria
     */
    public List<Object> get(DataType dataType, String fieldName, Object value) throws Exception;

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
    public Object getValue (DataType dataType, Object identifier, String field) throws Exception;

    /**
     * This method is to retrieve all the identifiers according to given filtering criteria. For an example, if you want
     * to get all the experiment ids for a given gateway, your field name will be "gateway" and the value will be the
     * name of the gateway ("default"). Similar manner you can retrieve all the experiment ids for a given user.
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param fieldName FieldName is the field that filtering should be done. For example, if we want to retrieve all
     *                the experiments for a given user, filterBy will be "userName"
     * @param value value for the filtering field. In the experiment case, value for "userName" can be "admin"
     * @return id list according to the filtering criteria
     */
    public List<String> getIds (DataType dataType, String fieldName, Object value) throws Exception;

    /**
     * This method is to remove a item from the registry
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param identifier Identifier which will uniquely identify the data model. For example, in Experiment_Basic_Type,
     *                   identifier will be generated experimentID
     */
    public void remove (DataType dataType, Object identifier) throws Exception;

    /**
     * This method will check whether a given data type which can be identified with the identifier exists or not
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param identifier Identifier which will uniquely identify the data model. For example, in Experiment_Basic_Type,
     *                   identifier will be generated experimentID
     * @return whether the given data type exists or not
     */
    public boolean isExist(DataType dataType, Object identifier) throws Exception;


}
