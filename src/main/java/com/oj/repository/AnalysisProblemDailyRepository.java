package com.oj.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oj.entity.AnalysisProblemDaily;
import com.oj.entity.AnalysisProblemDailyId;

public interface AnalysisProblemDailyRepository extends JpaRepository<AnalysisProblemDaily, AnalysisProblemDailyId> {
    //复合主键，所以括号里的第二个参数是一个类
    List<AnalysisProblemDaily> findByDtOrderByProblemId(String dt);
    List<AnalysisProblemDaily> findTop50ByDtOrderByTotalDesc(String dt);
}
