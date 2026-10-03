"""Release helpers for the standalone connector."""

from ._version import __version__


def docker_image_tag(repository: str = "radnav-connector") -> str:
    return f"{repository}:{__version__}"
