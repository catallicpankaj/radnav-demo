from __future__ import annotations

from dataclasses import dataclass, field
from typing import Iterable, Sequence

from .cad_guardrails_banner import (
    render_cad_guardrails_banner,
    render_cad_violation_message,
)


@dataclass(frozen=True)
class ContractDashboardState:
    title: str = "Contract Dashboard"
    subtitle: str = "Review API guardrails and ingestion constraints"
    cad_violations: Sequence[str] = field(default_factory=tuple)
    api_error_status: int | None = None
    api_error_detail: str | None = None


def render_contract_dashboard(state: ContractDashboardState | None = None) -> str:
    state = state or ContractDashboardState()

    banner = render_cad_guardrails_banner(state.cad_violations)
    error_panel = ""
    if state.api_error_status is not None and state.api_error_detail:
        error_panel = render_cad_violation_message(
            state.api_error_status,
            state.api_error_detail,
        )

    return (
        '<main class="contract-dashboard">'
        f"<header><h1>{_escape_html(state.title)}</h1>"
        f"<p>{_escape_html(state.subtitle)}</p></header>"
        f"{banner}"
        f"{error_panel}"
        '<section class="contract-dashboard__rules">'
        "<h2>Active guardrails</h2>"
        "<ul>"
        "<li>Append-only fact rows with supersedes chains</li>"
        "<li>Evidence fields: evidence_text and evidence_ref</li>"
        "<li>Unknown predicates are rejected unless present in the registry</li>"
        "<li>Redaction runs before extraction and fails closed</li>"
        "<li>Raw transcripts are never retained; store redacted-only transcripts</li>"
        "<li>CAD facts and outputs are rejected with 422</li>"
        "</ul>"
        "</section>"
        "</main>"
    )


def render_cad_violation_example(violations: Iterable[str]) -> str:
    return render_cad_guardrails_banner(list(violations))


def _escape_html(value: str) -> str:
    return (
        value.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace('"', "&quot;")
        .replace("'", "&#39;")
    )
