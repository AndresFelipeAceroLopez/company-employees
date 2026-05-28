package com.companyemployees.infrastructure.persistence.mongo.mapper;

import com.companyemployees.domain.company.CompanyId;
import com.companyemployees.domain.user.Role;
import com.companyemployees.domain.user.User;
import com.companyemployees.domain.user.UserId;
import com.companyemployees.infrastructure.persistence.mongo.document.UserDocument;
import org.springframework.stereotype.Component;

@Component
public class UserDocumentMapper {

    public UserDocument toDocument(User user) {
        if (user == null) return null;
        return new UserDocument(
                user.getId() != null ? user.getId().value() : null,
                user.getNombre(),
                user.getCorreo(),
                user.getPasswordHash(),
                user.getRole().name(),
                user.getCompaniaId() != null ? user.getCompaniaId().value() : null,
                user.getFechaCreacion()
        );
    }

    public User toDomain(UserDocument document) {
        if (document == null) return null;
        return new User(
                new UserId(document.getId()),
                document.getNombre(),
                document.getCorreo(),
                document.getPasswordHash(),
                Role.valueOf(document.getRole()),
                document.getCompaniaId() != null ? new CompanyId(document.getCompaniaId()) : null,
                document.getFechaCreacion()
        );
    }
}
