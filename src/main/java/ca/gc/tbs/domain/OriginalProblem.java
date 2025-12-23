package ca.gc.tbs.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("ORIGINAL_PROBLEM")
public class OriginalProblem extends Problem {

  public OriginalProblem() {
    super();
  }

  public OriginalProblem(
      String id,
      String url,
      int urlEntries,
      String deviceType,
      String browser,
      String problemDate,
      String timeStamp,
      String problemDetails,
      String language,
      String title,
      String institution,
      String theme,
      String section,
      String oppositeLang,
      String contact) {
    super(
        id,
        url,
        urlEntries,
        deviceType,
        browser,
        problemDate,
        timeStamp,
        problemDetails,
        language,
        title,
        institution,
        theme,
        section,
        oppositeLang,
        contact);
  }

  public OriginalProblem(Problem existingProblem) {
    super(existingProblem);
  }
}
