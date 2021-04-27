import io
import os
import tempfile
import uuid
from unittest.mock import MagicMock
from urllib.parse import urlparse

from airavata.model.data.replica.ttypes import (
    DataProductModel,
    DataProductType,
    DataReplicaLocationModel,
    ReplicaLocationCategory
)
from airavata.model.security.ttypes import AuthzToken
from django.contrib.auth.models import User
from django.test import RequestFactory, TestCase, override_settings

from airavata_django_portal_sdk import user_storage

GATEWAY_ID = 'test-gateway'


@override_settings(GATEWAY_ID=GATEWAY_ID)
class BaseTestCase(TestCase):

    def setUp(self):
        self.user = User.objects.create_user('testuser')
        self.factory = RequestFactory()
        # Dummy POST request
        self.request = self.factory.post('/upload', {})
        self.request.user = self.user
        self.request.airavata_client = MagicMock(name="airavata_client")
        self.product_uri = f"airavata-dp://{uuid.uuid4()}"
        self.request.airavata_client.registerDataProduct.return_value = \
            self.product_uri
        self.request.authz_token = AuthzToken(accessToken="dummy",
                                              claimsMap={'gatewayID': GATEWAY_ID,
                                                         'userName': self.user.username})


class SaveTests(BaseTestCase):

    def test_save_with_defaults(self):
        "Test save with default name and content type"
        with tempfile.TemporaryDirectory() as tmpdirname, \
                self.settings(GATEWAY_DATA_STORE_DIR=tmpdirname,
                              GATEWAY_DATA_STORE_HOSTNAME="gateway.com"):
            # path is just the user directory in gateway storage
            path = os.path.join(tmpdirname, self.user.username)
            file = io.StringIO("Foo file")
            file.name = "foo.txt"
            data_product = user_storage.save(self.request, path, file)

            self.assertEqual(data_product.productUri, self.product_uri)
            self.request.airavata_client.registerDataProduct.\
                assert_called_once()
            args, kws = self.request.airavata_client.registerDataProduct.\
                call_args
            dp = args[1]
            self.assertEqual(self.user.username, dp.ownerName)
            self.assertEqual("foo.txt", dp.productName)
            self.assertEqual(DataProductType.FILE, dp.dataProductType)
            self.assertDictEqual({'mime-type': 'text/plain'},
                                 dp.productMetadata)
            self.assertEqual(1, len(dp.replicaLocations))
            self.assertEqual(f"{path}/{file.name}",
                             dp.replicaLocations[0].filePath)

    def test_save_with_name_and_content_type(self):
        "Test save with specified name and content type"
        # TODO: either change _Datastore to be created anew each time
        #       or create the _Datastore manually with the tmpdirname
        with tempfile.TemporaryDirectory() as tmpdirname, \
                self.settings(GATEWAY_DATA_STORE_DIR=tmpdirname,
                              GATEWAY_DATA_STORE_HOSTNAME="gateway.com"):
            # path is just the user directory in gateway storage
            path = os.path.join(tmpdirname, self.user.username)
            file = io.StringIO("Foo file")
            file.name = "foo.txt"
            data_product = user_storage.save(
                self.request, path, file, name="bar.txt",
                content_type="application/some-app")

            self.assertEqual(data_product.productUri, self.product_uri)
            self.request.airavata_client.registerDataProduct.\
                assert_called_once()
            args, kws = self.request.airavata_client.registerDataProduct.\
                call_args
            dp = args[1]
            self.assertEqual(self.user.username, dp.ownerName)
            self.assertEqual("bar.txt", dp.productName)
            self.assertEqual(DataProductType.FILE, dp.dataProductType)
            self.assertDictEqual({'mime-type': 'application/some-app'},
                                 dp.productMetadata)
            self.assertEqual(1, len(dp.replicaLocations))
            self.assertEqual(f"{path}/bar.txt",
                             dp.replicaLocations[0].filePath)

    def test_save_with_unknown_text_file_type(self):
        "Test save with unknown file ext for text file"
        with tempfile.TemporaryDirectory() as tmpdirname, \
                self.settings(GATEWAY_DATA_STORE_DIR=tmpdirname,
                              GATEWAY_DATA_STORE_HOSTNAME="gateway.com"):
            path = os.path.join(
                tmpdirname, "foo.someext")
            os.makedirs(os.path.dirname(path), exist_ok=True)
            with open(path, 'w') as f:
                f.write("Some Unicode text")
            with open(path, 'r') as f:
                dp = user_storage.save(
                    self.request, "some/path", f,
                    content_type="application/octet-stream")
                # Make sure that the file contents are tested to see if text
                self.assertDictEqual({'mime-type': 'text/plain'},
                                     dp.productMetadata)

    def test_save_with_unknown_binary_file_type(self):
        "Test save with unknown file ext for binary file"
        with tempfile.TemporaryDirectory() as tmpdirname, \
                self.settings(GATEWAY_DATA_STORE_DIR=tmpdirname,
                              GATEWAY_DATA_STORE_HOSTNAME="gateway.com"):
            path = os.path.join(
                tmpdirname, "foo.someext")
            os.makedirs(os.path.dirname(path), exist_ok=True)
            with open(path, 'wb') as f:
                f.write(bytes(range(256)))
            with open(path, 'rb') as f:
                dp = user_storage.save(
                    self.request, "some/path", f,
                    content_type="application/octet-stream")
                # Make sure that DID NOT determine file contents are text
                self.assertDictEqual({'mime-type': 'application/octet-stream'},
                                     dp.productMetadata)


class CopyInputFileUploadTests(BaseTestCase):
    def test_copy_input_file_upload(self):
        "Test copy input file upload copies data product"
        with tempfile.TemporaryDirectory() as tmpdirname, \
                self.settings(GATEWAY_DATA_STORE_DIR=tmpdirname,
                              GATEWAY_DATA_STORE_HOSTNAME="gateway.com"):
            # path is just the user directory in gateway storage
            source_path = os.path.join(
                tmpdirname, self.user.username, "foo.ext")
            os.makedirs(os.path.dirname(source_path))
            with open(source_path, 'wb') as f:
                f.write(b"123")

            data_product = DataProductModel()
            data_product.productUri = f"airavata-dp://{uuid.uuid4()}"
            data_product.gatewayId = GATEWAY_ID
            data_product.ownerName = self.user.username
            data_product.productName = "foo.ext"
            data_product.dataProductType = DataProductType.FILE
            data_product.productMetadata = {
                'mime-type': 'application/some-app'
            }
            replica_category = ReplicaLocationCategory.GATEWAY_DATA_STORE
            replica_path = f"file://gateway.com:{source_path}"
            data_product.replicaLocations = [
                DataReplicaLocationModel(
                    filePath=replica_path,
                    replicaLocationCategory=replica_category)]

            data_product_copy = user_storage.copy_input_file(
                self.request, data_product)

            self.request.airavata_client.registerDataProduct.\
                assert_called_once()
            self.assertIsNot(data_product_copy, data_product)
            self.assertNotEqual(data_product_copy.productUri,
                                data_product.productUri)
            self.assertDictEqual(data_product_copy.productMetadata,
                                 data_product.productMetadata)
            self.assertEqual(data_product_copy.productName,
                             data_product.productName)
            self.assertEqual(data_product_copy.dataProductType,
                             data_product.dataProductType)
            replica_copy_path = data_product_copy.replicaLocations[0].filePath
            self.assertNotEqual(replica_copy_path, replica_path)
            replica_copy_filepath = urlparse(replica_copy_path).path
            self.assertEqual(
                os.path.dirname(replica_copy_filepath),
                os.path.join(tmpdirname, self.user.username, "tmp"),
                msg="Verify input file copied to user's tmp dir")


class ListDirTests(BaseTestCase):
    def test_listdir(self):
        "Verify basic listdir functionality"
        with tempfile.TemporaryDirectory() as tmpdirname, \
                self.settings(GATEWAY_DATA_STORE_DIR=tmpdirname,
                              GATEWAY_DATA_STORE_HOSTNAME="gateway.com"):
            # create test file
            test_file_path = os.path.join(
                tmpdirname, self.user.username, "foo.ext")
            os.makedirs(os.path.dirname(test_file_path))
            with open(test_file_path, 'wb') as f:
                f.write(b"123")
            # create test directory
            os.makedirs(os.path.join(tmpdirname, self.user.username, "testdir"))

            dirs, files = user_storage.listdir(self.request, "")
            self.assertEqual(len(dirs), 1)
            self.assertEqual("testdir", dirs[0]["name"])
            self.assertEqual(len(files), 1)
            self.assertEqual("foo.ext", files[0]["name"])

    def test_listdir_broken_symlink(self):
        "Test that broken symlinks are ignored"
        with tempfile.TemporaryDirectory() as tmpdirname, \
                self.settings(GATEWAY_DATA_STORE_DIR=tmpdirname,
                              GATEWAY_DATA_STORE_HOSTNAME="gateway.com"):
            # create test file
            test_file_path = os.path.join(
                tmpdirname, self.user.username, "foo.ext")
            os.makedirs(os.path.dirname(test_file_path))
            with open(test_file_path, 'wb') as f:
                f.write(b"123")
            # create test directory
            os.makedirs(os.path.join(tmpdirname, self.user.username, "testdir"))
            # create broken symlink
            not_existent = os.path.join("path", "does", "not", "exist")
            self.assertFalse(os.path.exists(not_existent))
            os.symlink(not_existent, os.path.join(tmpdirname, self.user.username, "broken-symlink"))
            self.assertIn("broken-symlink", os.listdir(os.path.join(tmpdirname, self.user.username)))
            # os.path.exists returns False for broken symbolic link
            self.assertFalse(os.path.exists(os.path.join(tmpdirname, self.user.username, "broken-symlink")))

            dirs, files = user_storage.listdir(self.request, "")
            # verify test file and directory are returned, but not broken-symlink
            self.assertEqual(len(dirs), 1)
            self.assertEqual("testdir", dirs[0]["name"])
            self.assertEqual(len(files), 1)
            self.assertEqual("foo.ext", files[0]["name"])
        pass
