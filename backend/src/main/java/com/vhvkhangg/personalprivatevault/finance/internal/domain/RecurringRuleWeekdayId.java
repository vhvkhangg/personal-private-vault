package com.vhvkhangg.personalprivatevault.finance.internal.domain;

import com.vhvkhangg.personalprivatevault.finance.enums.DayOfWeek;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite identifier for {@code recurring_rule_weekdays}.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RecurringRuleWeekdayId implements Serializable {

    @Column(name = "recurring_rule_id", nullable = false)
    private Long recurringRuleId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "weekday", nullable = false)
    private DayOfWeek weekday;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecurringRuleWeekdayId that)) return false;
        return Objects.equals(recurringRuleId, that.recurringRuleId) && weekday == that.weekday;
    }

    @Override
    public int hashCode() {
        return Objects.hash(recurringRuleId, weekday);
    }
}
