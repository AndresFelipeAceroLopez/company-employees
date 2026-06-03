package com.companyemployees.infrastructure.persistence.mongo.mapper;

import com.companyemployees.domain.user.PermissionId;
import com.companyemployees.domain.user.Role;
import com.companyemployees.domain.user.RoleId;
import com.companyemployees.infrastructure.persistence.mongo.document.RoleDocument;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class RoleDocumentMapper {

    public RoleDocument toDocument(Role role) {
        if (role == null) return null;
        List<String> permisoIds = role.getPermisos().stream().map(PermissionId::value).toList();
        return new RoleDocument(
                role.getId() != null ? role.getId().value() : null,
                role.getNombre(),
                permisoIds
        );
    }

    public Role toDomain(RoleDocument document) {
        if (document == null) return null;
        Set<PermissionId> permisos = new LinkedHashSet<>();
        if (document.getPermisos() != null) {
            for (String id : document.getPermisos()) {
                permisos.add(new PermissionId(id));
            }
        }
        return new Role(new RoleId(document.getId()), document.getNombre(), permisos);
    }
}
