from rest_framework import serializers


class FilenamePatternSerializer(serializers.Serializer):
    pattern = serializers.CharField()


class IncludeFilenamePatternSerializer(FilenamePatternSerializer):
    rename = serializers.CharField(
        required=False,
        help_text="""
            New name of matching file. Can be a pattern where $root is the original
            filename without the extension and $ext is the extension including the
            leading period.
        """)


class ExperimentDownloadSerializer(serializers.Serializer):
    experiment_id = serializers.CharField()
    path = serializers.CharField(default="")
    includes = IncludeFilenamePatternSerializer(many=True, required=False, default=None)
    excludes = FilenamePatternSerializer(many=True, required=False, default=None)


class MultiExperimentDownloadSerializer(serializers.Serializer):
    experiments = ExperimentDownloadSerializer(many=True)
    filename = serializers.CharField(default=None)
