import datetime
import os
import shutil
import tarfile
import tempfile
from pathlib import Path
from typing import Iterator

from django.conf import settings
from django.core.management.base import BaseCommand, CommandError
from django.db import transaction
from django.utils import timezone

from django_airavata.apps.admin import models


class Command(BaseCommand):
    help = "Create an archive of user data directories and optionally clean them up"

    def add_arguments(self, parser):
        parser.add_argument('--dry-run',
                            action='store_true',
                            help="Print the list of files/directories that would be archived then exit",
                            default=False)

    def handle(self, *args, **options):
        max_age_setting = getattr(settings, "GATEWAY_USER_DATA_ARCHIVE_MAX_AGE", None)
        if max_age_setting is None:
            raise CommandError("Setting GATEWAY_USER_DATA_ARCHIVE_MAX_AGE is not configured")

        max_age = timezone.now() - datetime.timedelta(**max_age_setting)
        entries_to_archive = self.get_archive_entries(older_than=max_age)
        gateway_id = settings.GATEWAY_ID

        archive_directory = Path(settings.GATEWAY_USER_DATA_ARCHIVE_DIRECTORY)
        archive_directory.mkdir(exist_ok=True)

        with tempfile.TemporaryDirectory(dir=archive_directory) as tmpdir:
            archive_basename = f"archive_{gateway_id}_older_than_{max_age.strftime('%Y-%m-%d-%H-%M-%S')}"
            archive_list_filename = f"{archive_basename}.txt"
            archive_list_filepath = os.path.join(tmpdir, archive_list_filename)
            with open(archive_list_filepath, "wt") as archive_list_file:
                for entry in entries_to_archive:
                    archive_list_file.write(f"{entry.path}\n")

            # if dry run, just print file and exit
            if options['dry_run']:
                self.stdout.write(f"DRY RUN: printing {archive_list_filename}, then exiting")
                with open(os.path.join(tmpdir, archive_list_filename)) as archive_list_file:
                    for line in archive_list_file:
                        self.stdout.write(line)
                self.stdout.write(self.style.SUCCESS("DRY RUN: exiting now"))
                return

            # otherwise, generate a tarball in tmpdir
            archive_tarball_filename = f"{archive_basename}.tgz"
            archive_tarball_filepath = os.path.join(tmpdir, archive_tarball_filename)
            with tarfile.open(archive_tarball_filepath, "w:gz") as tarball:
                with open(os.path.join(tmpdir, archive_list_filename)) as archive_list_file:
                    for line in archive_list_file:
                        tarball.add(line.strip())

            minimum_bytes_size = settings.GATEWAY_USER_DATA_ARCHIVE_MINIMUM_ARCHIVE_SIZE_GB * 1024 ** 3
            if os.stat(archive_tarball_filepath).st_size < minimum_bytes_size:
                self.stdout.write(self.style.WARNING("Aborting, archive size is not large enough to proceed (size less than GATEWAY_USER_DATA_ARCHIVE_MINIMUM_ARCHIVE_SIZE_GB)"))
                # Exit early
                return

            self.stdout.write(self.style.SUCCESS(f"Created tarball: {archive_tarball_filename}"))

            # Move the archive files into the final destination
            shutil.move(archive_list_filepath, archive_directory / archive_list_filename)
            shutil.move(archive_tarball_filepath, archive_directory / archive_tarball_filename)

        with transaction.atomic():
            user_data_archive = models.UserDataArchive(
                archive_name=archive_tarball_filename,
                archive_path=os.fspath(archive_directory / archive_tarball_filename),
                max_modification_time=max_age)
            user_data_archive.save()
            # delete archived entries
            with open(archive_directory / archive_list_filename) as archive_list_file:
                for archive_path in archive_list_file:
                    archive_path = archive_path.strip()
                    if os.path.isfile(archive_path):
                        os.remove(archive_path)
                    else:
                        shutil.rmtree(archive_path)
                    archive_entry = models.UserDataArchiveEntry(user_data_archive=user_data_archive, entry_path=archive_path)
                    archive_entry.save()

        self.stdout.write(self.style.SUCCESS("Successfully removed archived user data"))

    def get_archive_entries(self, older_than: datetime.datetime) -> Iterator[os.DirEntry]:

        GATEWAY_USER_DIR = settings.USER_STORAGES['default']['OPTIONS']['directory']

        with os.scandir(GATEWAY_USER_DIR) as user_dirs:
            for user_dir_entry in user_dirs:
                # Skip over any files (shouldn't be any but who knows)
                if not user_dir_entry.is_dir():
                    continue
                # Skip over shared directories
                if self._is_shared_directory(user_dir_entry):
                    continue
                with os.scandir(user_dir_entry.path) as project_dirs:
                    for project_dir_entry in project_dirs:
                        yield from self._scan_project_dir_for_archive_entries(
                            project_dir_entry=project_dir_entry,
                            older_than=older_than)

    def _scan_project_dir_for_archive_entries(self, project_dir_entry: os.DirEntry, older_than: datetime.datetime) -> Iterator[os.DirEntry]:
        # archive files here but not directories
        if project_dir_entry.is_file() and project_dir_entry.stat().st_mtime < older_than.timestamp():
            yield project_dir_entry
        # Skip over shared directories
        if project_dir_entry.is_dir() and not self._is_shared_directory(project_dir_entry):
            with os.scandir(project_dir_entry.path) as experiment_dirs:
                for experiment_dir_entry in experiment_dirs:
                    if experiment_dir_entry.stat().st_mtime < older_than.timestamp():
                        yield experiment_dir_entry

    def _is_shared_directory(self, dir_entry: os.DirEntry) -> bool:
        if not dir_entry.is_dir():
            return False
        shared_dirs = getattr(settings, "GATEWAY_DATA_SHARED_DIRECTORIES", {})
        for shared_dir in shared_dirs.values():
            if os.path.samefile(dir_entry.path, shared_dir["path"]):
                return True
        return False
