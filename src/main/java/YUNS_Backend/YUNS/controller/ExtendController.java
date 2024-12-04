package YUNS_Backend.YUNS.controller;

import YUNS_Backend.YUNS.dto.RentalDto;
import YUNS_Backend.YUNS.dto.ReservationDto;
import YUNS_Backend.YUNS.entity.Rental;
import YUNS_Backend.YUNS.entity.Reservation;
import YUNS_Backend.YUNS.entity.Type;
import YUNS_Backend.YUNS.repository.RentalRepository;
import YUNS_Backend.YUNS.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/request")
public class ExtendController {

    private final ReservationRepository reservationRepository;
    private final RentalRepository rentalRepository;

    @GetMapping("/extend")
    public ResponseEntity<ReservationDto.ReservationResponse> getExtendRequests() {
        // Lazy 로딩 문제 해결: 즉시 로딩(JOIN FETCH) 쿼리 사용
        List<Reservation> reservations = reservationRepository.findAllByType(Type.EXTEND);

        // Reservation -> DTO 변환
        List<ReservationDto.ReservationRequest> extendRequests = reservations.stream()
                .map(reservation -> ReservationDto.ReservationRequest.builder()
                        .reservationId(reservation.getReservationId())
                        .requestDate(reservation.getRequestDate().toString())
                        .userId(reservation.getUser().getUserId())
                        .name(reservation.getUser().getName())
                        .notebookId(reservation.getNotebook().getNotebookId())
                        .build())
                .collect(Collectors.toList());

        ReservationDto.ReservationResponse response = ReservationDto.ReservationResponse.builder()
                .reservationRequests(extendRequests)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/approve/{reservationId}")
    public ResponseEntity<Object> approveExtendRequest(@PathVariable Long reservationId, @RequestBody RentalDto.RentalApprovalRequest request) {
        // 1. 예약 정보 조회
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElse(null);

        if (reservation == null) {
            return ResponseEntity.status(404).body("해당 예약 정보를 찾을 수 없습니다.");
        }

        if (request.getType().equalsIgnoreCase("EXTEND")) {
            // 2. 대여 정보 조회 - studentNumber 기반으로 대여 정보 찾기
            Rental rental = rentalRepository.findByStudentNumber(reservation.getUser().getStudentNumber())
                    .orElse(null);

            if (rental == null) {
                return ResponseEntity.status(404).body("해당 사용자의 대여 정보를 찾을 수 없습니다.");
            }

            // 3. 기존 대여 종료일을 1개월 연장 (기존 Rental 객체 기반으로 builder 사용)
            Rental updatedRental = Rental.builder()
                    .rentalId(rental.getRentalId())  // 기존 rentalId 유지
                    .notebook(rental.getNotebook())
                    .user(rental.getUser())
                    .startDate(rental.getStartDate())  // 기존 시작일 유지
                    .endDate(rental.getEndDate().plusMonths(1))  // 종료일 연장
                    .build();

            rentalRepository.save(updatedRental);

        } else {
            return ResponseEntity.status(400).body("유효하지 않은 요청 타입입니다.");
        }

        // 4. 연장 요청(예약) 삭제
        reservationRepository.delete(reservation);

        Map<String, String> response = new HashMap<>();
        response.put("message", "연장 요청 승인이 완료되었습니다.");
        return ResponseEntity.ok(response);
    }

}
