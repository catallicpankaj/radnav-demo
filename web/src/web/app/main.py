from __future__ import annotations

from .contract_dashboard import ContractDashboardState, render_contract_dashboard


def build_app_shell() -> str:
    """Return the default web shell for contract-aware UI surfaces.

    This keeps the non-hideable clinical banner present on relevant screens.
    """

    state = ContractDashboardState(
        cad_violations=(
            "subject_type=cad_result is rejected",
            "predicate flagged as clinical_output was rejected",
        ),
        api_error_status=422,
        api_error_detail="CAD guardrail violation: payload contains a blocked fact or predicate.",
    )
    return render_contract_dashboard(state)
