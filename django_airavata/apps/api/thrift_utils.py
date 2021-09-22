"""
Used to create Django Rest Framework serializers for Apache Thrift Data Types
"""
import copy
import datetime
import logging

from rest_framework.serializers import (
    BooleanField,
    CharField,
    DateTimeField,
    DecimalField,
    DictField,
    Field,
    IntegerField,
    ListField,
    ListSerializer,
    Serializer,
    SerializerMetaclass,
    ValidationError
)
from thrift.Thrift import TType

logger = logging.getLogger(__name__)

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


class UTCPosixTimestampDateTimeField(DateTimeField):

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.default = self.current_time_ms
        self.initial = self.initial_value
        self.required = False

    def to_representation(self, obj):
        # Create datetime instance from milliseconds that is aware of timezon
        dt = datetime.datetime.fromtimestamp(obj / 1000, datetime.timezone.utc)
        return super().to_representation(dt)

    def to_internal_value(self, data):
        dt = super().to_internal_value(data)
        return int(dt.timestamp() * 1000)

    def initial_value(self):
        return self.to_representation(self.current_time_ms())

    def current_time_ms(self):
        return int(datetime.datetime.utcnow().timestamp() * 1000)


class ThriftEnumField(Field):

    def __init__(self, enumClass, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.enumClass = enumClass

    def to_representation(self, obj):
        if obj is None:
            return None
        return self.enumClass._VALUES_TO_NAMES[obj]

    def to_internal_value(self, data):
        if self.allow_null and data is None:
            return None
        if data not in self.enumClass._NAMES_TO_VALUES:
            raise ValidationError(
                "Not an allowed name of enum {}".format(
                    self.enumClass.__name__))
        return self.enumClass._NAMES_TO_VALUES.get(data, None)


def create_serializer(thrift_data_type, enable_date_time_conversion=False, **kwargs):
    """
    Create django rest framework serializer based on the thrift data type
    :param thrift_data_type: Thrift data type
    :param kwargs: Other Django Framework Serializer initialization parameters
    :param enable_date_time_conversion: enable conversion of field with name ending with time to
            UTCPosixTimestampDateTimeField instead of IntegerField
    :return: instance of custom serializer for the given thrift data type
    """
    return create_serializer_class(thrift_data_type, enable_date_time_conversion)(**kwargs)


def create_serializer_class(thrift_data_type, enable_date_time_conversion=False):
    class CustomSerializerMeta(SerializerMetaclass):

        def __new__(cls, name, bases, attrs):
            meta = attrs.get('Meta', None)
            thrift_spec = thrift_data_type.thrift_spec
            for field in thrift_spec:
                # Don't replace existing attrs to allow subclasses to override
                if field and field[2] not in attrs:
                    required = (field[2] in meta.required
                                if meta and hasattr(meta, 'required')
                                else False)
                    read_only = (field[2] in meta.read_only
                                 if meta and hasattr(meta, 'read_only')
                                 else False)
                    allow_null = not required
                    field_serializer = process_field(
                        field, enable_date_time_conversion, required=required, read_only=read_only,
                        allow_null=allow_null)
                    attrs[field[2]] = field_serializer
            return super().__new__(cls, name, bases, attrs)

    class CustomSerializer(Serializer, metaclass=CustomSerializerMeta):
        """
        Custom Serializer which handle the list fields which holds custom class objects
        """

        def process_nested_fields(self, validated_data):
            fields = self.fields
            params = copy.deepcopy(validated_data)
            for field_name, serializer in fields.items():
                if (isinstance(serializer, ListField) or
                        isinstance(serializer, ListSerializer)):
                    if (params[field_name] is not None or
                            not serializer.allow_null):
                        if isinstance(serializer.child, Serializer):
                            params[field_name] = [serializer.child.create(
                                item) for item in params[field_name]]
                        else:
                            params[field_name] = serializer.to_representation(
                                params[field_name])
                elif isinstance(serializer, Serializer):
                    if field_name in params and params[field_name] is not None:
                        params[field_name] = serializer.create(
                            params[field_name])
            return params

        def create(self, validated_data):
            params = self.process_nested_fields(validated_data)
            return thrift_data_type(**params)

        def update(self, instance, validated_data):
            return self.create(validated_data)

    return CustomSerializer


def process_field(field, enable_date_time_conversion, required=False, read_only=False, allow_null=False):
    """
    Used to process a thrift data type field
    :param field:
    :param required:
    :param read_only:
    :param allow_null:
    :return:
    """
    if field[1] in mapping:
        # handling scenarios when the thrift field type is present in the
        # mapping
        field_class = mapping[field[1]]
        kwargs = dict(required=required, read_only=read_only)
        # allow_null isn't allowed for BooleanField
        if field_class not in (BooleanField,):
            kwargs['allow_null'] = allow_null
        # allow_null CharField are also allowed to be blank
        if field_class == CharField:
            kwargs['allow_blank'] = allow_null
        thrift_model_class = mapping[field[1]]
        if enable_date_time_conversion and thrift_model_class == IntegerField and field[2].lower().endswith("time"):
            thrift_model_class = UTCPosixTimestampDateTimeField
        return thrift_model_class(**kwargs)
    elif field[1] == TType.LIST:
        # handling scenario when the thrift field type is list
        list_field_serializer = process_list_field(field)
        return ListField(child=list_field_serializer,
                         required=required,
                         read_only=read_only,
                         allow_null=allow_null)
    elif field[1] == TType.STRUCT:
        # handling scenario when the thrift field type is struct
        return create_serializer(field[3][0],
                                 required=required,
                                 read_only=read_only,
                                 allow_null=allow_null)


def process_list_field(field):
    """
    Used to process thrift list type field
    :param field:
    :return:
    """
    list_details = field[3]
    if list_details[0] in mapping:
        # handling scenario when the data type hold by the list is in the
        # mapping
        return mapping[list_details[0]]()
    elif list_details[0] == TType.STRUCT:
        # handling scenario when the data type hold by the list is a struct
        return create_serializer(list_details[1][0])
