
import os
import pathlib
import tarfile

from django.core.management.base import BaseCommand

from django_airavata.apps.admin import models


class Command(BaseCommand):
    help = "Unarchive a user data archive"

    def add_arguments(self, parser):
        parser.add_argument('archive_file',
                            help="Archive file (ending in .tgz) that was created by the archive_user_data")
        parser.add_argument('-m', '--reset-modification',
                            help="Set modification timestamp to current time for all entries in archive",
                            action="store_true")

    def handle(self, *args, **options):
        with tarfile.open(options["archive_file"]) as tf:
            tf.extractall(path="/")

        # mark archive as rolled back
        archive_name = os.path.basename(options["archive_file"])
        try:
            archive = models.UserDataArchive.objects.get(archive_name=archive_name)
            archive.rolled_back = True
            archive.save()
        except models.UserDataArchive.DoesNotExist:
            self.stdout.write(self.style.ERROR(f"Could not find UserDataArchive database record for {archive_name}"))

        if options["reset_modification"]:
            root = pathlib.Path('/')
            with tarfile.open(options["archive_file"]) as tf:
                for tarinfo in tf:
                    path = root / tarinfo.path
                    path.touch()
            self.stdout.write("Updated modification timestamps for all entries")

        self.stdout.write(self.style.SUCCESS(f"Successfully unarchived {options['archive_file']}"))
