from fastapi.testclient import TestClient

from ppv_rag.main import app

client = TestClient(app)


def test_health_endpoint() -> None:
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "UP"
    assert data["version"] == "0.1.0"
