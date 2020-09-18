import io
import paramiko

from airavata_sdk.clients.file_handling_client import FileHandler


def upload_files(api_server_client, credential_store_client, token, gateway_id, storage_id, storage_host,
                 username, project_name, experiment_id, local_folder_path, ):
    gateway_storage_preferance = api_server_client.get_gateway_storage_preference(token, gateway_id,
                                                                                  storage_id)

    credential = credential_store_client.get_SSH_credential(
        gateway_storage_preferance.resourceSpecificCredentialStoreToken,
        gateway_id);
    private_key_file = io.StringIO()
    private_key_file.write(credential.privateKey)
    private_key_file.seek(0)
    private_key = paramiko.RSAKey.from_private_key(private_key_file, credential.passphrase)

    file_handler = FileHandler(storage_host, 22, gateway_storage_preferance.loginUserName, credential.passphrase,
                               private_key)
    remotePath = gateway_storage_preferance.fileSystemRootLocation + "/" + username + "/" + project_name + "/" + experiment_id + "/"
    file_handler.upload_file(local_folder_path,
                             remotePath,
                             True, True)
    return remotePath


def download_files(api_server_client, credential_store_client, token, gateway_id, storage_id, storage_host,
                   username, project_name, experiment_id, local_folder_path):
    gateway_storage_preferance = api_server_client.get_gateway_storage_preference(token, gateway_id,
                                                                                  storage_id)

    credential = credential_store_client.get_SSH_credential(
        gateway_storage_preferance.resourceSpecificCredentialStoreToken,
        gateway_id);

    private_key_file = io.StringIO()
    private_key_file.write(credential.privateKey)
    private_key_file.seek(0)
    private_key = paramiko.RSAKey.from_private_key(private_key_file, credential.passphrase)

    file_handler = FileHandler(storage_host, 22, gateway_storage_preferance.loginUserName, credential.passphrase,
                               private_key)
    remotePath = gateway_storage_preferance.fileSystemRootLocation + "/" + username + "/" + project_name + "/" + experiment_id + "/"
    file_handler.download_file(remotePath, local_folder_path, True, True)
