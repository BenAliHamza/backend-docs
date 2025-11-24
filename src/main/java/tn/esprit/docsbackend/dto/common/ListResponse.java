package tn.esprit.docsbackend.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ListResponse<T> {

    private List<T> items;

    private long total;

    /**
     * Convenience factory: builds a ListResponse with total = items.size().
     */
    public static <T> ListResponse<T> of(List<T> items) {
        long total = (items != null) ? items.size() : 0L;
        return new ListResponse<>(items, total);
    }

    /**
     * Optional overload if you ever want a custom total (e.g. for pagination).
     */
    public static <T> ListResponse<T> of(List<T> items, long total) {
        return new ListResponse<>(items, total);
    }
}
