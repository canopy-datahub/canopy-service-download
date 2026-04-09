package ex.org.project.downloadService.repositories;

import ex.org.project.downloadService.entities.PublicData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublicDataRepository extends JpaRepository<PublicData, Integer> {}
