package com.example.uptimeChecker.Repositories;

import com.example.uptimeChecker.Entities.WebsiteUserMetaData;
import com.example.uptimeChecker.Entities.WebsiteUserMetaData_PK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebsiteUserMetaDataRepository extends JpaRepository<WebsiteUserMetaData, WebsiteUserMetaData_PK> {
     boolean existsByWebsiteUserMetaDataPkWbId(Integer wbId);

}
