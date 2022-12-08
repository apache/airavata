import io
import zipfile
from unittest.mock import ANY, call, patch

from django.test import TestCase
from rest_framework.test import APIRequestFactory

from airavata_django_portal_sdk import views


class DownloadDirTestCase(TestCase):

    @patch("airavata_django_portal_sdk.views.user_storage")
    def test_download_dir_empty_dir(self, user_storage):

        factory = APIRequestFactory()
        request = factory.get("/download-dir/")
        self.assertNotIn('path', request.GET)

        user_storage.listdir.return_value = ([], [])

        response = views.download_dir(request)

        user_storage.listdir.assert_called_once()
        self.assertEqual("", user_storage.listdir.call_args.args[1], "called with empty path")

        self.assertEqual("attachment; filename=home.zip", response['Content-Disposition'])
        self.assertEqual("application/zip", response['Content-Type'])
        zf = zipfile.ZipFile(io.BytesIO(response.getvalue()))
        self.assertSequenceEqual([], zf.namelist())

    @patch("airavata_django_portal_sdk.views.user_storage")
    def test_download_dir_files_only(self, user_storage):

        factory = APIRequestFactory()
        request = factory.get("/download-dir/")
        self.assertNotIn('path', request.GET)

        user_storage.listdir.return_value = ([], [{"name": "file1", "data-product-uri": "dp1"},
                                                  {"name": "file2", "data-product-uri": "dp2"},
                                                  {"name": "file3", "data-product-uri": "dp3"}])

        user_storage.open_file.side_effect = self._mock_open_file({
            "dp1": ("file1", b''),
            "dp2": ("file2", b'file2 content'),
            "dp3": ("file3", b'file3 content'),
        })

        response = views.download_dir(request)

        user_storage.listdir.assert_called_once()
        self.assertEqual("", user_storage.listdir.call_args.args[1], "called with empty path")

        self.assertEqual("attachment; filename=home.zip", response['Content-Disposition'])
        self.assertEqual("application/zip", response['Content-Type'])
        zf = zipfile.ZipFile(io.BytesIO(response.getvalue()))
        self.assertSequenceEqual(["file1", "file2", "file3"], zf.namelist())
        self.assertEqual(b'', zf.read("file1"))
        self.assertEqual(b'file2 content', zf.read("file2"))
        self.assertEqual(b'file3 content', zf.read("file3"))

    @ patch("airavata_django_portal_sdk.views.user_storage")
    def test_download_dir_files_and_subdirectories(self, user_storage):

        factory = APIRequestFactory()
        request = factory.get("/download-dir/")
        self.assertNotIn('path', request.GET)

        def listdir(request, path, *args, **kwargs):
            if path == "":
                return [{"name": "dir1"}, {"name": "dir2"}], [{"name": "file1", "data-product-uri": "dp"},
                                                              {"name": "file2", "data-product-uri": "dp"},
                                                              {"name": "file3", "data-product-uri": "dp"}]
            if path == "dir1":
                return [{"name": "dir3"}], [{"name": "dir1file1", "data-product-uri": "dp"},
                                            {"name": "dir1file2", "data-product-uri": "dp"}]
            elif path == "dir2":
                return [{"name": "dir4"}], [{"name": "dir2file1", "data-product-uri": "dp"},
                                            {"name": "dir2file2", "data-product-uri": "dp"}]
            elif path == "dir1/dir3":
                # empty dir
                return [], []
            elif path == "dir2/dir4":
                return [], [{"name": "dir4file1", "data-product-uri": "dp"}]
        user_storage.listdir.side_effect = listdir
        user_storage.open_file.side_effect = self._mock_open_file({"dp": ('file', b'')})

        response = views.download_dir(request)

        user_storage.listdir.assert_has_calls([call(ANY, ""),
                                               call(ANY, "dir1"),
                                               call(ANY, "dir2"),
                                               call(ANY, "dir1/dir3"),
                                               call(ANY, "dir2/dir4")], any_order=True)

        self.assertEqual("attachment; filename=home.zip", response['Content-Disposition'])
        self.assertEqual("application/zip", response['Content-Type'])

        zf = zipfile.ZipFile(io.BytesIO(response.getvalue()))
        self.assertSequenceEqual(["file1",
                                  "file2",
                                  "file3",
                                  "dir1/dir1file1",
                                  "dir1/dir1file2",
                                  "dir2/dir2file1",
                                  "dir2/dir2file2",
                                  "dir2/dir4/dir4file1"],
                                 zf.namelist())

    def _mock_open_file(self, dps: dict):
        def open_file(request, data_product_uri, *args, **kwargs):
            name, content = dps.get(data_product_uri, ('', b''))
            empty_file = io.BytesIO(content)
            empty_file.name = name
            return empty_file
        return open_file
