package com.example.teamcity.api.requests;

public interface CrudInterface {
    public Object create(Object object);

    public Object get(String id);

    public Object update(Object object);

    public Object delete(String id);
}
