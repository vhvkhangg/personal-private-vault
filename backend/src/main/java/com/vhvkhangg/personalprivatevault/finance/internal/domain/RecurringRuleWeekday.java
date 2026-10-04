package com.vhvkhangg.personalprivatevault.finance.internal.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Weekday assignment for recurring transaction rules mapped to {@code recurring_rule_weekdays}.
 */
@Entity
@Table(name = "recurring_rule_weekdays")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RecurringRuleWeekday {

    @EmbeddedId
    private RecurringRuleWeekdayId id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecurringRuleWeekday that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
