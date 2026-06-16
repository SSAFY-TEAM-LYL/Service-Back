package com.lyl.infrastructure.problem;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyl.domain.problem.ProblemBankProblemRepository;
import com.lyl.domain.problem.ProblemConstraint;
import com.lyl.domain.problem.ProblemDetail;
import com.lyl.domain.problem.ProblemJudgingData;
import com.lyl.domain.problem.ProblemSample;
import com.lyl.domain.problem.ProblemSummary;
import com.lyl.domain.problem.ProblemTestCase;
import com.lyl.domain.problem.exception.ProblemBankUnavailableException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Primary
@ConditionalOnBean(name = "problemBankJdbcTemplate")
public class ProblemBankJdbcProblemRepository implements ProblemBankProblemRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProblemBankJdbcProblemRepository(
            @Qualifier("problemBankJdbcTemplate") NamedParameterJdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ProblemSummary> findPublishedSummaries(int offset, int size) {
        try {
            return jdbcTemplate.query("""
                            select id, title, time_limit_ms, created_at
                            from problems
                            order by created_at desc, id desc
                            limit :limit offset :offset
                            """,
                    Map.of(
                            "limit", size,
                            "offset", offset
                    ),
                    (rs, rowNum) -> toSummary(rs)
            );
        } catch (DataAccessException e) {
            throw new ProblemBankUnavailableException();
        }
    }

    @Override
    public List<ProblemSummary> findSummariesByIds(List<String> problemIds) {
        if (problemIds.isEmpty()) {
            return List.of();
        }
        try {
            return jdbcTemplate.query("""
                            select id, title, time_limit_ms, created_at
                            from problems
                            where id in (:problemIds)
                            """,
                    new MapSqlParameterSource("problemIds", problemIds),
                    (rs, rowNum) -> toSummary(rs)
            );
        } catch (DataAccessException e) {
            throw new ProblemBankUnavailableException();
        }
    }

    @Override
    public Optional<ProblemDetail> findDetailById(String problemId) {
        try {
            List<ProblemDetail> details = jdbcTemplate.query("""
                            select id, title, description, input_format, output_format,
                                   constraints, samples, time_limit_ms
                            from problems
                            where id = :problemId
                            """,
                    Map.of("problemId", problemId),
                    (rs, rowNum) -> toDetail(rs)
            );
            return details.stream().findFirst();
        } catch (DataAccessException e) {
            throw new ProblemBankUnavailableException();
        }
    }

    @Override
    public Optional<ProblemJudgingData> findJudgingDataById(String problemId) {
        try {
            List<Integer> timeLimits = jdbcTemplate.query("""
                            select time_limit_ms
                            from problems
                            where id = :problemId
                            """,
                    Map.of("problemId", problemId),
                    (rs, rowNum) -> rs.getObject("time_limit_ms", Integer.class)
            );
            if (timeLimits.isEmpty()) {
                return Optional.empty();
            }
            List<ProblemTestCase> testCases = jdbcTemplate.query("""
                            select seq, input, expected, category
                            from test_cases
                            where problem_id = :problemId
                            order by seq asc
                            """,
                    Map.of("problemId", problemId),
                    (rs, rowNum) -> new ProblemTestCase(
                            rs.getInt("seq"),
                            rs.getString("input"),
                            rs.getString("expected"),
                            rs.getString("category")
                    )
            );
            return Optional.of(new ProblemJudgingData(problemId, timeLimits.getFirst(), testCases));
        } catch (DataAccessException e) {
            throw new ProblemBankUnavailableException();
        }
    }

    private ProblemSummary toSummary(ResultSet rs) throws SQLException {
        return new ProblemSummary(
                rs.getString("id"),
                rs.getString("title"),
                rs.getObject("time_limit_ms", Integer.class),
                rs.getObject("created_at", OffsetDateTime.class)
        );
    }

    private ProblemDetail toDetail(ResultSet rs) throws SQLException {
        return new ProblemDetail(
                rs.getString("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("input_format"),
                rs.getString("output_format"),
                parseConstraints(rs.getString("constraints")),
                parseSamples(rs.getString("samples")),
                rs.getObject("time_limit_ms", Integer.class)
        );
    }

    private List<ProblemConstraint> parseConstraints(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<ProblemConstraintJson>>() {
                    }).stream()
                    .map(item -> new ProblemConstraint(
                            item.name(),
                            item.minValue(),
                            item.maxValue(),
                            item.description()
                    ))
                    .toList();
        } catch (JsonProcessingException e) {
            throw new ProblemBankUnavailableException();
        }
    }

    private List<ProblemSample> parseSamples(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<ProblemSampleJson>>() {
                    }).stream()
                    .map(item -> new ProblemSample(
                            item.inputText(),
                            item.expectedOutput(),
                            item.description()
                    ))
                    .toList();
        } catch (JsonProcessingException e) {
            throw new ProblemBankUnavailableException();
        }
    }

    private record ProblemConstraintJson(
            String name,
            @JsonProperty("min_value") Long minValue,
            @JsonProperty("max_value") Long maxValue,
            String description
    ) {
    }

    private record ProblemSampleJson(
            @JsonProperty("input_text") String inputText,
            @JsonProperty("expected_output") String expectedOutput,
            String description
    ) {
    }
}
