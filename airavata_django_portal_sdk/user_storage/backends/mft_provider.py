from .base import UserStorageProvider


class MFTUserStorageProvider(UserStorageProvider):

    def exists(self, resource_path):
        return super().exists(resource_path)
#         with grpc.insecure_channel('localhost:7004') as channel:
#             # remove trailing slash and figure out parent path
#             # FIXME remove the hard coded /tmp path
#             parent_path, child_path = os.path.split(f"/tmp/{path}".rstrip("/"))
#             logger.debug(f"parent_path={parent_path}, child_path={child_path}")
#             stub = MFTApi_pb2_grpc.MFTApiServiceStub(channel)
#             # Get metadata for parent directory and see if child_path exists
#             request = MFTApi_pb2.FetchResourceMetadataRequest(
#                 resourceId="remote-ssh-dir-resource",
#                 resourceType="SCP",
#                 resourceToken="local-ssh-cred",
#                 resourceBackend="FILE",
#                 resourceCredentialBackend="FILE",
#                 targetAgentId="agent0",
#                 childPath=parent_path,
#                 mftAuthorizationToken="user token")
#             response = stub.getDirectoryResourceMetadata(request)
#             # if not child_path, then return True since the response was
#             # successful and we just need to confirm the existence of the root dir
#             if child_path == '':
#                 return True
#             return child_path in map(lambda f: f.friendlyName, response.directories)

    def get_metadata(self, resource_path):
        return super().get_metadata(resource_path)
#     def listdir(self, request, path):
#         # TODO setup resourceId, etc from __init__ arguments
#         channel = grpc.insecure_channel('localhost:7004')
#         stub = MFTApi_pb2_grpc.MFTApiServiceStub(channel)
#         request = MFTApi_pb2.FetchResourceMetadataRequest(
#             resourceId="remote-ssh-dir-resource",
#             resourceType="SCP",
#             resourceToken="local-ssh-cred",
#             resourceBackend="FILE",
#             resourceCredentialBackend="FILE",
#             targetAgentId="agent0",
#             childPath=f"/tmp/{path}",
#             mftAuthorizationToken="user token")
#         response = stub.getDirectoryResourceMetadata(request)
#         directories_data = []
#         for d in response.directories:

#             dpath = os.path.join(path, d.friendlyName)
#             created_time = datetime.fromtimestamp(d.createdTime)
#             # TODO MFT API doesn't report size
#             size = 0
#             directories_data.append(
#                 {
#                     "name": d.friendlyName,
#                     "path": dpath,
#                     "created_time": created_time,
#                     "size": size,
#                     # TODO how to handle hidden directories or directories for
#                     # staging input file uploads
#                     "hidden": False
#                 }
#             )
#         files_data = []
#         for f in response.files:
#             user_rel_path = os.path.join(path, f.friendlyName)
#             # TODO do we need to check for broken symlinks?
#             created_time = datetime.fromtimestamp(f.createdTime)
#             # TODO get the size as well
#             size = 0
#             # full_path = datastore.path(request.user.username, user_rel_path)
#             # TODO how do we register these as data products, do we need to?
#             # data_product_uri = _get_data_product_uri(request, full_path)

#             # data_product = request.airavata_client.getDataProduct(
#             #     request.authz_token, data_product_uri)
#             # mime_type = None
#             # if 'mime-type' in data_product.productMetadata:
#             #     mime_type = data_product.productMetadata['mime-type']
#             files_data.append(
#                 {
#                     "name": f.friendlyName,
#                     "path": user_rel_path,
#                     "data-product-uri": None,
#                     "created_time": created_time,
#                     "mime_type": None,
#                     "size": size,
#                     "hidden": False,
#                 }
#             )
#         return directories_data, files_data

#     def get_file(self, request, path):
#         # FIXME remove hard coded /tmp path
#         path = f"/tmp/{path}".rstrip("/")
#         file_metadata = self._get_file(path)
#         if file_metadata is not None:
#             user_rel_path = os.path.join(path, file_metadata.friendlyName)
#             created_time = datetime.fromtimestamp(file_metadata.createdTime)
#             # TODO get the size as well
#             size = 0

#             return {
#                 "name": file_metadata.friendlyName,
#                 "path": user_rel_path,
#                 "data-product-uri": None,
#                 "created_time": created_time,
#                 "mime_type": None,
#                 "size": size,
#                 "hidden": False,
#             }
#         else:
#             raise ObjectDoesNotExist("User storage file path does not exist")

#     def _get_file(self, path):
#         with grpc.insecure_channel('localhost:7004') as channel:
#             stub = MFTApi_pb2_grpc.MFTApiServiceStub(channel)
#             # Get metadata for parent directory and see if child_path exists
#             request = MFTApi_pb2.FetchResourceMetadataRequest(
#                 resourceId="remote-ssh-dir-resource",
#                 resourceType="SCP",
#                 resourceToken="local-ssh-cred",
#                 resourceBackend="FILE",
#                 resourceCredentialBackend="FILE",
#                 targetAgentId="agent0",
#                 childPath=path,
#                 mftAuthorizationToken="user token")
#             try:
#                 # TODO is there a better way to check if file exists than catching exception?
#                 return stub.getFileResourceMetadata(request)
#             except Exception:
#                 logger.exception(f"_get_file({path})")
#                 return None

#     def _get_download_url(self, path):

#         with grpc.insecure_channel('localhost:7004') as channel:
#             stub = MFTApi_pb2_grpc.MFTApiServiceStub(channel)
#             download_request = MFTApi_pb2.HttpDownloadApiRequest(sourceStoreId="remote-ssh-storage",
#                                                                  sourcePath="/tmp/a.txt",
#                                                                  sourceToken="local-ssh-cred",
#                                                                  sourceType="SCP",
#                                                                  targetAgent="agent0",
#                                                                  mftAuthorizationToken="")
#             try:
#                 # TODO is there a better way to check if file exists than catching exception?
#                 # response stub.submitHttpDownload(request)
#                 pass
#             except Exception:
#                 logger.exception(f"_get_file({path})")
#                 return None
