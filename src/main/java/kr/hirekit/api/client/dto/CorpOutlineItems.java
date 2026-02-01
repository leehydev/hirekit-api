package kr.hirekit.api.client.dto;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CorpOutlineItems {

    @JsonProperty("item")
    private List<CorpOutlineItem> item;

    @JsonProperty("numOfRows")
    private Integer numOfRows;

    @JsonProperty("pageNo")
    private Integer pageNo;

    @JsonProperty("totalCount")
    private Integer totalCount;

    /**
     * API가 단일 객체로 반환하는 경우를 위한 안전한 리스트 반환
     */
    public List<CorpOutlineItem> getItemSafe() {
        return item != null ? item : Collections.emptyList();
    }
}

