package com.tesim.dto.phone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhoneCatalogPageVO {
    @Builder.Default
    private List<PhoneNumberVO> numbers = Collections.emptyList();
    private long total;
    private int totalPages;
    private int currentPage;
}
