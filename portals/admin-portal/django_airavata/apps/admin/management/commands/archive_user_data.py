import datetime
import logging
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

logger = logging.getLogger(__name__)


class Command(BaseCommand):
    help = "Create an archive of user data directories and optionally clean them up"

    def add_arguments(self, parser):
        parser.add_argument('--dry-run',
                            action='store_true',
                            help="Print the list of files/directories that would be archived then exit",
                            default=False)
        parser.add_argument('--max-age',
                            help="Max age of files/directories in days. Any that are older will be archived.",
                            type=int,
                            default=getattr(settings, "GATEWAY_USER_DATA_ARCHIVE_MAX_AGE_DAYS", None))

    def handle(self, *args, **options):
        try:
            # Take --max-age from the command line first, then from the setting
            max_age_setting = options['max_age']
            if max_age_setting is None:
                raise CommandError("Setting GATEWAY_USER_DATA_ARCHIVE_MAX_AGE_DAYS is not configured and --max-age option missing.")

            max_age = timezone.now() - datetime.timedelta(days=max_age_setting)
            entries_to_archive = self.get_archive_entries(older_than=max_age)
            gateway_id = settings.GATEWAY_ID

            archive_directory = Path(settings.GATEWAY_USER_DATA_ARCHIVE_DIRECTORY)
            archive_directory.mkdir(parents=True, exist_ok=True)

            with tempfile.TemporaryDirectory(dir=archive_directory) as tmpdir:
                archive_basename = f"archive_{gateway_id}_older_than_{max_age.strftime('%Y-%m-%d-%H-%M-%S')}"
                archive_list_filename = f"{archive_basename}.txt"
                archive_list_filepath = os.path.join(tmpdir, archive_list_filename)
                entry_count = 0
                with open(archive_list_filepath, "wt") as archive_list_file:
                    for entry in entries_to_archive:
                        entry_count = entry_count + 1
                        archive_list_file.write(f"{entry.path}\n")

                # If nothing matching to archive, just exit
                if entry_count == 0:
                    self.stdout.write(self.style.WARNING("Nothing to archive, exiting now"))
                    return

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

                # Move the archive files into the final destination
                shutil.move(archive_list_filepath, archive_directory / archive_list_filename)
                shutil.move(archive_tarball_filepath, archive_directory / archive_tarball_filename)

                self.stdout.write(self.style.SUCCESS(f"Created tarball: {archive_directory / archive_tarball_filename}"))

            # Now we'll remove any files/directories that were in the archive
            # and create database records for the archive
            try:
                # If any error occurs in this block, the transaction will be rolled back
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
                            elif os.path.isdir(archive_path):
                                shutil.rmtree(archive_path)
                            else:
                                self.stdout.write(self.style.WARNING(f"Cannot delete {archive_path} as it is neither a file nor directory, perhaps was already removed"))
                            archive_entry = models.UserDataArchiveEntry(user_data_archive=user_data_archive, entry_path=archive_path)
                            archive_entry.save()
            except Exception as e:
                self.stdout.write(self.style.ERROR("Failed while deleting archived data, attempting to roll back"))
                with tarfile.open(archive_directory / archive_tarball_filename) as tf:
                    tf.extractall(path="/")
                logger.exception(f"[archive_user_data] Failed to delete archived files, but unarchived from tarball {archive_directory / archive_tarball_filename}", exc_info=e)
                raise CommandError(f"Failed to delete archived files, but unarchived from tarball {archive_directory / archive_tarball_filename}") from e

            self.stdout.write(self.style.SUCCESS("Successfully removed archived user data"))
        except CommandError:
            raise
        except Exception:
            logger.exception("[archive_user_data] Failed to create user data archive")

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
