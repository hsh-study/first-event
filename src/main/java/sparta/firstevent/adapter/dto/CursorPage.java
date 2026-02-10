package sparta.firstevent.adapter.dto;

import lombok.Getter;
import org.springframework.util.Assert;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public class CursorPage<T> {

    private List<T> contents;

    private Long nextCursor;

    private long totalElements;

    private boolean isLast;

    public CursorPage(List<T> contents, Long nextCursor, long totalElements, boolean isLast) {
        this.contents = contents;
        this.nextCursor = nextCursor;
        this.totalElements = totalElements;
        this.isLast = isLast;
    }

    public <U> CursorPage<U> map(Function<? super T, ? extends U> converter) {
        return new CursorPage<>(getConvertedContent(converter), nextCursor, totalElements, isLast);
    }

    protected <U> List<U> getConvertedContent(Function<? super T, ? extends U> converter) {

        Assert.notNull(converter, "Function must not be null");

        return contents.stream().map(converter::apply).collect(Collectors.toList());
    }
}
