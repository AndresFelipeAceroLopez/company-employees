package com.companyemployees.infrastructure.persistence.mongo.mapper;

import com.companyemployees.domain.user.Permission;
import com.companyemployees.domain.user.PermissionId;
import com.companyemployees.infrastructure.persistence.mongo.document.PermissionDocument;
import org.springframework.stereotype.Component;

@Component
public class PermissionDocumentMapper {

    public PermissionDocument toDocument(Permission permission) {
        if (permission == null) return null;
        return new PermissionDocument(
                permission.getId() != null ? permission.getId().value() : null,
                permission.getScope()
        );
    }

    public Permission toDomain(PermissionDocument document) {
        if (document == null) return null;
        return new Permission(new PermissionId(document.getId()), document.getScope());
    }
}
