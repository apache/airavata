import importlib

from airavata_sdk.transport.utils import create_resource_service_stub, create_user_storage_service_stub


class StorageClient:
    """Storage resource CRUD and data movement operations."""

    def __init__(self, channel, metadata, gateway_id):
        self._metadata = metadata
        self._gateway_id = gateway_id
        self._resource = create_resource_service_stub(channel)
        self._user_storage = create_user_storage_service_stub(channel)

    @staticmethod
    def _svc(name):
        return importlib.import_module(f"airavata_sdk.generated.services.{name}")

    # ================================================================
    # Storage Resource CRUD
    # ================================================================

    def register_storage_resource(self, storage_resource):
        pb2 = self._svc("resource_service_pb2")
        response = self._resource.RegisterStorageResource(
            pb2.RegisterStorageResourceRequest(storage_resource=storage_resource),
            metadata=self._metadata,
        )
        return response.storage_resource_id

    def get_storage_resource(self, storage_resource_id):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.GetStorageResource(
            pb2.GetStorageResourceRequest(storage_resource_id=storage_resource_id),
            metadata=self._metadata,
        )

    def update_storage_resource(self, storage_resource_id, storage_resource):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.UpdateStorageResource(
            pb2.UpdateStorageResourceRequest(storage_resource_id=storage_resource_id, storage_resource=storage_resource),
            metadata=self._metadata,
        )

    def delete_storage_resource(self, storage_resource_id):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.DeleteStorageResource(
            pb2.DeleteStorageResourceRequest(storage_resource_id=storage_resource_id),
            metadata=self._metadata,
        )

    def get_all_storage_resource_names(self):
        pb2 = self._svc("resource_service_pb2")
        response = self._resource.GetAllStorageResourceNames(
            pb2.GetAllStorageResourceNamesRequest(),
            metadata=self._metadata,
        )
        return dict(response.storage_resource_names)

    # ================================================================
    # Data Movement
    # ================================================================

    def add_local_data_movement(self, compute_resource_id, priority, dm_type, local_data_movement):
        pb2 = self._svc("resource_service_pb2")
        response = self._resource.AddLocalDataMovement(
            pb2.AddLocalDataMovementRequest(compute_resource_id=compute_resource_id, priority=priority, dm_type=dm_type, local_data_movement=local_data_movement),
            metadata=self._metadata,
        )
        return response.data_movement_id

    def update_local_data_movement(self, data_movement_id, local_data_movement):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.UpdateLocalDataMovement(
            pb2.UpdateLocalDataMovementRequest(data_movement_id=data_movement_id, local_data_movement=local_data_movement),
            metadata=self._metadata,
        )

    def get_local_data_movement(self, data_movement_id):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.GetLocalDataMovement(
            pb2.GetLocalDataMovementRequest(data_movement_id=data_movement_id),
            metadata=self._metadata,
        )

    def add_scp_data_movement(self, compute_resource_id, priority, dm_type, scp_data_movement):
        pb2 = self._svc("resource_service_pb2")
        response = self._resource.AddSCPDataMovement(
            pb2.AddSCPDataMovementRequest(compute_resource_id=compute_resource_id, priority=priority, dm_type=dm_type, scp_data_movement=scp_data_movement),
            metadata=self._metadata,
        )
        return response.data_movement_id

    def update_scp_data_movement(self, data_movement_id, scp_data_movement):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.UpdateSCPDataMovement(
            pb2.UpdateSCPDataMovementRequest(data_movement_id=data_movement_id, scp_data_movement=scp_data_movement),
            metadata=self._metadata,
        )

    def get_scp_data_movement(self, data_movement_id):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.GetSCPDataMovement(
            pb2.GetSCPDataMovementRequest(data_movement_id=data_movement_id),
            metadata=self._metadata,
        )

    def add_grid_ftp_data_movement(self, compute_resource_id, priority, dm_type, gridftp_data_movement):
        pb2 = self._svc("resource_service_pb2")
        response = self._resource.AddGridFTPDataMovement(
            pb2.AddGridFTPDataMovementRequest(compute_resource_id=compute_resource_id, priority=priority, dm_type=dm_type, gridftp_data_movement=gridftp_data_movement),
            metadata=self._metadata,
        )
        return response.data_movement_id

    def update_grid_ftp_data_movement(self, data_movement_id, gridftp_data_movement):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.UpdateGridFTPDataMovement(
            pb2.UpdateGridFTPDataMovementRequest(data_movement_id=data_movement_id, gridftp_data_movement=gridftp_data_movement),
            metadata=self._metadata,
        )

    def get_grid_ftp_data_movement(self, data_movement_id):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.GetGridFTPDataMovement(
            pb2.GetGridFTPDataMovementRequest(data_movement_id=data_movement_id),
            metadata=self._metadata,
        )

    def delete_data_movement_interface(self, compute_resource_id, data_movement_id):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.DeleteDataMovementInterface(
            pb2.DeleteDataMovementInterfaceRequest(compute_resource_id=compute_resource_id, data_movement_id=data_movement_id),
            metadata=self._metadata,
        )

    # ================================================================
    # User Storage
    # ================================================================

    def upload_file(self, path, content, name, storage_resource_id=None, content_type=""):
        pb2 = self._svc("file_service_pb2")
        return self._user_storage.UploadFile(
            pb2.UploadFileRequest(
                storage_resource_id=storage_resource_id or "",
                path=path,
                name=name,
                content_type=content_type,
                content=content,
            ),
            metadata=self._metadata,
        )

    def download_file(self, path, storage_resource_id=None):
        pb2 = self._svc("file_service_pb2")
        return self._user_storage.DownloadFile(
            pb2.DownloadFileRequest(storage_resource_id=storage_resource_id or "", path=path),
            metadata=self._metadata,
        )

    def file_exists(self, path, storage_resource_id=None):
        pb2 = self._svc("file_service_pb2")
        resp = self._user_storage.FileExists(
            pb2.FileExistsRequest(storage_resource_id=storage_resource_id or "", path=path),
            metadata=self._metadata,
        )
        return resp.exists

    def dir_exists(self, path, storage_resource_id=None):
        pb2 = self._svc("file_service_pb2")
        resp = self._user_storage.DirExists(
            pb2.DirExistsRequest(storage_resource_id=storage_resource_id or "", path=path),
            metadata=self._metadata,
        )
        return resp.exists

    def list_dir(self, path, storage_resource_id=None):
        pb2 = self._svc("file_service_pb2")
        return self._user_storage.ListDir(
            pb2.ListDirRequest(storage_resource_id=storage_resource_id or "", path=path),
            metadata=self._metadata,
        )

    def delete_file(self, path, storage_resource_id=None):
        pb2 = self._svc("file_service_pb2")
        return self._user_storage.DeleteFile(
            pb2.DeleteFileRequest(storage_resource_id=storage_resource_id or "", path=path),
            metadata=self._metadata,
        )

    def delete_dir(self, path, storage_resource_id=None):
        pb2 = self._svc("file_service_pb2")
        return self._user_storage.DeleteDir(
            pb2.DeleteDirRequest(storage_resource_id=storage_resource_id or "", path=path),
            metadata=self._metadata,
        )

    def move_file(self, source_path, dest_path, storage_resource_id=None):
        pb2 = self._svc("file_service_pb2")
        return self._user_storage.MoveFile(
            pb2.MoveFileRequest(
                storage_resource_id=storage_resource_id or "",
                source_path=source_path,
                destination_path=dest_path,
            ),
            metadata=self._metadata,
        )

    def create_dir(self, path, storage_resource_id=None):
        pb2 = self._svc("file_service_pb2")
        return self._user_storage.CreateDir(
            pb2.CreateDirRequest(storage_resource_id=storage_resource_id or "", path=path),
            metadata=self._metadata,
        )

    def create_symlink(self, source_path, dest_path, storage_resource_id=None):
        pb2 = self._svc("file_service_pb2")
        return self._user_storage.CreateSymlink(
            pb2.CreateSymlinkRequest(
                storage_resource_id=storage_resource_id or "",
                source_path=source_path,
                target_path=dest_path,
            ),
            metadata=self._metadata,
        )

    def get_file_metadata(self, path, storage_resource_id=None):
        pb2 = self._svc("file_service_pb2")
        return self._user_storage.GetFileMetadata(
            pb2.GetFileMetadataRequest(storage_resource_id=storage_resource_id or "", path=path),
            metadata=self._metadata,
        )

    def list_experiment_dir(self, experiment_id, path=""):
        pb2 = self._svc("file_service_pb2")
        return self._user_storage.ListExperimentDir(
            pb2.ListExperimentDirRequest(experiment_id=experiment_id),
            metadata=self._metadata,
        )

    def get_default_storage_resource_id(self):
        pb2 = self._svc("file_service_pb2")
        resp = self._user_storage.GetDefaultStorageResourceId(
            pb2.GetDefaultStorageResourceIdRequest(),
            metadata=self._metadata,
        )
        return resp.storage_resource_id
