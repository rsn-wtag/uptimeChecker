package com.example.uptimeChecker.Repositories;

import com.example.uptimeChecker.Entities.WebsiteDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebsiteDetailsRepository extends JpaRepository<WebsiteDetails, Integer> {
    public WebsiteDetails findByUrl(String url);


  /*  @Query(value = "SELECT " +
            "new com.example.uptimeChecker.Entities.User(u.userName, u.password , u.enabled  , u.email, u.slackId  ) " +
            "from User u inner join WebsiteUserMetaData w on u.userId=w.userId where w.wbId=?1", nativeQuery = false
    )
    public Set<User> findUserByWebId(Integer webId);*/

}
