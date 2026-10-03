package com.example.demo.connector.core_boundary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AppendOnlyFactStoreIntegrationTest {

    @Test
    void appendFact_happyPath_persistsImmutableCandidateRowWithEvidenceAndActor() {
        AppendOnlyFactStore store = new AppendOnlyFactStore(java.util.List.of("status", "owner"));

        var recordedAt = LocalDateTime.of(2026, 1, 2, 3, 4, 5).atOffset(ZoneOffset.UTC).toZonedDateTime().toInstant();
        var row = store.appendFact(
                factId:="fact-1",
                predicate:="status",
                value:="open",
                actor:="user-123",
                recorded_at:=recordedAt,
                evidence_text:="Source document says open",
                evidence_ref:=null,
                supersedes_id:=null,
                status:="candidate"
        );

        assertEquals("fact-1", row.factId());
        assertEquals("status", row.predicate());
        assertEquals("open", row.value());
        assertEquals("user-123", row.actor());
        assertEquals(recordedAt, row.recordedAt());
        assertEquals("Source document says open", row.evidenceText());
        assertEquals(null, row.evidenceRef());
        assertEquals(null, row.supersedesId());
        assertEquals("candidate", row.status());
        assertEquals(1, store.rows().size());
    }

    @Test
    void appendFact_rejectsUnknownPredicate_andRequiresEvidence() {
        AppendOnlyFactStore store = new AppendOnlyFactStore(java.util.List.of("status"));

        var recordedAt = LocalDateTime.of(2026, 2, 3, 4, 5, 6).atOffset(ZoneOffset.UTC).toZonedDateTime().toInstant();

        assertThrows(UnknownPredicateError.class, () -> store.appendFact(
                factId:="fact-unknown",
                predicate:="not-registered",
                value:="value",
                actor:="user-123",
                recorded_at:=recordedAt,
                evidence_text:="evidence",
                evidence_ref:=null,
                supersedes_id:=null,
                status:="candidate"
        ));

        assertThrows(IllegalArgumentException.class, () -> store.appendFact(
                factId:="fact-no-evidence",
                predicate:="status",
                value:="value",
                actor:="user-123",
                recorded_at:=recordedAt,
                evidence_text:=null,
                evidence_ref:=null,
                supersedes_id:=null,
                status:="candidate"
        ));
    }

    @Test
    void supersedeFact_createsNewRowLinkedToPriorRow_andRejectsNonCandidateStatus() {
        AppendOnlyFactStore store = new AppendOnlyFactStore(java.util.List.of("status"));

        var firstRecordedAt = LocalDateTime.of(2026, 3, 4, 5, 6, 7).atOffset(ZoneOffset.UTC).toZonedDateTime().toInstant();
        var first = store.appendFact(
                factId:="fact-1",
                predicate:="status",
                value:="draft",
                actor:="user-1",
                recorded_at:=firstRecordedAt,
                evidence_text:="Initial evidence",
                evidence_ref:=null,
                supersedes_id:=null,
                status:="candidate"
        );

        var secondRecordedAt = LocalDateTime.of(2026, 3, 5, 5, 6, 7).atOffset(ZoneOffset.UTC).toZonedDateTime().toInstant();
        var superseding = store.supersedeFact(
                factId:="fact-1",
                newFactId:="fact-2",
                actor:="user-2",
                recorded_at:=secondRecordedAt,
                value:="published",
                evidence_text:="Updated evidence",
                evidence_ref:=null
        );

        assertEquals("fact-2", superseding.factId());
        assertEquals("status", superseding.predicate());
        assertEquals("published", superseding.value());
        assertEquals("fact-1", superseding.supersedesId());
        assertEquals(2, store.rows().size());
        assertEquals(first.factId(), store.rows().get(0).factId());
        assertEquals(superseding.factId(), store.rows().get(1).factId());

        var invalidStatusRecordedAt = LocalDateTime.of(2026, 3, 6, 5, 6, 7).atOffset(ZoneOffset.UTC).toZonedDateTime().toInstant();
        assertThrows(IllegalArgumentException.class, () -> store.appendFact(
                factId:="fact-3",
                predicate:="status",
                value:="approved",
                actor:="ai",
                recorded_at:=invalidStatusRecordedAt,
                evidence_text:="AI proposed approval",
                evidence_ref:=null,
                supersedes_id:=null,
                status:="approved"
        ));
    }

    @Test
    void appendFact_rejectsUnknownSupersedesReference() {
        AppendOnlyFactStore store = new AppendOnlyFactStore(java.util.List.of("status"));
        var recordedAt = LocalDateTime.of(2026, 4, 5, 6, 7, 8).atOffset(ZoneOffset.UTC).toZonedDateTime().toInstant();

        assertThrows(IllegalArgumentException.class, () -> store.appendFact(
                factId:="fact-1",
                predicate:="status",
                value:="open",
                actor:="user-1",
                recorded_at:=recordedAt,
                evidence_text:="evidence",
                evidence_ref:=null,
                supersedes_id:="missing-fact",
                status:="candidate"
        ));
    }
}
