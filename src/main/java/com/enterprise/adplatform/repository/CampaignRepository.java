package com.enterprise.adplatform.repository;

import com.enterprise.adplatform.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    List<Campaign> findByAdvertiserId(Long advertiserId);

    List<Campaign> findByStatus(Campaign.CampaignStatus status);

    @Query("SELECT c FROM Campaign c WHERE c.advertiserId = :advertiserId AND c.status = :status")
    List<Campaign> findByAdvertiserIdAndStatus(
            @Param("advertiserId") Long advertiserId,
            @Param("status") Campaign.CampaignStatus status);

    boolean existsByCampaignNameAndAdvertiserId(String campaignName, Long advertiserId);
}
