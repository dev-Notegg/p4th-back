package com.p4th.backend.mapper;

import com.p4th.backend.domain.Report;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReportMapper {

    /**
     * 신고 정보를 삽입한다.
     *
     * @param report 신고 정보 객체
     * @return 삽입된 레코드 수
     */
    int insertReport(Report report);

    /**
     * 신고 ID를 기준으로 신고 정보를 조회한다.
     *
     * @param reportId 신고 ID
     * @return 신고 정보, 없으면 null
     */
    Report getReportById(@Param("reportId") String reportId);
}
