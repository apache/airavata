from rest_framework import serializers

# class DownloadIncludeSerializer(serializers.Serializer):
#     pattern = serializers.CharField()


class ExperimentDownloadSerializer(serializers.Serializer):
    experiment_id = serializers.CharField()
    # includes = DownloadIncludeSerializer(many=True)


class MultiExperimentDownloadSerializer(serializers.Serializer):
    experiments = ExperimentDownloadSerializer(many=True)
    # TODO: filename parameter?
