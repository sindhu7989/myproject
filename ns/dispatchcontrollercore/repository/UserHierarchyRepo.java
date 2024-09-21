package com.straviso.ns.dispatchcontrollercore.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.straviso.ns.dispatchcontrollercore.entity.UserHierarchyModel;

public interface UserHierarchyRepo extends MongoRepository<UserHierarchyModel, String> {

}
