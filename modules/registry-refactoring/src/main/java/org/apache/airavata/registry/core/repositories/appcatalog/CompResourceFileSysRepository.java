package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.computeresource.FileSystems;
import org.apache.airavata.registry.core.entities.appcatalog.ComputeResourceFileSystemEntity;
import org.apache.airavata.registry.core.entities.appcatalog.ComputeResourceFileSystemPK;

public class CompResourceFileSysRepository extends AppCatAbstractRepository<FileSystems, ComputeResourceFileSystemEntity, ComputeResourceFileSystemPK> {

    public CompResourceFileSysRepository() {
        super(FileSystems.class, ComputeResourceFileSystemEntity.class);
    }
}
