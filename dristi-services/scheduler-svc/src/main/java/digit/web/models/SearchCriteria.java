package digit.web.models;



import java.time.OffsetDateTime;

public interface SearchCriteria {

    String getTenantId();

    String getJudgeId();

    String getCourtId();

    OffsetDateTime getFromDate();

    OffsetDateTime getToDate();
}
