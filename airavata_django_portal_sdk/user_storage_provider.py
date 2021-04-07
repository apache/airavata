import logging
import os
from datetime import datetime

import grpc
from django.core.exceptions import ObjectDoesNotExist

from . import MFTApi_pb2, MFTApi_pb2_grpc
# from .user_storage import (TMP_INPUT_FILE_UPLOAD_DIR, _Datastore,
#                            _get_data_product_uri)

logger = logging.getLogger(__name__)


class UserStorageProvider:
    def __init__(self, authz_token, context=None, *args, **kwargs):
        self.authz_token = authz_token
        self.context = context

    def save(self, authz_token, path, file, name=None, content_type=None):
        raise NotImplementedError()

    def get_upload_url(self, authz_token, path):
        raise NotImplementedError()

    def open(self, authz_token, resource_id=None):
        raise NotImplementedError()

    def get_download_url(self, authz_token, resource_id=None):
        raise NotImplementedError()

    def exists(self, authz_token, resource_id=None):
        raise NotImplementedError()

    def is_file(self, authz_token, resource_id=None):
        # TODO: is this needed if we have get_metadata?
        raise NotImplementedError()

    def is_dir(self, authz_token, resource_id=None):
        # TODO: is this needed if we have get_metadata?
        raise NotImplementedError()

    def get_metadata(self, authz_token, resource_id=None):
        raise NotImplementedError()

    def delete(self, authz_token, resource_id=None):
        raise NotImplementedError()

    def update(self, authz_token, resource_id, file):
        raise NotImplementedError()


class FileSystemUserStorageProvider(UserStorageProvider):
    def listdir(self, request, path):
        datastore = _Datastore()
        if datastore.dir_exists(request.user.username, path):
            directories, files = datastore.list_user_dir(
                request.user.username, path)
            directories_data = []
            for d in directories:
                dpath = os.path.join(path, d)
                created_time = datastore.get_created_time(
                    request.user.username, dpath)
                size = datastore.size(request.user.username, dpath)
                directories_data.append(
                    {
                        "name": d,
                        "path": dpath,
                        "created_time": created_time,
                        "size": size,
                        "hidden": dpath == TMP_INPUT_FILE_UPLOAD_DIR,
                    }
                )
            files_data = []
            for f in files:
                user_rel_path = os.path.join(path, f)
                if not datastore.exists(request.user.username, user_rel_path):
                    logger.warning(f"listdir skipping {request.user.username}:{user_rel_path}, "
                                   "does not exist (broken symlink?)")
                    continue
                created_time = datastore.get_created_time(
                    request.user.username, user_rel_path
                )
                size = datastore.size(request.user.username, user_rel_path)
                full_path = datastore.path(request.user.username, user_rel_path)
                data_product_uri = _get_data_product_uri(request, full_path)

                data_product = request.airavata_client.getDataProduct(
                    request.authz_token, data_product_uri)
                mime_type = None
                if 'mime-type' in data_product.productMetadata:
                    mime_type = data_product.productMetadata['mime-type']
                files_data.append(
                    {
                        "name": f,
                        "path": user_rel_path,
                        "data-product-uri": data_product_uri,
                        "created_time": created_time,
                        "mime_type": mime_type,
                        "size": size,
                        "hidden": False,
                    }
                )
            return directories_data, files_data
        else:
            raise ObjectDoesNotExist("User storage path does not exist")


class MFTApiUserStorageProvider(UserStorageProvider):
    def __init__(self) -> None:
        super().__init__()

    def dir_exists(self, path):
        channel = grpc.insecure_channel('localhost:7004')
        stub = MFTApi_pb2_grpc.MFTApiServiceStub(channel)
        request = MFTApi_pb2.FetchResourceMetadataRequest(
            resourceId="remote-ssh-dir-resource",
            resourceType="SCP",
            resourceToken="local-ssh-cred",
            resourceBackend="FILE",
            resourceCredentialBackend="FILE",
            targetAgentId="agent0",
            childPath=path,
            mftAuthorizationToken="user token")
        response = stub.getDirectoryResourceMetadata(request)

    def listdir(self, request, path):
        # TODO setup resourceId, etc from __init__ arguments
        channel = grpc.insecure_channel('localhost:7004')
        stub = MFTApi_pb2_grpc.MFTApiServiceStub(channel)
        request = MFTApi_pb2.FetchResourceMetadataRequest(
            resourceId="remote-ssh-dir-resource",
            resourceType="SCP",
            resourceToken="local-ssh-cred",
            resourceBackend="FILE",
            resourceCredentialBackend="FILE",
            targetAgentId="agent0",
            childPath=path,
            mftAuthorizationToken="user token")
        response = stub.getDirectoryResourceMetadata(request)
        directories_data = []
        for d in response.directories:

            dpath = os.path.join(path, d.friendlyName)
            created_time = datetime.fromtimestamp(d.createdTime)
            # TODO MFT API doesn't report size
            size = 0
            directories_data.append(
                {
                    "name": d.friendlyName,
                    "path": dpath,
                    "created_time": created_time,
                    "size": size,
                    # TODO how to handle hidden directories or directories for
                    # staging input file uploads
                    "hidden": False
                }
            )
        # TODO implement
        files_data = []
        return directories_data, files_data
