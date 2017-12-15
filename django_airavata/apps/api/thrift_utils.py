"""
Used to create Django Rest Framework serializers for Apache Thrift Data Types
"""
from thrift.Thrift import TType
from rest_framework.serializers import CharField, BooleanField, DecimalField, IntegerField, Serializer, \
    DictField, SerializerMetaclass, ListField
from django.utils import six
import copy

# used to map apache thrift data types to django serializer fields
mapping = {
    TType.STRING: CharField,
    TType.I08: IntegerField,
    TType.I16: IntegerField,
    TType.I32: IntegerField,
    TType.I64: IntegerField,
    TType.DOUBLE: DecimalField,
    TType.BOOL: BooleanField,
    TType.MAP: DictField
}


def create_serializer(thrift_data_type, **kwargs):
    """
    Create django rest framework serializer based on the thrift data type
    :param thrift_data_type: Thrift data type
    :param kwargs: Other Django Framework Serializer initialization parameters
    :return: instance of custom serializer for the given thrift data type
    """
    return create_serializer_class(thrift_data_type)(**kwargs)

def create_serializer_class(thrift_data_type):
    class CustomSerializerMeta(SerializerMetaclass):

        def __new__(cls, name, bases, attrs):
            thrift_spec = thrift_data_type.thrift_spec
            for field in thrift_spec:
                if field:
                    field_serializer = process_field(field)
                    attrs[field[2]] = field_serializer
            return super().__new__(cls, name, bases, attrs)

    @six.add_metaclass(CustomSerializerMeta)
    class CustomSerializer(Serializer):
        """
        Custom Serializer which handle the list fields which holds custom class objects
        """

        def process_list_fields(self, validated_data):
            fields = self.fields
            params = copy.deepcopy(validated_data)
            for field_name, serializer in fields.items():
                if isinstance(serializer, ListField):
                    params[field_name] = serializer.to_representation(params[field_name])
            return params

        def create(self, validated_data):
            params = self.process_list_fields(validated_data)
            return thrift_data_type(**params)

        def update(self, instance, validated_data):
            raise Exception("Not implemented")

    return CustomSerializer


def process_field(field):
    """
    Used to process a thrift data type field
    :param field:
    :return:
    """
    if field[1] in mapping:
        # handling scenarios when the thrift field type is present in the mapping
        return mapping[field[1]](required=False)
    elif field[1] == TType.LIST:
        # handling scenario when the thrift field type is list
        list_field_serializer = process_list_field(field)
        return ListField(child=list_field_serializer, required=False)
    elif field[1] == TType.STRUCT:
        # handling scenario when the thrift field type is struct
        return create_serializer(field[3][0])


def process_list_field(field):
    """
    Used to process thrift list type field
    :param field:
    :return:
    """
    list_details = field[3]
    if list_details[0] in mapping:
        # handling scenario when the data type hold by the list is in the mapping
        return mapping[list_details[0]]()
    elif list_details[0] == TType.STRUCT:
        # handling scenario when the data type hold by the list is a struct
        return create_serializer(list_details[1][0])
