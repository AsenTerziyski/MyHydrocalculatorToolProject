package com.example.hydrocalc.services.impl;

import com.example.hydrocalc.model.entities.CalculatorPipeResults;
import com.example.hydrocalc.model.entities.UserEntity;
import com.example.hydrocalc.model.entities.UserRoleEntity;
import com.example.hydrocalc.model.view.CalculatorPipeResultsModelView;
import com.example.hydrocalc.repositrory.CalculatorPipeResultRepository;
import com.example.hydrocalc.repositrory.UserRepository;
import com.example.hydrocalc.services.CalcPipeResultService;
import com.example.hydrocalc.services.UserRoleService;
import com.example.hydrocalc.services.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserRoleService userRoleService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;


    public UserServiceImpl(UserRepository userRepository, UserRoleService userRoleService, PasswordEncoder passwordEncoder, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.userRoleService = userRoleService;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public void initUsers() {
        if (this.userRepository.count() == 0) {
            System.out.println();
            UserEntity admin = new UserEntity();
            List<UserRoleEntity> allRoles = this.userRoleService.findAllRoles();

            admin
                    .setFullName("admin admin")
                    .setPassword(this.passwordEncoder.encode("12345"))
                    .setUsername("admin")
                    .setRoles(allRoles);

            UserRoleEntity userRole = allRoles.stream().filter(userRoleEntity -> userRoleEntity.getRole().name().equalsIgnoreCase("user")).findFirst().orElse(null);
            UserEntity user = new UserEntity();
            user
                    .setFullName("test user 1")
                    .setPassword(this.passwordEncoder.encode("12345"))
                    .setUsername("tuser1")
                    .setRoles(List.of(userRole));
            this.userRepository.save(admin);
            this.userRepository.save(user);
        }

    }

    @Override
    public UserEntity findUserByUsername(String username) {
        return this.userRepository.findByUsername(username).orElse(null);
    }

    @Override
    @Transactional
    public List<CalculatorPipeResultsModelView> findMyCalculations(Principal principal) {
        List<CalculatorPipeResultsModelView> myResults = new LinkedList<>();
        UserEntity userEntity = this.userRepository
                .findByUsername(principal.getName())
                .orElse(null);
        if (userEntity != null) {
            List<CalculatorPipeResults> results = userEntity.getResults();
            for (CalculatorPipeResults result : results) {
                myResults.add(this.modelMapper.map(result, CalculatorPipeResultsModelView.class));
            }
        }
        return myResults;
    }

    @Override
    public boolean userIsAdmin(UserEntity caller) {
        Optional<UserEntity> byUsername = this.userRepository.findByUsername(caller.getUsername());
        if (byUsername.isEmpty()) {
            return false;
        }
        UserRoleEntity admin = byUsername
                .get()
                .getRoles()
                .stream()
                .filter(userRoleEntity -> userRoleEntity.getRole().name().equalsIgnoreCase("admin"))
                .findFirst()
                .orElse(null);
        return admin != null;
    }


}
