from thrift.Thrift import TType
from rest_framework.serializers import CharField, BooleanField, DecimalField, IntegerField, Serializer, ListSerializer, \
    DictField, SerializerMetaclass
from django.utils import six
import copy

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


def create_serializer(thrift_model, **kwargs):
    class CustomSerializerMeta(SerializerMetaclass):

        def __new__(cls, name, bases, attrs):
            thrift_spec = thrift_model.thrift_spec
            for field in thrift_spec:
                if field:
                    field_serializer = process_field(field)
                    attrs[field[2]] = field_serializer
            return super().__new__(cls, name, bases, attrs)

    @six.add_metaclass(CustomSerializerMeta)
    class CustomSerializer(Serializer):

        def process_list_fields(self, validated_data):
            fields = self.fields
            params = copy.deepcopy(validated_data)
            for field_name, serializer in fields.items():
                if isinstance(serializer, ListSerializer):
                    params[field_name] = serializer.create(params[field_name])
            return params

        def create(self, validated_data):
            params = self.process_list_fields(validated_data)
            return thrift_model(**params)

        def update(self, instance, validated_data):
            raise Exception("Not implemented")

    return CustomSerializer(**kwargs)


def process_field(field):
    if field[1] in mapping:
        return mapping[field[1]]()
    elif field[1] == TType.LIST:
        list_field_serializer = process_list_field(field)
        return ListSerializer(child=list_field_serializer)
    elif field[1] == TType.STRUCT:
        return create_serializer(field[3][0])


def process_list_field(field):
    list_details = field[3]
    if list_details[0] in mapping:
        return mapping[list_details[0]]()
    elif list_details[0] == TType.STRUCT:
        return create_serializer(list_details[1][0])
