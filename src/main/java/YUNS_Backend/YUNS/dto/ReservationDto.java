package YUNS_Backend.YUNS.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReservationDto {

    @Getter
    @Builder
    public static class ReservationRequest {
        private Long reservationId;
        private String requestDate;
        private Long userId;
        private String name;
        private Long notebookId;
        private String studentNumber;
        private String phoneNumber;
        private String notebookModel;
    }

    @Getter
    @Builder
    public static class ReservationResponse {
        private List<ReservationRequest> reservationRequests;
    }
}
