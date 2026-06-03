package com.companyemployees.infrastructure.persistence.mongo.mapper;

import com.companyemployees.domain.company.CompanyId;
import com.companyemployees.domain.user.PermissionId;
import com.companyemployees.domain.user.RoleId;
import com.companyemployees.domain.user.User;
import com.companyemployees.domain.user.UserId;
import com.companyemployees.infrastructure.persistence.mongo.document.UserDocument;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class UserDocumentMapper {

    public UserDocument toDocument(User user) {
        if (user == null) return null;
        List<String> roleIds = user.getRoles().stream().map(RoleId::value).toList();
        List<String> permisoIds = user.getPermisos().stream().map(PermissionId::value).toList();
        return new UserDocument(
                user.getId() != null ? user.getId().value() : null,
                user.getNombre(),
                user.getCorreo(),
                user.getPasswordHash(),
                roleIds,
                permisoIds,
                user.getCompaniaId() != null ? user.getCompaniaId().value() : null,
                user.getFechaCreacion()
        );
    }

    public User toDomain(UserDocument document) {
        if (document == null) return null;
        Set<RoleId> roles = new LinkedHashSet<>();
        if (document.getRoles() != null) {
            for (String id : document.getRoles()) {
                roles.add(new RoleId(id));
            }
        }
        Set<PermissionId> permisos = new LinkedHashSet<>();
        if (document.getPermisos() != null) {
            for (String id : document.getPermisos()) {
                permisos.add(new PermissionId(id));
            }
        }
        return new User(
                new UserId(document.getId()),
                document.getNombre(),
                document.getCorreo(),
                document.getPasswordHash(),
                roles,
                permisos,
                document.getCompaniaId() != null ? new CompanyId(document.getCompaniaId()) : null,
                document.getFechaCreacion()
        );
    }
}
