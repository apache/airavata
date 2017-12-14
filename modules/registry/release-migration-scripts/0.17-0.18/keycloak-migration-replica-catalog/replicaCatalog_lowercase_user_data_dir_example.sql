-- user_data_dir is the path to the gateway's data storage directory
set @user_data_dir = '/var/www/user_data/';
set @storage_id = '149.165.156.11_b5f26430-14d5-4372-8a7e-39b125aa640b';
update DATA_REPLICA_LOCATION
inner join (
    select
        REPLICA_ID,
        FILE_PATH,
        SUBSTR(FILE_PATH,
            LOCATE(@user_data_dir, FILE_PATH) + LENGTH(@user_data_dir),
            LOCATE('/', FILE_PATH, LOCATE(@user_data_dir, FILE_PATH) + LENGTH(@user_data_dir))
            - (LOCATE(@user_data_dir, FILE_PATH) + LENGTH(@user_data_dir))
        ) USERNAME
    from DATA_REPLICA_LOCATION where STORAGE_RESOURCE_ID = @storage_id
    and FILE_PATH like concat('%', @user_data_dir, '%')
) a
on a.REPLICA_ID = DATA_REPLICA_LOCATION.REPLICA_ID
set DATA_REPLICA_LOCATION.FILE_PATH = REPLACE(DATA_REPLICA_LOCATION.FILE_PATH, concat(@user_data_dir, a.USERNAME), concat(@user_data_dir, LOWER(a.USERNAME)));