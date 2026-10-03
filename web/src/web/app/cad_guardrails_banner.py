from __future__ import annotations

from typing import Iterable


def render_cad_guardrails_banner(violations: Iterable[str] | None = None) -> str:
    """Return HTML for the mandatory non-hideable clinical-safety banner.

    The banner is intentionally always rendered and does not include dismiss/
    hide controls. If CAD-related violations are present, they are shown in a
    clear, high-priority list for users.
    """

    items = list(violations or [])
    violation_list = ""
    if items:
        escaped_items = "".join(f"<li>{_escape_html(item)}</li>" for item in items)
        violation_list = (
            '<div class="cad-violations" aria-live="polite">'
            '<p class="cad-violations__title">CAD blocking errors</p>'
            f"<ul>{escaped_items}</ul>"
            "</div>"
        )

    return (
        '<section class="clinical-tool-banner" role="note" '
        'aria-label="Not a clinical tool">'
        '<strong>Not a clinical tool.</strong> '
        'This interface is for testing and workflow validation only. '
        'Do not use it for diagnosis, treatment, or clinical decision-making.'
        f"{violation_list}"
        "</section>"
    )


def render_cad_violation_message(status_code: int, detail: str) -> str:
    """Render a clear UI message for CAD-blocking API responses.

    The API contract requires a 422 response for CAD violations. This helper
    makes that failure state explicit and readable in the UI.
    """

    safe_detail = _escape_html(detail)
    return (
        '<section class="api-error api-error--cad" role="alert" '
        'aria-live="assertive">'
        f"<h2>Request blocked by CAD guardrails (HTTP {status_code})</h2>"
        f"<p>{safe_detail}</p>"
        "<p>Predicates flagged as clinical_output or CAD output, and any facts"
        " with subject_type=cad_result, are rejected by design.</p>"
        "</section>"
    )


def _escape_html(value: str) -> str:
    return (
        value.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace('"', "&quot;")
        .replace("'", "&#39;")
    )
