package ex.org.project.downloadService.repositories;

import ex.org.project.downloadService.entities.S3File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface S3FileRepository extends JpaRepository<S3File, Integer> {

    List<S3File> findByIdIn(List<Integer> fileIdList);
}
