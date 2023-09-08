package com.factura.demo.service.impl;

import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.factura.demo.constante.FacturaConstantes;
import com.factura.demo.dao.UserDao;
import com.factura.demo.pojo.User;
import com.factura.demo.security.CustomerDetailsService;
import com.factura.demo.security.jwt.JwtUtil;
import com.factura.demo.service.UserService;
import com.factura.demo.util.FacturaUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserServiceImpl implements	UserService{


    
    @Autowired
    private UserDao userDao;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomerDetailsService customerDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
       log.info("Registro interno de un usuario",requestMap);
       try {

            if(validateSignUpMap(requestMap)){
                User user = userDao.findByEmail(requestMap.get("email"));
                if(Objects.isNull(user)){
                    userDao.save(getUserFromMap(requestMap));
                    return FacturaUtils.getResponseEntity("Usuario Registrado con exito", HttpStatus.CREATED);
                }else{
                    return FacturaUtils.getResponseEntity("El usuario con ese email ya existe", HttpStatus.BAD_REQUEST);
                }
            }else{
                return FacturaUtils.getResponseEntity(FacturaConstantes.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
        
       } catch (Exception e) {
        e.printStackTrace();
       }
       return FacturaUtils.getResponseEntity(FacturaConstantes.SOMETHING_WENT_STRING, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        log.info("Dentro del login");
        try {

            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestMap.get("email"), requestMap.get("password"))
            );

            if(authentication.isAuthenticated()){
                if(customerDetailsService.getUserDetail().getStatus().equalsIgnoreCase("true")){
                    return new ResponseEntity<String>(
                        "{\"token\":\"" + 
                    jwtUtil.generateToken(customerDetailsService.getUserDetail().getEmail(), 
                    customerDetailsService.getUserDetail().getRole()) + "\"}",
                    HttpStatus.OK);
                }else{
                     return new ResponseEntity<String>("{\"mensaje\":\""+" Espere la aprobacion del administrador "+"\"}",HttpStatus.BAD_REQUEST);
                }
            }
            
        } catch (Exception e) {
            log.error("{}",e);
        }
        return new ResponseEntity<String>("{\"mensaje\":\""+" Credenciales incorrectas "+"\"}",HttpStatus.BAD_REQUEST);
    }

    private boolean validateSignUpMap(Map<String, String> requestMap){
        if(requestMap.containsKey("nombre") && requestMap.containsKey("numeroDeContacto") && requestMap.containsKey("email") && requestMap.containsKey("password")){
            return true;
        }
        return false;
    }

    private User getUserFromMap(Map<String, String> requestMap){
        User user = new User();

        user.setNombre(requestMap.get("nombre"));
        user.setNumeroDeContacto(requestMap.get("numeroDeContacto"));
        user.setEmail(requestMap.get("email"));
        user.setPassword(requestMap.get("password"));
        user.setStatus("false");
        user.setRole("user");

        return user;

    }

    
    
}
