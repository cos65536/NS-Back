package YUNS_Backend.YUNS.repository;

import YUNS_Backend.YUNS.entity.Reservation;
import YUNS_Backend.YUNS.entity.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r JOIN FETCH r.user JOIN FETCH r.notebook WHERE r.type = :type")
    List<Reservation> findAllByType(@Param("type") Type type);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.user JOIN FETCH r.notebook")
    List<Reservation> findAllWithDetails();

    @Query("SELECT r FROM Reservation r WHERE r.user.studentNumber = :studentNumber")
    Optional<Reservation> findByStudentNumber(@Param("studentNumber") String studentNumber);
}
