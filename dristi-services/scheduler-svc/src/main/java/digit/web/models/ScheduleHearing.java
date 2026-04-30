package digit.web.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.tracer.model.Error;

import java.time.OffsetDateTime;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScheduleHearing {

    @JsonProperty("hearingBookingId")
    private String hearingBookingId;

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("courtId")
    private String courtId;

    @JsonProperty("judgeId")
    private String judgeId;

    @JsonProperty("caseId")
    private String caseId;

    @JsonProperty("hearingType")
    private String hearingType;

    @JsonProperty("title")
    private String title;

    @JsonProperty("caseStage")
    private String caseStage;

    @JsonProperty("description")
    private String description;

    @JsonProperty("status")
    private String status;

    @JsonProperty("hearingDate")
    private OffsetDateTime hearingDate;

    @JsonProperty("startTime")
    private OffsetDateTime startTime;

    @JsonProperty("endTime")
    private OffsetDateTime endTime;

    @JsonProperty("expiryTime")
    private OffsetDateTime expiryTime;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails;

    @JsonProperty("rowVersion")
    private Integer rowVersion = null;

    @JsonProperty("error")
    @JsonIgnore
    private Error errors = null;

    @JsonProperty("rescheduleRequestId")
    private String rescheduleRequestId = null;

    @JsonProperty("hearingTimeInMinutes")
    private Integer hearingTimeInMinutes = null;

    //  copy constructor
    public ScheduleHearing(ScheduleHearing hearingObject) {
        this.hearingBookingId = hearingObject.hearingBookingId;
        this.tenantId = hearingObject.tenantId;
        this.courtId = hearingObject.courtId;
        this.judgeId = hearingObject.judgeId;
        this.caseId = hearingObject.caseId;
        this.hearingType = hearingObject.hearingType;
        this.title = hearingObject.title;
        this.description = hearingObject.description;
        this.status = hearingObject.status;
        this.startTime = hearingObject.startTime;
        this.endTime = hearingObject.endTime;
        this.auditDetails = hearingObject.auditDetails;
        this.rowVersion = hearingObject.rowVersion;
        this.errors = hearingObject.errors;
        this.rescheduleRequestId = hearingObject.rescheduleRequestId;
        this.hearingTimeInMinutes = hearingObject.hearingTimeInMinutes;
        this.caseStage = hearingObject.caseStage;
    }

    public boolean overlapsWith(ScheduleHearing other) {
        if (startTime == null || endTime == null || other.startTime == null || other.endTime == null) return false;
        return !(startTime.compareTo(other.endTime) >= 0 || endTime.compareTo(other.startTime) <= 0);

    }

    public boolean isBefore(long time1, long time2) {
        return time1 < time2;
    }

    public boolean isAfter(long time1, long time2) {
        return time1 > time2;
    }

}
