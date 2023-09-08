package com.factura.demo.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.factura.demo.pojo.User;

@Repository
public interface UserDao extends JpaRepository<User,Integer>{

    User findByEmail(@Param(("email")) String email);


    
}
