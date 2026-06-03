package com.companyemployees.infrastructure.persistence.mongo.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "permissions")
public class PermissionDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String scope;

    public PermissionDocument() {}

    public PermissionDocument(String id, String scope) {
        this.id = id;
        this.scope = scope;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
}
