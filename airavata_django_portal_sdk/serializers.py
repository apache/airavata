from rest_framework import serializers


class FilenamePatternSerializer(serializers.Serializer):
    pattern = serializers.CharField()


class IncludeFilenamePatternSerializer(FilenamePatternSerializer):
    rename = serializers.CharField(required=False)


class ExperimentDownloadSerializer(serializers.Serializer):
    experiment_id = serializers.CharField()
    path = serializers.CharField(default="")
    includes = IncludeFilenamePatternSerializer(many=True, required=False, default=None)
    excludes = FilenamePatternSerializer(many=True, required=False, default=None)


class MultiExperimentDownloadSerializer(serializers.Serializer):
    experiments = ExperimentDownloadSerializer(many=True)
    # TODO: filename parameter?
