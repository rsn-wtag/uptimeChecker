package com.example.uptimeChecker.Repositories;

import com.example.uptimeChecker.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {
     boolean existsUserByEmail(String email);
     User findUserByUserName(String userName);

     boolean existsUserByUserName(String userName);
}
