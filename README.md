# radnav-demo

Adopt Python 3.12 with FastAPI, SQLAlchemy 2, Alembic, Postgres in production, and SQLite only for dev/tests. Use a monorepo with /core, /connector, /web, /deploy, and /docs, with connector shipped separately and import boundaries enforced in CI.

- Sprint 2 must end with something deployable
- Core stack: Python 3.12, FastAPI, SQLAlchemy 2, Alembic
- Postgres for production; SQLite for dev and tests only
- Monorepo layout with /core, /connector, /web, /deploy, /docs
- Connector must be buildable and shippable on its own
- Connector needs its own Dockerfile and version tag
- CI must block connector imports of core internals

Open questions:
- What schema package boundaries will be exported?
- How will web integrate with the API contract?
- What exactly goes in the initial skeleton?
Raised by: Pankaj Sharma | Resolved by: 1/2 votes
Pod: TestNewFeatures | Mission: Test the new features from this demo account

Discussion thread:
(no discussion messages)

---> Composed instruction to the agent layer:
Implement the resolved option for this decision. Reference the
decision's full discussion thread for constraints already raised
by the team.

Supervisor focus for you specifically: Implement the Python 3.12/FastAPI/SQLAlchemy/Alembic monorepo skeleton with the /core, /connector, /web, /deploy, and /docs layout, including the initial deployable app structure and clear package separation.