from __future__ import annotations

from fastapi import APIRouter
from fastapi.responses import HTMLResponse

router = APIRouter()


@router.get("/contract-dashboard", response_class=HTMLResponse)
def contract_dashboard() -> str:
    return """<!doctype html>
<html lang=\"en\">
  <head>
    <meta charset=\"utf-8\" />
    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />
    <title>AIWorkhive B-005 API v1 Contract</title>
    <style>
      :root {
        color-scheme: light;
        --bg: #ffffff;
        --panel: #f6f8fb;
        --text: #172033;
        --muted: #5c667a;
        --border: #d8e0ec;
        --accent: #2457d6;
        --good: #146c43;
        --warn: #8a5a00;
      }
      body {
        margin: 0;
        font-family: Inter, Arial, Helvetica, sans-serif;
        background: var(--bg);
        color: var(--text);
      }
      main {
        max-width: 1080px;
        margin: 0 auto;
        padding: 32px 20px 48px;
      }
      header, section, article {
        background: var(--panel);
        border: 1px solid var(--border);
        border-radius: 16px;
        padding: 20px;
        margin-bottom: 16px;
      }
      h1, h2, h3 {
        margin: 0 0 12px;
        line-height: 1.2;
      }
      p, li {
        color: var(--muted);
        line-height: 1.55;
      }
      code, pre {
        font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
        font-size: 0.95rem;
      }
      pre {
        overflow: auto;
        background: #fff;
        border: 1px solid var(--border);
        border-radius: 12px;
        padding: 16px;
      }
      .badge {
        display: inline-block;
        padding: 4px 10px;
        border-radius: 999px;
        background: #eaf1ff;
        color: var(--accent);
        font-size: 0.875rem;
        font-weight: 600;
        margin-bottom: 12px;
      }
      .grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
        gap: 12px;
      }
      .card {
        background: #fff;
        border: 1px solid var(--border);
        border-radius: 12px;
        padding: 16px;
      }
      .ok { color: var(--good); font-weight: 600; }
      .warn { color: var(--warn); font-weight: 600; }
      a { color: var(--accent); }
    </style>
  </head>
  <body>
    <main>
      <header>
        <div class=\"badge\">B-005 API v1 contract</div>
        <h1>AIWorkhive contract dashboard</h1>
        <p>
          This page summarizes the repo-committed v1 OpenAPI contract and the key UI-facing rules:
          server-driven session navigation, filtered list endpoints, artifact fact metadata, and safe
          connector ingest.
        </p>
      </header>

      <section aria-labelledby=\"endpoints-heading\">
        <h2 id=\"endpoints-heading\">Required endpoints</h2>
        <div class=\"grid\">
          <article class=\"card\">
            <h3>Sessions</h3>
            <ul>
              <li><code>GET /sessions</code> — filters and pagination</li>
              <li><code>GET /sessions/{id}/next-block</code> — server-driven next question block</li>
            </ul>
          </article>
          <article class=\"card\">
            <h3>Artifacts</h3>
            <ul>
              <li><code>GET /artifacts</code> — filters and pagination</li>
              <li><code>GET /artifacts/{name}</code> — includes fact status, confidence, and source</li>
            </ul>
          </article>
          <article class=\"card\">
            <h3>Connector ingest</h3>
            <ul>
              <li><code>POST /connector/ingest</code> only</li>
              <li>Versioned payloads with per-site token signing</li>
              <li>Schema rejects any message body fields</li>
            </ul>
          </article>
        </div>
      </section>

      <section aria-labelledby=\"contract-heading\">
        <h2 id=\"contract-heading\">Contract status</h2>
        <ul>
          <li class=\"ok\">OpenAPI document committed at <code>/docs/openapi.json</code></li>
          <li class=\"ok\">Session question flow is server-driven by role</li>
          <li class=\"ok\">List endpoints expose filters and pagination</li>
          <li class=\"ok\">Connector ingest rejects message bodies</li>
          <li class=\"warn\">Open questions remain for auth, recorded_at schema, and accessibility color specifics</li>
        </ul>
      </section>

      <section aria-labelledby=\"openapi-heading\">
        <h2 id=\"openapi-heading\">OpenAPI reference</h2>
        <p>
          Review the committed contract document here:
          <a href=\"/docs/openapi.json\">/docs/openapi.json</a>
        </p>
        <pre aria-label=\"OpenAPI endpoint summary\">{
  "GET /sessions": "filters + pagination",
  "GET /sessions/{id}/next-block": "server-driven question block",
  "GET /artifacts": "filters + pagination",
  "GET /artifacts/{name}": "fact status, confidence, source",
  "POST /connector/ingest": "versioned signed ingest; no message bodies"
}</pre>
      </section>
    </main>
  </body>
</html>"""
