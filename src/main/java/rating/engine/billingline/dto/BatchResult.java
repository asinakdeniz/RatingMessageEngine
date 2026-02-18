package rating.engine.billingline.dto;

import java.util.List;

public record BatchResult(List<Long> successIds, List<Long> failedIds) {
}
