package com.example.uptimeChecker.Repositories;

import com.example.uptimeChecker.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {
    public boolean existsUserByEmail(String email);
    public User findUserByUserName(String userName);

    public boolean existsUserByUserName(String userName);
}
